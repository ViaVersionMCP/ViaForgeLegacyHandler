package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.type.Types;

public class ForgePacketHandlerVintage extends ForgePacketHandler {

    public ForgePacketHandlerVintage(Direction direction) {
        super(direction);
    }

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
        });
    }

    @Override
    public String toString() {
        return "1.11<->1.12";
    }
}
