


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Contains the whole hierarchy of categories and items, as defined
 * in the XML item tree. Is used to recognize keywords and store
 * item orders.
 * @author Jimeo Wan
 *
 */
public class InvTweaksItemTree {

    public static final int MAX_CATEGORY_RANGE = 1000;
    public static final String UNKNOWN_ITEM = "unknown";

    private static final Logger log = Logger.getLogger("InvTweaks");

    /** All categories, stored by name */
    private Map<String, InvTweaksItemTreeCategory> categories = 
        new HashMap<String, InvTweaksItemTreeCategory>();

    /** Items stored by ID. A same ID can hold several names. */
    private Map<Integer, Vector<InvTweaksItemTreeItem>> itemsById = 
        new HashMap<Integer, Vector<InvTweaksItemTreeItem>>(500);
    private static Vector<InvTweaksItemTreeItem> defaultItems = null;

    /** Items stored by name. A same name can match several IDs. */
    private Map<String, Vector<InvTweaksItemTreeItem>> itemsByName = 
        new HashMap<String, Vector<InvTweaksItemTreeItem>>(500);

    private String rootCategory;

    public InvTweaksItemTree() {
        reset();
    }

    public void reset() {

        if (defaultItems == null) {
            defaultItems = new Vector<InvTweaksItemTreeItem>();
            defaultItems.add(new InvTweaksItemTreeItem(UNKNOWN_ITEM, -1, -1, Integer.MAX_VALUE));
        }

        // Reset tree
        categories.clear();
        itemsByName.clear();
        itemsById.clear();

    }

    /**
     * Checks it given item ID matches a given keyword (either the item's name
     * is the keyword, or it is in the keyword category)
     * 
     * @param item
     * @param keyword
     * @return
     */
    public boolean matches(List<InvTweaksItemTreeItem> items, String keyword) {

        if (items == null)
            return false;

        // The keyword is an item
        for (InvTweaksItemTreeItem item : items) {
            if (item.getName().equals(keyword)) {
                return true;
            }
        }

        // The keyword is a category
        InvTweaksItemTreeCategory category = getCategory(keyword);
        if (category != null) {
            for (InvTweaksItemTreeItem item : items) {
                if (category.contains(item)) {
                    return true;
                }
            }
        }
        
        // Everything is stuff
        if (keyword.equals(rootCategory)) {
            return true;
        }

        return false;
    }

    public int getKeywordDepth(String keyword) {
        try {
            return getRootCategory().findKeywordDepth(keyword);
        } catch (NullPointerException e) {
            log.severe("The root category is missing: " + e.getMessage());
            return 0;
        }
    }

    public int getKeywordOrder(String keyword) {
        List<InvTweaksItemTreeItem> items = getItems(keyword);
        if (items != null && items.size() != 0) {
            return items.get(0).getOrder();
        } else {
            try {
                return getRootCategory().findCategoryOrder(keyword);
            } catch (NullPointerException e) {
                log.severe("The root category is missing: " + e.getMessage());
                return -1;
            }
        }
    }

    /**
     * Checks if the given keyword is valid (i.e. represents either a registered
     * item or a registered category)
     * 
     * @param keyword
     * @return
     */
    public boolean isKeywordValid(String keyword) {

        // Is the keyword an item?
        if (containsItem(keyword)) {
            return true;
        }

        // Or maybe a category ?
        else {
            InvTweaksItemTreeCategory category = getCategory(keyword);
            return category != null;
        }
    }

    /**
     * Returns a reference to all categories.
     */
    public Collection<InvTweaksItemTreeCategory> getAllCategories() {
        return categories.values();
    }

    public InvTweaksItemTreeCategory getRootCategory() {
        return categories.get(rootCategory);
    }

    public InvTweaksItemTreeCategory getCategory(String keyword) {
        return categories.get(keyword);
    }
    
    public boolean isItemUnknown(int id, int damage) {
        return itemsById.get(id) == null; 
    }

