package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import io.netty.buffer.ByteBuf;

import java.util.Map;

public class ForgeRegistryDataVintage extends ForgeRegistryData {

    @Override
    public void read(boolean legacy, ForgePacketHandler handler, ByteBuf buffer, Direction direction, byte packetID) {
        super.read(legacy, handler, buffer, direction, packetID);
        hasMore = buffer.readBoolean();
        registryType = RegistryTypes.from(readString(buffer));
        idCount = readVarInt(buffer);
        for (int i = 0; i < idCount; i++) {
            String name = readString(buffer);
            int id = readVarInt(buffer);
            if (shouldRemove(registryType, name, id)) {
                removeCount++;
            } else {
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
        if (version == ForgeModList.ForgeVersion.V0710 || version == ForgeModList.ForgeVersion.V0809) {
            LOGGER.info("Didn't rewrite for actually 1.7 or 1.8 client (stacking)");
            return super.write();
        }
        if (this.registryType == RegistryTypes.ITEMS) {
            return writeData(hasMore, RegistryTypes.ITEMS, idCount - removeCount, idMap);
        }
        return super.write();
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

    @Override
    public boolean shouldRemove(RegistryTypes type, String entry, int id) {
        ForgeModList.ForgeVersion version = handler.connection.get(ForgeModList.ForgeVersion.class);
        if (version != null && version.ordinal() >= ForgeModList.ForgeVersion.V0900.ordinal()) {
            if (type == RegistryTypes.ITEMS && (RegistryDatas.removeList.contains(entry))) {
                LOGGER.info("Removing registry {} entry: {}", registryType, entry);
                return true;
            }
        }
        return false;
    }
}
