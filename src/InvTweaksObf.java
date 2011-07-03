import java.io.File;
import java.util.List;

import net.minecraft.client.Minecraft;

public class InvTweaksObf {

	protected Minecraft mc;
	
	public InvTweaksObf(Minecraft mc) {
		this.mc = mc;
	}
	
	// Minecraft members

	public void addChatMessage(String message) {
		mc.v.a(message);
	}
	public boolean isMultiplayerWorld() {
		return mc.l();
	}
	public dc getThePlayer() {
		return mc.h;
	}
	public ob getPlayerController() {
		return mc.c;
	}
	public da getCurrentScreen() {
		return mc.r;
	}
	public static File getMinecraftDir() {
		return Minecraft.b();
	}

	// EntityPlayer members
	
	public ix getInventoryPlayer() {
		return getThePlayer().c;
	}
	public iz getCurrentEquippedItem() {
		return getThePlayer().G();
	}
	public dw getCraftingInventory() {
		return getThePlayer().e;
	}

	// InventoryPlayer members
	
	public iz[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	public void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	public void setHoldStack(iz stack) {
		getInventoryPlayer().b(stack); // MCP name: setItemStack
	}
	public boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	public iz getHoldStack() {
		return getInventoryPlayer().i(); // MCP name: getItemStack
	}
	public iz getFocusedStack() {
		return getInventoryPlayer().b(); // MCP name: getCurrentItem
	}
	public int getFocusedSlot() {
		return getInventoryPlayer().c; // MCP name: currentItem
	}
	
	// ItemStack members

	public iz copy(iz itemStack) {
		return itemStack.k();
	}
	public int getItemDamage(iz itemStack) {
		return itemStack.i();
	}
	public int getMaxStackSize(iz itemStack) {
		return itemStack.c();
	}
	public int getStackSize(iz itemStack) {
		return itemStack.a;
	}
	public void setStackSize(iz itemStack, int value) {
		itemStack.a = value;
	}
	public int getItemID(iz itemStack) {
		return itemStack.c;
	}
	public boolean areItemStacksEqual(iz itemStack1, iz itemStack2) {
		return iz.a(itemStack1, itemStack2);
	}
	public iz getItemStack(iz[] stacks, int i) {
		return stacks[i];
	}
	
	// PlayerController members

	public iz clickInventory(ob playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, gs entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				shiftHold, entityPlayer);
	}
	
	// Container members
	
	public int getWindowId(dw container) {
		return container.f;
	}
	public List<?> getSlots(dw container) {
		return container.e;
	}
	
}