package invtweaks.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;

public class ITPacketSortComplete implements ITPacket {
    @Override
    public void readBytes(ByteBuf bytes) {

    }

    @Override
    public void writeBytes(ByteBuf bytes) {

    }

    @Override
    public void handle(INetHandler handler) {
        if(handler instanceof NetHandlerPlayServer) {
            NetHandlerPlayServer serverHandler = (NetHandlerPlayServer) handler;
            EntityPlayerMP player = serverHandler.playerEntity;

            player.sendContainerToPlayer(player.openContainer);
        }
    }
}
