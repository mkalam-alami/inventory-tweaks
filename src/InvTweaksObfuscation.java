import java.util.List;

import net.minecraft.client.Minecraft;

/**
 * Minecraft 1.3 Obfuscation layer
 * 
 * @author Jimeo Wan
 *
 */
public class InvTweaksObfuscation {

    protected Minecraft mc;

    protected InvTweaksModCompatibility mods;
    
	public InvTweaksObfuscation(Minecraft mc) {
		this.mc = mc;
		this.mods = new InvTweaksModCompatibility(this);
	}
	
	// Minecraft members

	protected void addChatMessage(String message) {
		if (mc.v != null) {
			mc.v.a(message);
		}
	}
	protected boolean isMultiplayerWorld() {
	    return true; // FIXME?
		//return mc.l();
	}
    protected arf getThePlayer() { // EntityPlayer
        return mc.g;
    }
    protected arc getTheWorld() { // World
        return mc.e;
    }
	protected arb getPlayerController() { // PlayerController
		return mc.b;
	}
	protected anm getCurrentScreen() { // GuiScreen
		return mc.r;
	}
	protected amu getFontRenderer() { // FontRenderer
        return mc.p;
    }
    protected void displayGuiScreen(anm guiScreen) {
        mc.a(guiScreen);
    }
    protected int getDisplayWidth() {
        return mc.c;
    }
    protected int getDisplayHeight() {
        return mc.d;
    }
    protected net.minecraft.client.ab getGameSettings() {
        return mc.y;
    }
    public net.minecraft.client.i[] getRegisteredBindings() {
        return getGameSettings().K;
    }
    public void setRegisteredBindings(net.minecraft.client.i[] bindings) {
        getGameSettings().K = bindings;
    }
    protected int getKeyBindingForwardKeyCode() {
        return getKeyCode(getGameSettings().w);
    }
    protected int getKeyBindingBackKeyCode() {
        return getKeyCode(getGameSettings().y);
    }

	// EntityPlayer members

	protected no getInventoryPlayer() { // InventoryPlayer
		return getThePlayer().by;
	}
	protected qs getCurrentEquippedItem() { // ItemStack
		return getThePlayer().bC();
	}
	protected oe getCraftingInventory() { // Container
		return getThePlayer().bA;
	}
    protected or getPlayerContainer() { // ContainerPlayer
        return (or) getThePlayer().bz; // MCP name: inventorySlots
    }

	// InventoryPlayer members
	
