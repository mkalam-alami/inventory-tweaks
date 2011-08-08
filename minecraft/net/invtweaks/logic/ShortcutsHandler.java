package net.invtweaks.logic;

import net.invtweaks.library.ContainerManager;
import net.invtweaks.library.ContainerManager.ContainerSection;
import net.invtweaks.library.Obfuscation;
import net.minecraft.client.Minecraft;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

/**
 * 
 * @author Jimeo Wan
 *
 */
public class ShortcutsHandler extends Obfuscation {
    
    private ContainerManager container;
    private ContainerSection fromSection;
    private int fromIndex;
    
    public enum ShortcutType { 
        /** One item of the stack */ MOVE_ONE,
        /** The stack */ MOVE_STACK,
        /** All items of same type */ MOVE_ALL
    }
    
    public ShortcutsHandler(Minecraft mc) {
        super(mc);
        
    }
    
    public void move(ShortcutType shortcutType, int fromSlot,
            ContainerSection toSection, boolean separateStacks) throws Exception {
        // TODO: If separate stacks = false
        
        // IMPORTANT: This is called before the default action is executed.
        
        synchronized(this) {
            initAction(fromSlot);

            int emptySlotIndex = container.getFirstEmptyIndex(toSection); // TODO merge case
            
            // Switch to FURNACE_IN to FURNACE_FUEL if the slot is taken
            if (emptySlotIndex == -1 && toSection == ContainerSection.FURNACE_IN) {
                toSection =  ContainerSection.FURNACE_FUEL;
                emptySlotIndex = container.getFirstEmptyIndex(toSection);
            }
            
            if (emptySlotIndex != -1) {
                switch (shortcutType) {
                
                case MOVE_STACK:
                    container.move(fromSection, fromIndex, toSection, emptySlotIndex);
                    break;
    
                case MOVE_ONE:
                    container.moveSome(fromSection, fromIndex, toSection, emptySlotIndex, 1);
                    break;
                    
                case MOVE_ALL:
                    ItemStack fromStack = container.getItemStack(fromSection, fromIndex);
                    for (Slot slot : container.getSectionSlots(fromSection)) {
                        if (slot.getHasStack() && 
                                fromStack.isItemEqual(slot.getStack())) {
                            boolean moveResult = container.move(fromSection,
                                    container.getSlotIndex(slot.slotNumber),
                                    toSection, emptySlotIndex);
                            if (!moveResult) {
                                break;
                            }
                            emptySlotIndex = container.getFirstEmptyIndex(toSection);
                        }
                    }
                    
                }
            }
            
        }
    }
    
    public void drop(ShortcutType shortcutType, int fromSlot, 
            ContainerSection toSection) throws Exception {
        synchronized(this) {
            initAction(fromSlot);
            // TODO Drop
        }
    }
    
    private void initAction(int fromSlot) throws Exception {
        container = new ContainerManager(mc);
        fromSection = container.getSlotSection(fromSlot);
        fromIndex = container.getSlotIndex(fromSlot);
        
        // Put hold stack down
        if (getHoldStack() != null) {
            
            container.leftClick(fromSection, fromIndex);
            
            // Sometimes (ex: crafting output) we can't put back the item
            // in the slot, in that case choose a new one.
            if (getHoldStack() != null) {
                int firstEmptyIndex = container.getFirstEmptyIndex(ContainerSection.INVENTORY);
                if (firstEmptyIndex != -1) {
                   fromSection = ContainerSection.INVENTORY;
                   fromSlot = firstEmptyIndex;
                   container.leftClick(fromSection, fromSlot);
                   
                }
                else {
                    throw new Exception("Couldn't put hold item down");
                }
            }
        }
    }
    
}
