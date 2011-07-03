import java.io.File;
import java.util.List;

import net.minecraft.client.Minecraft;

public class InvTweaksObf {

	private Minecraft mc;
	
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
	public File getMinecraftDir() {
		return Minecraft.b();
	}
	public dc getThePlayer() {
		return mc.h;
	}
	public ix getInventoryPlayer() {
		return mc.h.c;
	}
	public iz[] getMainInventory() {
		return mc.h.c.a;
	}
	public ob getPlayerController() {
		return mc.c;
	}
	public da getCurrentScreen() {
		return mc.r;
	}
	
	// ItemStack members

	public iz copy(iz itemStack) {
		return itemStack.k();
	}
	public boolean areItemStacksEqual(iz itemStack1, iz itemStack2) {
		return iz.a(itemStack1, itemStack2);
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
	public int getItemID(iz itemStack) {
		return itemStack.c;
	}
	
	// InventoryPlayer members

	public boolean hasInventoryChanged(ix invPlayer) {
		return invPlayer.e;
	}
	public iz[] getMainInventory(ix inventory) {
		return inventory.a;
	}
	public iz getItemStack(ix invPlayer) { // TODO Rename
		return invPlayer.i();
	}
	public iz getCurrentItem(ix invPlayer) { // TODO Rename
		return invPlayer.b();
	}
	public int getSelectedSlot(ix invPlayer) {
		return invPlayer.c; // MCP name: currentItem
	}

	// EntityPlayer members

	public ix getInventoryPlayer(dc thePlayer) {
		return thePlayer.c;
	}
	public iz getCurrentEquippedItem(gs entityPlayer) {
		return entityPlayer.G();
	}
	public dw getCraftingInventory(gs entityPlayer) {
		return entityPlayer.e;
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