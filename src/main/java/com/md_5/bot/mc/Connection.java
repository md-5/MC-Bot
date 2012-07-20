package com.md_5.bot.mc;

import com.md_5.bot.mc.impl.BaseHandler;
import com.md_5.bot.mc.impl.NetworkReader;
import com.md_5.bot.mc.impl.NetworkWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.security.auth.login.LoginException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import net.minecraft.server.NetHandler;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet13PlayerLookMove;
import net.minecraft.server.Packet1Login;
import net.minecraft.server.Packet255KickDisconnect;
import net.minecraft.server.Packet2Handshake;
import net.minecraft.server.Packet3Chat;

@Data
public class Connection {

    private final String host;
    private final int port;
    //
    private String username;
    private String sessionId;
    private boolean isConnected;
    private String shutdownReason;
    //
    private int timeout = 30000;
    private Socket socket;
    //
    private BlockingQueue<Packet> receivedPackets = new LinkedBlockingQueue<Packet>();
    private BlockingQueue<Packet> queuedPackets = new LinkedBlockingQueue<Packet>();
    private List<Packet> sentPackets = new ArrayList<Packet>();
    //
    private NetworkReader reader;
    private NetworkWriter writer;
    private NetHandler baseHandler = new BaseHandler(this);
    //
    @Setter(AccessLevel.NONE)
    private Location location;
    private PlayerInventory inventory = new PlayerInventory();

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
                    throw new LoginException("Login failed");
                } else if (result.trim().equals("Old version")) {
                    throw new LoginException("Outdated launcher");
                } else {
                    throw new LoginException(result);
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
        this.reader.start();
        this.writer = new NetworkWriter(this, out);
        this.writer.start();


        sendPacket(new Packet2Handshake(this.username + ";" + this.host + ";" + this.port));
        Packet reponse = getPacket();
        checkResponse(reponse);

        Packet2Handshake handshake = (Packet2Handshake) reponse;
        if (!handshake.a.equals("-")) {
            // TODO
        }

        sendPacket(new Packet1Login(this.username, 29, null, 0, 0, (byte) 0, (byte) 0, (byte) 0));
        checkResponse(reponse);

        return true;
    }

    private void checkResponse(Packet reponse) throws RuntimeException {
        if (PacketUtil.getId(reponse) != 2) {
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

        getLocation().setX(getLocation().getX() + xToMove);
        getLocation().setZ(getLocation().getZ() + zToMove);
        getLocation().setYaw((float) Math.atan2(-xToMove, zToMove));

        sendLocationUpdate();
    }

    public void setLocation(Location location) {
        this.location = location;
        sendLocationUpdate();
    }

    /**
     * Internal method to send a location update packet pre populated with all
     * the necessary data.
     *
     * @deprecated should not be used manually
     */
    @Deprecated
    private void sendLocationUpdate() {
        Location loc = getLocation();
        Packet13PlayerLookMove packet = new Packet13PlayerLookMove(loc.getX(), loc.getY(), loc.getStance(), loc.getZ(), loc.getYaw(), loc.getPitch(), loc.isOnGround());

        sendPacket(packet);
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
}
