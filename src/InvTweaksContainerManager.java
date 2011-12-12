
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import net.minecraft.client.Minecraft;

/**
 * Allows to perform various operations on the inventory
 * and/or containers. Works in both single and multiplayer.
 * 
 * @author Jimeo Wan
 *
 */
public class InvTweaksContainerManager extends InvTweaksObfuscation {
	
    // TODO: Throw errors when the container isn't available anymore

    public static final int DROP_SLOT = -999;
    public static final int INVENTORY_SIZE = 36;
    public static final int HOTBAR_SIZE = 9;
    public static final int ACTION_TIMEOUT = 500;
    public static final int POLLING_DELAY = 3;
    
    private pj container;
    private Map<InvTweaksContainerSection, List<vv>> slotRefs 
            = new HashMap<InvTweaksContainerSection, List<vv>>();
    
    
    /**
     * Creates an container manager linked to the currently available container:
     * - If a container GUI is open, the manager gives access to this container contents.
     * - If no GUI is open, the manager works as if the player's inventory was open. 
     * @param mc Minecraft
     */
    @SuppressWarnings({"unchecked"})
    public InvTweaksContainerManager(Minecraft mc) {
        super(mc);
        
        xe currentScreen = getCurrentScreen();
        if (isGuiContainer(currentScreen)) {
            this.container = getContainer((mg) currentScreen);
        }
        else {
            this.container = getPlayerContainer();
        }
        
        List<vv> slots = (List<vv>) getSlots(container);
        int size = slots.size();
        boolean guiWithInventory = true;

        // Inventory: 4 crafting slots, then 4 armor slots, then inventory
        if (isContainerPlayer(container)) {
            slotRefs.put(InvTweaksContainerSection.CRAFTING_OUT, slots.subList(0, 1));
            slotRefs.put(InvTweaksContainerSection.CRAFTING_IN, slots.subList(1, 5));
            slotRefs.put(InvTweaksContainerSection.ARMOR, slots.subList(5, 9));
        }
        
        else if (isContainerWorkbench(container)) {
            slotRefs.put(InvTweaksContainerSection.CRAFTING_OUT, slots.subList(0, 1));
            slotRefs.put(InvTweaksContainerSection.CRAFTING_IN, slots.subList(1, 10));
        }
        
        // Chest/Dispenser
        else if (isContainerChest(container)
                || isContainerDispenser(container)) {
            slotRefs.put(InvTweaksContainerSection.CHEST, slots.subList(0, size-INVENTORY_SIZE));
        }
        
        // Furnace
        else if (isContainerFurnace(container)) {
            slotRefs.put(InvTweaksContainerSection.FURNACE_IN, slots.subList(0, 1));
            slotRefs.put(InvTweaksContainerSection.FURNACE_FUEL, slots.subList(1, 2));
            slotRefs.put(InvTweaksContainerSection.FURNACE_OUT, slots.subList(2, 3));
        }

        // Workbench
        else if (isContainerWorkbench(container)) {
            slotRefs.put(InvTweaksContainerSection.CRAFTING_OUT, slots.subList(0, 1));
            slotRefs.put(InvTweaksContainerSection.CRAFTING_IN, slots.subList(1, 10));
        }

        // Enchantment table
        else if (isContainerEnchantmentTable(container)) {
            slotRefs.put(InvTweaksContainerSection.ENCHANTMENT, slots.subList(0, 1));
        }
        
        // Brewing stand
        else if (isContainerBrewingStand(container)) {
            slotRefs.put(InvTweaksContainerSection.BREWING_BOTTLES, slots.subList(0, 3));
            slotRefs.put(InvTweaksContainerSection.BREWING_INGREDIENT, slots.subList(3, 4));
        }
        
        // Unknown = chest
        else {
            if (size >= INVENTORY_SIZE) {
                // Assuming the container ends with the inventory, just like all vanilla containers.
                slotRefs.put(InvTweaksContainerSection.CHEST, slots.subList(0, size-INVENTORY_SIZE));
            }
            else {
                guiWithInventory = false;
                slotRefs.put(InvTweaksContainerSection.CHEST, slots.subList(0, size));
            }
        }

        if (guiWithInventory) {
            slotRefs.put(InvTweaksContainerSection.INVENTORY, slots.subList(size-INVENTORY_SIZE, size));
            slotRefs.put(InvTweaksContainerSection.INVENTORY_NOT_HOTBAR, slots.subList(size-INVENTORY_SIZE, size-HOTBAR_SIZE));
            slotRefs.put(InvTweaksContainerSection.INVENTORY_HOTBAR, slots.subList(size-HOTBAR_SIZE, size));
        }
        
    }
    
