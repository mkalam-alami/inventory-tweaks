package net.invtweaks.tree;

import Obfuscation;
import net.minecraft.src.ItemStack;

/**
 * Representation of an item in the item tree.
 * @author Jimeo Wan
 *
 */
public class ItemTreeItem extends Obfuscation implements Comparable<ItemTreeItem> {

    private String name;
    private int id;
    private int damage;
    private int order;

    /**
     * @param name The item name
     * @param id The item ID
     * @param damage The item variant or -1
     * @param order The item order while sorting
     */
    public ItemTreeItem(String name, int id, int damage, int order) {
        super(null);
        this.name = name;
        this.id = id;
        this.damage = damage;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getDamage() {
        return damage;
    }

    public int getOrder() {
        return order;
    }

    public boolean matchesStack(ItemStack stack) {
        return getItemID(stack) == id && 
                (getMaxStackSize(stack) == 1 || getItemDamage(stack) == damage);
    }

    /**
     * Warning: the item equality is not reflective. They are equal if "o"
     * matches the item constraints (the opposite can be false).
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ItemTreeItem))
            return false;
        ItemTreeItem item = (ItemTreeItem) o;
        return id == item.getId() && (damage == -1 || damage == item.getDamage());
    }

    public String toString() {
        return name;
    }

    @Override
    public int compareTo(ItemTreeItem item) {
        return item.order - order;
    }

}
