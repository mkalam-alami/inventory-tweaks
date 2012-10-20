import invtweaks.InvTweaksConst;

import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

/**
 * ModLoader entry point to load and configure the mod.
 * 
 * @author Jimeo Wan
 * 
 * Contact: jimeo.wan (at) gmail (dot) com
 * Website: {@link http://wan.ka.free.fr/?invtweaks}
 * Source code: {@link https://github.com/mkalam-alami/inventory-tweaks}
 * License: MIT
 * 
 */
public class mod_InvTweaks extends BaseMod {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("InvTweaks");

	private InvTweaks instance;
	
    private InvTweaksObfuscation obf;
    
    @Override
    public String getName() {
        return "Inventory Tweaks";
    }

    @Override
    public String getVersion() {
        return InvTweaksConst.MOD_VERSION;
    }
    
	@Override
	public void load() {
		Minecraft mc = ModLoader.getMinecraftInstance();
		obf = new InvTweaksObfuscation(mc);
		
		// Register key (listen only for key down events)
		ModLoader.registerKey(this, InvTweaks.SORT_KEY_BINDING, false);
		
		// Register onTick hook
		ModLoader.setInGameHook(this, true, true);

		// Instantiate mod core
		instance = new InvTweaks(mc);
	}

    @Override
    public void keyboardEvent(anf keybinding) {
        instance.onSortingKeyPressed();
    }
    
	/**
	 * Called by ModLoader for each tick during the game.
	 */
	public boolean onTickInGame(float clock, Minecraft minecraft) {
		if (obf.getCurrentScreen() != null) {
            instance.onTickInGUI(obf.getCurrentScreen());
		}
		else {
	        instance.onTickInGame();
		}
		return true;
	}

    /**
	 * Called by ModLoader when an item has been picked up.
	 */
	public void onItemPickup(og player, rj itemStack) {
		instance.setItemPickupPending(true);
	}

}
