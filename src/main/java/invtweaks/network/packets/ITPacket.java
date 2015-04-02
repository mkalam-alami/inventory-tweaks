package invtweaks.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public interface ITPacket {
    public void readBytes(ByteBuf bytes);

    public void writeBytes(ByteBuf bytes);

    public void handle(INetHandler handler);
}
