package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import io.netty.buffer.ByteBuf;

public class ForgeRegistryDataLegacy extends ForgeRegistryData {

    @Override
    public void read(boolean legacy, ForgePacketHandler handler, ByteBuf buffer, Direction direction, byte packetID) {
        super.read(legacy, handler, buffer, direction, packetID);
        hasMore = false;
        registryType = RegistryTypes.ITEMS;
        idCount = readVarInt(buffer);
        for (int i = 0; i < idCount; i++) {
            String name = readString(buffer);
            int id = readVarInt(buffer);
            if (shouldRemove(registryType, name, id)) {
                removeCount++;
            } else {
                LOGGER.info("Entry: {}, {}", name, id);
                idMap.put(name, id);
            }
        }
        substitutionCount = readVarInt(buffer);
        for (int i = 0; i < substitutionCount; i++) {
            String substitution = readString(buffer);
            LOGGER.info("Blocks: " + substitution);
            substitutionList.add(substitution);
        }
        dummyCount = readVarInt(buffer);
        for (int i = 0; i < dummyCount; i++) {
            String dummy = readString(buffer);
            LOGGER.info("Items: " + dummy);
            dummyList.add(dummy);
        }
    }

    @Override
    public ByteBuf write() {
        return super.write();
    }

    @Override
    public String toString() {
        return "Legacy RegistryData side=" + direction + " hasMore=" + hasMore
                + " counts=" + idCount + " blockSubstitutions=" + substitutionCount + " itemSubstitutions" + dummyCount;
    }
}
