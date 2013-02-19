package invtweaks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.lang.reflect.Method;

import invtweaks.api.ContainerSection;
import invtweaks.api.ContainerGUI;
import invtweaks.api.InventoryGUI;
import net.minecraft.inventory.Container;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Slot;


public class InvTweaksModCompatibility {

    private InvTweaksObfuscation obf;

    public InvTweaksModCompatibility(InvTweaksObfuscation obf) {
    	this.obf = obf;
    }

    /**
     * Returns true if the screen is a chest/dispenser,
     * despite not being a GuiChest or a GuiDispenser.
     * @param guiContainer
     * @return
     */
    public boolean isSpecialChest(GuiScreen guiScreen) {
        return getContainerGUIAnnotation(guiScreen.getClass()) != null // API-marked classes
            || is(guiScreen, "GuiAlchChest") // Equivalent Exchange
        	|| is(guiScreen, "GuiCondenser") // Equivalent Exchange
        	|| is(guiScreen, "GUIChest") // Iron chests (formerly IC2)
                || is(guiScreen, "GuiMultiPageChest") // Multi Page chest
                || is(guiScreen, "GuiGoldSafe") // More Storage
                || is(guiScreen, "GuiLocker")
                || is(guiScreen, "GuiDualLocker")
                || is(guiScreen, "GuiSafe")
                || is(guiScreen, "GuiCabinet")
                || is(guiScreen, "GuiTower")
                || is(guiScreen, "GuiBufferChest") // Red Power 2
                || is(guiScreen, "GuiRetriever") // Red Power 2
                || is(guiScreen, "GuiItemDetect") // Red Power 2
                || is(guiScreen, "GuiAlloyFurnace") // Red Power 2
                || is(guiScreen, "GuiDeploy") // Red Power 2
                || is(guiScreen, "GuiSorter") // Red Power 2
                || is(guiScreen, "GuiFilter") // Red Power 2
                || is(guiScreen, "GuiAdvBench") // Red Power 2
                || is(guiScreen, "GuiEject") // Red Power 2
                || is(guiScreen, "GuiBag") // Red Power 2
                || is(guiScreen, "GuiPersonalChest")
                || is(guiScreen, "GuiNuclearReactor") // IC2
                || is(guiScreen, "GuiEnderChest") // EnderChest
                || is(guiScreen, "GuiColorBox")
                || is(guiScreen, "GuiLinkedColorBox") // ColorBox
                || is(guiScreen, "FC_GuiChest") // Metallurgy
                || is(guiScreen, "FM_GuiMintStorage") // Metallurgy
                || is(guiScreen, "GuiChestTFC") // TerraFirmaCraft
                || is(guiScreen, "GuiBackpack") // Backpacks
          ;
    }

    /**
     * Returns a special chest row size.
     * Given guiContainer must be checked first with isSpecialChest().
     * @param guiContainer
     * @param defaultValue
     * @return
     */
    public int getSpecialChestRowSize(GuiContainer guiContainer, int defaultValue) {
        ContainerGUI annotation = getContainerGUIAnnotation(guiContainer.getClass());
        if(annotation != null) {
            Method m = getAnnotatedMethod(guiContainer.getClass(), new Class[] { ContainerGUI.RowSizeCallback.class }, 0, int.class);
            if(m != null) {
                try {
                    return (Integer)m.invoke(guiContainer);
                } catch(Exception e) {
                    // TODO: Do something here to tell mod authors they're doing it wrong.
                    return annotation.rowSize();
                }
            } else {
                return annotation.rowSize();
            }
        } else if(is(guiContainer, "GuiAlchChest")
    			|| is(guiContainer, "GuiCondenser")) { // Equivalent Exchange
            return 13;
        } else if (is(guiContainer, "GUIChest")) { // Iron chests (formerly IC2)
	        try {
	          return (Integer)guiContainer.getClass().getMethod("getRowLength").invoke(guiContainer);
	        } catch (Exception e) {
	        	// Skip it
	        }
	    } else if (is(guiContainer, "GuiMultiPageChest")) { // Multi Page chest
	      return 13;
	    } else if (is(guiContainer, "GuiLocker") // More Storage
	    		|| is(guiContainer, "GuiDualLocker")
	    		|| is(guiContainer, "GuiTower")) {
	      return 8;
	    } else if (is(guiContainer, "GuiBufferChest")) { // Red Power 2
	      return 4;
	    } else if (is(guiContainer, "GuiSorter")) {
	      return 8;
	    } else if (is(guiContainer, "GuiRetriever")
	    		|| is(guiContainer, "GuiItemDetect")
	    		|| is(guiContainer, "GuiAlloyFurnace")
	    		|| is(guiContainer, "GuiDeploy")
	    		|| is(guiContainer, "GuiFilter")
	    		|| is(guiContainer, "GuiEject")) {
	      return 3;
	    } else if (is(guiContainer, "GuiNuclearReactor")) { // IC2
	    	return (obf.getSlots(obf.getContainer(guiContainer)).size() - 36) / 6;
	    }
        return defaultValue;
    }

