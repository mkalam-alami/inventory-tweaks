package net.minecraft.src;

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
		if (mc.ingameGUI != null) {
			mc.ingameGUI.addChatMessage(message);
		}
	}
	public boolean isMultiplayerWorld() {
		return mc.isMultiplayerWorld();
	}
	public EntityPlayer getThePlayer() {
		return mc.thePlayer;
	}
	public PlayerController getPlayerController() {
		return mc.playerController;
	}
	public GuiScreen getCurrentScreen() {
		return mc.currentScreen;
	}
	public static File getMinecraftDir() {
		return Minecraft.getMinecraftDir();
	}

	// EntityPlayer members
	
	public InventoryPlayer getInventoryPlayer() {
		return getThePlayer().inventory;
	}
	public ItemStack getCurrentEquippedItem() {
		return getThePlayer().getCurrentEquippedItem();
	}
	public Container getCraftingInventory() {
		return getThePlayer().craftingInventory;
	}

	// InventoryPlayer members
	
	public ItemStack[] getMainInventory() {
		return getInventoryPlayer().mainInventory;
	}
	public void setMainInventory(ItemStack[] value) {
		getInventoryPlayer().mainInventory = value;
	}
	public void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().inventoryChanged = value;
	}
	public void setHoldStack(ItemStack stack) {
		getInventoryPlayer().setItemStack(stack); // MCP name: setItemStack
	}
	public boolean hasInventoryChanged() {
		return getInventoryPlayer().inventoryChanged;
	}
	public ItemStack getHoldStack() {
		return getInventoryPlayer().getItemStack(); // MCP name: getItemStack
	}
	public ItemStack getFocusedStack() {
		return getInventoryPlayer().getCurrentItem(); // MCP name: getCurrentItem
	}
	public int getFocusedSlot() {
		return getInventoryPlayer().currentItem; // MCP name: currentItem
	}
	
	// ItemStack members

	public ItemStack createItemStack(int id, int size, int damage) {
		return new ItemStack(id, size, damage);
	}
	public ItemStack copy(ItemStack itemStack) {
		return itemStack.copy();
	}
	public int getItemDamage(ItemStack itemStack) {
		return itemStack.getItemDamage();
	}
	public int getMaxStackSize(ItemStack itemStack) {
		return itemStack.getMaxStackSize();
	}
	public int getStackSize(ItemStack itemStack) {
		return itemStack.stackSize;
	}
	public void setStackSize(ItemStack itemStack, int value) {
		itemStack.stackSize = value;
	}
	public int getItemID(ItemStack itemStack) {
		return itemStack.itemID;
	}
	public boolean areItemStacksEqual(ItemStack itemStack1, ItemStack itemStack2) {
		return ItemStack.areItemStacksEqual(itemStack1, itemStack2);
	}
	public ItemStack getItemStack(ItemStack[] stacks, int i) {
		return stacks[i];
	}
	
	// PlayerController members

	public ItemStack clickInventory(PlayerController playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, EntityPlayer entityPlayer) {
		return playerController.func_27174_a(windowId, slot, clickButton,
				shiftHold, entityPlayer); /* func_27174_a */
	}
	
	// Container members
	
	public int getWindowId(Container container) {
		return container.windowId;
	}
	public List<?> getSlots(Container container) {
		return container.slots;
	}
	
	// GuiContainer members
	
	public Container getInventorySlots(GuiContainer guiContainer) {
		return guiContainer.inventorySlots;
	}
	
}