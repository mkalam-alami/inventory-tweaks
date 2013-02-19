package invtweaks.forge;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.IPickupNotifier;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import invtweaks.InvTweaks;
import invtweaks.InvTweaksConst;
import invtweaks.forge.ForgeClientTick;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;

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
@Mod(modid = "inventorytweaks_forge", version = InvTweaksConst.MOD_VERSION,
        certificateFingerprint = "eac60f974c9439dc644ab1c03c62255b1fg30a78",
        dependencies = "required-after:FML@[4.6.0,);required-after:Forge@[6.5.0,)")
public class InvTweaksMod implements IPickupNotifier {
    private InvTweaks instance;
    private ForgeClientTick clientTick;

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

    /**
     * Called by ModLoader when an item has been picked up.
     */
    @Override
    public void notifyPickup(EntityItem item, EntityPlayer player) {
        instance.setItemPickupPending(true);
    }
}
