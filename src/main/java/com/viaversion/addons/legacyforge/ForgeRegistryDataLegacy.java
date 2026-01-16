package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import net.raphimc.vialegacy.protocol.release.r1_7_2_5tor1_7_6_10.packet.ClientboundPackets1_7_2;
import net.raphimc.vialegacy.protocol.release.r1_7_6_10tor1_8.Protocolr1_7_6_10Tor1_8;

import java.util.HashMap;
import java.util.Map;

public class ForgeRegistryDataLegacy extends ForgeRegistryData {

    public Map<String, Integer> blockIdMap = new HashMap<>();

    public Map<String, Integer> itemIdMap = new HashMap<>();

    private ForgeModList.ForgeVersion version;

    @Override
    public void read(boolean legacy, ForgePacketHandler handler, ByteBuf buffer, Direction direction, byte packetID) {
        super.read(legacy, handler, buffer, direction, packetID);
        version = this.handler.connection.get(ForgeModList.ForgeVersion.class);
        hasMore = false;
        registryType = RegistryTypes.ITEMS;
        idCount = readVarInt(buffer);
        for (int i = 0; i < idCount; i++) {
            String name = readString(buffer);
            char desc = name.charAt(0);
            name = name.substring(1);
            int id = readVarInt(buffer);
            switch (desc) {
                case '\u0001' -> blockIdMap.put(name, id);
                case '\u0002' -> {
                    if (shouldRemove(registryType, name, id)) {
                        removeCount++;
                    } else {
                        itemIdMap.put(name, id);
                    }
                }
                default -> LOGGER.warn("Unknown Entry: {}, {}", name, id);
            }
        }
        substitutionCount = readVarInt(buffer);
        for (int i = 0; i < substitutionCount; i++) {
            String substitution = readString(buffer);
            substitutionList.add(substitution);
        }
        dummyCount = readVarInt(buffer);
        for (int i = 0; i < dummyCount; i++) {
            String dummy = readString(buffer);
            dummyList.add(dummy);
        }
    }

    @Override
    public ByteBuf write() {
        return writeData(hasMore, RegistryTypes.ITEMS, itemIdMap.size(), itemIdMap);
    }

    @Override
    public boolean shouldRewrite(PacketWrapper wrapper) {
        if (version == ForgeModList.ForgeVersion.V0710) {
            LOGGER.info("Didn't rewrite for actually 1.7 client (stacking)");
            return false;
        }
        PacketWrapper recipePacket = PacketWrapper.create(ClientboundPackets1_7_2.CUSTOM_PAYLOAD, handler.connection);
        recipePacket.write(Types.STRING, ForgePacketHandler.CHANNEL);
        recipePacket.write(Types.REMAINING_BYTES, writeData(true, RegistryTypes.BLOCKS, blockIdMap.size(), blockIdMap).array());
        recipePacket.send(Protocolr1_7_6_10Tor1_8.class);
        LOGGER.info("Sending Blocks Registry for 1.8+.");
        return true;
    }

    @Override
    public boolean shouldRemove(RegistryTypes type, String entry, int id) {
        if (version != null && version.ordinal() >= ForgeModList.ForgeVersion.V0809.ordinal()) {
            if (type == RegistryTypes.ITEMS && (RegistryDatas.removeListLegacy.contains(entry))) {
                LOGGER.info("Removing item registry entry: {}", entry);
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Legacy RegistryData side=" + direction + " hasMore=" + hasMore
                + " counts=" + idCount + " blockSubstitutions=" + substitutionCount + " itemSubstitutions" + dummyCount;
    }
}
