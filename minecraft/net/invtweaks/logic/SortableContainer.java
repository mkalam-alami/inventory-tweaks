package net.invtweaks.logic;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import net.invtweaks.Const;
import net.invtweaks.Obfuscation;
import net.invtweaks.config.InvTweaksConfig;
import net.invtweaks.tree.ItemTree;
import net.invtweaks.tree.ItemTreeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Container;
import net.minecraft.src.ContainerPlayer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;

/**
 * Core of the sorting behaviour. Allows to move items in a container
 * (inventory or chest) with respect to the mod's configuration.
 * 
 * Here are the different layers of functions, from high to low levels:
 * moveStack
 *   |- swapOrMerge
 *       |- remove
 *           |- putStackInSlot
 *       |- put
 *           |- putStackInSlot
 *       |- click (SMP only)
 * 
 * @author Jimeo Wan
 *
 */
public class SortableContainer extends Obfuscation {

    private static final Logger log = Logger.getLogger("InvTweaks");

    public static final boolean STACK_NOT_EMPTIED = true;
    public static final boolean STACK_EMPTIED = false;

    public static final int MOVE_OK_OLD_SLOT_EMPTY = -2;
    public static final int MOVE_FAILURE = -1;

    private static int[] DEFAULT_LOCK_PRIORITIES = null;
    private static boolean[] DEFAULT_FROZEN_SLOTS = null;
    private static final int MAX_CONTAINER_SIZE = 100;

    private Container container;
    private ItemTree tree;
    private int[] rulePriority;
    private int[] keywordOrder;
    private int[] lockPriorities;
    private boolean[] frozenSlots;
    private int clickCount = 0;
    private int offset; // offset of the first sortable item
    private int size;
    private int timeSpentWaiting = 0;

    // Multiplayer
    private boolean isMultiplayer;
    private EntityPlayer entityPlayer;

    public SortableContainer(Minecraft mc, InvTweaksConfig config, Container container, boolean inventoryPart) {
        super(mc);

        // Init constants

        if (DEFAULT_LOCK_PRIORITIES == null) {
            DEFAULT_LOCK_PRIORITIES = new int[MAX_CONTAINER_SIZE];
            for (int i = 0; i < MAX_CONTAINER_SIZE; i++) {
                DEFAULT_LOCK_PRIORITIES[i] = 0;
            }
        }
        if (DEFAULT_FROZEN_SLOTS == null) {
            DEFAULT_FROZEN_SLOTS = new boolean[MAX_CONTAINER_SIZE];
            for (int i = 0; i < MAX_CONTAINER_SIZE; i++) {
                DEFAULT_FROZEN_SLOTS[i] = false;
            }
        }

        // Init attributes

        this.tree = config.getTree();
        this.container = container;

        if (container instanceof ContainerPlayer) {
            this.size = Const.INVENTORY_SIZE;
            this.offset = 9; // 5 crafting slots + 4 armor slots
            this.lockPriorities = config.getLockPriorities();
            this.frozenSlots = config.getFrozenSlots();
        }
        else if (inventoryPart) {
            this.size = Const.INVENTORY_SIZE;
            this.offset = getSlots(container).size() - Const.INVENTORY_SIZE;
            this.lockPriorities = config.getLockPriorities();
            this.frozenSlots = config.getFrozenSlots();
        }
        else {
            this.size = getSlots(container).size() - Const.INVENTORY_SIZE;
            this.offset = 0;
            this.lockPriorities = DEFAULT_LOCK_PRIORITIES;
            this.frozenSlots = DEFAULT_FROZEN_SLOTS;
        }

        this.rulePriority = new int[size];
        this.keywordOrder = new int[size];
        for (int i = 0; i < size; i++) {
            this.rulePriority[i] = -1;
            ItemStack stack = getStackInSlot(i);
            if (stack != null) {
                this.keywordOrder[i] = getItemOrder(getItemID(stack), getItemDamage(stack));
            } else {
                this.keywordOrder[i] = -1;
            }
        }
        this.entityPlayer = getThePlayer();
        this.isMultiplayer = isMultiplayerWorld();
    }

