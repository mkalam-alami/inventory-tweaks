package net.minecraft.src;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

public class InvTweaksSortingLogic {
	
	// (Note) In the inventory, indexes are :
	// 0 = bottom-left, 8 = bottom-right
	// 9 = top-left, 35 = 3rd-row-right
	
	private static final Logger log = Logger.getLogger("InvTweaksSortingLogic");

	public static int[] ALL_SLOTS;
	
	private boolean logging = false;
	private int[] lockedSlots;
	private ItemStack[] oldInv;
	private ItemStack[] newInv;
	private Vector<InvTweaksRule> rules;
	
	public InvTweaksSortingLogic() {
		
		logging = true;
		
		// Default slot order init
		if (ALL_SLOTS == null) {
			ALL_SLOTS = new int[InvTweaks.INV_SIZE];
	    	for (int i = 0; i < ALL_SLOTS.length; i++) {
	    		ALL_SLOTS[i] = (i + 9) % InvTweaks.INV_SIZE;
	    	}
		}
	}
	
    public ItemStack[] sort(ItemStack[] inventory,
    		Vector<InvTweaksRule> rules,
    		int[] lockedSlots) throws Exception {
    	
    	this.oldInv = inventory.clone();
    	this.lockedSlots = lockedSlots;
    	this.rules = rules;
    	newInv = new ItemStack[InvTweaks.INV_SIZE];
    	
    	mergeStacks();
    	applyRules();
    	applyLocks();
    	sortRemaining();
    	
		return newInv;
    }
    
