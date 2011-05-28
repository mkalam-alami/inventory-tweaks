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
    	px sortKey = new px("Sort inventory", Keyboard.KEY_R);

    	//// A ModLoader 1.6.4 bug forces to register manually
    	//// the key to the options menu. This should work even after
    	//// ModLoader is fixed.

    	// Add sort key to the settings if necessary
    	px[] oldKeys = mc.z.w;
    	px[] newKeys = new px[oldKeys.length+1];
    	boolean keyRegistered = false;
    	for (int i = 0; i < oldKeys.length; i++) {
    		if (oldKeys[i].a.equals(sortKey.a)) {
    			keyRegistered = true; 
    			break;
    		}
    		newKeys[i] = oldKeys[i];
    	}
    	if (!keyRegistered) {
	    	newKeys[newKeys.length-1] = sortKey;
	    	mc.z.w = newKeys;
    	}

    	// Reload options (will now load the sorting key config)
    	mc.z.a();

    	// Register KeyboardEvent, either with default or saved key
    	for (px key : mc.z.w) {
    		if (key.a.equals(sortKey.a)) {
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
    public final void KeyboardEvent(px keybinding)
    {
    	instance.onSortButtonPressed();
    }
    
    public void OnTickInGame(Minecraft minecraft)
    {
    	instance.onTick();
    }
    
}
