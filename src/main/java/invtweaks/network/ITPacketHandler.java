package invtweaks.network;

import invtweaks.forge.InvTweaksMod;
import invtweaks.network.packets.ITPacket;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.network.INetHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@ChannelHandler.Sharable
public class ITPacketHandler extends SimpleChannelInboundHandler<ITPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ITPacket msg) throws Exception {
        final INetHandler handler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        InvTweaksMod.proxy.addScheduledTask(InvTweaksMod.proxy.getCurrentTick() + 1L, () -> msg.handle(handler));
    }
}
