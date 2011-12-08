import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;


/**
 * Main class for Inventory Tweaks, which maintains various hooks
 * and dispatches the events to the correct handlers.
 * 
 * @author Jimeo Wan
 *
 * Contact: jimeo.wan (at) gmail (dot) com
 * Website: {@link http://wan.ka.free.fr/?invtweaks}
 * Source code: {@link https://github.com/jimeowan/inventory-tweaks}
 * License: MIT
 * 
 */
public class InvTweaks extends InvTweaksObfuscation {

    private static final Logger log = Logger.getLogger("InvTweaks");

    private static InvTweaks instance;

    /**
     * The configuration loader.
     */
    private InvTweaksConfigManager cfgManager = null;
    
    private InvTweaksModCompatibility modCompatibility = InvTweaksModCompatibility.getInstance();
    
    /**
     * Attributes to remember the status of chest sorting
     * while using middle clicks.
     */
    private int chestAlgorithm = InvTweaksHandlerSorting.ALGORITHM_DEFAULT;
    private long chestAlgorithmClickTimestamp = 0;
    private boolean chestAlgorithmButtonDown = false;
    
    /**
     * Various information concerning the context, stored on
     * each tick to allow for certain features (auto-refill,
     * sorting on pick up...)
     */
    private int storedStackId = 0, storedStackDamage = -1, storedFocusedSlot = -1;
    private dk[] hotbarClone = new dk[InvTweaksConst.INVENTORY_HOTBAR_SIZE];
    private boolean mouseWasInWindow = true, mouseWasDown = false;;
    
    /**
     * Allows to trigger some logic only every Const.POLLING_DELAY.
     */
    private int tickNumber = 0, lastPollingTickNumber = -InvTweaksConst.POLLING_DELAY;
    
    /**
     * Creates an instance of the mod, and loads the configuration
     * from the files, creating them if necessary.
     * @param mc
     */
    public InvTweaks(Minecraft mc) {
        super(mc);

        log.setLevel(InvTweaksConst.DEFAULT_LOG_LEVEL);

        // Store instance
        instance = this;

        // Load config files
        cfgManager = new InvTweaksConfigManager(mc);
        if (cfgManager.makeSureConfigurationIsLoaded()) {
            log.info("Mod initialized");
        } else {
            log.severe("Mod failed to initialize!");
        }
        

    }

    /**
     * To be called every time the sorting key is pressed.
     * Sorts the inventory.
     */
    public final void onSortingKeyPressed() {
        synchronized (this) {
            
            // Check config loading success
            if (!cfgManager.makeSureConfigurationIsLoaded()) {
                return;
            }
            
            // Check current GUI
            xe guiScreen = getCurrentScreen();
            if (guiScreen == null || (isValidChest(guiScreen) || isValidInventory(guiScreen))) {
                // Sorting!
                handleSorting(guiScreen);
            }
        }
    }

    /**
     * To be called everytime a stack has been picked up.
     * Moves the picked up item in another slot that matches best the current configuration.
     */
    public void onItemPickup() {

        if (!cfgManager.makeSureConfigurationIsLoaded()) {
            return;
        }
        InvTweaksConfig config = cfgManager.getConfig();
        // Handle option to disable this feature
        if (cfgManager.getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP).equals("false")) {
            return;
        }

