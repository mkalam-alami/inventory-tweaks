package invtweaks.network.packets;

import io.netty.buffer.ByteBuf;

public interface ITPacket {
    public void readBytes(ByteBuf bytes);

    public void writeBytes(ByteBuf bytes);
}
