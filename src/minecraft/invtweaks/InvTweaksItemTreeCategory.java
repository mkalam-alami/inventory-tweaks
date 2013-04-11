package invtweaks;


import invtweaks.api.IItemTreeCategory;

import java.util.*;
import java.util.logging.Logger;

/**
 * Representation of a category in the item tree, i.e. a group of items.
 *
 * @author Jimeo Wan
 */
public class InvTweaksItemTreeCategory implements IItemTreeCategory {

    private static final Logger log = InvTweaks.log;

    private final Map<Integer, List<InvTweaksItemTreeItem>> items =
            new HashMap<Integer, List<InvTweaksItemTreeItem>>();
    private final Vector<String> matchingItems = new Vector<String>();
    private final Vector<InvTweaksItemTreeCategory> subCategories =
            new Vector<InvTweaksItemTreeCategory>();
    private String name;
    private int order = -1;

    public InvTweaksItemTreeCategory(String name) {
        this.name = (name != null) ? name.toLowerCase() : null;
    }

    @Override
    public boolean contains(InvTweaksItemTreeItem item) {
        List<InvTweaksItemTreeItem> storedItems = items.get(item.getId());
        if (storedItems != null) {
            for (InvTweaksItemTreeItem storedItem : storedItems) {
                if (storedItem.equals(item))
                    return true;
            }
        }
        for (InvTweaksItemTreeCategory category : subCategories) {
            if (category.contains(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addCategory(InvTweaksItemTreeCategory category) {
        subCategories.add(category);
    }

    @Override
    public void addItem(InvTweaksItemTreeItem item) {

        // Add item to category
        if (items.get(item.getId()) == null) {
            List<InvTweaksItemTreeItem> itemList = new ArrayList<InvTweaksItemTreeItem>();
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
            for (InvTweaksItemTreeCategory category : subCategories) {
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
            for (InvTweaksItemTreeCategory category : subCategories) {
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
            for (InvTweaksItemTreeCategory category : subCategories) {
                result = category.findKeywordDepth(keyword);
                if (result != -1) {
                    return result + 1;
                }
            }
            return -1;
        }
    }

    /**
     * @return all categories contained in this one.
     */
    @Override
    public Collection<InvTweaksItemTreeCategory> getSubCategories() {
        return subCategories;
    }

    @Override
    public Collection<List<InvTweaksItemTreeItem>> getItems() {
        return items.values();
    }

    @Override
    public String getName() {
        return name;
    }

    public String toString() {
        return name + " (" + subCategories.size() +
                " cats, " + items.size() + " items)";
    }

}
