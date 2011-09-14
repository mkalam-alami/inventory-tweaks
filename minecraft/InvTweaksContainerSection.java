public enum InvTweaksContainerSection {
    /** The player's inventory */
    INVENTORY,
    /** The player's inventory (only the hotbar) */
    INVENTORY_HOTBAR,
    /** The player's inventory (all except the hotbar) */
    INVENTORY_NOT_HOTBAR,
    /** The chest or dispenser contents */
    CHEST,
    /** The crafting input */
    CRAFTING_IN,
    /** The crafting output */
    CRAFTING_OUT,
    /** The armor slots */
    ARMOR,
    /** The furnace input */
    FURNACE_IN,
    /** The furnace output */
    FURNACE_OUT,
    /** The furnace fuel */
    FURNACE_FUEL,
    /**
     * Any other type of slot. For unknown container types (such as mod
     * containers), only INVENTORY and OTHER sections are defined.
     */
    UNKNOWN
}