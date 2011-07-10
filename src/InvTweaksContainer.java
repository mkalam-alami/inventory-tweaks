package net.minecraft.src;

import java.util.List;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

public class InvTweaksContainer extends InvTweaksObf {

	private static final Logger log = Logger.getLogger("InvTweaks");
    
	public static final boolean STACK_NOT_EMPTIED = true;
	public static final boolean STACK_EMPTIED = false;

	private Container container;
	private int[] rulePriority;
	private int[] keywordOrder;
	private int[] lockLevels;
	private int clickCount = 0;
	private int offset; // offset of the first sortable item
	private int size;
	
	// Multiplayer
	private boolean isMultiplayer;
	private EntityPlayer entityPlayer;
	
	public InvTweaksContainer(Minecraft mc, int[] lockLevels, Container container) {
		super(mc);
		
		this.lockLevels = lockLevels;
		this.container = container;
		
		if (container instanceof ContainerPlayer) {
			this.size = InvTweaks.INVENTORY_SIZE;
			this.offset = 9; // 5 crafting slots + 4 armor slots
		}
		else {
			this.size = getSlots(container).size() - InvTweaks.INVENTORY_SIZE;
			this.offset = 0;
		}
		
		this.rulePriority = new int[size];
		this.keywordOrder = new int[size];
		for (int i = 0; i < size; i++) {
			this.rulePriority[i] = -1;
			ItemStack stack = getStackInSlot(i);
			if (stack != null) {
				this.keywordOrder[i] = getItemOrder(
						getItemID(stack),
						getItemDamage(stack));
			}
			else {
				this.keywordOrder[i] = -1;
			}
		}
		this.entityPlayer = getThePlayer();
		this.isMultiplayer = isMultiplayerWorld();
	}
	
	/**
	 * Tries to move a stack from i to j, and swaps them
	 * if j is already occupied but i is of grater priority
	 * (even if they are of same ID).
	 * CONTRACT: i slot must not be null.
	 * @param i from slot
	 * @param j to slot
	 * @param priority The rule priority. Use 1 if the stack was not moved using a rule.
	 * @return true if it has been done successfully.
	 */
	public boolean moveStack(int i, int j, int priority) {
		
		if (getLockLevel(i) <= priority) {
		
			if (i == j) {
				markAsMoved(i, priority);
				return true;
			}
			
			boolean targetEmpty = getStackInSlot(j) == null;
			
			// Move to empty slot
			if (targetEmpty && lockLevels[j] <= priority) {
				swapOrMerge(i, j, priority);
				return true;
			}

			// Try to swap/merge
			else if (!targetEmpty) {
				boolean canBeSwapped = false;
				if (lockLevels[j] <= priority) {
					if (rulePriority[j] < priority) {
						canBeSwapped = true;
					}
					else if (rulePriority[j] == priority) {
						if (isOrderedBefore(i, j)) {
							canBeSwapped = true;
						}
					}
				}
				if (canBeSwapped || canBeMerged(i, j)) {
					swapOrMerge(i, j, priority);
					return true;
				}
			}
			
		}
		
		return false;
	}

	/**
	 * Merge from stack i to stack j, only if i is not under a greater lock than j.
	 * @param i from slot
	 * @param j to slot
	 * @return STACK_NOT_EMPTIED if items remain in i, STACK_EMPTIED otherwise.
	 */
	public boolean mergeStacks(int i, int j) {
		if (lockLevels[i] <= lockLevels[j]) {
			return swapOrMerge(i, j, 1) ? STACK_EMPTIED : STACK_NOT_EMPTIED;
		}
		else {
			return STACK_NOT_EMPTIED;
		}
	}

	public boolean hasToBeMoved(int slot) {
		return getStackInSlot(slot) != null && rulePriority[slot] == -1;
	}

	/**
	 * Note: asserts stacks are not null
	 */
	public boolean areSameItem(ItemStack stack1, ItemStack stack2) {
		// Note: may be invalid if a stackable item can take damage
		// (currently never the case in vanilla, an never should be)
		return getItemID(stack1) == getItemID(stack2)
				&& (getItemDamage(stack1) == getItemDamage(stack2) // same item variant
						|| getMaxStackSize(stack1) == 1); // except if unstackable
	}

