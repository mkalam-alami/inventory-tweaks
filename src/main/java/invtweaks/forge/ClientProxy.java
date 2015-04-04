package invtweaks.forge;

import invtweaks.*;
import invtweaks.api.IItemTreeListener;
import invtweaks.api.SortingMethod;
import invtweaks.api.container.ContainerSection;
import invtweaks.network.packets.ITPacketClick;
import invtweaks.network.packets.ITPacketSortComplete;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {
    public static final KeyBinding KEYBINDING_SORT = new KeyBinding("invtweaks.key.sort", Keyboard.KEY_R, "invtweaks.key.category");
    public boolean serverSupportEnabled = false;
    public boolean serverSupportDetected = false;
    private InvTweaks instance;
    private ForgeClientTick clientTick;

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        InvTweaks.log = e.getModLog();
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);

        Minecraft mc = FMLClientHandler.instance().getClient();
        // Instantiate mod core
        instance = new InvTweaks(mc);
        clientTick = new ForgeClientTick(instance);

        FMLCommonHandler.instance().bus().register(clientTick);
        MinecraftForge.EVENT_BUS.register(this);

        ClientRegistry.registerKeyBinding(KEYBINDING_SORT);
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

            invtweaksChannel.get(Side.CLIENT).writeOutbound(new ITPacketClick(slot, data, action, windowId));
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

    @Override
    public void sort(ContainerSection section, SortingMethod method) {
        // TODO: This seems like something useful enough to be a util method somewhere.
        Minecraft mc = FMLClientHandler.instance().getClient();
        Container currentContainer = mc.thePlayer.inventoryContainer;
        if(mc.currentScreen instanceof GuiContainer) {
            currentContainer = ((GuiContainer) mc.currentScreen).inventorySlots;
        }

        try {
            new InvTweaksHandlerSorting(mc, InvTweaks.getConfigManager().getConfig(), section, method, InvTweaksObfuscation.getSpecialChestRowSize(currentContainer)).sort();
        } catch(Exception e) {
            InvTweaks.logInGameErrorStatic("invtweaks.sort.chest.error", e);
            e.printStackTrace();
        }
    }

    @Override
    public long getCurrentTick() {
        return Minecraft.getMinecraft().theWorld.getTotalWorldTime();
    }

    @SubscribeEvent
    public void onConnectionToServer(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        setServerHasInvTweaks(false);
    }
}
