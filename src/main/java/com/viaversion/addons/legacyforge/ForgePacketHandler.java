package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static com.viaversion.addons.legacyforge.ForgePayload.*;

public class ForgePacketHandler extends PacketHandlers {

    public ForgePacketHandler(Direction direction) {
        this.direction = direction;
    }

    public Direction direction;

    public byte packetID;

    public UserConnection connection;

    public ForgePayload type;

    @Override
    protected void register() {
        handlerSoftFail(wrapper -> {
            connection = wrapper.user();
            final String name = wrapper.get(Types.STRING, 0);
            if (name.contains("FML|HS")) {
                byte[] newPayload = this.loadPayload(direction, wrapper.read(Types.REMAINING_BYTES));
                if (this.shouldCancelPayload()) {
                    wrapper.cancel();
                } else {
                    wrapper.write(Types.REMAINING_BYTES, newPayload);
                }
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
            this.packetID = buffer.readByte();
            switch (packetID) {
                case 2 -> this.type = new ForgeModList();
                case 3 -> this.type = new ForgeRegistryData();
                default -> this.type = new ForgeHandshakes();
            }
            this.type.read(this, buffer, direction, packetID);
            log(direction, type.toString(), false);
            return this.type.write().array();
        } catch (RuntimeException exception) {
            LOGGER.warn("Failed to parse Forge Payload", exception);
        }
        return buffer.array();
    }

    public boolean shouldCancelPayload() {
        if (this.type != null) {
            return this.type.shouldCancelPayload();
        } else {
            return false;
        }
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
