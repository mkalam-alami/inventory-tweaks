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

public class SortButton {
	
	// (Note) In the inventory, indexes are :
	// 0 = bottom-left, 8 = bottom-right
	// 9 = top-left, 35 = 3rd-row-right
	
    private static final Logger log = Logger.getLogger("ModSortButton");

    private static final int HOT_RELOAD_DELAY = 1000;
    
    public static final String CONFIG_FILE = Minecraft.getMinecraftDir()+"/ModSortButtonConfig.txt";
    public static final String CONFIG_TREE_FILE = Minecraft.getMinecraftDir()+"/ModSortButtonTree.txt";
    public static final String INGAME_LOG_PREFIX = "SortButton: ";
    public static final Level LOG_LEVEL = Level.FINE;
    public static final int INV_SIZE = 36;
	public static int[] ALL_SLOTS = new int[INV_SIZE];

    private SortButtonConfig config = null;
    private long lastKeyPress = 0;
    private int keyPressDuration = 0;
    private boolean configErrorsShown = false;
	private int storedStackId = 0, storedPosition = -1;
    private Minecraft mc;
    
    public SortButton() {

    	log.setLevel(LOG_LEVEL);
    	
    	// Slot order init
    	for (int i = 0; i < ALL_SLOTS.length; i++) {
    		ALL_SLOTS[i] = (i + 9) % INV_SIZE;
    	}

		// Get Minecraft instance
    	mc = ModLoader.getMinecraftInstance();
    	
    	// Load config files
		tryLoading(true);
    	
    	log.info("Mod initialized");
    	
    }
    
	/**
	 * Sort inventory
	 */
    public final void onSortButtonPressed()
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
    	
    	// Do nothing if the inventory is closed
    	// if (!mc.currentScreen instanceof GuiContainer)
    	//		return;

    	// Main initialization
    	// (About oldInv: slots will be set to null as items are moved to
    	// the new inventory)
		ItemStack[] oldInv = mc.thePlayer.inventory.mainInventory.clone();
		int[] lockedSlots = config.getLockedSlots();
		ItemStack stack;
		int i, j, search;
		
		// Merge (non locked) stacks
		Vector<Integer> itemIDs = new Vector<Integer>();
		ItemStack destinationStack;
		
		for (i = 0; i < oldInv.length; i++) {
			
			stack = oldInv[i];
			if (stack != null) {
				
				search = 0;
				while (stack != null &&
						(j = itemIDs.indexOf(stack.itemID, search)) != -1) {
					
					if (lockedSlots[i] <= lockedSlots[j]) {
						destinationStack = oldInv[j];
						mergeStacks(stack, destinationStack);
						if (stack.stackSize == 0) {
							oldInv[i] = stack = null;
						}
						else {
							itemIDs.set(j, -1); // j is full
						}
					}
					search = j + 1;
				}
			}
			
			if (stack != null && lockedSlots[i] == 0) {
				itemIDs.add(
					(stack.stackSize < stack.getMaxStackSize())
					? stack.itemID : -1);
			}
			else {
				itemIDs.add(-1);
			}
		}
		
    	// Sorting initialization		
		ItemStack[] newInv = new ItemStack[INV_SIZE];
		Map<Integer, ItemStack> newlyOrderedStacks = new HashMap<Integer, ItemStack>();
		Vector<SortButtonRule> rules = config.getRules();
		Iterator<SortButtonRule> rulesIt = rules.iterator();
		SortButtonRule rule;
		ItemStack wantedSlotStack;
		String itemName;
		int rulePriority;
		
