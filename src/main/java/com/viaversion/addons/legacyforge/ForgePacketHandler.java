package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static com.viaversion.addons.legacyforge.ForgePayload.*;

public class ForgePacketHandler extends PacketHandlers {

    public static final String CHANNEL = "FML|HS";

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
            if (name.contains(CHANNEL)) {
                byte[] newPayload = this.loadPayload(false, direction, wrapper.read(Types.REMAINING_BYTES));
                if (this.onCallback(wrapper)) {
                    wrapper.write(Types.REMAINING_BYTES, newPayload);
                }
            }
            if (name.equals("FML")) {
                if (this.onCallback(wrapper.read(Types.REMAINING_BYTES))) {
                    wrapper.cancel();
                }
            }
        });
    }

    public byte[] loadPayload(final boolean legacy, final Direction direction, final byte[] payload) {
        if (payload == null || payload.length == 0) {
            log(this.toString(), direction, "Empty Forge handshake payload", true);
            return payload;
        }
        ByteBuf buffer = Unpooled.wrappedBuffer(payload);
        try {
            if (!buffer.isReadable()) {
                log(this.toString(), direction, "Unreadable Forge handshake payload", true);
                return buffer.array();
            }
            this.packetID = buffer.readByte();
            switch (packetID) {
                case 2 -> this.type = new ForgeModList();
                case 3 -> {
                    if (this instanceof ForgePacketHandlerLegacy) {
                        this.type = new ForgeRegistryDataLegacy();
                    } else if (this instanceof ForgePacketHandlerNewer) {
                        this.type = new ForgeRegistryDataNewer();
                    } else {
                        this.type = new ForgeRegistryDataVintage();
                    }
                }
                default -> this.type = new ForgeHandshakes();
            }
            this.type.read(legacy, this, buffer, direction, packetID);
            log(this.toString(), direction, type.toString(), false);
            return this.type.write().array();
        } catch (RuntimeException exception) {
            LOGGER.warn("Failed to parse Forge Payload", exception);
        }
        return buffer.array();
    }

    public boolean onCallback(PacketWrapper wrapper) {
        if (this.type != null) {
            return this.type.shouldRewrite(wrapper);
        } else {
            return false;
        }
    }

    public boolean onCallback(byte[] payload) {
        if (payload == null || payload.length == 0) {
            log(this.toString(), direction, "Empty Forge message payload", true);
            return true;
        } else {
            return payload[0] == 3; // Removed since 1.9
        }
    }

    @Override
    public String toString() {
        return "1.8<->1.9";
    }
}
