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
     * Stores when the sorting key was last pressed to help
     * trigger the configuration swapping.
     */
    private long sortingKeyPressedDate = 0;
    
    /**
     * Various information concerning the context, stored on
     * each tick to allow for certain features (auto-refill,
     * sorting on pick up...)
     */
    private int storedStackId = 0, storedStackDamage = -1, storedFocusedSlot = -1;
    private ul[] hotbarClone = new ul[InvTweaksConst.INVENTORY_HOTBAR_SIZE];
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
            qr guiScreen = getCurrentScreen();
            if (guiScreen == null || isGuiContainer(guiScreen)
                    && !isGuiContainerCreative(guiScreen)) {
                // Sorting!
                handleSorting(guiScreen);
            }
        }
    }

    /**
     * To be called everytime a stack has been picked up.
     * Moves the picked up item in anothet slot that matches best the current configuration.
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
                    ul currentHotbarStack = containerMgr.getItemStack(i + 27);
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
                ul stack = containerMgr.getItemStack(currentSlot);
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
     * Handles the GUi additions and the middle clicking.
     * @param guiScreen
     */
    public void onTickInGUI(qr guiScreen) {
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
    public static boolean getIsMouseOverSlot(em guiContainer, sx slot, int i, int j) { // Copied from GuiContainer
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
        qr currentScreen = getCurrentScreen();
        if (currentScreen == null || isGuiInventory(currentScreen)) {
            cloneHotbar();
        }

        // If the key is hold for 1s, switch config
        if (Keyboard.isKeyDown(getKeycode(InvTweaksConst.SORT_KEY_BINDING))) {
            long currentTime = System.currentTimeMillis();
            if (sortingKeyPressedDate == 0) {
                sortingKeyPressedDate = currentTime;
            } else if (currentTime - sortingKeyPressedDate > InvTweaksConst.RULESET_SWAP_DELAY) {
                String previousRuleset = config.getCurrentRulesetName();
                String newRuleset = config.switchConfig();
                if (newRuleset == null) {
                    logInGameError("Failed to switch the configuration", null);
                }
                // Log only if there is more than 1 ruleset
                else if (!previousRuleset.equals(newRuleset)) {
                    logInGame("'" + newRuleset + "' enabled");
                    handleSorting(currentScreen);
                }
                sortingKeyPressedDate = currentTime;
            }
        } else {
            sortingKeyPressedDate = 0;
        }

        return true;

    }

    private void handleSorting(qr guiScreen) {

        ul selectedItem = getMainInventory()[getFocusedSlot()];

        // Switch between configurations
        InvTweaksConfig config = cfgManager.getConfig();
        Vector<Integer> downKeys = cfgManager.getShortcutsHandler().getDownShortcutKeys();
        if (Keyboard.isKeyDown(getKeyCode(InvTweaksConst.SORT_KEY_BINDING))) {
            for (int downKey : downKeys) {
                String newRuleset = null;
                switch (downKey) {
                case Keyboard.KEY_1:
                case Keyboard.KEY_NUMPAD1:
                    newRuleset = config.switchConfig(0);
                    break;
                case Keyboard.KEY_2:
                case Keyboard.KEY_NUMPAD2:
                    newRuleset = config.switchConfig(1);
                    break;
                case Keyboard.KEY_3:
                case Keyboard.KEY_NUMPAD3:
                    newRuleset = config.switchConfig(2);
                    break;
                case Keyboard.KEY_4:
                case Keyboard.KEY_NUMPAD4:
                    newRuleset = config.switchConfig(3);
                    break;
                case Keyboard.KEY_5:
                case Keyboard.KEY_NUMPAD5:
                    newRuleset = config.switchConfig(4);
                    break;
                case Keyboard.KEY_6:
                case Keyboard.KEY_NUMPAD6:
                    newRuleset = config.switchConfig(5);
                    break;
                case Keyboard.KEY_7:
                case Keyboard.KEY_NUMPAD7:
                    newRuleset = config.switchConfig(6);
                    break;
                case Keyboard.KEY_8:
                case Keyboard.KEY_NUMPAD8:
                    newRuleset = config.switchConfig(7);
                    break;
                case Keyboard.KEY_9:
                case Keyboard.KEY_NUMPAD9:
                    newRuleset = config.switchConfig(8);
                    break;
                }
                if (newRuleset != null) {
                    logInGame("'" + newRuleset + "' enabled");
                }
            }
        }
        
        // Sorting
        try {
            new InvTweaksHandlerSorting(mc, cfgManager.getConfig(),
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
    
        ul currentStack = getFocusedStack();
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

    private void handleMiddleClick(qr guiScreen) {
    
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
                        em guiContainer = (em) guiScreen;
                        cf container = getContainer((em) guiScreen);
                        int slotCount = getSlots(container).size();
                        int mouseX = (Mouse.getEventX() * getWidth(guiContainer)) / getDisplayWidth();
                        int mouseY = getHeight(guiContainer) - (Mouse.getEventY() * getHeight(guiContainer)) / getDisplayHeight() - 1;
                        int target = 0; // 0 = nothing, 1 = chest, 2 = inventory
                        for (int i = 0; i < slotCount; i++) {
                            sx slot = getSlot(container, i);
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
                                new InvTweaksHandlerSorting(mc, cfgManager.getConfig(),
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
    
                    } else if (!isGuiContainerCreative(guiScreen)) {
                        handleSorting(guiScreen);
                    }
                }
            }
        } else {
            chestAlgorithmButtonDown = false;
        }
    }

    private void handleGUILayout(qr guiScreen) {

        InvTweaksConfig config = cfgManager.getConfig();
        boolean isValidChest = isValidChest(guiScreen);

        if (isValidChest || isValidInventory(guiScreen)) {

            int w = 10, h = 10;

            // Look for the mods buttons
            boolean customButtonsAdded = false;
            for (Object o : getControlList(guiScreen)) {
                vj button = (vj) o;
                if (getId(button) == InvTweaksConst.JIMEOWAN_ID) {
                    customButtonsAdded = true;
                    break;
                }
            }

            if (!customButtonsAdded) {

                // Inventory button
                if (!isValidChest) {
                    getControlList(guiScreen).add(new InvTweaksGuiInventorySettingsButton(
                            cfgManager, InvTweaksConst.JIMEOWAN_ID,
                            getWidth(guiScreen) / 2 + 73, getHeight(guiScreen) / 2 - 78,
                            w, h, "...", "Inventory settings"));
                }

                // Chest buttons
                else {
                    
                    // Reset sorting algorithm selector
                    chestAlgorithmClickTimestamp = 0;

                    em guiContainer = (em) guiScreen;
                    int id = InvTweaksConst.JIMEOWAN_ID,
                        x = getXSize(guiContainer) / 2 + getWidth(guiContainer) / 2 - 17,
                        y = (getHeight(guiContainer) - getYSize(guiContainer)) / 2 + 5;

                    // Settings button
                    getControlList(guiScreen).add(new InvTweaksGuiInventorySettingsButton(
                            cfgManager, id++, 
                            x - 1, y, w, h, "...", "Inventory settings"));

                    // Sorting buttons
                    if (!config.getProperty(InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS).equals("false")) {

                        int rowSize = getContainerRowSize(guiContainer);
                        
                        InvTweaksObfuscationGuiButton button = new InvTweaksGuiSortingButton(
                                cfgManager, id++,
                                x - 13, y, w, h, "h", "Sort in rows",
                                InvTweaksHandlerSorting.ALGORITHM_HORIZONTAL,
                                rowSize);
                        getControlList(guiContainer).add(button);

                        button = new InvTweaksGuiSortingButton(
                                cfgManager, id++,
                                x - 25, y, w, h, "v", "Sort in columns",
                                InvTweaksHandlerSorting.ALGORITHM_VERTICAL,
                                rowSize);
                        getControlList(guiContainer).add(button);

                        button = new InvTweaksGuiSortingButton(
                                cfgManager, id++,
                                x - 37, y, w, h, "s", "Default sorting",
                                InvTweaksHandlerSorting.ALGORITHM_DEFAULT,
                                rowSize);
                        getControlList(guiContainer).add(button);

                    }
                }
            }
        }

    }
    
    private void handleShortcuts(qr guiScreen) {
        
        // Check open GUI
        if (!isGuiContainer(guiScreen)
                || guiScreen.getClass().getSimpleName().equals("MLGuiChestBuilding")) { // Millenaire mod
            return;
        }
        
        if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
            if (!mouseWasDown) {
                mouseWasDown = true;
                
                // The mouse has just been clicked,
                // trigger a shortcut according to the pressed keys.
                if (cfgManager.getConfig().getProperty(
                        InvTweaksConfig.PROP_ENABLE_SHORTCUTS).equals("true")) {
                    cfgManager.getShortcutsHandler().handleShortcut(
                            (em) guiScreen);
                }
            }
        }
        else {
            mouseWasDown = false;
        }
    }

    private int getContainerRowSize(em guiContainer) {
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

    private boolean isValidChest(qr guiScreen) {
        if (isGuiContainer(guiScreen)) {
            em guiContainer = (em) guiScreen;
            return (isGuiChest(guiScreen) || isGuiDispenser(guiScreen) ||
              (isGuiContainer(guiScreen) && modCompatibility.isSpecialChest(guiContainer)))
                    && !modCompatibility.isForbiddenChest(guiContainer);
        }
        else {
            return false;
        }
    }
    
    private boolean isValidInventory(qr guiScreen) {
        return isGuiInventory(guiScreen) || 
            (isGuiContainer(guiScreen) 
                    && modCompatibility.isSpecialInventory((em) guiScreen));
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
        ul[] mainInventory = getMainInventory();
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
