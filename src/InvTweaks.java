package net.minecraft.src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

public class InvTweaks {
	
    private static final Logger log = Logger.getLogger("InvTweaks");

    public static final String CONFIG_FILE = Minecraft.getMinecraftDir()+"/InvTweaksConfig.txt";
    public static final String CONFIG_TREE_FILE = Minecraft.getMinecraftDir()+"/InvTweaksTree.txt";
    public static final String DEFAULT_CONFIG_FILE = "DefaultConfig.txt";
    public static final String DEFAULT_CONFIG_TREE_FILE = "DefaultTree.txt";
    public static final String INGAME_LOG_PREFIX = "SortButton: ";
    public static final Level LOG_LEVEL = Level.FINE;
    public static final int HOT_RELOAD_DELAY = 1000;
    
    private static final boolean logging = true;
    
	private static int[] ALL_SLOTS;
    private InvTweaksConfig config = null;
    private long lastKeyPress = 0;
    private int keyPressDuration = 0;
    private boolean configErrorsShown = false;
    private boolean selectedItemTookAwayBySorting = false;
	private int storedStackId = 0, storedPosition = -1;
    private Minecraft mc;
    private boolean doingBenchmarking = false;
	private ItemStack autoReplaceBackup;
    
    public InvTweaks(Minecraft minecraft) {

    	log.setLevel(LOG_LEVEL);
    	
    	mc = minecraft;
    	
    	// Default slot order init. In the inventory, indexes are :
		// 0 = bottom-left, 8 = bottom-right
		// 9 = top-left, 35 = 3rd-row-right
    	if (ALL_SLOTS == null) {
			ALL_SLOTS = new int[InvTweaksInventory.SIZE];
	    	for (int i = 0; i < ALL_SLOTS.length; i++) {
	    		ALL_SLOTS[i] = (i + 9) % InvTweaksInventory.SIZE;
	    	}
		}
    	
    	// Load config files
		tryLoading();
    	
    	log.info("Mod initialized");
    	
    }
    