		// Sort rule by rule, themselves being already sorted by decreasing priority
		while (rulesIt.hasNext()) {
			
			rule = rulesIt.next();
			rulePriority = rule.getPriority();
			//log.info("Rule : "+rule.getKeyword());
			
			// Look for item stacks that match the rule
			for (i = 0; i < oldInv.length; i++) {
				
				stack = oldInv[i];
				if (stack == null || lockedSlots[i] > rulePriority)
					continue;
				
				itemName = SortButtonTree.getItemName(stack.itemID);
				if (stack != null && SortButtonTree.matches(itemName, rule.getKeyword())
						&& lockedSlots[i] < rulePriority) {
					
					// Try to put the matching item stack to a preferred position,
					// theses positions being sorted by decreasing preference.
					int[] preferredPos = rule.getPreferredPositions();
					boolean checkedFilledPos = false;
					
					j = -1;
					while ((j = getNextSlot(preferredPos, j+1, rulePriority)) != -1) {
						
						wantedSlotStack = newInv[preferredPos[j]];
						
						// If the slot is free, no problem.
						// (except for a special case about locked stacks of the same ID)
						if (wantedSlotStack == null
								&& (lockedSlots[i] > lockedSlots[preferredPos[j]]
									|| oldInv[preferredPos[j]] == null
									|| (SortButtonTree.matches(
											SortButtonTree.getItemName(oldInv[preferredPos[j]].itemID),
											rule.getKeyword())))) {
							newInv[preferredPos[j]] = stack; // Put the stack in the new inventory!
							log.info(SortButtonTree.getItemName(stack.itemID)+i+" put in "+preferredPos[j]+", "+i+" OK");
							oldInv[i] = null;
							newlyOrderedStacks.put(preferredPos[j], stack);
							break;
						}
						
						else if (wantedSlotStack != null) {
							
							if (newInv[preferredPos[j]].itemID == stack.itemID
									&& lockedSlots[i] == lockedSlots[preferredPos[j]]) {
								mergeStacks(stack, newInv[preferredPos[j]]);
								if (stack.stackSize == 0) {
									oldInv[i] = stack = null;
								}
							}
							
							// If the slot is occupied, check (once) if the item
							// can replace one of the already put items. This
							// can be done if both constraints are respected:
							// * The item to replace has been put using the same rule
							// * The item to replace has a lower item priority
							if (stack != null && !checkedFilledPos) {
								
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
									oldInv[i] = wantedSlotStack;
									//log.info(SortButtonTree.getItemName(stack.itemID)+" replaces "+SortButtonTree.getItemName(wantedSlotStack.itemID));
									stack = wantedSlotStack;
								}
								else {
									checkedFilledPos = true;
								}
							}
						}
					}
				}
			}

