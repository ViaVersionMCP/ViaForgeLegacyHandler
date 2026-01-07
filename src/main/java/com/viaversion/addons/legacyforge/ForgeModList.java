package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import io.netty.buffer.ByteBuf;

import java.util.*;

public class ForgeModList extends ForgePayload {

    public int modCount;

    public List<String> mods;

    public ForgeModList() {}

    @Override
    public void read(ForgePacketHandler handler, ByteBuf buffer, Direction direction, byte packetID) {
        super.read(handler, buffer, direction, packetID);
        modCount = readVarInt(buffer);
        mods = new ArrayList<>(modCount);
        for (int i = 0; i < modCount; i++) {
            String modId = readString(buffer);
            String version = readString(buffer);
            if (modId.equals("Forge") || modId.equals("forge")) {
                if (direction == Direction.SERVERBOUND) {
                    LOGGER.info("Client Forge Version:" + version);
                    this.handler.connection.put(ForgeVersion.from(version));
                }
            }
            mods.add(modId + "@" + version);
        }
    }

    @Override
    public ByteBuf write() {
        return super.write();
    }

    @Override
    public String toString() {
        return "ModList mods=" + mods + " count=" + modCount;
    }

    public enum ForgeVersion implements StorableObject {
        V7, V8, V9, V11, V12;

        public String version;

        public ForgeVersion set(String versionIn) {
            this.version = versionIn;
            return this;
        }

        public static ForgeVersion from(String versionIn) {
            ForgeVersion fv;
            switch (versionIn.substring(0, 2)) {
                case "10" -> fv = V7;
                case "11" -> fv = V8;
                case "12" -> fv = V9;
                case "13" -> fv = V11;
                case "14" -> fv = V12;
                default -> throw new IllegalArgumentException();
            }
            return fv.set(versionIn);
        }
    }
}
