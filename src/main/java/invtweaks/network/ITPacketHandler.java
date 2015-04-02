package invtweaks.network;

import invtweaks.network.packets.ITPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.network.INetHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class ITPacketHandler extends SimpleChannelInboundHandler<ITPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ITPacket msg) throws Exception {
        INetHandler handler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        msg.handle(handler);
    }
}
