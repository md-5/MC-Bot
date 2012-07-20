package com.md_5.bot.mc.impl;

import com.md_5.bot.mc.Connection;

public class BaseThread extends Thread {

    private final Connection con;

    public BaseThread(Connection con) {
        super("BaseThread - " + con.getHost() + " - " + con.getUsername());
        this.con = con;
    }

    @Override
    public void run() {
        while (con.isConnected()) {
            con.sendLocationUpdate();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
            }
        }
    }
}
