package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;

public class Packet252KeyResponse extends Packet {

    private byte[] a = new byte[0];
    private byte[] b = new byte[0];
    private SecretKey c;

    public Packet252KeyResponse() {
    }

    public Packet252KeyResponse(SecretKey secret, PublicKey publicKey, byte[] verifyToken) {
        this.c = secret;
        this.a = encrypt(publicKey, secret.getEncoded());
        this.b = encrypt(publicKey, verifyToken);
    }

    public void a(DataInputStream datainputstream) {
        this.a = b(datainputstream);
        this.b = b(datainputstream);
    }

    public void a(DataOutputStream dataoutputstream) {
        a(dataoutputstream, this.a);
        a(dataoutputstream, this.b);
    }

    public void handle(NetHandler nethandler) {
        nethandler.a(this);
    }

    public int a() {
        return 2 + this.a.length + 2 + this.b.length;
    }

    public SecretKey a(PrivateKey privatekey) {
        return privatekey == null ? this.c : (this.c = MinecraftEncryption.a(privatekey, this.a));
    }

    public SecretKey d() {
        return this.a((PrivateKey) null);
    }

    public byte[] b(PrivateKey privatekey) {
        return privatekey == null ? this.b : MinecraftEncryption.b(privatekey, this.b);
    }

    @SneakyThrows
    private static byte[] encrypt(Key key, byte[] input) {
        Cipher ciph = Cipher.getInstance("RSA");
        ciph.init(Cipher.ENCRYPT_MODE, key);
        return ciph.doFinal(input);
    }
}
