import invtweaks.InvTweaksConst;
import invtweaks.InvTweaksItemTree;
import invtweaks.InvTweaksItemTreeItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;


import net.minecraft.client.Minecraft;

/**
 * Core of the sorting behaviour. Allows to move items in a container
 * (inventory or chest) with respect to the mod's configuration.
 *
 * @author Jimeo Wan
 *
 */
public class InvTweaksHandlerSorting extends InvTweaksObfuscation {

    private static final Logger log = Logger.getLogger("InvTweaks");

    public static final boolean STACK_NOT_EMPTIED = true;
    public static final boolean STACK_EMPTIED = false;

    private static int[] DEFAULT_LOCK_PRIORITIES = null;
    private static boolean[] DEFAULT_FROZEN_SLOTS = null;
    private static final int MAX_CONTAINER_SIZE = 999;

    public static final int ALGORITHM_DEFAULT = 0;
    public static final int ALGORITHM_VERTICAL = 1;
    public static final int ALGORITHM_HORIZONTAL = 2;
    public static final int ALGORITHM_INVENTORY = 3;
    public static final int ALGORITHM_EVEN_STACKS = 4;

    private InvTweaksContainerSectionManager containerMgr;
    private int algorithm;
    private int size;
    private boolean sortArmorParts;
    private int clickDelay;

    private InvTweaksItemTree tree;
    private Vector<InvTweaksConfigSortingRule> rules;
    private int[] rulePriority;
    private int[] keywordOrder;
    private int[] lockPriorities;
    private boolean[] frozenSlots;

    public InvTweaksHandlerSorting(Minecraft mc, InvTweaksConfig config,
            InvTweaksContainerSection section, int algorithm, int rowSize) throws Exception {
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

        this.clickDelay = config.getClickDelay();
        this.containerMgr = new InvTweaksContainerSectionManager(mc, section);
        this.containerMgr.setClickDelay(this.clickDelay);
        this.size = containerMgr.getSize();
        this.sortArmorParts = config.getProperty(InvTweaksConfig.PROP_ENABLE_AUTO_EQUIP_ARMOR).equals(InvTweaksConfig.VALUE_TRUE)
                && !isGuiInventoryCreative(getCurrentScreen()); // FIXME Armor parts disappear when sorting in creative mode while holding an item

        this.rules = config.getRules();
        this.tree = config.getTree();
        if (section == InvTweaksContainerSection.INVENTORY) {
            this.lockPriorities = config.getLockPriorities();
            this.frozenSlots = config.getFrozenSlots();
            this.algorithm = ALGORITHM_INVENTORY;
        } else {
            this.lockPriorities = DEFAULT_LOCK_PRIORITIES;
            this.frozenSlots = DEFAULT_FROZEN_SLOTS;
            this.algorithm = algorithm;
            if (algorithm != ALGORITHM_DEFAULT) {
                computeLineSortingRules(rowSize,
                        algorithm == ALGORITHM_HORIZONTAL);
            }
        }

        this.rulePriority = new int[size];
        this.keywordOrder = new int[size];
        for (int i = 0; i < size; i++) {
            this.rulePriority[i] = -1;
            um stack = containerMgr.getItemStack(i);
            if (stack != null) {
                this.keywordOrder[i] = getItemOrder(stack);
            } else {
                this.keywordOrder[i] = -1;
            }
        }
    }

