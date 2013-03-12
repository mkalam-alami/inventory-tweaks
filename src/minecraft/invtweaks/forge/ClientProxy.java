package invtweaks.forge;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.IPickupNotifier;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import invtweaks.InvTweaks;
import invtweaks.InvTweaksConfig;
import invtweaks.InvTweaksItemTreeLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;

public class ClientProxy extends CommonProxy implements IPickupNotifier {
    private InvTweaks instance;
    private ForgeClientTick clientTick;
    public boolean serverSupportEnabled = false;

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        InvTweaks.log = e.getModLog();
    }

    @Override
    public void init(FMLInitializationEvent e) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        // Instantiate mod core
        instance = new InvTweaks(mc);
        clientTick = new ForgeClientTick(instance);
        TickRegistry.registerTickHandler(clientTick, Side.CLIENT);
        GameRegistry.registerPickupHandler(this);
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        if(!InvTweaks.getConfigManager().getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_FORGE_ITEMTREE).equals(InvTweaksConfig.VALUE_FALSE)) {
            InvTweaksItemTreeLoader.addOnLoadListener(new ForgeItemTreeListener());
        }
    }

    @Override
    public void notifyPickup(EntityItem item, EntityPlayer player) {
        instance.setItemPickupPending(true);
    }

    @Override
    public void setServerHasInvTweaks(boolean hasInvTweaks) {
        serverSupportEnabled = hasInvTweaks && !InvTweaks.getConfigManager().getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_SERVER_ITEMSWAP).equals(InvTweaksConfig.VALUE_FALSE);
        InvTweaks.log.info("Server has support: " + hasInvTweaks + " support enabled: " + serverSupportEnabled);
    }

    @Override
    public void slotClick(PlayerControllerMP playerController,
                          int windowId, int slot, int clickButton,
                          boolean shiftHold, EntityPlayer player) {
        int modiferKeys = (shiftHold) ? 1 : 0 /* XXX Placeholder */;
        if (serverSupportEnabled) {
            player.openContainer.slotClick(slot, clickButton, modiferKeys, player);

            ByteArrayDataOutput packetData = ByteStreams.newDataOutput();
            packetData.writeByte(0x01);
            packetData.writeInt(slot);
            packetData.writeInt(clickButton);
            packetData.writeInt(modiferKeys);

            Packet250CustomPayload packet = PacketDispatcher.getPacket("InventoryTweaks", packetData.toByteArray());
            PacketDispatcher.sendPacketToServer(packet);
        } else {
            playerController.windowClick(windowId, slot, clickButton, modiferKeys, player);
        }
    }
}
