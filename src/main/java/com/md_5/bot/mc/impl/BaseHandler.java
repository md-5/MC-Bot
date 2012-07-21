package com.md_5.bot.mc.impl;

import com.md_5.bot.mc.Connection;
import com.md_5.bot.mc.Location;
import com.md_5.bot.mc.entity.Entity;
import com.md_5.bot.mc.entity.OtherPlayer;
import net.minecraft.server.NetHandler;
import net.minecraft.server.Packet0KeepAlive;
import net.minecraft.server.Packet103SetSlot;
import net.minecraft.server.Packet104WindowItems;
import net.minecraft.server.Packet10Flying;
import net.minecraft.server.Packet1Login;
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet255KickDisconnect;
import net.minecraft.server.Packet29DestroyEntity;
import net.minecraft.server.Packet30Entity;
import net.minecraft.server.Packet8UpdateHealth;
import net.minecraft.server.Packet9Respawn;

public class BaseHandler extends NetHandler {

    private final Connection con;

    public BaseHandler(Connection con) {
        this.con = con;
    }

    /**
     * Is this a server handler?
     */
    @Override
    public boolean c() {
        return false;
    }

    /**
     * Initial spawn
     */
    @Override
    public void a(Packet1Login pl) {
        con.setWorldType(pl.c);
        con.setGameMode((byte) pl.d);
        con.setDimension((byte) pl.e);
        con.setDifficulty(pl.f);
    }

    /**
     * respawn
     */
    @Override
    public void a(Packet9Respawn pr) {
        con.setWorldType(pr.e);
        con.setGameMode((byte) pr.d);
        con.setDimension(pr.a);
        con.setDifficulty((byte) pr.b);
    }

    /**
     * Handle keepalives
     */
    @Override
    public void a(Packet0KeepAlive pka) {
        con.sendPacket(new Packet0KeepAlive(pka.a));
    }

    /**
     * Shutdown on kicks
     */
    @Override
    public void a(Packet255KickDisconnect pkd) {
        con.shutdown(pkd.a);
    }

    /**
     * Health updates
     */
    @Override
    public void a(Packet8UpdateHealth puh) {
        con.setHealth(puh.a);
    }

    /**
     * Let the server tell us our position
     */
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
        // double stanceModifier = base.getStance();
        if (pf.hasPos) {
            x = pf.x;
            y = pf.y;
            z = pf.z;
            // stance = pf.stance;
        }
        boolean onGround = pf.g;

        Location location = new Location(yaw, pitch, x, y, z);
        con.setLocation(location);
        con.setOnGround(onGround);
    }

    /**
     * Setting of one slot
     */
    @Override
    public void a(Packet103SetSlot pss) {
        if (pss.a == 0) { // 0 = inventory
            con.getInventory().setItem(pss.b, pss.c);
        }
    }

    /**
     * Setting of the entire inventory
     */
    @Override
    public void a(Packet104WindowItems pwi) {
        if (pwi.a == 0) { // 0 = inventory
            for (int i = 0; i < pwi.b.length; i++) {
                con.getInventory().setItem(i, pwi.b[i]);
            }
        }
    }

    /**
     * Fellow human beings spawning in on us.
     */
    @Override
    public void a(Packet20NamedEntitySpawn pnes) {
        int id = pnes.a;
        String name = pnes.b;

        float yaw = unwrap(pnes.f);
        float pitch = unwrap(pnes.g);

        Location loc = new Location(yaw, pitch, unwrap(pnes.c), unwrap(pnes.d), unwrap(pnes.e));
        int currentItem = pnes.h;
        OtherPlayer player = new OtherPlayer(id, name, loc, currentItem);

        addEntity(player);
    }

    @Override
    public void a(Packet30Entity pe) {
        Entity entity = con.getEntity(pe.a);
        if (entity != null) {
            Location loc = entity.getLocation();
            loc.add(unwrap((int) pe.b), unwrap((int) pe.c), unwrap((int) pe.d), (pe.g) ? unwrap(pe.e) : 0, (pe.g) ? unwrap(pe.f) : 0);
        }
    }

    /**
     * Something died
     */
    @Override
    public void a(Packet29DestroyEntity pde) {
        con.getEntities().remove(pde.a);
    }

    /**
     * Helper method to add know entities.
     *
     * @param entity to add
     */
    private void addEntity(Entity entity) {
        con.getEntities().put(entity.getId(), entity);
    }

    /**
     * Helper method to unwrap the int entity location into a double one.
     *
     * @param wrapped the int wrapped location
     * @return the unwrapped location
     */
    private double unwrap(int wrapped) {
        return wrapped / 32D;
    }

    /**
     * Helper method to unwrap the byte entity angle into a float one.
     *
     * @param wrapped the byte wrapped angle
     * @return the unwrapped location
     */
    private float unwrap(byte wrapped) {
        return (wrapped * 360) / 256F;
    }
}
