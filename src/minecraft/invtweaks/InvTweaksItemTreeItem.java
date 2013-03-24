package invtweaks;

/**
 * Representation of an item in the item tree.
 *
 * @author Jimeo Wan
 */
public class InvTweaksItemTreeItem implements Comparable<InvTweaksItemTreeItem> {

    private String name;
    private int id;
    private int damage;
    private int order;

    /**
     * @param name   The item name
     * @param id     The item ID
     * @param damage The item variant or InvTweaksConst.DAMAGE_WILDCARD
     * @param order  The item order while sorting
     */
    public InvTweaksItemTreeItem(String name, int id, int damage, int order) {
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

    /**
     * Warning: the item equality is not reflective. They are equal if "o"
     * matches the item constraints (the opposite can be false).
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof InvTweaksItemTreeItem))
            return false;
        InvTweaksItemTreeItem item = (InvTweaksItemTreeItem) o;
        return id == item.getId() && (damage == InvTweaksConst.DAMAGE_WILDCARD || damage == item.getDamage());
    }

    public String toString() {
        return name;
    }

    @Override
    public int compareTo(InvTweaksItemTreeItem item) {
        return item.order - order;
    }

}
