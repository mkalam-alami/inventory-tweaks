package net.invtweaks.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Representation of a category in the item tree, i.e. a group of items.
 * @author Jimeo Wan
 *
 */
public class ItemTreeCategory {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("InvTweaks");

    private final Map<Integer, List<ItemTreeItem>> items = 
        new HashMap<Integer, List<ItemTreeItem>>();
    private final Vector<String> matchingItems = new Vector<String>();
    private final Vector<ItemTreeCategory> subCategories = 
        new Vector<ItemTreeCategory>();
    private String name;
    private int order = -1;

    public ItemTreeCategory(String name) {
        this.name = (name != null) ? name.toLowerCase() : null;
    }

    public boolean contains(ItemTreeItem item) {
        List<ItemTreeItem> storedItems = items.get(item.getId());
        if (storedItems != null) {
            for (ItemTreeItem storedItem : storedItems) {
                if (storedItem.equals(item))
                    return true;
            }
        }
        for (ItemTreeCategory category : subCategories) {
            if (category.contains(item)) {
                return true;
            }
        }
        return false;
    }

    public void addCategory(ItemTreeCategory category) {
        subCategories.add(category);
    }

    public void addItem(ItemTreeItem item) {

        // Add item to category
        if (items.get(item.getId()) == null) {
            List<ItemTreeItem> itemList = new ArrayList<ItemTreeItem>();
            itemList.add(item);
            items.put(item.getId(), itemList);
        } else {
            items.get(item.getId()).add(item);
        }
        matchingItems.add(item.getName());

        // Categorie's order is defined by its lowest item order
        if (order == -1 || order > item.getOrder()) {
            order = item.getOrder();
        }
    }

    public int getCategoryOrder() {
        if (this.order != -1) {
            return this.order;
        } else {
            int order;
            for (ItemTreeCategory category : subCategories) {
                order = category.getCategoryOrder();
                if (order != -1)
                    return order;
            }
            return -1;
        }
    }

    public int findCategoryOrder(String keyword) {
        if (keyword.equals(name)) {
            return getCategoryOrder();
        } else {
            int result;
            for (ItemTreeCategory category : subCategories) {
                result = category.findCategoryOrder(keyword);
                if (result != -1) {
                    return result;
                }
            }
            return -1;
        }
    }

    public int findKeywordDepth(String keyword) {
        if (name.equals(keyword)) {
            return 0;
        } else if (matchingItems.contains(keyword)) {
            return 1;
        } else {
            int result;
            for (ItemTreeCategory category : subCategories) {
                result = category.findKeywordDepth(keyword);
                if (result != -1) {
                    return result + 1;
                }
            }
            return -1;
        }
    }

    /**
     * Returns a references to all categories contained in this one.
     * 
     * @return
     */
    public Collection<ItemTreeCategory> getSubCategories() {
        return subCategories;
    }

    public Collection<List<ItemTreeItem>> getItems() {
        return items.values();
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name + " (" + subCategories.size() + 
                " cats, " + items.size() + " items)";
    }

}
