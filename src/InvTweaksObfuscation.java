import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

/**
 * Minecraft 1.3 Obfuscation layer
 * 
 * @author Jimeo Wan
 *
 */
public class InvTweaksObfuscation {

    private static final Logger log = Logger.getLogger("InvTweaks");
    
    protected Minecraft mc;

    protected InvTweaksModCompatibility mods;

    private static Field creativeSlotField = null;
    
	public InvTweaksObfuscation(Minecraft mc) {
		this.mc = mc;
		this.mods = new InvTweaksModCompatibility(this);

		if (creativeSlotField == null) {
            try {
                creativeSlotField = aqo.class.getDeclaredField("b");
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(creativeSlotField, Modifier.PUBLIC);
            }
            catch (Exception e) {
                creativeSlotField = null;
                log.severe("Failed to make creative slot accessible: " +  e.getMessage());
            }
		}
	}
	
	// Minecraft members

	protected void addChatMessage(String message) {
	    if (mc.v != null) {
	        mc.v.b().a(message);
	    }
	}
    protected atf getThePlayer() { // EntityPlayer
        return mc.g;
    }
    protected atc getTheWorld() { // World
        return mc.e;
    }
	protected atb getPlayerController() { // PlayerController
		return mc.b;
	}
	protected apm getCurrentScreen() { // GuiScreen
		return mc.r;
	}
	protected aou getFontRenderer() { // FontRenderer
        return mc.p;
    }
    protected void displayGuiScreen(apm guiScreen) {
        mc.a(guiScreen);
    }
    protected int getDisplayWidth() {
        return mc.c;
    }
    protected int getDisplayHeight() {
        return mc.d;
    }
    protected any getGameSettings() {
        return mc.y;
    }
    public ane[] getRegisteredBindings() {
        return getGameSettings().K;
    }
    public void setRegisteredBindings(ane[] bindings) {
        getGameSettings().K = bindings;
    }
    protected int getKeyBindingForwardKeyCode() {
        return getKeyCode(getGameSettings().w);
    }
    protected int getKeyBindingBackKeyCode() {
        return getKeyCode(getGameSettings().y);
    }

	// EntityPlayer members

	protected oe getInventoryPlayer() { // InventoryPlayer
		return getThePlayer().by;
	}
	protected ri getCurrentEquippedItem() { // ItemStack
		return getThePlayer().bC();
	}
	protected ou getCraftingInventory() { // Container
		return getThePlayer().bA;
	}
    protected ou getPlayerContainer() { // ContainerPlayer
        return (ou) getThePlayer().bz; // MCP name: inventorySlots
    }

	// InventoryPlayer members
	