    /**
     * Moves a stack from source to destination, adapting the behavior 
     * according to the context:
     * - If destination is empty, the source stack is moved.
     * - If the items can be merged, as much items are possible are put
     *   in the destination, and the eventual remains go back to the source.
     * - If the items cannot be merged, they are swapped.
     * @param srcSection The source section
     * @param srcIndex The destination slot
     * @param destSection The destination section
     * @param destIndex The destination slot
     * @return false if the source slot is empty or the player is
     * holding an item that couln't be put down.
     * @throws TimeoutException 
     */
	public boolean move(InvTweaksContainerSection srcSection, int srcIndex,
            InvTweaksContainerSection destSection, int destIndex) throws TimeoutException {
	    
	    dk srcStack = getItemStack(srcSection, srcIndex);
	    dk destStack = getItemStack(destSection, destIndex);
	    
        if (srcStack == null) {
            return false;
        }
        else if (srcSection == destSection && srcIndex == destIndex) {
            return true;
        }

        // Put hold item down
        if (getHoldStack() != null) {
            int firstEmptyIndex = getFirstEmptyIndex(InvTweaksContainerSection.INVENTORY);
            if (firstEmptyIndex != -1) {
                leftClick(InvTweaksContainerSection.INVENTORY, firstEmptyIndex);
            }
            else {
                return false;
            }
        }

        // Use intermediate slot if we have to swap tools, maps, etc.
        if (destStack != null
                && getItemID(srcStack) == getItemID(destStack)
                && getMaxStackSize(srcStack) == 1) {
            int intermediateSlot = getFirstEmptyUsableSlotNumber();
            InvTweaksContainerSection intermediateSection = getSlotSection(intermediateSlot);
            int intermediateIndex = getSlotIndex(intermediateSlot);
            if (intermediateIndex != -1) {
                // Step 1/3: Dest > Int
                leftClick(destSection, destIndex);
                leftClick(intermediateSection, intermediateIndex);
                // Step 2/3: Src > Dest
                leftClick(srcSection, srcIndex);
                leftClick(destSection, destIndex);
                // Step 3/3: Int > Src
                leftClick(intermediateSection, intermediateIndex);
                leftClick(srcSection, srcIndex);
            }
            else {
                return false;
            }
        }
        
        // Normal move
        else {
            leftClick(srcSection, srcIndex);
            leftClick(destSection, destIndex);
            if (getHoldStack() != null) {
                // FIXME What if we can't put the item back in the source? (for example crafting/furnace output)
                leftClick(srcSection, srcIndex);
            }
        }
        
      
        
        return true;
    }
	    
