package invtweaks.forge;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.IPickupNotifier;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import invtweaks.InvTweaks;
import invtweaks.InvTweaksConfig;
import invtweaks.InvTweaksItemTreeLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;

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
        InvTweaks.log.info("Server has support: " + hasInvTweaks);
        serverSupportEnabled = hasInvTweaks;
    }
}
