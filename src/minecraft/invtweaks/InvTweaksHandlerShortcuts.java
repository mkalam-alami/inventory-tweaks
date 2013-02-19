package invtweaks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import invtweaks.api.ContainerSection;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Slot;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;


/**
 *
 * @author Jimeo Wan
 *
 */
public class InvTweaksHandlerShortcuts extends InvTweaksObfuscation {

    private static final Logger log = Logger.getLogger("InvTweaks");

    private static final int DROP_SLOT = -999;

	private class ShortcutConfig {
        public InvTweaksShortcutType type = null;
	    public ContainerSection fromSection = null;
        public int fromIndex = -1;
        public ItemStack fromStack = null;
        public ContainerSection toSection = null;
        public int toIndex = -1;
        public boolean drop = false;
        public boolean forceEmptySlot = false;
	}

	private InvTweaksConfig config;

    private InvTweaksContainerManager container;

    /**
     * Stores all pressed keys (only the one that are related to shortcuts)
     */
    private Map<Integer, Boolean> pressedKeys;

    /**
     * Stores the shortcuts mappings
     */
    private Map<InvTweaksShortcutType, List<InvTweaksShortcutMapping>> shortcuts;


    public InvTweaksHandlerShortcuts(Minecraft mc, InvTweaksConfig config) {
		super(mc);
		this.config = config;
		this.pressedKeys = new HashMap<Integer, Boolean>();
		this.shortcuts = new HashMap<InvTweaksShortcutType, List<InvTweaksShortcutMapping>>();
	}

	public void loadShortcuts() {
		pressedKeys.clear();
		shortcuts.clear();

        // Register shortcut mappings
        Map<String, String> keys = config.getProperties(
                InvTweaksConfig.PROP_SHORTCUT_PREFIX);
        for (String key : keys.keySet()) {
            String[] keyMappings = keys.get(key).split("[ ]*,[ ]*");
            InvTweaksShortcutType shortcutType = InvTweaksShortcutType.fromConfigKey(key);
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

        registerShortcutMapping(InvTweaksShortcutType.MOVE_UP, new InvTweaksShortcutMapping(upKeyCode));
        registerShortcutMapping(InvTweaksShortcutType.MOVE_DOWN, new InvTweaksShortcutMapping(downKeyCode));

        // Add hotbar shortcuts (1-9) mappings
        int[] hotbarKeys = {Keyboard.KEY_1, Keyboard.KEY_2, Keyboard.KEY_3,
                Keyboard.KEY_4, Keyboard.KEY_5, Keyboard.KEY_6,
                Keyboard.KEY_7, Keyboard.KEY_8, Keyboard.KEY_9,
                Keyboard.KEY_NUMPAD1, Keyboard.KEY_NUMPAD2, Keyboard.KEY_NUMPAD3,
                Keyboard.KEY_NUMPAD4, Keyboard.KEY_NUMPAD5, Keyboard.KEY_NUMPAD6,
                Keyboard.KEY_NUMPAD7, Keyboard.KEY_NUMPAD8, Keyboard.KEY_NUMPAD9};
        for (int i : hotbarKeys) {
            registerShortcutMapping(InvTweaksShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT, new InvTweaksShortcutMapping(i));
        }

        // Register (L/R)SHIFT to allow to filter them
        pressedKeys.put(Keyboard.KEY_LSHIFT, false);
        pressedKeys.put(Keyboard.KEY_RSHIFT, false);
	}

	private void registerShortcutMapping(InvTweaksShortcutType type, InvTweaksShortcutMapping mapping) {
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
        	pressedKeys.put(keyCode, false);
        }
    }

    public void handleShortcut() {
        try {
    		// Init shortcut
    		ShortcutConfig shortcutToTrigger = computeShortcutToTrigger();
    		if (shortcutToTrigger != null) {
    	        int ex = Mouse.getEventX(), ey = Mouse.getEventY();

                // GO!
                runShortcut(shortcutToTrigger);

                // Reset mouse status to prevent default action.
                // TODO Find a better solution, like 'anticipate' default action?
                Mouse.destroy();
                Mouse.create();

                // Fixes a tiny glitch (Steve looks for a short moment
                // at [0, 0] because of the mouse reset).
                Mouse.setCursorPosition(ex, ey);
    		}

		} catch (Exception e) {
            InvTweaks.logInGameErrorStatic("invtweaks.shortcut.error", e);
        }
    }

