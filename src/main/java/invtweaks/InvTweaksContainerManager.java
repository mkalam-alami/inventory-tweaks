package invtweaks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import invtweaks.api.container.ContainerSection;
import invtweaks.forge.InvTweaksMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Allows to perform various operations on the inventory and/or containers. Works in both single and multiplayer.
 *
 * @author Jimeo Wan
 */
public class InvTweaksContainerManager/* extends InvTweaksObfuscation*/ {

    // TODO: Throw errors when the container isn't available anymore

    public static final int DROP_SLOT = -999;
    public static final int HOTBAR_SIZE = 9;

    private GuiContainer guiContainer;
    private Container container;
    private Map<ContainerSection, List<Slot>> slotRefs = new HashMap<ContainerSection, List<Slot>>();
    private int clickDelay = 0;


    /**
     * Creates an container manager linked to the currently available container: - If a container GUI is open, the
     * manager gives access to this container contents. - If no GUI is open, the manager works as if the player's
     * inventory was open.
     *
     * @param mc Minecraft
     */
    @SuppressWarnings({"unchecked"})
    @SideOnly(Side.CLIENT)
    public InvTweaksContainerManager(Minecraft mc) {
        GuiScreen currentScreen = mc.currentScreen;
        if(currentScreen instanceof GuiContainer) {
            guiContainer = (GuiContainer) currentScreen;
            container = guiContainer.inventorySlots;
        } else {
            container = mc.thePlayer.inventoryContainer;
        }

        initSlots();
    }


    // TODO: Remove dependency on Minecraft class
    // TODO: Refactor the mouse-coverage stuff that needs the GuiContainer into a different class.
    public InvTweaksContainerManager(Container cont, GuiContainer gui) {
        guiContainer = gui;
        container = cont;
        initSlots();
    }

