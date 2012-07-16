package com.md_5.bot.mc.impl;

import com.md_5.bot.mc.Connection;
import com.md_5.bot.mc.PacketUtil;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.server.Packet;

public class NetworkWriter extends Thread {

    private final Connection con;
    private final DataOutputStream out;

    public NetworkWriter(Connection con, DataOutputStream out) {
        super("NetworkWriter - " + con.getHost() + " - " + con.getUsername());
        this.con = con;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            while (con.isConnected()) {
                try {
                    Packet packet = con.getQueuedPackets().take();
                    out.write(PacketUtil.getId(packet));
                    packet.a(out);
                } catch (InterruptedException ex) {
                }
            }
        } catch (IOException ex) {
            con.shutdown("Writer - " + ex.getMessage());
        }
    }
}