    public ShortcutConfig computeShortcutToTrigger() {
        updatePressedKeys();

        // Init
        container = new InvTweaksContainerManager(mc);
        Slot slot = container.getSlotAtMousePosition();
        ShortcutConfig shortcutConfig = new ShortcutConfig();

        // If a valid and not empty slot is clicked
        if (slot != null && (hasStack(slot) || getHeldStack() != null)) {
            int slotNumber = getSlotNumber(slot);

            // Set shortcut origin
            shortcutConfig.fromSection = container.getSlotSection(slotNumber);
            shortcutConfig.fromIndex = container.getSlotIndex(slotNumber);
            shortcutConfig.fromStack = (hasStack(slot)) ? copy(getStack(slot)) : copy(getHeldStack());

            // Compute shortcut type
            if (isShortcutDown(InvTweaksShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT) != null) {
                shortcutConfig.type = InvTweaksShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT;
            } else if (isShortcutDown(InvTweaksShortcutType.MOVE_ALL_ITEMS) != null) {
                shortcutConfig.type = InvTweaksShortcutType.MOVE_ALL_ITEMS;
            } else if (isShortcutDown(InvTweaksShortcutType.MOVE_EVERYTHING) != null) {
                shortcutConfig.type = InvTweaksShortcutType.MOVE_EVERYTHING;
            } else if (isShortcutDown(InvTweaksShortcutType.MOVE_ONE_ITEM) != null) {
                shortcutConfig.type = InvTweaksShortcutType.MOVE_ONE_ITEM;
            } else if (shortcutConfig.type == null
                    && (isShortcutDown(InvTweaksShortcutType.MOVE_UP) != null
                        || isShortcutDown(InvTweaksShortcutType.MOVE_DOWN) != null
                        || isShortcutDown(InvTweaksShortcutType.DROP) != null)) {
                shortcutConfig.type = InvTweaksShortcutType.MOVE_ONE_STACK;
            }

            // (Special case: can't move 1 item from crafting output)
            // TODO Better mod compat by testing slot class to make sure the 'move one' shortcut works
            if (shortcutConfig.fromSection == ContainerSection.CRAFTING_OUT
                    && shortcutConfig.type == InvTweaksShortcutType.MOVE_ONE_ITEM) {
                shortcutConfig.type = InvTweaksShortcutType.MOVE_ONE_STACK;
            }

            if (shortcutConfig.fromSection != null && shortcutConfig.fromIndex != -1 && shortcutConfig.type != null) {

                // Compute shortcut target
                if (shortcutConfig.type == InvTweaksShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT) {
                    shortcutConfig.toSection = ContainerSection.INVENTORY_HOTBAR;
                    InvTweaksShortcutMapping hotbarShortcut = isShortcutDown(InvTweaksShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT);
                    if (hotbarShortcut != null && !hotbarShortcut.getKeyCodes().isEmpty()) {
                         String keyName = Keyboard.getKeyName(hotbarShortcut.getKeyCodes().get(0));
                         shortcutConfig.toIndex = -1 + Integer.parseInt(keyName.replace("NUMPAD", ""));
                    }
                }
                else {
                    // Compute targetable sections in order
                    Vector<ContainerSection> orderedSections = new Vector<ContainerSection>();

                    // (Top part)
                    if (container.hasSection(ContainerSection.CHEST)) {
                        orderedSections.add(ContainerSection.CHEST);
                    }
                    else if (container.hasSection(ContainerSection.CRAFTING_IN)) {
                        orderedSections.add(ContainerSection.CRAFTING_IN);
                    }
                    else if (container.hasSection(ContainerSection.CRAFTING_IN_PERSISTENT)) {
                        orderedSections.add(ContainerSection.CRAFTING_IN_PERSISTENT);
                    }
                    else if (container.hasSection(ContainerSection.FURNACE_IN)) {
                        orderedSections.add(ContainerSection.FURNACE_IN);
                    }
                    else if (container.hasSection(ContainerSection.BREWING_INGREDIENT)) {
                        ItemStack stack = container.getStack(slot);
                        if (stack != null) {
                            if (getItemID(stack) == 373 /* Water Bottle/Potions */) {
                                orderedSections.add(ContainerSection.BREWING_BOTTLES);
                            }
                            else {
                                orderedSections.add(ContainerSection.BREWING_INGREDIENT);
                            }
                        }
                    }
                    else if (container.hasSection(ContainerSection.ENCHANTMENT)) {
                        orderedSections.add(ContainerSection.ENCHANTMENT);
                    }

                    // (Inventory part)
                    orderedSections.add(ContainerSection.INVENTORY_NOT_HOTBAR);
                    orderedSections.add(ContainerSection.INVENTORY_HOTBAR);

                    // Choose target section
                    boolean upMapping = isShortcutDown(InvTweaksShortcutType.MOVE_UP) != null,
                            downMapping = isShortcutDown(InvTweaksShortcutType.MOVE_DOWN) != null;
                    if (upMapping || downMapping) { // Explicit section (up/down shortcuts)
                        int sectionOffset = 0;
                        if (upMapping) {
                            sectionOffset--;
                        }
                        if (downMapping) {
                            sectionOffset++;
                        }
                        int fromSectionIndex = orderedSections.indexOf(shortcutConfig.fromSection);
                        if (fromSectionIndex != -1) {
                            shortcutConfig.toSection = orderedSections.get(
                                    (orderedSections.size() + fromSectionIndex + sectionOffset) % orderedSections.size());
                        }
                        else {
                            shortcutConfig.toSection = ContainerSection.INVENTORY;
                        }
                    }
                    else { // Implicit section
                        switch (shortcutConfig.fromSection) {
                        case CHEST:
                            shortcutConfig.toSection = ContainerSection.INVENTORY; break;
                        case INVENTORY_HOTBAR:
                            if (orderedSections.contains(ContainerSection.CHEST)) {
                                shortcutConfig.toSection = ContainerSection.CHEST;
                            } else {
                                shortcutConfig.toSection = ContainerSection.INVENTORY_NOT_HOTBAR;
                            }
                            break;
                        case CRAFTING_IN:
                        case CRAFTING_IN_PERSISTENT:
                        case FURNACE_IN:
                            shortcutConfig.toSection = ContainerSection.INVENTORY_NOT_HOTBAR; break;
                        default:
                            if (orderedSections.contains(ContainerSection.CHEST)) {
                                shortcutConfig.toSection = ContainerSection.CHEST;
                            } else {
                                shortcutConfig.toSection = ContainerSection.INVENTORY_HOTBAR;
                            }
                        }
                    }
                }

                // Shortcut modifiers
                shortcutConfig.forceEmptySlot = Mouse.isButtonDown(1);
                shortcutConfig.drop = isShortcutDown(InvTweaksShortcutType.DROP) != null;

                return shortcutConfig;

            }
        }

        return null;
    }

