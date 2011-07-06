package net.minecraft.src;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

public class InvTweaksAlgorithm extends InvTweaksObf {
    
    private static final Logger log = Logger.getLogger("InvTweaks");

    public static final int AUTOREPLACE_DELAY = 200;
    public static final int POLLING_DELAY = 3;
    public static final int POLLING_TIMEOUT = 1500;
    
    private InvTweaksConfig config = null;
    
    public InvTweaksAlgorithm(Minecraft mc, InvTweaksConfig config) {
		super(mc);
		setConfig(config);
	}
    
    public void setConfig(InvTweaksConfig config) {
    	this.config = config;
    }

	/**
	 * Sort inventory
	 * @return The number of clicks that were needed
     */
    public final long sortContainer(int windowId) // TODO: Use window ID
    {
    	synchronized (this) {
    	
    	// Do nothing if the inventory is closed
    	// if (!mc.hrrentScreen instanceof GuiContainer)
    	//		return;
    	
    	long timer = System.nanoTime();
		
		Vector<InvTweaksRule> rules = config.getRules();
		InvTweaksInventory inventory = new InvTweaksInventory(
				mc, config.getLockPriorities());

		//// Empty hand (needed in SMP)
		if (isMultiplayerWorld())
			inventory.putHoldItemDown();
		
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

			if (log.getLevel() == InvTweaks.DEBUG)
				log.info("Rule : "+rule.getKeyword()+"("+rulePriority+")");

			for (int i = 0; i < inventory.getSize(); i++) {
				ItemStack from = inventory.getItemStack(i);
	    		
	    		if (inventory.hasToBeMoved(i) && 
	    				inventory.getLockLevel(i) < rulePriority) {
					List<InvTweaksItem> fromItems = InvTweaksTree.getItems(
							getItemID(from), getItemDamage(from));
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
	    									getItemID(from), getItemDamage(from));
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
		
		// Default slot order init. In the inventory, indexes are :
		// 0 = bottom-left, 8 = bottom-right
		// 9 = top-left, 35 = 3rd-row-right
		int[] slotsOrder = new int[inventory.getSize()];
    	for (int i = 0; i < slotsOrder.length; i++) {
    		slotsOrder[i] = (i + 9) % slotsOrder.length;
    	}

		int iterations = 0;
		while (remaining.size() > 0 && iterations++ < 50) {
			for (int i : remaining) {
				if (inventory.hasToBeMoved(i)) {
					for (int j : slotsOrder) {
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

		if (log.getLevel() == InvTweaks.DEBUG) {
			timer = System.nanoTime()-timer;
			log.info("Sorting done in "
					+ inventory.getClickCount() + " clicks and "
					+ timer + "ns");
		}

    	return inventory.getClickCount();
    	
    	}
    }
    

    /**
     * Autoreplace + middle click sorting
     */
	public void autoReplaceSlot(int slot, int wantedId, int wantedDamage) {
    	
		InvTweaksInventory inventory = new InvTweaksInventory(
				mc, config.getLockPriorities());  	
		ItemStack candidateStack, replacementStack = null;
		ItemStack storedStack = createItemStack(wantedId, 1, wantedDamage);
		int selectedStackId = -1;
		
		// Search replacement
		for (int i = 0; i < InvTweaks.INVENTORY_SIZE; i++) {
			
			// Look only for a matching stack
			candidateStack = inventory.getItemStack(i);
			if (candidateStack != null && 
					inventory.areSameItem(storedStack, candidateStack) &&
					config.canBeAutoReplaced(
							getItemID(candidateStack),
							getItemDamage(candidateStack))) {
				// Choose stack of lowest sItemStacke and (in case of tools) highest damage
				if (replacementStack == null ||
						getStackSize(replacementStack) > getStackSize(candidateStack) ||
						(getStackSize(replacementStack) == getStackSize(candidateStack) &&
								getMaxStackSize(replacementStack) == 1 &&
								getItemDamage(replacementStack) < getItemDamage(candidateStack))) {
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
					this.expectedItemId = getItemID(inventory.getItemStack(i));
					this.i = i;
					return this;
				}
				
				public void run() {
					
					if (isMultiplayerWorld()) {
						// Wait for the server to confirm that the
						// slot is now empty
						int pollingTime = 0;
						setHasInventoryChanged(false);
						while(!hasInventoryChanged()
								&& pollingTime < POLLING_TIMEOUT) {
							trySleep(POLLING_DELAY);
						}
						if (pollingTime < AUTOREPLACE_DELAY)
							trySleep(AUTOREPLACE_DELAY - pollingTime);
						if (pollingTime >= POLLING_TIMEOUT)
							log.warning("Autoreplace timout");
					}
					else {
						trySleep(AUTOREPLACE_DELAY);
					}
					
					// In POLLING_DELAY ms, things might have changed
					try {
						ItemStack stack = inventory.getItemStack(i);
						if (stack != null && getItemID(stack) == expectedItemId) {
							inventory.moveStack(i, currentItem, Integer.MAX_VALUE);
						}
					}
					catch (NullPointerException e) {
						// Nothing: Due to multithreading + 
						// unsafe accesses, NPE may (very rarely) occur (?).
					}
					
			    	//onTickBusy = false; // TODO Check commenting this doesn't break SMP
				}
				
			}.init(inventory, selectedStackId, wantedId)).start();
			
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