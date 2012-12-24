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
        makeFieldPublic(avu.class, "b");
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
    protected ays getThePlayer() { // EntityPlayer
        return mc.g;
    }
    protected ayp getTheWorld() { // World
        return mc.e;
    }
	protected ayo getPlayerController() { // PlayerController
		return mc.b;
	}
	protected aul getCurrentScreen() { // GuiScreen
		return mc.r;
	}
	protected atq getFontRenderer() { // FontRenderer
        return mc.p;
    }
    protected void displayGuiScreen(aul parentScreen) {
        mc.a(parentScreen);
    }
    protected int getDisplayWidth() {
        return mc.c;
    }
    protected int getDisplayHeight() {
        return mc.d;
    }
    protected ast getGameSettings() {
    
        return mc.y;
    }
    public arv[] getRegisteredBindings() {
        return getGameSettings().S;
    }
    public void setRegisteredBindings(arv[] bindings) {
        getGameSettings().S = bindings;
    }
    protected int getKeyBindingForwardKeyCode() {
        return getKeyCode(getGameSettings().E);
    }
    protected int getKeyBindingBackKeyCode() {
        return getKeyCode(getGameSettings().G);
    }

	// EntityPlayer members

	protected qw getInventoryPlayer() { // InventoryPlayer
		return getThePlayer().bJ;
	}
	protected ur getCurrentEquippedItem() { // ItemStack
		return getThePlayer().bS();
	}
	protected rq getCraftingInventory() { // Container
		return getThePlayer().bK;
	}
    protected rq getPlayerContainer() { // ContainerPlayer
        return (rq) getThePlayer().bK; // MCP name: inventorySlots // XXX Why the same as craftying inventory?
    }

	// InventoryPlayer members
	
	protected ur[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	protected void setMainInventory(ur[] value) {
		getInventoryPlayer().a = value;
	}
	protected void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	protected void setHeldStack(ur stack) {
		getInventoryPlayer().b(stack); // setItemStack
	}
	protected boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	protected ur getHeldStack() {
		return getInventoryPlayer().n(); // getItemStack
	}
	protected ur getFocusedStack() {
		return getInventoryPlayer().g(); // getCurrentItem
	}
	protected int getFocusedSlot() {
		return getInventoryPlayer().c; // currentItem
	}
	
    // GuiScreen members

	protected int getWindowWidth(aul guiScreen) {
	    return guiScreen.g;
	}
    protected int getWindowHeight(aul guiScreen) {
        return guiScreen.h;
    }
    protected int getGuiX(avf guiContainer) { // GuiContainer
        return guiContainer.n;
    }
    protected int getGuiY(avf guiContainer) {
        return guiContainer.o;
    }
    protected int getGuiWidth(avf guiContainer) { // GuiContainer
        return guiContainer.b;
    }
    protected int getGuiHeight(avf guiContainer) {
        return guiContainer.c;
    }
    @SuppressWarnings("unchecked")
	protected List<Object> getControlList(aul guiScreen) {
        return guiScreen.i;
    }
    protected void setControlList(aul guiScreen, List<?> controlList) {
        guiScreen.i = controlList;
    }
    protected avf asGuiContainer(aul guiScreen) {
        return (avf) guiScreen;
    }

    // FontRenderer members
	
	protected int getStringWidth(atq fontRenderer, String line) {
	    return fontRenderer.a(line);
	}
	protected void drawStringWithShadow(atq fontRenderer,
            String s, int i, int j, int k) {
        fontRenderer.a(s, i, j, k);
    }
	
	// ItemStack members

	protected ur createItemStack(int id, int size, int damage) {
		return new ur(id, size, damage);
	}
	protected ur copy(ur itemStack) {
		return itemStack.l();
	}
	protected int getItemDamage(ur itemStack) {
		return itemStack.j();
	}
	protected int getMaxStackSize(ur itemStack) {
		return itemStack.d();
	}
	protected boolean hasDataTags(ur itemStack) {
	  return itemStack.o();
	}
	protected int getStackSize(ur itemStack) {
		return itemStack.a;
	}
	protected int getItemID(ur itemStack) {
		return itemStack.c;
	}
	protected boolean areItemStacksEqual(ur itemStack1, ur itemStack2) {
		return itemStack1.a(itemStack2) && getStackSize(itemStack1) == getStackSize(itemStack2);
	}
    protected boolean isItemStackDamageable(ur itemStack) {
        return itemStack.f();
    }
    protected boolean areSameItemType(ur itemStack1, ur itemStack2) {
        return areItemsEqual(itemStack1, itemStack2) ||
                (isItemStackDamageable(itemStack1)
                        && getItemID(itemStack1) == getItemID(itemStack2));
    }
    protected boolean areItemsEqual(ur itemStack1, ur itemStack2) {
        return itemStack1.a(itemStack2); // isItemEqual
    }
    protected int getAnimationsToGo(ur itemStack) {
        return itemStack.b;
    }
    protected up getItem(ur itemStack) { // Item
        return itemStack.b();
    }
    
    // Item & ItemArmor
    
    protected boolean isDamageable(up item) {
        return item.n();
    }
    protected int getMaxDamage(up item) {
        return item.m();
    }
    protected int getArmorLevel(su itemArmor) { // ItemArmor
        return itemArmor.b;
    }
    protected su asItemArmor(up item) { // ItemArmor
        return (su) item;
    }
	
	// PlayerController members

	protected ur clickInventory(ayo playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, ays entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				(shiftHold) ? 1 : 0 /* XXX Placeholder */, entityPlayer);
	}
	
	// Container members

	protected int getWindowId(rq container) {
		return container.d;
	}
	protected List<?> getSlots(rq container) {
		return container.c;
	}
    protected sr getSlot(rq container, int i) { // Slot
        return (sr) (getSlots(container).get(i));
    }

    protected ur getSlotStack(rq container, int i) {
    	sr slot = getSlot(container, i);
        return (slot == null) ? null : getStack(slot); // getStack
    }

    protected void setSlotStack(rq container, int i, ur stack) {
        container.a(i, stack); // putStackInSlot
    }

    // Slot members
    
    protected boolean hasStack(sr slot) { 
        return slot.d();
    }
    protected int getSlotNumber(sr slot) {
        try {
            // Creative slots don't set the "g" property, serve as a proxy for true slots
            if (slot instanceof avu) {
            	sr underlyingSlot = (sr) getThroughReflection(avu.class, "b", slot);
                if (underlyingSlot != null) {
                    return underlyingSlot.g;
                }
            }
        } catch (Exception e) {
            log.warning("Failed to access creative slot number");
        }
        return slot.g;
    }
    protected ur getStack(sr slot) {
        return slot.c();
    }
    protected int getXDisplayPosition(sr slot) {
        return slot.h;
    }
    protected int getYDisplayPosition(sr slot) {
        return slot.i;
    }
    protected boolean areSlotAndStackCompatible(sr slot, ur itemStack) {
        return slot.a(itemStack); // isItemValid
    }

    // GuiContainer members

    protected rq getContainer(avf guiContainer) {
        return guiContainer.d; /* inventorySlots */
    }

    // GuiButton

    protected atb asGuiButton(Object o) {
        return (atb) o;
    }
    protected void setEnabled(atb guiButton, boolean enabled) { // GuiButton
        guiButton.g = enabled;
    }
    protected int getId(atb guiButton) { // GuiButton
        return guiButton.f;
    }
    protected void setDisplayString(atb guiButton, String string) {
        guiButton.e = string;
    }
    protected String getDisplayString(atb guiButton) {
        return guiButton.e;
    }
    
    // Other

    protected void playSound(String string, float f, float g) {
        mc.A.a(string, f, g);
    }
    protected long getCurrentTime() {
        return getTheWorld().E();
    }
    protected int getKeyCode(arv b) { // KeyBinding
        return b.d;
    }
    protected int getSpecialChestRowSize(avf guiContainer, int defaultValue) {
    	return mods.getSpecialChestRowSize(guiContainer, defaultValue);
    }
    protected boolean hasTexture(String texture) {
    	ben texturePacksManager = (ben) getThroughReflection(bap.class, "k", mc.o);
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
    public static ur getHoldStackStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getHeldStack();
    }
    public static aul getCurrentScreenStatic(Minecraft mc) {
        return new InvTweaksObfuscation(mc).getCurrentScreen();
    }
    
	// Classes
    
    protected boolean isValidChest(aul guiScreen) {
        return guiScreen != null && (isGuiChest(guiScreen)
        		|| isGuiDispenser(guiScreen)
        		|| mods.isSpecialChest(guiScreen));
    }
	protected boolean isValidInventory(aul guiScreen) {
        return isStandardInventory(guiScreen)
        		|| mods.isSpecialInventory(guiScreen);
    }
	protected boolean isStandardInventory(aul guiScreen) {
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
        return o != null && o instanceof avf;
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
        return o != null && o instanceof atb;
    }
    
    protected boolean isGuiEditSign(Object o) { // GuiEditSign
		return o != null && o.getClass().equals(awd.class);
    }
	// ================  TODO  ================

    protected boolean isContainerBeacon(Object o) { // ContainerBeacon
        return o != null && o.getClass().equals(rr.class);
    }
    protected boolean isContainerBrewingStand(Object o) { // ContainerBrewingStand
        return o != null && o.getClass().equals(rt.class);
    }
    protected boolean isContainerChest(Object o) { // ContainerChest
        return o != null && o.getClass().equals(rx.class);
    }
    protected boolean isContainerWorkbench(Object o) { // ContainerWorkbench
        return o != null && o.getClass().equals(ry.class);
    }
    protected boolean isContainerEnchantmentTable(Object o) { // ContainerEnchantmentTable
        return o != null && o.getClass().equals(sa.class);
    }
    protected boolean isContainerFurnace(Object o) { // ContainerFurnace 
        return o != null && o.getClass().equals(sd.class);
    }
	protected boolean isContainerPlayer(Object o) { // ContainerPlayer
	    return o != null && o.getClass().equals(sf.class);
	}
    protected boolean isContainerTrading(Object o) { // ContainerMerchant
        return o != null && o.getClass().equals(sj.class);
    }
    protected boolean isContainerAnvil(Object o) { // ContainerAnvil
        return o != null && o.getClass().equals(sm.class);
    }
    protected boolean isContainerDispenser(Object o) { // ContainerDispenser
        return o != null && o.getClass().equals(ss.class);
    }
    protected boolean isContainerCreative(Object o) { // ContainerCreative
        return o != null && o.getClass().equals(avt.class);
    }

    protected boolean isItemArmor(Object o) { // ItemArmor
        return o != null && o instanceof su;
    }

    protected boolean isBasicSlot(Object o) { // Slot
        return o != null && o.getClass().equals(sr.class);
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