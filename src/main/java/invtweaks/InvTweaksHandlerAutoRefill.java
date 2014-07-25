package invtweaks;

import invtweaks.api.IItemTreeItem;
import invtweaks.api.container.ContainerSection;
import invtweaks.forge.InvTweaksMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Handles the auto-refilling of the hotbar.
 *
 * @author Jimeo Wan
 */
public class InvTweaksHandlerAutoRefill extends InvTweaksObfuscation {

    private static final Logger log = InvTweaks.log;

    private InvTweaksConfig config = null;

    public InvTweaksHandlerAutoRefill(Minecraft mc, InvTweaksConfig config) {
        super(mc);
        setConfig(config);
    }

    public void setConfig(InvTweaksConfig config) {
        this.config = config;
    }

    /**
     * Auto-refill
     *
     * @throws Exception
     */
    public void autoRefillSlot(int slot, String wantedId, int wantedDamage) throws Exception {

        InvTweaksContainerSectionManager container = new InvTweaksContainerSectionManager(mc,
                                                                                          ContainerSection.INVENTORY);
        ItemStack candidateStack, replacementStack = null;
        int replacementStackSlot = -1;
        boolean refillBeforeBreak = config.getProperty(InvTweaksConfig.PROP_AUTO_REFILL_BEFORE_BREAK)
                                          .equals(InvTweaksConfig.VALUE_TRUE);
        boolean hasSubtypes = false;

        Item original = (Item)Item.itemRegistry.getObject(wantedId);
        if(original != null) {
            hasSubtypes = original.getHasSubtypes();
        }

        List<InvTweaksConfigSortingRule> matchingRules = new ArrayList<InvTweaksConfigSortingRule>();
        List<InvTweaksConfigSortingRule> rules = config.getRules();
        InvTweaksItemTree tree = config.getTree();

        // Check that the item is in the tree
        if(!tree.isItemUnknown(wantedId, wantedDamage)) {

            //// Search replacement

            List<IItemTreeItem> items = tree.getItems(wantedId, wantedDamage);

            // Find rules that match the slot
            for(IItemTreeItem item : items) {
                if(!hasSubtypes || (item.getDamage() == wantedDamage)) {
                    // Since we search a matching item using rules,
                    // create a fake one that matches the exact item first
                    matchingRules.add(new InvTweaksConfigSortingRule(tree, "D" + (slot - 26), item.getName(),
                                                                     InvTweaksConst.INVENTORY_SIZE,
                                                                     InvTweaksConst.INVENTORY_ROW_SIZE));
                }
            }
            for(InvTweaksConfigSortingRule rule : rules) {
                if(rule.getType() == InvTweaksConfigSortingRuleType.SLOT || rule
                        .getType() == InvTweaksConfigSortingRuleType.COLUMN) {
                    for(int preferredSlot : rule.getPreferredSlots()) {
                        if(slot == preferredSlot) {
                            matchingRules.add(rule);
                            break;
                        }
                    }
                }
            }

            // Look only for a matching stack
            // First, look for the same item,
            // else one that matches the slot's rules
            for(InvTweaksConfigSortingRule rule : matchingRules) {
                for(int i = 0; i < InvTweaksConst.INVENTORY_SIZE; i++) {
                    candidateStack = container.getItemStack(i);
                    if(candidateStack != null) {
                        List<IItemTreeItem> candidateItems = tree
                                .getItems(Item.itemRegistry.getNameForObject(candidateStack.getItem()), candidateStack.getItemDamage());
                        if(tree.matches(candidateItems, rule.getKeyword())) {
                            // Choose tool of highest damage value
                            if(candidateStack.getMaxStackSize() == 1) {
                                // Item
                                if((replacementStack == null || candidateStack.getItemDamage() > replacementStack
                                        .getItemDamage()) && (!refillBeforeBreak || candidateStack.getItem()
                                                                                                  .getMaxDamage() - candidateStack
                                        .getItemDamage() > config
                                        .getIntProperty(InvTweaksConfig.PROP_AUTO_REFILL_DAMAGE_THRESHHOLD))) {
                                    replacementStack = candidateStack;
                                    replacementStackSlot = i;
                                }
                            }
                            // Choose stack of lowest size
                            else if(replacementStack == null || candidateStack.stackSize < replacementStack.stackSize) {
                                replacementStack = candidateStack;
                                replacementStackSlot = i;
                            }
                        }
                    }
                }
            }
        }

        // If item is unknown, look for exact same item
        else {
            for(int i = 0; i < InvTweaksConst.INVENTORY_SIZE; i++) {
                candidateStack = container.getItemStack(i);
                if(candidateStack != null &&
                        ObjectUtils.equals(Item.itemRegistry.getNameForObject(candidateStack.getItem()), wantedId) &&
                        candidateStack.getItemDamage() == wantedDamage) {
                    replacementStack = candidateStack;
                    replacementStackSlot = i;
                    break;
                }
            }
        }

        //// Proceed to replacement

        if(replacementStack != null || (refillBeforeBreak && container.getSlot(slot).getStack() != null)) {

            log.info("Automatic stack replacement.");

		    /*
             * This allows to have a short feedback
		     * that the stack/tool is empty/broken.
		     */
            new Thread(new Runnable() {

                private InvTweaksContainerSectionManager containerMgr;
                private int targetedSlot;
                private int i;
                private String expectedItemId;
                private boolean refillBeforeBreak;

                public Runnable init(Minecraft mc, int i, int currentItem, boolean refillBeforeBreak) throws Exception {
                    this.containerMgr = new InvTweaksContainerSectionManager(mc, ContainerSection.INVENTORY);
                    this.targetedSlot = currentItem;
                    if(i != -1) {
                        this.i = i;
                        this.expectedItemId = Item.itemRegistry.getNameForObject(containerMgr.getItemStack(i).getItem());
                    } else {
                        this.i = containerMgr.getFirstEmptyIndex();
                        this.expectedItemId = null;
                    }
                    this.refillBeforeBreak = refillBeforeBreak;
                    return this;
                }

                public void run() {

                    // Wait for the server to confirm that the
                    // slot is now empty
                    int pollingTime = 0;
                    setHasInventoryChanged(false);
                    while(getThePlayer() != null && !hasInventoryChanged() && pollingTime < InvTweaksConst.POLLING_TIMEOUT) {
                        trySleep(InvTweaksConst.POLLING_DELAY);
                    }
                    if(getThePlayer() == null) {
                        return; // Game closed
                    }
                    if(pollingTime < InvTweaksConst.AUTO_REFILL_DELAY) {
                        trySleep(InvTweaksConst.AUTO_REFILL_DELAY - pollingTime);
                    }
                    if(pollingTime >= InvTweaksConst.POLLING_TIMEOUT) {
                        log.warn("Autoreplace timout");
                    }

                    // In POLLING_DELAY ms, things might have changed
                    try {
                        ItemStack stack = containerMgr.getItemStack(i);

                        if(stack != null && StringUtils.equals(Item.itemRegistry.getNameForObject(stack.getItem()),
                                                        expectedItemId) || this.refillBeforeBreak) {
                            if(containerMgr.move(targetedSlot, i) || containerMgr.move(i, targetedSlot)) {
                                if(!config.getProperty(InvTweaksConfig.PROP_ENABLE_SOUNDS)
                                          .equals(InvTweaksConfig.VALUE_FALSE)) {
                                    mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(
                                            new ResourceLocation("mob.chicken.plop"), 1.0F));
                                }
                                // If item are swapped (like for mushroom soups),
                                // put the item back in the inventory if it is in the hotbar
                                if(containerMgr.getItemStack(i) != null && i >= 27) {
                                    for(int j = 0; j < InvTweaksConst.INVENTORY_SIZE; j++) {
                                        if(containerMgr.getItemStack(j) == null) {
                                            containerMgr.move(i, j);
                                            break;
                                        }
                                    }
                                }

                                // Make sure the inventory resyncs
                                InvTweaksMod.proxy.sortComplete();
                            } else {
                                log.warn("Failed to move stack for autoreplace, despite of prior tests.");
                            }
                        }
                    } catch(NullPointerException e) {
                        // Nothing: Due to multithreading +
                        // unsafe accesses, NPE may (very rarely) occur (?).
                    }
                }

            }.init(mc, replacementStackSlot, slot, refillBeforeBreak)).start();

        }
    }

    private static void trySleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch(InterruptedException e) {
            // Do nothing
        }
    }

}
