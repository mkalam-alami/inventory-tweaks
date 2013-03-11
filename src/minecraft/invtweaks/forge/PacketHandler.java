package invtweaks.forge;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

public class PacketHandler implements IPacketHandler {
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
        if(packet.data.length == 1) {
            if(packet.data[0] == 0x55) {
                InvTweaksMod.proxy.setServerHasInvTweaks(true);
            }
        }
    }
}
