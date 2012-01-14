import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;


/**
 * 
 * @author Jimeo Wan
 *
 */
public class InvTweaksHandlerShortcuts extends InvTweaksObfuscation {

    /*
     * TODO: Big refactoring to make functions less context-dependant
     * (pass all as parameters instead of using class attributes)
     */
    
    private static final int DROP_SLOT = -999;
    
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("InvTweaks");
    
    private ShortcutType defaultAction = ShortcutType.MOVE_ONE_STACK;
    private ShortcutType defaultDestination = null;
    
    // Context attributes
    private InvTweaksConfig config;
    private InvTweaksContainerManager container;
    private InvTweaksContainerSection fromSection;
    private int fromIndex;
    private yq fromStack;
    private InvTweaksContainerSection toSection;
    private ShortcutType shortcutType;

    /**
     * Allows to monitor the keys related to shortcuts
     */
    private Map<Integer, Boolean> shortcutKeysStatus
    	= new HashMap<Integer, Boolean>();
    
    /**
     * Stores shortcuts mappings
     */
    private Map<ShortcutType, List<InvTweaksShortcutMapping>> shortcuts
    	= new HashMap<ShortcutType, List<InvTweaksShortcutMapping>>();
    
    private enum ShortcutType {
        MOVE_TO_SPECIFIC_HOTBAR_SLOT,
        MOVE_ONE_STACK,
        MOVE_ONE_ITEM,
        MOVE_ALL_ITEMS,
        MOVE_UP,
        MOVE_DOWN,
        MOVE_TO_EMPTY_SLOT,
        CRAFT,
        DROP
    }

    public InvTweaksHandlerShortcuts(Minecraft mc, InvTweaksConfig config) {
        super(mc);
        this.config = config;
        reset();
    }
    
    public void reset() {
        shortcutKeysStatus.clear();
        shortcuts.clear();

        // Register shortcut mappings
        Map<String, String> keys = config.getProperties(
                InvTweaksConfig.PROP_SHORTCUT_PREFIX);
        for (String key : keys.keySet()) {
            String[] keyMappings = keys.get(key).split("[ ]*,[ ]*");
            ShortcutType shortcutType = propNameToShortcutType(key);
            if (shortcutType != null) {
                for (String keyMapping : keyMappings) {
                	String[] keysToHold = keyMapping.split("\\+");
                	registerShortcutMapping(shortcutType, new InvTweaksShortcutMapping(keysToHold));
                }
            }
        }
        
        // Add Minecraft's Up & Down mappings
        int upKeyCode = getKeyBindingForwardKeyCode(),
            downKeyCode = getKeyBindingBackKeyCode();
        
        registerShortcutMapping(ShortcutType.MOVE_UP, new InvTweaksShortcutMapping(upKeyCode));
        registerShortcutMapping(ShortcutType.MOVE_DOWN, new InvTweaksShortcutMapping(downKeyCode));
        
        // Add hotbar shortcuts (1-9) mappings
        int[] hotbarKeys = {Keyboard.KEY_1, Keyboard.KEY_2, Keyboard.KEY_3, 
                Keyboard.KEY_4, Keyboard.KEY_5, Keyboard.KEY_6,
                Keyboard.KEY_7, Keyboard.KEY_8, Keyboard.KEY_9,
                Keyboard.KEY_NUMPAD1, Keyboard.KEY_NUMPAD2, Keyboard.KEY_NUMPAD3,
                Keyboard.KEY_NUMPAD4, Keyboard.KEY_NUMPAD5, Keyboard.KEY_NUMPAD6, 
                Keyboard.KEY_NUMPAD7, Keyboard.KEY_NUMPAD8, Keyboard.KEY_NUMPAD9};
        for (int i : hotbarKeys) {
            registerShortcutMapping(ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT, new InvTweaksShortcutMapping(i));
        }
        
    }
    