	/**
     * Moves some items from source to destination.
	 * @param srcSection The source section
	 * @param srcIndex The destination slot
     * @param destSection The destination section
     * @param destIndex The destination slot
	 * @param amount The amount of items to move. If <= 0, does nothing.
	 * If > to the source stack size, moves as much as possible from the stack size.
	 * If not all can be moved to the destination, only moves as much as possible.
	 * @return false if the destination slot is already occupied
	 * by a different item (meaning items cannot be moved to destination).
	 * @throws TimeoutException 
	 */
	public boolean moveSome(InvTweaksContainerSection srcSection, int srcIndex,
	        InvTweaksContainerSection destSection, int destIndex,
	        int amount) throws TimeoutException {

	    dk source = getItemStack(srcSection, srcIndex);
	    if (source == null || srcSection == destSection && srcIndex == destIndex) {
            return true;
        }

	    dk destination = getItemStack(srcSection, srcIndex);
        int sourceSize = getStackSize(source);
        int movedAmount = Math.min(amount, sourceSize);
	    
	    if (source != null && (destination == null
	            || areItemStacksEqual(source, destination))) {

	        leftClick(srcSection, srcIndex);
	        for (int i = 0; i < movedAmount; i++) {
	            rightClick(destSection, destIndex);
	        }
	        if (movedAmount < sourceSize) {
	            leftClick(srcSection, srcIndex);
	        }
	        return true;
	    }
	    else {
	        return false;
	    }
	    
	}

    public boolean drop(InvTweaksContainerSection srcSection, int srcIndex) throws TimeoutException {
        return move(srcSection, srcIndex, null, DROP_SLOT);
    }
    
    public boolean dropSome(InvTweaksContainerSection srcSection, int srcIndex, int amount) throws TimeoutException {
        return moveSome(srcSection, srcIndex, null, DROP_SLOT, amount);
    }
            
	public void leftClick(InvTweaksContainerSection section, int index) throws TimeoutException {
        click(section, index, false);
    }

    public void rightClick(InvTweaksContainerSection section, int index) throws TimeoutException {
        click(section, index, true);
    }

    public void click(InvTweaksContainerSection section, int index, boolean rightClick) throws TimeoutException {
        
        int slot = indexToSlot(section, index);
       // int timeSpentWaiting = 0;
        
        if (slot != -1) {
            
            /* boolean uselessClick = false;
            ItemStack stackInSlot = null;
            if (isMultiplayerWorld()) {
                // After clicking, we'll need to wait for server answer before continuing.
                // We'll do this by listening to any change in the slot, but this implies we
                // check first if the click will indeed produce a change.
                stackInSlot = (getItemStack(section, index) != null)
                        ? copy(getItemStack(section, index)) : null;
                ItemStack stackInHand = getHoldStack();
            
                // Useless if empty stacks
                if (stackInHand == null && stackInSlot == null)
                    uselessClick = true;
                // Useless if destination stack is full
                else if (stackInHand != null && stackInSlot != null
                        && stackInHand.isItemEqual(stackInSlot)
                        && getStackSize(stackInSlot) == getMaxStackSize(stackInSlot)) {
                    uselessClick = true;
                }
            }*/
        
            // Click!
            clickInventory(getPlayerController(),
                    getWindowId(container), // Select container
                    slot, // Targeted slot
                    (rightClick) ? 1 : 0, // Click #
                    false, // Shift not held
                    getThePlayer());
        
            // Wait for inventory update
            /*if (isMultiplayerWorld()) {
                if (!uselessClick) {
                    int pollingTime = 0;
                    // Note: Polling doesn't work for crafting output, if the same recipe
                    // can still be used.
                    while (areItemStacksEqual(getItemStack(section, index), stackInSlot)
                            && pollingTime < ACTION_TIMEOUT
                            && section != ContainerSection.CRAFTING_OUT) { 
                        try {
                            Thread.sleep(POLLING_DELAY);
                        } catch (InterruptedException e) {
                            // Do nothing
                        }
                        pollingTime += POLLING_DELAY;
                    }
                    if (pollingTime >= ACTION_TIMEOUT) {
                        log.warning("Click timeout");
                    }
                    timeSpentWaiting += pollingTime;
                }
            }*/
        }
    }

    public boolean hasSection(InvTweaksContainerSection section) {
        return slotRefs.containsKey(section);
    }

    public List<vv> getSlots(InvTweaksContainerSection section) {
        return slotRefs.get(section); 
    }

    /**
     * @return The size of the whole container
     */
    public int getSize() {
        int result = 0;
        for (List<vv> slots : slotRefs.values()) {
            result += slots.size();
        }
        return result;
    }
    
