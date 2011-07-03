import java.util.List;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

public class InvTweaksInventory extends InvTweaksObf {

	private static final Logger log = Logger.getLogger("InvTweaks");
    
	public static final int SIZE = 36;
	public static final boolean STACK_NOT_EMPTIED = true;
	public static final boolean STACK_EMPTIED = false;

	private iz[] inventory;
	private int[] rulePriority = new int[SIZE];
	private int[] keywordOrder = new int[SIZE];
	private int[] lockLevels;
	private int clickCount = 0;
	
	// Multiplayer
	private boolean isMultiplayer;
	private gs player;
	
	public InvTweaksInventory(Minecraft mc, int[] lockLevels) {
		super(mc);
		
		this.inventory = getMainInventory();
		this.lockLevels = lockLevels;
		for (int i = 0; i < SIZE; i++) {
			this.rulePriority[i] = -1;
			if (this.inventory[i] != null) {
				this.keywordOrder[i] = getItemOrder(
						getItemID(this.inventory[i]),
						getItemDamage(this.inventory[i]));
			}
		}

		this.player = getThePlayer();
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
			
			boolean targetEmpty = inventory[j] == null;
			
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
		return inventory[slot] != null && rulePriority[slot] == -1;
	}

	/**
	 * Note: asserts stacks are not null
	 */
	public boolean areSameItem(iz stack1, iz stack2) {
		// Note: may be invalid if a stackable item can take damage
		// (currently never the case in vanilla, an never should be)
		return getItemID(stack1) == getItemID(stack2)
				&& (getItemDamage(stack1) == getItemDamage(stack2) // same item variant
						|| stack1.c() == 1); // except if unstackable
	}

	public boolean canBeMerged(int i, int j) {
		return (i != j && inventory[i] != null && inventory[j] != null && 
				areSameItem(inventory[i], inventory[j]) &&
				inventory[j].a < inventory[j].c());
	}

	public boolean isOrderedBefore(int i, int j) {
		
		if (inventory[j] == null)
			return true;
		else if (inventory[i] == null || keywordOrder[i] == -1)
			return false;
		else {
			if (keywordOrder[i] == keywordOrder[j]) {
				// Items of same keyword orders can have different IDs,
				// in the case of categories defined by a range of IDs
				if (getItemID(inventory[i]) == getItemID(inventory[j])) {
					if (inventory[i].a == inventory[j].a) {
						// Highest damage first for tools, else lowest damage.
						// No tool ordering for same ID in multiplayer (cannot swap directly)
						return (getItemDamage(inventory[i]) > getItemDamage(inventory[j])
									&& inventory[j].c() == 1 && !isMultiplayer)
								|| (getItemDamage(inventory[i]) < getItemDamage(inventory[j])
										&& inventory[j].c() > 1);
					}
					else {
						return getStackSize(inventory[i]) > getStackSize(inventory[j]);
					}
				}
				else {
					return getItemID(inventory[i]) > getItemID(inventory[j]);
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
			
			int sum = inventory[i].a + inventory[j].a;
			int max = inventory[j].c();
			
			if (sum <= max) {
				
				remove(i);
				if (isMultiplayer)
					click(i);

				put(inventory[j], j, priority);
				if (isMultiplayer)
					click(j);
				else
					inventory[j].a = sum;
				return true;
			}
			else {
				if (isMultiplayer) {
					click(i);
					click(j);
					click(i);
				}
				else {
					setStackSize(inventory[i], sum - max);
					setStackSize(inventory[j], max);
				}
				put(inventory[j], j, priority);
				return false;
			}
		}
		
		// Swap stacks
		else {
			
			// i to j
			iz jStack = inventory[j];
			iz iStack = remove(i);
			if (isMultiplayer) {
				click(i);
				click(j);
			}
			put(iStack, j, priority);
			
			// j to i
			if (jStack != null) {
				int dropSlot = i;
				if (lockLevels[j] > lockLevels[i]) {
					for (int k = 0; k < SIZE; k++) {
						if (inventory[k] == null && lockLevels[k] == 0) {
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
		iz holdStack = getHoldStack();
		if (holdStack != null) {
			// Try to find an unlocked slot first, to avoid
			// impacting too much the sorting
			for (int step = 1; step <= 2; step++) {
				for (int i = SIZE-1; i >= 0; i--) {
					if (inventory[i] == null
							&& (lockLevels[i] == 0 || step == 2)) {
						if (isMultiplayer) {
							click(i);
						}
						else {
							inventory[i] = holdStack;
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

	public iz getItemStack(int i) {
		return inventory[i];
	}
	
	public int getLockLevel(int i) {
		return lockLevels[i];
	}

	/**
	 * Alternative to InvTweaksInventory.SIZE
	 */
	public int getSize() {
		return SIZE;
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
		iz stackInSlot = (inventory[slot] != null) ? copy(inventory[slot]) : null;
		iz stackInHand = getHoldStack();
		
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
				player.e.f, // Select active inventory
				((slot > 8) ? slot - 9 : slot + 27) + 
					player.e.e.size() - 36, // Targeted slot
						// (converted for the network protocol indexes,
						// see http://mc.kev009.com/Inventory#Windows)
				0, // Left-click
				false, // Shift not held 
				player
			);
		
		// Wait for inventory update
		if (!uselessClick) {
			int pollingTime = 0;
			while (iz.a(inventory[slot], stackInSlot)
					&& pollingTime < InvTweaks.POLLING_TIMEOUT) {
				InvTweaks.trySleep(InvTweaks.POLLING_DELAY);
				pollingTime += InvTweaks.POLLING_DELAY;
			}
			if (pollingTime >= InvTweaks.POLLING_TIMEOUT)
				log.warning("Click timout");
		}
	}
	
	/**
	 * SP: Removes the stack from the given slot
	 * SMP: Registers the action without actually doing it.
	 * @param slot
	 * @return The removed stack
	 */
	private iz remove(int slot) {
		iz removed = inventory[slot];
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
			inventory[slot] = null;
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
	private void put(iz stack, int slot, int priority) {
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
			inventory[slot] = stack;
		}
		rulePriority[slot] = priority;
		keywordOrder[slot] = getItemOrder(getItemID(stack), getItemDamage(stack));
	}
	
	private int getItemOrder(int itemID, int itemDamage) {
		List<InvTweaksItem> items = InvTweaksTree.getItems(itemID, itemDamage);
		return (items != null && items.size() > 0)
				? items.get(0).getOrder()
				: Integer.MAX_VALUE;
	}
}
