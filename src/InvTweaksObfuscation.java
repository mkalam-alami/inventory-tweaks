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
    
    protected Minecraft mc;

    protected InvTweaksModCompatibility mods;

    private static Map<String, Field> fieldsMap = new HashMap<String, Field>();
    
    static {
        // CreativeSlot.underlyingSlot
        makeFieldPublic(auf.class, "b");
        // RenderEngine.texturePack
        makeFieldPublic(azc.class, "k");
    }
    
	public InvTweaksObfuscation(Minecraft mc) {
		this.mc = mc;
		this.mods = new InvTweaksModCompatibility(this);
	}
	
	// Minecraft members

	protected void addChatMessage(String message) {
	    if (mc.v != null) {
	        mc.v.b().a(message);
	    }
	}
    protected axc getThePlayer() { // EntityPlayer
        return mc.g;
    }
    protected awz getTheWorld() { // World
        return mc.e;
    }
	protected awy getPlayerController() { // PlayerController
		return mc.b;
	}
	protected asw getCurrentScreen() { // GuiScreen
		return mc.r;
	}
	protected asb getFontRenderer() { // FontRenderer
        return mc.p;
    }
    protected void displayGuiScreen(asw guiScreen) {
        mc.a(guiScreen);
    }
    protected int getDisplayWidth() {
        return mc.c;
    }
    protected int getDisplayHeight() {
        return mc.d;
    }
    protected ard getGameSettings() {
        return mc.y;
    }
    public aqi[] getRegisteredBindings() {
        return getGameSettings().O;
    }
    public void setRegisteredBindings(aqi[] bindings) {
        getGameSettings().O = bindings;
    }
    protected int getKeyBindingForwardKeyCode() {
        return getKeyCode(getGameSettings().A);
    }
    protected int getKeyBindingBackKeyCode() {
        return getKeyCode(getGameSettings().C);
    }

	// EntityPlayer members

	protected qf getInventoryPlayer() { // InventoryPlayer
		return getThePlayer().bK;
	}
	protected tv getCurrentEquippedItem() { // ItemStack
		return getThePlayer().bP();
	}
	protected qy getCraftingInventory() { // Container
		return getThePlayer().bL;
	}
    protected qy getPlayerContainer() { // ContainerPlayer
        return (qy) getThePlayer().bL; // MCP name: inventorySlots
    }

	// InventoryPlayer members
	
	protected tv[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	protected void setMainInventory(tv[] value) {
		getInventoryPlayer().a = value;
	}
	protected void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	protected void setHeldStack(tv stack) {
		getInventoryPlayer().b(stack); // setItemStack
	}
	protected boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	protected tv getHeldStack() {
		return getInventoryPlayer().n(); // getItemStack
	}
	protected tv getFocusedStack() {
		return getInventoryPlayer().g(); // getCurrentItem
	}
	protected int getFocusedSlot() {
		return getInventoryPlayer().c; // currentItem
	}
	
    // GuiScreen members

	protected int getWindowWidth(asw guiScreen) {
	    return guiScreen.f;
	}
    protected int getWindowHeight(asw guiScreen) {
        return guiScreen.g;
    }
    protected int getGuiX(atq guiContainer) { // GuiContainer
        return guiContainer.m;
    }
    protected int getGuiY(atq guiContainer) {
        return guiContainer.n;
    }
    protected int getGuiWidth(atq guiContainer) { // GuiContainer
        return guiContainer.b;
    }
    protected int getGuiHeight(atq guiContainer) {
        return guiContainer.c;
    }
    @SuppressWarnings("unchecked")
	protected List<Object> getControlList(asw guiScreen) {
        return guiScreen.h;
    }
    protected void setControlList(asw guiScreen, List<?> controlList) {
        guiScreen.h = controlList;
    }
    protected atq asGuiContainer(asw guiScreen) {
        return (atq) guiScreen;
    }

    // FontRenderer members
	
	protected int getStringWidth(asb fontRenderer, String line) {
	    return fontRenderer.a(line);
	}
	protected void drawStringWithShadow(asb fontRenderer,
            String s, int i, int j, int k) {
        fontRenderer.a(s, i, j, k);
    }
	
	// ItemStack members

	protected tv createItemStack(int id, int size, int damage) {
		return new tv(id, size, damage);
	}
	protected tv copy(tv itemStack) {
		return itemStack.l();
	}
	protected int getItemDamage(tv itemStack) {
		return itemStack.j();
	}
	protected int getMaxStackSize(tv itemStack) {
		return itemStack.d();
	}
	protected boolean hasDataTags(tv itemStack) {
	  return itemStack.o();
	}
	protected int getStackSize(tv itemStack) {
		return itemStack.a;
	}
	protected int getItemID(tv itemStack) {
		return itemStack.c;
	}
	protected boolean areItemStacksEqual(tv itemStack1, tv itemStack2) {
		return itemStack1.a(itemStack2) && getStackSize(itemStack1) == getStackSize(itemStack2);
	}
    protected boolean isItemStackDamageable(tv itemStack) {
        return itemStack.f();
    }
    protected boolean areSameItemType(tv itemStack1, tv itemStack2) {
        return areItemsEqual(itemStack1, itemStack2) ||
                (isItemStackDamageable(itemStack1)
                        && getItemID(itemStack1) == getItemID(itemStack2));
    }
    protected boolean areItemsEqual(tv itemStack1, tv itemStack2) {
        return itemStack1.a(itemStack2); // isItemEqual
    }
    protected int getAnimationsToGo(tv itemStack) {
        return itemStack.b;
    }
    protected tt getItem(tv itemStack) { // Item
        return itemStack.b();
    }
    
    // Item & ItemArmor
    
    protected boolean isDamageable(tt item) {
        return item.n();
    }
    protected int getMaxDamage(tt item) {
        return item.m();
    }
    protected int getArmorLevel(sc itemArmor) { // ItemArmor
        return itemArmor.b;
    }
    protected sc asItemArmor(tt item) { // ItemArmor
        return (sc) item;
    }
	
	// PlayerController members

	protected tv clickInventory(awy playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, axc entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				(shiftHold) ? 1 : 0 /* XXX Placeholder */, entityPlayer);
	}
	
	// Container members

	protected int getWindowId(qy container) {
		return container.c;
	}
	protected List<?> getSlots(qy container) {
		return container.b;
	}
    protected rz getSlot(qy container, int i) { // Slot
        return (rz) (getSlots(container).get(i));
    }

    protected tv getSlotStack(qy container, int i) {
        rz slot = getSlot(container, i);
        return (slot == null) ? null : getStack(slot); // getStack
    }

    protected void setSlotStack(qy container, int i, tv stack) {
        container.a(i, stack); // putStackInSlot
    }

    // Slot members
    
    protected boolean hasStack(rz slot) { 
        return slot.d();
    }
    protected int getSlotNumber(rz slot) {
        try {
            // Creative slots don't set the "g" property, serve as a proxy for true slots
            if (slot instanceof auf) {
            	rz underlyingSlot = (rz) getThroughReflection(auf.class, "b", slot);
                if (underlyingSlot != null) {
                    return underlyingSlot.g;
                }
            }
        } catch (Exception e) {
            log.warning("Failed to access creative slot number");
        }
        return slot.g;
    }
    protected tv getStack(rz slot) {
        return slot.c();
    }
    protected int getXDisplayPosition(rz slot) {
        return slot.h;
    }
    protected int getYDisplayPosition(rz slot) {
        return slot.i;
    }
    protected boolean areSlotAndStackCompatible(rz slot, tv itemStack) {
        return slot.a(itemStack); // isItemValid
    }

    // GuiContainer members

    protected qy getContainer(atq guiContainer) {
        return guiContainer.d; /* inventorySlots */
    }

    // GuiButton

    protected arl asGuiButton(Object o) {
        return (arl) o;
    }
    protected void setEnabled(arl guiButton, boolean enabled) { // GuiButton
        guiButton.g = enabled;
    }
    protected int getId(arl guiButton) { // GuiButton
        return guiButton.f;
    }
    protected void setDisplayString(arl guiButton, String string) {
        guiButton.e = string;
    }
    protected String getDisplayString(arl guiButton) {
        return guiButton.e;
    }
    
    // Other

    protected void playSound(String string, float f, float g) {
        mc.A.a(string, f, g);
    }
    protected long getCurrentTime() {
        return getTheWorld().E();
    }
    protected int getKeyCode(aqi w) { // KeyBinding
        return w.d;
    }
    protected int getSpecialChestRowSize(atq guiContainer, int defaultValue) {
    	return mods.getSpecialChestRowSize(guiContainer, defaultValue);
    }
    protected boolean hasTexture(String texture) {
        bcp texturePacksManager = (bcp) getThroughReflection(azc.class, "k", mc.o);
        return texturePacksManager != null && texturePacksManager.e().a(texture) != null;
    }

    // Static access

    public static be getLocalizationService() { // StringTranslate
        return be.a(); // StringTranslate.getInstance()
    }
    public static String getCurrentLanguage() {
        return getLocalizationService().c();
    }
    public static String getLocalizedString(String key) {
        return getLocalizationService().b(key);
    }
    public static tv getHoldStackStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getHeldStack();
    }
    public static asw getCurrentScreenStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getCurrentScreen();
    }
    
	// Classes
    
    protected boolean isValidChest(asw guiScreen) {
        return guiScreen != null && (isGuiChest(guiScreen)
        		|| isGuiDispenser(guiScreen)
        		|| mods.isSpecialChest(guiScreen));
    }
	protected boolean isValidInventory(asw guiScreen) {
        return isStandardInventory(guiScreen)
        		|| mods.isSpecialInventory(guiScreen);
    }
	protected boolean isStandardInventory(asw guiScreen) {
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

	// ================  TODO  ================
	
    protected boolean isGuiContainer(Object o) { // GuiContainer (abstract class)
        return o != null && o instanceof atq;
    }
    protected boolean isGuiBeacon(Object o) { // GuiBeacon
        return o != null && o.getClass().equals(atr.class);
    }
    protected boolean isGuiBrewingStand(Object o) { // GuiBrewingStand
        return o != null && o.getClass().equals(aty.class);
    }
    protected boolean isGuiChest(Object o) { // GuiChest
        return o != null && o.getClass().equals(aua.class);
    }
    protected boolean isGuiWorkbench(Object o) { // GuiWorkbench
        return o != null && o.getClass().equals(aub.class);
    }
    protected boolean isGuiInventoryCreative(Object o) { // GuiInventoryCreative
        return o != null && o.getClass().equals(aud.class);
    }
    protected boolean isGuiEnchantmentTable(Object o) { // GuiEnchantmentTable
        return o != null && o.getClass().equals(aui.class);
    }
    protected boolean isGuiFurnace(Object o) { // GuiFurnace
        return o != null && o.getClass().equals(auj.class);
    }
    protected boolean isGuiInventory(Object o) { // GuiInventory
        return o != null && o.getClass().equals(auk.class);
    }
    protected boolean isGuiTrading(Object o) { // GuiTrading
        return o != null && o.getClass().equals(aul.class);
    }
    protected boolean isGuiAnvil(Object o) { // GuiAnvil
        return o != null && o.getClass().equals(aun.class);
    }
    protected boolean isGuiDispenser(Object o) { // GuiDispenser
        return o != null && o.getClass().equals(aup.class);
    }
    
    protected boolean isGuiButton(Object o) { // GuiButton
        return o != null && o instanceof arl;
    }
    protected boolean isGuiEditSign(Object o) { // GuiEditSign
        return o != null && o.getClass().equals(auo.class);
    }

    protected boolean isContainerBrewingStand(Object o) { // ContainerBrewingStand
        return o != null && o.getClass().equals(rb.class);
    }
    protected boolean isContainerChest(Object o) { // ContainerChest
        return o != null && o.getClass().equals(rf.class);
    }
    protected boolean isContainerWorkbench(Object o) { // ContainerWorkbench
        return o != null && o.getClass().equals(rh.class);
    }
    protected boolean isContainerEnchantmentTable(Object o) { // ContainerEnchantmentTable
        return o != null && o.getClass().equals(ri.class);
    }
    protected boolean isContainerFurnace(Object o) { // ContainerFurnace 
        return o != null && o.getClass().equals(rl.class);
    }
	protected boolean isContainerPlayer(Object o) { // ContainerPlayer
	    return o != null && o.getClass().equals(rn.class);
	}
    protected boolean isContainerTrading(Object o) { // ContainerTrading
        return o != null && o.getClass().equals(rr.class);
    }
    protected boolean isContainerAnvil(Object o) { // ContainerAnvil
        return o != null && o.getClass().equals(ru.class);
    }
    protected boolean isContainerDispenser(Object o) { // ContainerDispenser
        return o != null && o.getClass().equals(sa.class);
    }
    protected boolean isContainerCreative(Object o) { // ContainerCreative
        return o != null && o.getClass().equals(aue.class);
    }

    protected boolean isItemArmor(Object o) { // ItemArmor
        return o != null && o instanceof sc;
    }

    protected boolean isBasicSlot(Object o) { // Slot
        return o != null && o.getClass().equals(rz.class);
    }
    
    // Reflection utils
    
    protected static void makeFieldPublic(Class<?> c, String field) {
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
    protected static Object getThroughReflection(Class<?> c, String field, Object instance) {
        try {
            return fieldsMap.get(c.getName() + field).get(instance);
        } catch (Exception e) {
            return null;
        }
    }

}