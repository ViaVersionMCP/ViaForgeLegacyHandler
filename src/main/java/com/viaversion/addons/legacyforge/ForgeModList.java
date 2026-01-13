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
    public void read(boolean legacy, ForgePacketHandler handler, ByteBuf buffer, Direction direction, byte packetID) {
        super.read(legacy, handler, buffer, direction, packetID);
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
        V0710, V0809, V0900, V1100, V1200, V1202;

        public String version;

        public ForgeVersion set(String versionIn) {
            this.version = versionIn;
            return this;
        }

        public static ForgeVersion from(String versionIn) {
            ForgeVersion fv;
            switch (versionIn.substring(0, 2)) {
                case "10" -> fv = V0710;
                case "11" -> fv = V0809;
                case "12" -> fv = V0900;
                case "13" -> fv = V1100;
                case "14" -> {
                    if (Integer.parseInt(versionIn.substring(versionIn.lastIndexOf('.') + 1)) < 2676) {
                        fv = V1200;
                    } else {
                        fv = V1202;
                    }
                }
                default -> throw new IllegalArgumentException();
            }
            return fv.set(versionIn);
        }
    }
}
