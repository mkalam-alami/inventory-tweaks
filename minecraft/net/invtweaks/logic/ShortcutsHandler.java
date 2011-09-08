package net.invtweaks.logic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.invtweaks.config.InvTweaksConfig;
import net.invtweaks.library.ContainerManager;
import net.invtweaks.library.ContainerManager.ContainerSection;
import net.invtweaks.library.Obfuscation;
import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.InvTweaks;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * 
 * @author Jimeo Wan
 *
 */
public class ShortcutsHandler extends Obfuscation {

    private final static int DROP_SLOT = -999;
    
    private ShortcutType defaultAction = ShortcutType.MOVE_ONE_STACK;
    private ShortcutType defaultDestination = null;
    
    // Context attributes
    private InvTweaksConfig config;
    private ContainerManager container;
    private ContainerSection fromSection;
    private int fromIndex;
    private ItemStack fromStack;
    private ContainerSection toSection;
    private ShortcutType shortcutType;

    /**
     * Allows to monitor the keys related to shortcuts
     */
    private Map<Integer, Boolean> shortcutKeysStatus;
    
    /**
     * Stores shortcuts mappings
     */
    private Map<ShortcutType, List<Integer>> shortcuts;
    
    private enum ShortcutType {
        MOVE_TO_SPECIFIC_HOTBAR_SLOT,
        MOVE_ONE_STACK,
        MOVE_ONE_ITEM,
        MOVE_ALL_ITEMS,
        MOVE_UP,
        MOVE_DOWN,
        MOVE_TO_EMPTY_SLOT,
        DROP
    }

    public ShortcutsHandler(Minecraft mc, InvTweaksConfig config) {
        super(mc);
        this.config = config;
        reset();
    }
    
    public void reset() {
        
        shortcutKeysStatus = new HashMap<Integer, Boolean>();
        shortcuts = new HashMap<ShortcutType, List<Integer>>();
        
        Map<String, String> keys = config.getProperties(
                InvTweaksConfig.PROP_SHORTCUT_PREFIX);
        for (String key : keys.keySet()) {
            
            String value = keys.get(key);
            
            if (value.equals(InvTweaksConfig.VALUE_DEFAULT)) {
                // Customize default behaviour
                ShortcutType newDefault = propNameToShortcutType(key);
                if (newDefault == ShortcutType.MOVE_ALL_ITEMS
                        || newDefault == ShortcutType.MOVE_ONE_ITEM
                        || newDefault == ShortcutType.MOVE_ONE_STACK) {
                    defaultAction = newDefault;
                }
                else if (newDefault == ShortcutType.MOVE_DOWN
                        || newDefault == ShortcutType.MOVE_UP) {
                    defaultDestination = newDefault;
                }
            }
            else {
                // Register shortcut mappings
                String[] keyNames = keys.get(key).split("[ ]*,[ ]*");
                List<Integer> keyBindings = new LinkedList<Integer>();
                for (String keyName : keyNames) {
                    // - Accept both KEY_### and ###, in case someone
                    //   takes the LWJGL Javadoc at face value
                    // - Accept LALT & RALT instead of LMENU & RMENU
                    keyBindings.add(Keyboard.getKeyIndex(
                            keyName.replace("KEY_", "").replace("ALT", "MENU")));
                }
                ShortcutType shortcutType = propNameToShortcutType(key);
                if (shortcutType != null) {
                    shortcuts.put(shortcutType, keyBindings);
                }
                
                // Register key status listener
                for (Integer keyCode : keyBindings) {
                    shortcutKeysStatus.put(keyCode, false);
                }
            }
            
        }
        
        // Add Minecraft's Up & Down bindings to the shortcuts
        int upKeyCode = mc.gameSettings.keyBindForward.keyCode,
            downKeyCode = mc.gameSettings.keyBindBack.keyCode;
        shortcuts.get(ShortcutType.MOVE_UP).add(upKeyCode);
        shortcuts.get(ShortcutType.MOVE_DOWN).add(downKeyCode);
        shortcutKeysStatus.put(upKeyCode, false);
        shortcutKeysStatus.put(downKeyCode, false);
        
        // Add hotbar shortcuts (1-9) mappings & listeners
        List<Integer> keyBindings = new LinkedList<Integer>();
        int[] hotbarKeys = {Keyboard.KEY_1, Keyboard.KEY_2, Keyboard.KEY_3, 
                Keyboard.KEY_4, Keyboard.KEY_5, Keyboard.KEY_6,
                Keyboard.KEY_7, Keyboard.KEY_8, Keyboard.KEY_9,
                Keyboard.KEY_NUMPAD1, Keyboard.KEY_NUMPAD2, Keyboard.KEY_NUMPAD3,
                Keyboard.KEY_NUMPAD4, Keyboard.KEY_NUMPAD5, Keyboard.KEY_NUMPAD6, 
                Keyboard.KEY_NUMPAD7, Keyboard.KEY_NUMPAD8, Keyboard.KEY_NUMPAD9};
        for (int i : hotbarKeys) {
            keyBindings.add(i);
            shortcutKeysStatus.put(i, false);
        }
        shortcuts.put(ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT, keyBindings);
        
    }
    
