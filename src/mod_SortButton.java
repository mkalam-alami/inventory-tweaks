package net.minecraft.src;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;


/**
 * Allows to sort one's inventory with a single key.
 * @author Jimeo Wan (jimeo.wan at gmail.com)
 * @version 1.0-for-1.5_01
 * Website: http://wan.ka.free.fr/?sortbutton
 * Source code: https://github.com/jimeowan/minecraft-mod-sortbutton
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
		Map<Integer, ItemStack> newlyOrderedStacks = new HashMap<Integer, ItemStack>();
		Vector<SortButtonRule> rules = config.getRules();
		Iterator<SortButtonRule> rulesIt = rules.iterator();
		SortButtonRule rule;
		Iterator<ItemStack> stackIt;
		ItemStack stack, wantedSlotStack;
		String itemName;
		
		for (int i = 0; i < oldInv.length; i++) {
			if (oldInv[i] != null) {
				remainingStacks.add(oldInv[i]);
			}
		}
		
		// Sort rule by rule, themselves being already sorted by decreasing priority
		while (rulesIt.hasNext()) {
			
			rule = rulesIt.next();
			stackIt = remainingStacks.iterator();
			
			// Look for item stacks that match the rule
			while (stackIt.hasNext()) {
				
				stack = stackIt.next();
				itemName = SortButtonTree.getItemName(stack.itemID);
				if (stack != null && SortButtonTree.matches(itemName, rule.getKeyword())) {
					
					// Try to put the matching item stack to a preferred position,
					// theses positions being sorted by decreasing preference.
					int[] preferredPos = rule.getPreferredPositions();
					boolean checkedFilledPos = false;
					for (int j = 0; j < preferredPos.length; j++) {
						
						wantedSlotStack = newInv[preferredPos[j]];
						
						// If the slot is free, no problem
						if (wantedSlotStack == null) {
							log.info(rule.getKeyword()+" keyword put "+SortButtonTree.getItemName(stack.itemID)+" in "+preferredPos[j]);
							newInv[preferredPos[j]] = stack; // Put the stack in the new inventory!
							newlyOrderedStacks.put(preferredPos[j], stack);
							break;
						}
						
						// If the slot is occupied, check (once) if the item
						// can replace one of the already put items. This
						// can be done if both constraints are respected:
						// * The item to replace has been put using the same rule
						// * The item to replace has a lower item priority
						else if (!checkedFilledPos) {
							
							Integer stackToReplaceKey = null;
							for (Integer stackKey : newlyOrderedStacks.keySet()) {
								if (SortButtonTree.getKeywordPriority(
										SortButtonTree.getItemName(newlyOrderedStacks.get(stackKey).itemID))
									< SortButtonTree.getKeywordPriority(itemName)) {
									stackToReplaceKey = stackKey;
									break;
								}
							}
							
							// If an item can be replaced, the items are swapped.
							// (we are now trying to find a slot for the replaced item)
							if (stackToReplaceKey != null) {
								newlyOrderedStacks.put(stackToReplaceKey, stack);
								newInv[preferredPos[j]] = stack;
								stack = wantedSlotStack;
							}
							else {
								checkedFilledPos = true;
							}
						}
					}
				}
			}
			
			// Remove stacks placed during this rule from items to sort
			remainingStacks.removeAll(newlyOrderedStacks.values());
			newlyOrderedStacks.clear();
		}
		
		// Put stuff without a found spot in any free spot,
		// starting from top-left.
		// TODO: Handle case where only < #9 spots are free
		int index = 9;
		stackIt = remainingStacks.iterator();
		while (stackIt.hasNext()) {
			while (index < INV_SIZE && newInv[index] != null) {
				index++;
			}
			if (index < INV_SIZE) {
				stack = stackIt.next();
				newInv[index++] = stack;
				log.info("Remaining stuff rule put "+SortButtonTree.getItemName(stack.itemID)+" in "+index);
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
