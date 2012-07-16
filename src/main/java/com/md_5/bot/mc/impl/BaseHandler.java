package com.md_5.bot.mc.impl;

import com.md_5.bot.mc.Connection;
import net.minecraft.server.NetHandler;
import net.minecraft.server.Packet0KeepAlive;

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
}
