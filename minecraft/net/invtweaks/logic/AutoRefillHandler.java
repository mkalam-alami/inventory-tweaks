package net.invtweaks.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import net.invtweaks.Const;
import net.invtweaks.config.InvTweaksConfig;
import net.invtweaks.config.SortingRule;
import net.invtweaks.config.SortingRule.RuleType;
import net.invtweaks.library.ContainerSectionManager;
import net.invtweaks.library.Obfuscation;
import net.invtweaks.library.ContainerManager.ContainerSection;
import net.invtweaks.tree.ItemTree;
import net.invtweaks.tree.ItemTreeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.src.ItemStack;

/**
 * Handles the auto-refilling of the hotbar.
 * 
 * @author Jimeo Wan
 *
 */
public class AutoRefillHandler extends Obfuscation {
    
    private static final Logger log = Logger.getLogger("InvTweaks");

    private InvTweaksConfig config = null;
    
    public AutoRefillHandler(Minecraft mc, InvTweaksConfig config) {
		super(mc);
		setConfig(config);
	}
    
    public void setConfig(InvTweaksConfig config) {
    	this.config = config;
    }
    
	/**
     * Auto-refill
	 * @throws Exception 
     */
	public void autoRefillSlot(int slot, int wantedId, int wantedDamage) throws Exception {
   
		ContainerSectionManager container = new ContainerSectionManager(
		        mc, ContainerSection.INVENTORY);
		ItemStack candidateStack, replacementStack = null;
		int replacementStackSlot = -1;

		//// Search replacement
		
		List<SortingRule> matchingRules = new ArrayList<SortingRule>();
		List<SortingRule> rules = config.getRules();
		ItemTree tree = config.getTree();
		List<ItemTreeItem> items = tree.getItems(wantedId, wantedDamage);

		// Find rules that match the slot
		for (ItemTreeItem item : items) {
			// Fake rules that match the exact item first
			matchingRules.add(new SortingRule(
					tree, "D"+(slot-27), item.getName(),
					Const.INVENTORY_SIZE, Const.INVENTORY_ROW_SIZE));
		}
		for (SortingRule rule : rules) {
			if (rule.getType() == RuleType.TILE || rule.getType() == RuleType.COLUMN) {
				for (int preferredSlot : rule.getPreferredSlots()) {
					if (slot == preferredSlot) {
						matchingRules.add(rule);
						break;
					}
				}
			}
		}

		// Look only for a matching stack
		// First, look for the same item,
		// else one that matches the slot's rules
		for (SortingRule rule : matchingRules) {
			for (int i = 0; i < Const.INVENTORY_SIZE; i++) {
				candidateStack = container.getItemStack(i);
				if (candidateStack != null) {
					List<ItemTreeItem> candidateItems = tree.getItems(
							getItemID(candidateStack),
							getItemDamage(candidateStack));
					if (tree.matches(candidateItems, rule.getKeyword())) {
						// Choose stack of lowest size and (in case of tools) highest damage
						if (replacementStack == null || 
								getStackSize(replacementStack) > getStackSize(candidateStack) ||
								(getStackSize(replacementStack) == getStackSize(candidateStack) &&
										getMaxStackSize(replacementStack) == 1 &&
										getItemDamage(replacementStack) < getItemDamage(candidateStack))) {
							replacementStack = candidateStack;
							replacementStackSlot = i;
						}
					}
				}
			}
			if (replacementStack != null) {
				break;
			}
		}
		
		//// Proceed to replacement
	
		if (replacementStack != null) {
			
			log.info("Automatic stack replacement.");
			
		    /*
		     * This allows to have a short feedback 
		     * that the stack/tool is empty/broken.
		     */
			new Thread(new Runnable() {

				private ContainerSectionManager containerMgr;
				private int targetedSlot;
				private int i, expectedItemId;
				
				public Runnable init(Minecraft mc,
						int i, int currentItem) throws Exception {
					this.containerMgr = new ContainerSectionManager(
					        mc, ContainerSection.INVENTORY);
					this.targetedSlot = currentItem;
					this.expectedItemId = getItemID(
					        containerMgr.getItemStack(i));
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
								&& pollingTime < Const.POLLING_TIMEOUT) {
							trySleep(Const.POLLING_DELAY);
						}
						if (pollingTime < Const.AUTO_REFILL_DELAY)
							trySleep(Const.AUTO_REFILL_DELAY - pollingTime);
						if (pollingTime >= Const.POLLING_TIMEOUT)
							log.warning("Autoreplace timout");
					}
					else {
						trySleep(Const.AUTO_REFILL_DELAY);
					}
					
					// In POLLING_DELAY ms, things might have changed
					try {
						ItemStack stack = containerMgr.getItemStack(i);
						if (stack != null && getItemID(stack) == expectedItemId) {
							if (containerMgr.move(i, targetedSlot)) {
								if (!config.getProperty(InvTweaksConfig.PROP_ENABLE_AUTO_REFILL_SOUND).equals("false")) {
					    			mc.theWorld.playSoundAtEntity(getThePlayer(), 
					    					"mob.chickenplop", 0.15F, 0.2F);
								}
								// If item are swapped (like for mushroom soups),
								// put the item back in the inventory if it is in the hotbar
								if (containerMgr.getItemStack(i) != null && i >= 27) {
									for (int j = 0; j < Const.INVENTORY_SIZE; j++) {
										if (containerMgr.getItemStack(j) == null) {
										    containerMgr.move(i, j);
											break;
										}
									}
								}
							}
							else {
								log.warning("Failed to move stack for autoreplace, despite of prior tests.");
							}
						}
					}
					catch (NullPointerException e) {
						// Nothing: Due to multithreading + 
						// unsafe accesses, NPE may (very rarely) occur (?).
					} catch (TimeoutException e) {
						log.severe("Failed to trigger autoreplace: "+e.getMessage());
					}
					
				}
				
			}.init(mc, replacementStackSlot, slot)).start();
			
		}
    }
	
	private static void trySleep(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			// Do nothing
		}
    }

}