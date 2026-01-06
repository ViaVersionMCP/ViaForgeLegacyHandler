package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.*;
import net.lenni0451.lambdaevents.EventHandler;
import net.raphimc.vialegacy.protocol.release.r1_7_2_5tor1_7_6_10.packet.ClientboundPackets1_7_2;
import net.raphimc.vialegacy.protocol.release.r1_7_6_10tor1_8.Protocolr1_7_6_10Tor1_8;
import net.raphimc.viaproxy.ViaProxy;
import net.raphimc.viaproxy.plugins.ViaProxyPlugin;
import net.raphimc.viaproxy.plugins.events.ViaLoadingEvent;

@SuppressWarnings("unused")
public class ForgeChannelHandler extends ViaProxyPlugin {

    @Override
    public void onEnable() {
        ViaProxy.EVENT_MANAGER.register(this);
    }

    @EventHandler
    public void onViaLoading(final ViaLoadingEvent event) {
        final Protocol1_8To1_9 protocol = Via.getManager().getProtocolManager().getProtocol(Protocol1_8To1_9.class);
        final Protocolr1_7_6_10Tor1_8 oldProtocol = Via.getManager().getProtocolManager().getProtocol(Protocolr1_7_6_10Tor1_8.class);
        if (protocol == null || oldProtocol == null) {
            throw new IllegalStateException();
        }
        protocol.appendClientbound(ClientboundPackets1_8.CUSTOM_PAYLOAD, new ForgePacketHandler(Direction.CLIENTBOUND, false));
        protocol.appendServerbound(ServerboundPackets1_9.CUSTOM_PAYLOAD, new ForgePacketHandler(Direction.SERVERBOUND, false));
        oldProtocol.appendClientbound(ClientboundPackets1_7_2.CUSTOM_PAYLOAD, new ForgePacketHandler(Direction.CLIENTBOUND, true));
        oldProtocol.appendServerbound(ServerboundPackets1_8.CUSTOM_PAYLOAD, new ForgePacketHandler(Direction.SERVERBOUND, true));
    }

}
