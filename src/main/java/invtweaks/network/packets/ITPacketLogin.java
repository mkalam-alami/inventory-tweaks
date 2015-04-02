package invtweaks.network.packets;

import invtweaks.InvTweaksConst;
import invtweaks.forge.InvTweaksMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

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

    @Override
    public void handle(INetHandler handler) {
        if(protocolVersion == InvTweaksConst.PROTOCOL_VERSION) {
            InvTweaksMod.proxy.setServerHasInvTweaks(true);
        }
    }
}
