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
    public boolean isSpecialChest(mg guiContainer) {
        return is(guiContainer, "GuiAlchest"); // Equivalent Exchange
    }

    /**
     * Returns a special chest row size.
     * Given guiContainer must be checked first with isSpecialChest().
     * @param guiContainer
     * @param defaultValue
     * @return
     */
    public int getSpecialChestRowSize(mg guiContainer, int defaultValue) {
        if (is(guiContainer, "GuiAlchest")) { // Equivalent Exchange
            return 13;
        }
        return defaultValue;
    }
    
    /**
     * Returns true if the container is the inventory screen,
     * despite not being a GuiInventory.
     * @param guiContainer
     * @return
     */
    public boolean isSpecialInventory(mg guiContainer) {
        return is(guiContainer, "GuiInventoryMoreSlots"); // Aether mod
    }

    /**
     * Returns true if the guiScreen is a GuiChest or a GuiDispenser
     * but should not be handled for sorting/shortcuts.
     * @param guiContainer
     * @return
     */
    public boolean isForbiddenChest(mg guiContainer) {
        return is(guiContainer, "MLGuiChestBuilding"); // Millenaire mod
    }
    
    private static final boolean is(mg guiContainer, String className) {
        return guiContainer.getClass().getSimpleName().contains(className);
    }


}
