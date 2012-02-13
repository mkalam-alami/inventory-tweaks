
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
    public boolean isSpecialChest(ug guiScreen) {
        return is(guiScreen, "GuiAlchChest") // Equivalent Exchange
        		|| is(guiScreen, "GuiDiamondChest") // Iron chests (IC2)
                || is(guiScreen, "GuiGoldChest") 
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
                || is(guiScreen, "GuiNuclearReactor") // IC²
          ;
    }

    /**
     * Returns a special chest row size.
     * Given guiContainer must be checked first with isSpecialChest().
     * @param guiContainer
     * @param defaultValue
     * @return
     */
    public int getSpecialChestRowSize(ft guiContainer, int defaultValue) {
    	if (is(guiContainer, "GuiAlchChest")) { // Equivalent Exchange
            return 13;
        } else if (is(guiContainer, "GuiDiamondChest")) { // Iron chests (IC2)
          return 12;
        } else if (is(guiContainer, "GuiGoldChest")) {
          return 9;
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
	    		|| is(guiContainer, "GuiFilter")) {
	      return 3;
	    } else if (is(guiContainer, "GuiNuclearReactor")) { // IC²
	    	return (obf.getSlots(obf.getContainer(guiContainer)).size() - 36) / 6;
	    }
        return defaultValue;
    }

    public boolean isChestWayTooBig(ug guiScreen) {
        return is(guiScreen, "GuiAlchChest")
        		|| is(guiScreen, "GuiMultiPageChest");
    }
    
    /**
     * Returns true if the screen is the inventory screen, despite not being a GuiInventory.
     * @param guiScreen
     * @return
     */
    public boolean isSpecialInventory(ug guiScreen) {
        return is(guiScreen, "GuiInventoryMoreSlots"); // Aether mod
    }
    
    private static final boolean is(ug guiScreen, String className) {
        return guiScreen.getClass().getSimpleName().contains(className);
    }


}