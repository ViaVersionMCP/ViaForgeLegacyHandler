package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import io.netty.buffer.*;
import org.apache.logging.log4j.*;

import java.nio.charset.StandardCharsets;

public class ForgePayload {

    public static final Logger LOGGER = LogManager.getLogger("Forge-Payload");

    public ForgePacketHandler handler;

    public ByteBuf buffer;

    public Direction direction;

    public byte packetID;

    public ForgePayload() {}

    public void read(ForgePacketHandler handler, ByteBuf buffer, Direction direction, byte packetID) {
        this.handler = handler;
        this.buffer = buffer;
        this.direction = direction;
        this.packetID = packetID;
    }

    public ByteBuf write() {
        return buffer;
    }

    public void log(final String message, final boolean warn) {
        log(this.direction, message, warn);
    }

    public static int readVarInt(final ByteBuf buffer) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            if (!buffer.isReadable()) {
                throw new IllegalStateException("Not enough bytes for VarInt");
            }
            read = buffer.readByte();
            int value = read & 0x7F;
            result |= value << (7 * numRead);
            numRead++;
            if (numRead > 5) {
                throw new IllegalArgumentException("VarInt too big");
            }
        } while ((read & 0x80) != 0);
        return result;
    }

    public static String readString(final ByteBuf buffer) {
        int length = readVarInt(buffer);
        if (length < 0 ) {
            throw new IllegalStateException("Invalid string length " + length);
        }
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeVarInt(ByteBuf buf, int value) {
        while ((value & ~0x7F) != 0) {
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value);
    }

    public static void writeString(ByteBuf buf, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 32767) {
            throw new IllegalArgumentException("String too long: " + bytes.length);
        }
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    public static Integer parseInt(final String in) {
        try {
            return Integer.parseInt(in);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static void log(final Direction direction, final String message, final boolean warn) {
        if (warn) {
            LOGGER.warn("[ForgePayload] {}: {}", direction, message);
        } else {
            LOGGER.info("[ForgePayload] {}: {}", direction, message);
        }
    }

}
