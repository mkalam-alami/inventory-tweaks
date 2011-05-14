package net.minecraft.src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

public class InvTweaks {
	
	// (Note) In the inventory, indexes are :
	// 0 = bottom-left, 8 = bottom-right
	// 9 = top-left, 35 = 3rd-row-right
	
    private static final Logger log = Logger.getLogger("InvTweaks");

    public static final String CONFIG_FILE = Minecraft.getMinecraftDir()+"/ModSortButtonConfig.txt";
    public static final String CONFIG_TREE_FILE = Minecraft.getMinecraftDir()+"/ModSortButtonTree.txt";
    public static final String DEFAULT_CONFIG_FILE = "DefaultConfig.txt";
    public static final String DEFAULT_CONFIG_TREE_FILE = "DefaultTree.txt";
    public static final String INGAME_LOG_PREFIX = "SortButton: ";
    public static final Level LOG_LEVEL = Level.FINE;
    public static final int INV_SIZE = 36;
    public static final int HOT_RELOAD_DELAY = 1000;

    private InvTweaksConfig config = null;
    private long lastKeyPress = 0;
    private int keyPressDuration = 0;
    private boolean configErrorsShown = false;
	private int storedStackId = 0, storedPosition = -1;
    private Minecraft mc;
    
    public InvTweaks() {

    	log.setLevel(LOG_LEVEL);

		// Get Minecraft instance
    	mc = ModLoader.getMinecraftInstance();
    	
    	// Load config files
		tryLoading();
    	
    	log.info("Mod initialized");
    	
    }
    
	/**
	 * Sort inventory
	 */
    public final void onSortButtonPressed()
    {
    	// Do nothing if config loading failed
    	if (config == null) {
    		return;
    	}
    	
    	// Hot reload trigger
    	long currentTime = System.currentTimeMillis();
    	if (currentTime - lastKeyPress < 100) {
    		keyPressDuration += currentTime - lastKeyPress;
        	lastKeyPress = currentTime;
    		if (keyPressDuration > HOT_RELOAD_DELAY && keyPressDuration < 2*HOT_RELOAD_DELAY) {
    			tryLoading();
    			keyPressDuration = 2*HOT_RELOAD_DELAY; // Prevent from load repetition
    		}
    		else {
    			return;
    		}
    	}
    	else {
        	lastKeyPress = currentTime;
    		keyPressDuration = 0;
    	}
    	
    	// Config keywords error message
    	if (!configErrorsShown) {
    		showConfigErrors(config);
			configErrorsShown = true;
    	}
    	
    	// Do nothing if the inventory is closed
    	// if (!mc.currentScreen instanceof GuiContainer)
    	//		return;
    	
    	InvTweaksSortingLogic logic = new InvTweaksSortingLogic();
    	
    	try {
	    	mc.thePlayer.inventory.mainInventory =
	    		logic.sort(mc.thePlayer.inventory.mainInventory,
	    			config.getRules(), config.getLockedSlots());
    	}
    	catch (Exception e) {
    		log.severe("Sort failed: "+e.getMessage());
    	}
    	
    	// XXX Testing
    	if (mc.thePlayer.isJumping)
    		generateRandomInventory();
    		
    }
    
    public void onTick() {

    	// Auto-replace item stack
    	
    	ItemStack currentStack = mc.thePlayer.getCurrentEquippedItem();
    	int currentStackId = (currentStack == null) ? 0 : currentStack.itemID;
    	
    	if (currentStackId != storedStackId) {
    		InventoryPlayer inventory = mc.thePlayer.inventory;    		
	    	if (storedPosition != inventory.currentItem) { // Filter selection change
	    		storedPosition = inventory.currentItem;
	    	}
	    	else if (currentStackId == 0 &&
	    			inventory.getItemStack() == null) { // Filter item pickup from inv.
	    		ItemStack candidateStack;
	    		for (int i = 0; i < INV_SIZE; i++) {
	    			// Look only for an exactly matching ID
	    			candidateStack = inventory.mainInventory[i];
	    			if (candidateStack != null && candidateStack.itemID == storedStackId) {
	    				inventory.mainInventory[i] = null;
	    				inventory.mainInventory[inventory.currentItem] = candidateStack;
	    				break;
	    			}
	    		}
	    	}
	    	
	    	storedStackId = currentStackId;
    	}
    	
    }
    
    public static void inGameLog(String message) {
    	GuiIngame gui = ModLoader.getMinecraftInstance().ingameGUI;
    	if(gui != null)
    		gui.addChatMessage(INGAME_LOG_PREFIX + message);
    }
    
    /**
     * Tries to load mod configuration from file, with error handling.
     * @param config
     */
    private boolean tryLoading() {

    	// Create missing files
    	
    	if (!new File(CONFIG_FILE).exists()
    			&& copyFile(DEFAULT_CONFIG_FILE, CONFIG_FILE)) {
    		inGameLog(CONFIG_FILE+" missing, creating default one.");
		}
    	if (!new File(CONFIG_TREE_FILE).exists()
    			&& copyFile(DEFAULT_CONFIG_TREE_FILE, CONFIG_TREE_FILE)) {
    		inGameLog(CONFIG_TREE_FILE+" missing, creating default one.");
		}
    	
    	// Load
    	
		try {
	    	InvTweaksTree.loadTreeFromFile(CONFIG_TREE_FILE);
	    	if (config == null) {
	    		config = new InvTweaksConfig(CONFIG_FILE);
	    	}
			config.load();
			inGameLog("Configuration reloaded");
			showConfigErrors(config);
	    	return true;
		} catch (FileNotFoundException e) {
			String error = "Config file not found";
			inGameLog(error);
			log.severe(error);
	    	return false;
		} catch (IOException e) {
			String error = "Could not read config file";
			inGameLog(error);
			log.severe(error + " : " + e.getMessage());
	    	return false;
		}
    }

    private static void showConfigErrors(InvTweaksConfig config) {
    	Vector<String> invalid = config.getInvalidKeywords();
    	if (invalid.size() > 0) {
			String error = "Invalid keywords found (";
			for (String keyword : config.getInvalidKeywords()) {
				error += keyword+" ";
			}
			error.replaceFirst(" $", ")");
			inGameLog(error);
    	}
    }
    
    private boolean copyFile(String resource, String destination) {
    	
		URL resourceUrl = InvTweaks.class.getResource(resource);
		
		if (resourceUrl != null) {
			try  {
				Object o = resourceUrl.getContent();
				if (o instanceof InputStream) {
					InputStream content = (InputStream) o;
					String result = "";
					while (content.available() > 0) {
						byte[] bytes = new byte[content.available()];
						content.read(bytes);
						result += new String(bytes);
					}
					FileWriter f = new FileWriter(destination);
					f.write(result);
					f.close();
				}
				return true;
			}
			catch (IOException e) {
				log.severe("Cannot create "+destination+" file: "+e.getMessage());
				return false;
			}
		}
		else {
			log.severe("Source file "+resource+" doesn't exist, cannot create config file");
			return false;
		}
   	}
    
    /**
     * For testing
     */
    private void generateRandomInventory() {
    	Random r = new Random();
    	ItemStack[] inv = mc.thePlayer.inventory.mainInventory;
    	for (int i = 0; i < INV_SIZE; i++) {
    		if (r.nextInt() % 2 > 0) {
    			inv[i] = new ItemStack(r.nextInt() % 300, 1, 0);
    			inv[i].stackSize = r.nextInt() % inv[i].getMaxStackSize();
    		}
    	}
    }
    
}