        try {
            InvTweaksContainerSectionManager containerMgr = new InvTweaksContainerSectionManager(mc, InvTweaksContainerSection.INVENTORY);

            // Find stack slot (look in hotbar only).
            // We're looking for a brand new stack in the hotbar
            // (not an existing stack whose amount has been increased)
            int currentSlot = -1;
            do {
                // In SMP, we have to wait first for the inventory update
                if (isMultiplayerWorld() && currentSlot == -1) {
                    try {
                        Thread.sleep(InvTweaksConst.POLLING_DELAY);
                    } catch (InterruptedException e) {
                        // Do nothing (sleep interrupted)
                    }
                }
                for (int i = 0; i < InvTweaksConst.INVENTORY_HOTBAR_SIZE; i++) {
                    dk currentHotbarStack = containerMgr.getItemStack(i + 27);
                    // Don't move already started stacks
                    if (currentHotbarStack != null && getAnimationsToGo(currentHotbarStack) == 5 && hotbarClone[i] == null) {
                        currentSlot = i + 27;
                    }
                }

                // The loop is only relevant in SMP (polling)
            } while (isMultiplayerWorld() && currentSlot == -1);

            if (currentSlot != -1) {

                // Find preffered slots
                List<Integer> prefferedPositions = new LinkedList<Integer>();
                InvTweaksItemTree tree = config.getTree();
                dk stack = containerMgr.getItemStack(currentSlot);
                List<InvTweaksItemTreeItem> items = tree.getItems(getItemID(stack),
                        getItemDamage(stack));
                for (InvTweaksConfigSortingRule rule : config.getRules()) {
                    if (tree.matches(items, rule.getKeyword())) {
                        for (int slot : rule.getPreferredSlots()) {
                            prefferedPositions.add(slot);
                        }
                    }
                }

                // Find best slot for stack
                boolean hasToBeMoved = true;
                if (prefferedPositions != null) {
                    for (int newSlot : prefferedPositions) {
                        try {
                            // Already in the best slot!
                            if (newSlot == currentSlot) {
                                hasToBeMoved = false;
                                break;
                            }
                            // Is the slot available?
                            else if (containerMgr.getItemStack(newSlot) == null) {
                                // TODO: Check rule level before to move
                                if (containerMgr.move(currentSlot, newSlot)) {
                                    break;
                                }
                            }
                        } catch (TimeoutException e) {
                            logInGameError("Failed to move picked up stack", e);
                        }
                    }
                }

                // Else, put the slot anywhere
                if (hasToBeMoved) {
                    for (int i = 0; i < containerMgr.getSize(); i++) {
                        if (containerMgr.getItemStack(i) == null) {
                            if (containerMgr.move(currentSlot, i)) {
                                break;
                            }
                        }
                    }
                }

            }
            
        } catch (Exception e) {
            logInGameError("Failed to move picked up stack", e);
        }
    }

    /**
     * To be called on each tick during the game (except when in a menu).
     * Handles the auto-refill.
     */
    public void onTickInGame() {
        synchronized (this) {
            if (!onTick()) {
                return;
            }
            handleAutoRefill();
        }
    }
    
    /**
     * To be called on each tick when a menu is open.
     * Handles the GUI additions and the middle clicking.
     * @param guiScreen
     */
    public void onTickInGUI(xe guiScreen) {
        synchronized (this) {
            if (!onTick()) {
                return;
            }
            if (isTimeForPolling()) {
                unlockKeysIfNecessary();
            }
            handleGUILayout(guiScreen);
            handleMiddleClick(guiScreen);
            handleShortcuts(guiScreen);
        }
    }

    public void logInGame(String message) {
        String formattedMsg = buildlogString(Level.INFO, message);
        addChatMessage(formattedMsg);
        log.info(formattedMsg);
    }

    public void logInGameError(String message, Exception e) {
        String formattedMsg = buildlogString(Level.SEVERE, message, e);
        addChatMessage(formattedMsg);
        log.severe(formattedMsg);
    }

    public static void logInGameStatic(String message) {
        InvTweaks.getInstance().logInGame(message);
    }

    public static void logInGameErrorStatic(String message, Exception e) {
        InvTweaks.getInstance().logInGameError(message, e);
    }

    /**
     * Returns the mods single instance.
     * @return
     */
    public static InvTweaks getInstance() {
        return instance;
    }

    // Used by ShortcutsHandler only, but put here for convenience and 
    // performance, since the xSize/ySize attributes are protected
    public static boolean getIsMouseOverSlot(mg guiContainer, vv slot, int i, int j) { // Copied from GuiContainer
        // Copied from GuiContainer
        InvTweaks obf = getInstance();
        int k = (obf.getWidth(guiContainer) - obf.getXSize(guiContainer)) / 2;
        int l = (obf.getHeight(guiContainer) - obf.getYSize(guiContainer)) / 2;
        i -= k;
        j -= l;
        return i >= obf.getXDisplayPosition(slot) - 1 && i < obf.getXDisplayPosition(slot) + 16 + 1 && j >= obf.getYDisplayPosition(slot) - 1 && j < obf.getYDisplayPosition(slot) + 16 + 1;
    }

    private boolean onTick() {

        tickNumber++;
        
        // Not calling "cfgManager.makeSureConfigurationIsLoaded()" for performance reasons
        InvTweaksConfig config = cfgManager.getConfig();
        if (config == null) { 
            return false;
        }
        
        // Clone the hotbar to be able to monitor changes on it
        xe currentScreen = getCurrentScreen();
        if (currentScreen == null || isGuiInventory(currentScreen)) {
            cloneHotbar();
        }

        return true;

    }

    private void handleSorting(xe guiScreen) {

        dk selectedItem = getMainInventory()[getFocusedSlot()];

        // Sorting
        try {
            new InvTweaksHandlerSorting(mc, guiScreen, cfgManager.getConfig(),
                    InvTweaksContainerSection.INVENTORY,
                    InvTweaksHandlerSorting.ALGORITHM_INVENTORY,
                    InvTweaksConst.INVENTORY_ROW_SIZE).sort();
        } catch (Exception e) {
            logInGame("Failed to sort inventory: " + e.getMessage());
            e.printStackTrace();
        }

        playClick();

        // This needs to be remembered so that the
        // auto-refill feature doesn't trigger
        if (selectedItem != null && getMainInventory()[getFocusedSlot()] == null) {
            storedStackId = 0;
        }

    }

    private void handleAutoRefill() {
    
        dk currentStack = getFocusedStack();
        int currentStackId = (currentStack == null) ? 0 : getItemID(currentStack);
        int currentStackDamage = (currentStack == null) ? 0 : getItemDamage(currentStack);
        int focusedSlot = getFocusedSlot() + 27; // Convert to container slots index
        InvTweaksConfig config = cfgManager.getConfig();
        
        if (currentStackId != storedStackId || currentStackDamage != storedStackDamage) {
    
            if (storedFocusedSlot != focusedSlot) { // Filter selection change
                storedFocusedSlot = focusedSlot;
            } else if ((currentStack == null || getItemID(currentStack) == 281 && storedStackId == 282)  // Handle eaten mushroom soup
                    && (getCurrentScreen() == null || // Filter open inventory or other window
                    isGuiEditSign(getCurrentScreen()))) {
    
                if (config.isAutoRefillEnabled(storedStackId, storedStackId)) {
                    try {
                        cfgManager.getAutoRefillHandler().autoRefillSlot(focusedSlot, storedStackId, storedStackDamage);
                    } catch (Exception e) {
                        logInGameError("Failed to trigger auto-refill", e);
                    }
                }
            }
        }
    
        storedStackId = currentStackId;
        storedStackDamage = currentStackDamage;
    
    }

    private void handleMiddleClick(xe guiScreen) {
    
        if (Mouse.isButtonDown(2)) {
    
            if (!cfgManager.makeSureConfigurationIsLoaded()) {
                return;
            }
            InvTweaksConfig config = cfgManager.getConfig();
    
            // Check that middle click sorting is allowed
            if (config.getProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK)
                    .equals(InvTweaksConfig.VALUE_TRUE)) {
    
                if (!chestAlgorithmButtonDown) {
                    chestAlgorithmButtonDown = true;
    
                    if (isValidChest(guiScreen)) {
    
                        // Check if the middle click target the chest or the inventory
                        // (copied GuiContainer.getSlotAtPosition algorithm)
                        mg guiContainer = (mg) guiScreen;
                        pj container = getContainer((mg) guiScreen);
                        int slotCount = getSlots(container).size();
                        int mouseX = (Mouse.getEventX() * getWidth(guiContainer)) / getDisplayWidth();
                        int mouseY = getHeight(guiContainer) - (Mouse.getEventY() * getHeight(guiContainer)) / getDisplayHeight() - 1;
                        int target = 0; // 0 = nothing, 1 = chest, 2 = inventory
                        for (int i = 0; i < slotCount; i++) {
                            vv slot = getSlot(container, i);
                            int k = (getWidth(guiContainer) - getXSize(guiContainer)) / 2;
                            int l = (getHeight(guiContainer) - getYSize(guiContainer)) / 2;
                            if (mouseX - k >= getXDisplayPosition(slot) - 1 &&
                                    mouseX - k < getXDisplayPosition(slot) + 16 + 1 &&
                                    mouseY - l >= getYDisplayPosition(slot) - 1 &&
                                    mouseY - l < getYDisplayPosition(slot) + 16 + 1) {
                                target = (i < slotCount - InvTweaksConst.INVENTORY_SIZE) ? 1 : 2;
                                break;
                            }
                        }
    
                        if (target == 1) {
    
                            // Play click
                            playSoundAtEntity(getTheWorld(), getThePlayer(), "random.click", 0.2F, 1.8F);
    
                            long timestamp = System.currentTimeMillis();
                            if (timestamp - chestAlgorithmClickTimestamp > 
                                    InvTweaksConst.CHEST_ALGORITHM_SWAP_MAX_INTERVAL) {
                                chestAlgorithm = InvTweaksHandlerSorting.ALGORITHM_DEFAULT;
                            }
                            try {
                                new InvTweaksHandlerSorting(mc, guiScreen, cfgManager.getConfig(),
                                        InvTweaksContainerSection.CHEST,
                                        chestAlgorithm,
                                        getContainerRowSize(guiContainer)).sort();
                            } catch (Exception e) {
                                logInGameError("Failed to sort container", e);
                                e.printStackTrace();
                            }
                            chestAlgorithm = (chestAlgorithm + 1) % 3;
                            chestAlgorithmClickTimestamp = timestamp;
                        } else if (target == 2) {
                            handleSorting(guiScreen);
                        }
    
                    } else if (isGuiInventory(guiScreen) || isGuiWorkbench(guiScreen)) {
                        handleSorting(guiScreen);
                    }
                }
            }
        } else {
            chestAlgorithmButtonDown = false;
        }
    }

    private void handleGUILayout(xe guiScreen) {

        InvTweaksConfig config = cfgManager.getConfig();
        boolean isValidChest = isValidChest(guiScreen);

        if (isValidChest || isValidInventory(guiScreen)) {

            int w = 10, h = 10;

            // Look for the mods buttons
            boolean customButtonsAdded = false;
            List<Object> controlList = getControlList(guiScreen);
            for (Object o : controlList) {
                if (isGuiButton(o)) {
                    ct button = (ct) o;
                    if (getId(button) == InvTweaksConst.JIMEOWAN_ID) {
                        customButtonsAdded = true;
                        break;
                    }
                }
            }

            if (!customButtonsAdded) {

                // Inventory button
                if (!isValidChest) {
                    controlList.add(new InvTweaksGuiInventorySettingsButton(
                            cfgManager, InvTweaksConst.JIMEOWAN_ID,
                            getWidth(guiScreen) / 2 + 73, getHeight(guiScreen) / 2 - 78,
                            w, h, "...", "Inventory settings"));
                }

                // Chest buttons
                else {
                    
                    // Reset sorting algorithm selector
                    chestAlgorithmClickTimestamp = 0;

                    mg guiContainer = (mg) guiScreen;
                    int id = InvTweaksConst.JIMEOWAN_ID,
                        x = getXSize(guiContainer) / 2 + getWidth(guiContainer) / 2 - 17,
                        y = (getHeight(guiContainer) - getYSize(guiContainer)) / 2 + 5;

                    // Settings button
                    controlList.add(new InvTweaksGuiInventorySettingsButton(
                            cfgManager, id++, 
                            x - 1, y, w, h, "...", "Inventory settings"));

                    // Sorting buttons
                    if (!config.getProperty(InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS).equals("false")) {

                        int rowSize = getContainerRowSize(guiContainer);
                        
                        InvTweaksObfuscationGuiButton button = new InvTweaksGuiSortingButton(
                                cfgManager, id++,
                                x - 13, y, w, h, "h", "Sort in rows",
                                InvTweaksHandlerSorting.ALGORITHM_HORIZONTAL,
                                rowSize,guiContainer);
                        controlList.add(button);

                        button = new InvTweaksGuiSortingButton(
                                cfgManager, id++,
                                x - 25, y, w, h, "v", "Sort in columns",
                                InvTweaksHandlerSorting.ALGORITHM_VERTICAL,
                                rowSize,guiContainer);
                        controlList.add(button);

                        button = new InvTweaksGuiSortingButton(
                                cfgManager, id++,
                                x - 37, y, w, h, "s", "Default sorting",
                                InvTweaksHandlerSorting.ALGORITHM_DEFAULT,
                                rowSize,guiContainer);
                        controlList.add(button);

                    }
                }
            }
        }

    }
    
    private void handleShortcuts(xe guiScreen) {
/*        
        // Check open GUI
        if (!(isValidChest(guiScreen) || isValidInventory(guiScreen))) {
            return;
        }
*/        
        // Configurable shortcuts
        if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
            if (!mouseWasDown) {
                mouseWasDown = true;
                
                // The mouse has just been clicked,
                // trigger a shortcut according to the pressed keys.
                if (cfgManager.getConfig().getProperty(
                        InvTweaksConfig.PROP_ENABLE_SHORTCUTS).equals("true")) {
                    cfgManager.getShortcutsHandler().handleShortcut(guiScreen);
                }
            }
        }
        else {
            mouseWasDown = false;
        }
        
        // Switch between configurations
        InvTweaksConfig config = cfgManager.getConfig();
        Vector<Integer> downKeys = cfgManager.getShortcutsHandler().getDownShortcutKeys();
        if (isSortingShortcutDown()) {
            for (int downKey : downKeys) {
                String newRuleset = null;
            	if (downKey >= Keyboard.KEY_1 && downKey <= Keyboard.KEY_9) {
            		newRuleset = config.switchConfig(downKey - Keyboard.KEY_1);
            	}
            	else {
            		switch (downKey) {
                    case Keyboard.KEY_NUMPAD1: newRuleset = config.switchConfig(0); break;
                    case Keyboard.KEY_NUMPAD2: newRuleset = config.switchConfig(1); break;
                    case Keyboard.KEY_NUMPAD3: newRuleset = config.switchConfig(2); break;
                    case Keyboard.KEY_NUMPAD4: newRuleset = config.switchConfig(3); break;
                    case Keyboard.KEY_NUMPAD5: newRuleset = config.switchConfig(4); break;
                    case Keyboard.KEY_NUMPAD6: newRuleset = config.switchConfig(5); break;
                    case Keyboard.KEY_NUMPAD7: newRuleset = config.switchConfig(6); break;
                    case Keyboard.KEY_NUMPAD8: newRuleset = config.switchConfig(7); break;
                    case Keyboard.KEY_NUMPAD9: newRuleset = config.switchConfig(8); break;
            		}
            	}
            	if (downKey >= Keyboard.KEY_NUMPAD1 && downKey <= Keyboard.KEY_NUMPAD9) {
            		newRuleset = config.switchConfig(downKey - Keyboard.KEY_NUMPAD1 + 1);
            	}
                
                if (newRuleset != null) {
                    logInGame("'" + newRuleset + "' enabled");
                }
            }
        }
        
        
    }

    private int getContainerRowSize(mg guiContainer) {
        if (isGuiChest(guiContainer)) {
            return InvTweaksConst.CHEST_ROW_SIZE;
        }
        else if (isGuiDispenser(guiContainer)) {
            return InvTweaksConst.DISPENSER_ROW_SIZE;
        }
        else {
            return modCompatibility.getSpecialChestRowSize(
                    guiContainer, InvTweaksConst.CHEST_ROW_SIZE);
        }
    }

    private boolean isSortingShortcutDown() {
    	int keyCode = getKeyCode(InvTweaksConst.SORT_KEY_BINDING);
    	if (keyCode > 0) {
    		return Keyboard.isKeyDown(keyCode);
    	}
    	else {
    		return Mouse.isButtonDown(100 + keyCode);
    	}
	}

    private boolean isTimeForPolling() {
        if (tickNumber - lastPollingTickNumber >= InvTweaksConst.POLLING_DELAY) {
            lastPollingTickNumber = tickNumber;
        }
        return tickNumber - lastPollingTickNumber == 0;
    }

    /**
     * When the mouse gets inside the window, reset pressed keys
     * to avoid the "stuck keys" bug.
     */
    private void unlockKeysIfNecessary() {
        boolean mouseInWindow = Mouse.isInsideWindow();
        if (!mouseWasInWindow && mouseInWindow) {
            Keyboard.destroy();
            boolean firstTry = true;
            while (!Keyboard.isCreated()) {
                try {
                    Keyboard.create();
                } catch (LWJGLException e) {
                    if (firstTry) {
                        logInGameError("I'm having troubles with the keyboard: ", e);
                        firstTry = false;
                    }
                }
            }
            if (!firstTry) {
                logInGame("Ok it's repaired, sorry about that.");
            }
        }
        mouseWasInWindow = mouseInWindow;
    }

    /**
     * Allows to maintain a clone of the hotbar contents to track changes
     * (especially needed by the "on pickup" features).
     */
    private void cloneHotbar() {
        dk[] mainInventory = getMainInventory();
        for (int i = 0; i < 9; i++) {
            if (mainInventory[i] != null) {
                hotbarClone[i] = copy(mainInventory[i]);
            } else {
                hotbarClone[i] = null;
            }
        }
    }

    private void playClick() {
        if (!cfgManager.getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_SORTING_SOUND).equals("false")) {
            playSoundAtEntity(getTheWorld(), getThePlayer(), "random.click", 0.2F, 1.8F);
        }
    }

    private String buildlogString(Level level, String message, Exception e) {
        if (e != null) {
            return buildlogString(level, message) + ": " + e.getMessage();
        } else {
            return buildlogString(level, message) + ": (unknown error)";
        }
    }

    private String buildlogString(Level level, String message) {
        return InvTweaksConst.INGAME_LOG_PREFIX + ((level.equals(Level.SEVERE)) ? "[ERROR] " : "") + message;
    }
    
}
