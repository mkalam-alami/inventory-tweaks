package net.invtweaks.logic;

import java.util.concurrent.TimeoutException;

import net.invtweaks.library.ContainerManager;
import net.invtweaks.library.ContainerManager.ContainerSection;
import net.invtweaks.library.Obfuscation;
import net.minecraft.client.Minecraft;

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
            ContainerSection toSection) throws TimeoutException {
        synchronized(this) {
            initAction(fromSlot);

            int emptySlotIndex = container.getSlotIndex(
                    container.getFirstEmptySlot(toSection)); // TODO merge case
            
            switch (shortcutType) {
            
            case MOVE_STACK:
                container.move(fromSection, fromIndex,
                        toSection, emptySlotIndex);
                break;

            case MOVE_ONE:
                container.moveSome(fromSection, fromIndex,
                        toSection, emptySlotIndex, 1);
                break;
                
            case MOVE_ALL:
                container.move(fromSection, fromIndex,
                        toSection, emptySlotIndex);
                
            }
            
        }
        
        endAction();
    }
    
    public void drop(ShortcutType shortcutType, int fromSlot, 
            ContainerSection toSection) throws TimeoutException {
        synchronized(this) {
            initAction(fromSlot);
            // TODO Drop
            endAction();
        }
    }
    
    private void initAction(int fromSlot) throws TimeoutException {
        container = new ContainerManager(mc);
        fromSection = container.getSlotSection(fromSlot);
        fromIndex = container.getSlotIndex(fromSlot);
        
        // Cancel default click action
        if (getHoldStack() != null) {
            container.leftClick(fromSection, fromIndex);
        }
    }
    
    private void endAction() throws TimeoutException {
        if (getHoldStack() != null) {
            container.leftClick(fromSection, fromIndex);
        }
    }
    
}