    private void registerShortcutMapping(ShortcutType type, InvTweaksShortcutMapping mapping) {
        // Register shortcut
        if (shortcuts.containsKey(type)) {
            shortcuts.get(type).add(mapping);
        }
        else {
            List<InvTweaksShortcutMapping> newMappingList = new LinkedList<InvTweaksShortcutMapping>();
            newMappingList.add(mapping);
            shortcuts.put(type, newMappingList);
        }
        // Register key status listeners
        for (int keyCode : mapping.getKeyCodes()) {
            shortcutKeysStatus.put(keyCode, false);
        }
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
    
    public void handleShortcut(ft guiContainer) {
        // IMPORTANT: This method is called before the default action is executed.
        
        updateKeyStatuses();
        
        // Initialization
        int ex = Mouse.getEventX(), ey = Mouse.getEventY();
        int x = (ex * getWidth(guiContainer)) / getDisplayWidth();
        int y = getHeight(guiContainer) - (ey * getHeight(guiContainer)) / getDisplayHeight() - 1;
        boolean shortcutValid = false;
        
        // Check that the slot is not empty
        wz slot = getSlotAtPosition(guiContainer, x, y);
        
        if (slot != null && hasStack(slot)) {

            InvTweaksContainerManager container = new InvTweaksContainerManager(mc);
           
            // Filter shortcuts to let Minecraft ones run
            if (container.getSlotSection(getSlotNumber(slot)) 
                    == InvTweaksContainerSection.CRAFTING_OUT
                   && (shortcutKeysStatus.get(Keyboard.KEY_LSHIFT)
                   || shortcutKeysStatus.get(Keyboard.KEY_RSHIFT))) {
                return;
            }
            
            // Choose shortcut type
            ShortcutType shortcutType = defaultAction;
            if (isActive(ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT) != null) {
                shortcutType = ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT;
                shortcutValid = true;
            } else if (isActive(ShortcutType.MOVE_ALL_ITEMS) != null) {
                shortcutType = ShortcutType.MOVE_ALL_ITEMS;
                shortcutValid = true;
            } else if (isActive(ShortcutType.MOVE_ONE_STACK) != null) {
                shortcutType = ShortcutType.MOVE_ONE_STACK;
                shortcutValid = true;
            }
            else if (isActive(ShortcutType.MOVE_ONE_ITEM) != null) {
                shortcutType = ShortcutType.MOVE_ONE_ITEM;
                shortcutValid = true;
            }
            
            // Choose target section
            try {
                InvTweaksContainerSection srcSection = container.getSlotSection(getSlotNumber(slot));
                InvTweaksContainerSection destSection = null;
                
                // Set up available sections
                Vector<InvTweaksContainerSection> availableSections = new Vector<InvTweaksContainerSection>();
                if (container.hasSection(InvTweaksContainerSection.CHEST)) {
                    availableSections.add(InvTweaksContainerSection.CHEST);
                }
                else if (container.hasSection(InvTweaksContainerSection.CRAFTING_IN)) {
                    availableSections.add(InvTweaksContainerSection.CRAFTING_IN);
                }
                else if (container.hasSection(InvTweaksContainerSection.FURNACE_IN)) {
                    availableSections.add(InvTweaksContainerSection.FURNACE_IN);
                }
                else if (container.hasSection(InvTweaksContainerSection.BREWING_INGREDIENT)) {
                	yq stack = container.getStack(slot);
                	if (stack != null) {
                		if (getItemID(stack) == 373 && getItemDamage(stack) == 0 /* Water Bottle */) {
                			availableSections.add(InvTweaksContainerSection.BREWING_BOTTLES);
                		}
                		else {
                			availableSections.add(InvTweaksContainerSection.BREWING_INGREDIENT);
                		}
                	}
                }
                else if (container.hasSection(InvTweaksContainerSection.ENCHANTMENT)) {
                    availableSections.add(InvTweaksContainerSection.ENCHANTMENT);
                }
                availableSections.add(InvTweaksContainerSection.INVENTORY_NOT_HOTBAR);
                availableSections.add(InvTweaksContainerSection.INVENTORY_HOTBAR);
                
                // Check for destination modifiers
                int destinationModifier = 0; 
                if (isActive(ShortcutType.MOVE_UP) != null
                        || defaultDestination == ShortcutType.MOVE_UP) {
                    destinationModifier = -1;
                }
                else if (isActive(ShortcutType.MOVE_DOWN) != null
                        || defaultDestination == ShortcutType.MOVE_DOWN) {
                    destinationModifier = 1;
                }
                
                if (destinationModifier == 0) {
                        switch (srcSection) {
                        case CHEST:
                            destSection = InvTweaksContainerSection.INVENTORY; break;
                        case INVENTORY_HOTBAR:
							if (availableSections.contains(InvTweaksContainerSection.CHEST)) {
								destSection = InvTweaksContainerSection.CHEST;
							} else {
								destSection = InvTweaksContainerSection.INVENTORY_NOT_HOTBAR;
							}
							break;
                        case CRAFTING_IN:
                        case FURNACE_IN:
                            destSection = InvTweaksContainerSection.INVENTORY_NOT_HOTBAR; break;
                        default:
                            if (availableSections.contains(InvTweaksContainerSection.CHEST)) {
                              destSection = InvTweaksContainerSection.CHEST;
                            } else {
                              destSection = InvTweaksContainerSection.INVENTORY_HOTBAR;
                            }
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
                        destSection = InvTweaksContainerSection.INVENTORY;
                    }
                }
                
                if (shortcutValid || isActive(ShortcutType.DROP) != null) {
                    
                    initAction(getSlotNumber(slot), shortcutType, destSection);
                    
                    if (shortcutType == ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT) {
                        // Move to specific hotbar slot
                    	InvTweaksShortcutMapping hotbarShortcut = isActive(ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT);
                    	if (hotbarShortcut != null && !hotbarShortcut.getKeyCodes().isEmpty()) {
                    		 String keyName = Keyboard.getKeyName(hotbarShortcut.getKeyCodes().get(0));
                             int destIndex = -1+Integer.parseInt(keyName.replace("NUMPAD", ""));
                             container.move(fromSection, fromIndex,
                                     InvTweaksContainerSection.INVENTORY_HOTBAR, destIndex);
                    	}
                        
                    } else {
                        // Drop or move
                        move(Mouse.isButtonDown(1), isActive(ShortcutType.DROP) != null);
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
                    wz slot = container.getSlot(fromSection, fromIndex);
                    if (fromSection != InvTweaksContainerSection.CRAFTING_OUT
                    		&& toSection != InvTweaksContainerSection.ENCHANTMENT) {
                        boolean canStillMove = true;
                        while (hasStack(slot) && toIndex != -1 && canStillMove) {
                            canStillMove = container.move(fromSection, fromIndex, toSection, toIndex);
                            toIndex = getNextIndex(separateStacks, drop);
                        }
                    }
                    else {
                        // Move only once, since the crafting output might be refilled
                        container.move(fromSection, fromIndex, toSection, toIndex);
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
                    yq stackToMatch = copy(fromStack);
                    moveAll(fromSection, toSection, separateStacks, drop, stackToMatch);
                    if (fromSection == InvTweaksContainerSection.INVENTORY_NOT_HOTBAR
                            && toSection == InvTweaksContainerSection.CHEST) {
                        moveAll(InvTweaksContainerSection.INVENTORY_HOTBAR, toSection, separateStacks, drop, stackToMatch);
                    }
                }
                    
                }
            }
            
        }
    }
    
    private void moveAll(InvTweaksContainerSection fromSection, InvTweaksContainerSection toSection, 
            boolean separateStacks, boolean drop, yq stackToMatch) throws TimeoutException {
        int toIndex = getNextIndex(separateStacks, drop);
        for (wz slot : container.getSlots(fromSection)) {
            if (hasStack(slot) && areSameItemType(stackToMatch, getStack(slot))) {
                int fromIndex = container.getSlotIndex(getSlotNumber(slot));
                boolean canStillMove = true;
                while (hasStack(slot) && toIndex != -1 && !(fromSection == toSection && fromIndex == toIndex) && canStillMove) {
                    canStillMove = container.move(fromSection, fromIndex, toSection, toIndex);
                    toIndex = getNextIndex(separateStacks, drop);
                }
            }
            if (toIndex == -1) {
                break;
            }
        }
    }

    /**
     * Checks if the Up/Down controls that are listened are outdated
     * @return true if the shortuts listeners have to be reset
     */
    private boolean haveControlsChanged() {
        return (!shortcutKeysStatus.containsKey(getKeyBindingForwardKeyCode())
                || !shortcutKeysStatus.containsKey(getKeyBindingBackKeyCode()));
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
            for (wz slot : container.getSlots(toSection)) {
                if (hasStack(slot)) {
                    yq stack = getStack(slot);
                    if (areItemsEqual(stack, fromStack)
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
        if (result == -1 && toSection == InvTweaksContainerSection.FURNACE_IN) {
            toSection =  InvTweaksContainerSection.FURNACE_FUEL;
            result = container.getFirstEmptyIndex(toSection);
        }
        
        return result;
    }

    /**
     * @param shortcutType
     * @return The shortcut that made the shortcut active, or null if inactive
     */
    private InvTweaksShortcutMapping isActive(ShortcutType shortcutType) {
        for (InvTweaksShortcutMapping mapping : shortcuts.get(shortcutType)) {
        	if (mapping.isTriggered(shortcutKeysStatus)) {
        		return mapping;
        	}
        }
        return null;
    }

    private void initAction(int fromSlot, ShortcutType shortcutType, InvTweaksContainerSection destSection) throws Exception {
        
        // Set up context
        this.container = new InvTweaksContainerManager(mc);
        this.fromSection = container.getSlotSection(fromSlot);
        this.fromIndex = container.getSlotIndex(fromSlot);
        this.fromStack = container.getItemStack(fromSection, fromIndex);
        this.shortcutType = shortcutType;
        this.toSection = destSection;

        // Put hold stack down
        if (getHoldStack() != null) {
            
            if (fromSection != InvTweaksContainerSection.CRAFTING_OUT) {
                container.leftClick(fromSection, fromIndex);
            }
            
            // Sometimes (ex: crafting/furnace output) we can't put back the item
            // in the slot, in that case choose a new one.
            if (getHoldStack() != null) {
                // TODO Merge with existing stack if possible
                int firstEmptyIndex = container.getFirstEmptyIndex(InvTweaksContainerSection.INVENTORY);
                if (firstEmptyIndex != -1) {
                   fromSlot = firstEmptyIndex;
                   container.leftClick(InvTweaksContainerSection.INVENTORY, fromSlot);
                }
                else {
                    throw new Exception("Couldn't put hold item down");
                }
            }
        }
    }
    
    private wz getSlotAtPosition(ft guiContainer, int i, int j) { 
        // Copied from class 'mg' (GuiContainer)
        for (int k = 0; k < getSlots(getContainer(guiContainer)).size(); k++) {
            wz localsx = (wz) getSlots(getContainer(guiContainer)).get(k);
            if (InvTweaks.getIsMouseOverSlot(guiContainer, localsx, i, j)) {
                return localsx;
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
