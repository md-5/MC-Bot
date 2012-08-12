package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.security.PublicKey;
import lombok.Getter;

@Getter
public class Packet253KeyRequest extends Packet {

    private String serverId;
    private PublicKey publicKey;
    private byte[] verifyToken = new byte[0];

    public Packet253KeyRequest() {
    }

    public Packet253KeyRequest(String s, PublicKey publickey, byte[] abyte) {
        this.serverId = s;
        this.publicKey = publickey;
        this.verifyToken = abyte;
    }

    public void a(DataInputStream datainputstream) {
        this.serverId = a(datainputstream, 20);
        this.publicKey = MinecraftEncryption.a(b(datainputstream));
        this.verifyToken = b(datainputstream);
    }

    public void a(DataOutputStream dataoutputstream) {
        a(this.serverId, dataoutputstream);
        a(dataoutputstream, this.publicKey.getEncoded());
        a(dataoutputstream, this.verifyToken);
    }

    public void handle(NetHandler nethandler) {
        nethandler.a(this);
    }

    public int a() {
        return 2 + this.serverId.length() * 2 + 2 + this.publicKey.getEncoded().length + 2 + this.verifyToken.length;
    }
}
