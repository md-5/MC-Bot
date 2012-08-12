package com.md_5.bot.mc;

import com.md_5.bot.mc.entity.Entity;
import com.md_5.bot.mc.impl.BaseHandler;
import com.md_5.bot.mc.impl.BaseThread;
import com.md_5.bot.mc.impl.NetworkReader;
import com.md_5.bot.mc.impl.NetworkWriter;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URLEncoder;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.crypto.SecretKey;
import lombok.Data;
import net.minecraft.server.EnumGamemode;
import net.minecraft.server.MinecraftEncryption;
import net.minecraft.server.NetHandler;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet18ArmAnimation;
import net.minecraft.server.Packet1Login;
import net.minecraft.server.Packet205ClientCommand;
import net.minecraft.server.Packet252KeyResponse;
import net.minecraft.server.Packet253KeyRequest;
import net.minecraft.server.Packet255KickDisconnect;
import net.minecraft.server.Packet2Handshake;
import net.minecraft.server.Packet3Chat;
import net.minecraft.server.Packet7UseEntity;
import net.minecraft.server.Packet9Respawn;
import net.minecraft.server.WorldType;

@Data
public class Connection {

    public static final double STANDING_HEIGHT = 1.62;
    //
    private String host;
    private int port;
    //
    private String username;
    private String sessionId;
    private boolean isConnected;
    private String shutdownReason;
    //
    private boolean autoMoveUpdates = true;
    private int timeout = 30000;
    private Socket socket;
    //
    private BlockingQueue<Packet> receivedPackets = new LinkedBlockingQueue<Packet>();
    private BlockingQueue<Packet> queuedPackets = new LinkedBlockingQueue<Packet>();
    private List<Packet> sentPackets = new ArrayList<Packet>();
    // Login stuff
    private int dimension;
    private byte difficulty;
    private EnumGamemode gameMode;
    private WorldType worldType;
    //
    private NetworkReader reader;
    private NetworkWriter writer;
    private BaseThread mover;
    private NetHandler baseHandler = new BaseHandler(this);
    //
    private volatile Location location;
    private int entityId;
    private int health = 20;
    private boolean onGround;
    private final PlayerInventory inventory = new PlayerInventory();
    private final TIntObjectMap<Entity> entities = new TIntObjectHashMap<Entity>();

    /**
     * Initialize a session for given server.
     *
     * NOTE: This method will not connect to the server, you must use the
     * connect() method for that.
     *
     * @param host the server which will be connected to
     * @param port the port which will be connected to on the host
     */
    public Connection(String host, int port) {
        this.host = host;
        this.port = port;
        Main.getActiveConnections().add(this);
    }

