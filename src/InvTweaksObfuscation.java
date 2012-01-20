import java.io.File;
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
    protected uh /* extends xb */ getThePlayer() { // EntityPlayer
        return mc.h;
    }
    protected vq getTheWorld() { // World
        return mc.f;
    }
	protected js getPlayerController() { // PlayerController
		return mc.c;
	}
	protected ug getCurrentScreen() { // GuiScreen
		return mc.s;
	}
	protected mq getFontRenderer() { // FontRenderer
        return mc.q;
    }
    protected void displayGuiScreen(ug parentScreen) {
        mc.a(parentScreen);
    }
    protected int getDisplayWidth() {
        return mc.d;
    }
    protected int getDisplayHeight() {
        return mc.e;
    }
    protected hh getGameSettings() {
        return mc.A;
    }
    protected int getKeyBindingForwardKeyCode() {
        return getKeyCode(getGameSettings().n);
    }
    protected int getKeyBindingBackKeyCode() {
        return getKeyCode(getGameSettings().p);
    }

	// EntityPlayer members
	
	protected yn getInventoryPlayer() { // InventoryPlayer
		return getThePlayer().ap;
	}
	protected yq getCurrentEquippedItem() { // ItemStack
		return getThePlayer().au();
	}
	protected cx getCraftingInventory() { // Container
		return getThePlayer().ar;
	}
    protected w getPlayerContainer() { // ContainerPlayer
        return (w) getThePlayer().aq; // MCP name: inventorySlots
    }

	// InventoryPlayer members
	
	protected yq[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	protected void setMainInventory(yq[] value) {
		getInventoryPlayer().a = value;
	}
	protected void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	protected void setHoldStack(yq stack) {
		getInventoryPlayer().b(stack); // setItemStack
	}
	protected boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	protected yq getHoldStack() {
		return getInventoryPlayer().j(); // getItemStack
	}
	protected yq getFocusedStack() {
		return getInventoryPlayer().b(); // getCurrentItem
	}
	protected int getFocusedSlot() {
		return getInventoryPlayer().c; // currentItem
	}
	
    // GuiScreen members
	
	protected int getWidth(ug guiScreen) {
	    return guiScreen.q;
	}
    protected int getHeight(ug guiScreen) {
        return guiScreen.r;
    }
    protected int getXSize(ft guiContainer) { // GuiContainer
        return guiContainer.b;
    }
    protected int getYSize(ft guiContainer) {
        return guiContainer.c;
    }
    @SuppressWarnings("unchecked")
    protected List<Object> getControlList(ug guiScreen) {
        return guiScreen.s;
    }
    protected void setControlList(ug guiScreen, List<?> controlList) {
        guiScreen.s = controlList;
    }

    // FontRenderer members
	
	protected int getStringWidth(mq fontRenderer, String line) {
	    return fontRenderer.a(line);
	}
	protected void drawStringWithShadow(mq fontRenderer,
            String s, int i, int j, int k) {
        fontRenderer.a(s, i, j, k);
    }
	
	// ItemStack members

	protected yq createItemStack(int id, int size, int damage) {
		return new yq(id, size, damage);
	}
	protected yq copy(yq itemStack) {
		return itemStack.k();
	}
	protected int getItemDamage(yq itemStack) {
		return itemStack.i();
	}
	protected int getMaxStackSize(yq itemStack) {
		return itemStack.c();
	}
	protected int getStackSize(yq itemStack) {
		return itemStack.a;
	}
	protected void setStackSize(yq itemStack, int value) {
		itemStack.a = value;
	}
	protected int getItemID(yq itemStack) {
		return itemStack.c;
	}
	protected boolean areItemStacksEqual(yq itemStack1, yq itemStack2) {
		return itemStack1.c(itemStack2); // dk.a(itemStack1, itemStack2);
	}
    protected boolean isItemStackDamageable(yq itemStack) {
        return itemStack.e();
    }
    protected boolean areSameItemType(yq itemStack1, yq itemStack2) {
        return areItemsEqual(itemStack1, itemStack2) ||
                (isItemStackDamageable(itemStack1)
                        && getItemID(itemStack1) == getItemID(itemStack2));
    }
    protected boolean areItemsEqual(yq itemStack1, yq itemStack2) {
        return itemStack1.a(itemStack2); // isItemEqual
    }
    protected int getAnimationsToGo(yq itemStack) {
        return itemStack.b;
    }
    protected ww getItem(yq itemStack) { // Item
        return itemStack.a();
    }
    
    // Item & ItemArmor
    
    protected boolean isDamageable(ww item) {
        return item.i();
    }
    protected int getArmorLevel(po itemArmor) { // ItemArmor
        return itemArmor.a;
    }
	
	// PlayerController members

	protected yq clickInventory(js playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, xb entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				shiftHold, entityPlayer);
	}
	
	// Container members
	
	protected int getWindowId(cx container) {
		return container.f;
	}
	protected List<?> getSlots(cx container) {
		return container.e;
	}
    protected wz getSlot(cx container, int i) { // Slot
        return (wz) getSlots(container).get(i);
    }

    protected yq getSlotStack(cx container, int i) {
        wz slot = getSlot(container, i);
        return (slot == null) ? null : getStack(slot); // getStack
    }

    protected void setSlotStack(cx container, int i, yq stack) {
        container.a(i, stack); // putStackInSlot
    }

    // Slot members
    
    protected boolean hasStack(wz slot) { 
        return slot.c();
    }
    protected int getSlotNumber(wz slot) {
        return slot.c;
    }
    protected yq getStack(wz slot) {
        return slot.b();
    }
    protected int getXDisplayPosition(wz slot) {
        return slot.d;
    }
    protected int getYDisplayPosition(wz slot) {
        return slot.e;
    }
    protected boolean isItemValid(wz item, yq itemStack) {
        return item.a(itemStack);
    }

    // GuiContainer members

    protected cx getContainer(ft guiContainer) {
        return guiContainer.d; /* inventorySlots */
    }

    // GuiButton
    
    protected void setEnabled(zr guiButton, boolean enabled) { // GuiButton
        guiButton.h = enabled;
    }
    protected int getId(zr guiButton) { // GuiButton
        return guiButton.f;
    }
    protected void setDisplayString(zr guiButton, String string) {
        guiButton.e = string;
    }
    protected String getDisplayString(zr guiButton) {
        return guiButton.e;
    }
    
    // Other

    protected void playSoundAtEntity(vq theWorld, uh thePlayer, String string, float f, float g) {
        theWorld.a(thePlayer, string, f, g);
    }
    protected int getKeyCode(ads keyBinding) { // KeyBinding
        return keyBinding.d;
    }
    protected String getLocalizedString(String key) {
    	return abn.a().b(key);
    }
    protected int getSpecialChestRowSize(ft guiContainer, int defaultValue) {
    	return mods.getSpecialChestRowSize(guiContainer, defaultValue);
    }

    // Static access

    /**
     * Returns the Minecraft folder ensuring: - It is an absolute path - It ends
     * with a folder separator
     */
    public static String getMinecraftDir() {
        String absolutePath = Minecraft.b().getAbsolutePath();
        if (absolutePath.endsWith(".")) {
            return absolutePath.substring(0, absolutePath.length() - 1);
        }
        if (absolutePath.endsWith(File.separator)) {
            return absolutePath;
        } else {
            return absolutePath + File.separatorChar;
        }
    }
    public static yq getHoldStackStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getHoldStack();
    }
    public static ug getCurrentScreenStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getCurrentScreen();
    }
    public static String getCurrentLanguage() {
        return "en_US"; // TODO implement
    }
    
	// Classes
    
    protected boolean isValidChest(ug guiScreen) {
        return guiScreen != null && (isGuiChest(guiScreen)
        		|| isGuiDispenser(guiScreen)
        		|| mods.isSpecialChest(guiScreen));
    }
	protected boolean isValidInventory(ug guiScreen) {
        return guiScreen != null && (isGuiInventory(guiScreen)
        		|| isGuiWorkbench(guiScreen)
        		|| isGuiFurnace(guiScreen)
                || isGuiBrewingStand(guiScreen)
                || isGuiEnchantmentTable(guiScreen)
        		|| mods.isSpecialInventory(guiScreen));
    }
	
    protected boolean isGuiContainer(Object o) { // GuiContainer (abstract class)
        return o != null && o instanceof ft;
    }
    protected boolean isGuiContainerCreative(Object o) { // GuiContainerCreative
        return o != null && o.getClass().equals(rt.class);
    }
    protected boolean isGuiChest(Object o) { // GuiChest
        return o != null && o.getClass().equals(xq.class);
    }
    protected boolean isGuiDispenser(Object o) { // GuiDispenser
        return o != null && o.getClass().equals(ew.class);
    }
    protected boolean isGuiInventory(Object o) { // GuiInventory
        return o != null && o.getClass().equals(agi.class);
    }
    protected boolean isGuiEditSign(Object o) { // GuiEditSign
        return o != null && o.getClass().equals(ajg.class);
    }
    protected boolean isGuiWorkbench(Object o) { // GuiWorkbench
        return o != null && o.getClass().equals(acm.class);
    }
    protected boolean isGuiBrewingStand(Object o) { // GuiBrewingStand
        return o != null && o.getClass().equals(gt.class);
    }
    protected boolean isGuiEnchantmentTable(Object o) { // GuiEnchantmentTable
        return o != null && o.getClass().equals(ro.class);
    }
    protected boolean isGuiButton(Object o) { // GuiButton
        return o != null && o instanceof zr;
    }
    protected boolean isGuiFurnace(Object o) { // GuiFurnace
        return o != null && o instanceof ke;
    }
    
	protected boolean isContainerPlayer(Object o) { // ContainerPlayer
	    return o != null && o.getClass().equals(w.class);
	}
    protected boolean isContainerChest(Object o) { // ContainerChest
        return o != null && o.getClass().equals(bm.class);
    }
    protected boolean isContainerFurnace(Object o) { // ContainerFurnace 
        return o != null && o.getClass().equals(tc.class);
    }
    protected boolean isContainerDispenser(Object o) { // ContainerDispenser
        return o != null && o.getClass().equals(afc.class);
    }
    protected boolean isContainerWorkbench(Object o) { // ContainerWorkbench
        return o != null && o.getClass().equals(yk.class);
    }
    protected boolean isContainerBrewingStand(Object o) { // ContainerBrewingStand
        return o != null && o.getClass().equals(ys.class);
    }
    protected boolean isContainerEnchantmentTable(Object o) { // ContainerEnchantmentTable
        return o != null && o.getClass().equals(gc.class);
    }
    protected boolean isContainerCreative(Object o) { // ContainerCreative
        return o != null && o.getClass().equals(nv.class);
    }

    protected boolean isItemArmor(Object o) { // ItemArmor
        return o != null && o instanceof po;
    }

    protected boolean isSlot(Object o) { // Slot
        return o != null && o instanceof wz;
    }

}