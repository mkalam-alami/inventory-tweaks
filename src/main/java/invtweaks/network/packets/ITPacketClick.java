package invtweaks.network.packets;

import io.netty.buffer.ByteBuf;

public class ITPacketClick implements ITPacket {
    public int slot;
    public int data;
    public int action;

    public ITPacketClick() {
    }

    public ITPacketClick(int _slot, int _data, int _action) {
        slot = _slot;
        data = _data;
        action = _action;
    }

    @Override
    public void readBytes(ByteBuf bytes) {
        slot = bytes.readInt();
        data = bytes.readInt();
        action = bytes.readInt();
    }

    @Override
    public void writeBytes(ByteBuf bytes) {
        bytes.writeInt(slot);
        bytes.writeInt(data);
        bytes.writeInt(action);
    }
}
