package invtweaks.forge;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.IPickupNotifier;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import invtweaks.InvTweaks;
import invtweaks.InvTweaksConfig;
import invtweaks.InvTweaksConst;
import invtweaks.InvTweaksItemTreeLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;

import java.util.logging.Logger;

/**
 * ModLoader entry point to load and configure the mod.
 *
 * @author Jimeo Wan
 *         <p/>
 *         Contact: jimeo.wan (at) gmail (dot) com
 *         Website: {@link http://wan.ka.free.fr/?invtweaks}
 *         Source code: {@link https://github.com/mkalam-alami/inventory-tweaks}
 *         License: MIT
 */
@Mod(modid = "inventorytweaks_forge",
        dependencies = "required-after:FML@[4.6.0,);required-after:Forge@[7.7.0,)")
public class InvTweaksMod implements IPickupNotifier {
    private InvTweaks instance;
    private ForgeClientTick clientTick;

    @Mod.PreInit
    public void preInit(FMLPreInitializationEvent e) {
        InvTweaks.log = e.getModLog();
    }

    @Mod.Init
    public void init(FMLInitializationEvent e) {
        if(e.getSide() == Side.CLIENT) {
            Minecraft mc = FMLClientHandler.instance().getClient();
            // Instantiate mod core
            instance = new InvTweaks(mc);
            clientTick = new ForgeClientTick(instance);
            TickRegistry.registerTickHandler(clientTick, Side.CLIENT);
            GameRegistry.registerPickupHandler(this);
        }
    }

    @Mod.PostInit
    public void postInit(FMLPostInitializationEvent e) {
        if(!InvTweaks.getConfigManager().getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_FORGE_ITEMTREE).equals(InvTweaksConfig.VALUE_FALSE)) {
            InvTweaksItemTreeLoader.addOnLoadListener(new ForgeItemTreeListener());
        }
    }

    /**
     * Called by ModLoader when an item has been picked up.
     */
    @Override
    public void notifyPickup(EntityItem item, EntityPlayer player) {
        instance.setItemPickupPending(true);
    }
}
