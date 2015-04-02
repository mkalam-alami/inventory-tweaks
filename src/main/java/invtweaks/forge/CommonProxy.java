package invtweaks.forge;

import invtweaks.InvTweaksConst;
import invtweaks.TickScheduledTask;
import invtweaks.api.IItemTreeListener;
import invtweaks.api.InvTweaksAPI;
import invtweaks.api.SortingMethod;
import invtweaks.api.container.ContainerSection;
import invtweaks.network.ITMessageToMessageCodec;
import invtweaks.network.ITPacketHandler;
import invtweaks.network.packets.ITPacketLogin;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
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
import java.util.concurrent.PriorityBlockingQueue;

public class CommonProxy implements InvTweaksAPI {
    protected static EnumMap<Side, FMLEmbeddedChannel> invtweaksChannel;
    private PriorityBlockingQueue<TickScheduledTask> scheduledTasks = new PriorityBlockingQueue<TickScheduledTask>(16,
            (TickScheduledTask o1, TickScheduledTask o2) -> Long.compare(o1.getScheduledTickTime(), o2.getScheduledTickTime()));

    public void preInit(FMLPreInitializationEvent e) {
    }

    public void init(FMLInitializationEvent e) {
        invtweaksChannel = NetworkRegistry.INSTANCE
                .newChannel(InvTweaksConst.INVTWEAKS_CHANNEL, new ITMessageToMessageCodec(),
                        new ITPacketHandler());


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

    public void addScheduledTask(TickScheduledTask task) {
        scheduledTasks.add(task);
    }

    public void addScheduledTask(long time, final Runnable task) {
        addScheduledTask(new TickScheduledTask(time) {
            @Override
            public void run() {
                task.run();
            }
        });
    }

    public void runScheduledTasks(long currentTick) {
        while(!scheduledTasks.isEmpty() &&
                (scheduledTasks.peek().getScheduledTickTime() <= currentTick)) {
            scheduledTasks.poll().run();
        }
    }

    public long getCurrentTick() {
        MinecraftServer server = MinecraftServer.getServer();
        if(server != null) {
            return server.getTickCounter();
        }
        return 0;
    }
}
