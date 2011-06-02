package net.minecraft.src;

import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;

/**
 * @author Jimeo Wan (jimeo.wan at gmail.com)
 * @version 1.0 (1.5_01)
 * Website: {@link http://wan.ka.free.fr/?invtweaks}
 * Source code: {@link https://github.com/jimeowan/minecraft-mod-inventory-tweaks}
 * 
 */
public class mod_InvTweaks extends BaseMod {

    @SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("mod_InvTweaks");
    
	private InvTweaks instance;
    
    public mod_InvTweaks() {
    	
    	Minecraft mc = ModLoader.getMinecraftInstance();
    	KeyBinding sortKey = new KeyBinding("Sort inventory", Keyboard.KEY_R);

    	//// A ModLoader 1.6.4 bug forces to register manually
    	//// the key to the options menu. This should work even after
    	//// ModLoader is fixed.

    	// Add sort key to the settings if necessary
    	KeyBinding[] oldKeys = mc.gameSettings.keyBindings;
    	KeyBinding[] newKeys = new KeyBinding[oldKeys.length+1];
    	boolean keyRegistered = false;
    	for (int i = 0; i < oldKeys.length; i++) {
    		if (oldKeys[i].keyDescription.equals(sortKey.keyDescription)) {
    			keyRegistered = true; 
    			break;
    		}
    		newKeys[i] = oldKeys[i];
    	}
    	if (!keyRegistered) {
	    	newKeys[newKeys.length-1] = sortKey;
	    	mc.gameSettings.keyBindings = newKeys;
    	}

    	// Reload options (will now load the sorting key config)
    	mc.gameSettings.loadOptions();

    	// Register KeyboardEvent, either with default or saved key
    	for (KeyBinding key : mc.gameSettings.keyBindings) {
    		if (key.keyDescription.equals(sortKey.keyDescription)) {
    			sortKey = key;
    		}
    	}
    	ModLoader.RegisterKey(this, sortKey, true);
    	
  
    	//// Register OnTickInGame event
    	
    	ModLoader.SetInGameHook(this, true, true);

    	
    	//// Instantiate mod core
    	
    	instance = new InvTweaks(mc);
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
    	instance.sortInventory();
    }
    
    public void OnTickInGame(Minecraft minecraft)
    {
    	instance.onTick();
    }
    
}