    /**
     * Returns the size of a section of the container.
     * @param slot
     * @return The size, or 0 if there is no such section.
     */
    public int getSize(InvTweaksContainerSection section) {
        if (hasSection(section)) {
            return slotRefs.get(section).size();  
        }
        else {
            return 0;
        }
    }

    /**
     * 
     * @param section
     * @return -1 if no slot is free
     */
    public int getFirstEmptyIndex(InvTweaksContainerSection section) {
        int i = 0;
        for (vv slot : slotRefs.get(section)) { 
            if (!hasStack(slot)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * @param slot
     * @return true if the specified slot exists and is empty, false otherwise.
     */
    public boolean isSlotEmpty(InvTweaksContainerSection section, int slot) {
        if (hasSection(section)) {
            return getItemStack(section, slot) == null;
        }
        else {
            return false;
        }
    }

    public vv getSlot(InvTweaksContainerSection section, int index) {
        List<vv> slots = slotRefs.get(section);
        if (slots != null) {
            return slots.get(index);
        } else {
            return null;
        }
    }

    public int getSlotIndex(int slotNumber) {
        return getSlotIndex(slotNumber, false);
    }
    
    /**
     * 
     * @param slotNumber
     * @param preferInventory Set to true if you prefer to have the index according
     * to the whole inventory, instead of a more specific section (hotbar/not hotbar)
     * @return
     */
    public int getSlotIndex(int slotNumber, boolean preferInventory) {
        // TODO Caching with getSlotSection
        for (InvTweaksContainerSection section : slotRefs.keySet()) {
            if (!preferInventory && section != InvTweaksContainerSection.INVENTORY
                    || (preferInventory && section != InvTweaksContainerSection.INVENTORY_NOT_HOTBAR
                            && section != InvTweaksContainerSection.INVENTORY_HOTBAR)) {
                int i = 0;
                for (vv slot : slotRefs.get(section)) {
                    if (getSlotNumber(slot) == slotNumber) {
                        return i;
                    }
                    i++;
                }
            }
        }
        return -1;
    }
    
    /**
     * Note: Prefers INVENTORY_HOTBAR/NOT_HOTBAR instead of INVENTORY.
     * @param slotNumber
     * @return null if the slot number is invalid.
     */
    public InvTweaksContainerSection getSlotSection(int slotNumber) {
        // TODO Caching with getSlotIndex
        for (InvTweaksContainerSection section : slotRefs.keySet()) {
            if (section != InvTweaksContainerSection.INVENTORY) {
                for (vv slot : slotRefs.get(section)) {
                    if (getSlotNumber(slot) == slotNumber) {
                        return section;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Returns an ItemStack from the wanted section and slot.
     * @param section
     * @param slot
     * @return An ItemStack or null.
     */
    public dk getItemStack(InvTweaksContainerSection section, int index) 
            throws NullPointerException, IndexOutOfBoundsException {
        int slot = indexToSlot(section, index);
        if (slot >= 0 && slot < getSlots(container).size()) {
            return getSlotStack(container, slot);
        } else {
            return null;
        }
    }

    public pj getContainer() {
        return container;
    }

    private int getFirstEmptyUsableSlotNumber() {
        for (InvTweaksContainerSection section : slotRefs.keySet()) {
            for (vv slot : slotRefs.get(section)) {
                // Use only standard slot (to make sure
                // we can freely put and remove items there)
                if (isSlot(slot) && !hasStack(slot)) {
                    return getSlotNumber(slot);
                }
            }
        }
        return -1;
    }
    
    /**
     * Converts section/index values to slot ID.
     * @param section
     * @param index
     * @return -1 if not found
     */
    private int indexToSlot(InvTweaksContainerSection section, int index) {
        if (index == DROP_SLOT) {
            return DROP_SLOT;
        }
        if (hasSection(section)) {
            vv slot = slotRefs.get(section).get(index);
            if (slot != null) {
                return getSlotNumber(slot);
            }
            else {
                return -1;
            }
        }
        else {
            return -1;
        }
    }
    
}
