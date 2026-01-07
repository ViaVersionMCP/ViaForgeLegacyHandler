package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.*;

public class ForgeRegistryData extends ForgePayload {

    public boolean hasMore;

    public RegistryTypes registryType;

    public int idCount, removeCount, substitutionCount;

    public Integer dummyCount;

    public Map<String, Integer> idMap = new HashMap<>();

    public List<String> substitutionList = new ArrayList<>();

    public List<String> dummyList = new ArrayList<>();

    public static List<String> removeList = new ArrayList<>();

    public static List<Integer> removeListID = new ArrayList<>();

    public ForgeRegistryData() {}

    @Override
    public void read(ForgePacketHandler handler, ByteBuf buffer, Direction direction, byte packetID) {
        super.read(handler, buffer, direction, packetID);
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
            substitutionList.add(substitution);
        }
        if (buffer.isReadable()) {
            dummyCount = readVarInt(buffer);
            for (int i = 0; i < dummyCount; i++) {
                String dummy = readString(buffer);
                dummyList.add(dummy);
            }
        }
    }

    @Override
    public ByteBuf write() {
        if (this.handler.connection.get(ForgeModList.ForgeVersion.class) == ForgeModList.ForgeVersion.V8) {
            LOGGER.info("Didn't rewrite for actually 1.8 client");
            return super.write();
        }
        if (this.registryType == RegistryTypes.ITEMS) {
            ByteBuf rewrite = Unpooled.buffer();
            rewrite.writeByte(this.packetID);
            rewrite.writeBoolean(hasMore);
            writeString(rewrite, RegistryTypes.ITEMS.type);
            writeVarInt(rewrite, idCount - removeCount);
            for (Map.Entry<String, Integer> entry : idMap.entrySet()) {
                writeString(rewrite, entry.getKey());
                writeVarInt(rewrite, entry.getValue());
            }
            writeVarInt(rewrite, substitutionCount);
            for (String substitution : substitutionList) {
                writeString(rewrite, substitution);
            }
            if (dummyCount != null) {
                writeVarInt(rewrite, dummyCount);
                for (String dummy : dummyList) {
                    writeString(rewrite, dummy);
                }
            }
            return rewrite;
        }
        return super.write();
    }

    @Override
    public String toString() {
        return "RegistryData name=" + registryType.type + " side=" + direction + " hasMore=" + hasMore
                + " ids=" + idCount + " substitutions=" + substitutionCount + " dummy=" + dummyCount;
    }

    public boolean shouldRemove(RegistryTypes type, String entry, int id) {
        ForgeModList.ForgeVersion version = handler.connection.get(ForgeModList.ForgeVersion.class);
        if (version != null && version.ordinal() >= ForgeModList.ForgeVersion.V9.ordinal()) {
            if (type == RegistryTypes.ITEMS && (removeList.contains(entry) || removeListID.contains(id))) {
                LOGGER.info("Removing registry {} entry: {}", registryType, entry);
                return true;
            }
        }
        return false;
    }

    public static class RegistryTypes {
        static final RegistryTypes BLOCKS = new RegistryTypes("minecraft:blocks");
        static final RegistryTypes ENTITIES = new RegistryTypes("minecraft:entities");
        static final RegistryTypes ITEMS = new RegistryTypes("minecraft:items");
        static final RegistryTypes POTIONS = new RegistryTypes("minecraft:potions");
        static final RegistryTypes VILLAGER = new RegistryTypes("minecraft:villagerprofessions");
        final String type;

        RegistryTypes(String type) {
            this.type = type;
        }

        static RegistryTypes from(String type) {
            return switch (type) {
                case "minecraft:blocks" -> BLOCKS;
                case "minecraft:entities" -> ENTITIES;
                case "minecraft:items" -> ITEMS;
                case "minecraft:potions" -> POTIONS;
                case "minecraft:villagerprofessions" -> VILLAGER;
                default -> new RegistryTypes(type);
            };
        }

        @Override
        public String toString() {
            return this.type;
        }
    }

    static {
        removeList.add("minecraft:lit_furnace");
        removeListID.add(0);
    }
}
