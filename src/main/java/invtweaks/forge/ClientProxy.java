package invtweaks.forge;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;
import invtweaks.InvTweaks;
import invtweaks.InvTweaksConfig;
import invtweaks.InvTweaksItemTreeLoader;
import invtweaks.api.IItemTreeListener;
import invtweaks.network.packets.ITPacketClick;
import invtweaks.network.packets.ITPacketSortComplete;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ClientProxy extends CommonProxy {
    private InvTweaks instance;
    private ForgeClientTick clientTick;
    public boolean serverSupportEnabled = false;
    public boolean serverSupportDetected = false;

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

        FMLCommonHandler.instance().bus().register(clientTick);
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void notifyPickup(PlayerEvent.ItemPickupEvent e) {
        instance.setItemPickupPending(true);
    }

    @Override
    public void setServerAssistEnabled(boolean enabled) {
        serverSupportEnabled = serverSupportDetected && enabled;
        //InvTweaks.log.info("Server has support: " + serverSupportDetected + " support enabled: " + serverSupportEnabled);
    }

    @Override
    public void setServerHasInvTweaks(boolean hasInvTweaks) {
        serverSupportDetected = hasInvTweaks;
        serverSupportEnabled = hasInvTweaks && !InvTweaks.getConfigManager().getConfig()
                                                         .getProperty(InvTweaksConfig.PROP_ENABLE_SERVER_ITEMSWAP)
                                                         .equals(InvTweaksConfig.VALUE_FALSE);
        //InvTweaks.log.info("Server has support: " + hasInvTweaks + " support enabled: " + serverSupportEnabled);
    }

    @Override
    public void slotClick(PlayerControllerMP playerController, int windowId, int slot, int data, int action,
                          EntityPlayer player) {
        //int modiferKeys = (shiftHold) ? 1 : 0 /* XXX Placeholder */;
        if(serverSupportEnabled) {
            player.openContainer.slotClick(slot, data, action, player);

            invtweaksChannel.get(Side.CLIENT).writeOutbound(new ITPacketClick(slot, data, action));
        } else {
            playerController.windowClick(windowId, slot, data, action, player);
        }
    }

    @Override
    public void sortComplete() {
        if(serverSupportEnabled) {
            invtweaksChannel.get(Side.CLIENT).writeOutbound(new ITPacketSortComplete());
        }
    }

    @Override
    public void addOnLoadListener(IItemTreeListener listener) {
        InvTweaksItemTreeLoader.addOnLoadListener(listener);
    }

    @Override
    public boolean removeOnLoadListener(IItemTreeListener listener) {
        return InvTweaksItemTreeLoader.removeOnLoadListener(listener);
    }

    @Override
    public void setSortKeyEnabled(boolean enabled) {
        instance.setSortKeyEnabled(enabled);
    }

    @Override
    public void setTextboxMode(boolean enabled) {
        instance.setTextboxMode(enabled);
    }

    @Override
    public int compareItems(ItemStack i, ItemStack j) {
        return instance.compareItems(i, j);
    }
}