			newlyOrderedStacks.clear();
		}
		
		// Locked stacks don't move
		for (i = 0; i < oldInv.length; i++) {
			if (oldInv[i] != null && lockedSlots[i] > 0) {
				if (newInv[i] == null) {
					log.info(SortButtonTree.getItemName(oldInv[i].itemID)+" doesn't move");
					newInv[i] = oldInv[i];
					oldInv[i] = null;
				}
				else if (newInv[i].itemID == oldInv[i].itemID) {
					mergeStacks(oldInv[i], newInv[i]);
					if (oldInv[i].stackSize == 0) {
						oldInv[i] = null;
					}
				}
			}
		}

		// Put stuff without a found spot in any free spot,
		// starting from top-left.
		// In two steps: first by skipping locked spots,
		// then whatever spot it is, to avoid item loss
		
		int[] levels = new int[]{1, Integer.MAX_VALUE};
		int stackPriority, index;
		boolean emptySlotFound;
		
		for (j = 0; j < levels.length; j++) {
			
			for (i = 0; i < oldInv.length; i++) {

				stack = oldInv[i];
				if (stack == null || lockedSlots[i] > levels[j])
					continue;
				stackPriority = SortButtonTree.getItemPriority(stack.itemID);
				index = -1;
				
				// Look for an empty spot
				emptySlotFound = false;
				while (!emptySlotFound) {
					index = getNextSlot(index+1, levels[j]);
					if (newInv[ALL_SLOTS[index]] != null) {
						
						// Try to merge if same item
						if (newInv[ALL_SLOTS[index]].itemID == stack.itemID &&
								lockedSlots[i] == lockedSlots[ALL_SLOTS[index]]) {
							mergeStacks(stack, newInv[ALL_SLOTS[index]]);
							if (stack.stackSize == 0) {
								log.info("Merged (" +lockedSlots[i]+"="+lockedSlots[ALL_SLOTS[index]]+") : "+SortButtonTree.getItemName(stack.itemID)+i+" to "+ALL_SLOTS[index]);
								oldInv[i] = null;
								stack = null;
								break;
							}
						}
						
						wantedSlotStack = newInv[ALL_SLOTS[index]];
						
						// Swap items, then restart search
						if (stackPriority > SortButtonTree.
								getItemPriority(wantedSlotStack.itemID) &&
								lockedSlots[i] == lockedSlots[ALL_SLOTS[index]]) {
							log.info("Swapping : "+SortButtonTree.getItemName(stack.itemID)+i+" goes to "+ALL_SLOTS[index]);
							newInv[ALL_SLOTS[index]] = stack;
							oldInv[i] = wantedSlotStack;
							
							// TODO: Refactoring
							stack = wantedSlotStack;
							stackPriority = SortButtonTree.getItemPriority(stack.itemID);
							index = -1;
						}
					}
					else {
						emptySlotFound = true;
					}
				}
				
				// Empty spot found
				if (stack != null) {
					if (index != -1) {
						newInv[ALL_SLOTS[index]] = stack;
						oldInv[i] = null;
						log.info("Remaining : "+ALL_SLOTS[index]+" for "+SortButtonTree.getItemName(stack.itemID));
					}
					else if (j == levels.length) {
						log.severe("Aborting sort: some items could not be placed. The algorithm seems broken!");
						return;
					}
				}
			}
		}
		
		// Done!
		mc.thePlayer.inventory.mainInventory = newInv;
    		
    }
    
    public void onTick() {

		// Auto-replace item stack
    	
    	ItemStack currentStack = mc.thePlayer.getCurrentEquippedItem();
    	int currentStackId = (currentStack == null) ? 0 : currentStack.itemID;
    	
    	if (currentStackId != storedStackId) {
    		InventoryPlayer inventory = mc.thePlayer.inventory;
	    	if (storedPosition != inventory.currentItem) { // Filter selection change
	    		storedPosition = inventory.currentItem;
	    	} else if (currentStackId == 0 &&
	    			inventory.getItemStack() == null) { // Filter item pickup
	    		ItemStack candidateStack;
	    		for (int i = 0; i < INV_SIZE; i++) {
	    			// Look only for an exactly matching ID
	    			candidateStack = inventory.mainInventory[i];
	    			if (candidateStack != null && candidateStack.itemID == storedStackId) {
	    				inventory.mainInventory[i] = null;
	    				inventory.mainInventory[inventory.currentItem] = candidateStack;
	    				break;
	    			}
	    		}
	    	}
	    	storedStackId = currentStackId;
    	}
    	
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

    /**
     * Returns next slot index, or -1.
     * Uses the whole inventory, from top-left to down-right.
     * Given and return integers are ALL_SLOTS indexes.
     */
    private int getNextSlot(int index, int rulePriority) {
    	return getNextSlot(ALL_SLOTS, index, rulePriority); // TODO: Not locked first?
    }
    
    /**
     * Returns next slot index, or -1.
     */
    private int getNextSlot(int[] preffered, int index,
    		int rulePriority) {
    	int[] locked = config.getLockedSlots();
    	while (preffered.length > index) {
    		if (locked[preffered[index]] <= rulePriority)
    			return index;
    		index++;
    	}
    	return -1;
    }
    
    private void mergeStacks(ItemStack from, ItemStack to) {
		int sum = from.stackSize + to.stackSize;
		if (sum <= to.getMaxStackSize()) {
			to.stackSize = sum;
			from.stackSize = 0;
		}
		else {
			to.stackSize = to.getMaxStackSize();
			from.stackSize = sum - to.getMaxStackSize();
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
