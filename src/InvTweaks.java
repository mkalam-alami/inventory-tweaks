package net.minecraft.src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Mouse;

public class InvTweaks {

    private static final Logger log = Logger.getLogger("InvTweaks");
    
    public static final String CONFIG_FILE = Minecraft.getMinecraftDir()+"/InvTweaksConfig.txt";
    public static final String CONFIG_TREE_FILE = Minecraft.getMinecraftDir()+"/InvTweaksTree.txt";
    public static final String DEFAULT_CONFIG_FILE = "DefaultConfig.txt";
    public static final String DEFAULT_CONFIG_TREE_FILE = "DefaultTree.txt";
    public static final String INGAME_LOG_PREFIX = "InvTweaks: ";
    public static final Level DEFAULT_LOG_LEVEL = Level.WARNING;
    public static final Level DEBUG = Level.INFO;
    public static final int HOT_RELOAD_DELAY = 1000;
    public static final int AUTOREPLACE_DELAY = 200;
    public static final int POLLING_DELAY = 3;
    public static final int POLLING_TIMEOUT = 1500;
	private static int[] ALL_SLOTS;

	private static InvTweaks instance;
    private InvTweaksConfig config = null;
    private long lastKeyPress = 0;
    private int keyPressDuration = 0;
    private boolean configErrorsShown = false;
    private boolean onTickBusy = false;
	private int storedStackId = 0, storedStackDamage = -1, storedPosition = -1;
    private Minecraft mc;
    
    public InvTweaks(Minecraft minecraft) {

    	log.setLevel(DEFAULT_LOG_LEVEL);
    	
    	instance = this;
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
    
	public static InvTweaks getInstance() {
		return instance;
	}

	/**
	 * Sort inventory
	 * @return The number of clicks that were needed
	 */
    public final long sortInventory()
    {
    	// Check config loading success & current GUI
    	if (config == null ||
    			!(mc.currentScreen == null ||
    			mc.currentScreen instanceof GuiContainer)) {
    		return -1;
    	}
    	
    	synchronized (this) {
    		
    	// Hot reload trigger
    	long currentTime = System.currentTimeMillis();
    	if (currentTime - lastKeyPress < 100) {
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
    	// if (!mc.hrrentScreen instanceof GuiContainer)
    	//		return;
    	
    	long timer = System.nanoTime();
		InventoryPlayer invPlayer = mc.thePlayer.inventory;
		ItemStack selectedItem = invPlayer.mainInventory[invPlayer.currentItem];
		
		Vector<InvTweaksRule> rules = config.getRules();
		InvTweaksInventory inventory = new InvTweaksInventory(
				mc, config.getLockPriorities());

		//// Empty hand (needed in SMP)
		if (mc.isMultiplayerWorld())
			inventory.putSelectedItemDown();
		
    	//// Merge stacks to fill the ones in locked slots
		log.info("Merging stacks.");
		
		Vector<Integer> lockedSlots = config.getLockedSlots();
    	for (int i = inventory.getSize()-1; i >= 0; i--) {
    		ItemStack from = inventory.getItemStack(i);
    		if (from != null) {
    	    	for (Integer j : lockedSlots) {
    	    		ItemStack to = inventory.getItemStack(j);
    	    		if (to != null && inventory.canBeMerged(i, j)) {
    	    			boolean result = inventory.mergeStacks(i, j);
    	    			inventory.markAsNotMoved(j);
    	    			if (result == InvTweaksInventory.STACK_EMPTIED) {
        	    			break;
    	    			}
    	    		}
    	    	}
    		}
    	}
    	
    	//// Apply rules
		log.info("Applying rules.");
    	
    	// Sorts rule by rule, themselves being already sorted by decreasing priority
		Iterator<InvTweaksRule> rulesIt = rules.iterator();
		while (rulesIt.hasNext()) {
			
			InvTweaksRule rule = rulesIt.next();
			int rulePriority = rule.getPriority();

			if (log.getLevel() == DEBUG)
				log.info("Rule : "+rule.getKeyword()+"("+rulePriority+")");

			for (int i = 0; i < inventory.getSize(); i++) {
	    		ItemStack from = inventory.getItemStack(i);
	    		
	    		if (inventory.hasToBeMoved(i) && 
	    				inventory.getLockLevel(i) < rulePriority) {
					List<InvTweaksItem> fromItems = InvTweaksTree.getItems(
							from.itemID, from.getItemDamage());
	    			if (InvTweaksTree.matches(fromItems, rule.getKeyword())) {
	    				
	    				int[] preferredPos = rule.getPreferredPositions();
	    				for (int j = 0; j < preferredPos.length; j++) {
	    					int k = preferredPos[j];
	    					
	    					if (inventory.moveStack(i, k, rulePriority)) {
	    						from = inventory.getItemStack(i);
	    						if (from == null || i == k) {
	    							break;
	    						}
	    						else {
	    							fromItems = InvTweaksTree.getItems(
	    									from.itemID, from.getItemDamage());
	    							if (!InvTweaksTree.matches(
	    									fromItems, rule.getKeyword())) {
	    								break;
	    							}
	    							else {
	    								j--;
	    							}
	    						}
		    				}
	    				}
	    			}
	    		}
			}
		}
    	
		//// Don't move locked stacks
		log.info("Locking stacks.");
		
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.hasToBeMoved(i) && inventory.getLockLevel(i) > 0) {
				inventory.markAsMoved(i, 1);
			}
		}
    	
		//// Sort remaining
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

		if (log.getLevel() == DEBUG) {
			timer = System.nanoTime()-timer;
			log.info("Sorting done in "
					+ inventory.getClickCount() + " clicks and "
					+ timer + "ns");
		}
		
    	// This needs to be remembered so that the autoreplace feature doesn't trigger
    	if (selectedItem != null && 
    			invPlayer.mainInventory[invPlayer.currentItem] == null) {
    		storedStackId = 0;
    	}

    	return inventory.getClickCount();
    	
    	}
    }

