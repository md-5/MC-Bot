package com.md_5.bot.mc.impl;

import com.md_5.bot.mc.Connection;
import com.md_5.bot.mc.PacketUtil;
import java.io.DataOutputStream;
import java.io.IOException;
import lombok.Setter;
import net.minecraft.server.Packet;

public class NetworkWriter extends Thread {

    private final Connection con;
    @Setter
    private DataOutputStream out;

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
                    sendPacket(packet, out);
                } catch (InterruptedException ex) {
                }
            }
        } catch (Exception ex) {
            StackTraceElement el = ex.getStackTrace()[0];
            con.shutdown("Writer - Error @ " + el.getClassName() + " line " + el.getLineNumber() + " " + ex.getClass().getName() + "[" + ex.getMessage() + "]");
        }
    }

    public void sendPacket(Packet packet, DataOutputStream out) throws IOException {
        out.write(PacketUtil.getId(packet));
        packet.a(out);

        con.getSentPackets().add(packet);
    }
}
