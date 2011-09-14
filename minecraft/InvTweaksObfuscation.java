import java.io.File;
import java.util.List;

import net.minecraft.client.Minecraft;

/**
 * Minecraft 1.8 Obfuscation layer
 * 
 * @author Jimeo Wan
 *
 */
public class InvTweaksObfuscation {

	protected Minecraft mc;
	
	public InvTweaksObfuscation(Minecraft mc) {
		this.mc = mc;
	}
	
	// Minecraft members

	protected void addChatMessage(String message) {
		if (mc.v != null) {
			mc.v.a(message);
		}
	}
	protected boolean isMultiplayerWorld() {
		return mc.l();
	}
    protected qs getThePlayer() { // EntityPlayer
        return mc.h;
    }
	protected hw getPlayerController() { // PlayerController
		return mc.c;
	}
	protected qr getCurrentScreen() { // GuiScreen
		return mc.r;
	}

	// EntityPlayer members
	
	protected ui getInventoryPlayer() { // InventoryPlayer
		return getThePlayer().as;
	}
	protected ul getCurrentEquippedItem() { // ItemStack
		return getThePlayer().aj();
	}
	protected cf getCraftingInventory() { // Container
		return getThePlayer().au;
	}
    protected r getPlayerContainer() { // ContainerPlayer
        return (r) getThePlayer().at; // MCP name: inventorySlots
    }

	// InventoryPlayer members
	
	protected ul[] getMainInventory() {
		return getInventoryPlayer().a;
	}
	protected void setMainInventory(ul[] value) {
		getInventoryPlayer().a = value;
	}
	protected void setHasInventoryChanged(boolean value) {
		getInventoryPlayer().e = value;
	}
	protected void setHoldStack(ul stack) {
		getInventoryPlayer().b(stack); // MCP name: setItemStack
	}
	protected boolean hasInventoryChanged() {
		return getInventoryPlayer().e;
	}
	protected ul getHoldStack() {
		return getInventoryPlayer().j(); // MCP name: getItemStack
	}
	protected ul getFocusedStack() {
		return getInventoryPlayer().b(); // MCP name: getCurrentItem
	}
	protected int getFocusedSlot() {
		return getInventoryPlayer().c; // MCP name: currentItem
	}
	
	// ItemStack members

	protected ul createItemStack(int id, int size, int damage) {
		return new ul(id, size, damage);
	}
	protected ul copy(ul itemStack) {
		return itemStack.k();
	}
	protected int getItemDamage(ul itemStack) {
		return itemStack.i();
	}
	protected int getMaxStackSize(ul itemStack) {
		return itemStack.c();
	}
	protected int getStackSize(ul itemStack) {
		return itemStack.a;
	}
	protected void setStackSize(ul itemStack, int value) {
		itemStack.a = value;
	}
	protected int getItemID(ul itemStack) {
		return itemStack.c;
	}
	protected boolean areItemStacksEqual(ul itemStack1, ul itemStack2) {
		return ul.a(itemStack1, itemStack2);
	}
    protected boolean isItemStackDamageable(ul itemStack) {
        return itemStack.e();
    }
    protected boolean areSameItemType(ul itemStack1, ul itemStack2) {
        return areItemStacksEqual(itemStack1, itemStack2) ||
                (isItemStackDamageable(itemStack1)
                        && getItemID(itemStack1) == getItemID(itemStack2));
    }
    protected boolean areItemsEqual(ul itemStack1, ul itemStack2) {
        return itemStack1.c(itemStack2); // isItemEqual
    }
	
	// PlayerController members

	protected ul clickInventory(hw playerController,
			int windowId, int slot, int clickButton,
			boolean shiftHold, qs entityPlayer) {
		return playerController.a(windowId, slot, clickButton,
				shiftHold, entityPlayer);
	}
	
	// Container members
	
	protected int getWindowId(cf container) {
		return container.f;
	}
	protected List<?> getSlots(cf container) {
		return container.e;
	}
    protected sx getSlot(cf container, int i) { // Slot
        return (sx) getSlots(container).get(i);
    }

    protected ul getSlotStack(cf container, int i) {
        sx slot = (sx) getSlots(container).get(i);
        return (slot == null) ? null : getStack(slot); /* getStack */
    }

    protected void setSlotStack(cf container, int i, ul stack) {
        container.a(i, stack); /* putStackInSlot */
    }

    // Slot members
    
    protected boolean hasStack(sx slot) { 
        return slot.b();
    }
    protected int getSlotNumber(sx slot) {
        return slot.b;
    }
    protected ul getStack(sx slot) {
        return slot.a();
    }

    // GuiContainer members

    protected cf getContainer(?? guiContainer) {
        return guiContainer.inventorySlots;
    }
    
    // Other

    protected boolean isChestOrDispenser(qr guiScreen) {
        return ((isGuiChest(guiScreen) && !guiScreen.getClass()
                        .getSimpleName().equals("MLGuiChestBuilding")) // Millenaire mod
                || isGuiDispenser(guiScreen));
    }
    protected int getKeycode(KeyBinding keyBinding) {
        return keyBinding.keyCode;
    }

    // Static access

    /**
     * Returns the Minecraft folder ensuring: - It is an absolute path - It ends
     * with a folder separator
     */
    public static String getMinecraftDir() {
        String absolutePath = Minecraft.b().getAbsolutePath();
        if (absolutePath.endsWith(".")) {
            return absolutePath.substring(0, absolutePath.length() - 1);
        }
        if (absolutePath.endsWith(File.separator)) {
            return absolutePath;
        } else {
            return absolutePath + File.separatorChar;
        }
    }
    public static ul getHoldStackStatic(Minecraft mc) {
        return new Obfuscation(mc).getHoldStack();
    }
    public static qr getCurrentScreenStatic(Minecraft mc) {
        return new Obfuscation(mc).getCurrentScreen();
    }
    
	// Classes
	
	protected boolean isGuiContainer(Object o) {
        return o instanceof ?;
	}
    protected boolean isGuiInventory(Object o) {
        return o instanceof ?;
    }
    protected boolean isGuiEditSign(Object o) {
        return o instanceof ?;
    }
    
	protected boolean isContainerPlayer(Object o) {
	    return o instanceof r;
	}
    protected boolean isContainerChest(Object o) {
        return o instanceof ContainerChest;
    }
    protected boolean isContainerFurnace(Object o) {
        return o instanceof ContainerFurnace;
    }
    protected boolean isContainerDispenser(Object o) {
        return o instanceof ContainerDispenser;
    }
    protected boolean isContainerWorkbench(Object o) {
        return o instanceof ContainerWorkbench;
    }
	
}