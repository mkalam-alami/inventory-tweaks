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

	@Override
	public void load() {
		
		Minecraft mc = ModLoader.getMinecraftInstance();

		// Register key (listen only for key down events)
		ModLoader.RegisterKey(this, InvTweaks.SORT_KEY_BINDING, false);

		// Register in game hooks
		ModLoader.SetInGameHook(this, true, true);
		ModLoader.SetInGUIHook(this, true, false);

		// Instantiate mod core
		instance = new InvTweaks(mc);

	}

	@Override
	public String getVersion() {
		return InvTweaksConst.MOD_VERSION;
	}

	/**
	 * Called by ModLoader each time the sorting key is pressed.
	 */
	public void KeyboardEvent(ads keyBinding) {
		instance.onSortingKeyPressed();
	}

	/**
	 * Called by ModLoader for each tick during the game (except when a menu is
	 * open).
	 */
	public boolean OnTickInGame(float clock, Minecraft minecraft) {
		instance.onTickInGame();
		return true;
	}

	/**
	 * Called by ModLoader for each tick while the player is in a menu.
	 */
	public boolean OnTickInGUI(float clock, Minecraft minecraft, ug guiScreen) {
		if (guiScreen != null) {
			instance.onTickInGUI(guiScreen);
		}
		return true;
	}

	/**
	 * Called by ModLoader when an item has been picked up.
	 */
	public void OnItemPickup(xb entityplayer, yq stack) {
	    // ModLoader doesn't give the item amount in 'stack', so it's no use for us
		instance.onItemPickup();
	}

}
