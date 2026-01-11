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
            /*if (name.contains("FML|HS")) {
                byte[] newPayload = this.loadPayload(direction, wrapper.read(Types.REMAINING_BYTES));
                wrapper.write(Types.REMAINING_BYTES, newPayload);
            }*/
        });
    }
}
