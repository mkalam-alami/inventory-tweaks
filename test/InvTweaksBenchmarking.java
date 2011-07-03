import java.util.Random;
import java.util.logging.Logger;


public class InvTweaksBenchmarking extends InvTweaksObf {

    private static final Logger log = Logger.getLogger("InvTweaks (Test)");
    
	private InvTweaks invTweaks;
	
    public InvTweaksBenchmarking() {
		super(ModLoader.getMinecraftInstance());
		invTweaks = new InvTweaks(mc);
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
    	
    	iz[] invBackup = mc.h.c.a.clone();
    	Random r = new Random();
    	long delay, totalDelay = 0, worstDelay = -1, bestDelay = -1,
    		clickCount, totalClickCount = 0, worstClickCount = -1;
    	
    	synchronized (this) {
    	
	    	for (int i = 0; i < iterations; i++) {
	    		
	    		// Generate random inventory
	    		
	    		int stackCount = r.nextInt(maxOccupiedSlots-minOccupiedSlots)+minOccupiedSlots;
	    		iz[] inventory =  mc.h.c.a;
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
	    			
					inventory[k] = createItemStack(stackId, 1, 0);
					setStackSize(inventory[k], 1+r.nextInt(getMaxStackSize(inventory[k])));
	    			stacksOfSameID--;
	    		}
	    		
	    		// Benchmark
	    		
	    		delay = System.nanoTime();
	    		clickCount = invTweaks.sortContainer(0); // TODO
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
    	invTweaks.logInGame(results);
    	
    	// Restore inventory
    	mc.h.c.a = invBackup;
    	
    }

	
}
