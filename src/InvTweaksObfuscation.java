import java.util.List;

import net.minecraft.client.Minecraft;

/**
 * Minecraft 1.0 Obfuscation layer
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
		if (mc.w != null) {
			mc.w.a(message);
		}
	}
	protected boolean isMultiplayerWorld() {
		return mc.l();
	}
    protected vm /* extends yr */ getThePlayer() { // EntityPlayer
        return mc.h;
    }
    protected wz getTheWorld() { // World
        return mc.f;
    }
	protected kf getPlayerController() { // PlayerController
		return mc.c;
	}
	protected vl getCurrentScreen() { // GuiScreen
		return mc.s;
	}
	protected ni getFontRenderer() { // FontRenderer
        return mc.q;
    }
    protected void displayGuiScreen(vl parentScreen) {
        mc.a(parentScreen);
    }
    protected int getDisplayWidth() {
        return mc.d;
    }
    protected int getDisplayHeight() {
        return mc.e;
    }
    protected hr getGameSettings() {
        return mc.A;
    }
    protected int getKeyBindingForwardKeyCode() {
        return getKeyCode(getGameSettings().n);
    }
    protected int getKeyBindingBackKeyCode() {
        return getKeyCode(getGameSettings().p);
    }

	// EntityPlayer members
	
	protected aaf getInventoryPlayer() { // InventoryPlayer
		return getThePlayer().ap;
	}
	protected aai getCurrentEquippedItem() { // ItemStack
		return getThePlayer().av();
	}
	protected db getCraftingInventory() { // Container
		return getThePlayer().ar;
	}
    protected x getPlayerContainer() { // ContainerPlayer
        return (x) getThePlayer().aq; // MCP name: inventorySlots
    }

	// InventoryPlayer members
	
	protected aai[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	protected void setMainInventory(aai[] value) {
		getInventoryPlayer().a = value;
	}
	protected void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	protected void setHoldStack(aai stack) {
		getInventoryPlayer().b(stack); // setItemStack
	}
	protected boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	protected aai getHoldStack() {
		return getInventoryPlayer().k(); // getItemStack
	}
	protected aai getFocusedStack() {
		return getInventoryPlayer().b(); // getCurrentItem
	}
	protected int getFocusedSlot() {
		return getInventoryPlayer().c; // currentItem
	}
	
    // GuiScreen members
	
	protected int getWidth(vl guiScreen) {
	    return guiScreen.q;
	}
    protected int getHeight(vl guiScreen) {
        return guiScreen.r;
    }
    protected int getXSize(fy guiContainer) { // GuiContainer
        return guiContainer.b;
    }
    protected int getYSize(fy guiContainer) {
        return guiContainer.c;
    }
    @SuppressWarnings("unchecked")
    protected List<Object> getControlList(vl guiScreen) {
        return guiScreen.s;
    }
    protected void setControlList(vl guiScreen, List<?> controlList) {
        guiScreen.s = controlList;
    }

    // FontRenderer members
	
	protected int getStringWidth(ni fontRenderer, String line) {
	    return fontRenderer.a(line);
	}
	protected void drawStringWithShadow(ni fontRenderer,
            String s, int i, int j, int k) {
        fontRenderer.a(s, i, j, k);
    }
	
	// ItemStack members

	protected aai createItemStack(int id, int size, int damage) {
		return new aai(id, size, damage);
	}
	protected aai copy(aai itemStack) {
		return itemStack.k();
	}
	protected int getItemDamage(aai itemStack) {
		return itemStack.i();
	}
	protected int getMaxStackSize(aai itemStack) {
		return itemStack.c();
	}
	protected int getStackSize(aai itemStack) {
		return itemStack.a;
	}
	protected void setStackSize(aai itemStack, int value) {
		itemStack.a = value;
	}
	protected int getItemID(aai itemStack) {
		return itemStack.c;
	}
	protected boolean areItemStacksEqual(aai itemStack1, aai itemStack2) {
		return itemStack1.c(itemStack2); // dk.a(itemStack1, itemStack2);
	}
    protected boolean isItemStackDamageable(aai itemStack) {
        return itemStack.e();
    }
    protected boolean areSameItemType(aai itemStack1, aai itemStack2) {
        return areItemsEqual(itemStack1, itemStack2) ||
                (isItemStackDamageable(itemStack1)
                        && getItemID(itemStack1) == getItemID(itemStack2));
    }
    protected boolean areItemsEqual(aai itemStack1, aai itemStack2) {
        return itemStack1.a(itemStack2); // isItemEqual
    }
    protected int getAnimationsToGo(aai itemStack) {
        return itemStack.b;
    }
    protected ym getItem(aai itemStack) { // Item
        return itemStack.a();
    }
    
    // Item & ItemArmor
    
    protected boolean isDamageable(ym item) {
        return item.i();
    }
    protected int getArmorLevel(qh itemArmor) { // ItemArmor
        return itemArmor.a;
    }
	
	// PlayerController members

	protected aai clickInventory(kf playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, yr entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				shiftHold, entityPlayer);
	}
	
	// Container members
	
	protected int getWindowId(db container) {
		return container.f;
	}
	protected List<?> getSlots(db container) {
		return container.e;
	}
    protected yp getSlot(db container, int i) { // Slot
        return (yp) (getSlots(container).get(i));
    }

    protected aai getSlotStack(db container, int i) {
    	yp slot = getSlot(container, i);
        return (slot == null) ? null : getStack(slot); // getStack
    }

    protected void setSlotStack(db container, int i, aai stack) {
        container.a(i, stack); // putStackInSlot
    }

    // Slot members
    
    protected boolean hasStack(yp slot) { 
        return slot.c();
    }
    protected int getSlotNumber(yp slot) {
        return slot.c;
    }
    protected aai getStack(yp slot) {
        return slot.b();
    }
    protected int getXDisplayPosition(yp slot) {
        return slot.d;
    }
    protected int getYDisplayPosition(yp slot) {
        return slot.e;
    }
    protected boolean isItemValid(yp slot, aai itemStack) {
        return slot.a(itemStack);
    }

    // GuiContainer members

    protected db getContainer(fy guiContainer) {
        return guiContainer.d; /* inventorySlots */
    }

    // GuiButton
    
    protected void setEnabled(abk guiButton, boolean enabled) { // GuiButton
        guiButton.h = enabled;
    }
    protected int getId(abk guiButton) { // GuiButton
        return guiButton.f;
    }
    protected void setDisplayString(abk guiButton, String string) {
        guiButton.e = string;
    }
    protected String getDisplayString(abk guiButton) {
        return guiButton.e;
    }
    
    // Other

    protected void playSoundAtEntity(wz theWorld, yr thePlayer, String string, float f, float g) {
        theWorld.a(thePlayer, string, f, g);
    }
    protected int getKeyCode(afp keyBinding) { // KeyBinding
        return keyBinding.d;
    }
    public static adi getLocalizationService() { // = MCP's StringTranslate
        return adi.a(); // StringTranslate.getInstance()
    }
    protected String getLocalizedString(String key) {
    	return getLocalizationService().b(key);
    }
    protected int getSpecialChestRowSize(fy guiContainer, int defaultValue) {
    	return mods.getSpecialChestRowSize(guiContainer, defaultValue);
    }

    // Static access

    public static aai getHoldStackStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getHoldStack();
    }
    public static vl getCurrentScreenStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getCurrentScreen();
    }
    public static String getCurrentLanguage() {
        return getLocalizationService().c();
    }
    
	// Classes
    
    protected boolean isValidChest(vl guiScreen) {
        return guiScreen != null && (isGuiChest(guiScreen)
        		|| isGuiDispenser(guiScreen)
        		|| mods.isSpecialChest(guiScreen));
    }
	protected boolean isValidInventory(vl guiScreen) {
        return isStandardInventory(guiScreen)
        		|| mods.isSpecialInventory(guiScreen);
    }
	protected boolean isStandardInventory(vl guiScreen) {
        return isGuiInventory(guiScreen)
        		|| isGuiWorkbench(guiScreen)
        		|| isGuiFurnace(guiScreen)
                || isGuiBrewingStand(guiScreen)
                || isGuiEnchantmentTable(guiScreen);
    }
	
    protected boolean isGuiContainer(Object o) { // GuiContainer (abstract class)
        return o != null && o instanceof fy;
    }
    protected boolean isGuiContainerCreative(Object o) { // GuiContainerCreative
        return o != null && o.getClass().equals(sn.class);
    }
    protected boolean isGuiChest(Object o) { // GuiChest
        return o != null && o.getClass().equals(zi.class);
    }
    protected boolean isGuiDispenser(Object o) { // GuiDispenser
        return o != null && o.getClass().equals(ez.class);
    }
    protected boolean isGuiInventory(Object o) { // GuiInventory
        return o != null && o.getClass().equals(aih.class);
    }
    protected boolean isGuiEditSign(Object o) { // GuiEditSign
        return o != null && o.getClass().equals(alo.class);
    }
    protected boolean isGuiWorkbench(Object o) { // GuiWorkbench
        return o != null && o.getClass().equals(aei.class);
    }
    protected boolean isGuiBrewingStand(Object o) { // GuiBrewingStand
        return o != null && o.getClass().equals(hd.class);
    }
    protected boolean isGuiEnchantmentTable(Object o) { // GuiEnchantmentTable
        return o != null && o.getClass().equals(si.class);
    }
    protected boolean isGuiButton(Object o) { // GuiButton
        return o != null && o instanceof abk;
    }
    protected boolean isGuiFurnace(Object o) { // GuiFurnace
        return o != null && o instanceof ks;
    }
    
	protected boolean isContainerPlayer(Object o) { // ContainerPlayer
	    return o != null && o.getClass().equals(x.class);
	}
    protected boolean isContainerChest(Object o) { // ContainerChest
        return o != null && o.getClass().equals(bq.class);
    }
    protected boolean isContainerFurnace(Object o) { // ContainerFurnace 
        return o != null && o.getClass().equals(uc.class);
    }
    protected boolean isContainerDispenser(Object o) { // ContainerDispenser
        return o != null && o.getClass().equals(agz.class);
    }
    protected boolean isContainerWorkbench(Object o) { // ContainerWorkbench
        return o != null && o.getClass().equals(aac.class);
    }
    protected boolean isContainerBrewingStand(Object o) { // ContainerBrewingStand
        return o != null && o.getClass().equals(aak.class);
    }
    protected boolean isContainerEnchantmentTable(Object o) { // ContainerEnchantmentTable
        return o != null && o.getClass().equals(gj.class);
    }
    protected boolean isContainerCreative(Object o) { // ContainerCreative
        return o != null && o.getClass().equals(oo.class);
    }

    protected boolean isItemArmor(Object o) { // ItemArmor
        return o != null && o instanceof qh;
    }

    protected boolean isBasicSlot(Object o) { // Slot
        return o != null && o.getClass().equals(yp.class);
    }

}