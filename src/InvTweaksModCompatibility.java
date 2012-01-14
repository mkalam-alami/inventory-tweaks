public class InvTweaksModCompatibility {
    
    private static InvTweaksModCompatibility instance = null;
    
    private InvTweaksModCompatibility() {
        
    }
    
    public static InvTweaksModCompatibility getInstance() {
        if (instance == null) {
            instance = new InvTweaksModCompatibility();
        }
        return instance;
    }
    
    /**
     * Returns true if the screen is a chest/dispenser,
     * despite not being a GuiChest or a GuiDispenser.
     * @param guiContainer
     * @return
     */
    public boolean isSpecialChest(ug guiScreen) {
        return is(guiScreen, "GuiAlchest") // Equivalent Exchange
          || is(guiScreen, "GuiDiamondChest") // Iron chests (IC2)
          || is(guiScreen, "GuiGoldChest") // Iron chests (IC2)
          || is(guiScreen, "GuiMultiPageChest") // Multi Page chest
          || is(guiScreen, "GuiGoldSafe") // More Storage
          || is(guiScreen, "GuiLocker") // More Storage
          || is(guiScreen, "GuiDualLocker") // More Storage
          || is(guiScreen, "GuiSafe") // More Storage
          || is(guiScreen, "GuiCabinet") // More Storage
          || is(guiScreen, "GuiTower") // More Storage
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
        if (is(guiContainer, "GuiAlchest")) { // Equivalent Exchange
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
	    }
        return defaultValue;
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