    // XXX Bad API
    public void updatePressedKeys() {
        if (haveControlsChanged()) {
            loadShortcuts(); // Reset mappings
        }
        for (int keyCode : pressedKeys.keySet()) {
            if (keyCode > 0 && Keyboard.isKeyDown(keyCode)) {
                if (!pressedKeys.get(keyCode)) {
                    pressedKeys.put(keyCode, true);
                }
            }
            else {
                pressedKeys.put(keyCode, false);
            }
        }
    }

    /**
     * Checks if the Up/Down controls that are listened are outdated
     * @return true if the shortuts listeners have to be reset
     */
    private boolean haveControlsChanged() {
        return (!pressedKeys.containsKey(getKeyBindingForwardKeyCode())
                || !pressedKeys.containsKey(getKeyBindingBackKeyCode()));
    }

    private void runShortcut(ShortcutConfig shortcut) throws TimeoutException {
        // Try to put held item down
        if (getHeldStack() != null) {
            Slot slot = container.getSlotAtMousePosition();
            if (slot != null) {
                int slotNumber = getSlotNumber(slot);
                container.putHoldItemDown(container.getSlotSection(slotNumber), container.getSlotIndex(slotNumber));
                if (getHeldStack() != null) {
                    return;
                }
            }
            else {
                return;
            }
        }

        synchronized(this) {
            if (shortcut.type == InvTweaksShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT) {
                container.move(shortcut.fromSection, shortcut.fromIndex, shortcut.toSection, shortcut.toIndex);
            }
            else {

                int toIndex = getNextTargetIndex(shortcut);
                boolean success;
                int newIndex;

                if (toIndex != -1) {
                    switch (shortcut.type) {

                    case MOVE_ONE_STACK:
                    {
                        Slot slot = container.getSlot(shortcut.fromSection, shortcut.fromIndex);
                        if (shortcut.fromSection != ContainerSection.CRAFTING_OUT
                                && shortcut.toSection != ContainerSection.ENCHANTMENT) {
                            while (hasStack(slot) && toIndex != -1) {
                                success = container.move(shortcut.fromSection, shortcut.fromIndex, shortcut.toSection, toIndex);
                                newIndex = getNextTargetIndex(shortcut);
                                toIndex = (success || shortcut.drop || newIndex != toIndex) ? newIndex : -1; // Needed when we can't put items in the target slot
                            }
                        }
                        else {
                            // Move only once, since the crafting output might be refilled
                            container.move(shortcut.fromSection, shortcut.fromIndex, shortcut.toSection, toIndex);
                        }
                        break;

                    }

                    case MOVE_ONE_ITEM:
                    {
                        container.moveSome(shortcut.fromSection, shortcut.fromIndex, shortcut.toSection, toIndex, 1);
                        break;
                    }

                    case MOVE_ALL_ITEMS:
                    {
                        moveAll(shortcut, shortcut.fromStack);
                        if (shortcut.fromSection == ContainerSection.INVENTORY_NOT_HOTBAR
                                && shortcut.toSection == ContainerSection.CHEST) {
                            shortcut.fromSection = ContainerSection.INVENTORY_HOTBAR;
                            moveAll(shortcut, shortcut.fromStack);
                        }
                        break;
                    }

                    case MOVE_EVERYTHING:
                    {
                        moveAll(shortcut, null);
                        if (shortcut.fromSection == ContainerSection.INVENTORY_HOTBAR
                                && shortcut.toSection == ContainerSection.CHEST) {
                            shortcut.fromSection = ContainerSection.INVENTORY_HOTBAR;
                            moveAll(shortcut, null);
                        }
                        break;
                    }

                    default:

                    }
                }

            }

        }
    }

