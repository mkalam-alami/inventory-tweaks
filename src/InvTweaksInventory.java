package net.minecraft.src;

import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

public class InvTweaksInventory {

	private static final Logger log = Logger.getLogger("InvTweaksInventory");
	
	public static final int SIZE = 36;
	public static final boolean STACK_NOT_EMPTIED = true;
	public static final boolean STACK_EMPTIED = false;
	
	private ItemStack[] inventory;
	private int[] rulePriority = new int[SIZE];
	private int[] keywordOrder = new int[SIZE];
	private int[] oldSlot = new int[SIZE];
	private int[] lockLevels;
	private boolean logging;
	private int clickCount = 0;
	
	// Multiplayer
	private boolean isMultiplayer;
	private PlayerController playerController;
	private EntityPlayer player;
	
	public InvTweaksInventory(Minecraft minecraft, int[] lockLevels, boolean logging) {

		this.logging = logging;
		this.inventory = minecraft.thePlayer.inventory.mainInventory;
		this.playerController = minecraft.playerController;
		this.player = minecraft.thePlayer;
		this.isMultiplayer = minecraft.isMultiplayerWorld();
		this.lockLevels = lockLevels;
		
		for (int i = 0; i < SIZE; i++) {
			this.rulePriority[i] = this.keywordOrder[i] = 0;
			this.oldSlot[i] = i;
		}
		
	}
	
	public ItemStack getItemStack(int i) {
		return inventory[i];
	}
	
	/**
	 * Merge from stack i to stack j, only if i is not under a greater lock than j.
	 * @param i from slot
	 * @param j to slot
	 * @return STACK_NOT_EMPTIED if items remain in i, STACK_EMPTIED otherwise.
	 */
	public boolean mergeStacks(int i, int j) {
		if (lockLevels[i] <= lockLevels[j]) {
			return swap(i, j, 1) ? STACK_EMPTIED : STACK_NOT_EMPTIED;
		}
		else {
			return STACK_NOT_EMPTIED;
		}
	}
	
	/**
	 * Tries to move a stack from i to j, and swaps them
	 * if j is already occupied but i is of grater priority
	 * (even if they are of same ID).
	 * CONTRACT: i must not be null.
	 * @param i from slot
	 * @param j to slot
	 * @param priority The rule priority. Use 1 if the stack was not moved using a rule.
	 * @return true if it has been done successfully.
	 */
	public boolean moveStack(int i, int j, int priority) {
		
		if (getLockLevel(i) <= priority) {
		
			if (i == j) {
				markAsMoved(i);
				return true;
			}
			
			ItemStack from = inventory[i], to = inventory[j];
			
			// Move to empty slot
			if (to == null && lockLevels[j] <= priority) {
				if (swap(i, j, priority))
					oldSlot[i] = j;
				return true;
			}
			
			// Try to swap
			else if (to != null && lockLevels[j] <= priority
					&& (rulePriority[j] < priority ||
							(InvTweaksTree.getItem(from.itemID).getOrder()
								< InvTweaksTree.getItem(to.itemID).getOrder()))) {
				if (swap(i, j, priority))
					oldSlot[i] = j;
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasToBeMoved(int slot) {
		return inventory[slot] != null && rulePriority[slot] == 0;
	}

	/**
	 * Alternative to InvTweaksInventory.SIZE
	 */
	public int getSize() {
		return SIZE;
	}

	/**
	 * Swaps two stacks, i.e. clicks to i, then j, then back to i if necessary.
	 * If the stacks are able to be merged, the biggest part will then be in j.
	 * @param i
	 * @param j
	 * @return true if i is now empty
	 * 
	 */
	public boolean swap(int i, int j, int priority) {
		
		ItemStack jStack = inventory[j];
		
		// Merge stacks
		if (inventory[i] != null && inventory[j] != null && 
				inventory[i].itemID == inventory[j].itemID) {
			
			int sum = inventory[i].stackSize + inventory[j].stackSize;
			int max = inventory[j].getMaxStackSize();
			
			if (sum <= max) {
				if (isMultiplayer) {
					sendClicks(i, j);
				}
				else {
					remove(i);
					inventory[j].stackSize = sum;
				}
				return true;
			}
			else {
				if (isMultiplayer) {
					sendClicks(i, j, i);
				}
				else {
					inventory[j].stackSize = sum - max;
					inventory[j].stackSize = max;
				}
				return false;
			}
		}
		
		// Swap stacks
		else {
			// i to j
			if (isMultiplayer)
				sendClicks(i, j);
			else
				put(remove(i), j, priority);
			
			// j to i
			if (jStack != null) {
				if (isMultiplayer)
					sendClicks(i);
				else
					put(jStack, i, 0);
				return false;
			}
			else {
				return true;
			}
		}
	}
	
	/**
	 * Removes the stack from the given slot
	 * @param slot
	 * @return The removed stack
	 */
	private ItemStack remove(int slot) {
		ItemStack removed = inventory[slot];
		if (logging)
			log.info("Removed: "+InvTweaksTree.getItem(removed.itemID));
		inventory[slot] = null;
		rulePriority[slot] = 0;
		keywordOrder[slot] = 0;
		
		return removed;
	}
	
	/**
	 * Puts a stack in the given slot.
	 * WARNING: Any existing stack will be overriden!
	 * @param stack
	 * @param slot
	 * @param priority
	 */
	private void put(ItemStack stack, int slot, int priority) {
		if (logging)
			log.info("Put: "+InvTweaksTree.getItem(stack.itemID)+" in "+slot);
		inventory[slot] = stack;
		rulePriority[slot] = priority;
		keywordOrder[slot] = InvTweaksTree.getItem(stack.itemID).getOrder();
	}

	/**
	 * Notify server of a click.
	 * @param slot The targeted slot
	 * @param stack The stack that was in the slot before the operation
	 */
	private void sendClicks(int... slots) {
		clickCount += slots.length;
		for (int slot : slots) {
			if (logging) {
				log.info("Click on "+slot);
			}
			playerController.func_27174_a(
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

	public void markAsMoved(int i) {
		put(inventory[i], i, 1); // 1 = Just enough to consider it moved
	}

	public int getLockLevel(int i) {
		return lockLevels[oldSlot[i]];
	}
	
	public int getClickCount() {
		if (isMultiplayer) {
			return clickCount;
		}
		else
			return -1;
	}

}
