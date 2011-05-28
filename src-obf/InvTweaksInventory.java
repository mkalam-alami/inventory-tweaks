import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

public class InvTweaksInventory {

	private static final Logger log = Logger.getLogger("InvTweaksInventory");
	
	public static final int SIZE = 36;
	public static final boolean STACK_NOT_EMPTIED = true;
	public static final boolean STACK_EMPTIED = false;

	private iw[] inventory;
	private int[] rulePriority = new int[SIZE];
	private int[] keywordOrder = new int[SIZE];
	private int[] oldSlot = new int[SIZE];
	private int[] lockLevels;
	private boolean logging;
	private int clickCount = 0;
	
	// Multiplayer
	private boolean isMultiplayer;
	private nx playerController;
	private gq player;
	
	public InvTweaksInventory(Minecraft minecraft, int[] lockLevels, boolean logging) {

		this.logging = logging;
		this.inventory = minecraft.h.c.a;
		this.playerController = minecraft.c;
		this.player = minecraft.h;
		this.isMultiplayer = minecraft.l();
		this.lockLevels = lockLevels;
		
		for (int i = 0; i < SIZE; i++) {
			this.rulePriority[i] = 0;
			this.oldSlot[i] = i;
			if (this.inventory[i] != null)
				this.keywordOrder[i] = InvTweaksTree.getItem(this.inventory[i].c).getOrder();
		}
		
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
			else if (!targetEmpty && lockLevels[j] <= priority
					&& (rulePriority[j] < priority || 
							(rulePriority[j] == priority && isOrderedBefore(i, j)))) {
				swapOrMerge(i, j, priority);
				return true;
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
		return inventory[slot] != null && rulePriority[slot] == 0;
	}

	public boolean canBeMerged(int i, int j) {
		return (inventory[i] != null && inventory[j] != null && 
				inventory[i].c == inventory[j].c &&
				inventory[i].i() == inventory[j].i() &&
				inventory[j].a < inventory[j].c());
	}

	public boolean isOrderedBefore(int i, int j) {
		
		if (inventory[j] == null)
			return true;
		
		else if (inventory[i] == null)
			return false;
		
		else if (keywordOrder[i] == 0)
			return false;
		
		else {
			if (keywordOrder[i] == keywordOrder[j]) {
				if (inventory[i].a == inventory[j].a) {
					return inventory[i].i() < inventory[j].i();
				}
				else {
					return inventory[i].a < inventory[j].a;
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
		
		iw jStack = inventory[j];
		
		// Merge stacks
		if (canBeMerged(i, j)) {
			
			int sum = inventory[i].a + inventory[j].a;
			int max = inventory[j].c();
			
			if (sum <= max) {
				remove(i);
				if (!isMultiplayer) {
					inventory[j].a = sum;
				}
				put(inventory[j], j, priority);
				return true;
			}
			else {
				if (!isMultiplayer) {
					inventory[j].a = sum - max;
					inventory[j].a = max;
				}
				put(inventory[j], j, priority);
				return false;
			}
		}
		
		// Swap stacks
		else {
			
			// Swap original slots
			int buffer = oldSlot[i];
			oldSlot[i] = oldSlot[j];
			oldSlot[j] = buffer;
			
			// i to j
			put(remove(i), j, priority);
			
			// j to i
			if (jStack != null) {
				put(jStack, i, 0);
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
		rulePriority[i] = 0;
	}

	public int getClickCount() {
		if (isMultiplayer) {
			return clickCount;
		}
		else
			return -1;
	}

	public iw getItemStack(int i) {
		return inventory[i];
	}
	
	public int getLockLevel(int i) {
		return lockLevels[oldSlot[i]];
	}

	/**
	 * Alternative to InvTweaksInventory.SIZE
	 */
	public int getSize() {
		return SIZE;
	}

	/**
	 * Removes the stack from the given slot
	 * @param slot
	 * @return The removed stack
	 */
	private iw remove(int slot) {
		
		rulePriority[slot] = 0;
		keywordOrder[slot] = 0;

		iw removed = inventory[slot];
		
		if (isMultiplayer) {
			rulePriority[slot] = 0;
			keywordOrder[slot] = 0;
			sendClick(slot);
		}
		else {
			if (logging)
				log.info("Removed: "+InvTweaksTree.getItem(removed.c));
			inventory[slot] = null;
		}
		
		return removed;
	}
	
	/**
	 * Puts a stack in the given slot.
	 * WARNING: Any existing stack will be overriden!
	 * @param stack
	 * @param slot
	 * @param priority
	 */
	private void put(iw stack, int slot, int priority) {
		if (isMultiplayer) {
			sendClick(slot);
		}
		else {
			if (logging)
				log.info("Put: "+InvTweaksTree.getItem(stack.c)+" in "+slot);
			inventory[slot] = stack;
		}
		rulePriority[slot] = priority;
		keywordOrder[slot] = InvTweaksTree.getItem(stack.c).getOrder();
	}

	/**
	 * Click on the interface. Slower than manual swapping, but works in multiplayer.
	 * Use this method if the stack still has to be moved.
	 * @param slot The targeted slot
	 * @param priority Ignored
	 * @param oldSlot The stacks previous spot
	 * @param stack The stack that was in the slot before the operation
	 */
	private void sendClick(int slot) {
		clickCount++;
		if (logging) {
			log.info("Click on "+slot);
		}
		playerController.a(
				0, // Select player inventory
				(slot > 8) ? slot : slot+36, // Targeted slot
						// (converted for the network protocol indexes,
						// see http://mc.kev009.com/Inventory#Windows)
				0, // Left-click
				false, // Shift not held 
				player
			);
	}

}
