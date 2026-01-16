package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.Protocol1_11_1To1_12;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import io.netty.buffer.ByteBuf;

import java.util.Map;

public class ForgeRegistryDataNewer extends ForgeRegistryData {

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
        if (this.registryType == RegistryTypes.SOUNDS) {
            return writeData(hasMore, RegistryTypes.SOUNDS, idCount - removeCount, idMap);
        }
        return super.write();
    }

    @Override
    public boolean shouldRewrite(PacketWrapper wrapper) {
        if (this.handler.connection.get(ForgeModList.ForgeVersion.class) == ForgeModList.ForgeVersion.V1202) {
            if (this.handler.connection.getProtocolInfo().serverProtocolVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
                if (this.registryType == RegistryTypes.ITEMS) {
                    Map<String, Integer> items = RegistryDatas.initItems();
                    Map<String, Integer> recipes = RegistryDatas.initRecipes();
                    wrapper.write(Types.REMAINING_BYTES, writeData(true, RegistryTypes.ITEMS, items.size(), items).array());
                    LOGGER.info("Resending Item Registry for 1.12.2.");
                    wrapper.send(Protocol1_11_1To1_12.class);
                    PacketWrapper recipePacket = PacketWrapper.create(ClientboundPackets1_9_3.CUSTOM_PAYLOAD, handler.connection);
                    recipePacket.write(Types.STRING, ForgePacketHandler.CHANNEL);
                    recipePacket.write(Types.REMAINING_BYTES, writeData(false, RegistryTypes.RECIPES, recipes.size(), recipes).array());
                    recipePacket.send(Protocol1_11_1To1_12.class, false);
                    LOGGER.info("Sending Recipes Registry for 1.12.2.");
                    return false;
                }
            } else if (this.hasMore || this.registryType == RegistryTypes.RECIPES) {
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean shouldRemove(RegistryTypes type, String entry, int id) {
        if (type == RegistryTypes.SOUNDS) {
            if (RegistryDatas.removeList.contains(entry)) {
                LOGGER.info("Removing registry {} entry: {}", registryType, entry);
                return true;
            }
        }
        return false;
    }
}
