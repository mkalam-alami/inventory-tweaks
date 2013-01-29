package invtweaks.api;

/**
 * Names for specific parts of containers.
 * For unknown container types (such as mod containers),
 * only INVENTORY and CHEST sections are available.
 */
public enum ContainerSection {
    /** The player's inventory */
    INVENTORY,
    /** The player's inventory (only the hotbar) */
    INVENTORY_HOTBAR,
    /** The player's inventory (all except the hotbar) */
    INVENTORY_NOT_HOTBAR,
    /** The chest or dispenser contents.
     * Also used for unknown container contents. */
    CHEST,
    /** The crafting input */
    CRAFTING_IN,
    /** The crafting input, for containters that store it internally */
    CRAFTING_IN_PERSISTENT,
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
    /** The enchantment table slot */
    ENCHANTMENT,
    /** The three bottles slots in brewing tables */
    BREWING_BOTTLES,
    /** The top slot in brewing tables */
    BREWING_INGREDIENT
}
