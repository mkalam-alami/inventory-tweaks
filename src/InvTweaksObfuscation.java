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
        makeFieldPublic(avn.class, "b");
        // RenderEngine.texturePack
        makeFieldPublic(bap.class, "k");
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
    protected ayk getThePlayer() { // EntityPlayer
        return mc.g;
    }
    protected ayh getTheWorld() { // World
        return mc.e;
    }
	protected ayg getPlayerController() { // PlayerController
		return mc.b;
	}
	protected aue getCurrentScreen() { // GuiScreen
		return mc.r;
	}
	protected atj getFontRenderer() { // FontRenderer
        return mc.p;
    }
    protected void displayGuiScreen(aue parentScreen) {
        mc.a(parentScreen);
    }
    protected int getDisplayWidth() {
        return mc.c;
    }
    protected int getDisplayHeight() {
        return mc.d;
    }
    protected asl getGameSettings() {
        return mc.y;
    }
    public arn[] getRegisteredBindings() {
        return getGameSettings().P;
    }
    public void setRegisteredBindings(arn[] bindings) {
        getGameSettings().P = bindings;
    }
    protected int getKeyBindingForwardKeyCode() {
        return getKeyCode(getGameSettings().B);
    }
    protected int getKeyBindingBackKeyCode() {
        return getKeyCode(getGameSettings().D);
    }

	// EntityPlayer members

	protected qw getInventoryPlayer() { // InventoryPlayer
		return getThePlayer().bI;
	}
	protected um getCurrentEquippedItem() { // ItemStack
		return getThePlayer().bT();
	}
	protected rp getCraftingInventory() { // Container
		return getThePlayer().bJ;
	}
    protected rp getPlayerContainer() { // ContainerPlayer
        return (rp) getThePlayer().bJ; // MCP name: inventorySlots // XXX Why the same as craftying inventory?
    }

	// InventoryPlayer members
	
	protected um[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	protected void setMainInventory(um[] value) {
		getInventoryPlayer().a = value;
	}
	protected void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	protected void setHeldStack(um stack) {
		getInventoryPlayer().b(stack); // setItemStack
	}
	protected boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	protected um getHeldStack() {
		return getInventoryPlayer().n(); // getItemStack
	}
	protected um getFocusedStack() {
		return getInventoryPlayer().g(); // getCurrentItem
	}
	protected int getFocusedSlot() {
		return getInventoryPlayer().c; // currentItem
	}
	
    // GuiScreen members

	protected int getWindowWidth(aue guiScreen) {
	    return guiScreen.g;
	}
    protected int getWindowHeight(aue guiScreen) {
        return guiScreen.h;
    }
    protected int getGuiX(auy guiContainer) { // GuiContainer
        return guiContainer.n;
    }
    protected int getGuiY(auy guiContainer) {
        return guiContainer.o;
    }
    protected int getGuiWidth(auy guiContainer) { // GuiContainer
        return guiContainer.b;
    }
    protected int getGuiHeight(auy guiContainer) {
        return guiContainer.c;
    }
    @SuppressWarnings("unchecked")
	protected List<Object> getControlList(aue guiScreen) {
        return guiScreen.i;
    }
    protected void setControlList(aue guiScreen, List<?> controlList) {
        guiScreen.i = controlList;
    }
    protected auy asGuiContainer(aue guiScreen) {
        return (auy) guiScreen;
    }

    // FontRenderer members
	
	protected int getStringWidth(atj fontRenderer, String line) {
	    return fontRenderer.a(line);
	}
	protected void drawStringWithShadow(atj fontRenderer,
            String s, int i, int j, int k) {
        fontRenderer.a(s, i, j, k);
    }
	
	// ItemStack members

	protected um createItemStack(int id, int size, int damage) {
		return new um(id, size, damage);
	}
	protected um copy(um itemStack) {
		return itemStack.l();
	}
	protected int getItemDamage(um itemStack) {
		return itemStack.j();
	}
	protected int getMaxStackSize(um itemStack) {
		return itemStack.d();
	}
	protected boolean hasDataTags(um itemStack) {
	  return itemStack.o();
	}
	protected int getStackSize(um itemStack) {
		return itemStack.a;
	}
	protected int getItemID(um itemStack) {
		return itemStack.c;
	}
	protected boolean areItemStacksEqual(um itemStack1, um itemStack2) {
		return itemStack1.a(itemStack2) && getStackSize(itemStack1) == getStackSize(itemStack2);
	}
    protected boolean isItemStackDamageable(um itemStack) {
        return itemStack.f();
    }
    protected boolean areSameItemType(um itemStack1, um itemStack2) {
        return areItemsEqual(itemStack1, itemStack2) ||
                (isItemStackDamageable(itemStack1)
                        && getItemID(itemStack1) == getItemID(itemStack2));
    }
    protected boolean areItemsEqual(um itemStack1, um itemStack2) {
        return itemStack1.a(itemStack2); // isItemEqual
    }
    protected int getAnimationsToGo(um itemStack) {
        return itemStack.b;
    }
    protected uk getItem(um itemStack) { // Item
        return itemStack.b();
    }
    
    // Item & ItemArmor
    
    protected boolean isDamageable(uk item) {
        return item.n();
    }
    protected int getMaxDamage(uk item) {
        return item.m();
    }
    protected int getArmorLevel(st itemArmor) { // ItemArmor
        return itemArmor.b;
    }
    protected st asItemArmor(uk item) { // ItemArmor
        return (st) item;
    }
	
	// PlayerController members

	protected um clickInventory(ayg playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, ayk entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				(shiftHold) ? 1 : 0 /* XXX Placeholder */, entityPlayer);
	}
	
	// Container members

	protected int getWindowId(rp container) {
		return container.d;
	}
	protected List<?> getSlots(rp container) {
		return container.c;
	}
    protected sq getSlot(rp container, int i) { // Slot
        return (sq) (getSlots(container).get(i));
    }

    protected um getSlotStack(rp container, int i) {
        sq slot = getSlot(container, i);
        return (slot == null) ? null : getStack(slot); // getStack
    }

    protected void setSlotStack(rp container, int i, um stack) {
        container.a(i, stack); // putStackInSlot
    }

    // Slot members
    
    protected boolean hasStack(sq slot) { 
        return slot.d();
    }
    protected int getSlotNumber(sq slot) {
        try {
            // Creative slots don't set the "g" property, serve as a proxy for true slots
            if (slot instanceof avn) {
            	sq underlyingSlot = (sq) getThroughReflection(avn.class, "b", slot);
                if (underlyingSlot != null) {
                    return underlyingSlot.g;
                }
            }
        } catch (Exception e) {
            log.warning("Failed to access creative slot number");
        }
        return slot.g;
    }
    protected um getStack(sq slot) {
        return slot.c();
    }
    protected int getXDisplayPosition(sq slot) {
        return slot.h;
    }
    protected int getYDisplayPosition(sq slot) {
        return slot.i;
    }
    protected boolean areSlotAndStackCompatible(sq slot, um itemStack) {
        return slot.a(itemStack); // isItemValid
    }

    // GuiContainer members

    protected rp getContainer(auy guiContainer) {
        return guiContainer.d; /* inventorySlots */
    }

    // GuiButton

    protected ast asGuiButton(Object o) {
        return (ast) o;
    }
    protected void setEnabled(ast guiButton, boolean enabled) { // GuiButton
        guiButton.g = enabled;
    }
    protected int getId(ast guiButton) { // GuiButton
        return guiButton.f;
    }
    protected void setDisplayString(ast guiButton, String string) {
        guiButton.e = string;
    }
    protected String getDisplayString(ast guiButton) {
        return guiButton.e;
    }
    
    // Other

    protected void playSound(String string, float f, float g) {
        mc.A.a(string, f, g);
    }
    protected long getCurrentTime() {
        return getTheWorld().E();
    }
    protected int getKeyCode(arn b) { // KeyBinding
        return b.d;
    }
    protected int getSpecialChestRowSize(auy guiContainer, int defaultValue) {
    	return mods.getSpecialChestRowSize(guiContainer, defaultValue);
    }
    protected boolean hasTexture(String texture) {
    	bec texturePacksManager = (bec) getThroughReflection(bap.class, "k", mc.o);
        return texturePacksManager != null && texturePacksManager.e().a(texture) != null;
    }

    // Static access

    public static bn getLocalizationService() { // StringTranslate
        return bn.a(); // StringTranslate.getInstance()
    }
    public static String getCurrentLanguage() {
        return getLocalizationService().c();
    }
    public static String getLocalizedString(String key) {
        return getLocalizationService().b(key);
    }
    public static um getHoldStackStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getHeldStack();
    }
    public static aue getCurrentScreenStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getCurrentScreen();
    }
    
	// Classes
    
    protected boolean isValidChest(aue guiScreen) {
        return guiScreen != null && (isGuiChest(guiScreen)
        		|| isGuiDispenser(guiScreen)
        		|| mods.isSpecialChest(guiScreen));
    }
	protected boolean isValidInventory(aue guiScreen) {
        return isStandardInventory(guiScreen)
        		|| mods.isSpecialInventory(guiScreen);
    }
	protected boolean isStandardInventory(aue guiScreen) {
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
        return o != null && o instanceof auy;
    }
	
    protected boolean isGuiBeacon(Object o) { // GuiBeacon
        return o != null && o.getClass().equals(auz.class);
    }
    protected boolean isGuiBrewingStand(Object o) { // GuiBrewingStand
        return o != null && o.getClass().equals(avg.class);
    }
    protected boolean isGuiChest(Object o) { // GuiChest
        return o != null && o.getClass().equals(avi.class);
    }
    protected boolean isGuiWorkbench(Object o) { // GuiWorkbench
        return o != null && o.getClass().equals(avj.class);
    }
    protected boolean isGuiInventoryCreative(Object o) { // GuiInventoryCreative
        return o != null && o.getClass().equals(avl.class);
    }
    protected boolean isGuiEnchantmentTable(Object o) { // GuiEnchantmentTable
        return o != null && o.getClass().equals(avq.class);
    }
    protected boolean isGuiFurnace(Object o) { // GuiFurnace
        return o != null && o.getClass().equals(avr.class);
    }
    protected boolean isGuiInventory(Object o) { // GuiInventory
        return o != null && o.getClass().equals(avs.class);
    }
    protected boolean isGuiTrading(Object o) { // GuiTrading
        return o != null && o.getClass().equals(avt.class);
    }
    protected boolean isGuiAnvil(Object o) { // GuiAnvil
        return o != null && o.getClass().equals(avv.class);
    }
    protected boolean isGuiDispenser(Object o) { // GuiDispenser
        return o != null && o.getClass().equals(avx.class);
    }
    
    protected boolean isGuiButton(Object o) { // GuiButton
        return o != null && o instanceof ast;
    }
    
    protected boolean isGuiEditSign(Object o) { // GuiEditSign
        return o != null && o.getClass().equals(avw.class);
    }
	// ================  TODO  ================

    protected boolean isContainerBeacon(Object o) { // ContainerBeacon
        return o != null && o.getClass().equals(rq.class);
    }
    protected boolean isContainerBrewingStand(Object o) { // ContainerBrewingStand
        return o != null && o.getClass().equals(rs.class);
    }
    protected boolean isContainerChest(Object o) { // ContainerChest
        return o != null && o.getClass().equals(rw.class);
    }
    protected boolean isContainerWorkbench(Object o) { // ContainerWorkbench
        return o != null && o.getClass().equals(ry.class);
    }
    protected boolean isContainerEnchantmentTable(Object o) { // ContainerEnchantmentTable
        return o != null && o.getClass().equals(rz.class);
    }
    protected boolean isContainerFurnace(Object o) { // ContainerFurnace 
        return o != null && o.getClass().equals(sc.class);
    }
	protected boolean isContainerPlayer(Object o) { // ContainerPlayer
	    return o != null && o.getClass().equals(se.class);
	}
    protected boolean isContainerTrading(Object o) { // ContainerTrading
        return o != null && o.getClass().equals(si.class);
    }
    protected boolean isContainerAnvil(Object o) { // ContainerAnvil
        return o != null && o.getClass().equals(sl.class);
    }
    protected boolean isContainerDispenser(Object o) { // ContainerDispenser
        return o != null && o.getClass().equals(sr.class);
    }
    protected boolean isContainerCreative(Object o) { // ContainerCreative
        return o != null && o.getClass().equals(avm.class);
    }

    protected boolean isItemArmor(Object o) { // ItemArmor
        return o != null && o instanceof st;
    }

    protected boolean isBasicSlot(Object o) { // Slot
        return o != null && o.getClass().equals(sq.class);
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