	protected qs[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	protected void setMainInventory(qs[] value) {
		getInventoryPlayer().a = value;
	}
	protected void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	protected void setHoldStack(qs stack) {
		getInventoryPlayer().b(stack); // setItemStack
	}
	protected boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	protected qs getHoldStack() {
		return getInventoryPlayer().o(); // getItemStack
	}
	protected qs getFocusedStack() {
		return getInventoryPlayer().g(); // getCurrentItem
	}
	protected int getFocusedSlot() {
		return getInventoryPlayer().c; // currentItem
	}
	
    // GuiScreen members

	protected int getWidth(anm guiScreen) {
	    return guiScreen.f;
	}
    protected int getHeight(anm guiScreen) {
        return guiScreen.g;
    }
    protected int getXSize(aog guiContainer) { // GuiContainer
        return guiContainer.b;
    }
    protected int getYSize(aog guiContainer) {
        return guiContainer.c;
    }
    @SuppressWarnings("unchecked")
	protected List<Object> getControlList(anm guiScreen) {
        return guiScreen.h;
    }
    protected void setControlList(anm guiScreen, List<?> controlList) {
        guiScreen.h = controlList;
    }
    protected aog asGuiContainer(anm guiScreen) {
        return (aog) guiScreen;
    }

    // FontRenderer members
	
	protected int getStringWidth(amu fontRenderer, String line) {
	    return fontRenderer.a(line);
	}
	protected void drawStringWithShadow(amu fontRenderer,
            String s, int i, int j, int k) {
        fontRenderer.a(s, i, j, k);
    }
	
	// ItemStack members

	protected qs createItemStack(int id, int size, int damage) {
		return new qs(id, size, damage);
	}
	protected qs copy(qs itemStack) {
		return itemStack.l();
	}
	protected int getItemDamage(qs itemStack) {
		return itemStack.j();
	}
	protected int getMaxStackSize(qs itemStack) {
		return itemStack.d();
	}
	protected boolean hasDataTags(qs itemStack) {
	  return itemStack.o();
	}
	protected int getStackSize(qs itemStack) {
		return itemStack.a;
	}
	protected int getItemID(qs itemStack) {
		return itemStack.c;
	}
	protected boolean areItemStacksEqual(qs itemStack1, qs itemStack2) {
		return itemStack1.c(itemStack2); // dk.a(itemStack1, itemStack2);
	}
    protected boolean isItemStackDamageable(qs itemStack) {
        return itemStack.f();
    }
    protected boolean areSameItemType(qs itemStack1, qs itemStack2) {
        return areItemsEqual(itemStack1, itemStack2) ||
                (isItemStackDamageable(itemStack1)
                        && getItemID(itemStack1) == getItemID(itemStack2));
    }
    protected boolean areItemsEqual(qs itemStack1, qs itemStack2) {
        return itemStack1.a(itemStack2); // isItemEqual
    }
    protected int getAnimationsToGo(qs itemStack) {
        return itemStack.b;
    }
    protected qq getItem(qs itemStack) { // Item
        return itemStack.b();
    }
    
    // Item & ItemArmor
    
    protected boolean isDamageable(qq item) {
        return item.m();
    }
    protected int getArmorLevel(pc itemArmor) { // ItemArmor
        return itemArmor.b;
    }
    protected pc asItemArmor(qq item) { // ItemArmor
        return (pc) item;
    }
	
	// PlayerController members

	protected qs clickInventory(arb playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, arf entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				shiftHold, entityPlayer);
	}
	
	// Container members

	protected int getWindowId(oe container) {
		return container.c;
	}
	protected List<?> getSlots(oe container) {
		return container.b;
	}
    protected pa getSlot(oe container, int i) { // Slot
        return (pa) (getSlots(container).get(i));
    }

    protected qs getSlotStack(oe container, int i) {
        pa slot = getSlot(container, i);
        return (slot == null) ? null : getStack(slot); // getStack
    }

    protected void setSlotStack(oe container, int i, qs stack) {
        container.a(i, stack); // putStackInSlot
    }

    // Slot members
    
    protected boolean hasStack(pa slot) { 
        return slot.d();
    }
    protected int getSlotNumber(pa slot) {
        return slot.d;
    }
    protected qs getStack(pa slot) {
        return slot.c();
    }
    protected int getXDisplayPosition(pa slot) {
        return slot.e;
    }
    protected int getYDisplayPosition(pa slot) {
        return slot.f;
    }
    protected boolean areSlotAndStackCompatible(pa slot, qs itemStack) {
        return slot.a(itemStack); // isItemValid
    }

    // GuiContainer members

    protected oe getContainer(aog guiContainer) {
        return guiContainer.d; /* inventorySlots */
    }

    // GuiButton

    protected amg asGuiButton(Object o) {
        return (amg) o;
    }
    protected void setEnabled(amg guiButton, boolean enabled) { // GuiButton
        guiButton.g = enabled;
    }
    protected int getId(amg guiButton) { // GuiButton
        return guiButton.f;
    }
    protected void setDisplayString(amg guiButton, String string) {
        guiButton.e = string;
    }
    protected String getDisplayString(amg guiButton) {
        return guiButton.e;
    }
    
    // Other

    protected void playSoundAtEntity(arc theWorld, arf thePlayer, String string, float f, float g) {
        theWorld.a(thePlayer, string, f, g);
    }
    public long getCurrentTime() {
        return getTheWorld().D();
    }