    private void initSlots() {
        slotRefs = InvTweaksObfuscation.getContainerSlotMap(container);
        if(slotRefs == null) {
            slotRefs = new HashMap<ContainerSection, List<Slot>>();
        }

        // TODO: Detect if there is a big enough unassigned section for inventory.
        @SuppressWarnings("unchecked")
        List<Slot> slots = (List<Slot>) container.inventorySlots;
        int size = slots.size();
        if(size >= InvTweaksConst.INVENTORY_SIZE && !slotRefs.containsKey(ContainerSection.INVENTORY)) {
            slotRefs.put(ContainerSection.INVENTORY, slots.subList(size - InvTweaksConst.INVENTORY_SIZE, size));
            slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR,
                         slots.subList(size - InvTweaksConst.INVENTORY_SIZE, size - HOTBAR_SIZE));
            slotRefs.put(ContainerSection.INVENTORY_HOTBAR, slots.subList(size - HOTBAR_SIZE, size));
        }
    }

    /**
     * Moves a stack from source to destination, adapting the behavior according to the context: - If destination is
     * empty, the source stack is moved. - If the items can be merged, as much items are possible are put in the
     * destination, and the eventual remains go back to the source. - If the items cannot be merged, they are swapped.
     *
     * @param srcSection  The source section
     * @param srcIndex    The source slot
     * @param destSection The destination section
     * @param destIndex   The destination slot
     * @return false if the source slot is empty or the player is holding an item that couln't be put down.
     * @throws TimeoutException
     */
    // TODO: Server helper directly implementing this as a swap without the need for intermediate slots.
    public boolean move(ContainerSection srcSection, int srcIndex, ContainerSection destSection, int destIndex) {
        ItemStack srcStack = getItemStack(srcSection, srcIndex);
        ItemStack destStack = getItemStack(destSection, destIndex);

        if(srcStack == null && destIndex != DROP_SLOT) {
            return false;
        } else if(srcSection == destSection && srcIndex == destIndex) {
            return true;
        }

        // Mod support -- some mods play tricks with slots to display an item but not let it be interacted with.
        // (Specifically forestry backpack UI)
        if(destIndex != DROP_SLOT) {
            Slot destSlot = getSlot(destSection, destIndex);
            if(!destSlot.isItemValid(srcStack)) {
                return false;
            }
        }


        // Put hold item down
        if(InvTweaks.getInstance().getHeldStack() != null) {
            int firstEmptyIndex = getFirstEmptyIndex(ContainerSection.INVENTORY);
            if(firstEmptyIndex != -1) {
                leftClick(ContainerSection.INVENTORY, firstEmptyIndex);
            } else {
                return false;
            }
        }

        // Use intermediate slot if we have to swap tools, maps, etc.
        if(destStack != null && srcStack.getItem() == destStack.getItem() && (srcStack.getMaxStackSize() == 1 ||
                srcStack.hasTagCompound() || destStack.hasTagCompound())) {
            int intermediateSlot = getFirstEmptyUsableSlotNumber();
            ContainerSection intermediateSection = getSlotSection(intermediateSlot);
            int intermediateIndex = getSlotIndex(intermediateSlot);
            if(intermediateIndex != -1) {
                Slot interSlot = getSlot(intermediateSection, intermediateIndex);
                if(!interSlot.isItemValid(destStack)) {
                    return false;
                }
                Slot srcSlot = getSlot(srcSection, srcIndex);
                if(!srcSlot.isItemValid(destStack)) {
                    return false;
                }
                // Step 1/3: Dest > Int
                leftClick(destSection, destIndex);
                leftClick(intermediateSection, intermediateIndex);
                // Step 2/3: Src > Dest
                leftClick(srcSection, srcIndex);
                leftClick(destSection, destIndex);
                // Step 3/3: Int > Src
                leftClick(intermediateSection, intermediateIndex);
                leftClick(srcSection, srcIndex);
            } else {
                return false;
            }
        }

        // Normal move
        else {
            leftClick(srcSection, srcIndex);
            leftClick(destSection, destIndex);
            if(InvTweaks.getInstance().getHeldStack() != null) {
                // Only return to original slot if it can be placed in that slot.
                // (Ex. crafting/furnace outputs)
                Slot srcSlot = getSlot(srcSection, srcIndex);
                if(srcSlot.isItemValid(InvTweaks.getInstance().getHeldStack())) {
                    leftClick(srcSection, srcIndex);
                } else {
                    // If the item cannot be placed in its original slot, move to an empty slot.
                    int firstEmptyIndex = getFirstEmptyIndex(ContainerSection.INVENTORY);
                    if(firstEmptyIndex != -1) {
                        leftClick(ContainerSection.INVENTORY, firstEmptyIndex);
                    }
                    // else leave there because we have nowhere to put it.
                }
            }
        }

        return true;
    }

    /**
     * Moves some items from source to destination.
     *
     * @param srcSection  The source section
     * @param srcIndex    The source slot
     * @param destSection The destination section
     * @param destIndex   The destination slot
     * @param amount      The amount of items to move. If <= 0, does nothing. If > to the source stack size, moves as
     *                    much as possible from the stack size. If not all can be moved to the destination, only moves
     *                    as much as possible.
     * @return false if the destination slot is already occupied by a different item (meaning items cannot be moved to
     * destination).
     * @throws TimeoutException
     */
    // TODO: Server helper directly implementing this.
    public boolean moveSome(ContainerSection srcSection, int srcIndex, ContainerSection destSection, int destIndex,
                            int amount) {

        ItemStack source = getItemStack(srcSection, srcIndex);
        if(source == null || srcSection == destSection && srcIndex == destIndex) {
            return true;
        }

        ItemStack destination = getItemStack(srcSection, srcIndex);
        int sourceSize = source.stackSize;
        int movedAmount = Math.min(amount, sourceSize);

        if(destination == null || InvTweaksObfuscation.areItemStacksEqual(source, destination)) {

            leftClick(srcSection, srcIndex);
            for(int i = 0; i < movedAmount; i++) {
                rightClick(destSection, destIndex);
            }
            if(movedAmount < sourceSize) {
                leftClick(srcSection, srcIndex);
            }
            return true;
        } else {
            return false;
        }

    }

    // TODO: Server helper directly implementing this.
    public boolean drop(ContainerSection srcSection, int srcIndex) {
        return move(srcSection, srcIndex, null, DROP_SLOT);
    }

    // TODO: Server helper directly implementing this.
    public boolean dropSome(ContainerSection srcSection, int srcIndex, int amount) {
        return moveSome(srcSection, srcIndex, null, DROP_SLOT, amount);
    }

    /**
     * If an item is in hand (= attached to the cursor), puts it down.
     *
     * @return true unless the item could not be put down
     * @throws Exception
     */
    public boolean putHoldItemDown(ContainerSection destSection, int destIndex) {
        ItemStack heldStack = InvTweaks.getInstance().getHeldStack();
        if(heldStack != null) {
            if(getItemStack(destSection, destIndex) == null) {
                click(destSection, destIndex, false);
                return true;
            }
            return false;
        }
        return true;
    }

    public void leftClick(ContainerSection section, int index) {
        click(section, index, false);
    }

    public void rightClick(ContainerSection section, int index) {
        click(section, index, true);
    }

    public void click(ContainerSection section, int index, boolean rightClick) {
        //System.out.println("Click " + section + ":" + index);
        // Click! (we finally call the Minecraft code)
        int slot = indexToSlot(section, index);
        if(slot != -1) {
            int data = (rightClick) ? 1 : 0;
            InvTweaksMod.proxy
                        .slotClick(InvTweaks.getInstance().getPlayerController(), container.windowId, slot, data, 0,
                                   InvTweaks.getInstance().getThePlayer());
        }

        if(clickDelay > 0) {
            try {
                Thread.sleep(clickDelay);
            } catch(InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public boolean hasSection(ContainerSection section) {
        return slotRefs.containsKey(section);
    }

    public List<Slot> getSlots(ContainerSection section) {
        return slotRefs.get(section);
    }

    /**
     * @return The size of the whole container
     */
    public int getSize() {
        int result = 0;
        for(List<Slot> slots : slotRefs.values()) {
            result += slots.size();
        }
        return result;
    }

    /**
     * Returns the size of a section of the container.
     *
     * @param section
     * @return The size, or 0 if there is no such section.
     */
    public int getSize(ContainerSection section) {
        if(hasSection(section)) {
            return slotRefs.get(section).size();
        } else {
            return 0;
        }
    }

    /**
     * @param section
     * @return -1 if no slot is free
     */
    public int getFirstEmptyIndex(ContainerSection section) {
        int i = 0;
        for(Slot slot : slotRefs.get(section)) {
            if(!slot.getHasStack()) {
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
    public boolean isSlotEmpty(ContainerSection section, int slot) {
        if(hasSection(section)) {
            return getItemStack(section, slot) == null;
        } else {
            return false;
        }
    }

    public Slot getSlot(ContainerSection section, int index) {
        List<Slot> slots = slotRefs.get(section);
        if(slots != null) {
            return slots.get(index);
        } else {
            return null;
        }
    }

    /**
     * @param slotNumber
     * @return -1 if not found
     */
    public int getSlotIndex(int slotNumber) {
        return getSlotIndex(slotNumber, false);
    }

    /**
     * @param slotNumber
     * @param preferInventory Set to true if you prefer to have the index according to the whole inventory, instead of a
     *                        more specific section (hotbar/not hotbar)
     * @return Full index of slot in the container
     */
    public int getSlotIndex(int slotNumber, boolean preferInventory) {
        // TODO Caching with getSlotSection
        for(ContainerSection section : slotRefs.keySet()) {
            if(!preferInventory && section != ContainerSection.INVENTORY || (preferInventory && section != ContainerSection.INVENTORY_NOT_HOTBAR && section != ContainerSection.INVENTORY_HOTBAR)) {
                int i = 0;
                for(Slot slot : slotRefs.get(section)) {
                    if(InvTweaksObfuscation.getSlotNumber(slot) == slotNumber) {
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
     *
     * @param slotNumber
     * @return null if the slot number is invalid.
     */
    public ContainerSection getSlotSection(int slotNumber) {
        // TODO Caching with getSlotIndex
        for(ContainerSection section : slotRefs.keySet()) {
            if(section != ContainerSection.INVENTORY) {
                for(Slot slot : slotRefs.get(section)) {
                    if(InvTweaksObfuscation.getSlotNumber(slot) == slotNumber) {
                        return section;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns an ItemStack from the wanted section and slot.
     *
     * @param section
     * @param index
     * @return An ItemStack or null.
     */
    public ItemStack getItemStack(ContainerSection section, int index)
            throws NullPointerException, IndexOutOfBoundsException {
        int slot = indexToSlot(section, index);
        if(slot >= 0 && slot < container.inventorySlots.size()) {
            return InvTweaksObfuscation.getSlotStack(container, slot);
        } else {
            return null;
        }
    }

    public Container getContainer() {
        return container;
    }

    private int getFirstEmptyUsableSlotNumber() {
        for(ContainerSection section : slotRefs.keySet()) {
            for(Slot slot : slotRefs.get(section)) {
                // Use only standard slot (to make sure
                // we can freely put and remove items there)
                if(InvTweaksObfuscation.isBasicSlot(slot) && !slot.getHasStack()) {
                    return InvTweaksObfuscation.getSlotNumber(slot);
                }
            }
        }
        return -1;
    }

    /**
     * Converts section/index values to slot ID.
     *
     * @param section
     * @param index
     * @return -1 if not found
     */
    private int indexToSlot(ContainerSection section, int index) {
        if(index == DROP_SLOT) {
            return DROP_SLOT;
        }
        if(hasSection(section)) {
            Slot slot = slotRefs.get(section).get(index);
            if(slot != null) {
                return InvTweaksObfuscation.getSlotNumber(slot);
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public void setClickDelay(int delay) {
        this.clickDelay = delay;
    }

}
