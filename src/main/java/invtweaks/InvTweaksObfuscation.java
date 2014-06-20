package invtweaks;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import invtweaks.api.container.ContainerSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minecraft 1.3 Obfuscation layer
 *
 * @author Jimeo Wan
 */
public class InvTweaksObfuscation {

    private static final Logger log = InvTweaks.log;

    public Minecraft mc;

    private static Map<String, Field> fieldsMap = new HashMap<String, Field>();

    public InvTweaksObfuscation(Minecraft mc) {
        this.mc = mc;
    }

    // Minecraft members

    public void addChatMessage(String message) {
        if(mc.ingameGUI != null) {
            mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(message));
        }
    }

    public static String getNamespacedID(String id) {
        if(id == null) {
            return null;
        } else if(id.indexOf(':') == -1) {
            return "minecraft:" + id;
        }
        return id;
    }

    public EntityPlayer getThePlayer() {
        return mc.thePlayer;
    }

    public PlayerControllerMP getPlayerController() {
        return mc.playerController;
    }

    public GuiScreen getCurrentScreen() {
        return mc.currentScreen;
    }

    public FontRenderer getFontRenderer() {
        return mc.fontRenderer;
    }

    public void displayGuiScreen(GuiScreen parentScreen) {
        mc.displayGuiScreen(parentScreen);
    }

    public static int getDisplayWidth() {
        return FMLClientHandler.instance().getClient().displayWidth;
    }

    public static int getDisplayHeight() {
        return FMLClientHandler.instance().getClient().displayHeight;
    }

    public GameSettings getGameSettings() {
        return mc.gameSettings;
    }

    public int getKeyBindingForwardKeyCode() {
        return getGameSettings().keyBindForward.keyCode;
    }

    public int getKeyBindingBackKeyCode() {
        return getGameSettings().keyBindBack.keyCode;
    }

    // EntityPlayer members

    public InventoryPlayer getInventoryPlayer() { // InventoryPlayer
        return getThePlayer().inventory;
    }

    public ItemStack getCurrentEquippedItem() { // ItemStack
        return getThePlayer().getCurrentEquippedItem();
    }

    public ContainerPlayer getPlayerContainer() {
        return (ContainerPlayer) getThePlayer().inventoryContainer;
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

    public void setHeldStack(ItemStack stack) {
        getInventoryPlayer().setItemStack(stack); // setItemStack
    }

    public boolean hasInventoryChanged() {
        return getInventoryPlayer().inventoryChanged;
    }

    public ItemStack getHeldStack() {
        return getInventoryPlayer().getItemStack(); // getItemStack
    }

    public ItemStack getFocusedStack() {
        return getInventoryPlayer().getCurrentItem(); // getCurrentItem
    }

    public int getFocusedSlot() {
        return getInventoryPlayer().currentItem; // currentItem
    }

    // FontRenderer members

    public static boolean areItemStacksEqual(ItemStack itemStack1, ItemStack itemStack2) {
        return itemStack1.isItemEqual(itemStack2) && itemStack1.stackSize == itemStack2.stackSize;
    }

    public boolean areSameItemType(ItemStack itemStack1, ItemStack itemStack2) {
        return itemStack1.isItemEqual(itemStack2) || (itemStack1.isItemStackDamageable() && itemStack1
                .getItem() == itemStack2.getItem());
    }

    public boolean areItemsStackable(ItemStack itemStack1, ItemStack itemStack2) {
        return itemStack1 != null && itemStack2 != null && itemStack1.isItemEqual(itemStack2) &&
                itemStack1.isStackable() &&
                (!itemStack1.getHasSubtypes() || itemStack1.getItemDamage() == itemStack2.getItemDamage()) &&
                ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);
    }

    // Container members

    public static ItemStack getSlotStack(Container container, int i) {
        // Slot
        Slot slot = (Slot) (container.inventorySlots.get(i));
        return (slot == null) ? null : slot.getStack(); // getStack
    }

    // Slot members

    @SuppressWarnings("unchecked")
    public static int getSlotNumber(Slot slot) {
        /* FIXME: Cannot compile until SpecialSource update
        try {
            // Creative slots don't set the "slotNumber" property, serve as a proxy for true slots
            if(slot instanceof GuiContainerCreative.CreativeSlot) {
                Slot underlyingSlot = ((SlotCreativeInventory)slot).theSlot;
                if(underlyingSlot != null) {
                    return underlyingSlot.slotNumber;
                } else {
                    log.warn("Creative inventory: Failed to get real slot");
                }
            }
        } catch(Exception e) {
            log.warn("Failed to access creative slot number");
        }
        */
        return slot.slotNumber;
    }

    @SideOnly(Side.CLIENT)
    public static Slot getSlotAtMousePosition(GuiContainer guiContainer) {
        // Copied from GuiContainer
        if(guiContainer != null) {
            Container container = guiContainer.inventorySlots;

            int x = getMouseX(guiContainer);
            int y = getMouseY(guiContainer);
            for(int k = 0; k < container.inventorySlots.size(); k++) {
                Slot slot = (Slot) container.inventorySlots.get(k);
                if(getIsMouseOverSlot(guiContainer, slot, x, y)) {
                    return slot;
                }
            }
            return null;
        } else {
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean getIsMouseOverSlot(GuiContainer guiContainer, Slot slot) {
        return getIsMouseOverSlot(guiContainer, slot, getMouseX(guiContainer), getMouseY(guiContainer));
    }

    @SideOnly(Side.CLIENT)
    private static boolean getIsMouseOverSlot(GuiContainer guiContainer, Slot slot, int x, int y) {
        // Copied from GuiContainer
        if(guiContainer != null) {
            x -= guiContainer.guiLeft;
            y -= guiContainer.guiTop;
            return x >= slot.xDisplayPosition - 1 && x < slot.xDisplayPosition + 16 + 1 && y >= slot.yDisplayPosition - 1 && y < slot.yDisplayPosition + 16 + 1;
        } else {
            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    private static int getMouseX(GuiContainer guiContainer) {
        return (Mouse.getEventX() * guiContainer.width) / getDisplayWidth();
    }

    @SideOnly(Side.CLIENT)
    private static int getMouseY(GuiContainer guiContainer) {
        return guiContainer.height -
                (Mouse.getEventY() * guiContainer.height) / getDisplayHeight() - 1;
    }

    public static int getSpecialChestRowSize(Container container) {
        // This method gets replaced by the transformer with "return container.invtweaks$rowSize()"
        return 0;
    }

    public boolean hasTexture(ResourceLocation texture) {
        try {
            mc.getResourceManager().getResource(texture);
        } catch(/*IOException*/Exception e) { //FIXME: Java is stupid, the exception annotations just aren't being generated correctly at the moment.
            return false;
        }
        return true;
    }

    // Static access
    public static String getCurrentLanguage() {
        return Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
    }

    // Classes

    public static boolean isValidChest(Container container) {
        // This method gets replaced by the transformer with "return container.invtweaks$validChest()"
        return false;
    }

    public static boolean isLargeChest(Container container) {
        // This method gets replaced by the transformer with "return container.invtweaks$largeChest()"
        return false;
    }

    public static boolean isValidInventory(Container container) {
        // This method gets replaced by the transformer with "return container.invtweaks$validInventory()"
        return false;
    }

    public static boolean showButtons(Container container) {
        // This method gets replaced by the transformer with "return container.invtweaks$showButtons()"
        return false;
    }

    public static Map<ContainerSection, List<Slot>> getContainerSlotMap(Container container) {
        // This method gets replaced by the transformer with "return container.invtweaks$slotMap()"
        return null;
    }

    public static boolean isGuiContainer(Object o) { // GuiContainer (abstract class)
        return o != null && o instanceof GuiContainer;
    }

    public static boolean isGuiInventoryCreative(Object o) { // GuiInventoryCreative
        return o != null && o.getClass().equals(GuiContainerCreative.class);
    }

    public static boolean isGuiEnchantmentTable(Object o) { // GuiEnchantmentTable
        return o != null && o.getClass().equals(GuiEnchantment.class);
    }

    public static boolean isGuiInventory(Object o) { // GuiInventory
        return o != null && o.getClass().equals(GuiInventory.class);
    }

    public static boolean isGuiButton(Object o) { // GuiButton
        return o != null && o instanceof GuiButton;
    }

    public static boolean isGuiEditSign(Object o) {
        return o != null && o.getClass().equals(GuiEditSign.class);
    }

    public static boolean isItemArmor(Object o) { // ItemArmor
        return o != null && o instanceof ItemArmor;
    }

    public static boolean isBasicSlot(Object o) { // Slot
        // TODO: SpecialSource, class ATs, cannot compile
        return o != null && (o.getClass()
                              .equals(Slot.class)/* || o.getClass().equals(GuiContainerCreative.CreativeSlot.class)*/);
    }

}
