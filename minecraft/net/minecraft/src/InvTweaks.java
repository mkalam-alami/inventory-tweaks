package net.minecraft.src;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.invtweaks.Const;
import net.invtweaks.config.InvTweaksConfig;
import net.invtweaks.config.InvTweaksConfigManager;
import net.invtweaks.config.InventoryConfigRule;
import net.invtweaks.gui.GuiSettingsButton;
import net.invtweaks.gui.GuiSortingButton;
import net.invtweaks.library.ContainerManager.ContainerSection;
import net.invtweaks.library.ContainerSectionManager;
import net.invtweaks.library.Obfuscation;
import net.invtweaks.logic.SortingHandler;
import net.invtweaks.tree.ItemTree;
import net.invtweaks.tree.ItemTreeItem;
import net.minecraft.client.Minecraft;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class InvTweaks extends Obfuscation {

    private static final Logger log = Logger.getLogger("InvTweaks");

    private static InvTweaks instance;
    private static KeyBinding sortKeyBinding = new KeyBinding("Sort inventory", Keyboard.KEY_R); /* KeyBinding */

    /**
     * The configuration loader.
     */
    private InvTweaksConfigManager cfgManager = null;
    
    /**
     * Attributes to remember the status of chest sorting
     * while using middle clicks.
     */
    private int chestAlgorithm = SortingHandler.ALGORITHM_DEFAULT;
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
    private ItemStack[] hotbarClone = new ItemStack[Const.INVENTORY_HOTBAR_SIZE];
    private boolean mouseWasInWindow = true, mouseWasDown = false;;
    
    /**
     * Allows to trigger some logic only every Const.POLLING_DELAY.
     */
    private int tickNumber = 0, lastPollingTickNumber = -Const.POLLING_DELAY;
    
    /**
     * Creates an instance of the mod, and loads the configuration
     * from the files, creating them if necessary.
     * @param mc
     */
    public InvTweaks(Minecraft mc) {
        super(mc);

        log.setLevel(Const.DEFAULT_LOG_LEVEL);

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
            if (!cfgManager.makeSureConfigurationIsLoaded()) {
                return;
            }

            // Check config loading success & current GUI
            GuiScreen guiScreen = getCurrentScreen();
            if (guiScreen != null && !(guiScreen instanceof GuiContainer) /* GuiContainer */) {

                return;
            }

            // Sorting!
            handleSorting(guiScreen);
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
            ContainerSectionManager containerMgr = new ContainerSectionManager(mc, ContainerSection.INVENTORY);

            // Find stack slot (look in hotbar only).
            // We're looking for a brand new stack in the hotbar
            // (not an existing stack whose amount has been increased)
            int currentSlot = -1;
            do {
                // In SMP, we have to wait first for the inventory update
                if (isMultiplayerWorld() && currentSlot == -1) {
                    try {
                        Thread.sleep(Const.POLLING_DELAY);
                    } catch (InterruptedException e) {
                        // Do nothing (sleep interrupted)
                    }
                }
                for (int i = 0; i < Const.INVENTORY_HOTBAR_SIZE; i++) {
                    ItemStack currentHotbarStack = containerMgr.getItemStack(i + 27);
                    // Don't move already started stacks
                    if (currentHotbarStack != null && currentHotbarStack.animationsToGo == 5 && hotbarClone[i] == null) {
                        currentSlot = i + 27;
                    }
                }

                // The loop is only relevant in SMP (polling)
            } while (isMultiplayerWorld() && currentSlot == -1);

            if (currentSlot != -1) {

                // Find preffered slots
                List<Integer> prefferedPositions = new LinkedList<Integer>();
                ItemTree tree = config.getTree();
                ItemStack stack = containerMgr.getItemStack(currentSlot);
                List<ItemTreeItem> items = tree.getItems(getItemID(stack),
                        getItemDamage(stack));
                for (InventoryConfigRule rule : config.getRules()) {
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
                    for (int i = 0; i < containerMgr.getSectionSize(); i++) {
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
    public void onTickInGUI(GuiScreen guiScreen) {
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

    /**
     * Returns the binding for the sort key.
     * Object maintained by Minecraft so that it's keycode is actually
     * what has been configured by the player (not always the R key).
     * @return
     */
    public static KeyBinding getSortKeyBinding() {
        return sortKeyBinding;
    }
    
    // Used by ShortcutsHandler only, but put here for convenience and 
    // performance, since the xSize/ySize attributes are protected
    public static boolean getIsMouseOverSlot(GuiContainer guiContainer, Slot slot, int i, int j) { // Copied from GuiContainer
     // Copied from GuiContainer
        int k = (guiContainer.width - guiContainer.xSize) / 2;
        int l = (guiContainer.height - guiContainer.ySize) / 2;
        i -= k;
        j -= l;
        return i >= slot.xDisplayPosition - 1 && i < slot.xDisplayPosition + 16 + 1 && j >= slot.yDisplayPosition - 1 && j < slot.yDisplayPosition + 16 + 1;
    }

    private boolean onTick() {

        tickNumber++;
        
        // Not calling "cfgManager.makeSureConfigurationIsLoaded()" for performance reasons
        InvTweaksConfig config = cfgManager.getConfig();
        if (config == null) { 
            return false;
        }
        
        // Clone the hotbar to be able to monitor changes on it
        GuiScreen currentScreen = getCurrentScreen();
        if (currentScreen == null || currentScreen instanceof GuiInventory) {
            cloneHotbar();
        }

        // If the key is hold for 1s, switch config
        if (Keyboard.isKeyDown(getKeycode(sortKeyBinding))) {
            long currentTime = System.currentTimeMillis();
            if (sortingKeyPressedDate == 0) {
                sortingKeyPressedDate = currentTime;
            } else if (currentTime - sortingKeyPressedDate > Const.RULESET_SWAP_DELAY) {
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

    private void handleSorting(GuiScreen guiScreen) {

        ItemStack selectedItem = getMainInventory()[getFocusedSlot()];

        try {
            new SortingHandler(mc, cfgManager.getConfig(),
                    ContainerSection.INVENTORY,
                    SortingHandler.ALGORITHM_INVENTORY).sort();
        } catch (Exception e) {
            logInGame("Failed to sort inventory: " + e.getMessage());
        }

        playClick();

        // This needs to be remembered so that the
        // auto-refill feature doesn't trigger
        if (selectedItem != null && getMainInventory()[getFocusedSlot()] == null) {
            storedStackId = 0;
        }

    }

    private void handleAutoRefill() {
    
        ItemStack currentStack = getFocusedStack();
        int currentStackId = (currentStack == null) ? 0 : getItemID(currentStack);
        int currentStackDamage = (currentStack == null) ? 0 : getItemDamage(currentStack);
        int focusedSlot = getFocusedSlot() + 27; // Convert to container slots index
        InvTweaksConfig config = cfgManager.getConfig();
        
        if (currentStackId != storedStackId || currentStackDamage != storedStackDamage) {
    
            if (storedFocusedSlot != focusedSlot) { // Filter selection change
                storedFocusedSlot = focusedSlot;
            } else if ((currentStack == null || getItemID(currentStack) == 281 && storedStackId == 282)  // Handle eaten mushroom soup
                    && (getCurrentScreen() == null || // Filter open inventory or other window
                    getCurrentScreen() instanceof GuiEditSign /* GuiEditSign */)) {
    
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

    private void handleMiddleClick(GuiScreen guiScreen) {
    
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
    
                    if (isChestOrDispenser(guiScreen)) {
    
                        // Check if the middle click target the chest or the
                        // inventory
                        // (copied GuiContainer.getSlotAtPosition algorithm)
                        GuiContainer guiContainer = (GuiContainer) guiScreen;
                        Container container = getContainer((GuiContainer) guiScreen);
                        int slotCount = getSlots(container).size();
                        int mouseX = (Mouse.getEventX() * guiContainer.width) / mc.displayWidth;
                        int mouseY = guiContainer.height - (Mouse.getEventY() * guiContainer.height) / mc.displayHeight - 1;
                        int target = 0; // 0 = nothing, 1 = chest, 2 = inventory
                        for (int i = 0; i < slotCount; i++) {
                            Slot slot = getSlot(container, i);
                            int k = (guiContainer.width - guiContainer.xSize) / 2;
                            int l = (guiContainer.height - guiContainer.ySize) / 2;
                            if (mouseX - k >= slot.xDisplayPosition - 1 &&
                                    mouseX - k < slot.xDisplayPosition + 16 + 1 &&
                                    mouseY - l >= slot.yDisplayPosition - 1 &&
                                    mouseY - l < slot.yDisplayPosition + 16 + 1) {
                                target = (i < slotCount - Const.INVENTORY_SIZE) ? 1 : 2;
                                break;
                            }
                        }
    
                        if (target == 1) {
    
                            // Play click
                            mc.theWorld.playSoundAtEntity(getThePlayer(), "random.click", 0.2F, 1.8F);
    
                            long timestamp = System.currentTimeMillis();
                            if (timestamp - chestAlgorithmClickTimestamp > 
                                    Const.CHEST_ALGORITHM_SWAP_MAX_INTERVAL) {
                                chestAlgorithm = SortingHandler.ALGORITHM_DEFAULT;
                            }
                            try {
                                new SortingHandler(mc, cfgManager.getConfig(),
                                        ContainerSection.CHEST, chestAlgorithm).sort();
                            } catch (Exception e) {
                                logInGameError("Failed to sort container", e);
                            }
                            chestAlgorithm = (chestAlgorithm + 1) % 3;
                            chestAlgorithmClickTimestamp = timestamp;
                        } else if (target == 2) {
                            handleSorting(guiScreen);
                        }
    
                    } else {
                        handleSorting(guiScreen);
                    }
                }
            }
        } else {
            chestAlgorithmButtonDown = false;
        }
    }

    @SuppressWarnings("unchecked")
    private void handleGUILayout(GuiScreen guiScreen) {

        InvTweaksConfig config = cfgManager.getConfig();
        boolean isContainer = isChestOrDispenser(guiScreen);

        if (isContainer || guiScreen instanceof GuiInventory
                || guiScreen.getClass().getSimpleName()
                        .equals("GuiInventoryMoreSlots") /* Aether mod */) {

            int w = 10, h = 10;

            // Look for the mods buttons
            boolean customButtonsAdded = false;
            for (Object o : guiScreen.controlList) {
                GuiButton button = (GuiButton) o;
                if (button.id == Const.JIMEOWAN_ID) {
                    customButtonsAdded = true;
                    break;
                }
            }

            if (!customButtonsAdded) {

                // Inventory button
                if (!isContainer) {
                    guiScreen.controlList.add(new GuiSettingsButton(cfgManager, 
                            Const.JIMEOWAN_ID,
                            guiScreen.width / 2 + 73, guiScreen.height / 2 - 78,
                            w, h, "..."));
                }

                // Chest buttons
                else {

                    GuiContainer guiContainer = (GuiContainer) guiScreen;
                    int id = Const.JIMEOWAN_ID,
                        x = guiContainer.xSize / 2 + guiContainer.width / 2 - 17,
                        y = (guiContainer.height - guiContainer.ySize) / 2 + 5;

                    // Settings button
                    guiScreen.controlList.add(new GuiSettingsButton(cfgManager, 
                            id++, x - 1, y, w, h, "..."));

                    // Sorting buttons
                    if (!config.getProperty(InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS).equals("false")) {

                        GuiButton button = new GuiSortingButton(cfgManager,
                                id++, x - 37, y, w, h, "s",
                                SortingHandler.ALGORITHM_DEFAULT);
                        guiContainer.controlList.add((GuiButton) button);

                        button = new GuiSortingButton(cfgManager,
                                id++, x - 25, y, w, h, "v",
                                SortingHandler.ALGORITHM_VERTICAL);
                        guiContainer.controlList.add((GuiButton) button);

                        button = new GuiSortingButton(cfgManager,
                                id++, x - 13, y, w, h, "h",
                                SortingHandler.ALGORITHM_HORIZONTAL);

                        guiContainer.controlList.add((GuiButton) button);

                    }
                }
            }
        }

    }
    
    private void handleShortcuts(GuiScreen guiScreen) {
        if (!(guiScreen instanceof GuiContainer)) {
            return;
        }
        if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
            if (!mouseWasDown) {
                mouseWasDown = true;
                
                // The mouse has just been clicked,
                // trigger a shortcut according to the pressed keys.
                cfgManager.getShortcutsHandler().handleShortcut(
                        (GuiContainer) guiScreen);
            }
        }
        else {
            mouseWasDown = false;
        }
    }

    private boolean isTimeForPolling() {
        if (tickNumber - lastPollingTickNumber >= Const.POLLING_DELAY) {
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
        ItemStack[] mainInventory = getMainInventory();
        for (int i = 0; i < 9; i++) {
            if (mainInventory[i] != null) {
                hotbarClone[i] = mainInventory[i].copy();
            } else {
                hotbarClone[i] = null;
            }
        }
    }

    private void playClick() {
        if (!cfgManager.getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_SORTING_SOUND).equals("false")) {
            mc.theWorld.playSoundAtEntity(getThePlayer(), "random.click", 0.2F, 1.8F);
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
        return Const.INGAME_LOG_PREFIX + ((level.equals(Level.SEVERE)) ? "[ERROR] " : "") + message;
    }
    
}
