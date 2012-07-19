package com.md_5.bot.mc.impl;

import com.md_5.bot.mc.Connection;
import com.md_5.bot.mc.Location;
import net.minecraft.server.NetHandler;
import net.minecraft.server.Packet0KeepAlive;
import net.minecraft.server.Packet10Flying;
import net.minecraft.server.Packet255KickDisconnect;

public class BaseHandler extends NetHandler {

    private final Connection con;

    public BaseHandler(Connection con) {
        this.con = con;
    }

    @Override
    public boolean c() {
        return false;
    }

    @Override
    public void a(Packet0KeepAlive pka) {
        con.sendPacket(new Packet0KeepAlive(pka.a));
    }

    @Override
    public void a(Packet255KickDisconnect pkd) {
        con.shutdown(pkd.a);
    }

    @Override
    public void a(Packet10Flying pf) {
        final Location base = (con.getLocation() != null) ? con.getLocation() : new Location();

        float yaw = base.getYaw();
        float pitch = base.getPitch();
        if (pf.hasLook) {
            yaw = pf.yaw;
            pitch = pf.pitch;
        }

        double x = base.getX();
        double y = base.getY();
        double z = base.getZ();
        double stance = base.getStance();
        if (pf.hasPos) {
            x = pf.x;
            y = pf.y;
            z = pf.z;
            stance = pf.stance;
        }
        boolean onGround = pf.g;

        Location location = new Location(yaw, pitch, x, y, z, stance, onGround);
        con.setLocation(location);
    }
}
