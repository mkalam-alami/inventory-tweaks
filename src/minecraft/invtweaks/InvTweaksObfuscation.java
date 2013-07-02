package invtweaks;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import invtweaks.api.container.ContainerSection;
import invtweaks.forge.InvTweaksMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringTranslate;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
            mc.ingameGUI.getChatGUI().printChatMessage(message);
        }
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
        return getKeyCode(getGameSettings().keyBindForward);
    }

    public int getKeyBindingBackKeyCode() {
        return getKeyCode(getGameSettings().keyBindBack);
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

    // GuiScreen members

    public static int getWindowWidth(GuiScreen guiScreen) {
        return guiScreen.width;
    }

    public static int getWindowHeight(GuiScreen guiScreen) {
        return guiScreen.height;
    }

    public static int getGuiX(GuiContainer guiContainer) {
        return guiContainer.guiLeft;
    }

    public static int getGuiY(GuiContainer guiContainer) {
        return guiContainer.guiTop;
    }

    public static int getGuiWidth(GuiContainer guiContainer) {
        return guiContainer.xSize;
    }

    public static int getGuiHeight(GuiContainer guiContainer) {
        return guiContainer.ySize;
    }

    @SuppressWarnings("unchecked")
    public List<Object> getControlList(GuiScreen guiScreen) {
        return guiScreen.buttonList;
    }

    public void setControlList(GuiScreen guiScreen, List<?> controlList) {
        guiScreen.buttonList = controlList;
    }

    public GuiContainer asGuiContainer(GuiScreen guiScreen) {
        return (GuiContainer) guiScreen;
    }

    // FontRenderer members

    public int getStringWidth(FontRenderer fontRenderer, String line) {
        return fontRenderer.getStringWidth(line);
    }

    public void drawStringWithShadow(FontRenderer fontRenderer,
                                     String s, int i, int j, int k) {
        fontRenderer.drawStringWithShadow(s, i, j, k);
    }

    // ItemStack members

    public static ItemStack copy(ItemStack itemStack) {
        return itemStack.copy();
    }

    public static int getItemDamage(ItemStack itemStack) {
        return itemStack.getItemDamage();
    }

    public static int getMaxStackSize(ItemStack itemStack) {
        return itemStack.getMaxStackSize();
    }

    public static boolean hasDataTags(ItemStack itemStack) {
        return itemStack.hasTagCompound();
    }

    public static int getStackSize(ItemStack itemStack) {
        return itemStack.stackSize;
    }

    public static int getItemID(ItemStack itemStack) {
        return itemStack.itemID;
    }

    public static boolean areItemStacksEqual(ItemStack itemStack1, ItemStack itemStack2) {
        return itemStack1.isItemEqual(itemStack2) && getStackSize(itemStack1) == getStackSize(itemStack2);
    }

    public boolean isItemStackDamageable(ItemStack itemStack) {
        return itemStack.isItemStackDamageable();
    }

    public boolean areSameItemType(ItemStack itemStack1, ItemStack itemStack2) {
        return areItemsEqual(itemStack1, itemStack2) ||
                (isItemStackDamageable(itemStack1)
                        && getItemID(itemStack1) == getItemID(itemStack2));
    }

    public boolean areItemsEqual(ItemStack itemStack1, ItemStack itemStack2) {
        return itemStack1.isItemEqual(itemStack2);
    }

    public boolean areItemsStackable(ItemStack itemStack1, ItemStack itemStack2) {
        return itemStack1 != null && itemStack2 != null && itemStack1.isItemEqual(itemStack2) &&
                itemStack1.isStackable() &&
                (!itemStack1.getHasSubtypes() || itemStack1.getItemDamage() == itemStack2.getItemDamage()) &&
                ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);
    }


    public int getAnimationsToGo(ItemStack itemStack) {
        return itemStack.animationsToGo;
    }

    public Item getItem(ItemStack itemStack) { // Item
        return itemStack.getItem();
    }

    // Item & ItemArmor

    public boolean isDamageable(Item item) {
        return item.isDamageable();
    }

    public int getMaxDamage(Item item) {
        return item.getMaxDamage();
    }

    public int getArmorLevel(ItemArmor itemArmor) { // ItemArmor
        return itemArmor.damageReduceAmount;
    }

    public ItemArmor asItemArmor(Item item) { // ItemArmor
        return (ItemArmor) item;
    }

    // PlayerController members

    public void clickInventory(PlayerControllerMP playerController,
                               int windowId, int slot, int data,
                               int action, EntityPlayer entityPlayer) {
        InvTweaksMod.proxy.slotClick(playerController, windowId, slot, data, action, entityPlayer);
    }

    // Container members

    public int getWindowId(Container container) {
        return container.windowId;
    }

    public static List<?> getSlots(Container container) {
        return container.inventorySlots;
    }

    public Slot getSlot(Container container, int i) { // Slot
        return (Slot) (getSlots(container).get(i));
    }

    public ItemStack getSlotStack(Container container, int i) {
        Slot slot = getSlot(container, i);
        return (slot == null) ? null : getStack(slot); // getStack
    }

    public void setSlotStack(Container container, int i, ItemStack stack) {
        container.putStackInSlot(i, stack); // putStackInSlot
    }

    // Slot members

    public boolean hasStack(Slot slot) {
        return slot.getHasStack();
    }

    @SuppressWarnings("unchecked")
    public int getSlotNumber(Slot slot) {
        try {
            // Creative slots don't set the "slotNumber" property, serve as a proxy for true slots
            if(slot instanceof SlotCreativeInventory) {
                Slot underlyingSlot = SlotCreativeInventory.func_75240_a((SlotCreativeInventory) slot);
                if(underlyingSlot != null) {
                    return underlyingSlot.slotNumber;
                } else {
                    log.warning("Creative inventory: Failed to get real slot");
                }
            }
        } catch(Exception e) {
            log.warning("Failed to access creative slot number");
        }
        return slot.slotNumber;
    }

    public ItemStack getStack(Slot slot) {
        return slot.getStack();
    }

    public static int getXDisplayPosition(Slot slot) {
        return slot.xDisplayPosition;
    }

    public static int getYDisplayPosition(Slot slot) {
        return slot.yDisplayPosition;
    }

    public boolean areSlotAndStackCompatible(Slot slot, ItemStack itemStack) {
        return slot.isItemValid(itemStack); // isItemValid
    }

    // GuiContainer members

    public Container getContainer(GuiContainer guiContainer) {
        return guiContainer.inventorySlots;
    }

    @SideOnly(Side.CLIENT)
    public static Slot getSlotAtMousePosition(GuiContainer guiContainer) {
        // Copied from GuiContainer
        if(guiContainer != null) {
            Container container = guiContainer.inventorySlots;

            int x = getMouseX(guiContainer);
            int y = getMouseY(guiContainer);
            for(int k = 0; k < getSlots(container).size(); k++) {
                Slot slot = (Slot) getSlots(container).get(k);
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
            x -= getGuiX(guiContainer);
            y -= getGuiY(guiContainer);
            return x >= getXDisplayPosition(slot) - 1
                    && x < getXDisplayPosition(slot) + 16 + 1
                    && y >= getYDisplayPosition(slot) - 1
                    && y < getYDisplayPosition(slot) + 16 + 1;
        } else {
            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    private static int getMouseX(GuiContainer guiContainer) {
        return (Mouse.getEventX() * getWindowWidth(guiContainer)) / getDisplayWidth();
    }

    @SideOnly(Side.CLIENT)
    private static int getMouseY(GuiContainer guiContainer) {
        return getWindowHeight(guiContainer) -
                (Mouse.getEventY() * getWindowHeight(guiContainer)) / getDisplayHeight() - 1;
    }
    // GuiButton

    public GuiButton asGuiButton(Object o) {
        return (GuiButton) o;
    }

    public void setEnabled(GuiButton guiButton, boolean enabled) { // GuiButton
        guiButton.enabled = enabled;
    }

    public int getId(GuiButton guiButton) { // GuiButton
        return guiButton.id;
    }

    public void setDisplayString(GuiButton guiButton, String string) {
        guiButton.displayString = string;
    }

    public String getDisplayString(GuiButton guiButton) {
        return guiButton.displayString;
    }

    // Other

    public void playSound(String string, float f, float g) {
        mc.sndManager.playSoundFX(string, f, g);
    }

    public int getKeyCode(KeyBinding b) {
        return b.keyCode;
    }

    public static int getSpecialChestRowSize(Container container) {
        // This method gets replaced by the transformer with "return container.invtweaks$rowSize()"
        return 0;
    }

    public boolean hasTexture(String texture) {
        return false;
        // TODO: Figure out if there's even a possible way to find out if a texture is sucessfully loaded with resource packs.
        /*
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = mc.renderEngine.texturePack.getSelectedTexturePack().getResourceAsStream(texture);
            return resourceAsStream != null;
        } catch(IOException e) {
            return false;
        } finally {
            if(resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
        */
    }

    // Static access

    public static StringTranslate getLocalizationService() { // StringTranslate
        return StringTranslate.getInstance();
    }

    public static String getCurrentLanguage() {
        return Minecraft.getMinecraft().func_135016_M().func_135041_c().func_135034_a();
    }

    public static String getLocalizedString(String key) {
        return getLocalizationService().translateKey(key);
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

    public static boolean isStandardInventory(Container container) {
        // This method gets replaced by the transformer with "return container.invtweaks$standardInventory()"
        return false;
    }

    public static Map<ContainerSection, List<Slot>> getContainerSlotMap(Container container) {
        // This method gets replaced by the transformer with "return container.invtweaks$slotMap()"
        return null;
    }

    public boolean isGuiContainer(Object o) { // GuiContainer (abstract class)
        return o != null && o instanceof GuiContainer;
    }

    public boolean isGuiInventoryCreative(Object o) { // GuiInventoryCreative
        return o != null && o.getClass().equals(GuiContainerCreative.class);
    }

    public boolean isGuiEnchantmentTable(Object o) { // GuiEnchantmentTable
        return o != null && o.getClass().equals(GuiEnchantment.class);
    }

    public boolean isGuiInventory(Object o) { // GuiInventory
        return o != null && o.getClass().equals(GuiInventory.class);
    }

    public boolean isGuiButton(Object o) { // GuiButton
        return o != null && o instanceof GuiButton;
    }

    public boolean isGuiEditSign(Object o) {
        return o != null && o.getClass().equals(GuiEditSign.class);
    }

    public boolean isItemArmor(Object o) { // ItemArmor
        return o != null && o instanceof ItemArmor;
    }

    public boolean isBasicSlot(Object o) { // Slot
        return o != null && (o.getClass().equals(Slot.class) || o.getClass().equals(SlotCreativeInventory.class));
    }

}
