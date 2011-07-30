package net.minecraft.src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.invtweaks.Const;
import net.invtweaks.Obfuscation;
import net.invtweaks.config.InvTweaksConfig;
import net.invtweaks.config.InventoryConfigRule;
import net.invtweaks.gui.GuiInventorySettings;
import net.invtweaks.logic.InventoryAlgorithms;
import net.invtweaks.logic.SortableContainer;
import net.invtweaks.tree.ItemTree;
import net.invtweaks.tree.ItemTreeItem;
import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class InvTweaks extends Obfuscation {

    private static final Logger log = Logger.getLogger("InvTweaks");

    private static InvTweaks instance;
    private static KeyBinding sortKeyBinding = new KeyBinding("Sort inventory", Keyboard.KEY_R); /* KeyBinding */

    /**
     * The mod's configuration.
     */
    private InvTweaksConfig config = null;
    private long storedConfigLastModified = 0;
    
    /**
     * The mod's logic for sorting and moving items.
     */
    private InventoryAlgorithms inventoryAlgorithms = null;
    
    /**
     * Attributes to remember the status of chest sorting
     * while using middle clicks.
     */
    private int chestAlgorithm = InventoryAlgorithms.DEFAULT;
    private long chestAlgorithmClickTimestamp = 0;
    private boolean chestAlgorithmButtonDown = false;

    /**
     * Stores when the sorting key was last pressed to help
     * trigger the configuration swapping.
     */
    private long sortingKeyPressedDate = 0;
    
    /**
     * Various information concerning the inventory, stored on
     * each tick to allow for certain features (auto-refill,
     * sorting on pick up)
     */
    private int storedStackId = 0, storedStackDamage = -1, storedFocusedSlot = -1;
    private ItemStack[] hotbarClone = new ItemStack[Const.INVENTORY_HOTBAR_SIZE];

    
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
        if (makeSureConfigurationIsLoaded()) {
            log.info("Mod initialized");
        } else {
            log.severe("Mod failed to initialize!");
        }

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

    /**
     * To be called every time the sorting key is pressed.
     * Sorts the inventory.
     */
    public final void onSortingKeyPressed() {
        synchronized (this) {

            if (!makeSureConfigurationIsLoaded()) {
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

        if (!makeSureConfigurationIsLoaded()) {
            return;
        }

        // Handle option to disable this feature
        if (config.getProperty(InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP).equals("false")) {
            return;
        }

        SortableContainer container = new SortableContainer(mc, config,
                getPlayerContainer(), true);

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
                ItemStack currentHotbarStack = container.getItemStack(i + 27);
                // Don't move already started stacks
                if (currentHotbarStack != null && 
                        currentHotbarStack.animationsToGo == 5 && hotbarClone[i] == null) {
                    currentSlot = i + 27;
                }
            }

            // The loop is only relevant in SMP (polling)
        } while (isMultiplayerWorld() && currentSlot == -1);

        //
        if (currentSlot != -1) {

            // Find preffered slots
            List<Integer> prefferedPositions = new LinkedList<Integer>();
            InventoryConfigRule matchingRule = null;
            ItemTree tree = config.getTree();
            ItemStack stack = container.getItemStack(currentSlot);
            List<ItemTreeItem> items = tree.getItems(getItemID(stack), getItemDamage(stack));
            for (InventoryConfigRule rule : config.getRules()) {
                if (tree.matches(items, rule.getKeyword())) {
                    for (int slot : rule.getPreferredSlots()) {
                        prefferedPositions.add(slot);
                    }
                    matchingRule = rule;
                }
            }

            // Find best slot for stack
            if (prefferedPositions != null) {
                for (int newSlot : prefferedPositions) {
                    try {
                        // Already in the best slot!
                        if (newSlot == currentSlot) {
                            container.markAsMoved(newSlot, Integer.MAX_VALUE);
                            break;
                        }
                        // Is the slot available?
                        else if (container.getItemStack(newSlot) == null) {
                            if (container.moveStack(currentSlot, newSlot, matchingRule.getPriority()) != SortableContainer.MOVE_FAILURE) {
                                break;
                            }
                        }
                    } catch (TimeoutException e) {
                        logInGameError("Failed to move picked up stack", e);
                    }
                }
            }

            // Else, put the slot anywhere
            if (container.hasToBeMoved(currentSlot)) {
                for (int i = 0; i < container.getSize(); i++) {
                    try {
                        if (container.getItemStack(i) == null) {
                            if (container.moveStack(currentSlot, i, Integer.MAX_VALUE) != SortableContainer.MOVE_FAILURE) {
                                break;
                            }
                        }
                    } catch (TimeoutException e) {
                        logInGameError("Failed to move picked up stack", e);
                    }
                }
            }

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
            handleAutoReplace();
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
            handleGUILayout(guiScreen);
            handleMiddleClick(guiScreen);
        }
    }

    public boolean isConfigLoaded() {
        return config != null;
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

    private boolean onTick() {

        if (!isConfigLoaded())
            return false;

        // Clone the hotbar to be able to monitor changes on it
        GuiScreen currentScreen = getCurrentScreen();
        if (currentScreen == null || currentScreen instanceof GuiInventory) {
            cloneHotbar();
        }

        // If the key is hold for 1s, switch config
        if (Keyboard.isKeyDown(sortKeyBinding.keyCode)) { // TODO Obf layer
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

    private void handleSorting(GuiScreen guiScreen) {

        ItemStack selectedItem = getItemStack(getMainInventory(), getFocusedSlot());

        try {
            inventoryAlgorithms.sortContainer((guiScreen == null) ? getPlayerContainer() : getContainer((GuiContainer) guiScreen), /* GuiContainer */
            true, InventoryAlgorithms.INVENTORY);
        } catch (TimeoutException e) {
            logInGame("Failed to sort inventory: " + e.getMessage());
        }

        playClick();

        // This needs to be remembered so that the
        // autoreplace feature doesn't trigger
        if (selectedItem != null && getItemStack(getMainInventory(), getFocusedSlot()) == null) {
            storedStackId = 0;
        }

    }

    @SuppressWarnings("unchecked")
    private void handleGUILayout(GuiScreen guiScreen) {

        boolean isContainer = isChestOrDispenser(guiScreen);

        if (isContainer || guiScreen instanceof GuiInventory) {

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
                    guiScreen.controlList.add(new SettingsButton(Const.JIMEOWAN_ID,
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
                    guiScreen.controlList.add(new SettingsButton(id++, x - 1, y, w, h, "..."));

                    // Sorting buttons
                    if (!config.getProperty(InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS).equals("false")) {

                        Container container = getContainer((GuiContainer) guiScreen);

                        GuiButton button = new SortingButton(id++, x - 37,
                                y, w, h, "s", container, InventoryAlgorithms.DEFAULT);
                        guiContainer.controlList.add((GuiButton) button);

                        button = new SortingButton(id++, x - 25, y, w, h, "v",
                                container, InventoryAlgorithms.VERTICAL);
                        guiContainer.controlList.add((GuiButton) button);

                        button = new SortingButton(id++, x - 13, y, w, h, "h",
                                container, InventoryAlgorithms.HORIZONTAL);
                        guiContainer.controlList.add((GuiButton) button);

                    }
                }
            }
        }

    }

    private void handleMiddleClick(GuiScreen guiScreen) {

        if (Mouse.isButtonDown(2)) {

            if (!makeSureConfigurationIsLoaded()) {
                return;
            }

            if (!config.getProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK).equals("false")) {

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
                                chestAlgorithm = InventoryAlgorithms.DEFAULT;
                            }
                            try {
                                inventoryAlgorithms.sortContainer(container, false, chestAlgorithm);
                            } catch (TimeoutException e) {
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

    private void handleAutoReplace() {

        ItemStack currentStack = getFocusedStack();
        int currentStackId = (currentStack == null) ? 0 : getItemID(currentStack);
        int currentStackDamage = (currentStack == null) ? 0 : getItemDamage(currentStack);
        int focusedSlot = getFocusedSlot() + 27; // Convert to container slots
                                                 // index

        // Auto-replace item stack
        if (currentStackId != storedStackId || currentStackDamage != storedStackDamage) {

            if (storedFocusedSlot != focusedSlot) { // Filter selection change
                storedFocusedSlot = focusedSlot;
            } else if ((currentStack == null || getItemID(currentStack) == 281 && storedStackId == 282) // Handle
                                                                                                        // eaten
                                                                                                        // mushroom
                                                                                                        // soup
                    && (getCurrentScreen() == null || // Filter open inventory
                                                      // or other window
                    getCurrentScreen() instanceof GuiEditSign /* GuiEditSign */)) {

                if (config.autoreplaceEnabled(storedStackId, storedStackId)) {
                    inventoryAlgorithms.autoReplaceSlot(focusedSlot, storedStackId, storedStackDamage);
                }
            }
        }

        storedStackId = currentStackId;
        storedStackDamage = currentStackDamage;

    }

    // TODO Only reload modified file(s)
    private boolean makeSureConfigurationIsLoaded() {

        // Load properties
        try {
            if (config != null && config.refreshProperties()) {
                logInGame("Mod properties loaded");
            }
        } catch (IOException e) {
            logInGameError("Failed to refresh properties from file", e);
        }

        // Load rules + tree files
        long configLastModified = computeConfigLastModified();
        if (isConfigLoaded()) {
            // Check time of last edit for both configuration files.
            if (storedConfigLastModified != configLastModified) {
                return loadConfig(); // Reload
            } else {
                return true;
            }
        } else {
            storedConfigLastModified = configLastModified;
            return loadConfig(); // Reload
        }
    }

    private long computeConfigLastModified() {
        return new File(Const.CONFIG_RULES_FILE).lastModified()
        + new File(Const.CONFIG_TREE_FILE).lastModified();
    }

    /**
     * Tries to load mod configuration from file, with error handling. If it
     * fails, the config attribute will remain null.
     * 
     * @param config
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean loadConfig() {

        // Compatibility: Move/Remove old files

        if (new File(Const.OLD_CONFIG_RULES_FILE).exists()) {
            if (new File(Const.CONFIG_RULES_FILE).exists()) {
                backupFile(new File(Const.CONFIG_RULES_FILE), Const.CONFIG_RULES_FILE);
            }
            new File(Const.OLD_CONFIG_RULES_FILE).renameTo(new File(Const.CONFIG_RULES_FILE));
        }
        if (new File(Const.OLD_CONFIG_TREE_FILE).exists()) {
            backupFile(new File(Const.OLD_CONFIG_TREE_FILE), Const.CONFIG_TREE_FILE);
        }

        // Create missing files

        if (!new File(Const.CONFIG_RULES_FILE).exists() && 
                extractFile(Const.DEFAULT_CONFIG_FILE, Const.CONFIG_RULES_FILE)) {
            logInGame(Const.CONFIG_RULES_FILE + " missing, creating default one.");
        }
        if (!new File(Const.CONFIG_TREE_FILE).exists() && 
                extractFile(Const.DEFAULT_CONFIG_TREE_FILE, Const.CONFIG_TREE_FILE)) {
            logInGame(Const.CONFIG_TREE_FILE + " missing, creating default one.");
        }

        storedConfigLastModified = computeConfigLastModified();

        // Load

        String error = null;

        try {
            
            // Configuration creation
            if (config == null) {
                config = new InvTweaksConfig(Const.CONFIG_RULES_FILE, Const.CONFIG_TREE_FILE);
                inventoryAlgorithms = new InventoryAlgorithms(mc, config); // Load
                                                                           // algorithm
            }
            
            // Configuration loading
            config.load();
            
            // Convenient Inventory compatibility: Disable middle clicking
            // (Only once, by registering a new property. The player can then
            // change this by editing both mods' configuration files).
            if (config.getProperty(InvTweaksConfig.PROP_CONVENIENT_INVENTORY_COMPATIBILITY) == null) {
                try {
                    
                    Class convenientInventory = Class.forName("ConvenientInventory");
                    boolean defaultCISortingShortcutEnabled = false;
                    logInGame("1"); //XXXXXXXXXXXXX
                    // Look for the default sorting shortcut (middle click) in CI settings.
                    List<LinkedList<Integer>> actionMap[][] = (List[][]) convenientInventory.
                            getField("actionMap").get(null);
                    if (actionMap[7] != null) { // 7 = SORT
                        logInGame("2"); //XXXXXXXXXXXXX
                        for (List<LinkedList<Integer>> combo : actionMap[7]) {
                            logInGame("3"); //XXXXXXXXXXXXX
                            if (combo != null && combo.size() == 1) {
                                logInGame("4"); //XXXXXXXXXXXXX
                                LinkedList<Integer> buttons = combo.get(0);
                                if (buttons != null && buttons.size() == 1
                                        && buttons.get(0) == 2) { // 2 = Middle click
                                    logInGame("5"); //XXXXXXXXXXXXX
                                    defaultCISortingShortcutEnabled = true;
                                    break;
                                }
                            }
                        }
                    }
                    
                    // If CI's middle click is enabled, disable InvTweaks shortcut
                    if (defaultCISortingShortcutEnabled) {
                        config.setProperty(InvTweaksConfig.PROP_ENABLE_AUTOREPLACE_SOUND, "false");
                        config.setProperty(InvTweaksConfig.PROP_CONVENIENT_INVENTORY_COMPATIBILITY, "true");
                    }
                    
                }
                catch (ClassNotFoundException e) {
                    logInGame("not founf"); //XXXXXXXXXXXXX
                    // Failed to find Convenient Inventory class, not a problem
                }
                catch (Exception e) {
                    logInGameError("Failed to manage Convenient Inventory compatibility", e);
                }
            }
            
            log.setLevel(config.getLogLevel());
            logInGame("Configuration loaded");
            showConfigErrors(config);
        } catch (FileNotFoundException e) {
            error = "Config file not found";
        } catch (Exception e) {
            error = "Error while loading config: " + e.getMessage();
        }

        if (error != null) {
            logInGame(error);
            log.severe(error);
            config = null;
            return false;
        } else {
            return true;
        }
    }

    private void backupFile(File file, String baseName) {
        String newFileName;
        if (new File(baseName + ".bak").exists()) {
            int i = 1;
            while (new File(baseName + ".bak" + i).exists()) {
                i++;
            }
            newFileName = baseName + ".bak" + i;
        } else {
            newFileName = baseName + ".bak";
        }
        file.renameTo(new File(newFileName));
    }

    private boolean extractFile(String resource, String destination) {

        String resourceContents = "";
        URL resourceUrl = InvTweaks.class.getResource(resource);

        // Extraction from minecraft.jar
        if (resourceUrl != null) {
            try {
                Object o = resourceUrl.getContent();
                if (o instanceof InputStream) {
                    InputStream content = (InputStream) o;
                    while (content.available() > 0) {
                        byte[] bytes = new byte[content.available()];
                        content.read(bytes);
                        resourceContents += new String(bytes);
                    }
                }
            } catch (IOException e) {
                resourceUrl = null;
            }
        }

        // Extraction from mods folder
        if (resourceUrl == null) {

            File modFolder = new File(Const.MINECRAFT_DIR + File.separatorChar + "mods");

            File[] zips = modFolder.listFiles();
            if (zips != null && zips.length > 0) {
                for (File zip : zips) {
                    try {
                        ZipFile invTweaksZip = new ZipFile(zip);
                        ZipEntry zipResource = invTweaksZip.getEntry(resource);
                        if (zipResource != null) {
                            InputStream content = invTweaksZip.getInputStream(zipResource);
                            while (content.available() > 0) {
                                byte[] bytes = new byte[content.available()];
                                content.read(bytes);
                                resourceContents += new String(bytes);
                            }
                            break;
                        }
                    } catch (Exception e) {
                        log.warning("Failed to extract " + resource + " from mod: " + e.getMessage());
                    }
                }
            }
        }

        // Write to destination
        if (!resourceContents.isEmpty()) {
            try {
                FileWriter f = new FileWriter(destination);
                f.write(resourceContents);
                f.close();
                return true;
            } catch (IOException e) {
                logInGame("The mod won't work, because " + destination + " creation failed!");
                log.severe("Cannot create " + destination + " file: " + e.getMessage());
                return false;
            }
        } else {
            logInGame("The mod won't work, because " + resource + " could not be found!");
            log.severe("Cannot create " + destination + " file: " + resource + " not found");
            return false;
        }
    }

    private void playClick() {
        if (!config.getProperty(InvTweaksConfig.PROP_ENABLE_SORTING_SOUND).equals("false")) {
            mc.theWorld.playSoundAtEntity(getThePlayer(), "random.click", 0.2F, 1.8F);
        }
    }

    private void showConfigErrors(InvTweaksConfig config) {
        Vector<String> invalid = config.getInvalidKeywords();
        if (invalid.size() > 0) {
            String error = "Invalid keywords found: ";
            for (String keyword : config.getInvalidKeywords()) {
                error += keyword + " ";
            }
            logInGame(error);
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

    private class SettingsButton extends GuiButton {

        public SettingsButton(int id, int x, int y, int w, int h, String displayString) {
            super(id, x, y, w, h, displayString);
        }

        public void drawButton(Minecraft minecraft, int i, int j) {

            if (!enabled2) {
                return;
            }

            // TODO Refactoring
            // Draw little button
            // (use the 4 corners of the texture to fit best its small size)
            GL11.glBindTexture(3553, minecraft.renderEngine.getTexture("/gui/gui.png"));
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            boolean flag = i >= xPosition && j >= yPosition && i < xPosition + width && j < yPosition + height;
            int k = getHoverState(flag);
            drawTexturedModalRect(xPosition, yPosition, 1, 46 + k * 20 + 1, width / 2, height / 2);
            drawTexturedModalRect(xPosition, yPosition + height / 2, 1, 46 + k * 20 + 20 - height / 2 - 1, width / 2, height / 2);
            drawTexturedModalRect(xPosition + width / 2, yPosition, 200 - width / 2 - 1, 46 + k * 20 + 1, width / 2, height / 2);
            drawTexturedModalRect(xPosition + width / 2, yPosition + height / 2, 200 - width / 2 - 1, 46 + k * 20 + 19 - height / 2, width / 2,
                    height / 2);

            // Button status specific behaviour
            int textColor = 0xffe0e0e0;
            if (!enabled) {
                textColor = 0xffa0a0a0;
            } else if (flag) {
                textColor = 0xffffffa0;
            }

            // Display string
            drawCenteredString(mc.fontRenderer, displayString, xPosition + 5, yPosition - 1, textColor);
        }

        /**
         * Sort container
         */
        public boolean mousePressed(Minecraft minecraft, int i, int j) {
            if (super.mousePressed(minecraft, i, j)) {
                // Put hold item down if necessary
                SortableContainer container = new SortableContainer(mc, config, getPlayerContainer(), true);
                if (getHoldStack() != null) {
                    try {
                        container.putHoldItemDown();
                    } catch (TimeoutException e) {
                        logInGameError("Failed to put item down", e);
                    }
                }

                // Display menu
                mc.displayGuiScreen(new GuiInventorySettings(getCurrentScreen(), config));
                return true;
            } else {
                return false;
            }

        }

    }

    private class SortingButton extends GuiButton {

        private boolean buttonClicked = false;
        private Container container;
        private int algorithm;

        public SortingButton(int id, int x, int y, int w, int h, String displayString, Container container, int algorithm) {
            super(id, x, y, w, h, displayString);
            this.container = container;
            this.algorithm = algorithm;
        }

        public void drawButton(Minecraft minecraft, int i, int j) {

            if (!enabled2) {
                return;
            }

            // Draw little button
            // (use the 4 corners of the texture to fit best its small size)
            GL11.glBindTexture(3553, minecraft.renderEngine.getTexture("/gui/gui.png"));
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            boolean flag = i >= xPosition && j >= yPosition && i < xPosition + width && j < yPosition + height;
            int k = getHoverState(flag) - ((buttonClicked) ? 1 : 0);
            drawTexturedModalRect(xPosition, yPosition, 1, 46 + k * 20 + 1, width / 2, height / 2);
            drawTexturedModalRect(xPosition, yPosition + height / 2, 1, 46 + k * 20 + 20 - height / 2 - 1, width / 2, height / 2);
            drawTexturedModalRect(xPosition + width / 2, yPosition, 200 - width / 2 - 1, 46 + k * 20 + 1, width / 2, height / 2);
            drawTexturedModalRect(xPosition + width / 2, yPosition + height / 2, 200 - width / 2 - 1, 46 + k * 20 + 19 - height / 2, width / 2,
                    height / 2);

            // Button status specific behaviour
            int textColor = 0xffe0e0e0;
            if (!enabled) {
                textColor = 0xffa0a0a0;
            } else if (flag) {
                textColor = 0xffffffa0;
            }

            // Display symbol
            if (displayString.equals("h")) {
                drawRect(xPosition + 3, yPosition + 3, xPosition + width - 3, yPosition + 4, textColor);
                drawRect(xPosition + 3, yPosition + 6, xPosition + width - 3, yPosition + 7, textColor);
            } else if (displayString.equals("v")) {
                drawRect(xPosition + 3, yPosition + 3, xPosition + 4, yPosition + height - 3, textColor);
                drawRect(xPosition + 6, yPosition + 3, xPosition + 7, yPosition + height - 3, textColor);
            } else {
                drawRect(xPosition + 3, yPosition + 3, xPosition + width - 3, yPosition + 4, textColor);
                drawRect(xPosition + 5, yPosition + 4, xPosition + 6, yPosition + 5, textColor);
                drawRect(xPosition + 4, yPosition + 5, xPosition + 5, yPosition + 6, textColor);
                drawRect(xPosition + 3, yPosition + 6, xPosition + width - 3, yPosition + 7, textColor);
            }
        }

        /**
         * Sort container
         */
        public boolean mousePressed(Minecraft minecraft, int i, int j) {
            if (super.mousePressed(minecraft, i, j)) {
                try {
                    inventoryAlgorithms.sortContainer(container, false, algorithm);
                } catch (TimeoutException e) {
                    logInGameError("Failed to sort container", e);
                }
                return true;
            } else {
                return false;
            }

        }
    }
}