    private void moveAll(ShortcutConfig shortcut, ItemStack stackToMatch) throws TimeoutException {
        int toIndex = getNextTargetIndex(shortcut), newIndex;
        boolean success;
        for (Slot slot : container.getSlots(shortcut.fromSection)) {
            if (hasStack(slot) && (stackToMatch == null || areSameItemType(stackToMatch, getStack(slot)))) {
                int fromIndex = container.getSlotIndex(getSlotNumber(slot));
                while (hasStack(slot) && toIndex != -1 && !(shortcut.fromSection == shortcut.toSection && fromIndex == toIndex)) {
                    success = container.move(shortcut.fromSection, fromIndex, shortcut.toSection, toIndex);
                    newIndex = getNextTargetIndex(shortcut);
                    toIndex = (success || shortcut.drop || newIndex != toIndex) ? newIndex : -1; // Needed when we can't put items in the target slot
                }
            }
            if (toIndex == -1) {
                break;
            }
        }
    }
    private int getNextTargetIndex(ShortcutConfig shortcut) {

        if (shortcut.drop) {
            return DROP_SLOT;
        }

        int result = -1;

        // Try to merge with existing slot
        if (!shortcut.forceEmptySlot) {
            int i = 0;
            for (Slot slot : container.getSlots(shortcut.toSection)) {
                if (hasStack(slot)) {
                    ItemStack stack = getStack(slot);
                    if (!hasDataTags(stack) && areItemsEqual(stack, shortcut.fromStack)
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
            result = container.getFirstEmptyIndex(shortcut.toSection);
        }

        // Switch from FURNACE_IN to FURNACE_FUEL if the slot is taken
        // TODO Better furnace shortcuts
        if (result == -1 && shortcut.toSection == ContainerSection.FURNACE_IN) {
            shortcut.toSection = ContainerSection.FURNACE_FUEL;
            result = container.getFirstEmptyIndex(shortcut.toSection);
        }

        return result;
    }
    /**
     * Checks if shortcut has been triggered
     * @return The mapping that triggered the shortcut
     */
    public InvTweaksShortcutMapping isShortcutDown(InvTweaksShortcutType type) {
    	List<InvTweaksShortcutMapping> mappings = shortcuts.get(type);
    	if (mappings != null) {
    		for (InvTweaksShortcutMapping mapping : mappings) {
    			if (mapping.isTriggered(pressedKeys)) {
    				return mapping;
    			}
    		}
    	}
    	return null;
    }

}
