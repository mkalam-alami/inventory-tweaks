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
    public boolean isSpecialChest(aul guiScreen) {
        return is(guiScreen, "GuiAlchChest") // Equivalent Exchange
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
                || is(guiScreen, "GuiPersonalChest")
                || is(guiScreen, "GuiNuclearReactor") // IC2
                || is(guiScreen, "GuiEnderChest") // EnderChest
                || is(guiScreen, "GuiColorBox")
                || is(guiScreen, "GuiLinkedColorBox") // ColorBox
                || is(guiScreen, "FC_GuiChest") // Metallurgy
                || is(guiScreen, "FM_GuiMintStorage") // Metallurgy
                || is(guiScreen, "GuiChestTFC") // TerraFirmaCraft
          ;
    }

    /**
     * Returns a special chest row size.
     * Given guiContainer must be checked first with isSpecialChest().
     * @param guiContainer
     * @param defaultValue
     * @return
     */
    public int getSpecialChestRowSize(avf guiContainer, int defaultValue) {
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

    public boolean isChestWayTooBig(aul guiScreen) {
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
    public boolean isSpecialInventory(aul guiScreen) {
    	try {
			return obf.getSlots(obf.getContainer(obf.asGuiContainer(guiScreen))).size() > InvTweaksConst.INVENTORY_SIZE
					&& !obf.isGuiInventoryCreative(guiScreen);
		} catch (Exception e) {
			return false;
		}
    }

	@SuppressWarnings("unchecked")
    public Map<InvTweaksContainerSection, List<sr>> getSpecialContainerSlots(aul guiScreen, rq container) {
    	
    	Map<InvTweaksContainerSection, List<sr>> result = new HashMap<InvTweaksContainerSection, List<sr>>();
		List<sr> slots = (List<sr>) obf.getSlots(container);
    	
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

	private static final boolean is(aul guiScreen, String className) {
	    try {
	        return guiScreen.getClass().getSimpleName().contains(className);
	    }
	    catch (Exception e) {
	        return false;
	    }
    }


}