    public void sort() throws TimeoutException {

        // Do nothing if the inventory is closed
        // if (!mc.hrrentScreen instanceof GuiContainer)
        //      return;

        long timer = System.nanoTime();
        InvTweaksContainerManager globalContainer = new InvTweaksContainerManager(mc);
        globalContainer.setClickDelay(this.clickDelay);

        // Put hold item down
        if (getHeldStack() != null) {
            int emptySlot = globalContainer.getFirstEmptyIndex(InvTweaksContainerSection.INVENTORY);
            if (emptySlot != -1) {
                globalContainer.putHoldItemDown(InvTweaksContainerSection.INVENTORY, emptySlot);
            }
            else {
                return; // Not enough room to work, abort
            }
        }

        if (algorithm != ALGORITHM_DEFAULT) {

            if (algorithm == ALGORITHM_EVEN_STACKS) {
                log.info("Distributing items.");

                //item and slot counts for each unique item
                HashMap itemCounts = new HashMap();
                for(int i = 0; i < size; i++) {
                    um stack = containerMgr.getItemStack(i);
                    if(stack != null) {
                        List<Integer> item = Arrays.asList(getItemID(stack),getItemDamage(stack));
                        int[] count = (int[])itemCounts.get(item);
                        if(count == null) {
                            int[] newCount = {getStackSize(stack),1};
                            itemCounts.put(item,newCount);
                        } else {
                            count[0] += getStackSize(stack); //amount of item
                            count[1]++;                      //slots with item
                        }
                    }
                }

                //handle each unique item separately
                for(Object object:itemCounts.entrySet()) {
                    Map.Entry entry = (Map.Entry)object;
                    List<Integer> item = (List<Integer>)entry.getKey();
                    int[] count = (int[])entry.getValue();
                    int numPerSlot = count[0]/count[1];  //totalNumber/numberOfSlots

                    //skip hacked itemstacks that are larger than their max size
                    //no idea why they would be here, but may as well account for them anyway
                    if(numPerSlot <= getMaxStackSize(new um(item.get(0),1,0))) {

                        //linkedlists to store which stacks have too many/few items
                        LinkedList smallStacks = new LinkedList();
                        LinkedList largeStacks = new LinkedList();
                        for(int i = 0; i < size; i++) {
                            um stack = containerMgr.getItemStack(i);
                            if(stack != null && Arrays.asList(getItemID(stack),getItemDamage(stack)).equals(item)) {
                                int stackSize = getStackSize(stack);
                                if(stackSize > numPerSlot)
                                    largeStacks.offer(i);
                                else if(stackSize < numPerSlot)
                                    smallStacks.offer(i);
                            }
                        }

                        //move items from stacks with too many to those with too little
                        while((!smallStacks.isEmpty())) {
                            int largeIndex = (Integer)largeStacks.peek();
                            int largeSize = getStackSize(containerMgr.getItemStack(largeIndex));
                            int smallIndex = (Integer)smallStacks.peek();
                            int smallSize = getStackSize(containerMgr.getItemStack(smallIndex));
                            containerMgr.moveSome(largeIndex, smallIndex, Math.min(numPerSlot-smallSize,largeSize-numPerSlot));

                            //update stack lists
                            largeSize = getStackSize(containerMgr.getItemStack(largeIndex));
                            smallSize = getStackSize(containerMgr.getItemStack(smallIndex));
                            if(largeSize == numPerSlot)
                                largeStacks.remove();
                            if(smallSize == numPerSlot)
                                smallStacks.remove();
                        }

                        //put all leftover into one stack for easy removal
                        while(largeStacks.size() > 1) {
                            int largeIndex = (Integer)largeStacks.poll();
                            int largeSize = getStackSize(containerMgr.getItemStack(largeIndex));
                            containerMgr.moveSome(largeIndex,(Integer)largeStacks.peek(),largeSize-numPerSlot);
                        }
                    }
                }

                //mark all items as moved. (is there a better way?)
                for(int i=0;i<size;i++)
                    markAsMoved(i,1);

            } else if (algorithm == ALGORITHM_INVENTORY) {

                //// Move items out of the crafting slots
                log.info("Handling crafting slots.");
                if (globalContainer.hasSection(InvTweaksContainerSection.CRAFTING_IN)) {
                    List<sq> craftingSlots = globalContainer.getSlots(InvTweaksContainerSection.CRAFTING_IN);
                    int emptyIndex = globalContainer.getFirstEmptyIndex(InvTweaksContainerSection.INVENTORY);
                    if (emptyIndex != -1) {
                        for (sq craftingSlot : craftingSlots) {
                            if (hasStack(craftingSlot)) {
                                globalContainer.move(
                                        InvTweaksContainerSection.CRAFTING_IN,
                                        globalContainer.getSlotIndex(getSlotNumber(craftingSlot)),
                                        InvTweaksContainerSection.INVENTORY,
                                        emptyIndex);
                                emptyIndex = globalContainer.getFirstEmptyIndex(InvTweaksContainerSection.INVENTORY);
                                if(emptyIndex == -1) {
                                    break;
                                }
                            }
                        }
                    }
                }


                //// Merge stacks to fill the ones in locked slots
                //// + Move armor parts to the armor slots

                log.info("Merging stacks.");
                for (int i = size - 1; i >= 0; i--) {
                    um from = containerMgr.getItemStack(i);
                    if (from != null) {

                        // Move armor parts
	                    uk fromItem = getItem(from);
	                    if (isDamageable(fromItem)) {
	                        if (sortArmorParts) {
	                             if (isItemArmor(fromItem)) {
	                            	 st fromItemArmor = asItemArmor(fromItem);
	                                 if (globalContainer.hasSection(InvTweaksContainerSection.ARMOR)) {
	                                     List<sq> armorSlots = globalContainer.getSlots(InvTweaksContainerSection.ARMOR);
	                                     for (sq slot : armorSlots) {
	                                    	boolean move = false;
	                                    	if (!hasStack(slot)) {
	                                    		move = true;
	                                    	}
	                                    	else {
	                                    		int armorLevel = getArmorLevel(asItemArmor(getItem(getStack(slot))));
	                                    		if (armorLevel < getArmorLevel(fromItemArmor)
	                                    				|| (armorLevel == getArmorLevel(fromItemArmor)
	                                    						&& getItemDamage(getStack(slot)) < getItemDamage(from))) {
	                                    			move = true;
	                                    		}
	                                    	}
	                                        if (areSlotAndStackCompatible(slot, from) && move) {
	                                            globalContainer.move(InvTweaksContainerSection.INVENTORY, i, InvTweaksContainerSection.ARMOR,
	                                                    globalContainer.getSlotIndex(getSlotNumber(slot)));
	                                        }
	                                    }
	                                 }
	                             }
	                        }
                    	}

                        // Stackable objects are never damageable
                        else {
                            int j = 0;
                            for (Integer lockPriority : lockPriorities) {
                                if (lockPriority > 0) {
                                    um to = containerMgr.getItemStack(j);
                                    if (to != null && areItemsEqual(from, to)) {
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

            }

            //// Apply rules
            log.info("Applying rules.");

            // Sorts rule by rule, themselves being already sorted by decreasing priority
            Iterator<InvTweaksConfigSortingRule> rulesIt = rules.iterator();
            while (rulesIt.hasNext()) {

                InvTweaksConfigSortingRule rule = rulesIt.next();
                int rulePriority = rule.getPriority();

                if (log.getLevel() == InvTweaksConst.DEBUG)
                    log.info("Rule : "+rule.getKeyword()+"("+rulePriority+")");

                // For every item in the inventory
                for (int i = 0; i < size; i++) {
                    um from = containerMgr.getItemStack(i);

                    // If the rule is strong enough to move the item and it matches the item, move it
                    if (hasToBeMoved(i) && lockPriorities[i] < rulePriority) {
                        List<InvTweaksItemTreeItem> fromItems = tree.getItems(
                                getItemID(from), getItemDamage(from));
                        if (tree.matches(fromItems, rule.getKeyword())) {

                            // Test preffered slots
                            int[] preferredSlots = rule.getPreferredSlots();
                            int stackToMove = i;
                            for (int j = 0; j < preferredSlots.length; j++) {
                                int k = preferredSlots[j];

                                // Move the stack!
                                int moveResult = move(stackToMove, k, rulePriority);
                                if (moveResult != -1) {
                                    if (moveResult == k) {
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

        if (log.getLevel() == InvTweaksConst.DEBUG) {
            timer = System.nanoTime()-timer;
            log.info("Sorting done in " + timer + "ns");
        }

        //// Put hold item down, just in case
        if (getHeldStack() != null) {
            int emptySlot = globalContainer.getFirstEmptyIndex(InvTweaksContainerSection.INVENTORY);
            if (emptySlot != -1) {
                globalContainer.putHoldItemDown(InvTweaksContainerSection.INVENTORY, emptySlot);
            }
        }
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
        if (iterations == 100) {
            log.warning("Sorting takes too long, aborting.");
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

        um from = containerMgr.getItemStack(i), to = containerMgr.getItemStack(j);

        if (from == null || frozenSlots[j] || frozenSlots[i]) {
            return -1;
        }

        //log.info("Moving " + i + " (" + from + ") to " + j + " (" + to + ") ");

        if (lockPriorities[i] <= priority) {

            if (i == j) {
                markAsMoved(i, priority);
                return j;
            }

            // Move to empty slot
            if (to == null && lockPriorities[j] <= priority && !frozenSlots[j]) {
                rulePriority[i] = -1;
                keywordOrder[i] = -1;
                rulePriority[j] = priority;
                keywordOrder[j] = getItemOrder(from);
                containerMgr.move(i, j);
                return j;
            }

            // Try to swap/merge
            else if (to != null) {

                boolean canBeSwappedOrMerged = false;

                // Can be swapped?
                if (lockPriorities[j] <= priority) {
                    if (rulePriority[j] < priority) {
                        canBeSwappedOrMerged = true;
                    } else if (rulePriority[j] == priority) {
                        if (isOrderedBefore(i, j)) {
                            canBeSwappedOrMerged = true;
                        }
                    }
                }

                if (!hasDataTags(from) && !hasDataTags(to) && areItemsEqual(from, to)) {
                    // Can be merged?
                    if (getStackSize(to) < getMaxStackSize(to)) {
                        canBeSwappedOrMerged = true;
                    }
                    // Safety check (for TooManyItems)
                    else if (getStackSize(from) > getMaxStackSize(from)) {
                        canBeSwappedOrMerged = false;
                    }
                }

                if (canBeSwappedOrMerged) {

                    keywordOrder[j] = keywordOrder[i];
                    rulePriority[j] = priority;
                    rulePriority[i] = -1;
                    containerMgr.move(i, j);

                    um remains = containerMgr.getItemStack(i);

                    if (remains != null) {
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
                        if (dropSlot != i) {
                            containerMgr.move(i, dropSlot);
                        }
                        rulePriority[dropSlot] = -1;
                        keywordOrder[dropSlot] = getItemOrder(remains);
                        return dropSlot;
                    }
                    else {
                        return j;
                    }
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

        um iStack = containerMgr.getItemStack(i), jStack = containerMgr.getItemStack(j);

        if (jStack == null) {
            return true;
        } else if (iStack == null || keywordOrder[i] == -1) {
            return false;
        } else {
            if (keywordOrder[i] == keywordOrder[j]) {
                // Items of same keyword orders can have different IDs,
                // in the case of categories defined by a range of IDs
                if (getItemID(iStack) == getItemID(jStack)) {
                    if (getItemDamage(iStack) != getItemDamage(jStack)) {
                        if (isItemStackDamageable(iStack)) {
                            return getItemDamage(iStack) > getItemDamage(jStack);
                        }
                        else {
                            return getItemDamage(iStack) < getItemDamage(jStack);
                        }
                    }
                    else {
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

    private int getItemOrder(um itemStack) {
        List<InvTweaksItemTreeItem> items = tree.getItems(
                getItemID(itemStack), getItemDamage(itemStack));
        return (items != null && items.size() > 0)
                ? items.get(0).getOrder()
                : Integer.MAX_VALUE;
    }

    private void computeLineSortingRules(int rowSize, boolean horizontal) {

        rules = new Vector<InvTweaksConfigSortingRule>();


        Map<InvTweaksItemTreeItem, Integer> stats = computeContainerStats();
        List<InvTweaksItemTreeItem> itemOrder = new ArrayList<InvTweaksItemTreeItem>();

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
            return;

        // (Partially) sort stats by decreasing item stack count
        List<InvTweaksItemTreeItem> unorderedItems = new ArrayList<InvTweaksItemTreeItem>(stats.keySet());
        boolean hasStacksToOrderFirst = true;
        while (hasStacksToOrderFirst) {
            hasStacksToOrderFirst = false;
            for (InvTweaksItemTreeItem item : unorderedItems) {
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
        Iterator<InvTweaksItemTreeItem> it = itemOrder.iterator();
        while (it.hasNext()) {

            InvTweaksItemTreeItem item = it.next();

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
            rules.add(new InvTweaksConfigSortingRule(tree, constraint, item.getName(), size, rowSize));

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
        rules.add(new InvTweaksConfigSortingRule(tree, defaultRule,
                tree.getRootCategory().getName(), size, rowSize));

    }

    private Map<InvTweaksItemTreeItem, Integer> computeContainerStats() {
        Map<InvTweaksItemTreeItem, Integer> stats = new HashMap<InvTweaksItemTreeItem, Integer>();
        Map<Integer, InvTweaksItemTreeItem> itemSearch = new HashMap<Integer, InvTweaksItemTreeItem>();

        for (int i = 0; i < size; i++) {
            um stack = containerMgr.getItemStack(i);
            if (stack != null) {
                int itemSearchKey = getItemID(stack)*100000 +
                        ((getMaxStackSize(stack) != 1) ? getItemDamage(stack) : 0);
                InvTweaksItemTreeItem item = itemSearch.get(itemSearchKey);
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