    protected int getKeyCode(net.minecraft.client.i w) { // KeyBinding
        return w.d;
    }
    protected int getSpecialChestRowSize(aog guiContainer, int defaultValue) {
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
    public static qs getHoldStackStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getHoldStack();
    }
    public static anm getCurrentScreenStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getCurrentScreen();
    }
    
	// Classes
    
    protected boolean isValidChest(anm guiScreen) {
        return guiScreen != null && (isGuiChest(guiScreen)
        		|| isGuiDispenser(guiScreen)
        		|| mods.isSpecialChest(guiScreen));
    }
	protected boolean isValidInventory(anm guiScreen) {
        return isStandardInventory(guiScreen)
        		|| mods.isSpecialInventory(guiScreen);
    }
	protected boolean isStandardInventory(anm guiScreen) {
        return isGuiInventory(guiScreen)
        		|| isGuiWorkbench(guiScreen)
        		|| isGuiFurnace(guiScreen)
                || isGuiBrewingStand(guiScreen)
                || isGuiEnchantmentTable(guiScreen)
                || isGuiTrading(guiScreen);
    }
	
    protected boolean isGuiContainer(Object o) { // GuiContainer (abstract class)
        return o != null && o instanceof aog;
    }
    protected boolean isGuiContainerCreative(Object o) { // GuiContainerCreative
        return o != null && o.getClass().equals(aom.class);
    }
    protected boolean isGuiChest(Object o) { // GuiChest
        return o != null && o.getClass().equals(aok.class);
    }
    protected boolean isGuiDispenser(Object o) { // GuiDispenser
        return o != null && o.getClass().equals(aox.class);
    }
    protected boolean isGuiInventory(Object o) { // GuiInventory
        return o != null && o.getClass().equals(aop.class);
    }
    protected boolean isGuiEditSign(Object o) { // GuiEditSign
        return o != null && o.getClass().equals(aow.class);
    }
    protected boolean isGuiWorkbench(Object o) { // GuiWorkbench
        return o != null && o.getClass().equals(aol.class);
    }
    protected boolean isGuiBrewingStand(Object o) { // GuiBrewingStand
        return o != null && o.getClass().equals(aoj.class);
    }
    protected boolean isGuiEnchantmentTable(Object o) { // GuiEnchantmentTable
        return o != null && o.getClass().equals(aor.class);
    }
    protected boolean isGuiTrading(Object o) { // GuiTrading
        return o != null && o.getClass().equals(aou.class);
    }
    protected boolean isGuiFurnace(Object o) { // GuiFurnace
        return o != null && o.getClass().equals(aos.class);
    }
    protected boolean isGuiButton(Object o) { // GuiButton
        return o != null && o instanceof amg;
    }
    
	protected boolean isContainerPlayer(Object o) { // ContainerPlayer
	    return o != null && o.getClass().equals(or.class);
	}
    protected boolean isContainerChest(Object o) { // ContainerChest
        return o != null && o.getClass().equals(oj.class);
    }
    protected boolean isContainerFurnace(Object o) { // ContainerFurnace 
        return o != null && o.getClass().equals(op.class);
    }
    protected boolean isContainerDispenser(Object o) { // ContainerDispenser
        return o != null && o.getClass().equals(pb.class);
    }
    protected boolean isContainerWorkbench(Object o) { // ContainerWorkbench
        return o != null && o.getClass().equals(ol.class);
    }
    protected boolean isContainerBrewingStand(Object o) { // ContainerBrewingStand
        return o != null && o.getClass().equals(of.class);
    }
    protected boolean isContainerEnchantmentTable(Object o) { // ContainerEnchantmentTable
        return o != null && o.getClass().equals(om.class);
    }
    protected boolean isContainerCreative(Object o) { // ContainerCreative
        return o != null && o.getClass().equals(aon.class);
    }

    protected boolean isItemArmor(Object o) { // ItemArmor
        return o != null && o instanceof pc;
    }

    protected boolean isBasicSlot(Object o) { // Slot
        return o != null && o.getClass().equals(pa.class);
    }

}