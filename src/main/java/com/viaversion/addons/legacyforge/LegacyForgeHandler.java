package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import net.raphimc.vialegacy.protocol.release.r1_7_2_5tor1_7_6_10.packet.ClientboundPackets1_7_2;
import net.raphimc.vialegacy.protocol.release.r1_7_6_10tor1_8.Protocolr1_7_6_10Tor1_8;

public class LegacyForgeHandler {

    public static void init() {
        final Protocol1_8To1_9 protocol8 = Via.getManager().getProtocolManager().getProtocol(Protocol1_8To1_9.class);
        final Protocolr1_7_6_10Tor1_8 protocol7 = Via.getManager().getProtocolManager().getProtocol(Protocolr1_7_6_10Tor1_8.class);
        if (protocol8 == null || protocol7 == null) {
            throw new IllegalStateException();
        }
        protocol8.appendClientbound(ClientboundPackets1_8.CUSTOM_PAYLOAD, new ForgePacketHandler(Direction.CLIENTBOUND));
        protocol8.appendServerbound(ServerboundPackets1_9.CUSTOM_PAYLOAD, new ForgePacketHandler(Direction.SERVERBOUND));
        protocol7.appendClientbound(ClientboundPackets1_7_2.CUSTOM_PAYLOAD, new ForgePacketHandlerLegacy(Direction.CLIENTBOUND));
        protocol7.appendServerbound(ServerboundPackets1_8.CUSTOM_PAYLOAD, new ForgePacketHandlerLegacy(Direction.SERVERBOUND));
    }
}
