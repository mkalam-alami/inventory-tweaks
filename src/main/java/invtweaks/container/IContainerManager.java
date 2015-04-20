package invtweaks.container;

import invtweaks.api.container.ContainerSection;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.concurrent.TimeoutException;

public interface IContainerManager {
    int DROP_SLOT = -999;
    int HOTBAR_SIZE = 9;

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
    boolean move(ContainerSection srcSection, int srcIndex, ContainerSection destSection, int destIndex);

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
    boolean moveSome(ContainerSection srcSection, int srcIndex, ContainerSection destSection, int destIndex,
                     int amount);

    // TODO: Server helper directly implementing this.
    default boolean drop(ContainerSection srcSection, int srcIndex) {
        return move(srcSection, srcIndex, null, DROP_SLOT);
    }

    // TODO: Server helper directly implementing this.
    default boolean dropSome(ContainerSection srcSection, int srcIndex, int amount) {
        return moveSome(srcSection, srcIndex, null, DROP_SLOT, amount);
    }

    /**
     * If an item is in hand (= attached to the cursor), puts it down.
     *
     * @return true unless the item could not be put down
     * @throws Exception
     */
    boolean putHoldItemDown(ContainerSection destSection, int destIndex);

    default void leftClick(ContainerSection section, int index) {
        click(section, index, false);
    }

    default void rightClick(ContainerSection section, int index) {
        click(section, index, true);
    }

    void click(ContainerSection section, int index, boolean rightClick);

    boolean hasSection(ContainerSection section);

    List<Slot> getSlots(ContainerSection section);

    /**
     * @return The size of the whole container
     */
    int getSize();

    /**
     * Returns the size of a section of the container.
     *
     * @param section
     * @return The size, or 0 if there is no such section.
     */
    int getSize(ContainerSection section);

    /**
     * @param section
     * @return -1 if no slot is free
     */
    int getFirstEmptyIndex(ContainerSection section);

    /**
     * @param slot
     * @return true if the specified slot exists and is empty, false otherwise.
     */
    boolean isSlotEmpty(ContainerSection section, int slot);

    Slot getSlot(ContainerSection section, int index);

    /**
     * @param slotNumber
     * @return -1 if not found
     */
    default int getSlotIndex(int slotNumber) {
        return getSlotIndex(slotNumber, false);
    }

    /**
     * @param slotNumber
     * @param preferInventory Set to true if you prefer to have the index according to the whole inventory, instead of a
     *                        more specific section (hotbar/not hotbar)
     * @return Full index of slot in the container
     */
    int getSlotIndex(int slotNumber, boolean preferInventory);

    /**
     * Note: Prefers INVENTORY_HOTBAR/NOT_HOTBAR instead of INVENTORY.
     *
     * @param slotNumber
     * @return null if the slot number is invalid.
     */
    ContainerSection getSlotSection(int slotNumber);

    /**
     * Returns an ItemStack from the wanted section and slot.
     *
     * @param section
     * @param index
     * @return An ItemStack or null.
     */
    ItemStack getItemStack(ContainerSection section, int index);

    Container getContainer();

    void setClickDelay(int delay);
}
