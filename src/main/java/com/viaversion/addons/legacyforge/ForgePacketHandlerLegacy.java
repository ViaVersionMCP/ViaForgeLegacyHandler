package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.type.Types;

public class ForgePacketHandlerLegacy extends ForgePacketHandler {

    public ForgePacketHandlerLegacy(Direction direction) {
        super(direction);
    }

    @Override
    protected void register() {
        handlerSoftFail(wrapper -> {
            connection = wrapper.user();
            final String name = wrapper.get(Types.STRING, 0);
            if (name.contains(CHANNEL)) {
                byte[] newPayload = this.loadPayload(true, direction, wrapper.read(Types.REMAINING_BYTES));
                if (this.onCallback(wrapper)) {
                    wrapper.write(Types.REMAINING_BYTES, newPayload);
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
                return new ForgeRegistryDataLegacy();
            }
            default -> {
                return new ForgeHandshakes();
            }
        }
    }

    @Override
    public String toString() {
        return "1.7<->1.8";
    }
}
