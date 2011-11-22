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

    //private static final Logger log = Logger.getLogger("InvTweaks");

    protected Minecraft mc;
	
	public InvTweaksObfuscation(Minecraft mc) {
		this.mc = mc;
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
    protected di /* extends vi */ getThePlayer() { // EntityPlayer
        return mc.h;
    }
    protected ry getTheWorld() { // World
        return mc.f;
    }
	protected aes getPlayerController() { // PlayerController
		return mc.c;
	}
	protected xe getCurrentScreen() { // GuiScreen
		return mc.s;
	}
	protected abe getFontRenderer() { // FontRenderer
        return mc.q;
    }
    protected void displayGuiScreen(xe parentScreen) {
        mc.a(parentScreen);
    }
    protected int getDisplayWidth() {
        return mc.d;
    }
    protected int getDisplayHeight() {
        return mc.e;
    }
    protected ki getGameSettings() {
        return mc.A;
    }
    protected int getKeyBindingForwardKeyCode() {
        return getKeycode(getGameSettings().n);
    }
    protected int getKeyBindingBackKeyCode() {
        return getKeycode(getGameSettings().p);
    }

	// EntityPlayer members
	
	protected x getInventoryPlayer() { // InventoryPlayer
		return getThePlayer().by;
	}
	protected dk getCurrentEquippedItem() { // ItemStack
		return getThePlayer().aH();
	}
	protected pj getCraftingInventory() { // Container
		return getThePlayer().bA;
	}
    protected gd getPlayerContainer() { // ContainerPlayer
        return (gd) getThePlayer().bz; // MCP name: inventorySlots
    }

	// InventoryPlayer members
	
	protected dk[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	protected void setMainInventory(dk[] value) {
		getInventoryPlayer().a = value;
	}
	protected void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	protected void setHoldStack(dk stack) {
		getInventoryPlayer().b(stack); // setItemStack
	}
	protected boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	protected dk getHoldStack() {
		return getInventoryPlayer().i(); // getItemStack
	}
	protected dk getFocusedStack() {
		return getInventoryPlayer().a(); // getCurrentItem
	}
	protected int getFocusedSlot() {
		return getInventoryPlayer().c; // currentItem
	}
	
    // GuiScreen members
	
	protected int getWidth(xe guiScreen) {
	    return guiScreen.m;
	}
    protected int getHeight(xe guiScreen) {
        return guiScreen.n;
    }
    protected int getXSize(mg guiContainer) { // GuiContainer
        return guiContainer.b;
    }
    protected int getYSize(mg guiContainer) {
        return guiContainer.c;
    }
    
    @SuppressWarnings("unchecked")
    protected List<InvTweaksObfuscationGuiButton> getControlList(xe guiScreen) {
        return guiScreen.o;
    }
    protected void setControlList(xe guiScreen, List<?> controlList) {
        guiScreen.o = controlList;
    }

    // FontRenderer members
	
	protected int getStringWidth(abe fontRenderer, String line) {
	    return fontRenderer.a(line);
	}
	protected void drawStringWithShadow(abe fontRenderer,
            String s, int i, int j, int k) {
        fontRenderer.a(s, i, j, k);
    }
	
	// ItemStack members

	protected dk createItemStack(int id, int size, int damage) {
		return new dk(id, size, damage);
	}
	protected dk copy(dk itemStack) {
		return itemStack.k();
	}
	protected int getItemDamage(dk itemStack) {
		return itemStack.i();
	}
	protected int getMaxStackSize(dk itemStack) {
		return itemStack.c();
	}
	protected int getStackSize(dk itemStack) {
		return itemStack.a;
	}
	protected void setStackSize(dk itemStack, int value) {
		itemStack.a = value;
	}
	protected int getItemID(dk itemStack) {
		return itemStack.c;
	}
	protected boolean areItemStacksEqual(dk itemStack1, dk itemStack2) {
		return itemStack1.c(itemStack2); // dk.a(itemStack1, itemStack2);
	}
    protected boolean isItemStackDamageable(dk itemStack) {
        return itemStack.e();
    }
    protected boolean areSameItemType(dk itemStack1, dk itemStack2) {
        return areItemsEqual(itemStack1, itemStack2) ||
                (isItemStackDamageable(itemStack1)
                        && getItemID(itemStack1) == getItemID(itemStack2));
    }
    protected boolean areItemsEqual(dk itemStack1, dk itemStack2) {
        return itemStack1.a(itemStack2); // isItemEqual
    }
    protected int getAnimationsToGo(dk itemStack) {
        return itemStack.b;
    }
    protected acy getItem(dk itemStack) { // Item
        return itemStack.a();
    }
    
    // Item & ItemArmor
    
    protected boolean isDamageable(acy item) {
        return item.h();
    }
    protected int getArmorLevel(agi itemArmor) { // ItemArmor
        return itemArmor.a;
    }
	
	// PlayerController members

	protected dk clickInventory(aes playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, vi entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				shiftHold, entityPlayer);
	}
	
	// Container members
	
	protected int getWindowId(pj container) {
		return container.f;
	}
	protected List<?> getSlots(pj container) {
		return container.e;
	}
    protected vv getSlot(pj container, int i) { // Slot
        return (vv) getSlots(container).get(i);
    }

    protected dk getSlotStack(pj container, int i) {
        vv slot = (vv) getSlots(container).get(i);
        return (slot == null) ? null : getStack(slot); // getStack
    }

    protected void setSlotStack(pj container, int i, dk stack) {
        container.a(i, stack); // putStackInSlot
    }

    // Slot members
    
    protected boolean hasStack(vv slot) { 
        return slot.c();
    }
    protected int getSlotNumber(vv slot) {
        return slot.d;
    }
    protected dk getStack(vv slot) {
        return slot.b();
    }
    protected int getXDisplayPosition(vv slot) {
        return slot.e;
    }
    protected int getYDisplayPosition(vv slot) {
        return slot.f;
    }
    protected boolean isItemValid(vv item, dk itemStack) {
        return item.a(itemStack);
    }

    // GuiContainer members

    protected pj getContainer(mg guiContainer) {
        return guiContainer.d; /* inventorySlots */
    }

    // GuiButton
    
    protected void setEnabled(ct guiButton, boolean enabled) { // GuiButton
        guiButton.h = enabled;
    }
    protected int getId(ct guiButton) { // GuiButton
        return guiButton.f;
    }
    protected void setDisplayString(ct guiButton, String string) {
        guiButton.e = string;
    }
    protected String getDisplayString(ct guiButton) {
        return guiButton.e;
    }
    
    // Other

    protected int getKeycode(aby keyBinding) {
        return keyBinding.d;
    }
    protected void playSoundAtEntity(ry theWorld, di thePlayer, String string, float f, float g) {
        theWorld.a(thePlayer, string, f, g);
    }
    protected int getKeyCode(aby keyBinding) { // KeyBinding
        return keyBinding.d;
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
    public static dk getHoldStackStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getHoldStack();
    }
    public static xe getCurrentScreenStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getCurrentScreen();
    }
    
	// Classes
	
	protected boolean isGuiContainer(Object o) { // GuiContainer
        return o instanceof mg;
	}
    protected boolean isGuiContainerCreative(Object o) { // GuiContainerCreative
        return o instanceof aec;
    }
    protected boolean isGuiChest(Object o) { // GuiChest
        return o instanceof gj;
    }
    protected boolean isGuiDispenser(Object o) { // GuiDispenser
        return o instanceof wt;
    }
    protected boolean isGuiInventory(Object o) { // GuiInventory
        return o instanceof hw;
    }
    protected boolean isGuiEditSign(Object o) { // GuiEditSign
        return o instanceof fl;
    }
    
	protected boolean isContainerPlayer(Object o) { // ContainerPlayer
	    return o instanceof gd;
	}
    protected boolean isContainerChest(Object o) { // ContainerChest
        return o instanceof ak;
    }
    protected boolean isContainerFurnace(Object o) { // ContainerFurnace
        return o instanceof eg;
    }
    protected boolean isContainerDispenser(Object o) { // ContainerDispenser
        return o instanceof qu;
    }
    protected boolean isContainerWorkbench(Object o) { // ContainerWorkbench
        return o instanceof ace;
    }

    protected boolean isItemArmor(Object o) { // ItemArmor
        return o instanceof agi;
    }

    protected boolean isSlot(Object o) { // Slot
        return o instanceof vv;
    }
}