	/**
	 * Sort inventory
	 * @return The number of clicks that were needed
	 */
    public final long onSortButtonPressed()
    {
    	
    	// Do nothing if config loading failed
    	if (config == null) {
    		return -1;
    	}
    	
    	// Hot reload trigger
    	long currentTime = System.currentTimeMillis();
    	if (currentTime - lastKeyPress < 100 && !doingBenchmarking) {
    		keyPressDuration += currentTime - lastKeyPress;
        	lastKeyPress = currentTime;
    		if (keyPressDuration > HOT_RELOAD_DELAY && keyPressDuration < 2*HOT_RELOAD_DELAY) {
    			tryLoading(); // Hot-reload
    			keyPressDuration = 2*HOT_RELOAD_DELAY; // Prevent from load repetition
    		}
    		else {
    			return -1;
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
    	
    	long timer = System.nanoTime();
		InventoryPlayer invPlayer = mc.thePlayer.inventory;
		ItemStack selectedItem = invPlayer.mainInventory[invPlayer.currentItem];
		
		Vector<InvTweaksRule> rules = config.getRules();
		InvTweaksInventory inventory = new InvTweaksInventory(
				mc, config.getLockedSlots(), logging);

    	//// Merge stacks
		if (logging)
			log.info("Merging stacks.");
    	
    	// TODO: Lower complexity from 36² to 36.log(36)+36
    	// (sort items by increasing priority, then 1 pass is enough)
    	for (int i = inventory.getSize()-1; i >= 0; i--) {
    		ItemStack from = inventory.getItemStack(i);
    		if (from != null) {
    	    	for (int j = inventory.getSize()-1; j >= 0; j--) {
    	    		ItemStack to = inventory.getItemStack(j);
    	    		if (i != j && to != null
    	    				&& from.itemID == to.itemID
    	    				&& inventory.mergeStacks(i, j) 
    	    					== InvTweaksInventory.STACK_EMPTIED) {
    	    			break;
    	    		}
    	    	}
    		}
    	}
    	
    	//// Apply rules
		if (logging)
			log.info("Applying rules.");
    	
    	// Sorts rule by rule, themselves being already sorted by decreasing priority
		Iterator<InvTweaksRule> rulesIt = rules.iterator();
		while (rulesIt.hasNext()) {
			
			InvTweaksRule rule = rulesIt.next();
			int rulePriority = rule.getPriority();

			if (logging)
				log.info("Rule : "+rule.getKeyword()+"("+rulePriority+")");

			for (int i = 0; i < inventory.getSize(); i++) {
	    		ItemStack from = inventory.getItemStack(i);
	    		
	    		if (inventory.hasToBeMoved(i) && inventory.getLockLevel(i) < rulePriority) {
					InvTweaksItem fromItem = InvTweaksTree.getItem(from.itemID);
					
	    			if (InvTweaksTree.matches(fromItem, rule.getKeyword())) {
	    				
	    				int[] preferredPos = rule.getPreferredPositions();
	    				for (int j : preferredPos) {
	    					if (inventory.moveStack(i, j, rulePriority)) {
	    						break;
		    				}
	    				}
	    			}
	    		}
			}
		}
    	
		//// Don't move locked stacks
		if (logging)
			log.info("Locking stacks.");
		
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.hasToBeMoved(i) && inventory.getLockLevel(i) > 0) {
				inventory.markAsMoved(i);
			}
		}
    	
		//// Sort remaining
		if (logging)
			log.info("Sorting remaining.");
		
		Vector<Integer> remaining = new Vector<Integer>(), nextRemaining = new Vector<Integer>();
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.hasToBeMoved(i)) {
				remaining.add(i);
				nextRemaining.add(i);
			}
		}
		
		int iterations = 0;
		while (remaining.size() > 0 && iterations++ < 50) {
			for (int i : remaining) {
				if (inventory.hasToBeMoved(i)) {
					for (int j : ALL_SLOTS) {
						if (inventory.moveStack(i, j, 1)) {
							nextRemaining.remove((Object) j);
							break;
						}
					}
				}
				else {
					nextRemaining.remove((Object) i);
				}
			}
			remaining.clear();
			remaining.addAll(nextRemaining);
		}
		if (iterations == 50) {
			log.info("Sorting takes too long, aborting.");
		}

		if (logging) {
			timer = System.nanoTime()-timer;
			log.info("Sorting done in "
					+ inventory.getClickCount() + " clicks and "
					+ timer + "ns");
		}
		
    	// This needs to be remembered so that the autoreplace tool doesn't trigger
    	if (selectedItem != null && invPlayer.mainInventory[invPlayer.currentItem] == null)
    		selectedItemTookAwayBySorting = true;
    	
    	return inventory.getClickCount();
    }
    
    public void onTick() {

    	ItemStack currentStack = mc.thePlayer.getCurrentEquippedItem();
    	int currentStackId = (currentStack == null) ? 0 : currentStack.itemID;
		int currentItem = mc.thePlayer.inventory.currentItem;  
    	
    	// Protection against what seems to be a server bug
    	if (autoReplaceBackup != null && currentStack == null) {
    		mc.thePlayer.inventory.mainInventory[currentItem] = autoReplaceBackup;
    		autoReplaceBackup = null;
    	}

    	// Auto-replace item stack
    	if (currentStackId != storedStackId) {

    		InvTweaksInventory inventory = new InvTweaksInventory(
    				mc, config.getLockedSlots(), logging);  	
    		
	    	if (storedPosition != currentItem) { // Filter selection change
	    		storedPosition = currentItem;
	    	}
	    	else {
	    		
	    		if (selectedItemTookAwayBySorting) // Filter inentory sorting
	    			selectedItemTookAwayBySorting = false;
	    		else if (currentStackId == 0 &&
	    				mc.thePlayer.inventory.getItemStack() == null) { // Filter item pickup from inv.
		    		ItemStack candidateStack;
		    		for (int i = 0; i < InvTweaksInventory.SIZE; i++) {
		    			// Look only for an exactly matching ID
		    			candidateStack = inventory.getItemStack(i);
	    				// TODO: Choose stack of lowest size
		    			if (candidateStack != null && candidateStack.itemID == storedStackId) {
		    				if (logging)
		    					log.info("Automatic stack replacement.");
		    				
		    				inventory.moveStack(i, currentItem, Integer.MAX_VALUE);
		    				
		    				// TODO: Synchronize
		    				if (mc.isMultiplayerWorld()) {
		    					autoReplaceBackup = inventory.getItemStack(currentItem); 
		    				}
		    				
		    				break;
		    			}
		    		}
	    		}
	    	}
	    	
	    	storedStackId = currentStackId;
    	}
    	
    }
    
    public static void logInGame(String message) {
    	GuiIngame gui = ModLoader.getMinecraftInstance().ingameGUI;
    	if(gui != null)
    		gui.addChatMessage(INGAME_LOG_PREFIX + message);
    }
    
    /**
     * Allows to test algorithm performance in time and clicks,
     * by generating random inventories and trying to sort them.
     * Results are given with times in microseconds, following the format:
     *   [besttime timemean worsttime] [clicksmean worstclicks]
     * @param iterations The number of random inventories to sort.
     */
    public final void doBenchmarking(int iterations)
    {
    	// Note that benchmarking is also specific to
    	// a ruleset, a keyword tree, and the game mode (SP/SMP).
    	final int minOccupiedSlots = 0;
    	final int maxOccupiedSlots = InvTweaksInventory.SIZE;
    	final int maxDuplicateStacks = 5;
    	
    	ItemStack[] invBackup = mc.thePlayer.inventory.mainInventory.clone();
    	Random r = new Random();
    	long delay, totalDelay = 0, worstDelay = -1, bestDelay = -1,
    		clickCount, totalClickCount = 0, worstClickCount = -1;
    	
    	doingBenchmarking = true;
    	
    	for (int i = 0; i < iterations; i++) {
    		
    		// Generate random inventory
    		
    		int stackCount = r.nextInt(maxOccupiedSlots-minOccupiedSlots)+minOccupiedSlots;
    		ItemStack[] inventory =  mc.thePlayer.inventory.mainInventory;
    		for (int j = 0; j < InvTweaksInventory.SIZE; j++) {
    			inventory[j] = null;
    		}
    		
    		int stacksOfSameID = 0, stackId = 1;
    		
    		for (int j = 0; j < stackCount; j++) {
    			if (stacksOfSameID == 0) {
    				stacksOfSameID = 1+r.nextInt(maxDuplicateStacks);
    				do {
    					stackId = InvTweaksTree.getRandomItem(r).getId();
    				} while (stackId <= 0); // Needed or NPExc. may occur, don't know why
    			}
    			
    			int k;
    			do {
    				k = r.nextInt(InvTweaksInventory.SIZE);
    			} while (inventory[k] != null);
    			
				inventory[k] = new ItemStack(stackId, 1, 0);
				inventory[k].stackSize = 1+r.nextInt(inventory[k].getMaxStackSize());
    			stacksOfSameID--;
    		}
    		
    		// Benchmark
    		
    		delay = System.nanoTime();
    		clickCount = onSortButtonPressed();
    		delay = System.nanoTime() - delay;
    		
    		totalDelay += delay;
    		totalClickCount += clickCount;
    		if (worstDelay < delay || worstDelay == -1) {
    			worstDelay = delay;
    		}
    		if (bestDelay > delay || bestDelay == -1) {
    			bestDelay = delay;
    		}
    		if (worstClickCount < clickCount || worstClickCount == -1) {
    			worstClickCount = clickCount;
    		}
    		
    	}
    	
    	doingBenchmarking = false;
    	
    	// Display results
    	String results = "Time: [" + bestDelay/1000 + " "
				+ (totalDelay/iterations/1000) + " " + worstDelay/1000 + "] "
				+ "Clicks : [" + (totalClickCount/iterations)
				+ " " + worstClickCount + "]";
    	log.info(results);
    	logInGame(results);
    	
    	// Restore inventory
    	mc.thePlayer.inventory.mainInventory = invBackup;
    	
    }
    	
    
    /**
     * Tries to load mod configuration from file, with error handling.
     * @param config
     */
    private boolean tryLoading() {

    	// Create missing files
    	
    	if (!new File(CONFIG_FILE).exists()
    			&& copyFile(DEFAULT_CONFIG_FILE, CONFIG_FILE)) {
    		logInGame(CONFIG_FILE+" missing, creating default one.");
		}
    	if (!new File(CONFIG_TREE_FILE).exists()
    			&& copyFile(DEFAULT_CONFIG_TREE_FILE, CONFIG_TREE_FILE)) {
    		logInGame(CONFIG_TREE_FILE+" missing, creating default one.");
		}
    	
    	// Load
    	
		try {
	    	InvTweaksTree.loadTreeFromFile(CONFIG_TREE_FILE);
	    	if (config == null) {
	    		config = new InvTweaksConfig(CONFIG_FILE);
	    	}
			config.load();
			logInGame("Configuration reloaded");
			showConfigErrors(config);
	    	return true;
		} catch (FileNotFoundException e) {
			String error = "Config file not found";
			logInGame(error);
			log.severe(error);
	    	return false;
		} catch (IOException e) {
			String error = "Could not read config file";
			logInGame(error);
			log.severe(error + " : " + e.getMessage());
	    	return false;
		}
    }

    private static void showConfigErrors(InvTweaksConfig config) {
    	Vector<String> invalid = config.getInvalidKeywords();
    	if (invalid.size() > 0) {
			String error = "Invalid keywords found: ";
			for (String keyword : config.getInvalidKeywords()) {
				error += keyword+" ";
			}
			logInGame(error);
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
    
}
