package invtweaks.network.packets;

import io.netty.buffer.ByteBuf;

public class ITPacketClick implements ITPacket {
    public int slot;
    public int data;
    public int action;
    public int window;

    public ITPacketClick() {
    }

    public ITPacketClick(int _slot, int _data, int _action, int _window) {
        slot = _slot;
        data = _data;
        action = _action;
        window = _window;
    }

    @Override
    public void readBytes(ByteBuf bytes) {
        slot = bytes.readInt();
        data = bytes.readInt();
        action = bytes.readInt();
        window = bytes.readByte();
    }

    @Override
    public void writeBytes(ByteBuf bytes) {
        bytes.writeInt(slot);
        bytes.writeInt(data);
        bytes.writeInt(action);
        bytes.writeByte(window);
    }
}
