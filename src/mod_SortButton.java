package net.minecraft.src;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;


/**
 * Allows to sort your inventory with a one-key shortcut.
 * @author Jimeo Wan (marwane.ka at gmail.com)
 *
 */
public class mod_SortButton extends BaseMod {
	
    private static final Logger log = Logger.getLogger(mod_SortButton.class.getName());

    private static final KeyBinding myKey = new KeyBinding("Sort inventory", Keyboard.KEY_S);
    private static final String CONFIG_FILE = Minecraft.getMinecraftDir()+"/ModSortButton.txt";
    private static final int INV_SIZE = 36; 
    
    private SortButtonConfig config = null;
    private SortButtonCategories categories;
    
    public mod_SortButton() {

    	log.setLevel(Level.FINE);

    	// Register customizable custom key
    	ModLoader.RegisterKey(this, myKey, false);
    	
    	// Load config
    	try {
    		config = new SortButtonConfig(CONFIG_FILE);
		} catch (FileNotFoundException e) {
	    	log.severe("Config file "+CONFIG_FILE+" not found");
	    	return;
		} catch (IOException e) {
	    	log.severe("Could not read config file "+CONFIG_FILE+" : "+e.getMessage());
	    	return;
		}
    	
		// Load categories
		categories = new SortButtonCategories();
		
    	log.info("Mod initialized");
    	
    }
    
	@Override
	public String Version() {
		return "1.0";
	}
    
	/**
	 * Sort inventory
	 */
    public final void KeyboardEvent(KeyBinding keybinding)
    {
    	// Do nothing if config loading failed
    	if (config == null) {
    		return;
    	}
    	
    	Minecraft mc = ModLoader.getMinecraftInstance();
		
    	// Do nothing if the inventory is closed
    	// if (!mc.currentScreen instanceof GuiContainer)
    	//		return;
    		
		// Note: 0 = bottom-left, 36 = top-right
		ItemStack[] oldInv = mc.thePlayer.inventory.mainInventory;    		
		ItemStack[] newInv = new ItemStack[INV_SIZE];	
		SortButtonEntry entry;
		Vector<ItemStack> remainingStacks = new Vector<ItemStack>();
		Vector<ItemStack> newlyOrderedStacks = new Vector<ItemStack>();
		
		for (int i = 0; i < oldInv.length; i++) {
			remainingStacks.add(oldInv[i]);
		}
		
		// TODO: Comment, iterate
		while ((entry = config.nextEntry()) != null) {
			for (int i = 0; i < remainingStacks.size(); i++) {
				ItemStack stack = remainingStacks.get(i);
				if (stack != null && categories.matches(stack.itemID, entry.getKeyword())) {
					int[] preferredPos = entry.getPreferedPositions();
					for (int j = 0; j < preferredPos.length; j++) {
						if (newInv[j] == null) {
							newInv[j] = stack;
							newlyOrderedStacks.add(stack);
							break;
						}
					}
				}
			}
			for (int i = 0; i < newlyOrderedStacks.size(); i++) {
				remainingStacks.remove(newlyOrderedStacks.get(i));
			}
			newlyOrderedStacks.clear();
		}
		
		// Stuff without a found spot
		int index = INV_SIZE-1;
		for (int i = 0; i < remainingStacks.size(); i++) {
			while (index >= 0 && newInv[index] != null) {
				index--;
			}
			if (index >= 0) {
				newInv[index--] = remainingStacks.get(i);
			}
			else {
				log.severe("Some items could not be placed. The algorithm seems broken.");
			}
		}
		
		// Done!
		mc.thePlayer.inventory.mainInventory = newInv;
    		
    }
    
}
