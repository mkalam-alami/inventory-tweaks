package net.invtweaks.library;

import java.io.File;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiChest;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiDispenser;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.PlayerController;
import net.minecraft.src.Slot;

/**
 * Obfuscation layer, used to centralize most calls to Minecraft code.
 * Eases transitions when Minecraft then MCP are updated.
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

    /**
     * Returns the Minecraft folder ensuring: - It is an absolute path - It ends
     * with a folder separator
     */
    public static String getMinecraftDir() {
        String absolutePath = Minecraft.getMinecraftDir().getAbsolutePath();
        if (absolutePath.endsWith(".")) {
            return absolutePath.substring(0, absolutePath.length() - 1);
        }
        if (absolutePath.endsWith(File.separator)) {
            return absolutePath;
        } else {
            return absolutePath + File.separatorChar;
        }
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

    public Container getPlayerContainer() {
        return getThePlayer().inventorySlots; // MCP name: inventorySlots
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

    // PlayerController members

    public ItemStack clickInventory(PlayerController playerController,
            int windowId, int slot, int clickButton, boolean shiftHold,
            EntityPlayer entityPlayer) {
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

    public Slot getSlot(Container container, int i) {
        return (Slot) getSlots(container).get(i);
    }

    public ItemStack getSlotStack(Container container, int i) {
        Slot slot = (Slot) getSlots(container).get(i);
        return (slot == null) ? null : slot.getStack(); /* getStack */
    }

    public void setSlotStack(Container container, int i, ItemStack stack) {
        container.putStackInSlot(i, stack); /* putStackInSlot */
    }

    // GuiContainer members

    public Container getContainer(GuiContainer guiContainer) {
        return guiContainer.inventorySlots;
    }

    // Other

    public boolean isChestOrDispenser(GuiScreen guiScreen) {
        return (guiScreen instanceof GuiChest /* GuiChest */
        || guiScreen instanceof GuiDispenser /* GuiDispenser */);
    }
    
    public int getKeycode(KeyBinding keyBinding) {
        return keyBinding.keyCode;
    }

}