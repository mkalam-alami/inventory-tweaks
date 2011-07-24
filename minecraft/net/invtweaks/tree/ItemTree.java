package net.invtweaks.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

public class ItemTree {

    public static final int MAX_CATEGORY_RANGE = 1000;
	
    private static final Logger log = Logger.getLogger("InvTweaks");
    
    /** All categories, stored by name */
	private Map<String, ItemTreeCategory> categories =
		new HashMap<String, ItemTreeCategory>();

	/** Items stored by ID. A same ID can hold several names. */
	private Map<Integer, Vector<ItemTreeItem>> itemsById = 
		new HashMap<Integer, Vector<ItemTreeItem>>(500);
	private static Vector<ItemTreeItem> defaultItems = null;
	
	/** Items stored by name. A same name can match several IDs. */
	private Map<String, Vector<ItemTreeItem>> itemsByName =
		new HashMap<String, Vector<ItemTreeItem>>(500);

	private String rootCategory;
	
	public ItemTree() {
		reset();
	}
	
	public void reset() {

		if (defaultItems == null) {
			defaultItems = new Vector<ItemTreeItem>();
			defaultItems.add(
					new ItemTreeItem("unknown", -1, -1, Integer.MAX_VALUE));
		}
		
		// Reset tree
		categories.clear();
		itemsByName.clear();
		itemsById.clear();
		
	}

	/**
	 * Checks it given item ID matches a given keyword
	 * (either the item's name is the keyword, or it is
	 * in the keyword category)
	 * @param item
	 * @param keyword
	 * @return
	 */
	public boolean matches(List<ItemTreeItem> items, String keyword) {

		if (items == null)
			return false;
		
		// The keyword is an item
		for (ItemTreeItem item : items) {
			if (item.getName().equals(keyword)) {
				return true;
			}
		}
		
		// The keyword is a category
		ItemTreeCategory category = getCategory(keyword);
		if (category != null) {
			for (ItemTreeItem item : items) {
				if (category.contains(item)) {
					return true;
				}
			}
		}
		
		return false;
	}

	public int getKeywordDepth(String keyword) {
		try {
			return getRootCategory().findKeywordDepth(keyword);
		}
		catch (NullPointerException e) {
			log.severe("The root category is missing: " + e.getMessage());
			return 0;
		}
	}
	
	public int getKeywordOrder(String keyword) {
		List<ItemTreeItem> items = getItems(keyword);
		if (items != null && items.size() != 0) {
			return items.get(0).getOrder();
		}
		else {
			try {
				return getRootCategory().findCategoryOrder(keyword);
			}
			catch (NullPointerException e) {
				log.severe("The root category is missing: " + e.getMessage());
				return -1;
			}
		}
	}
	
	/**
	 * Checks if the given keyword is valid (i.e. represents either
	 * a registered item or a registered category)
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
			ItemTreeCategory category = getCategory(keyword);
			return category != null;
		}
	}

	/**
	 * Returns a reference to all categories.
	 */
	public Collection<ItemTreeCategory> getAllCategories() {
		return categories.values();
	}

	public ItemTreeCategory getRootCategory() {
		return categories.get(rootCategory);
	}

	public ItemTreeCategory getCategory(String keyword) {
		return categories.get(keyword);
	}

	public List<ItemTreeItem> getItems(int id, int damage) {
		List<ItemTreeItem> items = itemsById.get(id);
		List<ItemTreeItem> filteredItems = null;
		if (items != null) {
			// Filter items of same ID, but different damage value
			for (ItemTreeItem item : items) {
				if (item.getDamage() != -1 && item.getDamage() != damage) {
					if (filteredItems == null) {
						filteredItems = new ArrayList<ItemTreeItem>(items);
					}
					filteredItems.remove(item);
				}
			}
			return (filteredItems != null && !filteredItems.isEmpty()) ? filteredItems : items;
		}
		else {
			log.warning("Unknown item id: "+id);
			return defaultItems;
		}
	}

	public List<ItemTreeItem> getItems(String name) {
		return itemsByName.get(name);
	}

	public ItemTreeItem getRandomItem(Random r) {
		return (ItemTreeItem) itemsByName.values().
				toArray()[r.nextInt(itemsByName.size())];
	}

	public boolean containsItem(String name) {
		return itemsByName.containsKey(name);
	}
	
	public boolean containsCategory(String name) {
		return categories.containsKey(name);
	}

	protected void setRootCategory(ItemTreeCategory category) {
		rootCategory = category.getName();
		categories.put(rootCategory, category);
	}

	protected void addCategory(String parentCategory,
			ItemTreeCategory newCategory) throws NullPointerException {
		// Build tree
		categories.get(parentCategory).addCategory(newCategory);
		
		// Register category
		categories.put(newCategory.getName(), newCategory);
	}

	protected void addItem(String parentCategory,
			ItemTreeItem newItem) throws NullPointerException {
		// Build tree
		categories.get(parentCategory).addItem(newItem);
		
		// Register item
		if (itemsByName.containsKey(newItem.getName())) {
			itemsByName.get(newItem.getName()).add(newItem);
		}
		else {
			Vector<ItemTreeItem> list = 
				new Vector<ItemTreeItem>();
			list.add(newItem);
			itemsByName.put(newItem.getName(), list);
		}
		if (itemsById.containsKey(newItem.getId())) {
			itemsById.get(newItem.getId()).add(newItem);
		}
		else {
			Vector<ItemTreeItem> list = 
				new Vector<ItemTreeItem>();
			list.add(newItem);
			itemsById.put(newItem.getId(), list);
		}
	}
	
	/**
	 * For debug purposes.
	 * Call log(getRootCategory(), 0) to log the whole tree.
	 */
	@SuppressWarnings("unused")
	private void log(ItemTreeCategory category, int indentLevel) {
		
		String logIdent = "";
		for (int i = 0; i < indentLevel; i++) {
			logIdent += "  ";
		}
		log.info(logIdent + category.getName());
		
		for (ItemTreeCategory subCategory : category.getSubCategories()) {
			log(subCategory, indentLevel + 1);
		}
		
		for (List<ItemTreeItem> itemList : category.getItems()) {
			for (ItemTreeItem item : itemList) {
				log.info(logIdent + "  " + item + " " + 
						item.getId() + " " + item.getDamage());
			}
		}
		
	}
	
}