    /**
     * Tries to move a stack from i to j, and swaps them if j is already
     * occupied but i is of grater priority (even if they are of same ID).
     * CONTRACT: i slot must not be null.
     * 
     * @param i from slot
     * @param j to slot
     * @param priority  The rule priority. Use 1 if the stack was not moved using a rule.
     * @return MOVE_FAILURE if it failed, MOVE_TO_EMPTY_SLOT if the j slot was
     *         empty, n if the j stack has been put in the n slot.
     * @throws TimeoutException
     */
    public int moveStack(int i, int j, int priority) throws TimeoutException {

        if (frozenSlots[j] || frozenSlots[i]) {
            return MOVE_FAILURE;
        }

        if (getLockPriority(i) <= priority) {

            if (i == j) {
                markAsMoved(i, priority);
                return i;
            }

            boolean targetEmpty = (getStackInSlot(j) == null);

            // Move to empty slot
            if (targetEmpty && lockPriorities[j] <= priority && !frozenSlots[j]) {
                swapOrMerge(i, j, priority);
                return MOVE_OK_OLD_SLOT_EMPTY;
            }

            // Try to swap/merge
            else if (!targetEmpty) {
                boolean canBeSwapped = false;
                if (lockPriorities[j] <= priority) {
                    if (rulePriority[j] < priority) {
                        canBeSwapped = true;
                    } else if (rulePriority[j] == priority) {
                        if (isOrderedBefore(i, j)) {
                            canBeSwapped = true;
                        }
                    }
                }
                if (canBeSwapped || canBeMerged(i, j)) {
                    return swapOrMerge(i, j, priority);
                }
            }

        }

        return MOVE_FAILURE;
    }

