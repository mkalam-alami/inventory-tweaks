package invtweaks.network.handlers;

import invtweaks.network.packets.ITPacketClick;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClickMessageHandler extends SimpleChannelInboundHandler<ITPacketClick> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ITPacketClick msg) throws Exception {

    }
}
