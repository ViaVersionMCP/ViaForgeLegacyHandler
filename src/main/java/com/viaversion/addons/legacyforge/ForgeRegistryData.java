package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.*;

public abstract class ForgeRegistryData extends ForgePayload {

    public boolean hasMore;

    public RegistryTypes registryType;

    public int idCount, removeCount, substitutionCount;

    public Integer dummyCount;

    public Map<String, Integer> idMap = new HashMap<>();

    public List<String> substitutionList = new ArrayList<>();

    public List<String> dummyList = new ArrayList<>();

    @Override
    public void read(boolean legacy, ForgePacketHandler handler, ByteBuf buffer, Direction direction, byte packetID) {
        super.read(legacy, handler, buffer, direction, packetID);
    }

    @Override
    public ByteBuf write() {
        return super.write();
    }

    public static ByteBuf writeData(boolean more, RegistryTypes types, int counts, Map<String, Integer> registry) {
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
        return "RegistryData name=" + registryType.type + " side=" + direction + " hasMore=" + hasMore
                + " counts=" + idCount + " substitutions=" + substitutionCount + " dummy=" + dummyCount;
    }

    @Override
    public boolean shouldRewrite(PacketWrapper wrapper) {
        return super.shouldRewrite(wrapper);
    }

    public boolean shouldRemove(RegistryTypes type, String entry, int id) {
        return false;
    }

}
