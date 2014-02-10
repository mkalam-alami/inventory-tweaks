package invtweaks.network.handlers;

import cpw.mods.fml.common.network.NetworkRegistry;
import invtweaks.network.packets.ITPacketSortComplete;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;

@ChannelHandler.Sharable
public class SortingCompleteMessageHandler extends SimpleChannelInboundHandler<ITPacketSortComplete> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ITPacketSortComplete msg) throws Exception {
        INetHandler handler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();

        if(handler instanceof NetHandlerPlayServer) {
            NetHandlerPlayServer serverHandler = (NetHandlerPlayServer)handler;
            EntityPlayerMP player = serverHandler.playerEntity;

            player.sendContainerToPlayer(player.openContainer);
        }
    }
}