    public Vector<Integer> getDownShortcutKeys() {
        updateKeyStatuses();
        Vector<Integer> downShortcutKeys = new Vector<Integer>();
        for (Integer key : shortcutKeysStatus.keySet()) {
            if (shortcutKeysStatus.get(key)) {
                downShortcutKeys.add(key);
            }
        }
        return downShortcutKeys;
    }
    
    public void handleShortcut(GuiContainer guiScreen) {
        // IMPORTANT: This method is called before the default action is executed.
        
        updateKeyStatuses();
        
        // Initialization
        int ex = Mouse.getEventX(), ey = Mouse.getEventY();
        int x = (ex * guiScreen.width) / mc.displayWidth;
        int y = guiScreen.height - (ey * guiScreen.height) / mc.displayHeight - 1;
        boolean shortcutValid = false;
        
        // Check that the slot is not empty
        Slot slot = getSlotAtPosition((GuiContainer) guiScreen, x, y);
        
        if (slot != null && slot.getHasStack()) {
    
            // Choose shortcut type
            ShortcutType shortcutType = defaultAction;
            if (isActive(ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT) != -1) {
                shortcutType = ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT;
                shortcutValid = true;
            }
            if (isActive(ShortcutType.MOVE_ALL_ITEMS) != -1) {
                shortcutType = ShortcutType.MOVE_ALL_ITEMS;
                shortcutValid = true;
            }
            else if (isActive(ShortcutType.MOVE_ONE_ITEM) != -1) {
                shortcutType = ShortcutType.MOVE_ONE_ITEM;
                shortcutValid = true;
            }
            
            // Choose target section
            try {
                ContainerManager container = new ContainerManager(mc);
                ContainerSection srcSection = container.getSlotSection(slot.slotNumber);
                ContainerSection destSection = null;
                
                // Set up available sections
                Vector<ContainerSection> availableSections = new Vector<ContainerSection>();
                if (container.hasSection(ContainerSection.CHEST)) {
                    availableSections.add(ContainerSection.CHEST);
                }
                else if (container.hasSection(ContainerSection.CRAFTING_IN)) {
                    availableSections.add(ContainerSection.CRAFTING_IN);
                }
                else if (container.hasSection(ContainerSection.FURNACE_IN)) {
                    availableSections.add(ContainerSection.FURNACE_IN);
                }
                availableSections.add(ContainerSection.INVENTORY_NOT_HOTBAR);
                availableSections.add(ContainerSection.INVENTORY_HOTBAR);
                
                // Check for destination modifiers
                int destinationModifier = 0; 
                if (isActive(ShortcutType.MOVE_UP) != -1
                        || defaultDestination == ShortcutType.MOVE_UP) {
                    destinationModifier = -1;
                }
                else if (isActive(ShortcutType.MOVE_DOWN) != -1
                        || defaultDestination == ShortcutType.MOVE_DOWN) {
                    destinationModifier = 1;
                }
                
                if (destinationModifier == 0) {
                    // Default behavior
                    switch (srcSection) {

                    case INVENTORY_HOTBAR:
                        destSection = ContainerSection.INVENTORY_NOT_HOTBAR;
                        break;
                        
                    case CRAFTING_IN:
                    case FURNACE_IN:
                        destSection = ContainerSection.INVENTORY_NOT_HOTBAR;
                        break;
                        
                    default:
                        destSection = ContainerSection.INVENTORY_HOTBAR;
                    }
                }
                
                else {
                    // Specific destination
                    shortcutValid = true;
                    int srcSectionIndex = availableSections.indexOf(srcSection);
                    if (srcSectionIndex != -1) {
                        destSection = availableSections.get(
                                (availableSections.size() + srcSectionIndex + 
                                        destinationModifier) % availableSections.size());
                    }
                    else {
                        destSection = ContainerSection.INVENTORY;
                    }
                }
                
                if (shortcutValid || isActive(ShortcutType.DROP) != -1) {
                    
                    initAction(slot.slotNumber, shortcutType, destSection);
                    
                    if (shortcutType == ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT) {
                        
                        // Move to specific hotbar slot
                        String keyName = Keyboard.getKeyName(
                                isActive(ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT));
                        int destIndex = -1+Integer.parseInt(keyName.replace("NUMPAD", ""));
                        container.move(fromSection, fromIndex,
                                ContainerSection.INVENTORY_HOTBAR, destIndex);
                        
                    } else {
                        
                        // Drop or move
                        if (srcSection == ContainerSection.CRAFTING_OUT) {
                            craftAll(Mouse.isButtonDown(1), isActive(ShortcutType.DROP) != -1);
                        } else {
                            move(Mouse.isButtonDown(1), isActive(ShortcutType.DROP) != -1);
                        }
                    }
                    
                    // Reset mouse status to prevent default action.
                    Mouse.destroy();
                    Mouse.create();
                    
                    // Fixes a tiny glitch (Steve looks for a short moment
                    // at [0, 0] because of the mouse reset).
                    Mouse.setCursorPosition(ex, ey);
                }
    
            } catch (Exception e) {
               InvTweaks.logInGameErrorStatic("Failed to trigger shortcut", e);
            }
        }
            
    }