	public boolean canBeMerged(int i, int j) {
		if (i == j || getStackInSlot(i) == null || getStackInSlot(j) == null) {
			return false;
		}
		return areSameItem(getStackInSlot(i), getStackInSlot(j)) &&
			getStackSize(getStackInSlot(j)) < getMaxStackSize(getStackInSlot(j));
	}

	public boolean isOrderedBefore(int i, int j) {
		
		if (getStackInSlot(j) == null) {
			return true;
		}
		else if (getStackInSlot(i) == null || keywordOrder[i] == -1) {
			return false;
		}
		else {
			if (keywordOrder[i] == keywordOrder[j]) {
				// Items of same keyword orders can have different IDs,
				// in the case of categories defined by a range of IDs
				if (getItemID(getStackInSlot(i)) == getItemID(getStackInSlot(j))) {
					if (getStackSize(getStackInSlot(i)) == getStackSize(getStackInSlot(j))) {
						// Highest damage first for tools, else lowest damage.
						// No tool ordering for same ID in multiplayer (cannot swap directly)
						return (getItemDamage(getStackInSlot(i)) > getItemDamage(getStackInSlot(j))
									&& getMaxStackSize(getStackInSlot(j)) == 1 && !isMultiplayer)
								|| (getItemDamage(getStackInSlot(i)) < getItemDamage(getStackInSlot(j))
										&& getMaxStackSize(getStackInSlot(j)) > 1);
					}
					else {
						return getStackSize(getStackInSlot(i)) > getStackSize(getStackInSlot(j));
					}
				}
				else {
					return getItemID(getStackInSlot(i)) > getItemID(getStackInSlot(j));
				}
			}
			else {
				return keywordOrder[i] < keywordOrder[j];
			}
		}
	}

	/**
	 * Swaps two stacks, i.e. clicks to i, then j, then back to i if necessary.
	 * If the stacks are able to be merged, the biggest part will then be in j.
	 * @param i
	 * @param j
	 * @return true if i is now empty
	 * 
	 */
	public boolean swapOrMerge(int i, int j, int priority) {
		
		// Merge stacks
		if (canBeMerged(i, j)) {
			
			int sum = getStackSize(getStackInSlot(i)) + getStackSize(getStackInSlot(j));
			int max = getMaxStackSize(getStackInSlot(j));
			
			if (sum <= max) {
				
				remove(i);
				if (isMultiplayer) {
					click(i);
				}

				put(getStackInSlot(j), j, priority);
				if (isMultiplayer) {
					click(j);
				}
				else {
					setStackSize(getStackInSlot(j), sum);
				}
				return true;
			}
			else {
				if (isMultiplayer) {
					click(i);
					click(j);
					click(i);
				}
				else {
					setStackSize(getStackInSlot(i), sum - max);
					setStackSize(getStackInSlot(j), max);
				}
				put(getStackInSlot(j), j, priority);
				return false;
			}
		}
		
		// Swap stacks
		else {
			
			// i to j
			ItemStack jStack = getStackInSlot(j);
			ItemStack iStack = remove(i);
			if (isMultiplayer) {
				click(i);
				click(j);
			}
			put(iStack, j, priority);
			
			// j to i
			if (jStack != null) {
				int dropSlot = i;
				if (lockLevels[j] > lockLevels[i]) {
					for (int k = 0; k < size; k++) {
						if (getStackInSlot(k) == null && lockLevels[k] == 0) {
							dropSlot = k;
							break;
						}
					}
				}
				if (isMultiplayer) {
					click(dropSlot);
				}
				put(jStack, dropSlot, -1);
				return false;
			}
			else {
				return true;
			}
		}
	}

	public void markAsMoved(int i, int priority) {
		rulePriority[i] = priority;
	}

	public void markAsNotMoved(int i) {
		rulePriority[i] = -1;
	}

	/**
	 * If an item is in hand (= attached to the cursor), puts it down.
	 * @return false if there is no room to put the item.
	 */
	public boolean putHoldItemDown() {
		ItemStack holdStack = getHoldStack();
		if (holdStack != null) {
			// Try to find an unlocked slot first, to avoid
			// impacting too much the sorting
			for (int step = 1; step <= 2; step++) {
				for (int i = size-1; i >= 0; i--) {
					if (getStackInSlot(i) == null
							&& (lockLevels[i] == 0 || step == 2)) {
						if (isMultiplayer) {
							click(i);
						}
						else {
							putStackInSlot(i, holdStack);
							setHoldStack(null);
						}
						return true;
					}
				}
			}
			return false;
		}
		return true;
	}

