package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_8;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.raphimc.vialegacy.protocol.release.r1_7_6_10tor1_8.Protocolr1_7_6_10Tor1_8;

import java.util.LinkedHashMap;
import java.util.Map;

public class ForgeHandshakesLegacy extends ForgeHandshakes {

    @Override
    public boolean shouldRewrite(PacketWrapper wrapper) {
        if (this.direction == Direction.CLIENTBOUND) {
            if (this.shouldHandleVanilla()) {
                this.handleVanillaClient();
                return false;
            }
        }
        return super.shouldRewrite(wrapper);
    }
    
    public void handleVanillaClient() {
        UserConnection connection = this.handler.connection;
        SimulatedClientStates state = connection.get(SimulatedClientStates.class);
        if (state == null) {
            state = new SimulatedClientStates();
            this.handler.connection.put(state);
        }
        switch (packetID) {
            case 0 -> {
                sendServerBound(connection, 1, firstData);
                sendServerBound(connection, 2, null);
                state.stage++;
            }
            case -1 -> sendServerBound(connection, -1, firstData);
            default -> {}
        }
    }

    public static void sendServerBound(UserConnection connection, int id, Byte phase) {
        ByteBuf payload = Unpooled.buffer();
        payload.writeByte(id);
        switch (id) {
            case -1 -> {
                payload.writeByte(phase);
                LOGGER.info("Simulated HandshakeAck for vanilla client (phase={}).", phase);
            }
            case 1 -> {
                payload.writeByte(2);
                LOGGER.info("Simulated ClientHello for vanilla client.");
            }
            case 2 -> {
                Map<String, String> modList = buildVanillaModList(connection);
                LOGGER.info("Simulated ModList for vanilla client.");
                writeVarInt(payload, modList.size());
                for (Map.Entry<String, String> entry : modList.entrySet()) {
                    writeString(payload, entry.getKey());
                    writeString(payload, entry.getValue());
                }
            }
            default -> {
                return;
            }
        }
        PacketWrapper packet = PacketWrapper.create(ServerboundPackets1_8.CUSTOM_PAYLOAD, connection);
        packet.write(Types.STRING, ForgePacketHandler.CHANNEL);
        packet.write(Types.REMAINING_BYTES, payload.array());
        packet.scheduleSendToServer(Protocolr1_7_6_10Tor1_8.class, false);
    }

    private static Map<String, String> buildVanillaModList(UserConnection connection) {
        Map<String, String> mods = new LinkedHashMap<>();
        mods.put("minecraft", connection.getProtocolInfo().protocolVersion().getName());
        mods.put("mcp", "9.05");
        mods.put("FML", "7.10.99.99");
        mods.put("Forge", "10.13.4.1614");
        return mods;
    }

    public static class SimulatedClientStates implements StorableObject {

        public int stage = 0;
    }
}