    private void move(boolean separateStacks, boolean drop) throws Exception {
        
        int toIndex = -1;
        
        synchronized(this) {
    
            toIndex = getNextIndex(separateStacks, drop);
            if (toIndex != -1) {
                switch (shortcutType) {
                
                case MOVE_ONE_STACK:
                {
                    Slot slot = container.getSlot(fromSection, fromIndex);
                    while (slot.getHasStack() && toIndex != -1) {
                        container.move(fromSection, fromIndex, toSection, toIndex);
                        toIndex = getNextIndex(separateStacks, drop);
                    }
                    break;
    
                }
                
                case MOVE_ONE_ITEM:
                {
                    container.moveSome(fromSection, fromIndex, toSection, toIndex, 1);
                    break;
                }
                    
                case MOVE_ALL_ITEMS:
                {
                    for (Slot slot : container.getSlots(fromSection)) {
                        if (slot.getHasStack() && areSameItemType(fromStack, slot.getStack())) {
                            int fromIndex = container.getSlotIndex(slot.slotNumber);
                            while (slot.getHasStack() && toIndex != -1 && 
                                    !(fromSection == toSection && fromIndex == toIndex)) {
                                boolean moveResult = container.move(fromSection, fromIndex,
                                        toSection, toIndex);
                                if (!moveResult) {
                                    break;
                                }
                                toIndex = getNextIndex(separateStacks, drop);
                            }
                        }
                    }
                }
                    
                }
            }
            
        }
    }
    
    private void craftAll(boolean separateStacks, boolean drop) throws Exception {
        int toIndex = getNextIndex(separateStacks, drop);
        Slot slot = container.getSlot(fromSection, fromIndex);
        while (slot.getHasStack() && toIndex != -1) {
            container.move(fromSection, fromIndex, toSection, toIndex);
            toIndex = getNextIndex(separateStacks, drop);
            if (getHoldStack() != null) {
                container.leftClick(toSection, toIndex);
                toIndex = getNextIndex(separateStacks, drop);
            }
        }
    }

