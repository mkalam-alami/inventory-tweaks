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
        makeFieldPublic(aue.class, "b");
        // RenderEngine.texturePack
        makeFieldPublic(azb.class, "k");
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
    protected axb getThePlayer() { // EntityPlayer
        return mc.g;
    }
    protected awy getTheWorld() { // World
        return mc.e;
    }
	protected awx getPlayerController() { // PlayerController
		return mc.b;
	}
	protected asv getCurrentScreen() { // GuiScreen
		return mc.r;
	}
	protected asa getFontRenderer() { // FontRenderer
        return mc.p;
    }
    protected void displayGuiScreen(asv guiScreen) {
        mc.a(guiScreen);
    }
    protected int getDisplayWidth() {
        return mc.c;
    }
    protected int getDisplayHeight() {
        return mc.d;
    }
    protected arc getGameSettings() {
        return mc.y;
    }
    public aqh[] getRegisteredBindings() {
        return getGameSettings().O;
    }
    public void setRegisteredBindings(aqh[] bindings) {
        getGameSettings().O = bindings;
    }
    protected int getKeyBindingForwardKeyCode() {
        return getKeyCode(getGameSettings().A);
    }
    protected int getKeyBindingBackKeyCode() {
        return getKeyCode(getGameSettings().C);
    }

	// EntityPlayer members

	protected qe getInventoryPlayer() { // InventoryPlayer
		return getThePlayer().bJ;
	}
	protected tu getCurrentEquippedItem() { // ItemStack
		return getThePlayer().bP();
	}
	protected qx getCraftingInventory() { // Container
		return getThePlayer().bL;
	}
    protected qx getPlayerContainer() { // ContainerPlayer
        return (qx) getThePlayer().bK; // MCP name: inventorySlots
    }

	// InventoryPlayer members
	
	protected tu[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	protected void setMainInventory(tu[] value) {
		getInventoryPlayer().a = value;
	}
	protected void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	protected void setHeldStack(tu stack) {
		getInventoryPlayer().b(stack); // setItemStack
	}
	protected boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	protected tu getHeldStack() {
		return getInventoryPlayer().n(); // getItemStack
	}
	protected tu getFocusedStack() {
		return getInventoryPlayer().g(); // getCurrentItem
	}
	protected int getFocusedSlot() {
		return getInventoryPlayer().c; // currentItem
	}
	
    // GuiScreen members

	protected int getWindowWidth(asv guiScreen) {
	    return guiScreen.f;
	}
    protected int getWindowHeight(asv guiScreen) {
        return guiScreen.g;
    }
    protected int getGuiX(atp guiContainer) { // GuiContainer
        return guiContainer.m;
    }
    protected int getGuiY(atp guiContainer) {
        return guiContainer.n;
    }
    protected int getGuiWidth(atp guiContainer) { // GuiContainer
        return guiContainer.b;
    }
    protected int getGuiHeight(atp guiContainer) {
        return guiContainer.c;
    }
    @SuppressWarnings("unchecked")
	protected List<Object> getControlList(asv guiScreen) {
        return guiScreen.h;
    }
    protected void setControlList(asv guiScreen, List<?> controlList) {
        guiScreen.h = controlList;
    }
    protected atp asGuiContainer(asv guiScreen) {
        return (atp) guiScreen;
    }

    // FontRenderer members
	
	protected int getStringWidth(asa fontRenderer, String line) {
	    return fontRenderer.a(line);
	}
	protected void drawStringWithShadow(asa fontRenderer,
            String s, int i, int j, int k) {
        fontRenderer.a(s, i, j, k);
    }
	
	// ItemStack members

	protected tu createItemStack(int id, int size, int damage) {
		return new tu(id, size, damage);
	}
	protected tu copy(tu itemStack) {
		return itemStack.l();
	}
	protected int getItemDamage(tu itemStack) {
		return itemStack.j();
	}
	protected int getMaxStackSize(tu itemStack) {
		return itemStack.d();
	}
	protected boolean hasDataTags(tu itemStack) {
	  return itemStack.o();
	}
	protected int getStackSize(tu itemStack) {
		return itemStack.a;
	}
	protected int getItemID(tu itemStack) {
		return itemStack.c;
	}
	protected boolean areItemStacksEqual(tu itemStack1, tu itemStack2) {
		return itemStack1.a(itemStack2) && getStackSize(itemStack1) == getStackSize(itemStack2);
	}
    protected boolean isItemStackDamageable(tu itemStack) {
        return itemStack.f();
    }
    protected boolean areSameItemType(tu itemStack1, tu itemStack2) {
        return areItemsEqual(itemStack1, itemStack2) ||
                (isItemStackDamageable(itemStack1)
                        && getItemID(itemStack1) == getItemID(itemStack2));
    }
    protected boolean areItemsEqual(tu itemStack1, tu itemStack2) {
        return itemStack1.a(itemStack2); // isItemEqual
    }
    protected int getAnimationsToGo(tu itemStack) {
        return itemStack.b;
    }
    protected ts getItem(tu itemStack) { // Item
        return itemStack.b();
    }
    
    // Item & ItemArmor
    
    protected boolean isDamageable(ts item) {
        return item.n();
    }
    protected int getMaxDamage(ts item) {
        return item.m();
    }
    protected int getArmorLevel(sb itemArmor) { // ItemArmor
        return itemArmor.b;
    }
    protected sb asItemArmor(ts item) { // ItemArmor
        return (sb) item;
    }
	
	// PlayerController members

	protected tu clickInventory(awx playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, axb entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				(shiftHold) ? 1 : 0 /* XXX Placeholder */, entityPlayer);
	}
	
	// Container members

	protected int getWindowId(qx container) {
		return container.c;
	}
	protected List<?> getSlots(qx container) {
		return container.b;
	}
    protected ry getSlot(qx container, int i) { // Slot
        return (ry) (getSlots(container).get(i));
    }

    protected tu getSlotStack(qx container, int i) {
        ry slot = getSlot(container, i);
        return (slot == null) ? null : getStack(slot); // getStack
    }

    protected void setSlotStack(qx container, int i, tu stack) {
        container.a(i, stack); // putStackInSlot
    }

    // Slot members
    
    protected boolean hasStack(ry slot) { 
        return slot.d();
    }
    protected int getSlotNumber(ry slot) {
        try {
            // Creative slots don't set the "g" property, serve as a proxy for true slots
            if (slot instanceof aue) {
                ry underlyingSlot = (ry) getThroughReflection(aue.class, "b", slot);
                if (underlyingSlot != null) {
                    return underlyingSlot.g;
                }
            }
        } catch (Exception e) {
            log.warning("Failed to access creative slot number");
        }
        return slot.g;
    }
    protected tu getStack(ry slot) {
        return slot.c();
    }
    protected int getXDisplayPosition(ry slot) {
        return slot.h;
    }
    protected int getYDisplayPosition(ry slot) {
        return slot.i;
    }
    protected boolean areSlotAndStackCompatible(ry slot, tu itemStack) {
        return slot.a(itemStack); // isItemValid
    }

    // GuiContainer members

    protected qx getContainer(atp guiContainer) {
        return guiContainer.d; /* inventorySlots */
    }

    // GuiButton

    protected ark asGuiButton(Object o) {
        return (ark) o;
    }
    protected void setEnabled(ark guiButton, boolean enabled) { // GuiButton
        guiButton.g = enabled;
    }
    protected int getId(ark guiButton) { // GuiButton
        return guiButton.f;
    }
    protected void setDisplayString(ark guiButton, String string) {
        guiButton.e = string;
    }
    protected String getDisplayString(ark guiButton) {
        return guiButton.e;
    }
    
    // Other

    protected void playSound(String string, float f, float g) {
        mc.A.a(string, f, g);
    }
    protected long getCurrentTime() {
        return getTheWorld().E();
    }
    protected int getKeyCode(aqh w) { // KeyBinding
        return w.d;
    }
    protected int getSpecialChestRowSize(atp guiContainer, int defaultValue) {
    	return mods.getSpecialChestRowSize(guiContainer, defaultValue);
    }
    protected boolean hasTexture(String texture) {
        bco texturePacksManager = (bco) getThroughReflection(azb.class, "k", mc.o);
        return texturePacksManager != null && texturePacksManager.e().a(texture) != null;
    }

    // Static access

    public static bd getLocalizationService() { // StringTranslate
        return bd.a(); // StringTranslate.getInstance()
    }
    public static String getCurrentLanguage() {
        return getLocalizationService().c();
    }
    public static String getLocalizedString(String key) {
        return getLocalizationService().b(key);
    }
    public static tu getHoldStackStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getHeldStack();
    }
    public static asv getCurrentScreenStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getCurrentScreen();
    }
    
	// Classes
    
    protected boolean isValidChest(asv guiScreen) {
        return guiScreen != null && (isGuiChest(guiScreen)
        		|| isGuiDispenser(guiScreen)
        		|| mods.isSpecialChest(guiScreen));
    }
	protected boolean isValidInventory(asv guiScreen) {
        return isStandardInventory(guiScreen)
        		|| mods.isSpecialInventory(guiScreen);
    }
	protected boolean isStandardInventory(asv guiScreen) {
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

    protected boolean isGuiContainer(Object o) { // GuiContainer (abstract class)
        return o != null && o instanceof atp;
    }
    protected boolean isGuiBeacon(Object o) { // GuiBeacon
        return o != null && o.getClass().equals(atq.class);
    }
    protected boolean isGuiBrewingStand(Object o) { // GuiBrewingStand
        return o != null && o.getClass().equals(atx.class);
    }
    protected boolean isGuiChest(Object o) { // GuiChest
        return o != null && o.getClass().equals(atz.class);
    }
    protected boolean isGuiWorkbench(Object o) { // GuiWorkbench
        return o != null && o.getClass().equals(aua.class);
    }
    protected boolean isGuiInventoryCreative(Object o) { // GuiInventoryCreative
        return o != null && o.getClass().equals(auc.class);
    }
    protected boolean isGuiEnchantmentTable(Object o) { // GuiEnchantmentTable
        return o != null && o.getClass().equals(auh.class);
    }
    protected boolean isGuiFurnace(Object o) { // GuiFurnace
        return o != null && o.getClass().equals(aui.class);
    }
    protected boolean isGuiInventory(Object o) { // GuiInventory
        return o != null && o.getClass().equals(auj.class);
    }
    protected boolean isGuiTrading(Object o) { // GuiTrading
        return o != null && o.getClass().equals(auk.class);
    }
    protected boolean isGuiAnvil(Object o) { // GuiAnvil
        return o != null && o.getClass().equals(aum.class);
    }
    protected boolean isGuiDispenser(Object o) { // GuiDispenser
        return o != null && o.getClass().equals(auo.class);
    }
    
    protected boolean isGuiButton(Object o) { // GuiButton
        return o != null && o instanceof ark;
    }
    protected boolean isGuiEditSign(Object o) { // GuiEditSign
        return o != null && o.getClass().equals(aun.class);
    }

    protected boolean isContainerBrewingStand(Object o) { // ContainerBrewingStand
        return o != null && o.getClass().equals(ra.class);
    }
    protected boolean isContainerChest(Object o) { // ContainerChest
        return o != null && o.getClass().equals(re.class);
    }
    protected boolean isContainerWorkbench(Object o) { // ContainerWorkbench
        return o != null && o.getClass().equals(rg.class);
    }
    protected boolean isContainerEnchantmentTable(Object o) { // ContainerEnchantmentTable
        return o != null && o.getClass().equals(rh.class);
    }
    protected boolean isContainerFurnace(Object o) { // ContainerFurnace 
        return o != null && o.getClass().equals(rk.class);
    }
	protected boolean isContainerPlayer(Object o) { // ContainerPlayer
	    return o != null && o.getClass().equals(rm.class);
	}
    protected boolean isContainerTrading(Object o) { // ContainerTrading
        return o != null && o.getClass().equals(rq.class);
    }
    protected boolean isContainerAnvil(Object o) { // ContainerAnvil
        return o != null && o.getClass().equals(rt.class);
    }
    protected boolean isContainerDispenser(Object o) { // ContainerDispenser
        return o != null && o.getClass().equals(rz.class);
    }
    protected boolean isContainerCreative(Object o) { // ContainerCreative
        return o != null && o.getClass().equals(aud.class);
    }

    protected boolean isItemArmor(Object o) { // ItemArmor
        return o != null && o instanceof sb; // OK
    }

    protected boolean isBasicSlot(Object o) { // Slot
        return o != null && o.getClass().equals(ry.class); // OK
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