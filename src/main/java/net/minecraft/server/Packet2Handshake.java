package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class Packet2Handshake extends Packet {

    private int version;
    private String username;
    private String host;
    private int port;

    public Packet2Handshake() {
    }

    public Packet2Handshake(int version, String username, String host, int port) {
        this.version = version;
        this.username = username;
        this.host = host;
        this.port = port;
    }

    @SneakyThrows(value = IOException.class)
    public void a(DataInputStream datainputstream) {
        this.version = datainputstream.readByte();
        this.username = a(datainputstream, 16);
        this.host = a(datainputstream, 255);
        this.port = datainputstream.readInt();
    }

    @SneakyThrows(value = IOException.class)
    public void a(DataOutputStream dataoutputstream) {
        dataoutputstream.writeByte(this.version);
        a(this.username, dataoutputstream);
        a(this.host, dataoutputstream);
        dataoutputstream.writeInt(this.port);
    }

    public void handle(NetHandler nethandler) {
        nethandler.a(this);
    }

    public int a() {
        return 3 + 2 * this.username.length();
    }
}
