package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.*;

public class ForgeRegistryData extends ForgePayload {

    private ForgeModList.ForgeVersion version;

    public boolean hasMore;

    public RegistryTypes registryType;

    public int idCount, removeCount, substitutionCount;

    public Integer dummyCount;

    public Map<String, Integer> idMap = new HashMap<>();

    public List<String> substitutionList = new ArrayList<>();

    public List<String> dummyList = new ArrayList<>();

    public ForgeRegistryData() {}

    @Override
    public void read(boolean legacy, ForgePacketHandler handler, ByteBuf buffer, Direction direction, byte packetID) {
        super.read(legacy, handler, buffer, direction, packetID);
        version = this.handler.connection.get(ForgeModList.ForgeVersion.class);
        if (legacy) {
            hasMore = false;
            registryType = RegistryTypes.ITEMS;
        } else {
            hasMore = buffer.readBoolean();
            registryType = RegistryTypes.from(readString(buffer));
        }
        idCount = readVarInt(buffer);
        for (int i = 0; i < idCount; i++) {
            String name = readString(buffer);
            int id = readVarInt(buffer);
            if (shouldRemove(registryType, name, id)) {
                removeCount++;
            } else {
                if (legacy) {
                    LOGGER.info("Entry: {}, {}", name, id);
                }
                idMap.put(name, id);
            }
        }
        substitutionCount = readVarInt(buffer);
        for (int i = 0; i < substitutionCount; i++) {
            String substitution = readString(buffer);
            LOGGER.info("Subs: " + substitution);
            substitutionList.add(substitution);
        }
        if (buffer.isReadable()) {
            dummyCount = readVarInt(buffer);
            for (int i = 0; i < dummyCount; i++) {
                String dummy = readString(buffer);
                LOGGER.info("Dummy: " + dummy);
                dummyList.add(dummy);
            }
        }
    }

    @Override
    public ByteBuf write() {
        if ((legacy && version == ForgeModList.ForgeVersion.V0710) || (!legacy && version == ForgeModList.ForgeVersion.V0809)) {
            LOGGER.info("Didn't rewrite for actually 1.7 or 1.8 client (stacking)");
            return super.write();
        }
        if (this.registryType == RegistryTypes.ITEMS) {
            return writeData(hasMore, RegistryTypes.ITEMS, idCount - removeCount, idMap);
        }
        return super.write();
    }

    public ByteBuf writeData(boolean more, RegistryTypes types, int counts, Map<String, Integer> registry) {
        ByteBuf rewrite = Unpooled.buffer();
        rewrite.writeByte(3);
        rewrite.writeBoolean(more);
        writeString(rewrite, types.type);
        writeVarInt(rewrite, counts);
        for (Map.Entry<String, Integer> entry : registry.entrySet()) {
            writeString(rewrite, entry.getKey());
            writeVarInt(rewrite, entry.getValue());
        }
        writeVarInt(rewrite, 0);
        return rewrite;
    }

    @Override
    public String toString() {
        if (legacy) {
            return "Legacy RegistryData side=" + direction + " hasMore=" + hasMore
                    + " counts=" + idCount + " blockSubstitutions=" + substitutionCount + " itemSubstitutions" + dummyCount;
        }
        return "RegistryData name=" + registryType.type + " side=" + direction + " hasMore=" + hasMore
                + " counts=" + idCount + " substitutions=" + substitutionCount + " dummy=" + dummyCount;
    }

    @Override
    public boolean shouldRewrite(PacketWrapper wrapper) {
        if (version == ForgeModList.ForgeVersion.V1202) {
            if (this.registryType == RegistryTypes.ITEMS) {
                RegistryDatas.init();
                Map<String, Integer> items = RegistryDatas.items;
                Map<String, Integer> recipes = RegistryDatas.recipes;
                wrapper.write(Types.REMAINING_BYTES, writeData(true, RegistryTypes.ITEMS, items.size(), items).array());
                LOGGER.info("Resending Item Registry for 1.12.2.");
                wrapper.send(Protocol1_8To1_9.class);
                PacketWrapper recipePacket = PacketWrapper.create(ClientboundPackets1_8.CUSTOM_PAYLOAD, handler.connection);
                recipePacket.write(Types.STRING, ForgePacketHandler.CHANNEL);
                recipePacket.write(Types.REMAINING_BYTES, writeData(false, RegistryTypes.RECIPES, recipes.size(), recipes).array());
                recipePacket.send(Protocol1_8To1_9.class, false);
                LOGGER.info("Sending Recipes Registry for 1.12.2.");
                return false;
            }
        }
        return true;
    }

    public boolean shouldRemove(RegistryTypes type, String entry, int id) {
        ForgeModList.ForgeVersion version = handler.connection.get(ForgeModList.ForgeVersion.class);
        if (version != null && version.ordinal() >= ForgeModList.ForgeVersion.V0900.ordinal()) {
            if (type == RegistryTypes.ITEMS && (RegistryDatas.removeList.contains(entry) || RegistryDatas.removeListID.contains(id))) {
                LOGGER.info("Removing registry {} entry: {}", registryType, entry);
                return true;
            }
        }
        return false;
    }

}