    public List<InvTweaksItemTreeItem> getItems(int id, int damage) {
        List<InvTweaksItemTreeItem> items = itemsById.get(id);
        List<InvTweaksItemTreeItem> filteredItems = null;
        if (items != null) {
            // Filter items of same ID, but different damage value
            for (InvTweaksItemTreeItem item : items) {
                if (item.getDamage() != -1 && item.getDamage() != damage) {
                    if (filteredItems == null) {
                        filteredItems = new ArrayList<InvTweaksItemTreeItem>(items);
                    }
                    filteredItems.remove(item);
                }
            }
            if (filteredItems !=null && filteredItems.isEmpty()) {
              //Generate a dummy if I've run out of items
              InvTweaksItemTreeItem newItem=new InvTweaksItemTreeItem(String.format("UnknownItem-%d-%d",id,damage),id,damage,id*16+damage);
              addItem(getRootCategory().getName(),newItem);
              return getItems(id,damage);
            }
            return (filteredItems != null && !filteredItems.isEmpty())
                    ? filteredItems : items;
        } else {
            // Generate a dummy item at the root for sorting 
            InvTweaksItemTreeItem newItem=new InvTweaksItemTreeItem(String.format("UnknownItem-%d-%d",id,damage),id,damage,id*16+damage);
            addItem(getRootCategory().getName(),newItem);
            return getItems(id,damage);
        }
    }

    public List<InvTweaksItemTreeItem> getItems(String name) {
        return itemsByName.get(name);
    }

    public InvTweaksItemTreeItem getRandomItem(Random r) {
        return (InvTweaksItemTreeItem) itemsByName.values()
                .toArray()[r.nextInt(itemsByName.size())];
    }

    public boolean containsItem(String name) {
        return itemsByName.containsKey(name);
    }

    public boolean containsCategory(String name) {
        return categories.containsKey(name);
    }

    protected void setRootCategory(InvTweaksItemTreeCategory category) {
        rootCategory = category.getName();
        categories.put(rootCategory, category);
    }

    protected void addCategory(String parentCategory, 
            InvTweaksItemTreeCategory newCategory) throws NullPointerException {
        // Build tree
        categories.get(parentCategory.toLowerCase()).addCategory(newCategory);

        // Register category
        categories.put(newCategory.getName(), newCategory);
    }

    protected void addItem(String parentCategory,
            InvTweaksItemTreeItem newItem) throws NullPointerException {
        // Build tree
        categories.get(parentCategory.toLowerCase()).addItem(newItem);

        // Register item
        if (itemsByName.containsKey(newItem.getName())) {
            itemsByName.get(newItem.getName()).add(newItem);
        } else {
            Vector<InvTweaksItemTreeItem> list = new Vector<InvTweaksItemTreeItem>();
            list.add(newItem);
            itemsByName.put(newItem.getName(), list);
        }
        if (itemsById.containsKey(newItem.getId())) {
            itemsById.get(newItem.getId()).add(newItem);
        } else {
            Vector<InvTweaksItemTreeItem> list = new Vector<InvTweaksItemTreeItem>();
            list.add(newItem);
            itemsById.put(newItem.getId(), list);
        }
    }

    /**
     * For debug purposes. Call log(getRootCategory(), 0) to log the whole tree.
     */
    @SuppressWarnings("unused")
    private void log(InvTweaksItemTreeCategory category, int indentLevel) {

        String logIdent = "";
        for (int i = 0; i < indentLevel; i++) {
            logIdent += "  ";
        }
        log.info(logIdent + category.getName());

        for (InvTweaksItemTreeCategory subCategory : category.getSubCategories()) {
            log(subCategory, indentLevel + 1);
        }

        for (List<InvTweaksItemTreeItem> itemList : category.getItems()) {
            for (InvTweaksItemTreeItem item : itemList) {
                log.info(logIdent + "  " + item + " " + 
                        item.getId() + " " + item.getDamage());
            }
        }

    }

}
