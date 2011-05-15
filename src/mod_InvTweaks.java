package net.minecraft.src;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;

/**
 * Allows to sort one's inventory with a single keystroke.
 * @author Jimeo Wan (jimeo.wan at gmail.com)
 * @version 1.0-for-1.5_01
 * Website: http://wan.ka.free.fr/?invtweaks
 * Source code: https://github.com/jimeowan/minecraft-mod-inventory-tuning
 * 
 */
public class mod_InvTweaks extends BaseMod {
	
	private InvTweaks instance;
	
	private static final KeyBinding myKey = new KeyBinding("Sort inventory", Keyboard.KEY_E);
    
    public mod_InvTweaks() {
    	
    	// Register customizable sort key
    	ModLoader.RegisterKey(this, myKey, true);
    	
    	// Register OnTickInGame event
    	ModLoader.SetInGameHook(this, true, true);
    	
    	instance = new InvTweaks();
    }
    
	@Override
	public String Version() {
		return "1.0 (1.5_01)";
	}
    
	/**
	 * Sort inventory
	 */
    public final void KeyboardEvent(KeyBinding keybinding)
    {
    	instance.onSortButtonPressed();
    }
    

    public void OnTickInGame(Minecraft minecraft)
    {
    	instance.onTick();
    }

}
