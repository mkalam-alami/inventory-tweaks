package net.minecraft.src;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;


/**
 * Allows to sort one's inventory with a single key.
 * @author Jimeo Wan (marwane.ka at gmail.com)
 *
 */
public class mod_SortButton extends BaseMod {
	
	// (Note) In the inventory, indexes are :
	// 0 = bottom-left, 9 = bottom-right
	// 10 = top-left, 35 = 3rd-row-right
	
    private static final Logger log = Logger.getLogger(mod_SortButton.class.getName());

    private static final KeyBinding myKey = new KeyBinding("Sort inventory", Keyboard.KEY_S);
    private static final String CONFIG_FILE = Minecraft.getMinecraftDir()+"/ModSortButton.txt";
    private static final String INGAME_LOG_PREFIX = "SortButton: ";
    private static final Level LOG_LEVEL = Level.FINE;
    private static final int INV_SIZE = 36;

    private SortButtonConfig config = null;
    private SortButtonCategories categories;
    
    private long lastKeyPress = 0;
    private int keyPressDuration = 0;
    
    public mod_SortButton() {

    	log.setLevel(LOG_LEVEL);

    	// Register customizable custom key
    	ModLoader.RegisterKey(this, myKey, true);
    	
    	// Load config
		config = new SortButtonConfig(CONFIG_FILE);
		tryLoading(config, true);
    	
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
    	
    	// Hot reload trigger
    	long currentTime = System.currentTimeMillis();
    	if (currentTime - lastKeyPress < 100) {
    		keyPressDuration += currentTime - lastKeyPress;
        	lastKeyPress = currentTime;
    		if (keyPressDuration > 500 && keyPressDuration < 1000) {
    			tryLoading(config, false);
    			keyPressDuration = 1000; // Prevent from load repetition
    		}
    		else {
    			return;
    		}
    	}
    	else {
        	lastKeyPress = currentTime;
    		keyPressDuration = 0;
    	}
    	
    	
    	Minecraft mc = ModLoader.getMinecraftInstance();
		
    	// Do nothing if the inventory is closed
    	// if (!mc.currentScreen instanceof GuiContainer)
    	//		return;
    		
		ItemStack[] oldInv = mc.thePlayer.inventory.mainInventory;    		
		ItemStack[] newInv = new ItemStack[INV_SIZE];
		Vector<ItemStack> remainingStacks = new Vector<ItemStack>();
		Vector<ItemStack> newlyOrderedStacks = new Vector<ItemStack>();
		
		for (int i = 0; i < oldInv.length; i++) {
			remainingStacks.add(oldInv[i]);
		}
		
		// TODO: Comment
		Vector<SortButtonRule> rules = config.getRules();
		Iterator<SortButtonRule> rulesIt = rules.iterator();
		SortButtonRule rule;
		Iterator<ItemStack> stackIt;
		ItemStack stack;
		while (rulesIt.hasNext()) {
			rule = rulesIt.next();
			stackIt = remainingStacks.iterator();
			while (stackIt.hasNext()) {
				stack = stackIt.next();
				if (stack != null && categories.matches(stack.itemID, rule.getKeyword())) {
					int[] preferredPos = rule.getPreferedPositions();
					for (int j = 0; j < preferredPos.length; j++) {
						if (newInv[preferredPos[j]] == null) {
							newInv[preferredPos[j]] = stack;
							newlyOrderedStacks.add(stack);
							break;
						}
					}
				}
			}
			stackIt = newlyOrderedStacks.iterator();
			while (stackIt.hasNext()) {
				remainingStacks.remove(stackIt.next());
			}
			newlyOrderedStacks.clear();
		}
		
		// Stuff without a found spot
		int index = INV_SIZE-1;
		stackIt = remainingStacks.iterator();
		while (stackIt.hasNext()) {
			while (index >= 0 && newInv[index] != null) {
				index--;
			}
			if (index >= 0) {
				newInv[index--] = stackIt.next();
			}
			else {
				log.severe("Aborting sort: some items could not be placed. The algorithm seems broken!");
				return;
			}
		}
		
		// Done!
		mc.thePlayer.inventory.mainInventory = newInv;
    		
    }
    
    /**
     * Tries to load mod configuration from file, with error handling.
     * @param config
     */
    private static boolean tryLoading(SortButtonConfig config, boolean silently) {
		try {
			config.load();
			if (!silently) {
				ModLoader.getMinecraftInstance().ingameGUI.
						addChatMessage(INGAME_LOG_PREFIX + "Configuration reloaded");
			}
	    	return true;
		} catch (FileNotFoundException e) {
			if (!silently) {
				String error = "Config file "+CONFIG_FILE+" not found";
				ModLoader.getMinecraftInstance().ingameGUI.
						addChatMessage(INGAME_LOG_PREFIX + error);
				log.severe(error);
			}
	    	return false;
		} catch (IOException e) {
			if (!silently) {
				String error = "Could not read config file "+CONFIG_FILE;
				ModLoader.getMinecraftInstance().ingameGUI.
						addChatMessage(INGAME_LOG_PREFIX + error);
				log.severe(error + " : " + e.getMessage());
			}
	    	return false;
		}
    }
    
}