	protected ri[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	protected void setMainInventory(ri[] value) {
		getInventoryPlayer().a = value;
	}
	protected void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	protected void setHeldStack(ri stack) {
		getInventoryPlayer().b(stack); // setItemStack
	}
	protected boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	protected ri getHeldStack() {
		return getInventoryPlayer().o(); // getItemStack
	}
	protected ri getFocusedStack() {
		return getInventoryPlayer().g(); // getCurrentItem
	}
	protected int getFocusedSlot() {
		return getInventoryPlayer().c; // currentItem
	}
	
    // GuiScreen members

	protected int getWindowWidth(apm guiScreen) {
	    return guiScreen.f;
	}
    protected int getWindowHeight(apm guiScreen) {
        return guiScreen.g;
    }
    protected int getGuiX(aqg guiContainer) { // GuiContainer
        return guiContainer.m;
    }
    protected int getGuiY(aqg guiContainer) {
        return guiContainer.n;
    }
    protected int getGuiWidth(aqg guiContainer) { // GuiContainer
        return guiContainer.b;
    }
    protected int getGuiHeight(aqg guiContainer) {
        return guiContainer.c;
    }
    @SuppressWarnings("unchecked")
	protected List<Object> getControlList(apm guiScreen) {
        return guiScreen.h;
    }
    protected void setControlList(apm guiScreen, List<?> controlList) {
        guiScreen.h = controlList;
    }
    protected aqg asGuiContainer(apm guiScreen) {
        return (aqg) guiScreen;
    }

    // FontRenderer members
	
	protected int getStringWidth(aou fontRenderer, String line) {
	    return fontRenderer.a(line);
	}
	protected void drawStringWithShadow(aou fontRenderer,
            String s, int i, int j, int k) {
        fontRenderer.a(s, i, j, k);
    }
	
	// ItemStack members

	protected ri createItemStack(int id, int size, int damage) {
		return new ri(id, size, damage);
	}
	protected ri copy(ri itemStack) {
		return itemStack.l();
	}
	protected int getItemDamage(ri itemStack) {
		return itemStack.j();
	}
	protected int getMaxStackSize(ri itemStack) {
		return itemStack.d();
	}
	protected boolean hasDataTags(ri itemStack) {
	  return itemStack.o();
	}
	protected int getStackSize(ri itemStack) {
		return itemStack.a;
	}
	protected int getItemID(ri itemStack) {
		return itemStack.c;
	}
	protected boolean areItemStacksEqual(ri itemStack1, ri itemStack2) {
		return itemStack1.c(itemStack2);
	}
    protected boolean isItemStackDamageable(ri itemStack) {
        return itemStack.f();
    }
    protected boolean areSameItemType(ri itemStack1, ri itemStack2) {
        return areItemsEqual(itemStack1, itemStack2) ||
                (isItemStackDamageable(itemStack1)
                        && getItemID(itemStack1) == getItemID(itemStack2));
    }
    protected boolean areItemsEqual(ri itemStack1, ri itemStack2) {
        return itemStack1.a(itemStack2); // isItemEqual
    }
    protected int getAnimationsToGo(ri itemStack) {
        return itemStack.b;
    }
    protected rg getItem(ri itemStack) { // Item
        return itemStack.b();
    }
    
    // Item & ItemArmor
    
    protected boolean isDamageable(rg item) {
        return item.m();
    }
    protected int getArmorLevel(ps itemArmor) { // ItemArmor
        return itemArmor.b;
    }
    protected ps asItemArmor(rg item) { // ItemArmor
        return (ps) item;
    }
	
	// PlayerController members

	protected ri clickInventory(atb playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, atf entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				shiftHold, entityPlayer);
	}
	
	// Container members

	protected int getWindowId(ou container) {
		return container.c;
	}
	protected List<?> getSlots(ou container) {
		return container.b;
	}
    protected pq getSlot(ou container, int i) { // Slot
        return (pq) (getSlots(container).get(i));
    }

    protected ri getSlotStack(ou container, int i) {
        pq slot = getSlot(container, i);
        return (slot == null) ? null : getStack(slot); // getStack
    }

    protected void setSlotStack(ou container, int i, ri stack) {
        container.a(i, stack); // putStackInSlot
    }

    // Slot members
    
    protected boolean hasStack(pq slot) { 
        return slot.d();
    }
    protected int getSlotNumber(pq slot) {
        try {
            // Creative slots don't set the "d" property, serve as a proxy for true slots
            if (slot instanceof aqo) {
                pq underlyingSlot = (pq) creativeSlotField.get(slot);
                if (underlyingSlot != null) {
                    return underlyingSlot.d;
                }
            }
        } catch (Exception e) {
            log.warning("Failed to access creative slot number");
        }
        return slot.d;
    }
    protected ri getStack(pq slot) {
        return slot.c();
    }
    protected int getXDisplayPosition(pq slot) {
        return slot.e;
    }
    protected int getYDisplayPosition(pq slot) {
        return slot.f;
    }
    protected boolean areSlotAndStackCompatible(pq slot, ri itemStack) {
        return slot.a(itemStack); // isItemValid
    }

    // GuiContainer members

    protected ou getContainer(aqg guiContainer) {
        return guiContainer.d; /* inventorySlots */
    }

    // GuiButton

    protected aog asGuiButton(Object o) {
        return (aog) o;
    }
    protected void setEnabled(aog guiButton, boolean enabled) { // GuiButton
        guiButton.g = enabled;
    }
    protected int getId(aog guiButton) { // GuiButton
        return guiButton.f;
    }
    protected void setDisplayString(aog guiButton, String string) {
        guiButton.e = string;
    }
    protected String getDisplayString(aog guiButton) {
        return guiButton.e;
    }
    
    // Other

    protected void playSound(String string, float f, float g) {
        mc.A.a(string, f, g);
    }
    public long getCurrentTime() {
        return getTheWorld().D();
    }

    protected int getKeyCode(ane w) { // KeyBinding
        return w.d;
    }
    protected int getSpecialChestRowSize(aqg guiContainer, int defaultValue) {
    	return mods.getSpecialChestRowSize(guiContainer, defaultValue);
    }

    // Static access