    /**
     * Autoreplace + middle click sorting
     */
	public void onTick() {
    	
    	if (config == null || onTickBusy == true)
    		return;
    	
    	if (Mouse.isButtonDown(2) && config.isMiddleClickEnabled())
    		sortInventory();
    	
    	synchronized (this) {
    		
    	onTickBusy = true;
    	
    	ItemStack currentStack = mc.thePlayer.inventory.getCurrentItem();
    	ItemStack replacementStack = null;
    	int currentStackId = (currentStack == null) ? 0 : currentStack.itemID;
    	int currentStackDamage = (currentStack == null) ? 0 : currentStack.getItemDamage();
		int currentItem = mc.thePlayer.inventory.currentItem;
		
    	// Auto-replace item stack
    	if (currentStackId != storedStackId
    			|| currentStackDamage != storedStackDamage) {
    		
	    	if (storedPosition != currentItem) { // Filter selection change
	    		storedPosition = currentItem;
	    	}
	    	else if (currentStack == null &&
	    			(mc.currentScreen == null || 
	    			mc.currentScreen instanceof GuiEditSign)) { // Filter open inventory or other window
		    		
        		InvTweaksInventory inventory = new InvTweaksInventory(
        				mc, config.getLockPriorities());  	
    			ItemStack candidateStack;
    			ItemStack storedStack = new ItemStack(storedStackId, 1, storedStackDamage);
    			int selectedStackId = -1;
	    		
    			// Search replacement
    			for (int i = 0; i < InvTweaksInventory.SIZE; i++) {
    				
	    			// Look only for a matching stack
	    			candidateStack = inventory.getItemStack(i);
	    			if (candidateStack != null && 
	    					inventory.areSameItem(storedStack, candidateStack) &&
	    					config.canBeAutoReplaced(
	    							candidateStack.itemID,
	    							candidateStack.getItemDamage())) {
	    				// Choose stack of lowest size and (in case of tools) highest damage
	    				if (replacementStack == null ||
	    						replacementStack.stackSize > candidateStack.stackSize ||
	    						(replacementStack.stackSize == candidateStack.stackSize &&
	    								replacementStack.getMaxStackSize() == 1 &&
	    								replacementStack.getItemDamage() < candidateStack.getItemDamage())) {
	    					replacementStack = candidateStack;
	    					selectedStackId = i;
	    				}
	    			}
    			}
    			
    			// Proceed to replacement
    			if (replacementStack != null) {
    				
    				log.info("Automatic stack replacement.");
    				
				    /*
				     * This allows to have a short feedback 
				     * that the stack/tool is empty/broken.
				     */
					new Thread(new Runnable() {

						private InvTweaksInventory inventory;
						private int currentItem;
						private int i, expectedItemId;
						
						public Runnable init(
								InvTweaksInventory inventory,
								int i, int currentItem) {
							this.inventory = inventory;
							this.currentItem = currentItem;
							this.expectedItemId = inventory.getItemStack(i).itemID;
							this.i = i;
							return this;
						}
						
						public void run() {
							
							if (mc.isMultiplayerWorld()) {
								// Wait for the server to confirm that the
								// slot is now empty
								int pollingTime = 0;
								mc.thePlayer.inventory.inventoryChanged = false;
								while(!mc.thePlayer.inventory.inventoryChanged
										&& pollingTime < POLLING_TIMEOUT) {
									trySleep(POLLING_DELAY);
								}
								if (pollingTime < AUTOREPLACE_DELAY)
									trySleep(AUTOREPLACE_DELAY - pollingTime);
								if (pollingTime >= InvTweaks.POLLING_TIMEOUT)
									log.warning("Autoreplace timout");
							}
							else {
								trySleep(AUTOREPLACE_DELAY);
							}
							
							// In POLLING_DELAY ms, things might have changed
							try {
								ItemStack stack = inventory.getItemStack(i);
								if (stack != null && stack.itemID == expectedItemId) {
									inventory.moveStack(i, currentItem, Integer.MAX_VALUE);
								}
							}
							catch (NullPointerException e) {
								// Nothing: Due to multithreading + 
								// unsafe accesses, NPE may (very rarely) occur (?).
							}
							
					    	onTickBusy = false;
						}
						
					}.init(inventory, selectedStackId, currentItem)).start();
    				
	    		}
	    	}
	    	
	    	storedStackId = currentStackId;
	    	storedStackDamage = currentStackDamage;
    	}

		if (replacementStack == null)
	    	onTickBusy = false;
		
    	}
    }
	
    public void logInGame(String message) {
    	if(mc.ingameGUI != null)
    		mc.ingameGUI.addChatMessage(INGAME_LOG_PREFIX + message);
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
    	
    	synchronized (this) {
    	
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
	    		clickCount = sortInventory();
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
    	
    	}
    	
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
    	
    	String error = null;
    	
		try {
	    	InvTweaksTree.loadTreeFromFile(CONFIG_TREE_FILE);
	    	if (config == null) {
	    		config = new InvTweaksConfig(CONFIG_FILE);
	    	}
			config.load();
			log.setLevel(config.getLogLevel());
			logInGame("Configuration reloaded");
			showConfigErrors(config);
		} catch (FileNotFoundException e) {
			error = "Config file not found";
		} catch (Exception e) {
			error = "Error while loading config: "+e.getMessage();
		}
			
		if (error != null) {
			logInGame(error);
			log.severe(error);
		    return false;
		}
		else {
			return true;
		}
    }

    private void showConfigErrors(InvTweaksConfig config) {
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
			
			File modFolder = new File(Minecraft.getMinecraftDir().getPath()+
					File.separatorChar+"mods");
			
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

	public static void trySleep(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			// Do nothing
		}
    }
}
