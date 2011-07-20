package net.minecraft.src;

import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;

/**
 * @author Jimeo Wan (jimeo.wan at gmail.com)
 * Website: {@link http://wan.ka.free.fr/?invtweaks}
 * Source code: {@link https://github.com/jimeowan/inventory-tweaks}
 * 
 */
public class mod_InvTweaks extends BaseMod {

    @SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("mod_InvTweaks");
    
	private InvTweaks instance;
    
    public mod_InvTweaks() {
    	
    	// Register key
    	Minecraft mc = ModLoader.getMinecraftInstance();
    	KeyBinding sortKey = new KeyBinding(
    			"Sort inventory", Keyboard.KEY_R); /* KeyBinding */
    	ModLoader.RegisterKey(this, sortKey, false);
    	
    	// Register OnTickInGame event
    	ModLoader.SetInGameHook(this, true, true);

    	// Instantiate mod core
    	instance = new InvTweaks(mc);
    }
    
	@Override
	public String Version() {
		return "1.11 (1.7.3)";
	}
    
	/**
	 * Sort inventory
	 */
    public final void KeyboardEvent(KeyBinding keyBinding)
    {
    	instance.onSortingKeyPressed();
    }
    
    public boolean OnTickInGame(Minecraft minecraft)
    {
    	instance.onTick();
    	return true; 
    }
    
}
