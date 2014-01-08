package invtweaks.network.packets;

import invtweaks.InvTweaksConst;
import io.netty.buffer.ByteBuf;

public class ITPacketLogin implements ITPacket {
    public byte protocolVersion = InvTweaksConst.PROTOCOL_VERSION;

    @Override
    public void readBytes(ByteBuf bytes) {
        protocolVersion = bytes.readByte();
    }

    @Override
    public void writeBytes(ByteBuf bytes) {
        bytes.writeByte(protocolVersion);
    }
}
