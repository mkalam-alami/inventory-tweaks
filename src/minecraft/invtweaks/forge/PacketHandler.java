package invtweaks.forge;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import invtweaks.InvTweaks;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.util.ArrayList;

public class PacketHandler implements IPacketHandler {
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
        if(packet.data.length == 1) {
            if(packet.data[0] == 0x55) {
                InvTweaksMod.proxy.setServerHasInvTweaks(true);
            }
        } else {
            EntityPlayerMP realPlayer = (EntityPlayerMP) player;
            ByteArrayDataInput packetData = ByteStreams.newDataInput(packet.data);
            byte packetId = packetData.readByte();
            if (packetId == 0x01) {
                int slot = packetData.readInt();
                int buttonPressed = packetData.readInt();
                int modiferKeys = packetData.readInt();

                realPlayer.openContainer.slotClick(slot, buttonPressed, modiferKeys, realPlayer);

                ArrayList arraylist = new ArrayList();

                for (int i = 0; i < realPlayer.openContainer.inventorySlots.size(); ++i) {
                    arraylist.add(((Slot) realPlayer.openContainer.inventorySlots.get(i)).getStack());
                }

                realPlayer.sendContainerAndContentsToPlayer(realPlayer.openContainer, arraylist);
            }
        }
    }
}
