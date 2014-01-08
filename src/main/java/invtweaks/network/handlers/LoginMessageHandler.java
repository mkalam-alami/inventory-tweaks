package invtweaks.network.handlers;

import invtweaks.InvTweaksConst;
import invtweaks.forge.InvTweaksMod;
import invtweaks.network.packets.ITPacketLogin;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class LoginMessageHandler extends SimpleChannelInboundHandler<ITPacketLogin> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ITPacketLogin msg) throws Exception {
        if(msg.protocolVersion == InvTweaksConst.PROTOCOL_VERSION) {
            InvTweaksMod.proxy.setServerHasInvTweaks(true);
        }
    }
}
