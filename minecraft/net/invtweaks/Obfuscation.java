package net.invtweaks;

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
 * Eases transitions when Minecraft then MCP is updated.
 * @author Jimeo Wan
 *
 */
public class Obfuscation {

    protected Minecraft mc;

    protected Obfuscation(Minecraft mc) {
        this.mc = mc;
    }

    // Minecraft members

    protected void addChatMessage(String message) {
        if (mc.ingameGUI != null) {
            mc.ingameGUI.addChatMessage(message);
        }
    }

    protected boolean isMultiplayerWorld() {
        return mc.isMultiplayerWorld();
    }

    protected EntityPlayer getThePlayer() {
        return mc.thePlayer;
    }

    protected PlayerController getPlayerController() {
        return mc.playerController;
    }

    protected GuiScreen getCurrentScreen() {
        return mc.currentScreen;
    }

    /**
     * Returns the Minecraft folder ensuring: - It is an absolute path - It ends
     * with a folder separator
     */
    protected static String getMinecraftDir() {
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

    protected InventoryPlayer getInventoryPlayer() {
        return getThePlayer().inventory;
    }

    protected ItemStack getCurrentEquippedItem() {
        return getThePlayer().getCurrentEquippedItem();
    }

    protected Container getCraftingInventory() {
        return getThePlayer().craftingInventory;
    }

    protected Container getPlayerContainer() {
        return getThePlayer().inventorySlots; // MCP name: inventorySlots
    }

    // InventoryPlayer members

    protected ItemStack[] getMainInventory() {
        return getInventoryPlayer().mainInventory;
    }

    protected void setMainInventory(ItemStack[] value) {
        getInventoryPlayer().mainInventory = value;
    }

    protected void setHasInventoryChanged(boolean value) {
        getInventoryPlayer().inventoryChanged = value;
    }

    protected void setHoldStack(ItemStack stack) {
        getInventoryPlayer().setItemStack(stack); // MCP name: setItemStack
    }

    protected boolean hasInventoryChanged() {
        return getInventoryPlayer().inventoryChanged;
    }

    protected ItemStack getHoldStack() {
        return getInventoryPlayer().getItemStack(); // MCP name: getItemStack
    }

    protected ItemStack getFocusedStack() {
        return getInventoryPlayer().getCurrentItem(); // MCP name:
                                                      // getCurrentItem
    }

    protected int getFocusedSlot() {
        return getInventoryPlayer().currentItem; // MCP name: currentItem
    }

    // ItemStack members

    protected ItemStack createItemStack(int id, int size, int damage) {
        return new ItemStack(id, size, damage);
    }

    protected ItemStack copy(ItemStack itemStack) {
        return itemStack.copy();
    }

    protected int getItemDamage(ItemStack itemStack) {
        return itemStack.getItemDamage();
    }

    protected int getMaxStackSize(ItemStack itemStack) {
        return itemStack.getMaxStackSize();
    }

    protected int getStackSize(ItemStack itemStack) {
        return itemStack.stackSize;
    }

    protected void setStackSize(ItemStack itemStack, int value) {
        itemStack.stackSize = value;
    }

    protected int getItemID(ItemStack itemStack) {
        return itemStack.itemID;
    }

    protected boolean areItemStacksEqual(ItemStack itemStack1, ItemStack itemStack2) {
        return ItemStack.areItemStacksEqual(itemStack1, itemStack2);
    }

    protected ItemStack getItemStack(ItemStack[] stacks, int i) {
        return stacks[i];
    }

    // PlayerController members

    protected ItemStack clickInventory(PlayerController playerController,
            int windowId, int slot, int clickButton, boolean shiftHold,
            EntityPlayer entityPlayer) {
        return playerController.func_27174_a(windowId, slot, clickButton,
                shiftHold, entityPlayer); /* func_27174_a */
    }

    // Container members

    protected int getWindowId(Container container) {
        return container.windowId;
    }

    protected List<?> getSlots(Container container) {
        return container.slots;
    }

    protected Slot getSlot(Container container, int i) {
        return (Slot) getSlots(container).get(i);
    }

    protected ItemStack getSlotStack(Container container, int i) {
        Slot slot = (Slot) getSlots(container).get(i);
        return (slot == null) ? null : slot.getStack(); /* getStack */
    }

    protected void setSlotStack(Container container, int i, ItemStack stack) {
        container.putStackInSlot(i, stack); /* putStackInSlot */
    }

    // GuiContainer members

    protected Container getContainer(GuiContainer guiContainer) {
        return guiContainer.inventorySlots;
    }

    // Other

    protected boolean isChestOrDispenser(GuiScreen guiScreen) {
        return (guiScreen instanceof GuiChest /* GuiChest */
        || guiScreen instanceof GuiDispenser /* GuiDispenser */);
    }
    
    protected int getKeycode(KeyBinding keyBinding) {
        return keyBinding.keyCode;
    }

}