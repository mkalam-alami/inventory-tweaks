package net.minecraft.src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.invtweaks.Obfuscation;
import net.invtweaks.config.InventoryConfig;
import net.invtweaks.config.InventoryConfigRule;
import net.invtweaks.gui.GuiInventorySettings;
import net.invtweaks.logic.InventoryAlgorithm;
import net.invtweaks.logic.SortableContainer;
import net.invtweaks.tree.ItemTreeItem;
import net.invtweaks.tree.ItemTree;
import net.minecraft.client.Minecraft;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class InvTweaks extends Obfuscation {

    private static final Logger log = Logger.getLogger("InvTweaks");
    
    public static final String MINECRAFT_DIR = 
    	(getMinecraftDir().getAbsolutePath().endsWith(".")) ?
    			getMinecraftDir().getAbsolutePath().substring(
    			0, getMinecraftDir().getAbsolutePath().length()-1) :
    				getMinecraftDir().getAbsolutePath();
    public static final String CONFIG_RULES_FILE = MINECRAFT_DIR+"InvTweaksConfig.txt";
    public static final String CONFIG_TREE_FILE = MINECRAFT_DIR+"InvTweaksTree.xml";
    public static final String OLD_CONFIG_TREE_FILE = MINECRAFT_DIR+"InvTweaksTree.txt"; 
    public static final String DEFAULT_CONFIG_FILE = "/net/invtweaks/DefaultConfig.dat";
    public static final String DEFAULT_CONFIG_TREE_FILE = "/net/invtweaks/DefaultTree.dat";
    public static final String HELP_URL = "http://wan.ka.free.fr/?invtweaks#doc";
	
    public static final String INGAME_LOG_PREFIX = "InvTweaks: ";
    public static final Level DEFAULT_LOG_LEVEL = Level.WARNING;
    public static final Level DEBUG = Level.INFO;
    
    public static final int HOT_RELOAD_DELAY = 1000;
    public static final int INVENTORY_SIZE = 36;
	public static final int INVENTORY_ROW_SIZE = 9;
	public static final int INVENTORY_HOTBAR_SIZE = INVENTORY_ROW_SIZE;
    public static final int AUTOREPLACE_DELAY = 200;
    public static final int POLLING_DELAY = 3;
    public static final int POLLING_TIMEOUT = 1500;
    public static final int CHEST_ALGORITHM_SWAP_MAX_INTERVAL = 3000;
    public static final int PLAYER_INVENTORY_WINDOW_ID = 0;
	public static final int SORTING_TIMEOUT = 2999; // > POLLING_TIMEOUT
    public static final int JIMEOWAN_ID = 54696386;

	private static InvTweaks instance;
    private InventoryConfig config = null;
    private InventoryAlgorithm sortingAlgorithm = null;
    private long storedConfigLastModified = 0;
	private int storedStackId = 0, storedStackDamage = -1, storedFocusedSlot = -1;
	private int chestAlgorithm = InventoryAlgorithm.DEFAULT;
	private long chestAlgorithmClickTimestamp = 0; 
	private boolean chestAlgorithmButtonDown = false;
	private ItemStack[] hotbarClone = new ItemStack[INVENTORY_HOTBAR_SIZE];
	private Random rand = new Random();
    
    public InvTweaks(Minecraft mc) {

    	super(mc);
    	
    	log.setLevel(DEFAULT_LOG_LEVEL);
    	
    	// Store instance
    	instance = this;
    	
    	// Load config files
		if (makeSureConfigurationIsLoaded()) {
	    	sortingAlgorithm = new InventoryAlgorithm(mc, config); // Load algorithm
	    	log.info("Mod initialized");
		}
		else {
	    	log.severe("Mod failed to initialize!");
		}
    	
    }
    
	public static InvTweaks getInstance() {
		return instance;
	}

    public final void onSortingKeyPressed() {
    	synchronized (this) {
			
	    	// Check config loading success & current GUI
	    	GuiScreen guiScreen = getCurrentScreen();
	    	
	    	if (!isConfigLoaded() && !loadConfig()) {
	    		return;
	    	}
			if (guiScreen != null && !(guiScreen instanceof GuiContainer) /* GuiContainer */) {
				return;
			}
	    	
	    	handleSorting(guiScreen);
		}
    }

    public void onItemPickup(ItemStack stack) {
    	
    	if (!isConfigLoaded() && !loadConfig()) {
    		return;
    	}
    	
    	SortableContainer container = new SortableContainer(mc,
    			config, getPlayerContainer(), true);
    	
    	// Find stack slot (look in hotbar only)
    	int currentSlot = -1;
    	for (int i = 0; i < INVENTORY_HOTBAR_SIZE; i++) { // TODO const
    		ItemStack currentHotbarStack = container.getItemStack(i+27);
    		// Don't move already started stacks
    		if (currentHotbarStack != null && currentHotbarStack.animationsToGo == 5
    				&& hotbarClone[i] == null) {
    			currentSlot = i+27;
    		}
    	}
    	
    	if (currentSlot != -1) {
    	
	    	// Find preffered slots
	    	int[] prefferedPositions = null;
	    	InventoryConfigRule matchingRule = null;
	    	ItemTree tree = config.getTree();
	    	List<ItemTreeItem> items = tree.getItems(getItemID(stack), getItemDamage(stack));
	    	for (InventoryConfigRule rule : config.getRules()) {
	    		if (tree.matches(items, rule.getKeyword())) {
	    			prefferedPositions = rule.getPreferredPositions();
	    			matchingRule = rule;
	    			break;
	    		}
	    	}

    		// Find best slot for stack
	    	if (prefferedPositions != null) {
	    		for (int newSlot : prefferedPositions) {
	    			try {
	    				if (container.getItemStack(newSlot) == null
	    						&& container.moveStack(currentSlot,
	    								newSlot, matchingRule.getPriority())) {
							break;
						}
					} catch (TimeoutException e) {
						logInGame("Failed to move picked up stack", e);
					}
	    		}
	    	}
	    	
    		// Else, put the slot anywhere
	    	if (container.hasToBeMoved(currentSlot)) {
	        	for (int i = 0; i < container.getSize(); i++) {
	    			try {
	    				if (container.getItemStack(i) == null) {
	    					if (container.moveStack(currentSlot, i, Integer.MAX_VALUE)) {
	    						break;
	    					}
	    				}
					} catch (TimeoutException e) {
						logInGame("Failed to move picked up stack", e);
					}
	        	}
	    	}
    		
    	}
	}

	public void onTickInGame() {
		if (!isConfigLoaded())
			return;
		synchronized (this) {
			cloneHotbar();
			GuiScreen guiScreen = getCurrentScreen();
			handleMiddleClick(guiScreen);
			handleAutoReplace();
		}
	}
    
    public void onTickInGUI(GuiScreen guiScreen) {
		if (!isConfigLoaded())
			return;
		synchronized (this) {
			if (isChestOrDispenser(guiScreen)) {
				cloneHotbar();
			}
	    	handleGUILayout(guiScreen);
			handleOptionsMenuLayout(guiScreen);
		}
    }

	public void logInGame(String message) {
		String formattedMsg = buildlogString(Level.INFO, message);
		addChatMessage(formattedMsg);
    	log.info(formattedMsg);
    }
	
	public void logInGame(String message, Exception e) {
		String formattedMsg = buildlogString(Level.SEVERE, message, e);
		addChatMessage(formattedMsg);
    	log.severe(formattedMsg);
    }
	
	public boolean isConfigLoaded() {
		return config != null;
	}

	/**
	 * Allows to maintain a clone of the hotbar contents
	 * to track changes (especially needed by the "on pickup"
	 * features).
	 */
	private void cloneHotbar() {
		ItemStack[] mainInventory = getMainInventory();
		for (int i = 0; i < 9; i++) {
			if (mainInventory[i] != null) {
				hotbarClone[i] = mainInventory[i].copy();
			}
			else {
				hotbarClone[i] = null;
			}
		}
	}
	
	private void handleSorting(GuiScreen guiScreen) {

    	if (!makeSureConfigurationIsLoaded()) {
    		return;
    	}

    	ItemStack selectedItem = getItemStack(
    			getMainInventory(),
    			getFocusedSlot());
    	
    	try {
			sortingAlgorithm.sortContainer(
					(guiScreen == null) ? getPlayerContainer() : getContainer((GuiContainer) guiScreen),  /* GuiContainer */
					true, InventoryAlgorithm.INVENTORY);
		} catch (TimeoutException e) {
			logInGame("Failed to sort inventory: "+e.getMessage());
		}
		
    	// This needs to be remembered so that the
    	// autoreplace feature doesn't trigger
    	if (selectedItem != null && 
    			getItemStack(getMainInventory(), getFocusedSlot()) == null) {
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
				if (button.id == JIMEOWAN_ID) {
					customButtonsAdded = true;
					break;
				}
			}
			
			if (!customButtonsAdded) {
				
				// Inventory button
				if (!isContainer) {
					guiScreen.controlList.add(new SettingsButton(
							JIMEOWAN_ID,
							guiScreen.width/2 + 73,
							guiScreen.height/2 - 78,
							w, h, "..."));
				}
				
				// Chest buttons
				else {
	
					GuiContainer guiContainer = (GuiContainer) guiScreen;
					int id = JIMEOWAN_ID,
						x = guiContainer.xSize/2 + guiContainer.width/2 - 17,
						y = (guiContainer.height - guiContainer.ySize)/2 + 5;
					
					// Sorting buttons
					
					Container container = getContainer((GuiContainer) guiScreen);
		
					GuiButton button = new SortingButton(
							id++, x-37, y, w, h, "s",
							container, InventoryAlgorithm.DEFAULT);
					guiContainer.controlList.add((GuiButton) button);
		
					button = new SortingButton(
							id++, x-25, y, w, h, "v",
							container, InventoryAlgorithm.VERTICAL);
					guiContainer.controlList.add((GuiButton) button);
					
					button = new SortingButton(
							id++, x-13, y, w, h, "h",
							container, InventoryAlgorithm.HORIZONTAL);
					guiContainer.controlList.add((GuiButton) button);
					
					// Settings button
	
					guiScreen.controlList.add(new SettingsButton(
							JIMEOWAN_ID, x-1, y, w, h, "..."));
						
				}
			}
		}

	}
	
	private void handleMiddleClick(GuiScreen guiScreen) {
		
		if (!makeSureConfigurationIsLoaded()) {
			return;
		}
		
		if (Mouse.isButtonDown(2) && config.isMiddleClickEnabled()) {
			if (!chestAlgorithmButtonDown) {
	    		chestAlgorithmButtonDown = true;
		    	
	    		
	        	if (isChestOrDispenser(guiScreen)) {
	        		
	        		// Check if the middle click target the chest or the inventory
	        		// (copied GuiContainer.getSlotAtPosition algorithm)
	        		GuiContainer guiContainer = (GuiContainer) guiScreen;
	    			Container container = getContainer((GuiContainer) guiScreen);
	    			int slotCount = getSlots(container).size();
	                int mouseX = (Mouse.getEventX() * guiContainer.width) / mc.displayWidth;
	                int mouseY = guiContainer.height - (Mouse.getEventY() * guiContainer.height) / mc.displayHeight - 1;
	    			int target = 0; // 0 = nothing, 1 = chest, 2 = inventory
	    			for(int i = 0; i < slotCount; i++) {
	    				Slot slot = getSlot(container, i);
	    		        int k = (guiContainer.width - guiContainer.xSize) / 2;
	    		        int l = (guiContainer.height - guiContainer.ySize) / 2;
	    		        if (mouseX-k >= slot.xDisplayPosition - 1 && mouseX-k < slot.xDisplayPosition + 16 + 1 
	    		        	&& mouseY-l >= slot.yDisplayPosition - 1 && mouseY-l < slot.yDisplayPosition + 16 + 1) {
	    	            	target = (i < slotCount - INVENTORY_SIZE) ? 1 : 2;
	    	            	break;
	    	            }
	    	        }
	    			
	    			if (target == 1) {
	    				long timestamp = System.currentTimeMillis();
	    				if (timestamp - chestAlgorithmClickTimestamp > CHEST_ALGORITHM_SWAP_MAX_INTERVAL) {
	    					chestAlgorithm = InventoryAlgorithm.DEFAULT;
	    				}
	    				try {
							sortingAlgorithm.sortContainer(container, false, chestAlgorithm);
						} catch (TimeoutException e) {
							logInGame("Failed to sort container", e);
						}
	    				chestAlgorithm = (chestAlgorithm + 1) % 3;
	    				chestAlgorithmClickTimestamp = timestamp;
	    			}
	    			else if (target == 2) {
	    				try {
							sortingAlgorithm.sortContainer(container, true, InventoryAlgorithm.INVENTORY);
						} catch (TimeoutException e) {
							logInGame("Failed to sort inventory", e);
						}
	    			}
	    			
	        	}
	        	else {
	        		handleSorting(guiScreen);
	        	}
			}
		}
		else {
			chestAlgorithmButtonDown = false;
		}
	}

	private void handleAutoReplace() {
		
		ItemStack currentStack = getFocusedStack();
		int currentStackId = (currentStack == null) ? 0 : getItemID(currentStack);
		int currentStackDamage = (currentStack == null) ? 0 : getItemDamage(currentStack);
		int focusedSlot = getFocusedSlot() + 27; // Convert to container slots index
		
		// Auto-replace item stack
		if (currentStackId != storedStackId
				|| currentStackDamage != storedStackDamage) {
			
	    	if (storedFocusedSlot != focusedSlot) { // Filter selection change
	    		storedFocusedSlot = focusedSlot;
	    	}
	    	else if ((currentStack == null ||
	    			getItemID(currentStack) == 281 && storedStackId == 282) // Handle eaten mushroom soup
	    			&&
	    			(getCurrentScreen() == null || // Filter open inventory or other window
	    			getCurrentScreen() instanceof GuiEditSign /* GuiEditSign */)) { 
		    		
	    			sortingAlgorithm.autoReplaceSlot(focusedSlot, 
	    					storedStackId, storedStackDamage);
	    		}
	    	}
	    	
		storedStackId = currentStackId;
		storedStackDamage = currentStackDamage;
		
	}

	/**
	 * Adds a button nicely, according to the last small button's position.
	 * (supports the addition of buttons by other mods)
	 * @param guiScreen
	 */
	private void handleOptionsMenuLayout(GuiScreen guiScreen) {
		
		if (guiScreen instanceof GuiOptions) {
			GuiOptions options = (GuiOptions) guiScreen;

			boolean optionsButtonAdded = false;
			for (Object controlItem : options.controlList) {
				if (controlItem instanceof GuiSmallButton
						&& ((GuiSmallButton) controlItem).id == JIMEOWAN_ID) {
					optionsButtonAdded = true;
					break;
				}
			}
			
			if (!optionsButtonAdded) {
				int maxY = 0, minX = 0, maxX = 0, maxYX = 0;
				for (Object controlItem : options.controlList) {
					if (controlItem instanceof GuiSmallButton ||
							controlItem instanceof GuiSlider) {
						int x = ((GuiButton) controlItem).xPosition,
							y = ((GuiButton) controlItem).yPosition;
						if (y >= maxY) {
							if (y == maxY) {
								maxYX = Math.max(maxYX, x);
							}
							else {
								maxYX = x;
							}
							maxY = y;
						}
						if (minX > x || minX == 0) {
							minX = x;
						}
						if (maxX < x) {
							maxX = x;
						}
					}
				}
				
				/*options.controlList.add(new OptionsButton(JIMEOWAN_ID, 
						(maxYX == minX) ? maxX : minX,
						(maxYX == minX) ? maxY : maxY + 24,
						150, 20, "Inventory"));*/
			}
		}
	}
	
	private boolean makeSureConfigurationIsLoaded() {
		long configLastModified = new File(CONFIG_RULES_FILE).lastModified() + 
			new File(CONFIG_TREE_FILE).lastModified();
		if (isConfigLoaded()) {
			// Check time of last edit for both configuration files.
			if (storedConfigLastModified != configLastModified) {
				storedConfigLastModified = configLastModified;
				return loadConfig(); // Reload
			}
			else
				return true;
		}
		else {
			storedConfigLastModified = configLastModified;
			return loadConfig(); // Reload
		}
	}

	/**
	 * Tries to load mod configuration from file, with error handling.
	 * If it fails, the config attribute will remain null.
	 * @param config
	 */
	private boolean loadConfig() {
	
		// Create missing files
		
		if (!new File(CONFIG_RULES_FILE).exists()
				&& extractFile(DEFAULT_CONFIG_FILE, CONFIG_RULES_FILE)) {
			logInGame(CONFIG_RULES_FILE+" missing, creating default one.");
		}
		if (!new File(CONFIG_TREE_FILE).exists()
				&& extractFile(DEFAULT_CONFIG_TREE_FILE, CONFIG_TREE_FILE)) {
			logInGame(CONFIG_TREE_FILE+" missing, creating default one.");
		}
		
		// Remove old file
		
		if (new File(OLD_CONFIG_TREE_FILE).exists()) {
			new File(OLD_CONFIG_TREE_FILE).renameTo(new File(OLD_CONFIG_TREE_FILE+".bak"));
		}
		
		// Load
		
		String error = null;
		
		try {
			if (config == null) {
				config = new InventoryConfig(
						CONFIG_RULES_FILE, CONFIG_TREE_FILE);
			}
			config.load();
			log.setLevel(config.getLogLevel());
			logInGame("Configuration loaded");
			showConfigErrors(config);
		} catch (FileNotFoundException e) {
			error = "Config file not found";
		} catch (Exception e) {
			error = "Error while loading config: "+e.getMessage();
		}
	
		if (error != null) {
			logInGame(error);
			log.severe(error);
			config = null;
		    return false;
		}
		else {
			return true;
		}
	}
	
	private boolean extractFile(String resource, String destination) {
	
		String resourceContents = "";
		URL resourceUrl = InvTweaks.class.getResource(resource);
		
		// Extraction from minecraft.jar
		if (resourceUrl != null) {
			try  {
				Object o = resourceUrl.getContent();
				if (o instanceof InputStream) {
					InputStream content = (InputStream) o;
					while (content.available() > 0) {
						byte[] bytes = new byte[content.available()];
						content.read(bytes);
						resourceContents += new String(bytes);
					}
				}
			}
			catch (IOException e) {
				resourceUrl = null;
			}
		}
		
		// Extraction from mods folder
		if (resourceUrl == null) {
			
			File modFolder = new File(MINECRAFT_DIR+File.separatorChar+"mods");
			
			File[] zips = modFolder.listFiles();
			if (zips != null && zips.length > 0) {
				for (File zip : zips) {
					try {
						ZipFile invTweaksZip = new ZipFile(zip);
						ZipEntry zipResource = invTweaksZip.getEntry(resource);
						if (zipResource != null) {
							InputStream content = invTweaksZip.
									getInputStream(zipResource);
							while (content.available() > 0) {
								byte[] bytes = new byte[content.available()];
								content.read(bytes);
								resourceContents += new String(bytes);
							}
							break;
						}
					} catch (Exception e) {
						log.warning("Failed to extract "+resource+" from mod: "+e.getMessage());
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
			}
			catch (IOException e) {
				logInGame("The mod won't work, because "+destination+" creation failed!");
				log.severe("Cannot create "+destination+" file: "+e.getMessage());
				return false;
			}
		}
		else {
			logInGame("The mod won't work, because "+resource+" could not be found!");
			log.severe("Cannot create "+destination+" file: "+resource+" not found");
			return false;
		}
	}

	private void showConfigErrors(InventoryConfig config) {
		Vector<String> invalid = config.getInvalidKeywords();
		if (invalid.size() > 0) {
			String error = "Invalid keywords found: ";
			for (String keyword : config.getInvalidKeywords()) {
				error += keyword+" ";
			}
			logInGame(error);
		}
	}

    
	private String buildlogString(Level level, String message, Exception e) {
    	return buildlogString(level, message) + ": " + e.getMessage();
    }
	private String buildlogString(Level level, String message) {
    	return INGAME_LOG_PREFIX + 
    		((level.equals(Level.SEVERE)) ? "[ERROR] " : "" ) +
    		message;
    }
    
    private class SettingsButton extends GuiButton {

		public SettingsButton(int id, int x, int y,
				int w, int h, String displayString) {
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
	        drawTexturedModalRect(xPosition, yPosition, 1,
	        		46 + k * 20 + 1, width / 2, height / 2);
	        drawTexturedModalRect(xPosition, yPosition + height / 2, 1,
	        		46 + k * 20 + 20 - height / 2 - 1, width / 2, height / 2);
	        drawTexturedModalRect(xPosition + width / 2, yPosition,
	        		200 - width / 2 - 1, 46 + k * 20 + 1, width / 2, height / 2);
	        drawTexturedModalRect(xPosition + width / 2, yPosition + height / 2,
	        		200 - width / 2 - 1, 46 + k * 20 + 19 - height / 2, 
	        		width / 2, height / 2);
	
	        // Button status specific behaviour
	    	int textColor = 0xffe0e0e0;
	        if (!enabled) {
	        	textColor = 0xffa0a0a0;
	        }
	        else if (flag) {
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
						logInGame("Failed to put item down", e);
					}
	    		}
	    			
				// Display menu
	    		mc.displayGuiScreen(new GuiInventorySettings(getCurrentScreen()));
	    		return true;
	    	}
	    	else {
	    		return false;
	    	}
    		
	    }
		
	}

	/*private class OptionsButton extends GuiButton {

		public OptionsButton(int id, int x, int y,
				int w, int h, String displayString) {
			super(id, x, y, w, h, displayString);
		}
		
	    public boolean mousePressed(Minecraft minecraft, int i, int j) {
	    	if (super.mousePressed(minecraft, i, j)) {
	    		mc.displayGuiScreen(new InvTweaksGuiOptions(getCurrentScreen()));
	    		return true;
	    	}
	    	else {
	    		return false;
	    	}
	    }
		
	}*/
	
	private class SortingButton extends GuiButton {
	
		private boolean buttonClicked = false;
		private Container container;
		private int algorithm;
		
		public SortingButton(int id, int x, int y,
				int w, int h, String displayString,
				Container container, int algorithm) {
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
	        int k = getHoverState(flag) - ((buttonClicked) ? 1:0);
	        drawTexturedModalRect(xPosition, yPosition, 1,
	        		46 + k * 20 + 1, width / 2, height / 2);
	        drawTexturedModalRect(xPosition, yPosition + height / 2, 1,
	        		46 + k * 20 + 20 - height / 2 - 1, width / 2, height / 2);
	        drawTexturedModalRect(xPosition + width / 2, yPosition,
	        		200 - width / 2 - 1, 46 + k * 20 + 1, width / 2, height / 2);
	        drawTexturedModalRect(xPosition + width / 2, yPosition + height / 2,
	        		200 - width / 2 - 1, 46 + k * 20 + 19 - height / 2, 
	        		width / 2, height / 2);
	
	        // Button status specific behaviour
	    	int textColor = 0xffe0e0e0;
	        if (!enabled) {
	        	textColor = 0xffa0a0a0;
	        }
	        else if (flag) {
	        	textColor = 0xffffffa0;
	        }
	        
	        // Display symbol
	        if (displayString.equals("h")) {
	        	drawRect(xPosition + 3, yPosition + 3, xPosition + width - 3, yPosition + 4, textColor);
	        	drawRect(xPosition + 3, yPosition + 6, xPosition + width - 3, yPosition + 7, textColor);
	        }
	        else if (displayString.equals("v")) {
	        	drawRect(xPosition + 3, yPosition + 3, xPosition + 4, yPosition + height - 3, textColor);
	        	drawRect(xPosition + 6, yPosition + 3, xPosition + 7, yPosition + height - 3, textColor);
	        }
	        else {
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
					sortingAlgorithm.sortContainer(container, false, algorithm);
				} catch (TimeoutException e) {
					logInGame("Failed to sort container", e);
				}
	    		return true;
	    	}
	    	else {
	    		return false;
	    	}
    		
	    }
	}

}
