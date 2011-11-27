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
     * Returns true if the container is a chest/dispenser,
     * despite not being a GuiChest or a GuiDispenser.
     * @param guiContainer
     * @return
     */
    public boolean isSpecialChest(em guiContainer) {
        return is(guiContainer, "GuiAlchest") // Equivalent Exchange
          || is(guiContainer, "GuiDiamondChest")
          || is(guiContainer, "GuiGoldChest"); // Iron chests (IC2)
    }

    /**
     * Returns a special chest row size.
     * Given guiContainer must be checked first with isSpecialChest().
     * @param guiContainer
     * @param defaultValue
     * @return
     */
    public int getSpecialChestRowSize(em guiContainer, int defaultValue) {
        if (is(guiContainer, "GuiAlchest")) { // Equivalent Exchange
            return 13;
        } else if (is(guiContainer, "GuiDiamondChest")) { //Iron chests
          return 12;
        } else if (is(guiContainer, "GuiGoldChest")) {
          return 9;
        }
        return defaultValue;
    }
    
    /**
     * Returns true if the container is the inventory screen,
     * despite not being a GuiInventory.
     * @param guiContainer
     * @return
     */
    public boolean isSpecialInventory(em guiContainer) {
        return is(guiContainer, "GuiInventoryMoreSlots"); // Aether mod
    }

    /**
     * Returns true if the guiScreen is a GuiChest or a GuiDispenser
     * but should not be handled for sorting/shortcuts.
     * @param guiContainer
     * @return
     */
    public boolean isForbiddenChest(em guiContainer) {
        return is(guiContainer, "MLGuiChestBuilding"); // Millenaire mod
    }
    
    private static final boolean is(em guiContainer, String className) {
        return guiContainer.getClass().getSimpleName().contains(className);
    }


}
