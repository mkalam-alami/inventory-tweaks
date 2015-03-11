package invtweaks.network.handlers;

import invtweaks.network.packets.ITPacketClick;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@ChannelHandler.Sharable
public class ClickMessageHandler extends SimpleChannelInboundHandler<ITPacketClick> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ITPacketClick msg) throws Exception {
        INetHandler handler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();

        if(handler instanceof NetHandlerPlayServer) {
            NetHandlerPlayServer serverHandler = (NetHandlerPlayServer) handler;
            EntityPlayerMP player = serverHandler.playerEntity;

            if(player.openContainer.windowId == msg.window) {
                player.openContainer.slotClick(msg.slot, msg.data, msg.action, player);
            }
            // TODO: Might want to set a flag to ignore all packets until next sortcomplete even if client window changes.
        }
    }
}
