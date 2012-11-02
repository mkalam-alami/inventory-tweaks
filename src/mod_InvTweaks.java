import invtweaks.InvTweaksConst;

import java.util.logging.Logger;

import org.lwjgl.input.Mouse;

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
    
    private Thread tickThread = null;
    
    private InvTweaksRunnable tickRunnable;
    
    private boolean mouseWasDown = false;
    
    private class InvTweaksRunnable implements Runnable {

        private InvTweaks instance;
        
        private InvTweaksObfuscation obf;

        public InvTweaksRunnable(Minecraft mc) {
            this.obf = new InvTweaksObfuscation(mc);
            this.instance = new InvTweaks(mc);
        }
        
        @Override
        public void run() {
            if (obf.getCurrentScreen() != null) {
                instance.onTickInGUI(obf.getCurrentScreen());
            }
            else {
                instance.onTickInGame();
            }
        }
        
        public InvTweaks getInstance() {
            return instance;
        }
        
    }
    
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
		
		// Register onTick hook
		ModLoader.setInGameHook(this, true, true);

		// Instantiate mod
		tickRunnable = new InvTweaksRunnable(mc);
	}
    
	/**
	 * Called by ModLoader for each tick during the game.
	 */
	@SuppressWarnings("deprecation")
    public boolean onTickInGame(float clock, Minecraft minecraft) {
	    // Launch mod loop asynchronously to support slow sorting
	    if (tickThread != null && !tickThread.isAlive()) {
	        tickThread = null;
	    }
	    if (tickThread == null) {
	        tickThread = new Thread(tickRunnable);
	        tickThread.start();
	        mouseWasDown = true;
	    }
	    else {
	        if (!mouseWasDown && (Mouse.isButtonDown(0) || Mouse.isButtonDown(1))) {
	            tickThread.stop();
	            tickThread = null;
	            System.out.println("int!");
	        }
	    }
        mouseWasDown = Mouse.isButtonDown(0) || Mouse.isButtonDown(1);
        return true;
	}

    /**
	 * Called by ModLoader when an item has been picked up.
	 */
	public void onItemPickup(qg player, tv itemStack) {
	    tickRunnable.getInstance().setItemPickupPending(true);
	}

}