    public static ak getLocalizationService() { // StringTranslate
        return ak.a(); // StringTranslate.getInstance()
    }
    public static String getCurrentLanguage() {
        return getLocalizationService().c();
    }
    public static String getLocalizedString(String key) {
        return getLocalizationService().b(key);
    }
    public static ri getHoldStackStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getHeldStack();
    }
    public static apm getCurrentScreenStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getCurrentScreen();
    }
    
	// Classes
    
    protected boolean isValidChest(apm guiScreen) {
        return guiScreen != null && (isGuiChest(guiScreen)
        		|| isGuiDispenser(guiScreen)
        		|| mods.isSpecialChest(guiScreen));
    }
	protected boolean isValidInventory(apm guiScreen) {
        return isStandardInventory(guiScreen)
        		|| mods.isSpecialInventory(guiScreen);
    }
	protected boolean isStandardInventory(apm guiScreen) {
        return isGuiInventory(guiScreen)
        		|| isGuiWorkbench(guiScreen)
        		|| isGuiFurnace(guiScreen)
                || isGuiBrewingStand(guiScreen)
                || isGuiEnchantmentTable(guiScreen)
                || isGuiTrading(guiScreen)
                || (isGuiInventoryCreative(guiScreen) && getSlots(getContainer((aqg) guiScreen)).size() == 46);
    }
	
    protected boolean isGuiContainer(Object o) { // GuiContainer (abstract class)
        return o != null && o instanceof aqg;
    }
    protected boolean isGuiContainerCreative(Object o) { // GuiContainerCreative
        return o != null && o.getClass().equals(aqm.class);
    }
    protected boolean isGuiChest(Object o) { // GuiChest
        return o != null && o.getClass().equals(aqk.class);
    }
    protected boolean isGuiDispenser(Object o) { // GuiDispenser
        return o != null && o.getClass().equals(aqx.class);
    }
    protected boolean isGuiInventory(Object o) { // GuiInventory
        return o != null && o.getClass().equals(aqt.class);
    }
    protected boolean isGuiInventoryCreative(Object o) { // GuiInventoryCreative
        return o != null && o.getClass().equals(aqm.class);
    }
    protected boolean isGuiEditSign(Object o) { // GuiEditSign
        return o != null && o.getClass().equals(aqw.class);
    }
    protected boolean isGuiWorkbench(Object o) { // GuiWorkbench
        return o != null && o.getClass().equals(aql.class);
    }
    protected boolean isGuiBrewingStand(Object o) { // GuiBrewingStand
        return o != null && o.getClass().equals(aqj.class);
    }
    protected boolean isGuiEnchantmentTable(Object o) { // GuiEnchantmentTable
        return o != null && o.getClass().equals(aqr.class);
    }
    protected boolean isGuiTrading(Object o) { // GuiTrading
        return o != null && o.getClass().equals(aqu.class);
    }
    protected boolean isGuiFurnace(Object o) { // GuiFurnace
        return o != null && o.getClass().equals(aqs.class);
    }
    protected boolean isGuiButton(Object o) { // GuiButton
        return o != null && o instanceof aog;
    }
    
    protected boolean isContainerBrewingStand(Object o) { // ContainerBrewingStand
        return o != null && o.getClass().equals(ov.class);
    }
    protected boolean isContainerChest(Object o) { // ContainerChest
        return o != null && o.getClass().equals(oz.class);
    }
    protected boolean isContainerWorkbench(Object o) { // ContainerWorkbench
        return o != null && o.getClass().equals(pb.class);
    }
    protected boolean isContainerEnchantmentTable(Object o) { // ContainerEnchantmentTable
        return o != null && o.getClass().equals(pc.class);
    }
    protected boolean isContainerFurnace(Object o) { // ContainerFurnace 
        return o != null && o.getClass().equals(pf.class);
    }
	protected boolean isContainerPlayer(Object o) { // ContainerPlayer
	    return o != null && o.getClass().equals(ph.class);
	}
	// TODO pl?
    protected boolean isContainerDispenser(Object o) { // ContainerDispenser
        return o != null && o.getClass().equals(pr.class);
    }
    protected boolean isContainerCreative(Object o) { // ContainerCreative
        return o != null && o.getClass().equals(aqn.class);
    }

    protected boolean isItemArmor(Object o) { // ItemArmor
        return o != null && o instanceof ps;
    }

    protected boolean isBasicSlot(Object o) { // Slot
        return o != null && o.getClass().equals(pq.class);
    }

}