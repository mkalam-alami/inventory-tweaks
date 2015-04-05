package invtweaks.network;

import invtweaks.forge.InvTweaksMod;
import invtweaks.network.packets.ITPacket;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.INetHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class ITPacketHandlerClient extends SimpleChannelInboundHandler<ITPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ITPacket msg) throws Exception {
        final NetHandlerPlayClient handler = (NetHandlerPlayClient)ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        Minecraft.getMinecraft().addScheduledTask(() -> msg.handle(handler));
    }
}
