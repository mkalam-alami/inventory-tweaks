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
    protected vq getThePlayer() { // EntityPlayer
        return mc.h;
    }
    protected xd getTheWorld() { // World
        return mc.f;
    }
	protected ki getPlayerController() { // PlayerController
		return mc.c;
	}
	protected vp getCurrentScreen() { // GuiScreen
		return mc.s;
	}
	protected nl getFontRenderer() { // FontRenderer
        return mc.q;
    }
    protected void displayGuiScreen(vp parentScreen) {
        mc.a(parentScreen);
    }
    protected int getDisplayWidth() {
        return mc.d;
    }
    protected int getDisplayHeight() {
        return mc.e;
    }
    protected hu getGameSettings() {
        return mc.A;
    }
    protected int getKeyBindingForwardKeyCode() {
        return getKeyCode(getGameSettings().n);
    }
    protected int getKeyBindingBackKeyCode() {
        return getKeyCode(getGameSettings().p);
    }

	// EntityPlayer members
	
	protected aak getInventoryPlayer() { // InventoryPlayer
		return getThePlayer().ap;
	}
	protected aan getCurrentEquippedItem() { // ItemStack
		return getThePlayer().av();
	}
	protected dd getCraftingInventory() { // Container
		return getThePlayer().ar;
	}
    protected y getPlayerContainer() { // ContainerPlayer
        return (y) getThePlayer().aq; // MCP name: inventorySlots
    }

	// InventoryPlayer members
	
	protected aan[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	protected void setMainInventory(aan[] value) {
		getInventoryPlayer().a = value;
	}
	protected void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	protected void setHoldStack(aan stack) {
		getInventoryPlayer().b(stack); // setItemStack
	}
	protected boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	protected aan getHoldStack() {
		return getInventoryPlayer().k(); // getItemStack
	}
	protected aan getFocusedStack() {
		return getInventoryPlayer().b(); // getCurrentItem
	}
	protected int getFocusedSlot() {
		return getInventoryPlayer().c; // currentItem
	}
	
    // GuiScreen members
	
	protected int getWidth(vp guiScreen) {
	    return guiScreen.q;
	}
    protected int getHeight(vp guiScreen) {
        return guiScreen.r;
    }
    protected int getXSize(gb guiContainer) { // GuiContainer
        return guiContainer.b;
    }
    protected int getYSize(gb guiContainer) {
        return guiContainer.c;
    }
    @SuppressWarnings("unchecked")
	protected List<Object> getControlList(vp guiScreen) {
        return guiScreen.s;
    }
    protected void setControlList(vp guiScreen, List<?> controlList) {
        guiScreen.s = controlList;
    }

    // FontRenderer members
	
	protected int getStringWidth(nl fontRenderer, String line) {
	    return fontRenderer.a(line);
	}
	protected void drawStringWithShadow(nl fontRenderer,
            String s, int i, int j, int k) {
        fontRenderer.a(s, i, j, k);
    }
	
	// ItemStack members

	protected aan createItemStack(int id, int size, int damage) {
		return new aan(id, size, damage);
	}
	protected aan copy(aan itemStack) {
		return itemStack.k();
	}
	protected int getItemDamage(aan itemStack) {
		return itemStack.i();
	}
	protected int getMaxStackSize(aan itemStack) {
		return itemStack.c();
	}
	protected int getStackSize(aan itemStack) {
		return itemStack.a;
	}
	protected void setStackSize(aan itemStack, int value) {
		itemStack.a = value;
	}
	protected int getItemID(aan itemStack) {
		return itemStack.c;
	}
	protected boolean areItemStacksEqual(aan itemStack1, aan itemStack2) {
		return itemStack1.c(itemStack2); // dk.a(itemStack1, itemStack2);
	}
    protected boolean isItemStackDamageable(aan itemStack) {
        return itemStack.e();
    }
    protected boolean areSameItemType(aan itemStack1, aan itemStack2) {
        return areItemsEqual(itemStack1, itemStack2) ||
                (isItemStackDamageable(itemStack1)
                        && getItemID(itemStack1) == getItemID(itemStack2));
    }
    protected boolean areItemsEqual(aan itemStack1, aan itemStack2) {
        return itemStack1.a(itemStack2); // isItemEqual
    }
    protected int getAnimationsToGo(aan itemStack) {
        return itemStack.b;
    }
    protected yr getItem(aan itemStack) { // Item
        return itemStack.a();
    }
    
    // Item & ItemArmor
    
    protected boolean isDamageable(yr item) {
        return item.i();
    }
    protected int getArmorLevel(ql itemArmor) { // ItemArmor
        return itemArmor.a;
    }
	
	// PlayerController members

	protected aan clickInventory(ki playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, yw entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				shiftHold, entityPlayer);
	}
	
	// Container members
	
	protected int getWindowId(dd container) {
		return container.f;
	}
	protected List<?> getSlots(dd container) {
		return container.e;
	}
    protected yu getSlot(dd container, int i) { // Slot
        return (yu) (getSlots(container).get(i));
    }

    protected aan getSlotStack(dd container, int i) {
    	yu slot = getSlot(container, i);
        return (slot == null) ? null : getStack(slot); // getStack
    }

    protected void setSlotStack(dd container, int i, aan stack) {
        container.a(i, stack); // putStackInSlot
    }

    // Slot members
    
    protected boolean hasStack(yu slot) { 
        return slot.c();
    }
    protected int getSlotNumber(yu slot) {
        return slot.c;
    }
    protected aan getStack(yu slot) {
        return slot.b();
    }
    protected int getXDisplayPosition(yu slot) {
        return slot.d;
    }
    protected int getYDisplayPosition(yu slot) {
        return slot.e;
    }
    protected boolean isItemValid(yu slot, aan itemStack) {
        return slot.a(itemStack);
    }

    // GuiContainer members

    protected dd getContainer(gb guiContainer) {
        return guiContainer.d; /* inventorySlots */
    }

    // GuiButton
    
    protected void setEnabled(abp guiButton, boolean enabled) { // GuiButton
        guiButton.h = enabled;
    }
    protected int getId(abp guiButton) { // GuiButton
        return guiButton.f;
    }
    protected void setDisplayString(abp guiButton, String string) {
        guiButton.e = string;
    }
    protected String getDisplayString(abp guiButton) {
        return guiButton.e;
    }
    
    // Other

    protected void playSoundAtEntity(xd theWorld, yw thePlayer, String string, float f, float g) {
        theWorld.a(thePlayer, string, f, g);
    }
    protected int getKeyCode(afu keyBinding) { // KeyBinding
        return keyBinding.d;
    }
    public static adn getLocalizationService() { // = MCP's StringTranslate
        return adn.a(); // StringTranslate.getInstance()
    }
    protected String getLocalizedString(String key) {
    	return getLocalizationService().b(key);
    }
    protected int getSpecialChestRowSize(gb guiContainer, int defaultValue) {
    	return mods.getSpecialChestRowSize(guiContainer, defaultValue);
    }

    // Static access

    public static aan getHoldStackStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getHoldStack();
    }
    public static vp getCurrentScreenStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getCurrentScreen();
    }
    public static String getCurrentLanguage() {
        return getLocalizationService().c();
    }
    
	// Classes
    
    protected boolean isValidChest(vp guiScreen) {
        return guiScreen != null && (isGuiChest(guiScreen)
        		|| isGuiDispenser(guiScreen)
        		|| mods.isSpecialChest(guiScreen));
    }
	protected boolean isValidInventory(vp guiScreen) {
        return isStandardInventory(guiScreen)
        		|| mods.isSpecialInventory(guiScreen);
    }
	protected boolean isStandardInventory(vp guiScreen) {
        return isGuiInventory(guiScreen)
        		|| isGuiWorkbench(guiScreen)
        		|| isGuiFurnace(guiScreen)
                || isGuiBrewingStand(guiScreen)
                || isGuiEnchantmentTable(guiScreen);
    }
	
    protected boolean isGuiContainer(Object o) { // GuiContainer (abstract class)
        return o != null && o instanceof gb;
    }
    protected boolean isGuiContainerCreative(Object o) { // GuiContainerCreative
        return o != null && o.getClass().equals(sr.class);
    }
    protected boolean isGuiChest(Object o) { // GuiChest
        return o != null && o.getClass().equals(zn.class);
    }
    protected boolean isGuiDispenser(Object o) { // GuiDispenser
        return o != null && o.getClass().equals(fb.class);
    }
    protected boolean isGuiInventory(Object o) { // GuiInventory
        return o != null && o.getClass().equals(ain.class);
    }
    protected boolean isGuiEditSign(Object o) { // GuiEditSign
        return o != null && o.getClass().equals(alu.class); 
    }
    protected boolean isGuiWorkbench(Object o) { // GuiWorkbench
        return o != null && o.getClass().equals(aen.class);
    }
    protected boolean isGuiBrewingStand(Object o) { // GuiBrewingStand
        return o != null && o.getClass().equals(hg.class);
    }
    protected boolean isGuiEnchantmentTable(Object o) { // GuiEnchantmentTable
        return o != null && o.getClass().equals(sm.class);
    }
    protected boolean isGuiButton(Object o) { // GuiButton
        return o != null && o instanceof abp;
    }
    protected boolean isGuiFurnace(Object o) { // GuiFurnace
        return o != null && o instanceof kv;
    }
    
	protected boolean isContainerPlayer(Object o) { // ContainerPlayer
	    return o != null && o.getClass().equals(y.class);
	}
    protected boolean isContainerChest(Object o) { // ContainerChest
        return o != null && o.getClass().equals(bs.class);
    }
    protected boolean isContainerFurnace(Object o) { // ContainerFurnace 
        return o != null && o.getClass().equals(ug.class);
    }
    protected boolean isContainerDispenser(Object o) { // ContainerDispenser
        return o != null && o.getClass().equals(ahf.class);
    }
    protected boolean isContainerWorkbench(Object o) { // ContainerWorkbench
        return o != null && o.getClass().equals(aah.class);
    }
    protected boolean isContainerBrewingStand(Object o) { // ContainerBrewingStand
        return o != null && o.getClass().equals(aap.class);
    }
    protected boolean isContainerEnchantmentTable(Object o) { // ContainerEnchantmentTable
        return o != null && o.getClass().equals(gm.class);
    }
    protected boolean isContainerCreative(Object o) { // ContainerCreative
        return o != null && o.getClass().equals(os.class);
    }

    protected boolean isItemArmor(Object o) { // ItemArmor
        return o != null && o instanceof ql;
    }

    protected boolean isBasicSlot(Object o) { // Slot
        return o != null && o.getClass().equals(yu.class);
    }

}