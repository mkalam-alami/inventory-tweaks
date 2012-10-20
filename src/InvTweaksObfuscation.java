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
        makeFieldPublic(aqp.class, "b");
        // RenderEngine.texturePack
        makeFieldPublic(avf.class, "k");
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
    protected atg getThePlayer() { // EntityPlayer
        return mc.g;
    }
    protected atd getTheWorld() { // World
        return mc.e;
    }
	protected atc getPlayerController() { // PlayerController
		return mc.b;
	}
	protected apn getCurrentScreen() { // GuiScreen
		return mc.r;
	}
	protected aov getFontRenderer() { // FontRenderer
        return mc.p;
    }
    protected void displayGuiScreen(apn guiScreen) {
        mc.a(guiScreen);
    }
    protected int getDisplayWidth() {
        return mc.c;
    }
    protected int getDisplayHeight() {
        return mc.d;
    }
    protected anz getGameSettings() {
        return mc.y;
    }
    public anf[] getRegisteredBindings() {
        return getGameSettings().L;
    }
    public void setRegisteredBindings(anf[] bindings) {
        getGameSettings().L = bindings;
    }
    protected int getKeyBindingForwardKeyCode() {
        return getKeyCode(getGameSettings().x);
    }
    protected int getKeyBindingBackKeyCode() {
        return getKeyCode(getGameSettings().z);
    }

	// EntityPlayer members

	protected of getInventoryPlayer() { // InventoryPlayer
		return getThePlayer().by;
	}
	protected rj getCurrentEquippedItem() { // ItemStack
		return getThePlayer().bC();
	}
	protected ov getCraftingInventory() { // Container
		return getThePlayer().bA;
	}
    protected ov getPlayerContainer() { // ContainerPlayer
        return (ov) getThePlayer().bz; // MCP name: inventorySlots
    }

	// InventoryPlayer members
	
	protected rj[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	protected void setMainInventory(rj[] value) {
		getInventoryPlayer().a = value;
	}
	protected void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	protected void setHeldStack(rj stack) {
		getInventoryPlayer().b(stack); // setItemStack
	}
	protected boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	protected rj getHeldStack() {
		return getInventoryPlayer().o(); // getItemStack
	}
	protected rj getFocusedStack() {
		return getInventoryPlayer().g(); // getCurrentItem
	}
	protected int getFocusedSlot() {
		return getInventoryPlayer().c; // currentItem
	}
	
    // GuiScreen members

	protected int getWindowWidth(apn guiScreen) {
	    return guiScreen.f;
	}
    protected int getWindowHeight(apn guiScreen) {
        return guiScreen.g;
    }
    protected int getGuiX(aqh guiContainer) { // GuiContainer
        return guiContainer.m;
    }
    protected int getGuiY(aqh guiContainer) {
        return guiContainer.n;
    }
    protected int getGuiWidth(aqh guiContainer) { // GuiContainer
        return guiContainer.b;
    }
    protected int getGuiHeight(aqg guiContainer) {
        return guiContainer.c;
    }
    @SuppressWarnings("unchecked")
	protected List<Object> getControlList(apn guiScreen) {
        return guiScreen.h;
    }
    protected void setControlList(apn guiScreen, List<?> controlList) {
        guiScreen.h = controlList;
    }
    protected aqh asGuiContainer(apn guiScreen) {
        return (aqh) guiScreen;
    }

    // FontRenderer members
	
	protected int getStringWidth(aov fontRenderer, String line) {
	    return fontRenderer.a(line);
	}
	protected void drawStringWithShadow(aov fontRenderer,
            String s, int i, int j, int k) {
        fontRenderer.a(s, i, j, k);
    }
	
	// ItemStack members

	protected rj createItemStack(int id, int size, int damage) {
		return new rj(id, size, damage);
	}
	protected rj copy(rj itemStack) {
		return itemStack.l();
	}
	protected int getItemDamage(rj itemStack) {
		return itemStack.j();
	}
	protected int getMaxStackSize(rj itemStack) {
		return itemStack.d();
	}
	protected boolean hasDataTags(rj itemStack) {
	  return itemStack.o();
	}
	protected int getStackSize(rj itemStack) {
		return itemStack.a;
	}
	protected int getItemID(rj itemStack) {
		return itemStack.c;
	}
	protected boolean areItemStacksEqual(rj itemStack1, rj itemStack2) {
		return itemStack1.c(itemStack2);
	}
    protected boolean isItemStackDamageable(rj itemStack) {
        return itemStack.f();
    }
    protected boolean areSameItemType(rj itemStack1, rj itemStack2) {
        return areItemsEqual(itemStack1, itemStack2) ||
                (isItemStackDamageable(itemStack1)
                        && getItemID(itemStack1) == getItemID(itemStack2));
    }
    protected boolean areItemsEqual(rj itemStack1, rj itemStack2) {
        return itemStack1.a(itemStack2); // isItemEqual
    }
    protected int getAnimationsToGo(rj itemStack) {
        return itemStack.b;
    }
    protected rh getItem(rj itemStack) { // Item
        return itemStack.b();
    }
    
    // Item & ItemArmor
    
    protected boolean isDamageable(rh item) {
        return item.m();
    }
    protected int getArmorLevel(pt itemArmor) { // ItemArmor
        return itemArmor.b;
    }
    protected pt asItemArmor(rh item) { // ItemArmor
        return (pt) item;
    }
	
	// PlayerController members

	protected rj clickInventory(atc playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, atg entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				shiftHold, entityPlayer);
	}
	
	// Container members

	protected int getWindowId(ov container) {
		return container.c;
	}
	protected List<?> getSlots(ov container) {
		return container.b;
	}
    protected pr getSlot(ov container, int i) { // Slot
        return (pr) (getSlots(container).get(i));
    }

    protected rj getSlotStack(ov container, int i) {
        pr slot = getSlot(container, i);
        return (slot == null) ? null : getStack(slot); // getStack
    }

    protected void setSlotStack(ov container, int i, rj stack) {
        container.a(i, stack); // putStackInSlot
    }

    // Slot members
    
    protected boolean hasStack(pr slot) { 
        return slot.d();
    }
    protected int getSlotNumber(pr slot) {
        try {
            // Creative slots don't set the "d" property, serve as a proxy for true slots
            if (slot instanceof aqp) {
                pr underlyingSlot = (pr) getThroughReflection(aqp.class, "b", slot);
                if (underlyingSlot != null) {
                    return underlyingSlot.d;
                }
            }
        } catch (Exception e) {
            log.warning("Failed to access creative slot number");
        }
        return slot.d;
    }
    protected rj getStack(pr slot) {
        return slot.c();
    }
    protected int getXDisplayPosition(pr slot) {
        return slot.e;
    }
    protected int getYDisplayPosition(pr slot) {
        return slot.f;
    }
    protected boolean areSlotAndStackCompatible(pr slot, rj itemStack) {
        return slot.a(itemStack); // isItemValid
    }

    // GuiContainer members

    protected ov getContainer(aqh guiContainer) {
        return guiContainer.d; /* inventorySlots */
    }

    // GuiButton

    protected aoh asGuiButton(Object o) {
        return (aoh) o;
    }
    protected void setEnabled(aoh guiButton, boolean enabled) { // GuiButton
        guiButton.g = enabled;
    }
    protected int getId(aoh guiButton) { // GuiButton
        return guiButton.f;
    }
    protected void setDisplayString(aoh guiButton, String string) {
        guiButton.e = string;
    }
    protected String getDisplayString(aoh guiButton) {
        return guiButton.e;
    }
    
    // Other

    protected void playSound(String string, float f, float g) {
        mc.A.a(string, f, g);
    }
    protected long getCurrentTime() {
        return getTheWorld().D();
    }
    protected int getKeyCode(anf w) { // KeyBinding
        return w.d;
    }
    protected int getSpecialChestRowSize(aqh guiContainer, int defaultValue) {
    	return mods.getSpecialChestRowSize(guiContainer, defaultValue);
    }
    protected boolean hasTexture(String texture) {
        ayj texturePacksManager = (ayj) getThroughReflection(avf.class, "k", mc.o);
        return texturePacksManager != null && texturePacksManager.e().a(texture) != null;
    }

    // Static access

    public static ak getLocalizationService() { // StringTranslate
        return ak.a(); // StringTranslate.getInstance()
    }
    public static String getCurrentLanguage() {
        return getLocalizationService().c();
    }
    public static String getLocalizedString(String key) {
        return getLocalizationService().b(key);
    }
    public static rj getHoldStackStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getHeldStack();
    }
    public static apn getCurrentScreenStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getCurrentScreen();
    }
    
	// Classes
    
    protected boolean isValidChest(apn guiScreen) {
        return guiScreen != null && (isGuiChest(guiScreen)
        		|| isGuiDispenser(guiScreen)
        		|| mods.isSpecialChest(guiScreen));
    }
	protected boolean isValidInventory(apn guiScreen) {
        return isStandardInventory(guiScreen)
        		|| mods.isSpecialInventory(guiScreen);
    }
	protected boolean isStandardInventory(apn guiScreen) {
        return isGuiInventory(guiScreen)
        		|| isGuiWorkbench(guiScreen)
        		|| isGuiFurnace(guiScreen)
                || isGuiBrewingStand(guiScreen)
                || isGuiEnchantmentTable(guiScreen)
                || isGuiTrading(guiScreen)
                || (isGuiInventoryCreative(guiScreen) && getSlots(getContainer((aqh) guiScreen)).size() == 46);
    }

    protected boolean isGuiContainer(Object o) { // GuiContainer (abstract class)
        return o != null && o instanceof aqh;
    }
    protected boolean isGuiBrewingStand(Object o) { // GuiBrewingStand
        return o != null && o.getClass().equals(aqk.class);
    }
    protected boolean isGuiChest(Object o) { // GuiChest
        return o != null && o.getClass().equals(aql.class);
    }
    protected boolean isGuiWorkbench(Object o) { // GuiWorkbench
        return o != null && o.getClass().equals(aqm.class);
    }
    protected boolean isGuiInventoryCreative(Object o) { // GuiInventoryCreative
        return o != null && o.getClass().equals(aqn.class);
    }
    protected boolean isGuiEnchantmentTable(Object o) { // GuiEnchantmentTable
        return o != null && o.getClass().equals(aqs.class);
    }
    protected boolean isGuiFurnace(Object o) { // GuiFurnace
        return o != null && o.getClass().equals(aqt.class);
    }
    protected boolean isGuiInventory(Object o) { // GuiInventory
        return o != null && o.getClass().equals(aqu.class);
    }
    protected boolean isGuiTrading(Object o) { // GuiTrading
        return o != null && o.getClass().equals(aqv.class);
    }
    protected boolean isGuiDispenser(Object o) { // GuiDispenser
        return o != null && o.getClass().equals(aqy.class);
    }
    
    protected boolean isGuiButton(Object o) { // GuiButton
        return o != null && o instanceof aoh;
    }
    protected boolean isGuiEditSign(Object o) { // GuiEditSign
        return o != null && o.getClass().equals(aqx.class);
    }
    
    protected boolean isContainerBrewingStand(Object o) { // ContainerBrewingStand
        return o != null && o.getClass().equals(ov.class);
    }
    protected boolean isContainerChest(Object o) { // ContainerChest
        return o != null && o.getClass().equals(pa.class);
    }
    protected boolean isContainerWorkbench(Object o) { // ContainerWorkbench
        return o != null && o.getClass().equals(pc.class);
    }
    protected boolean isContainerEnchantmentTable(Object o) { // ContainerEnchantmentTable
        return o != null && o.getClass().equals(pd.class);
    }
    protected boolean isContainerFurnace(Object o) { // ContainerFurnace 
        return o != null && o.getClass().equals(pg.class);
    }
	protected boolean isContainerPlayer(Object o) { // ContainerPlayer
	    return o != null && o.getClass().equals(pi.class);
	}
	// pm? TODO
    protected boolean isContainerDispenser(Object o) { // ContainerDispenser
        return o != null && o.getClass().equals(ps.class);
    }
    protected boolean isContainerCreative(Object o) { // ContainerCreative
        return o != null && o.getClass().equals(aqo.class);
    }

    protected boolean isItemArmor(Object o) { // ItemArmor
        return o != null && o instanceof pt;
    }

    protected boolean isBasicSlot(Object o) { // Slot
        return o != null && o.getClass().equals(pr.class);
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