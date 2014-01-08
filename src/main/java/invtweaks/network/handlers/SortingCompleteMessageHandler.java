package invtweaks.network.handlers;

import invtweaks.network.packets.ITPacketSortComplete;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SortingCompleteMessageHandler extends SimpleChannelInboundHandler<ITPacketSortComplete> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ITPacketSortComplete msg) throws Exception {

    }
}
