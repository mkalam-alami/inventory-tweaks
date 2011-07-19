package net.minecraft.src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

public class InvTweaksAlgorithm extends InvTweaksObf {
    
    private static final Logger log = Logger.getLogger("InvTweaks");

    public static final int DEFAULT = 0;
    public static final int INVENTORY = 1;
    public static final int HORIZONTAL = 2;
    public static final int VERTICAL = 3;

    private static final int MAX_CONTAINER_SIZE = 100;
    private static int[] DEFAULT_LOCK_PRIORITIES = null;
    
    private InvTweaksConfig config = null;
    
    public InvTweaksAlgorithm(Minecraft mc, InvTweaksConfig config) {
		super(mc);
		setConfig(config);
		
		if (DEFAULT_LOCK_PRIORITIES == null) {
			DEFAULT_LOCK_PRIORITIES = new int[MAX_CONTAINER_SIZE];
			for (int i = 0; i < MAX_CONTAINER_SIZE; i++) {
				DEFAULT_LOCK_PRIORITIES[i] = 0;
			}
		}
	}
    
    public void setConfig(InvTweaksConfig config) {
    	this.config = config;
    }

	/**
	 * Sort inventory
	 * @return The number of clicks that were needed
     */
    public final long sortContainer(Container container, int algorithm) {
    	
    	if (config == null)
    		return -1;
    	
    	// Do nothing if the inventory is closed
    	// if (!mc.hrrentScreen instanceof GuiContainer)
    	//		return;
    	
    	long timer = System.nanoTime();

    	InvTweaksContainer inventory;
    	
    	if (algorithm == INVENTORY) {
    		inventory = new InvTweaksContainer(mc, config.getLockPriorities(), container);
    	}
    	else {
    		inventory = new InvTweaksContainer(mc, DEFAULT_LOCK_PRIORITIES, container);
    	}

		//// Empty hand (needed in SMP)
		if (isMultiplayerWorld())
			inventory.putHoldItemDown();
		
		if (algorithm != DEFAULT) {
			
			Vector<InvTweaksRule> rules;
			Vector<Integer> lockedSlots;
			
			if (algorithm == INVENTORY) {
				rules = config.getRules();
				lockedSlots = config.getLockedSlots();
				
		    	//// Merge stacks to fill the ones in locked slots
				log.info("Merging stacks.");
				
		    	for (int i = inventory.getSize()-1; i >= 0; i--) {
		    		ItemStack from = inventory.getItemStack(i);
		    		if (from != null) {
		    	    	for (Integer j : lockedSlots) {
		    	    		ItemStack to = inventory.getItemStack(j);
		    	    		if (to != null && inventory.canBeMerged(i, j)) {
		    	    			boolean result = inventory.mergeStacks(i, j);
		    	    			inventory.markAsNotMoved(j);
		    	    			if (result == InvTweaksContainer.STACK_EMPTIED) {
		        	    			break;
		    	    			}
		    	    		}
		    	    	}
		    		}
		    	}
				
			}
			else {
				int rowSize = (container instanceof ContainerDispenser) ? 3 : 9;
				rules = computeLineSortingRules(inventory,
						rowSize, (algorithm == HORIZONTAL));
				lockedSlots = new Vector<Integer>();
			}
	    	
	    	//// Apply rules
			log.info("Applying rules.");
	    	
	    	// Sorts rule by rule, themselves being already sorted by decreasing priority
			Iterator<InvTweaksRule> rulesIt = rules.iterator();
			while (rulesIt.hasNext()) {
				
				InvTweaksRule rule = rulesIt.next();
				int rulePriority = rule.getPriority();
	
				if (log.getLevel() == InvTweaks.DEBUG)
					log.info("Rule : "+rule.getKeyword()+"("+rulePriority+")");
	
				for (int i = 0; i < inventory.getSize(); i++) {
					ItemStack from = inventory.getItemStack(i);
		    		
		    		if (inventory.hasToBeMoved(i) && 
		    				inventory.getLockLevel(i) < rulePriority) {
						List<InvTweaksItem> fromItems = InvTweaksTree.getItems(
								getItemID(from), getItemDamage(from));
		    			if (InvTweaksTree.matches(fromItems, rule.getKeyword())) {
		    				
		    				int[] preferredPos = rule.getPreferredPositions();
		    				for (int j = 0; j < preferredPos.length; j++) {
		    					int k = preferredPos[j];
		    					
		    					if (inventory.moveStack(i, k, rulePriority)) {
		    						from = inventory.getItemStack(i);
		    						if (from == null || i == k) {
		    							break;
		    						}
		    						else {
		    							fromItems = InvTweaksTree.getItems(
		    									getItemID(from), getItemDamage(from));
		    							if (!InvTweaksTree.matches(
		    									fromItems, rule.getKeyword())) {
		    								break;
		    							}
		    							else {
		    								j--;
		    							}
		    						}
			    				}
		    				}
		    			}
		    		}
				}
			}
	    	
			//// Don't move locked stacks
			log.info("Locking stacks.");
			
			for (int i = 0; i < inventory.getSize(); i++) {
				if (inventory.hasToBeMoved(i) && inventory.getLockLevel(i) > 0) {
					inventory.markAsMoved(i, 1);
				}
			}

		}
		
		//// Sort remaining
		defaultSorting(inventory);

		if (log.getLevel() == InvTweaks.DEBUG) {
			timer = System.nanoTime()-timer;
			log.info("Sorting done in "
					+ inventory.getClickCount() + " clicks and "
					+ timer + "ns");
		}

    	return inventory.getClickCount();
    }
    
