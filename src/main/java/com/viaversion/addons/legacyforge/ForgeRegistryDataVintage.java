package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import io.netty.buffer.ByteBuf;

public class ForgeRegistryDataVintage extends ForgeRegistryData {

    private ForgeModList.ForgeVersion version;

    @Override
    public void read(boolean legacy, ForgePacketHandler handler, ByteBuf buffer, Direction direction, byte packetID) {
        super.read(legacy, handler, buffer, direction, packetID);
        version = this.handler.connection.get(ForgeModList.ForgeVersion.class);
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
