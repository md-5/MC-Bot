package com.md_5.bot.mc;

import java.io.DataInputStream;
import java.io.IOException;
import net.minecraft.server.Packet;

public class PacketUtil {

    /**
     * Reads a string of the given length from the DataInputStream
     *
     * @param in stream to read from
     * @param maxLength max length of the string
     * @return the read string
     * @throws IOException when the underlying read method throws an error
     */
    public static String readString(DataInputStream in, int maxLength) throws IOException {
        return Packet.a(in, maxLength);
    }

    /**
     * Gets the id of the specified packet.
     *
     * @param packet packet to id
     * @return the packets id
     */
    public static int getId(Packet packet) {
        return packet.b();
    }
}