	/**
	 * First pass to merge stacks.
	 * Locked stacks won't be processed.
	 * @param from
	 * @param to
	 */
    private void mergeStacks() {

		Vector<Integer> itemIDs = new Vector<Integer>();
		ItemStack stack;
		int search, i, j, k;
		
		for (k = 0; k < oldInv.length; k++) {
			i = ALL_SLOTS[k];
			
			stack = oldInv[i];
			if (stack != null) {
				
				search = 0;
				while (stack != null &&
						(j = itemIDs.indexOf(stack.itemID, search)) != -1) {
					if (mergeStacks(stack, i, oldInv[j], j))
						oldInv[i] = stack = null;
					else
						itemIDs.set(j, -1); // j is full
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
    	
    }

	private void applyRules() {

		Map<Integer, ItemStack> newlyOrderedStacks = new HashMap<Integer, ItemStack>();
		Iterator<InvTweaksRule> rulesIt = rules.iterator();
		InvTweaksRule rule;
		ItemStack wantedSlotStack;
		ItemStack stack;
		int rulePriority, i, j, k;
		
		// Sort rule by rule, themselves being already sorted by decreasing priority
		while (rulesIt.hasNext()) {
			
			rule = rulesIt.next();
			rulePriority = rule.getPriority();
			if (logging)
				log.info("Rule : "+rule.getKeyword()+"("+rule.getPriority()+")");
			
			// Look for item stacks that match the rule
			for (k = 0; k < oldInv.length; k++) {
				i = ALL_SLOTS[k];
				
				stack = oldInv[i];
				if (stack == null || lockedSlots[i] > rulePriority)
					continue;
				
				InvTweaksItem item = InvTweaksTree.getItem(stack.itemID);
				if (stack != null && InvTweaksTree.matches(item, rule.getKeyword())
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
									|| (InvTweaksTree.matches(
											InvTweaksTree.getItem(oldInv[preferredPos[j]].itemID),
											rule.getKeyword())))) {
							newInv[preferredPos[j]] = stack; // Put the stack in the new inventory!
							if (logging)
								log.info(InvTweaksTree.getItem(stack.itemID)+" ("+i+") put in "+preferredPos[j]+", "+i+" OK");
							oldInv[i] = null;
							newlyOrderedStacks.put(preferredPos[j], stack);
							break;
						}
						
						else if (wantedSlotStack != null) {
							
							if (mergeStacks(stack, i,
									newInv[preferredPos[j]], preferredPos[j]))
								oldInv[i] = stack = null;
							
							// If the slot is occupied, check (once) if the item
							// can replace one of the already put items. This
							// can be done if both constraints are respected:
							// * The item to replace has been put using the same rule
							// * The item to replace has a lower item priority
							if (stack != null && !checkedFilledPos) {
								
								Integer stackToReplaceKey = null;
								for (Integer stackKey : newlyOrderedStacks.keySet()) {
									if (InvTweaksTree.getKeywordOrder(item.getName()) <
										InvTweaksTree.getKeywordOrder(
											InvTweaksTree.getItem(
												newlyOrderedStacks.get(stackKey).itemID
											).getName())) {
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
									if (logging)
										log.info(InvTweaksTree.getItem(stack.itemID)+" replaces "+InvTweaksTree.getItem(wantedSlotStack.itemID));
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
		
	}
    
	/**
	 * Locked stacks don't move
	 */
	private void applyLocks() {
		for (int i = 0; i < oldInv.length; i++) {
			if (oldInv[i] != null && lockedSlots[i] > 0) {
				if (newInv[i] == null) {
					if (logging)
						log.info(InvTweaksTree.getItem(oldInv[i].itemID)+" doesn't move");
					newInv[i] = oldInv[i];
					oldInv[i] = null;
				}
				else if (newInv[i].itemID == oldInv[i].itemID) {
					mergeStacks(oldInv[i], i,
							newInv[i], i);
					if (oldInv[i].stackSize == 0) {
						oldInv[i] = null;
					}
				}
			}
		}
	}

	/** 
	 * Put stuff without a found spot in any free spot,
	 * starting from top-left.
	 * In two steps: first by skipping locked spots,
	 * then whatever spot it is, to avoid item loss.
	 * @throws Exception 
	 */
	private void sortRemaining() throws Exception {
		
		ItemStack stack, wantedSlotStack;
		int[] levels = new int[]{0, Integer.MAX_VALUE};
		int stackOrder, index, i, j, k;
		boolean emptySlotFound;
		
		for (j = 0; j < levels.length; j++) {
			
			for (k = 0; k < oldInv.length; k++) {
				i = ALL_SLOTS[k];

				stack = oldInv[i];
				if (stack == null || lockedSlots[i] > levels[j])
					continue;
				stackOrder = InvTweaksTree.getItem(stack.itemID).getOrder();
				index = -1;
				
				// Look for an empty spot
				emptySlotFound = false;
				while (!emptySlotFound) {
					index = getNextSlot(index+1, levels[j]);
					if (newInv[ALL_SLOTS[index]] != null) {
						
						// Try to merge if same item
						if (mergeStacks(stack, i,
								newInv[ALL_SLOTS[index]], ALL_SLOTS[index])) {
							if (logging)
								log.info("Merged (" +lockedSlots[i]+"="+lockedSlots[ALL_SLOTS[index]]+") : "+InvTweaksTree.getItem(stack.itemID)+i+" to "+ALL_SLOTS[index]);
							oldInv[i] = stack = null;
							break;
						}
						
						wantedSlotStack = newInv[ALL_SLOTS[index]];
						
						// Swap items, then restart search
						if (stackOrder < InvTweaksTree.
								getItem(wantedSlotStack.itemID).getOrder() &&
								lockedSlots[i] == lockedSlots[ALL_SLOTS[index]]) {
							if (logging)
								log.info("Swapping : "+InvTweaksTree.getItem(stack.itemID)+i+" goes to "+ALL_SLOTS[index]);
							newInv[ALL_SLOTS[index]] = stack;
							oldInv[i] = wantedSlotStack;
							
							// TODO: Refactoring
							stack = wantedSlotStack;
							stackOrder = InvTweaksTree.getItem(stack.itemID).getOrder();
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
						if (logging)
							log.info("Remaining : "+ALL_SLOTS[index]+" for "+InvTweaksTree.getItem(stack.itemID));
					}
					else if (j == levels.length) {
						throw new Exception("Some items could not be placed. The algorithm seems broken!");
					}
				}
			}
		}
	}
    
    /**
     * Tries to merge two stacks together.
     * Includes the check for matching item IDs.
     * @param from
     * @param to
     * @return true if the stacks have been merged in a single stack
     */
    private boolean mergeStacks(ItemStack from, int fromSlot,
    		ItemStack to, int toSlot) {
    	
    	// Check item IDs
    	if (from != null && to != null
    			&& from.itemID == to.itemID
    			&& lockedSlots[fromSlot] == lockedSlots[toSlot]) {
			int sum = from.stackSize + to.stackSize;
			if (sum <= to.getMaxStackSize()) {
				to.stackSize = sum;
				from.stackSize = 0;
				return true;
			}
			else {
				to.stackSize = to.getMaxStackSize();
				from.stackSize = sum - to.getMaxStackSize();
				return false;
			}
    	}
    	
    	return false;
    }
    
    /**
     * Returns next slot index, or -1.
     * Uses the whole inventory, from top-left to down-right.
     * Given and return integers are ALL_SLOTS indexes.
     */
    public static int getNextSlot(int index, int rulePriority, int[] lockedSlots) {
    	return getNextSlot(ALL_SLOTS, index, rulePriority, lockedSlots);
    }

    private int getNextSlot(int index, int rulePriority) {
    	return getNextSlot(ALL_SLOTS, index, rulePriority, lockedSlots);
    }

    /**
     * Returns next slot index, or -1.
     */
    private int getNextSlot(int[] preffered, int index,
    		int rulePriority) {
    	return getNextSlot(preffered, index, rulePriority, lockedSlots);
    }

    private static int getNextSlot(int[] preffered, int index,
    		int rulePriority, int[] lockedSlots) {
    	while (preffered.length > index) {
    		if (lockedSlots[preffered[index]] <= rulePriority)
    			return index;
    		index++;
    	}
    	return -1;
    }
        
}