    /**
     * Autoreplace + middle click sorting
     */
	public void autoReplaceSlot(int slot, int wantedId, int wantedDamage) {
   
		InvTweaksContainer inventory = new InvTweaksContainer(
				mc, config.getLockPriorities(), getPlayerContainer());  	
		ItemStack candidateStack, replacementStack = null;
		ItemStack storedStack = createItemStack(wantedId, 1, wantedDamage);
		int replacementStackSlot = -1;

		// Search replacement
		for (int i = 0; i < InvTweaks.INVENTORY_SIZE; i++) {
			// Look only for a matching stack
			candidateStack = inventory.getItemStack(i);
			if (candidateStack != null && 
					inventory.areSameItem(storedStack, candidateStack) &&
					config.canBeAutoReplaced(
							getItemID(candidateStack),
							getItemDamage(candidateStack))) {
				// Choose stack of lowest sItemStacke and (in case of tools) highest damage
				if (replacementStack == null ||
						getStackSize(replacementStack) > getStackSize(candidateStack) ||
						(getStackSize(replacementStack) == getStackSize(candidateStack) &&
								getMaxStackSize(replacementStack) == 1 &&
								getItemDamage(replacementStack) < getItemDamage(candidateStack))) {
					replacementStack = candidateStack;
					replacementStackSlot = i;
				}
			}
		}
		
		// Proceed to replacement
		if (replacementStack != null) {
			
			log.info("Automatic stack replacement.");
			
		    /*
		     * This allows to have a short feedback 
		     * that the stack/tool is empty/broken.
		     */
			new Thread(new Runnable() {

				private InvTweaksContainer inventory;
				private int targetedSlot;
				private int i, expectedItemId;
				
				public Runnable init(
						InvTweaksContainer inventory,
						int i, int currentItem) {
					this.inventory = inventory;
					this.targetedSlot = currentItem;
					this.expectedItemId = getItemID(inventory.getItemStack(i));
					this.i = i;
					return this;
				}
				
				public void run() {
					
					if (isMultiplayerWorld()) {
						// Wait for the server to confirm that the
						// slot is now empty
						int pollingTime = 0;
						setHasInventoryChanged(false);
						while(!hasInventoryChanged()
								&& pollingTime < InvTweaks.POLLING_TIMEOUT) {
							trySleep(InvTweaks.POLLING_DELAY);
						}
						if (pollingTime < InvTweaks.AUTOREPLACE_DELAY)
							trySleep(InvTweaks.AUTOREPLACE_DELAY - pollingTime);
						if (pollingTime >= InvTweaks.POLLING_TIMEOUT)
							log.warning("Autoreplace timout");
					}
					else {
						trySleep(InvTweaks.AUTOREPLACE_DELAY);
					}
					
					// In POLLING_DELAY ms, things might have changed
					try {
						ItemStack stack = inventory.getItemStack(i);
						if (stack != null && getItemID(stack) == expectedItemId) {
							inventory.moveStack(i, targetedSlot, Integer.MAX_VALUE);
						}
					}
					catch (NullPointerException e) {
						// Nothing: Due to multithreading + 
						// unsafe accesses, NPE may (very rarely) occur (?).
					}
					
			    	//onTickBusy = false; // TODO Check commenting this doesn't break SMP
				}
				
			}.init(inventory, replacementStackSlot, slot)).start();
			
		}
    }
	
