package com.md_5.bot.mc;

import com.md_5.bot.mc.entity.Entity;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Comparator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.crypto.CipherKeyGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;

public class Util {

    public static String excutePost(String targetURL, String urlParameters) throws IOException {
        URL url = new URL(targetURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.connect();

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();

        return response.toString();
    }

    public static PingResponse pingServer(String host, int port) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(3000);
        socket.setTcpNoDelay(true);
        socket.setTrafficClass(18);
        socket.connect(new InetSocketAddress(host, port), 3000);

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.write(254);

        if (in.read() != 255) {
            throw new IOException("Bad message");
        }

        String response = PacketUtil.readString(in, 256);

        String[] split = response.split("§");
        String motd = split[0];

        int online = -1;
        try {
            online = Integer.parseInt(split[1]);
        } catch (NumberFormatException ex) {
        }

        int max = -1;
        try {
            max = Integer.parseInt(split[2]);
        } catch (NumberFormatException ex) {
        }

        return new PingResponse(motd, online, max);
    }

    public static SecretKey getSecretKey() {
        CipherKeyGenerator gen = new CipherKeyGenerator();
        gen.init(new KeyGenerationParameters(new SecureRandom(), 128));
        return new SecretKeySpec(gen.generateKey(), "AES");
    }

    @RequiredArgsConstructor
    public static class DistanceComparator implements Comparator<Entity> {

        private final Location base;

        @Override
        public int compare(Entity o1, Entity o2) {
            double l1 = o1.getLocation().distanceSquared(base);
            double l2 = o2.getLocation().distanceSquared(base);
            return (int) (l1 - l2);
        }
    }
}
