package com.md_5.bot.mc.impl;

import com.md_5.bot.mc.Connection;
import com.md_5.bot.mc.PacketUtil;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import net.minecraft.server.Packet;

public class NetworkReader extends Thread {

    private final Connection con;
    private final DataInputStream in;

    public NetworkReader(Connection con, DataInputStream in) {
        super("NetworkReader - " + con.getHost() + " - " + con.getUsername());
        this.con = con;
        this.in = in;
    }

    @Override
    public void run() {
        try {
            while (con.isConnected()) {
                Packet packet = readPacket(in);
                if (packet == null) {
                    con.shutdown("End of stream");
                } else {
                    packet.handle(con.getBaseHandler());
                    con.getReceivedPackets().add(packet);
                }
            }
        } catch (IOException ex) {
            con.shutdown("Reader - " + ex.getMessage());
        }
    }

    private Packet readPacket(DataInputStream in) throws IOException {
        Packet packet = null;
        try {
            int id = in.read();
            if (id == -1) {
                return null;
            }

            packet = Packet.a(id);
            if (packet == null) {
                throw new IOException("Bad packet id " + id);
            }

            packet.a(in);
        } catch (EOFException ex) {
        }
        return packet;
    }
}
