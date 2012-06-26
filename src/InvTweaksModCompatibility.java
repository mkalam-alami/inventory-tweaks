import invtweaks.InvTweaksConst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InvTweaksModCompatibility {
	
    private InvTweaksObfuscation obf;
    
    InvTweaksModCompatibility(InvTweaksObfuscation obf) {
    	this.obf = obf;
    }
    
    /**
     * Returns true if the screen is a chest/dispenser,
     * despite not being a GuiChest or a GuiDispenser.
     * @param guiContainer
     * @return
     */
    public boolean isSpecialChest(vp guiScreen) {
        return is(guiScreen, "GuiAlchChest") // Equivalent Exchange
        		|| is(guiScreen, "GuiCondenser")
        		|| is(guiScreen, "GUIChest") // Iron chests (formerly IC2)
                || is(guiScreen, "GuiMultiPageChest") // Multi Page chest
                || is(guiScreen, "GuiGoldSafe") // More Storage
                || is(guiScreen, "GuiLocker")
                || is(guiScreen, "GuiDualLocker")
                || is(guiScreen, "GuiSafe") 
                || is(guiScreen, "GuiCabinet") 
                || is(guiScreen, "GuiTower")
                || is(guiScreen, "GuiBufferChest") // Red Power 2
                || is(guiScreen, "GuiRetriever")
                || is(guiScreen, "GuiItemDetect")
                || is(guiScreen, "GuiAlloyFurnace")
                || is(guiScreen, "GuiDeploy")
                || is(guiScreen, "GuiSorter")
                || is(guiScreen, "GuiFilter")
                || is(guiScreen, "GuiAdvBench")
                || is(guiScreen, "GuiEject")
                || is(guiScreen, "GuiNuclearReactor") // IC2
                || is(guiScreen, "GuiEnderChest") // EnderChest
          ;
    }

    /**
     * Returns a special chest row size.
     * Given guiContainer must be checked first with isSpecialChest().
     * @param guiContainer
     * @param defaultValue
     * @return
     */
    public int getSpecialChestRowSize(gb guiContainer, int defaultValue) {
    	if (is(guiContainer, "GuiAlchChest")
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

    public boolean isChestWayTooBig(vp guiScreen) {
        return is(guiScreen, "GuiAlchChest") // Equivalent Exchange
        		|| is(guiScreen, "GuiMultiPageChest") // MultiPage Chest
        		|| is(guiScreen, "GUIChest"); // IronChests
    }
    
    /**
     * Returns true if the screen is the inventory screen, despite not being a GuiInventory.
     * @param guiScreen
     * @return
     */
    public boolean isSpecialInventory(vp guiScreen) {
    	try {
			return obf.getSlots(obf.getContainer((gb) guiScreen)).size() > InvTweaksConst.INVENTORY_SIZE
					&& !obf.isGuiContainerCreative(guiScreen);
		} catch (Exception e) {
			return false;
		}
    }

	@SuppressWarnings("unchecked")
    public Map<InvTweaksContainerSection, List<yu>> getSpecialContainerSlots(vp guiScreen, dd container) {
    	
    	Map<InvTweaksContainerSection, List<yu>> result = new HashMap<InvTweaksContainerSection, List<yu>>();
		List<yu> slots = (List<yu>) obf.getSlots(container);
    	
    	if (is(guiScreen, "GuiCondenser")) { // EE
    		result.put(InvTweaksContainerSection.CHEST, slots.subList(1, slots.size() - 36));
    	}
    	else if (is(guiScreen, "GuiAdvBench")) { // RedPower 2
    		result.put(InvTweaksContainerSection.CRAFTING_IN, slots.subList(0, 9));
    		result.put(InvTweaksContainerSection.CRAFTING_OUT, slots.subList(9, 10));
    		result.put(InvTweaksContainerSection.CHEST, slots.subList(10, 28));
    	}
    	
		return result;
		
	}

	private static final boolean is(vp guiScreen, String className) {
        return guiScreen.getClass().getSimpleName().contains(className);
    }


}