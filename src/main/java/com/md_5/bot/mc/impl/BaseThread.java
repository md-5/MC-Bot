package com.md_5.bot.mc.impl;

import com.md_5.bot.mc.Connection;
import com.md_5.bot.mc.Location;
import net.minecraft.server.Packet13PlayerLookMove;

public class BaseThread extends Thread {

    private final Connection con;

    public BaseThread(Connection con) {
        super("BaseThread - " + con.getHost() + " - " + con.getUsername());
        this.con = con;
    }

    @Override
    public void run() {
        while (con.isConnected()) {
            sendLocationUpdate();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
            }
        }
    }

    /**
     * Internal method to send a location update packet pre populated with all
     * the necessary data.
     *
     * @deprecated should not be used manually
     */
    @Deprecated
    public void sendLocationUpdate() {
        Location loc = con.getLocation();
        Packet13PlayerLookMove packet = new Packet13PlayerLookMove(loc.getX(), loc.getY(), con.getStance(), loc.getZ(), loc.getYaw(), loc.getPitch(), con.isOnGround());

        con.sendPacket(packet);
    }
}