	public int getClickCount() {
		if (isMultiplayer) {
			return clickCount;
		}
		else
			return -1;
	}

	public ItemStack getItemStack(int i) {
		return getStackInSlot(i);
	}
	
	public int getLockLevel(int i) {
		return lockLevels[i];
	}

	public int getSize() {
		return size;
	}
	
	/**
	 * (Multiplayer only)
	 * Click on the interface. Slower than manual swapping, but works in multiplayer.
	 * @param slot The targeted slot
	 * @param priority Ignored
	 * @param oldSlot The stacks previous spot
	 * @param stack The stack that was in the slot before the operation
	 */
	public void click(int slot) {
		clickCount++;
		
		if (log.getLevel() == InvTweaks.DEBUG)
			log.info("Click on "+slot);
		
		// After clicking, we'll need to wait for server answer before continuing.
		// We'll do this by listening to any change in the slot, but this implies we
		// check first if the click will indeed produce a change.
		boolean uselessClick = false;
		ItemStack stackInSlot = (getStackInSlot(slot) != null) ? copy(getStackInSlot(slot)) : null;
		ItemStack stackInHand = getHoldStack();
		
		// Useless if empty stacks
		if (stackInHand == null && stackInSlot == null)
			uselessClick = true;
		// Useless if destination stack is full
		else if (stackInHand != null && stackInSlot != null &&
				areSameItem(stackInHand, stackInSlot) &&
				getStackSize(stackInSlot) == getMaxStackSize(stackInSlot)) {
			uselessClick = true;
		}
		
		// Click!
		clickInventory(getPlayerController(),
				getWindowId(container), // Select container
				slot + offset, // Targeted slot
				0, // Left-click
				false, // Shift not held 
				entityPlayer
			);
		
		// Wait for inventory update
		if (!uselessClick) {
			int pollingTime = 0;
			while (areItemStacksEqual(getStackInSlot(slot), stackInSlot)
					&& pollingTime < InvTweaks.POLLING_TIMEOUT) {
				InvTweaksAlgorithm.trySleep(InvTweaks.POLLING_DELAY);
				pollingTime += InvTweaks.POLLING_DELAY;
			}
			if (pollingTime >= InvTweaks.POLLING_TIMEOUT) {
				log.warning("Click timout");
			}
		}
	}
	
	private int getItemOrder(int itemID, int itemDamage) {
		List<InvTweaksItem> items = InvTweaksTree.getItems(itemID, itemDamage);
		return (items != null && items.size() > 0)
				? items.get(0).getOrder()
				: Integer.MAX_VALUE;
	}

	/**
	 * SP: Removes the stack from the given slot
	 * SMP: Registers the action without actually doing it.
	 * @param slot
	 * @return The removed stack
	 */
	private ItemStack remove(int slot) {
		ItemStack removed = getStackInSlot(slot);
		if (log.getLevel() == InvTweaks.DEBUG) {
			try {
				log.info("Removed: "+InvTweaksTree.getItems(
						getItemID(removed), getItemDamage(removed)).get(0)+" from "+slot);
			}
			catch (NullPointerException e) {
				log.info("Removed: null from "+slot);
			}
		}
		if (!isMultiplayer) {
			putStackInSlot(slot, null);
		}
		rulePriority[slot] = -1;
		keywordOrder[slot] = -1;
		return removed;
	}
	
	/**
	 * SP: Puts a stack in the given slot. WARNING: Any existing stack will be overriden!
	 * SMP: Registers the action without actually doing it.
	 * @param stack
	 * @param slot
	 * @param priority
	 */
	private void put(ItemStack stack, int slot, int priority) {
		if (log.getLevel() == InvTweaks.DEBUG) {
			try {
				log.info("Put: "+InvTweaksTree.getItems(
						getItemID(stack), getItemDamage(stack)).get(0)+" in "+slot);
			}
			catch (NullPointerException e) {
				log.info("Removed: null");
			}
		}
		if (!isMultiplayer) {
			putStackInSlot(slot, stack);
		}
		rulePriority[slot] = priority;
		keywordOrder[slot] = getItemOrder(getItemID(stack), getItemDamage(stack));
	}
	
	private ItemStack getStackInSlot(int i) {
		return getSlotStack(container, i + offset);
	}
	
	private void putStackInSlot(int i, ItemStack stack) {
		setSlotStack(container, i + offset, stack);
	}
	
}