    public boolean isChestWayTooBig(GuiScreen guiScreen) {
        return is(guiScreen, "GuiAlchChest") // Equivalent Exchange
        		|| is(guiScreen, "GuiMultiPageChest") // MultiPage Chest
        		|| is(guiScreen, "GUIChest") // IronChests
        		|| is(guiScreen, "FC_GuiChest") // Metallurgy
        	;
    }

    /**
     * Returns true if the screen is the inventory screen, despite not being a GuiInventory.
     * @param guiScreen
     * @return
     */
    public boolean isSpecialInventory(GuiScreen guiScreen) {
        if(getInventoryGUIAnnotation(guiScreen.getClass()) != null) {
            return true;
        }
    	try {
			return obf.getSlots(obf.getContainer(obf.asGuiContainer(guiScreen))).size() > InvTweaksConst.INVENTORY_SIZE
					&& !obf.isGuiInventoryCreative(guiScreen);
		} catch (Exception e) {
			return false;
		}
    }

	@SuppressWarnings("unchecked")
    public Map<ContainerSection, List<Slot>> getSpecialContainerSlots(GuiScreen guiScreen, Container container) {
        Class<? extends GuiScreen> clazz = guiScreen.getClass();
        if(isAPIClass(clazz)) {
            Method m = getAnnotatedMethod(clazz, new Class[] { ContainerGUI.ContainerSectionCallback.class, InventoryGUI.ContainerSectionCallback.class }, 0, Map.class);
            if(m != null) {
                try {
                    return (Map<ContainerSection, List<Slot>>)m.invoke(guiScreen);
                } catch(Exception e) {
                    // TODO: Do something here to tell mod authors they're doing it wrong.
                }
            }
        }

    	Map<ContainerSection, List<Slot>> result = new HashMap<ContainerSection, List<Slot>>();
		List<Slot> slots = (List<Slot>) obf.getSlots(container);

    	if (is(guiScreen, "GuiCondenser")) { // EE
    		result.put(ContainerSection.CHEST, slots.subList(1, slots.size() - 36));
    	}
    	else if (is(guiScreen, "GuiAdvBench")) { // RedPower 2
            result.put(ContainerSection.CRAFTING_IN_PERSISTENT, slots.subList(0, 9));
            result.put(ContainerSection.CRAFTING_OUT, slots.subList(10, 11));
            result.put(ContainerSection.CHEST, slots.subList(11, 29));
    	} else if(is(guiScreen, "GuiArcaneWorkbench") || is(guiScreen, "GuiInfusionWorkbench")) { // Thaumcraft 3
            result.put(ContainerSection.CRAFTING_OUT, slots.subList(0, 1));
            result.put(ContainerSection.CRAFTING_IN_PERSISTENT, slots.subList(2, 11));
    	}

		return result;

	}

	private static final boolean is(GuiScreen guiScreen, String className) {
	    try {
	        return guiScreen.getClass().getSimpleName().contains(className);
	    }
	    catch (Exception e) {
	        return false;
	    }
    }

    private static final ContainerGUI getContainerGUIAnnotation(Class<? extends GuiScreen> clazz) {
        ContainerGUI annotation = clazz.getAnnotation(ContainerGUI.class);
        return annotation;
    }

    private static final InventoryGUI getInventoryGUIAnnotation(Class<? extends GuiScreen> clazz) {
        InventoryGUI annotation = clazz.getAnnotation(InventoryGUI.class);
        return annotation;
    }

    private static final boolean isAPIClass(Class<? extends GuiScreen> clazz) {
        return (getContainerGUIAnnotation(clazz) != null) || (getInventoryGUIAnnotation(clazz) != null);
    }

    private static final Method getAnnotatedMethod(Class clazz, Class[] annotations, int numParams, Class retClass) {
        Method[] methods = clazz.getMethods();
        for(Method m : methods) {
            for(Class annotation : annotations) {
                if(m.getAnnotation(annotation) != null) {
                    if(m.getParameterTypes().length == numParams && retClass.isAssignableFrom(m.getReturnType())) {
                        return m;
                    }
                }
            }
        }
        return null;
    }
}
