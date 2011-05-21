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
	private NetClientHandler client;
	private Container container; 
	private InventoryPlayer invPlayer;
	
	public InvTweaksInventory(Minecraft minecraft, int[] lockLevels, boolean logging) {

		this.logging = logging;
		this.invPlayer = minecraft.thePlayer.inventory;
		this.inventory = minecraft.thePlayer.inventory.mainInventory;
		this.client = minecraft.func_20001_q();
		this.isMultiplayer = minecraft.isMultiplayerWorld();
		this.container = minecraft.thePlayer.craftingInventory;
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
	 * CONTRACT: i & j must not be null, and contain items of same ID
	 * @param i from slot
	 * @param j to slot
	 * @return STACK_NOT_EMPTIED if items remain in i, STACK_EMPTIED otherwise.
	 */
	public boolean mergeStacks(int i, int j) {
		if (lockLevels[i] <= lockLevels[j]) {
			int sum = inventory[i].stackSize + inventory[j].stackSize;
			int max = inventory[i].getMaxStackSize();
			
			// Stacks are merged into one
			if (sum <= max) {
				sendClick(i, inventory[i]);
				sendClick(j, inventory[j]);
				remove(i);
				inventory[j].stackSize = sum;
				return false;
			}
			
			// The rest goes back to the origin slot
			else {
				sendClick(i, inventory[i]);
				sendClick(j, inventory[j]);
				sendClick(i, null);
				inventory[i].stackSize = sum - max;
				inventory[j].stackSize = max;
				return true;
			}
		}
		
		else {
			return true;
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
				sendClick(i, inventory[i]);
				sendClick(j, inventory[j]);
				put(remove(i), j, priority);
				return true;
			}
			
			// Try to swap
			else if (to != null && lockLevels[j] <= priority
					&& (rulePriority[j] < priority ||
							(InvTweaksTree.getItem(from.itemID).getOrder()
								< InvTweaksTree.getItem(to.itemID).getOrder()))) {
				sendClick(i, inventory[i]);
				sendClick(j, inventory[j]);
				sendClick(i, null);
				put(remove(i), j, priority);
				put(to, i, 0);
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
	
	private ItemStack remove(int slot) {
		ItemStack removed = inventory[slot];
		if (logging)
			log.info("Removed: "+InvTweaksTree.getItem(removed.itemID));
		inventory[slot] = null;
		rulePriority[slot] = 0;
		keywordOrder[slot] = 0;
		return removed;
	}
	
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
	private void sendClick(int slot, ItemStack stack) {
		clickCount++;
		if (isMultiplayer) {
			if (logging) {
				if (stack != null)
					log.info("Click on "+slot+" containing "+InvTweaksTree.getItem(stack.itemID).getName());
				else
					log.info("Click on "+slot+" (empty)");
			}
			client.addToSendQueue(new Packet102WindowClick(
					0, // Select player inventory
					(slot > 8) ? slot : slot+36, // Targeted slot
							// (converted for the network protocol indexes,
							// see http://mc.kev009.com/Inventory#Windows)
					0, // Left-click
					false, // Shift not held 
					stack, // ItemStack
					container.func_20111_a(invPlayer) // Packet ID
				));
		}
	}

	public void markAsMoved(int i) {
		put(inventory[i], i, 1); // 1 = Just enough to consider it moved
	}

	public int getLockLevel(int i) {
		return lockLevels[oldSlot[i]];
	}
	
	public int getClickCount() {
		return clickCount;
	}

}
