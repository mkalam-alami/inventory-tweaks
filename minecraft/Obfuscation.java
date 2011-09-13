
import java.io.File;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.src.*;

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
    public qs getThePlayer() {
        return mc.h;
    }
	public hw getPlayerController() {
		return mc.c;
	}
	public qr getCurrentScreen() {
		return mc.r;
	}
	public static File getMinecraftDir() {
		return Minecraft.b();
	}

	// EntityPlayer members
	
	public ui getInventoryPlayer() {
		return getThePlayer().as;
	}
	public ul getCurrentEquippedItem() {
		return getThePlayer().G();/*?*/
	}
	public dw getCraftingInventory() {
		return getThePlayer().e;/*?*/
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
	
	// PlayerController members

	public ul clickInventory(ob/*?*/ playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, qs entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				shiftHold, entityPlayer);
	}
	
	// Container members
	
	public int getWindowId(dw container) {
		return container.f;/*?*/
	}
	public List<?> getSlots(dw container) {
		return container.e;/*?*/
	}
	
}