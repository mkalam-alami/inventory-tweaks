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
public class InvTweaks extends InvTweaksObfuscation implements Runnable {

    private static final Logger log = Logger.getLogger("InvTweaks");

    private static InvTweaks instance;

    /**
     * The configuration loader.
     */
    private InvTweaksConfigManager cfgManager = null;
    
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
    private yq[] hotbarClone = new yq[InvTweaksConst.INVENTORY_HOTBAR_SIZE];
    private boolean mouseWasInWindow = true, mouseWasDown = false;;
    
    /**
     * Allows to trigger some logic only every Const.POLLING_DELAY.
     */
    private int tickNumber = 0, lastPollingTickNumber = -InvTweaksConst.POLLING_DELAY;
    
    /**
    * Stores when the sorting key was last pressed (allows to detect long key holding)
    */
    private long sortingKeyPressedDate = 0;
    
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
            ug guiScreen = getCurrentScreen();
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

        // Handle option to disable this feature
        if (!cfgManager.getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP).equals("false")) {
            // Run sorting on pickup
            new Thread(this).start();
        }

    }    
    
    /**
     * On Item Pickup handler
     */
    @Override
    public void run() {

        InvTweaksConfig config = cfgManager.getConfig();
        
        try {
            
            InvTweaksContainerSectionManager containerMgr = new InvTweaksContainerSectionManager(mc, InvTweaksContainerSection.INVENTORY);

            // Find where the item has been picked up
            int pickupSlot = -1;
            long tStart = System.currentTimeMillis();
            while (pickupSlot == -1 && System.currentTimeMillis() - tStart < InvTweaksConst.SORTING_TIMEOUT) {
                for (int i = 0; i < 9; i++) {
                    yq stackToCompare = containerMgr.getItemStack(27 + i);
                    if (stackToCompare != null) {
                        if (hotbarClone[i] == null) {
                            pickupSlot = 27 + i;
                            break;
                        }
                        else if (!areItemStacksEqual(stackToCompare, hotbarClone[i])) {
                            // We're looking for a brand new stack in the hotbar,
                            // not an existing stack whose amount has been increased
                            return;
                        }
                    }
                }
                Thread.yield();
            }
            
            if (pickupSlot != -1) {
            
	            // Find preffered slots
	            List<Integer> prefferedPositions = new LinkedList<Integer>();
	            InvTweaksItemTree tree = config.getTree();
	            yq stack = containerMgr.getItemStack(pickupSlot);
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
	                        if (newSlot == pickupSlot) {
	                            hasToBeMoved = false;
	                            break;
	                        }
	                        // Is the slot available?
	                        else if (containerMgr.getItemStack(newSlot) == null) {
	                            // TODO: Check rule level before to move
	                            if (containerMgr.move(pickupSlot, newSlot)) {
	                                break;
	                            }
	                        }
	                    } catch (TimeoutException e) {
	                        logInGameError("invtweaks.pickup.error", e);
	                    }
	                }
	            }
	            // Else, put the slot anywhere
	            if (hasToBeMoved) {
	                for (int i = 0; i < containerMgr.getSize(); i++) {
	                    if (containerMgr.getItemStack(i) == null) {
	                        if (containerMgr.move(pickupSlot, i)) {
	                            break;
	                        }
	                    }
	                }
	            }
	            
            }
            
        } catch (Exception e) {
            logInGameError("invtweaks.pickup.error", e);
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
    public void onTickInGUI(ug guiScreen) {
        synchronized (this) {
            handleMiddleClick(guiScreen); // Called before the rest to be able to trigger config reload 
            if (!onTick()) {
                return;
            }
            if (isTimeForPolling()) {
                unlockKeysIfNecessary();
            }
            handleGUILayout(guiScreen);
            handleShortcuts(guiScreen);
        }
    }

    public void logInGame(String message) {
    	logInGame(message, false);
    }

    public void logInGame(String message, boolean alreadyTranslated) {
        String formattedMsg = buildlogString(Level.INFO, (alreadyTranslated) ? message : InvTweaksLocalization.get(message));
        addChatMessage(formattedMsg);
        log.info(formattedMsg);
    }
    
    public void logInGameError(String message, Exception e) {
        String formattedMsg = buildlogString(Level.SEVERE, InvTweaksLocalization.get(message), e);
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

    public static Minecraft getMinecraftInstance() {
        return instance.mc;
    }
    
    // Used by ShortcutsHandler only, but put here for convenience and 
    // performance, since the xSize/ySize attributes are protected
    public static boolean getIsMouseOverSlot(ft guiContainer, wz slot, int i, int j) { // Copied from GuiContainer
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
        ug currentScreen = getCurrentScreen();
        if (currentScreen == null || isGuiInventory(currentScreen)) {
            cloneHotbar();
        }

        // Switch between configurations (shorcut)
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
                    logInGame(String.format(InvTweaksLocalization.get("invtweaks.loadconfig.enabled"), newRuleset), true);
                    // Hack to prevent 2nd way to switch configs from being enabled
                    sortingKeyPressedDate = Integer.MAX_VALUE; 
                }
            }
        }

        // Switch between configurations (by holding the sorting key)
        if (isSortingShortcutDown()) {
            long currentTime = System.currentTimeMillis();
            if (sortingKeyPressedDate == 0) {
                sortingKeyPressedDate = currentTime;
            } else if (currentTime - sortingKeyPressedDate > InvTweaksConst.RULESET_SWAP_DELAY
                    && sortingKeyPressedDate != Integer.MAX_VALUE) {
                String previousRuleset = config.getCurrentRulesetName();
                String newRuleset = config.switchConfig();
                // Log only if there is more than 1 ruleset
                if (newRuleset != null && !previousRuleset.equals(newRuleset)) {
                    logInGame(String.format(InvTweaksLocalization.get("invtweaks.loadconfig.enabled"), newRuleset), true);
                    handleSorting(currentScreen);
                }
                sortingKeyPressedDate = currentTime;
            }
        } else {
            sortingKeyPressedDate = 0;
        }
        
        

        return true;

    }

    private void handleSorting(ug guiScreen) {
    	
        yq selectedItem = getMainInventory()[getFocusedSlot()];

        // Sorting
        try {
            new InvTweaksHandlerSorting(mc, cfgManager.getConfig(),
                    InvTweaksContainerSection.INVENTORY,
                    InvTweaksHandlerSorting.ALGORITHM_INVENTORY,
                    InvTweaksConst.INVENTORY_ROW_SIZE).sort();
        } catch (Exception e) {
            logInGameError("invtweaks.sort.inventory.error", e);
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
    
        yq currentStack = getFocusedStack();
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
                        logInGameError("invtweaks.sort.autorefill.error", e);
                    }
                }
            }
        }
    
        storedStackId = currentStackId;
        storedStackDamage = currentStackDamage;
    
    }

    private void handleMiddleClick(ug guiScreen) {
    
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
                        ft guiContainer = (ft) guiScreen;
                        cx container = getContainer((ft) guiScreen);
                        int slotCount = getSlots(container).size();
                        int mouseX = (Mouse.getEventX() * getWidth(guiContainer)) / getDisplayWidth();
                        int mouseY = getHeight(guiContainer) - (Mouse.getEventY() * getHeight(guiContainer)) / getDisplayHeight() - 1;
                        int target = 0; // 0 = nothing, 1 = chest, 2 = inventory
                        for (int i = 0; i < slotCount; i++) {
                            wz slot = getSlot(container, i);
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
                            playClick();
    
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
                                logInGameError("invtweaks.sort.chest.error", e);
                                e.printStackTrace();
                            }
                            chestAlgorithm = (chestAlgorithm + 1) % 3;
                            chestAlgorithmClickTimestamp = timestamp;
                        } else if (target == 2) {
                            handleSorting(guiScreen);
                        }
    
                    } else if (isValidInventory(guiScreen)) {
                        handleSorting(guiScreen);
                    }
                }
            }
        } else {
            chestAlgorithmButtonDown = false;
        }
    }

    private void handleGUILayout(ug guiScreen) {

        InvTweaksConfig config = cfgManager.getConfig();
        boolean isValidChest = isValidChest(guiScreen);

        if (isValidChest || isGuiInventory(guiScreen) || isGuiWorkbench(guiScreen)) {

            int w = 10, h = 10;

            // Look for the mods buttons
            boolean customButtonsAdded = false;
            List<Object> controlList = getControlList(guiScreen);
            for (Object o : controlList) {
                if (isGuiButton(o)) {
                    zr button = (zr) o;
                    if (getId(button) == InvTweaksConst.JIMEOWAN_ID) {
                        customButtonsAdded = true;
                        break;
                    }
                }
            }

            if (!customButtonsAdded) {

                // Inventory button
                if (!isValidChest) {
                    controlList.add(new InvTweaksGuiSettingsButton(
                            cfgManager, InvTweaksConst.JIMEOWAN_ID,
                            getWidth(guiScreen) / 2 + 73, getHeight(guiScreen) / 2 - 78,
                            w, h, "...",
                            InvTweaksLocalization.get("invtweaks.button.settings.tooltip")));
                }

                // Chest buttons
                else {
                    
                    // Reset sorting algorithm selector
                    chestAlgorithmClickTimestamp = 0;

                    ft guiContainer = (ft) guiScreen;
                    int id = InvTweaksConst.JIMEOWAN_ID,
                        x = getXSize(guiContainer) / 2 + getWidth(guiContainer) / 2 - 17,
                        y = (getHeight(guiContainer) - getYSize(guiContainer)) / 2 + 5;
                    boolean isChestWayTooBig = mods.isChestWayTooBig(guiScreen);

                    // Settings button
                    controlList.add(new InvTweaksGuiSettingsButton(
                            cfgManager, id++, 
                            (isChestWayTooBig) ? x + 22 : x - 1,
                            (isChestWayTooBig) ? y - 3 : y,
                            w, h, "...", 
                            InvTweaksLocalization.get("invtweaks.button.settings.tooltip")));

                    // Sorting buttons
                    if (!config.getProperty(InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS).equals("false")) {

                        int rowSize = getContainerRowSize(guiContainer);
                        
                        InvTweaksObfuscationGuiButton button = new InvTweaksGuiSortingButton(
                                cfgManager, id++,
                                (isChestWayTooBig) ? x + 22 : x - 13,
                                (isChestWayTooBig) ? y + 12 : y,
                                w, h, "h", InvTweaksLocalization.get("invtweaks.button.chest3.tooltip"),
                                InvTweaksHandlerSorting.ALGORITHM_HORIZONTAL,
                                rowSize);
                        controlList.add(button);

                        button = new InvTweaksGuiSortingButton(
                                cfgManager, id++,
                                (isChestWayTooBig) ? x + 22 : x - 25,
                                (isChestWayTooBig) ? y + 25 : y,
                                w, h, "v", InvTweaksLocalization.get("invtweaks.button.chest2.tooltip"),
                                InvTweaksHandlerSorting.ALGORITHM_VERTICAL,
                                rowSize);
                        controlList.add(button);

                        button = new InvTweaksGuiSortingButton(
                                cfgManager, id++,
                                (isChestWayTooBig) ? x + 22 : x - 37,
                                (isChestWayTooBig) ? y + 38 : y,
                                w, h, "s", InvTweaksLocalization.get("invtweaks.button.chest1.tooltip"),
                                InvTweaksHandlerSorting.ALGORITHM_DEFAULT,
                                rowSize);
                        controlList.add(button);

                    }
                }
            }
        }

    }
    
    private void handleShortcuts(ug guiScreen) {
        
        // Check open GUI
        if (!(isValidChest(guiScreen) || isValidInventory(guiScreen))) {
            return;
        }
        
        // Configurable shortcuts
        if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
            if (!mouseWasDown) {
                mouseWasDown = true;
                
                // The mouse has just been clicked,
                // trigger a shortcut according to the pressed keys.
                if (cfgManager.getConfig().getProperty(
                        InvTweaksConfig.PROP_ENABLE_SHORTCUTS).equals("true")) {
                    cfgManager.getShortcutsHandler().handleShortcut((ft) guiScreen);
                }
            }
        }
        else {
            mouseWasDown = false;
        }
        
    }

    private int getContainerRowSize(ft guiContainer) {
        if (isGuiChest(guiContainer)) {
            return InvTweaksConst.CHEST_ROW_SIZE;
        }
        else if (isGuiDispenser(guiContainer)) {
            return InvTweaksConst.DISPENSER_ROW_SIZE;
        }
        else {
            return getSpecialChestRowSize(guiContainer, InvTweaksConst.CHEST_ROW_SIZE);
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
                        logInGameError("invtweaks.keyboardfix.error", e);
                        firstTry = false;
                    }
                }
            }
            if (!firstTry) {
                logInGame("invtweaks.keyboardfix.recover");
            }
        }
        mouseWasInWindow = mouseInWindow;
    }

    /**
     * Allows to maintain a clone of the hotbar contents to track changes
     * (especially needed by the "on pickup" features).
     */
    private void cloneHotbar() {
        yq[] mainInventory = getMainInventory();
        for (int i = 0; i < 9; i++) {
            if (mainInventory[i] != null) {
                hotbarClone[i] = copy(mainInventory[i]);
            } else {
                hotbarClone[i] = null;
            }
        }
    }

    private void playClick() {
        if (!cfgManager.getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_SOUNDS).equals(InvTweaksConfig.VALUE_FALSE)) {
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