    /**
     * Swaps two stacks, i.e. clicks to i, then j, then back to i if necessary.
     * If the stacks are able to be merged, the biggest part will then be in j.
     * 
     * @param i
     * @param j
     * @return MOVE_FAILURE if it failed, MOVE_TO_EMPTY_SLOT if j was empty or
     *         stacks are merged into one, n if j is now in n (at least partially)
     * @throws TimeoutException
     * 
     */
    public int swapOrMerge(int i, int j, int priority) throws TimeoutException {
    
        if (frozenSlots[j] || frozenSlots[i]) {
            return MOVE_FAILURE;
        }
    
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
                } else {
                    setStackSize(getStackInSlot(j), sum);
                }
                return MOVE_OK_OLD_SLOT_EMPTY;
            } else {
                if (isMultiplayer) {
                    click(i);
                    click(j);
                    click(i);
                } else {
                    setStackSize(getStackInSlot(i), sum - max);
                    setStackSize(getStackInSlot(j), max);
                }
                put(getStackInSlot(j), j, priority);
                return i;
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
                if (lockPriorities[j] > lockPriorities[i]) {
                    for (int k = 0; k < size; k++) {
                        if (getStackInSlot(k) == null && lockPriorities[k] == 0) {
                            dropSlot = k;
                            break;
                        }
                    }
                }
                if (isMultiplayer) {
                    click(dropSlot);
                }
                put(jStack, dropSlot, -1);
                return dropSlot;
            } else {
                return MOVE_OK_OLD_SLOT_EMPTY;
            }
        }
    }

    /**
     * (Multiplayer only) Click on the interface. Slower than manual swapping,
     * but works in multiplayer.
     * 
     * @param slot The targeted slot
     * @param priority Ignored
     * @param oldSlot The stacks previous spot
     * @param stack The stack that was in the slot before the operation
     * @throws Exception
     */
    public void click(int slot) throws TimeoutException {
        clickCount++;
    
        if (log.getLevel() == Const.DEBUG)
            log.info("Click on " + slot);
    
        // After clicking, we'll need to wait for server answer before
        // continuing.
        // We'll do this by listening to any change in the slot, but this
        // implies we
        // check first if the click will indeed produce a change.
        boolean uselessClick = false;
        ItemStack stackInSlot = (getStackInSlot(slot) != null) ? copy(getStackInSlot(slot)) : null;
        ItemStack stackInHand = getHoldStack();
    
        // Useless if empty stacks
        if (stackInHand == null && stackInSlot == null)
            uselessClick = true;
        // Useless if destination stack is full
        else if (stackInHand != null && stackInSlot != null && areSameItem(stackInHand, stackInSlot)
                && getStackSize(stackInSlot) == getMaxStackSize(stackInSlot)) {
            uselessClick = true;
        }
    
        // Click!
        clickInventory(getPlayerController(), getWindowId(container), // Select container
                slot + offset, // Targeted slot
                0, // Left-click
                false, // Shift not held
                entityPlayer);
    
        // Wait for inventory update
        if (!uselessClick) {
            int pollingTime = 0;
            while (areItemStacksEqual(getStackInSlot(slot), stackInSlot) && pollingTime < Const.POLLING_TIMEOUT) {
                InventoryAlgorithms.trySleep(Const.POLLING_DELAY);
                pollingTime += Const.POLLING_DELAY;
            }
            if (pollingTime >= Const.POLLING_TIMEOUT) {
                log.warning("Click timeout");
            }
            timeSpentWaiting += pollingTime;
        }
    
        // Freeze protection (to protect from infinite clicking in SMP
        // due to a mod bug, or the server not responding)
        if (timeSpentWaiting > Const.SORTING_TIMEOUT) {
            throw new TimeoutException("Timeout");
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
     * 
     * @return -1 if there is no room to put the item, or the hand is not holding anything.
     * @throws Exception
     */
    public int putHoldItemDown() throws TimeoutException {
        ItemStack holdStack = getHoldStack();
        if (holdStack != null) {
            // Try to find an unlocked slot first, to avoid
            // impacting too much the sorting
            for (int step = 1; step <= 2; step++) {
                for (int i = size - 1; i >= 0; i--) {
                    if (getStackInSlot(i) == null && (lockPriorities[i] == 0 && !frozenSlots[i]) || step == 2) {
                        if (isMultiplayer) {
                            click(i);
                        } else {
                            putStackInSlot(i, holdStack);
                            setHoldStack(null);
                        }
                        return i;
                    }
                }
            }
            return -1;
        }
        return -1;
    }

    public boolean canBeMerged(int i, int j) {
        if (i == j || getStackInSlot(i) == null || getStackInSlot(j) == null) {
            return false;
        }
        return areSameItem(getStackInSlot(i), getStackInSlot(j)) && getStackSize(getStackInSlot(j)) < getMaxStackSize(getStackInSlot(j));
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
        return getItemID(stack1) == getItemID(stack2) && (getItemDamage(stack1) == getItemDamage(stack2) // same
                                                                                                         // item
                                                                                                         // variant
                || getMaxStackSize(stack1) == 1); // except if unstackable
    }

    public boolean isOrderedBefore(int i, int j) {

        if (getStackInSlot(j) == null) {
            return true;
        } else if (getStackInSlot(i) == null || keywordOrder[i] == -1) {
            return false;
        } else {
            if (keywordOrder[i] == keywordOrder[j]) {
                // Items of same keyword orders can have different IDs,
                // in the case of categories defined by a range of IDs
                if (getItemID(getStackInSlot(i)) == getItemID(getStackInSlot(j))) {
                    if (getStackSize(getStackInSlot(i)) == getStackSize(getStackInSlot(j))) {
                        // Highest damage first for tools, else lowest damage.
                        // No tool ordering for same ID in multiplayer (cannot
                        // swap directly)
                        return (getItemDamage(getStackInSlot(i)) > getItemDamage(getStackInSlot(j)) && getMaxStackSize(getStackInSlot(j)) == 1 && !isMultiplayer)
                                || (getItemDamage(getStackInSlot(i)) < getItemDamage(getStackInSlot(j)) && getMaxStackSize(getStackInSlot(j)) > 1);
                    } else {
                        return getStackSize(getStackInSlot(i)) > getStackSize(getStackInSlot(j));
                    }
                } else {
                    return getItemID(getStackInSlot(i)) > getItemID(getStackInSlot(j));
                }
            } else {
                return keywordOrder[i] < keywordOrder[j];
            }
        }
    }

    public int getClickCount() {
        if (isMultiplayer) {
            return clickCount;
        } else
            return -1;
    }

    public ItemStack getItemStack(int i) {
        return getStackInSlot(i);
    }

    public int getLockPriority(int i) {
        return lockPriorities[i];
    }

    public int getSize() {
        return size;
    }

    private int getItemOrder(int itemID, int itemDamage) {
        List<ItemTreeItem> items = tree.getItems(itemID, itemDamage);
        return (items != null && items.size() > 0) ? items.get(0).getOrder() : Integer.MAX_VALUE;
    }

    /**
     * SP: Removes the stack from the given slot.
     * SMP: Registers the action without actually doing it.
     * 
     * @param slot
     * @return The removed stack
     */
    private ItemStack remove(int slot) {
        ItemStack removed = getStackInSlot(slot);
        if (log.getLevel() == Const.DEBUG) {
            try {
                log.info("Removed: " + tree.getItems(getItemID(removed), getItemDamage(removed)).get(0) + " from " + slot);
            } catch (NullPointerException e) {
                log.info("Removed: null from " + slot);
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
     * 
     * @param stack
     * @param slot
     * @param priority
     */
    private void put(ItemStack stack, int slot, int priority) {
        if (log.getLevel() == Const.DEBUG) {
            try {
                log.info("Put: " + tree.getItems(getItemID(stack), getItemDamage(stack)).get(0) + " in " + slot);
            } catch (NullPointerException e) {
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
