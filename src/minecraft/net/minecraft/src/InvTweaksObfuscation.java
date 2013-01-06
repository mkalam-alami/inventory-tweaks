package net.minecraft.src;

import invtweaks.InvTweaksModCompatibility;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

/**
 * Minecraft 1.3 Obfuscation layer
 * 
 * @author Jimeo Wan
 *
 */
public class InvTweaksObfuscation {

    private static final Logger log = Logger.getLogger("InvTweaks");
    
    public Minecraft mc;

    public InvTweaksModCompatibility mods;

    private static Map<String, Field> fieldsMap = new HashMap<String, Field>();
    
    static {
        // CreativeSlot.underlyingSlot
        makeFieldPublic(SlotCreativeInventory.class, "theSlot");
        // RenderEngine.texturePack
        makeFieldPublic(RenderEngine.class, "texturePack");
    }
    
	public InvTweaksObfuscation(Minecraft mc) {
		this.mc = mc;
		this.mods = new InvTweaksModCompatibility(this);
	}
	
	// Minecraft members

	public void addChatMessage(String message) {
	    if (mc.ingameGUI != null) {
	        mc.ingameGUI.getChatGUI().printChatMessage(message);
	    }
	}
    public EntityPlayer getThePlayer() {
        return mc.thePlayer;
    }
    public World getTheWorld() {
        return mc.theWorld;
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
    public int getDisplayWidth() {
        return mc.displayWidth;
    }
    public int getDisplayHeight() {
        return mc.displayHeight;
    }
    public GameSettings getGameSettings() {
        return mc.gameSettings;
    }
    public KeyBinding[] getRegisteredBindings() {
        return getGameSettings().keyBindings;
    }
    public void setRegisteredBindings(KeyBinding[] bindings) {
        getGameSettings().keyBindings = bindings;
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

	public int getWindowWidth(GuiScreen guiScreen) {
	    return guiScreen.width;
	}
    public int getWindowHeight(GuiScreen guiScreen) {
        return guiScreen.height;
    }
    public int getGuiX(GuiContainer guiContainer) {
        return guiContainer.guiLeft;
    }
    public int getGuiY(GuiContainer guiContainer) {
        return guiContainer.guiTop;
    }
    public int getGuiWidth(GuiContainer guiContainer) {
        return guiContainer.width;
    }
    public int getGuiHeight(GuiContainer guiContainer) {
        return guiContainer.height;
    }
    @SuppressWarnings("unchecked")
	public List<Object> getControlList(GuiScreen guiScreen) {
        return guiScreen.controlList;
    }
    public void setControlList(GuiScreen guiScreen, List<?> controlList) {
        guiScreen.controlList = controlList;
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
	public boolean hasDataTags(ItemStack itemStack) {
	  return itemStack.hasTagCompound();
	}
	public int getStackSize(ItemStack itemStack) {
		return itemStack.stackSize;
	}
	public int getItemID(ItemStack itemStack) {
		return itemStack.itemID;
	}
	public boolean areItemStacksEqual(ItemStack itemStack1, ItemStack itemStack2) {
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

	public ItemStack clickInventory(PlayerControllerMP playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, EntityPlayer entityPlayer) {
		return playerController.windowClick(windowId, slot, clickButton,
				(shiftHold) ? 1 : 0 /* XXX Placeholder */, entityPlayer);
	}
	
	// Container members

	public int getWindowId(Container container) {
		return container.windowId;
	}
	public List<?> getSlots(Container container) {
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
    public int getSlotNumber(Slot slot) {
        try {
            // Creative slots don't set the "slotNumber" property, serve as a proxy for true slots
            if (slot instanceof SlotCreativeInventory) {
            	Slot underlyingSlot = (Slot) getThroughReflection(SlotCreativeInventory.class, "theSlot", slot);
                if (underlyingSlot != null) {
                    return underlyingSlot.slotNumber;
                }
            }
        } catch (Exception e) {
            log.warning("Failed to access creative slot nItemStackber");
        }
        return slot.slotNumber;
    }
    public ItemStack getStack(Slot slot) {
        return slot.getStack();
    }
    public int getXDisplayPosition(Slot slot) {
        return slot.xDisplayPosition;
    }
    public int getYDisplayPosition(Slot slot) {
        return slot.yDisplayPosition;
    }
    public boolean areSlotAndStackCompatible(Slot slot, ItemStack itemStack) {
        return slot.isItemValid(itemStack); // isItemValid
    }

    // GuiContainer members

    public Container getContainer(GuiContainer guiContainer) {
        return guiContainer.inventorySlots;
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
    public long getCurrentTime() {
        return getTheWorld().getTotalWorldTime();
    }
    public int getKeyCode(KeyBinding b) {
        return b.keyCode;
    }
    public int getSpecialChestRowSize(GuiContainer guiContainer, int defaultValue) {
    	return mods.getSpecialChestRowSize(guiContainer, defaultValue);
    }
    public boolean hasTexture(String texture) {
    	TexturePackList texturePacksManager = (TexturePackList) getThroughReflection(RenderEngine.class, "texturePack", mc.renderEngine);
    	InputStream resourceAsStream = null;
    	try {
	        resourceAsStream = texturePacksManager.getSelectedTexturePack().getResourceAsStream(texture);
			return texturePacksManager != null && resourceAsStream != null;
    	}
    	finally {
    		if (resourceAsStream != null) {
    			try {
					resourceAsStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	}
    }

    // Static access

    public static StringTranslate getLocalizationService() { // StringTranslate
        return StringTranslate.getInstance(); // StringTranslate.getInstance()
    }
    public static String getCurrentLanguage() {
        return getLocalizationService().getCurrentLanguage();
    }
    public static String getLocalizedString(String key) {
        return getLocalizationService().translateKey(key);
    }
    public static ItemStack getHoldStackStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getHeldStack();
    }
    public static GuiScreen getCurrentScreenStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getCurrentScreen();
    }
    
	// Classes
    
    public boolean isValidChest(GuiScreen guiScreen) {
        return guiScreen != null && (isGuiChest(guiScreen)
        		|| isGuiDispenser(guiScreen)
        		|| mods.isSpecialChest(guiScreen))
                && (guiScreen instanceof GuiContainer);
    }
	public boolean isValidInventory(GuiScreen guiScreen) {
        return isStandardInventory(guiScreen)
        		|| mods.isSpecialInventory(guiScreen);
    }
	public boolean isStandardInventory(GuiScreen guiScreen) {
        return isGuiInventory(guiScreen)
        		|| isGuiWorkbench(guiScreen)
        		|| isGuiFurnace(guiScreen)
                || isGuiBrewingStand(guiScreen)
                || isGuiEnchantmentTable(guiScreen)
                || isGuiTrading(guiScreen)
                || isGuiAnvil(guiScreen)
                || isGuiBeacon(guiScreen)
                || (isGuiInventoryCreative(guiScreen) 
                        && getSlots(getContainer(asGuiContainer(guiScreen))).size() == 46);
    }

    public boolean isGuiContainer(Object o) { // GuiContainer (abstract class)
        return o != null && o instanceof GuiContainer;
    }
	
    public boolean isGuiBeacon(Object o) { // GuiBeacon
        return o != null && o.getClass().equals(GuiBeacon.class);
    }
    public boolean isGuiBrewingStand(Object o) { // GuiBrewingStand
        return o != null && o.getClass().equals(GuiBrewingStand.class);
    }
    public boolean isGuiChest(Object o) { // GuiChest
        return o != null && o.getClass().equals(GuiChest.class);
    }
    public boolean isGuiWorkbench(Object o) { // GuiWorkbench
        return o != null && o.getClass().equals(GuiCrafting.class);
    }
    public boolean isGuiInventoryCreative(Object o) { // GuiInventoryCreative
        return o != null && o.getClass().equals(GuiContainerCreative.class);
    }
    public boolean isGuiEnchantmentTable(Object o) { // GuiEnchantmentTable
        return o != null && o.getClass().equals(GuiEnchantment.class);
    }
    public boolean isGuiFurnace(Object o) { // GuiFurnace
        return o != null && o.getClass().equals(GuiFurnace.class);
    }
    public boolean isGuiInventory(Object o) { // GuiInventory
        return o != null && o.getClass().equals(GuiInventory.class);
    }
    public boolean isGuiTrading(Object o) { // GuiTrading
        return o != null && o.getClass().equals(GuiMerchant.class);
    }
    public boolean isGuiAnvil(Object o) { // GuiAnvil
        return o != null && o.getClass().equals(GuiRepair.class);
    }
    public boolean isGuiDispenser(Object o) { // GuiDispenser
        return o != null && o.getClass().equals(GuiDispenser.class);
    }
    
    public boolean isGuiButton(Object o) { // GuiButton
        return o != null && o instanceof GuiButton;
    }
    
    public boolean isGuiEditSign(Object o) {
        return o != null && o.getClass().equals(GuiEditSign.class);
    }
    public boolean isContainerBeacon(Object o) {
        return o != null && o.getClass().equals(ContainerBeacon.class);
    }
    public boolean isContainerBrewingStand(Object o) {
        return o != null && o.getClass().equals(ContainerBrewingStand.class);
    }
    public boolean isContainerChest(Object o) {
        return o != null && o.getClass().equals(ContainerChest.class);
    }
    public boolean isContainerWorkbench(Object o) {
        return o != null && o.getClass().equals(ContainerWorkbench.class);
    }
    public boolean isContainerEnchantmentTable(Object o) {
        return o != null && o.getClass().equals(ContainerEnchantment.class);
    }
    public boolean isContainerFurnace(Object o) { 
        return o != null && o.getClass().equals(ContainerFurnace.class);
    }
	public boolean isContainerPlayer(Object o) {
	    return o != null && o.getClass().equals(ContainerPlayer.class);
	}
    public boolean isContainerTrading(Object o) {
        return o != null && o.getClass().equals(ContainerMerchant.class);
    }
    public boolean isContainerAnvil(Object o) {
        return o != null && o.getClass().equals(ContainerRepair.class);
    }
    public boolean isContainerDispenser(Object o) {
        return o != null && o.getClass().equals(ContainerDispenser.class);
    }
    public boolean isContainerCreative(Object o) { // ContainerCreative
        return o != null && o.getClass().equals(ContainerCreative.class);
    }

    public boolean isItemArmor(Object o) { // ItemArmor
        return o != null && o instanceof ItemArmor;
    }

    public boolean isBasicSlot(Object o) { // Slot
        return o != null && o.getClass().equals(Slot.class);
    }
    
    // Reflection utils
    
    public static void makeFieldPublic(Class<?> c, String field) {
        try {
            Field f = c.getDeclaredField(field);
            f.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, Modifier.PUBLIC);
            fieldsMap.put(c.getName() + field, f);
        }
        catch (Exception e) {
            log.severe("Failed to make " + c.getName() + "." + field + " accessible: " +  e.getMessage());
        }
    }
   
    /**
     * Access value from any field, even private.
     * Field must be made public through the makeFieldPublic() function first.
     * @return
     */
    public static Object getThroughReflection(Class<?> c, String field, Object instance) {
        try {
            return fieldsMap.get(c.getName() + field).get(instance);
        } catch (Exception e) {
            return null;
        }
    }

}