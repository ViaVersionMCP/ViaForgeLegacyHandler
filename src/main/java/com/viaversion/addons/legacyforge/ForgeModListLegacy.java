package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;

public class ForgeModListLegacy extends ForgeModList {

    @Override
    public boolean shouldRewrite(PacketWrapper wrapper) {
        if (this.direction == Direction.CLIENTBOUND) {
            if (this.shouldHandleVanilla()) {
                UserConnection connection = this.handler.connection;
                ForgeHandshakesLegacy.SimulatedClientStates state = connection.get(ForgeHandshakesLegacy.SimulatedClientStates.class);
                if (state != null) {
                    ForgeHandshakesLegacy.sendServerBound(connection, -1, (byte) 2);
                    return super.shouldRewrite(wrapper);
                }
                LOGGER.warn("Shouldn't go here?");
            }
        }
        return super.shouldRewrite(wrapper);
    }

}
