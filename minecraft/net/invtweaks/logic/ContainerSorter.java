package net.invtweaks.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import net.invtweaks.Const;
import net.invtweaks.config.InvTweaksConfig;
import net.invtweaks.config.InventoryConfigRule;
import net.invtweaks.framework.ContainerManager.ContainerSection;
import net.invtweaks.framework.ContainerSectionManager;
import net.invtweaks.framework.Obfuscation;
import net.invtweaks.tree.ItemTree;
import net.invtweaks.tree.ItemTreeItem;
import net.minecraft.client.Minecraft;
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
public class ContainerSorter extends Obfuscation {

    
    private static final Logger log = Logger.getLogger("InvTweaks");

    public static final boolean STACK_NOT_EMPTIED = true;
    public static final boolean STACK_EMPTIED = false;

    private static int[] DEFAULT_LOCK_PRIORITIES = null;
    private static boolean[] DEFAULT_FROZEN_SLOTS = null;
    private static final int MAX_CONTAINER_SIZE = 100;
    
    public static final int ALGORITHM_DEFAULT = 0;
    public static final int ALGORITHM_VERTICAL = 1;
    public static final int ALGORITHM_HORIZONTAL = 2;
    public static final int ALGORITHM_INVENTORY = 3;

    private ContainerSectionManager containerMgr;
    private int algorithm;
    private int size;
    
    private ItemTree tree;
    private Vector<InventoryConfigRule> rules;
    private int[] rulePriority;
    private int[] keywordOrder;
    private int[] lockPriorities;
    private boolean[] frozenSlots;

    private boolean isMultiplayer;

    public ContainerSorter(Minecraft mc, InvTweaksConfig config,
            ContainerSection section, int algorithm) throws Exception {
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

        this.containerMgr = new ContainerSectionManager(mc, section);
        
        this.rules = config.getRules();
        this.tree = config.getTree();
        if (section == ContainerSection.INVENTORY) {
            this.lockPriorities = config.getLockPriorities();
            this.frozenSlots = config.getFrozenSlots();
            this.algorithm = ALGORITHM_INVENTORY;
        } else {
            this.lockPriorities = DEFAULT_LOCK_PRIORITIES;
            this.frozenSlots = DEFAULT_FROZEN_SLOTS;
            this.algorithm = algorithm;
            if (algorithm != ALGORITHM_DEFAULT) {
                computeLineSortingRules(Const.INVENTORY_ROW_SIZE,
                        algorithm == ALGORITHM_HORIZONTAL);
            }
        }

        this.size = containerMgr.getSectionSize();
        this.rulePriority = new int[size];
        this.keywordOrder = new int[size];
        for (int i = 0; i < size; i++) {
            this.rulePriority[i] = -1;
            ItemStack stack = containerMgr.getItemStack(i);
            if (stack != null) {
                this.keywordOrder[i] = getItemOrder(getItemID(stack), getItemDamage(stack));
            } else {
                this.keywordOrder[i] = -1;
            }
        }
        
        this.isMultiplayer = isMultiplayerWorld();
    }
    
