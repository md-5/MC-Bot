package com.md_5.bot.mc;

import java.io.IOException;
import java.net.Socket;
import java.net.URLEncoder;
import javax.security.auth.login.LoginException;
import lombok.Data;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet255KickDisconnect;

@Data
public class Connection {

    private final String host;
    private final int port;
    //
    private String username;
    private String sessionId;
    private boolean isConnected;
    //
    private int timeout = 30000;
    private Socket socket;

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
     * @throws IOException when the underlying socket throws an exception
     */
    public void connect() throws IOException {
        if (this.isConnected) {
            throw new IllegalStateException("Already connected to a server");
        }

        this.socket = new Socket(host, port);
        this.socket.setSoTimeout(this.timeout);
        this.socket.setTrafficClass(24);

        this.isConnected = true;
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
            this.isConnected = false;
        }
    }

    /**
     * Sends the specified packet to the connected server.
     *
     * @param packet the packet to be sent
     * @throws IOException when there is an error sending the packet
     */
    public void sendPacket(Packet packet) throws IOException {
        if (!this.isConnected) {
            throw new IllegalStateException("Not connected to a server.");
        }

        // TODO
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

        return null;
    }
}
