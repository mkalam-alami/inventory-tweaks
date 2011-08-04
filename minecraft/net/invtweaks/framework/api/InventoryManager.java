package net.invtweaks.framework.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.invtweaks.framework.Obfuscation;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Container;
import net.minecraft.src.ContainerChest;
import net.minecraft.src.ContainerDispenser;
import net.minecraft.src.ContainerFurnace;
import net.minecraft.src.ContainerPlayer;
import net.minecraft.src.ContainerWorkbench;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class InventoryManager extends Obfuscation {
	
    public static final int INVENTORY_SIZE = 36;
    
    public enum ContainerSection{
        /** The player's inventory */ INVENTORY,
        /** The chest or dispenser contents */ CHEST,
        /** The crafting slots */ CRAFTING,
        /** The armor slots */ ARMOR,
        /** The furnace input */ FURNACE_IN,
        /** The furnace output */ FURNACE_OUT,
        /** The furnace fuel */ FURNACE_FUEL,
        /** Any other type of slot. For unknown container types (such as
         * mod containers), only INVENTORY and OTHER sections are defined. */
        OTHER
    }
    
    private Container container;
    private Map<ContainerSection, List<Slot>> slotRefs = new HashMap<ContainerSection, List<Slot>>();
    
    
    /**
     * Creates an inventory manager according to the given container.
     * @param mc
     * @param container
     */
    @SuppressWarnings({"unchecked"})
    public InventoryManager(Minecraft mc, Container container) {
        super(mc);
        this.container = container;
        
        List<Slot> slots = container.slots;
        int size = slots.size();

        try {
            // Inventory: 4 crafting slots, then 4 armor slots, then inventory
            if (container instanceof ContainerPlayer) {
                slotRefs.put(ContainerSection.CRAFTING, slots.subList(0, 4));
                slotRefs.put(ContainerSection.ARMOR, slots.subList(4, 8));
                slotRefs.put(ContainerSection.INVENTORY, slots.subList(8, 44));
            }
            
            // Chest/Dispenser
            if ((container instanceof ContainerChest)
                    || (container instanceof ContainerDispenser)) {
                slotRefs.put(ContainerSection.CHEST, slots.subList(0, size-INVENTORY_SIZE));
                slotRefs.put(ContainerSection.INVENTORY, slots.subList(size-INVENTORY_SIZE, size));
            }
            
            // Furnace
            if ((container instanceof ContainerFurnace)) {
                slotRefs.put(ContainerSection.FURNACE_IN, slots.subList(0, 1));
                slotRefs.put(ContainerSection.FURNACE_FUEL, slots.subList(1, 2));
                slotRefs.put(ContainerSection.FURNACE_OUT, slots.subList(2, 3));
                slotRefs.put(ContainerSection.INVENTORY, slots.subList(size-INVENTORY_SIZE, size));
            }

            // Workbench
            if ((container instanceof ContainerWorkbench)) {
                slotRefs.put(ContainerSection.CRAFTING, slots.subList(0, 9));
                slotRefs.put(ContainerSection.INVENTORY, slots.subList(size-INVENTORY_SIZE, size));
            }
            
            // Unkown
            else {
                throw new Exception();
            }
        }
        catch (Exception e) {
            // Assuming the container ends with the inventory,
            // just like all vanilla containers.
            if (size >= INVENTORY_SIZE) {
                slotRefs.put(ContainerSection.OTHER, slots.subList(0, size-INVENTORY_SIZE));
                slotRefs.put(ContainerSection.INVENTORY, slots.subList(size-INVENTORY_SIZE, size));
            }
            else {
                slotRefs.put(ContainerSection.OTHER, slots.subList(0, size));
            }
        }
        
    }
    
    /**
     * Moves a stack from source to destination, adapting the behavior 
     * according to the context:
     * - If the items can be merged, as much items are possible are put
     *   in the destination, and the eventual remains go back to the source.
     * - If the items cannot be merged, they are swapped.
     * @param srcSection The source section
     * @param srcIndex The destination slot
     * @param destSection The destination section
     * @param destIndex The destination slot
     */
	public void swapOrMerge(ContainerSection srcSection, int srcIndex,
            ContainerSection destSection, int destIndex) {
	    //TODO
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
	 */
	public boolean moveSome(ContainerSection srcSection, int srcIndex,
	        ContainerSection destSection, int destIndex,
	        int amount) {
        return true; //TODO
	}

	/**
	 * Returns an ItemStack from the wanted section and slot.
     * @param section
	 * @param slot
	 * @return An ItemStack or null.
	 */
	public ItemStack getSlotStack(ContainerSection section, int slot) 
	        throws NullPointerException, IndexOutOfBoundsException {
        return slotRefs.get(section).get(slot).getStack();  
	}
	
	/**
	 * @param slot
	 * @return true if the specified slot exists and is empty, false otherwise.
	 */
	public boolean isSlotEmpty(ContainerSection section, int slot) {
        if (isSectionAvailable(section)) {
	        return getSlotStack(section, slot) == null;
	    }
        else {
	        return false;
	    }
	}
	
	/**
	 * @param sourceSlot
	 * @param destinationSlot
	 * @return true if the slots contents are of the same type, 
	 * and at least 1 item can be moved from source to destination.
	 */
    public boolean canBeMerged(int sourceSlot, int destinationSlot) {
        return true;
    }

    public boolean isSectionAvailable(ContainerSection section) {
        return slotRefs.containsKey(section);
    }
    
    /**
     * Returns the size of a section of the container.
     * @param slot
     * @return The size, or 0 if there is no such section.
     */
    public int getSectionSize(ContainerSection section) {
        if (isSectionAvailable(section)) {
            return slotRefs.get(section).size();  
        }
        else {
            return 0;
        }
    }
    
}
