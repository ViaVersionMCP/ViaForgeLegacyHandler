package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.type.Types;

public class ForgePacketHandlerNewer extends ForgePacketHandler {

    public ForgePacketHandlerNewer(Direction direction) {
        super(direction);
    }

    @Override
    protected void register() {
        handlerSoftFail(wrapper -> {
            connection = wrapper.user();
            if (this.direction == Direction.SERVERBOUND) {
                final String name = wrapper.passthrough(Types.STRING);
                if (name.contains(CHANNEL)) {
                    byte[] newPayload = this.loadPayload(false, direction, wrapper.read(Types.SERVERBOUND_CUSTOM_PAYLOAD_DATA));
                    wrapper.write(Types.SERVERBOUND_CUSTOM_PAYLOAD_DATA, newPayload);
                }
            } else {
                final String name = wrapper.get(Types.STRING, 0);
                if (name.contains(CHANNEL)) {
                    byte[] newPayload = this.loadPayload(false, direction, wrapper.read(Types.REMAINING_BYTES));
                    if (this.onCallback(wrapper)) {
                        wrapper.write(Types.REMAINING_BYTES, newPayload);
                    }
                }
            }
        });
    }

    @Override
    public ForgePayload getType() {
        switch (this.packetID) {
            case 2 -> {
                return new ForgeModList();
            }
            case 3 -> {
                return new ForgeRegistryDataNewer();
            }
            default -> {
                return new ForgeHandshakes();
            }
        }
    }

    @Override
    public String toString() {
        return "1.11<->1.12";
    }
}