    /**
     * Checks if the Up/Down controls that are listened are outdated
     * @return true if the shortuts listeners have to be reset
     */
    private boolean haveControlsChanged() {
        return (!shortcutKeysStatus.containsKey(mc.gameSettings.keyBindForward.keyCode)
                || !shortcutKeysStatus.containsKey(mc.gameSettings.keyBindBack.keyCode));
    }

    private void updateKeyStatuses() {
        if (haveControlsChanged())
            reset();
        for (int keyCode : shortcutKeysStatus.keySet()) {
            if (Keyboard.isKeyDown(keyCode)) {
                if (!shortcutKeysStatus.get(keyCode)) {
                    shortcutKeysStatus.put(keyCode, true);
                }
            }
            else {
                shortcutKeysStatus.put(keyCode, false);
            }
        }
    }

    private int getNextIndex(boolean emptySlotOnly, boolean drop) {
        
        if (drop) {
            return DROP_SLOT;
        }
        
        int result = -1;

        // Try to merge with existing slot
        if (!emptySlotOnly) {
            int i = 0;
            for (Slot slot : container.getSlots(toSection)) {
                if (slot.getHasStack()) {
                    ItemStack stack = slot.getStack();
                    if (stack.isItemEqual(fromStack)
                            && getStackSize(stack) < getMaxStackSize(stack)) {
                        result = i;
                        break;
                    }
                }
                i++;
            }
        }
        
        // Else find empty slot
        if (result == -1) {
            result = container.getFirstEmptyIndex(toSection);
        }
        
        // Switch from FURNACE_IN to FURNACE_FUEL if the slot is taken
        if (result == -1 && toSection == ContainerSection.FURNACE_IN) {
            toSection =  ContainerSection.FURNACE_FUEL;
            result = container.getFirstEmptyIndex(toSection);
        }
        
        return result;
    }

    /**
     * @param shortcutType
     * @return The key that made the shortcut active
     */
    private int isActive(ShortcutType shortcutType) {
        for (Integer keyCode : shortcuts.get(shortcutType)) {
            if (shortcutKeysStatus.get(keyCode) && 
                    // AltGr also activates LCtrl, make sure the real LCtrl has been pressed
                    (keyCode != 29 || !Keyboard.isKeyDown(184))) {
                return keyCode;
            }
        }
        return -1;
    }

    private void initAction(int fromSlot, ShortcutType shortcutType, ContainerSection destSection) throws Exception {
        
        // Set up context
        this.container = new ContainerManager(mc);
        this.fromSection = container.getSlotSection(fromSlot);
        this.fromIndex = container.getSlotIndex(fromSlot);
        this.fromStack = container.getItemStack(fromSection, fromIndex);
        this.shortcutType = shortcutType;
        this.toSection = destSection;
        
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
    
    private Slot getSlotAtPosition(GuiContainer guiContainer, int i, int j) { 
        // Copied from GuiContainer
        for (int k = 0; k < guiContainer.inventorySlots.slots.size(); k++) {
            Slot slot = (Slot)guiContainer.inventorySlots.slots.get(k);
            if (InvTweaks.getIsMouseOverSlot(guiContainer, slot, i, j)) {
                return slot;
            }
        }
        return null;
    }

    private ShortcutType propNameToShortcutType(String property) {
        if (property.equals(InvTweaksConfig.PROP_SHORTCUT_ALL_ITEMS)) {
            return ShortcutType.MOVE_ALL_ITEMS;
        } else if (property.equals(InvTweaksConfig.PROP_SHORTCUT_DOWN)) {
            return ShortcutType.MOVE_DOWN;
        } else if (property.equals(InvTweaksConfig.PROP_SHORTCUT_DROP)) {
            return ShortcutType.DROP;
        } else if (property.equals(InvTweaksConfig.PROP_SHORTCUT_ONE_ITEM)) {
            return ShortcutType.MOVE_ONE_ITEM;
        } else if (property.equals(InvTweaksConfig.PROP_SHORTCUT_ONE_STACK)) {
            return ShortcutType.MOVE_ONE_STACK;
        } else if (property.equals(InvTweaksConfig.PROP_SHORTCUT_UP)) {
            return ShortcutType.MOVE_UP;
        } else {
            return null;
        }
    }
    
}
