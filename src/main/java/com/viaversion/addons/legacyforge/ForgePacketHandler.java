package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static com.viaversion.addons.legacyforge.ForgePayload.*;

public class ForgePacketHandler extends PacketHandlers {

    public ForgePacketHandler(Direction direction, boolean legacy) {
        this.direction = direction;
        this.legacy = legacy;
    }

    public Direction direction;

    public boolean legacy;

    public byte packetID;

    public UserConnection connection;

    @Override
    protected void register() {
        handlerSoftFail(wrapper -> {
            connection = wrapper.user();
            final String name = wrapper.get(Types.STRING, 0);
            if (name.contains("FML|HS")) {
                byte[] newPayload = this.loadPayload(direction, wrapper.read(Types.REMAINING_BYTES));
                wrapper.write(Types.REMAINING_BYTES, newPayload);
            }
            if (name.equals("FML")) {
                if (this.shouldCancelPayload(wrapper.read(Types.REMAINING_BYTES))) {
                    wrapper.cancel();
                }
            }
        });
    }

    public byte[] loadPayload(final Direction direction, final byte[] payload) {
        if (payload == null || payload.length == 0) {
            log(direction, "Empty Forge handshake payload", true);
            return payload;
        }
        ByteBuf buffer = Unpooled.wrappedBuffer(payload);
        try {
            if (!buffer.isReadable()) {
                log(direction, "Unreadable Forge handshake payload", true);
                return buffer.array();
            }
            packetID = buffer.readByte();
            ForgePayload type;
            switch (packetID) {
                case 2 -> type = new ForgeModList();
                case 3 -> type = new ForgeRegistryData();
                default -> type = new ForgeHandshakes();
            }
            type.read(this, buffer, direction, packetID);
            log(direction, type.toString(), false);
            return type.write().array();
        } catch (RuntimeException exception) {
            LOGGER.warn("Failed to parse Forge Payload", exception);
        }
        return buffer.array();
    }

    public boolean shouldCancelPayload(byte[] payload) {
        if (payload == null || payload.length == 0) {
            log(direction, "Empty Forge message payload", true);
            return true;
        } else {
            return payload[0] == 3; // Removed since 1.9
        }
    }
}
