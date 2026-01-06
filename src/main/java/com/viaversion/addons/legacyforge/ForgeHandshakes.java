package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import io.netty.buffer.*;

public class ForgeHandshakes extends ForgePayload {

    public byte firstData;

    public String additionalData;

    public ForgeHandshakes() {
        super();
    }

    @Override
    public void read(ForgePacketHandler handler, ByteBuf buffer, Direction direction, byte packetID) {
        super.read(handler, buffer, direction, packetID);
        additionalData = "None";
        switch (packetID) {
            case -2:
                additionalData = "HandshakeReset";
                return;
            case -1:
                firstData = buffer.readByte(); // Phase
                return;
            case 0:
            case 1:
                firstData = buffer.readByte(); // Forge protocolVersion
                if (packetID == 0 && firstData > 1 && buffer.readableBytes() >= Integer.BYTES) {
                    additionalData = String.valueOf(buffer.readInt()); // overrideDimension
                }
                return;
            default:
                LOGGER.warn("Unknown discriminator {} ({} bytes remaining)", packetID, buffer.readableBytes());
        }
    }

    @Override
    public ByteBuf write() {
        ByteBuf out = Unpooled.buffer();
        switch (this.packetID) {
            case -1:
                out.writeByte(this.packetID);
                out.writeByte(this.firstData);
                return out;
            case 0:
                out.writeByte(this.packetID);
                out.writeByte(this.firstData);
                Integer i = parseInt(additionalData);
                if (i != null) {
                    out.writeInt(i);
                }
            default: return super.write();
        }
    }

    @Override
    public String toString() {
        return switch (this.packetID) {
            case -2 -> additionalData;
            case -1 -> "HandshakeAck phase=" + firstData;
            case 0 -> "ServerHello protocol=" + firstData + " overrideDimension=" + additionalData;
            case 1 -> "ClientHello protocol=" + firstData;
            default -> "Unknown protocol=" + firstData;
        };
    }
}
