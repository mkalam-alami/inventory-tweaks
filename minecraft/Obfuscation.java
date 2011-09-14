import java.io.File;
import java.util.List;

import net.minecraft.client.Minecraft;

/**
 * Minecraft 1.8 Obfuscation layer
 * 
 * @author Jimeo Wan
 *
 */
public class Obfuscation {

	protected Minecraft mc;
	
	public Obfuscation(Minecraft mc) {
		this.mc = mc;
	}
	
	// Minecraft members

	public void addChatMessage(String message) {
		if (mc.v != null) {
			mc.v.a(message);
		}
	}
	public boolean isMultiplayerWorld() {
		return mc.l();
	}
    public qs getThePlayer() { // EntityPlayer
        return mc.h;
    }
	public hw getPlayerController() { // PlayerController
		return mc.c;
	}
	public qr getCurrentScreen() { // GuiScreen
		return mc.r;
	}
	public static File getMinecraftDir() { 
		return Minecraft.b();
	}

	// EntityPlayer members
	
	public ui getInventoryPlayer() { // InventoryPlayer
		return getThePlayer().as;
	}
	public ul getCurrentEquippedItem() { // ItemStack
		return getThePlayer().aj();
	}
	public cf getCraftingInventory() { // Container
		return getThePlayer().au;
	}

	// InventoryPlayer members
	
	public ul[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	public void setMainInventory(ul[] value) {
		getInventoryPlayer().a = value;
	}
	public void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	public void setHoldStack(ul stack) {
		getInventoryPlayer().b(stack); // MCP name: setItemStack
	}
	public boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	public ul getHoldStack() {
		return getInventoryPlayer().j(); // MCP name: getItemStack
	}
	public ul getFocusedStack() {
		return getInventoryPlayer().b(); // MCP name: getCurrentItem
	}
	public int getFocusedSlot() {
		return getInventoryPlayer().c; // MCP name: currentItem
	}
	
	// ItemStack members

	public ul createItemStack(int id, int size, int damage) {
		return new ul(id, size, damage);
	}
	public ul copy(ul itemStack) {
		return itemStack.k();
	}
	public int getItemDamage(ul itemStack) {
		return itemStack.i();
	}
	public int getMaxStackSize(ul itemStack) {
		return itemStack.c();
	}
	public int getStackSize(ul itemStack) {
		return itemStack.a;
	}
	public void setStackSize(ul itemStack, int value) {
		itemStack.a = value;
	}
	public int getItemID(ul itemStack) {
		return itemStack.c;
	}
	public boolean areItemStacksEqual(ul itemStack1, ul itemStack2) {
		return ul.a(itemStack1, itemStack2);
	}
	public ul getItemStack(ul[] stacks, int i) {
		return stacks[i];
	}
	
	// Slot members
	
	public boolean hasStack(sx slot) { // Slot
	    return slot.b();
	}
    public int getSlotNumber(sx slot) { // Slot
        return slot.?;
    }
	
	// PlayerController members

	public ul clickInventory(hw playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, qs entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				shiftHold, entityPlayer);
	}
	
	// Container members
	
	public int getWindowId(cf container) {
		return container.f;
	}
	public List<?> getSlots(cf container) {
		return container.e;
	}
	
	// Classes
	
	public boolean isGuiContainer(Object o) {
        return o instanceof GuiContainer;
	}
	public boolean isContainerPlayer(Object o) {
	    return o instanceof ContainerPlayer;
	}
    public boolean isContainerChest(Object o) {
        return o instanceof ContainerPlayer;
    }
    public boolean isContainerFurnace(Object o) {
        return o instanceof ContainerPlayer;
    }
    public boolean isContainerDispenser(Object o) {
        return o instanceof ContainerPlayer;
    }
    public boolean isContainerWorkbench(Object o) {
        return o instanceof ContainerPlayer;
    }
	
}