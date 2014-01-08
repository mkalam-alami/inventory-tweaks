package invtweaks.network;

import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import invtweaks.InvTweaksConst;
import invtweaks.network.packets.ITPacket;
import invtweaks.network.packets.ITPacketClick;
import invtweaks.network.packets.ITPacketLogin;
import invtweaks.network.packets.ITPacketSortComplete;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ITMessageToMessageCodec extends FMLIndexedMessageToMessageCodec<ITPacket> {
    public ITMessageToMessageCodec() {
        addDiscriminator(InvTweaksConst.PACKET_LOGIN, ITPacketLogin.class);
        addDiscriminator(InvTweaksConst.PACKET_CLICK, ITPacketClick.class);
        addDiscriminator(InvTweaksConst.PACKET_SORTCOMPLETE, ITPacketSortComplete.class);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ITPacket source, ByteBuf target) throws Exception {
        source.writeBytes(target);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, ITPacket target) {
        target.readBytes(source);
    }
}
