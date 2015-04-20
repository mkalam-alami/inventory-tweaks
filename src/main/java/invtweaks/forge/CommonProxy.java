package invtweaks.forge;

import invtweaks.InvTweaksConst;
import invtweaks.api.IItemTreeListener;
import invtweaks.api.InvTweaksAPI;
import invtweaks.api.SortingMethod;
import invtweaks.api.container.ContainerSection;
import invtweaks.container.IContainerManager;
import invtweaks.network.ITMessageToMessageCodec;
import invtweaks.network.ITPacketHandlerServer;
import invtweaks.network.packets.ITPacketLogin;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumMap;

public class CommonProxy implements InvTweaksAPI {
    protected static EnumMap<Side, FMLEmbeddedChannel> invtweaksChannel;

    public void preInit(FMLPreInitializationEvent e) {
    }

    public void init(FMLInitializationEvent e) {
        invtweaksChannel = NetworkRegistry.INSTANCE
                .newChannel(InvTweaksConst.INVTWEAKS_CHANNEL, new ITMessageToMessageCodec());
        invtweaksChannel.get(Side.SERVER).pipeline().addAfter("ITMessageToMessageCodec#0", "InvTweaks Handler Server", new ITPacketHandlerServer());

        FMLCommonHandler.instance().bus().register(this);
    }

    public void postInit(FMLPostInitializationEvent e) {
    }

    public void setServerAssistEnabled(boolean enabled) {
    }

    public void setServerHasInvTweaks(boolean hasInvTweaks) {
    }

    /* Action values:
     * 0: Standard Click
     * 1: Shift-Click
     * 2: Move item to/from hotbar slot (Depends on current slot and hotbar slot being full or empty)
     * 3: Duplicate item (only while in creative)
     * 4: Drop item
     * 5: Spread items (Drag behavior)
     * 6: Merge all valid items with held item
     */
    @SideOnly(Side.CLIENT)
    public void slotClick(PlayerControllerMP playerController, int windowId, int slot, int data, int action,
                          EntityPlayer player) {
    }

    public void sortComplete() {

    }

    @Override
    public void addOnLoadListener(IItemTreeListener listener) {

    }

    @Override
    public boolean removeOnLoadListener(IItemTreeListener listener) {
        return false;
    }

    @Override
    public void setSortKeyEnabled(boolean enabled) {
    }

    @Override
    public void setTextboxMode(boolean enabled) {
    }

    @Override
    public int compareItems(ItemStack i, ItemStack j) {
        return 0;
    }

    @Override
    public void sort(ContainerSection section, SortingMethod method) {
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent e) {
        FMLEmbeddedChannel channel = invtweaksChannel.get(Side.SERVER);

        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(
                FMLOutboundHandler.OutboundTarget.PLAYER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(e.player);

        channel.writeOutbound(new ITPacketLogin());
    }

    public void addServerScheduledTask(Runnable task) {
        MinecraftServer.getServer().addScheduledTask(task);
    }

    public void addClientScheduledTask(Runnable task) {
    }
}
