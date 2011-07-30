package net.minecraft.src;

import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

/**
 * Entry point for Inventory Tweaks.
 * This client mod for Minecraft allows you to easily manage your inventory.
 * 
 * @author Jimeo Wan (jimeo.wan at gmail.com)
 *  Website: {@link http://wan.ka.free.fr/?invtweaks}
 *  Source code: {@link https://github.com/jimeowan/inventory-tweaks}
 * 
 */
public class mod_InvTweaks extends BaseMod {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("InvTweaks");

    private InvTweaks instance;

    public mod_InvTweaks() {

        Minecraft mc = ModLoader.getMinecraftInstance();

        // Register key (listen only for key down events)
        ModLoader.RegisterKey(this, InvTweaks.getSortKeyBinding(), false);

        // Register in game hooks
        ModLoader.SetInGameHook(this, true, true);
        ModLoader.SetInGUIHook(this, true, false);

        // Instantiate mod core
        instance = new InvTweaks(mc);

    }

    @Override
    public String Version() {
        return "1.22 (1.7.3)";
    }

    /**
     * Called by ModLoader each time the sorting key is pressed.
     */
    public final void KeyboardEvent(KeyBinding keyBinding) {
        instance.onSortingKeyPressed();
    }

    /**
     * Called by ModLoader for each tick during the game (except when a menu is
     * open).
     */
    public boolean OnTickInGame(Minecraft minecraft) {
        instance.onTickInGame();
        return true;
    }

    /**
     * Called by ModLoader for each tick while the player is in a menu.
     */
    public boolean OnTickInGUI(Minecraft minecraft, GuiScreen guiScreen) {
        instance.onTickInGUI(guiScreen);
        return true;
    }

    /**
     * Called by ModLoader when an item has been picked up.
     */
    public void OnItemPickup(EntityPlayer entityplayer, ItemStack stack) {
        // It's useless to give stack as a parameter since the
        // given object doesn't contain enough information to know
        // which stack in the inventory has been updated. We have to
        // do it ourselves.
        instance.onItemPickup();
    }

}
