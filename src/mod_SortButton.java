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
	
    private static final Logger log = Logger.getLogger("ModSortButton");

    private static final KeyBinding myKey = new KeyBinding("Sort inventory", Keyboard.KEY_S);
    private static final int HOT_RELOAD_DELAY = 1000;
    
    public static final String CONFIG_FILE = Minecraft.getMinecraftDir()+"/ModSortButtonConfig.txt";
    public static final String CONFIG_TREE_FILE = Minecraft.getMinecraftDir()+"/ModSortButtonTree.txt";
    public static final String INGAME_LOG_PREFIX = "SortButton: ";
    public static final Level LOG_LEVEL = Level.FINE;
    public static final int INV_SIZE = 36;

    private SortButtonConfig config = null;
    
    private long lastKeyPress = 0;
    private int keyPressDuration = 0;
    private boolean configErrorsShown = false;
    
    public mod_SortButton() {

    	log.setLevel(LOG_LEVEL);
    	
    	// Register customizable custom key
    	ModLoader.RegisterKey(this, myKey, true);
    	
    	// Load config files
		tryLoading(true);
		
    	log.info("Mod initialized");
    	
    }
    
	@Override
	public String Version() {
		return "1.0-1.5_01";
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
    		if (keyPressDuration > HOT_RELOAD_DELAY && keyPressDuration < 2*HOT_RELOAD_DELAY) {
    			tryLoading(false);
    			keyPressDuration = 2*HOT_RELOAD_DELAY; // Prevent from load repetition
    		}
    		else {
    			return;
    		}
    	}
    	else {
        	lastKeyPress = currentTime;
    		keyPressDuration = 0;
    	}
    	
    	// Config keywords error message
    	if (!configErrorsShown) {
    		showConfigErrors(config);
			configErrorsShown = true;
    	}
    	
    	Minecraft mc = ModLoader.getMinecraftInstance();
		
    	// Do nothing if the inventory is closed
    	// if (!mc.currentScreen instanceof GuiContainer)
    	//		return;
    	
    	// Sorting initialization
		ItemStack[] oldInv = mc.thePlayer.inventory.mainInventory;    		
		ItemStack[] newInv = new ItemStack[INV_SIZE];
		Vector<ItemStack> remainingStacks = new Vector<ItemStack>();
		Vector<ItemStack> newlyOrderedStacks = new Vector<ItemStack>();
		
		for (int i = 0; i < oldInv.length; i++) {
			if (oldInv[i] != null) {
				remainingStacks.add(oldInv[i]);
			}
		}
		
		// Sorting
		Vector<SortButtonRule> rules = config.getRules();
		Iterator<SortButtonRule> rulesIt = rules.iterator();
		SortButtonRule rule;
		Iterator<ItemStack> stackIt;
		ItemStack stack;
		while (rulesIt.hasNext()) {
			rule = rulesIt.next();
			stackIt = remainingStacks.iterator();
			log.info(rule.getKeyword()+" rule has priority "+SortButtonKeywords.getKeywordPriority(rule.getKeyword()));
			while (stackIt.hasNext()) {
				stack = stackIt.next();
				if (stack != null && SortButtonKeywords.matches(
						SortButtonTree.getItemName(stack.itemID), rule.getKeyword())) {
					int[] preferredPos = rule.getPreferedPositions();
					for (int j = 0; j < preferredPos.length; j++) {
						if (newInv[preferredPos[j]] == null) {
							log.info(rule.getKeyword()+" keyword put "+stack.itemID+" in "+j);
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
		int index = 9;
		stackIt = remainingStacks.iterator();
		while (stackIt.hasNext()) {
			while (index < INV_SIZE && newInv[index] != null) {
				index++;
			}
			if (index < INV_SIZE) {
				stack = stackIt.next();
				log.info("Remaining stuff rule put "+stack.itemID+" in "+index);
				newInv[index++] = stack;
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
    private boolean tryLoading(boolean silently) {
		try {
	    	SortButtonTree.loadTreeFromFile(CONFIG_TREE_FILE);
	    	if (config == null) {
	    		config = new SortButtonConfig(CONFIG_FILE);
	    	}
			config.load();
			if (!silently) {
				ModLoader.getMinecraftInstance().ingameGUI.
						addChatMessage(INGAME_LOG_PREFIX + "Configuration reloaded");
				showConfigErrors(config);
			}
	    	return true;
		} catch (FileNotFoundException e) {
			if (!silently) {
				String error = "Config file not found";
				ModLoader.getMinecraftInstance().ingameGUI.
						addChatMessage(INGAME_LOG_PREFIX + error);
				log.severe(error);
			}
	    	return false;
		} catch (IOException e) {
			if (!silently) {
				String error = "Could not read config file";
				ModLoader.getMinecraftInstance().ingameGUI.
						addChatMessage(INGAME_LOG_PREFIX + error);
				log.severe(error + " : " + e.getMessage());
			}
	    	return false;
		}
    }
    
    private static void showConfigErrors(SortButtonConfig config) {
    	Vector<String> invalid = config.getInvalidKeywords();
    	if (invalid.size() > 0) {
			String error = "Invalid keywords found (";
			for (String keyword : config.getInvalidKeywords()) {
				error += keyword+" ";
			}
			error.replaceFirst(" $", ")");
			ModLoader.getMinecraftInstance().ingameGUI.
					addChatMessage(INGAME_LOG_PREFIX + error);
    	}
    }
    
}