    public void sort() throws TimeoutException {
        
        // Do nothing if the inventory is closed
        // if (!mc.hrrentScreen instanceof GuiContainer)
        //      return;
        
        long timer = System.nanoTime();

        //// Empty hand (needed in SMP)
        if (isMultiplayerWorld()) {
            putHoldItemDown();
        }
        
        if (algorithm != ALGORITHM_DEFAULT) {
            
            if (algorithm == ALGORITHM_INVENTORY) {
                
                //// Merge stacks to fill the ones in locked slots
                log.info("Merging stacks.");
                for (int i = size-1; i >= 0; i--) {
                    ItemStack from = containerMgr.getItemStack(i);
                    if (from != null) {
                        int j = 0;
                        for (Integer lockPriority : lockPriorities) {
                            if (lockPriority > 0) {
                                ItemStack to = containerMgr.getItemStack(j);
                                if (to != null && from.isItemEqual(to)) {
                                    move(i, j, Integer.MAX_VALUE);
                                    markAsNotMoved(j);
                                    if (containerMgr.getItemStack(i) == null) {
                                        break;
                                    }
                                }
                            }
                            j++;
                        }
                    }
                }
                
            }
            
            //// Apply rules
            log.info("Applying rules.");
            
            // Sorts rule by rule, themselves being already sorted by decreasing priority
            Iterator<InventoryConfigRule> rulesIt = rules.iterator();
            while (rulesIt.hasNext()) {
                
                InventoryConfigRule rule = rulesIt.next();
                int rulePriority = rule.getPriority();
    
                if (log.getLevel() == Const.DEBUG)
                    log.info("Rule : "+rule.getKeyword()+"("+rulePriority+")");
    
                // For every item in the inventory
                for (int i = 0; i < size; i++) {
                    ItemStack from = containerMgr.getItemStack(i);

                    // If the rule is strong enough to move the item and it matches the item
                    if (hasToBeMoved(i) && lockPriorities[i] < rulePriority) {
                        List<ItemTreeItem> fromItems = tree.getItems(
                                getItemID(from), getItemDamage(from));
                        if (tree.matches(fromItems, rule.getKeyword())) {
                            
                            // Test preffered slots
                            int[] preferredSlots = rule.getPreferredSlots();
                            int stackToMove = i;
                            for (int j = 0; j < preferredSlots.length; j++) {
                                int k = preferredSlots[j];
                                int moveResult = move(stackToMove, k, rulePriority);
                                if (moveResult != -1) {
                                    if (containerMgr.getItemStack(stackToMove) == null ||
                                            moveResult == k) {
                                        break;
                                    }
                                    else {
                                        from = containerMgr.getItemStack(moveResult);
                                        fromItems = tree.getItems(getItemID(from), getItemDamage(from));
                                        if (!tree.matches(fromItems, rule.getKeyword())) {
                                            break;
                                        }
                                        else {
                                            stackToMove = moveResult;
                                            j = -1;
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
            
            for (int i = 0; i < size; i++) {
                if (hasToBeMoved(i) && lockPriorities[i] > 0) {
                    markAsMoved(i, 1);
                }
            }

        }
        
        //// Sort remaining
        defaultSorting();

        if (log.getLevel() == Const.DEBUG) {
            timer = System.nanoTime()-timer;
            log.info("Sorting done in " + timer + "ns");
        }
        
    }

    /**
     * If an item is in hand (= attached to the cursor), puts it down.
     * 
     * @return -1 if there is no room to put the item, or the hand is not holding anything.
     * @throws Exception
     */
    private int putHoldItemDown() throws TimeoutException {
        ItemStack holdStack = getHoldStack();
        if (holdStack != null) {
            // Try to find an unlocked slot first, to avoid
            // impacting too much the sorting
            for (int step = 1; step <= 2; step++) {
                for (int i = size - 1; i >= 0; i--) {
                    if (containerMgr.getItemStack(i) == null
                            && (lockPriorities[i] == 0 && !frozenSlots[i])
                            || step == 2) {
                        containerMgr.leftClick(i);
                        return i;
                    }
                }
            }
            return -1;
        }
        return -1;
    }

    private void defaultSorting() throws TimeoutException {
    
        log.info("Default sorting.");
        
        Vector<Integer> remaining = new Vector<Integer>(), nextRemaining = new Vector<Integer>();
        for (int i = 0; i < size; i++) {
            if (hasToBeMoved(i)) {
                remaining.add(i);
                nextRemaining.add(i);
            }
        }
        
        int iterations = 0;
        while (remaining.size() > 0 && iterations++ < 50) {
            for (int i : remaining) {
                if (hasToBeMoved(i)) {
                    for (int j = 0; j < size; j++) {
                        if (move(i, j, 1) != -1) {
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

    /**
     * Tries to move a stack from i to j, and swaps them if j is already
     * occupied but i is of greater priority (even if they are of same ID).
     * 
     * @param i from slot
     * @param j to slot
     * @param priority  The rule priority. Use 1 if the stack was not moved using a rule.
     * @return -1 if it failed,
     *      j if the stacks were merged into one,
     *      n if the j stack has been moved to the n slot.
     * @throws TimeoutException
     */
    private int move(int i, int j, int priority) throws TimeoutException {

        ItemStack from = containerMgr.getItemStack(i);
        ItemStack to = containerMgr.getItemStack(j);
        
        if (from == null || frozenSlots[j] || frozenSlots[i]) {
            return -1;
        }

        if (lockPriorities[i] <= priority) {

            if (i == j) {
                markAsMoved(i, priority);
                return i;
            }
            
            // Move to empty slot
            if (to == null && lockPriorities[j] <= priority && !frozenSlots[j]) {
                containerMgr.move(i, j); 
                return j;
            }

            // Try to swap/merge
            else if (to != null) {
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
                if (canBeSwapped || from.isItemEqual(to)) {
                    containerMgr.move(i, j); 
                    
                    int dropSlot = i;
                    if (lockPriorities[j] > lockPriorities[i]) {
                        for (int k = 0; k < size; k++) {
                            if (containerMgr.getItemStack(k) == null
                                    && lockPriorities[k] == 0) {
                                dropSlot = k;
                                break;
                            }
                        }
                    }
                    if (i != dropSlot) {
                        containerMgr.move(i, dropSlot);
                    }
                    
                    return dropSlot;
                }
            }

        }

        return -1;
    }

    private void markAsMoved(int i, int priority) {
        rulePriority[i] = priority;
    }

    private void markAsNotMoved(int i) {
        rulePriority[i] = -1;
    }

    private boolean hasToBeMoved(int slot) {
        return containerMgr.getItemStack(slot) != null
                && rulePriority[slot] == -1;
    }

    private boolean isOrderedBefore(int i, int j) {

        ItemStack iStack = containerMgr.getItemStack(i),
                jStack = containerMgr.getItemStack(j);
        
        if (jStack == null) {
            return true;
        } else if (iStack == null || keywordOrder[i] == -1) {
            return false;
        } else {
            if (keywordOrder[j] == keywordOrder[j]) {
                // Items of same keyword orders can have different IDs,
                // in the case of categories defined by a range of IDs
                if (getItemID(iStack) == getItemID(jStack)) {
                    if (getStackSize(iStack) == getStackSize(jStack)) {
                        // Highest damage first for tools, else lowest damage.
                        // No tool ordering for same ID in multiplayer (cannot
                        // swap directly)
                        return (getItemDamage(iStack) > getItemDamage(jStack) && getMaxStackSize(jStack) == 1 && !isMultiplayer)
                                || (getItemDamage(iStack) < getItemDamage(jStack) && getMaxStackSize(jStack) > 1);
                    } else {
                        return getStackSize(iStack) > getStackSize(jStack);
                    }
                } else {
                    return getItemID(iStack) > getItemID(jStack);
                }
            } else {
                return keywordOrder[i] < keywordOrder[j];
            }
        }
    }

    private int getItemOrder(int itemID, int itemDamage) {
        List<ItemTreeItem> items = tree.getItems(itemID, itemDamage);
        return (items != null && items.size() > 0)
                ? items.get(0).getOrder()
                : Integer.MAX_VALUE;
    }
///TODO
    /**
     * SP: Removes the stack from the given slot.
     * SMP: Registers the action without actually doing it.
     * 
     * @param slot
     * @return The removed stack
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
     */

    /**
     * SP: Puts a stack in the given slot. WARNING: Any existing stack will be overriden!
     * SMP: Registers the action without actually doing it.
     * 
     * @param stack
     * @param slot
     * @param priority
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
     */
    
    private Vector<InventoryConfigRule> computeLineSortingRules(
            int rowSize, boolean horizontal) {
        
        Map<ItemTreeItem, Integer> stats = computeContainerStats();        
        List<ItemTreeItem> itemOrder = new ArrayList<ItemTreeItem>();

        int distinctItems = stats.size();
        int columnSize = getContainerColumnSize(rowSize);
        int spaceWidth;
        int spaceHeight;
        int availableSlots = size;
        int remainingStacks = 0;
        for (Integer stacks : stats.values()) {
            remainingStacks += stacks; 
        }
        
        // No need to compute rules for an empty chest
        if (distinctItems == 0)
            return rules;
        
        // (Partially) sort stats by decreasing item stack count
        List<ItemTreeItem> unorderedItems = new ArrayList<ItemTreeItem>(stats.keySet());
        boolean hasStacksToOrderFirst = true;
        while (hasStacksToOrderFirst) {
            hasStacksToOrderFirst = false;
            for (ItemTreeItem item : unorderedItems) {
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
        Iterator<ItemTreeItem> it = itemOrder.iterator();
        while (it.hasNext()) {
            
            ItemTreeItem item = it.next();
            
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
            rules.add(new InventoryConfigRule(tree, 
                    constraint, item.getName(), size, rowSize));
            
            // Check if ther's still room for more rules
            availableSlots -= thisSpaceHeight*thisSpaceWidth;
            remainingStacks -= stats.get(item);
            if (availableSlots >= remainingStacks) {
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
            else {
                break;
            }
        }
        
        String defaultRule;
        if (horizontal) {
            defaultRule = maxRow + "1-a" + maxColumn;
        }
        else {
            defaultRule = "a" + maxColumn + "-" + maxRow + "1v";
        }
        rules.add(new InventoryConfigRule(tree, defaultRule, 
                tree.getRootCategory().getName(), size, rowSize));
        
        return rules;
        
    }
    
    private Map<ItemTreeItem, Integer> computeContainerStats() {
        Map<ItemTreeItem, Integer> stats = new HashMap<ItemTreeItem, Integer>();
        Map<Integer, ItemTreeItem> itemSearch = new HashMap<Integer, ItemTreeItem>();
 
        for (int i = 0; i < size; i++) {
            ItemStack stack = containerMgr.getItemStack(i);
            if (stack != null) {
                int itemSearchKey = getItemID(stack)*100000 + 
                        ((getMaxStackSize(stack) != 1) ? getItemDamage(stack) : 0);
                ItemTreeItem item = itemSearch.get(itemSearchKey);
                if (item == null) {
                    item = tree.getItems(getItemID(stack),
                            getItemDamage(stack)).get(0);
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
    
    private int getContainerColumnSize(int rowSize) {
        return size / rowSize;
    }
}