    /**
     * Log into minecraft.net in order to receive a session id. This session id
     * is used when connecting to online mode servers.
     *
     * @param user the user to be used for logging in. This is an email address
     * for migrated accounts.
     * @param password the password to be used
     * @throws InvalidLoginException when a session id is unable to be retrieved
     * for any reason
     */
    public void login(String user, String password) throws InvalidLoginException {
        try {
            String parameters = "user=" + URLEncoder.encode(user, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8") + "&version=13";
            String result = Util.excutePost("https://login.minecraft.net/", parameters);

            if (result == null) {
                throw new InvalidLoginException("Can't connect to minecraft.net");
            }
            if (!result.contains(":")) {
                if (result.trim().equals("Bad login")) {
                    throw new InvalidLoginException("Login failed");
                } else if (result.trim().equals("Old version")) {
                    throw new InvalidLoginException("Outdated launcher");
                } else {
                    throw new InvalidLoginException(result);
                }
            }
            String[] values = result.split(":");

            this.username = values[2].trim();
            this.sessionId = values[3].trim();
        } catch (Exception ex) {
            throw new InvalidLoginException(ex);
        }
    }

    /**
     * Sets this sessions username.
     *
     * NOTE: This method must only be called if you do not plan on logging into
     * minecraft.net or an online mode server.
     *
     * @param username the username to set
     * @return the same connection
     */
    public Connection setUsername(String username) {
        if (this.username != null) {
            throw new IllegalArgumentException("Username has already been set for this connection.");
        }
        this.username = username;
        return this;
    }

    /**
     * Actually connects to the given server. After this method has executed
     * without error, the socket object will no longer be null, and the login
     * sequence completed will have been completed.
     *
     * @return whether the connection is successful
     * @throws IOException when the underlying socket throws an exception
     */
    public boolean connect() throws IOException {
        if (this.isConnected) {
            throw new IllegalStateException("Already connected to a server");
        }
        this.isConnected = true;

        try {
            this.socket = new Socket(host, port);
        } catch (ConnectException ex) {
            this.shutdown("Connection refused.");
            return false;
        }
        this.socket.setSoTimeout(this.timeout);
        this.socket.setTrafficClass(24);

        DataInputStream in = new DataInputStream(this.socket.getInputStream());
        DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
        this.reader = new NetworkReader(this, in);
        this.writer = new NetworkWriter(this, out);

        writer.sendPacket(new Packet2Handshake(39, this.username, this.host, this.port), out);
        Packet response = reader.readPacket(in);
        checkResponse(response);

        Packet253KeyRequest encryptRequest = (Packet253KeyRequest) response;
        PublicKey serverKey = encryptRequest.getPublicKey();
        SecretKey myKey = Util.getSecretKey();

        if (!encryptRequest.getServerId().equals("-")) {
            if (this.sessionId == null) {
                throw new InvalidLoginException("Not logged in.");
            }
            String user = URLEncoder.encode(this.username, "UTF-8");
            String sessionId = URLEncoder.encode(this.sessionId, "UTF-8");
            String serverId = new BigInteger(MinecraftEncryption.a(encryptRequest.getServerId(), serverKey, myKey)).toString(16);

            Util.excutePost("http://session.minecraft.net/game/joinserver.jsp", "user=" + user + "&sessionId=" + sessionId + "&serverId=" + serverId);
        }

        Packet252KeyResponse keyShare = new Packet252KeyResponse(myKey, serverKey, encryptRequest.getVerifyToken());
        writer.sendPacket(keyShare, out);

        response = reader.readPacket(in);
        checkResponse(response);

        this.reader.setIn(new DataInputStream(MinecraftEncryption.a(keyShare.d(), in)));
        this.writer.setOut(new DataOutputStream(MinecraftEncryption.a(keyShare.d(), out)));
        this.reader.start();
        this.writer.start();

        sendPacket(new Packet205ClientCommand());

        response = getPacket();
        checkResponse(response);
        Packet1Login login = (Packet1Login) response;
        this.entityId = login.a;

        while (this.location == null) {
            ;
        }
        this.mover = new BaseThread(this);
        this.mover.start();
        return true;
    }

    private void checkResponse(Packet reponse) throws RuntimeException {
        int id = PacketUtil.getId(reponse);
        if (id != 0x01 && id != 0xFC && id != 0xFD) {
            String message = "Disconnected by server: " + ((Packet255KickDisconnect) reponse).a;
            shutdown(message);
            throw new RuntimeException(message);
        }
    }

    /**
     * Gracefully disconnect from the current server.
     *
     * @throws IOException when the quit packet cannot be sent or the socket
     * cannot be closed.
     */
    public void disconnect() throws IOException {
        if (!this.isConnected) {
            throw new IllegalStateException("Not connected to a server.");
        }
        try {
            Packet packet = new Packet255KickDisconnect("Quitting");
            sendPacket(packet);

            this.socket.close();
        } finally {
            shutdown("Quit");
        }
    }

    /**
     * Sends the specified packet to the connected server.
     *
     * @param packet the packet to be sent
     */
    public void sendPacket(Packet packet) {
        queuedPackets.add(packet);
    }

    /**
     * Gets the next availible packet from the server. This method will block
     * until there is a packet availible, or until the connectin is closed and a
     * packet is unable to be recevied.
     *
     * @return the next availible packet, null if there is no packet.
     * @throws IOException when there is an error receiving the packet
     */
    public Packet getPacket() throws IOException {
        if (!this.isConnected) {
            throw new IllegalStateException("Not connected to a server.");
        }

        Packet next = null;
        try {
            next = this.receivedPackets.take();
        } catch (InterruptedException ex) {
        }
        return next;
    }

    /**
     * Forcefully ends the currrent connection shutting it down.
     *
     * @param reason the reason for this early termination
     */
    public void shutdown(String reason) {
        if (this.isConnected) {
            System.out.println("Shutting down with reason: " + reason);
            this.isConnected = false;
            this.shutdownReason = reason;
            if (this.socket != null) {
                try {
                    this.socket.close();
                } catch (IOException ex) {
                }
            }
            if (this.reader != null) {
                this.reader.interrupt();
            }
            if (this.writer != null) {
                this.writer.interrupt();
            }
            if (this.mover != null) {
                this.mover.interrupt();
            }
            Main.getActiveConnections().remove(this);
        }
    }

    /**
     * Sends the specified message into chat.
     *
     * @param message the message to send.
     */
    public void sendMessage(String message) {
        this.queuedPackets.add(new Packet3Chat(message));
    }

    /**
     * Moves the specified amount forwards and left in the current direction
     * (calculated based on current yaw)
     *
     * @param forward, double sideways how far to move
     * @param left how far to the left to move
     */
    public void moveRelative(double forward, double left) {
        final float yaw = getLocation().getYaw();
        final double xToMove = -forward * Math.sin(yaw) + left * Math.cos(yaw);
        final double zToMove = forward * Math.cos(yaw) + left * Math.sin(yaw);

        getLocation().add(xToMove, 0, zToMove);
        getLocation().setYawRadians((float) Math.atan2(-xToMove, zToMove));
    }

    /**
     * Gets whether the packet has been sent to the server.
     *
     * @param packet the packet to check
     * @return whether the packet has been written and sent
     */
    public boolean wasSent(Packet packet) {
        return this.getSentPackets().contains(packet);
    }

    /**
     * Hits (left clicks) the spcified entity.
     *
     * @param entity the entity to hit.
     */
    public void attack(Entity entity) {
        Packet7UseEntity packet = new Packet7UseEntity();
        packet.a = this.getEntityId();
        packet.target = entity.getId();
        packet.action = 1;
        //
        look(entity.getLocation().getX(), entity.getLocation().getY() + STANDING_HEIGHT, entity.getLocation().getZ());
        swingArm();
        sendPacket(packet);
    }

    /**
     * Swing the bots arm.
     */
    public void swingArm() {
        Packet18ArmAnimation swing = new Packet18ArmAnimation();
        swing.a = this.getEntityId();
        swing.b = 1;
        sendPacket(swing);
    }

    /**
     * Get the entity of the given id.
     *
     * @param id of the entity
     * @return the entity
     */
    public Entity getEntity(int id) {
        return getEntities().get(id);
    }

    /**
     * Get the entities surrounding the bot in any direction, bounded by the
     * distance paramater.
     *
     * @param distance in any direction
     * @return list of entities on no particular order
     */
    public TreeSet<Entity> getNearbyEntities(double distance) {
        TreeSet<Entity> nearby = new TreeSet<Entity>(new Util.DistanceComparator(this.location));
        synchronized (this.getEntities()) {
            for (Entity e : this.getEntities().valueCollection()) {
                if (this.getLocation().distance(e.getLocation()) <= distance) {
                    nearby.add(e);
                }
            }
        }
        return nearby;
    }

    /**
     * Send the respawn packet. Will only be sent if the bot is currently dead.
     */
    public void respawn() {
        if (this.health <= 0) {
            sendPacket(new Packet9Respawn(this.dimension, this.difficulty, WorldType.NORMAL, 0, gameMode));
        }
    }

    /**
     * Internal method to set the bots health and respawn if need be.
     *
     * @param health the new health level
     * @deprecated should not be used manually
     */
    @Deprecated
    public void setHealth(int health) {
        this.health = health;
        if (this.health <= 0) {
            this.respawn();
        }
    }

    /**
     * Make the bot look at the specified unit vector.
     *
     * @param x to look at
     * @param y to look at
     * @param z to look at
     */
    public void look(double x, double y, double z) {
        double xDiff = x - getLocation().getX();
        double yDiff = y - getStance();
        double zDiff = z - getLocation().getZ();

        double pitch = -Math.atan2(yDiff, Math.sqrt(Math.pow(xDiff, 2) + Math.pow(zDiff, 2)));
        double yaw = Math.atan2(-xDiff, zDiff);

        getLocation().setYawRadians((float) yaw);
        getLocation().setPitchRadians((float) pitch);
    }

    /**
     * Helper method for {@link #look} to automatically parse in a locations x,y
     * and z coordinates.
     *
     * @param location location to look at
     */
    public void look(Location location) {
        look(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Helper method for {@link #look} to automatically add
     * {@link #STANDING_HEIGHT} to the entities y, before looking at it.
     *
     * @param entity entity to look at
     */
    public void look(Entity entity) {
        look(entity.getLocation().getX(), entity.getLocation().getY() + STANDING_HEIGHT, entity.getLocation().getZ());
    }

    public double getStance() {
        return getLocation().getY() + STANDING_HEIGHT;
    }

    public boolean isOnGround() {
        return this.onGround;
    }
}
