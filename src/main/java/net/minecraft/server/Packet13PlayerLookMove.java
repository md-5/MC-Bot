package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import lombok.SneakyThrows;

public class Packet13PlayerLookMove extends Packet10Flying {

    public Packet13PlayerLookMove() {
        this.hasLook = true;
        this.hasPos = true;
    }

    public Packet13PlayerLookMove(double x, double y, double stance, double z, float yaw, float pitch, boolean onGround) {
        this.x = x;
        this.y = y;
        this.stance = stance;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.g = onGround;
        this.hasLook = true;
        this.hasPos = true;
    }

    @SneakyThrows(value = IOException.class)
    public void a(DataInputStream datainputstream) {
        this.x = datainputstream.readDouble();
        this.stance = datainputstream.readDouble();
        this.y = datainputstream.readDouble();
        this.z = datainputstream.readDouble();
        this.yaw = datainputstream.readFloat();
        this.pitch = datainputstream.readFloat();
        super.a(datainputstream);
    }

    @SneakyThrows(value = IOException.class)
    public void a(DataOutputStream dataoutputstream) {
        dataoutputstream.writeDouble(this.x);
        dataoutputstream.writeDouble(this.y);
        dataoutputstream.writeDouble(this.stance);
        dataoutputstream.writeDouble(this.z);
        dataoutputstream.writeFloat(this.yaw);
        dataoutputstream.writeFloat(this.pitch);
        super.a(dataoutputstream);
    }

    public int a() {
        return 41;
    }
}
