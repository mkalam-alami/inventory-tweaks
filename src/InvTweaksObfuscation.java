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
    protected anx getThePlayer() { // EntityPlayer
        return mc.g;
    }
    protected anu getTheWorld() { // World
        return mc.e;
    }
	protected ant getPlayerController() { // PlayerController
		return mc.b;
	}
	protected akm getCurrentScreen() { // GuiScreen
		return mc.r;
	}
	protected adn getFontRenderer() { // FontRenderer
        return mc.p;
    }
    protected void displayGuiScreen(akm guiScreen) {
        mc.a(guiScreen);
    }
    protected int getDisplayWidth() {
        return mc.c;
    }
    protected int getDisplayHeight() {
        return mc.d;
    }
    protected net.minecraft.client.z getGameSettings() {
        return mc.y;
    }
    protected int getKeyBindingForwardKeyCode() {
        return getKeyCode(getGameSettings().w);
    }
    protected int getKeyBindingBackKeyCode() {
        return getKeyCode(getGameSettings().y);
    }

	// EntityPlayer members
	
	protected nn getInventoryPlayer() { // InventoryPlayer
		return getThePlayer().by;
	}
	protected qr getCurrentEquippedItem() { // ItemStack
		return getThePlayer().bC();
	}
	protected od getCraftingInventory() { // Container
		return getThePlayer().bA;
	}
    protected oq getPlayerContainer() { // ContainerPlayer
        return (oq) getThePlayer().bz; // MCP name: inventorySlots
    }

	// InventoryPlayer members
	
	protected qr[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	protected void setMainInventory(qr[] value) {
		getInventoryPlayer().a = value;
	}
	protected void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	protected void setHoldStack(qr stack) {
		getInventoryPlayer().b(stack); // setItemStack
	}
	protected boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	protected qr getHoldStack() {
		return getInventoryPlayer().o(); // getItemStack
	}
	protected qr getFocusedStack() {
		return getInventoryPlayer().g(); // getCurrentItem
	}
	protected int getFocusedSlot() {
		return getInventoryPlayer().c; // currentItem
	}
	
    // GuiScreen members
	
	protected int getWidth(akm guiScreen) {
	    return guiScreen.f;
	}
    protected int getHeight(akm guiScreen) {
        return guiScreen.g;
    }
    protected int getXSize(alg guiContainer) { // GuiContainer
        return guiContainer.b;
    }
    protected int getYSize(alg guiContainer) {
        return guiContainer.c;
    }
    @SuppressWarnings("unchecked")
	protected List<Object> getControlList(akm guiScreen) {
        return guiScreen.h;
    }
    protected void setControlList(akm guiScreen, List<?> controlList) {
        guiScreen.h = controlList;
    }
    protected alg asGuiContainer(akm guiScreen) {
        return (alg) guiScreen;
    }

    // FontRenderer members
	
	protected int getStringWidth(adn fontRenderer, String line) {
	    return fontRenderer.a(line);
	}
	protected void drawStringWithShadow(adn fontRenderer,
            String s, int i, int j, int k) {
        fontRenderer.a(s, i, j, k);
    }
	
	// ItemStack members

	protected qr createItemStack(int id, int size, int damage) {
		return new qr(id, size, damage);
	}
	protected qr copy(qr itemStack) {
		return itemStack.l();
	}
	protected int getItemDamage(qr itemStack) {
		return itemStack.j();
	}
	protected int getMaxStackSize(qr itemStack) {
		return itemStack.d();
	}
	protected boolean hasDataTags(qr itemStack) {
	  return itemStack.o();
	}
	protected int getStackSize(qr itemStack) {
		return itemStack.a;
	}
	protected int getItemID(qr itemStack) {
		return itemStack.c;
	}
	protected boolean areItemStacksEqual(qr itemStack1, qr itemStack2) {
		return itemStack1.c(itemStack2); // dk.a(itemStack1, itemStack2);
	}
    protected boolean isItemStackDamageable(qr itemStack) {
        return itemStack.f();
    }
    protected boolean areSameItemType(qr itemStack1, qr itemStack2) {
        return areItemsEqual(itemStack1, itemStack2) ||
                (isItemStackDamageable(itemStack1)
                        && getItemID(itemStack1) == getItemID(itemStack2));
    }
    protected boolean areItemsEqual(qr itemStack1, qr itemStack2) {
        return itemStack1.a(itemStack2); // isItemEqual
    }
    protected int getAnimationsToGo(qr itemStack) {
        return itemStack.b;
    }
    protected qp getItem(qr itemStack) { // Item
        return itemStack.b();
    }
    
    // Item & ItemArmor
    
    protected boolean isDamageable(qp item) {
        return item.m();
    }
    protected int getArmorLevel(pb itemArmor) { // ItemArmor
        return itemArmor.b;
    }
    protected pb asItemArmor(qp item) { // ItemArmor
        return (pb) item;
    }
	
	// PlayerController members

	protected qr clickInventory(ant playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, anx entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				shiftHold, entityPlayer);
	}
	
	// Container members
	
	protected int getWindowId(od container) {
		return container.c;
	}
	protected List<?> getSlots(od container) {
		return container.b;
	}
    protected oz getSlot(od container, int i) { // Slot
        return (oz) (getSlots(container).get(i));
    }

    protected qr getSlotStack(od container, int i) {
        oz slot = getSlot(container, i);
        return (slot == null) ? null : getStack(slot); // getStack
    }

    protected void setSlotStack(od container, int i, qr stack) {
        container.a(i, stack); // putStackInSlot
    }

    // Slot members
    
    protected boolean hasStack(oz slot) { 
        return slot.d();
    }
    protected int getSlotNumber(oz slot) {
        return slot.d;
    }
    protected qr getStack(oz slot) {
        return slot.c();
    }
    protected int getXDisplayPosition(oz slot) {
        return slot.e;
    }
    protected int getYDisplayPosition(oz slot) {
        return slot.f;
    }
    protected boolean areSlotAndStackCompatible(oz slot, qr itemStack) {
        return slot.a(itemStack); // isItemValid
    }

    // GuiContainer members

    protected od getContainer(alg guiContainer) {
        return guiContainer.d; /* inventorySlots */
    }

    // GuiButton
    
    protected ye asGuiButton(Object o) {
        return (ye) o;
    }
    protected void setEnabled(ye guiButton, boolean enabled) { // GuiButton
        guiButton.g = enabled;
    }
    protected int getId(ye guiButton) { // GuiButton
        return guiButton.f;
    }
    protected void setDisplayString(ye guiButton, String string) {
        guiButton.e = string;
    }
    protected String getDisplayString(ye guiButton) {
        return guiButton.e;
    }
    
    // Other

    protected void playSoundAtEntity(anu theWorld, anx thePlayer, String string, float f, float g) {
        theWorld.a(thePlayer, string, f, g);
    }
    protected int getKeyCode(net.minecraft.client.g w) { // KeyBinding
        return w.d;
    }
    protected int getSpecialChestRowSize(alg guiContainer, int defaultValue) {
    	return mods.getSpecialChestRowSize(guiContainer, defaultValue);
    }

    // Static access

    public static aj getLocalizationService() { // StringTranslate
        return aj.a(); // StringTranslate.getInstance()
    }
    public static String getCurrentLanguage() {
        return getLocalizationService().c();
    }
    public static String getLocalizedString(String key) {
        return getLocalizationService().b(key);
    }
    public static qr getHoldStackStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getHoldStack();
    }
    public static akm getCurrentScreenStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getCurrentScreen();
    }
    
	// Classes
    
    protected boolean isValidChest(akm guiScreen) {
        return guiScreen != null && (isGuiChest(guiScreen)
        		|| isGuiDispenser(guiScreen)
        		|| mods.isSpecialChest(guiScreen));
    }
	protected boolean isValidInventory(akm guiScreen) {
        return isStandardInventory(guiScreen)
        		|| mods.isSpecialInventory(guiScreen);
    }
	protected boolean isStandardInventory(akm guiScreen) {
        return isGuiInventory(guiScreen)
        		|| isGuiWorkbench(guiScreen)
        		|| isGuiFurnace(guiScreen)
                || isGuiBrewingStand(guiScreen)
                || isGuiEnchantmentTable(guiScreen)
                || isGuiTrading(guiScreen);
    }
	
    protected boolean isGuiContainer(Object o) { // GuiContainer (abstract class)
        return o != null && o instanceof alg;
    }
    protected boolean isGuiContainerCreative(Object o) { // GuiContainerCreative
        return o != null && o.getClass().equals(alm.class);
    }
    protected boolean isGuiChest(Object o) { // GuiChest
        return o != null && o.getClass().equals(alk.class);
    }
    protected boolean isGuiDispenser(Object o) { // GuiDispenser
        return o != null && o.getClass().equals(alx.class);
    }
    protected boolean isGuiInventory(Object o) { // GuiInventory
        return o != null && o.getClass().equals(alp.class);
    }
    protected boolean isGuiEditSign(Object o) { // GuiEditSign
        return o != null && o.getClass().equals(alw.class);
    }
    protected boolean isGuiWorkbench(Object o) { // GuiWorkbench
        return o != null && o.getClass().equals(all.class);
    }
    protected boolean isGuiBrewingStand(Object o) { // GuiBrewingStand
        return o != null && o.getClass().equals(alj.class);
    }
    protected boolean isGuiEnchantmentTable(Object o) { // GuiEnchantmentTable
        return o != null && o.getClass().equals(alr.class);
    }
    protected boolean isGuiTrading(Object o) { // GuiTrading
        return o != null && o.getClass().equals(alu.class);
    }
    protected boolean isGuiFurnace(Object o) { // GuiFurnace
        return o != null && o.getClass().equals(als.class);
    }
    protected boolean isGuiButton(Object o) { // GuiButton
        return o != null && o instanceof ye;
    }
    
	protected boolean isContainerPlayer(Object o) { // ContainerPlayer
	    return o != null && o.getClass().equals(oq.class);
	}
    protected boolean isContainerChest(Object o) { // ContainerChest
        return o != null && o.getClass().equals(oi.class);
    }
    protected boolean isContainerFurnace(Object o) { // ContainerFurnace 
        return o != null && o.getClass().equals(oo.class);
    }
    protected boolean isContainerDispenser(Object o) { // ContainerDispenser
        return o != null && o.getClass().equals(pa.class);
    }
    protected boolean isContainerWorkbench(Object o) { // ContainerWorkbench
        return o != null && o.getClass().equals(ok.class);
    }
    protected boolean isContainerBrewingStand(Object o) { // ContainerBrewingStand
        return o != null && o.getClass().equals(oe.class);
    }
    protected boolean isContainerEnchantmentTable(Object o) { // ContainerEnchantmentTable
        return o != null && o.getClass().equals(ol.class);
    }
    protected boolean isContainerCreative(Object o) { // ContainerCreative
        return o != null && o.getClass().equals(aln.class);
    }

    protected boolean isItemArmor(Object o) { // ItemArmor
        return o != null && o instanceof pb;
    }

    protected boolean isBasicSlot(Object o) { // Slot
        return o != null && o.getClass().equals(oz.class);
    }

}