	public static void trySleep(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			// Do nothing
		}
    }

	private void defaultSorting(InvTweaksContainer inventory) {
	
		log.info("Default sorting.");
		
		Vector<Integer> remaining = new Vector<Integer>(), nextRemaining = new Vector<Integer>();
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.hasToBeMoved(i)) {
				remaining.add(i);
				nextRemaining.add(i);
			}
		}
		
		int iterations = 0;
		while (remaining.size() > 0 && iterations++ < 50) {
			for (int i : remaining) {
				if (inventory.hasToBeMoved(i)) {
					for (int j = 0; j < inventory.getSize(); j++) {
						if (inventory.moveStack(i, j, 1)) {
							nextRemaining.remove((Object) j);
							break;
						}
					}
				}
				else {
					nextRemaining.remove((Object) i);
				}
			}
			remaining.clear();
			remaining.addAll(nextRemaining);
		}
		if (iterations == 50) {
			log.info("Sorting takes too long, aborting.");
		}
		
	}
	
	private Vector<InvTweaksRule> computeLineSortingRules(
			InvTweaksContainer container, int rowSize, boolean horizontal) {
		
		Vector<InvTweaksRule> rules = new Vector<InvTweaksRule>();
		Map<InvTweaksItem, Integer> stats = computeContainerStats(container);		
		List<InvTweaksItem> itemOrder = new ArrayList<InvTweaksItem>();

		int distinctItems = stats.size();
		int columnSize = getContainerColumnSize(container, rowSize);
		int spaceWidth;
		int spaceHeight;
		
		// (Partially) sort stats by decreasing item stack count
		List<InvTweaksItem> unorderedItems = new ArrayList<InvTweaksItem>(stats.keySet());
		boolean hasStacksToOrderFirst = true;
		while (hasStacksToOrderFirst) {
			hasStacksToOrderFirst = false;
			for (InvTweaksItem item : unorderedItems) {
				Integer value = stats.get(item);
				if (value > ((horizontal) ? rowSize : columnSize)
						&& !itemOrder.contains(item)) {
					hasStacksToOrderFirst = true;
					itemOrder.add(item);
					unorderedItems.remove(item);
					break;
				}
			}
		}
		Collections.sort(unorderedItems, Collections.reverseOrder());
		itemOrder.addAll(unorderedItems);
		
		// Define space size used for each item type.
		if (horizontal) {
			spaceHeight = 1;
			spaceWidth = rowSize/((distinctItems+columnSize-1)/columnSize);
		}
		else {
			spaceWidth = 1;
			spaceHeight = columnSize/((distinctItems+rowSize-1)/rowSize);
		}
		
		char row = 'a', maxRow = (char) (row - 1 + columnSize);
		char column = '1', maxColumn = (char) (column - 1 + rowSize);
		
		// Create rules
		Iterator<InvTweaksItem> it = itemOrder.iterator();
		while (it.hasNext()) {
			
			InvTweaksItem item = it.next();
			
			// Adapt rule dimensions to fit the amount
			int thisSpaceWidth = spaceWidth,
				thisSpaceHeight = spaceHeight;
			while (stats.get(item) > thisSpaceHeight*thisSpaceWidth) {
				if (horizontal) {
					if (column + thisSpaceWidth < maxColumn) {
						thisSpaceWidth = maxColumn - column + 1;
					}
					else if (row + thisSpaceHeight < maxRow) {
						thisSpaceHeight++;
					}
					else {
						break;
					}
				}
				else {
					if (row + thisSpaceHeight < maxRow) {
						thisSpaceHeight = maxRow - row + 1;
					}
					else if (column + thisSpaceWidth < maxColumn) {
						thisSpaceWidth++;
					}
					else {
						break;
					}
				}
			}
			
			// Adjust line/column ends to fill empty space
			if (horizontal && (column + thisSpaceWidth == maxColumn)) {
				thisSpaceWidth++;
			}
			else if (!horizontal && row + thisSpaceHeight == maxRow) {
				thisSpaceHeight++;
			}
			
			// Create rule
			String constraint = row + "" + column + "-"
					+ (char)(row - 1 + thisSpaceHeight)
					+ (char)(column - 1 + thisSpaceWidth);
			if (!horizontal) {
				constraint += 'v';
			}
			rules.add(new InvTweaksRule(constraint, item.getName(),
					container.getSize(), rowSize));
			
			// Move origin for next rule
			if (horizontal) {
				if (column + thisSpaceWidth + spaceWidth <= maxColumn + 1) {
					column += thisSpaceWidth;
				}
				else {
					column = '1';
					row += thisSpaceHeight;
				}
			}
			else {
				if (row + thisSpaceHeight + spaceHeight <= maxRow + 1) {
					row += thisSpaceHeight;
				}
				else {
					row = 'a';
					column += thisSpaceWidth;
				}
			}
			if (row > maxRow || column > maxColumn)
				break;
		}
		
		String defaultRule;
		if (horizontal) {
			defaultRule = maxRow + "1-a" + maxColumn;
		}
		else {
			defaultRule = "a" + maxColumn + "-" + maxRow + "1v";
		}
		rules.add(new InvTweaksRule(defaultRule, 
				InvTweaksTree.getRootCategory().getName(),
				container.getSize(), rowSize));
		
		return rules;
		
	}
	
	private Map<InvTweaksItem, Integer> computeContainerStats(InvTweaksContainer container) {
		Map<InvTweaksItem, Integer> stats = new HashMap<InvTweaksItem, Integer>();
		Map<Integer, InvTweaksItem> itemSearch = new HashMap<Integer, InvTweaksItem>();
		
		for (int i = 0; i < container.getSize(); i++) {
			ItemStack stack = container.getItemStack(i);
			if (stack != null) {
				int itemSearchKey = getItemID(stack)*100000 + 
						((getMaxStackSize(stack) != 1) ? getItemDamage(stack) : 0);
				InvTweaksItem item = itemSearch.get(itemSearchKey);
				if (item == null) {
					item = InvTweaksTree.getItems(
							getItemID(stack), getItemDamage(stack)).get(0);
					itemSearch.put(itemSearchKey, item);	
					stats.put(item, 1);
				}
				else {
					stats.put(item, stats.get(item) + 1);
				}
			}
		}
		
		return stats;
	}
	
	private int getContainerColumnSize(InvTweaksContainer container, int rowSize) {
		return container.getSize() / rowSize;
	}

}