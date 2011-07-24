package net.invtweaks.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

public class InvTweaksTree {

    public static final int MAX_CATEGORY_RANGE = 1000;
	
    private static final Logger log = Logger.getLogger("InvTweaks");
    
    /** All categories, stored by name */
	private Map<String, InvTweaksCategory> categories =
		new HashMap<String, InvTweaksCategory>();

	/** Items stored by ID. A same ID can hold several names. */
	private Map<Integer, Vector<InvTweaksItem>> itemsById = 
		new HashMap<Integer, Vector<InvTweaksItem>>(500);
	private static Vector<InvTweaksItem> defaultItems = null;
	
	/** Items stored by name. A same name can match several IDs. */
	private Map<String, Vector<InvTweaksItem>> itemsByName =
		new HashMap<String, Vector<InvTweaksItem>>(500);

	private String rootCategory;
	
	public InvTweaksTree() {
		reset();
	}
	
	public void reset() {

		if (defaultItems == null) {
			defaultItems = new Vector<InvTweaksItem>();
			defaultItems.add(
					new InvTweaksItem("unknown", -1, -1, Integer.MAX_VALUE));
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
	public boolean matches(List<InvTweaksItem> items, String keyword) {

		if (items == null)
			return false;
		
		// The keyword is an item
		for (InvTweaksItem item : items) {
			if (item.getName().equals(keyword)) {
				return true;
			}
		}
		
		// The keyword is a category
		InvTweaksCategory category = getCategory(keyword);
		if (category != null) {
			for (InvTweaksItem item : items) {
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
		List<InvTweaksItem> items = getItems(keyword);
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
			InvTweaksCategory category = getCategory(keyword);
			return category != null;
		}
	}

	/**
	 * Returns a reference to all categories.
	 */
	public Collection<InvTweaksCategory> getAllCategories() {
		return categories.values();
	}

	public InvTweaksCategory getRootCategory() {
		return categories.get(rootCategory);
	}

	public InvTweaksCategory getCategory(String keyword) {
		return categories.get(keyword);
	}

	public List<InvTweaksItem> getItems(int id, int damage) {
		List<InvTweaksItem> items = itemsById.get(id);
		List<InvTweaksItem> filteredItems = null;
		if (items != null) {
			// Filter items of same ID, but different damage value
			for (InvTweaksItem item : items) {
				if (item.getDamage() != -1 && item.getDamage() != damage) {
					if (filteredItems == null) {
						filteredItems = new ArrayList<InvTweaksItem>(items);
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

	public List<InvTweaksItem> getItems(String name) {
		return itemsByName.get(name);
	}

	public InvTweaksItem getRandomItem(Random r) {
		return (InvTweaksItem) itemsByName.values().
				toArray()[r.nextInt(itemsByName.size())];
	}

	public boolean containsItem(String name) {
		return itemsByName.containsKey(name);
	}
	
	public boolean containsCategory(String name) {
		return categories.containsKey(name);
	}

	protected void setRootCategory(InvTweaksCategory category) {
		rootCategory = category.getName();
		categories.put(rootCategory, category);
	}

	protected void addCategory(String parentCategory,
			InvTweaksCategory newCategory) throws NullPointerException {
		// Build tree
		categories.get(parentCategory).addCategory(newCategory);
		
		// Register category
		categories.put(newCategory.getName(), newCategory);
	}

	protected void addItem(String parentCategory,
			InvTweaksItem newItem) throws NullPointerException {
		// Build tree
		categories.get(parentCategory).addItem(newItem);
		
		// Register item
		if (itemsByName.containsKey(newItem.getName())) {
			itemsByName.get(newItem.getName()).add(newItem);
		}
		else {
			Vector<InvTweaksItem> list = 
				new Vector<InvTweaksItem>();
			list.add(newItem);
			itemsByName.put(newItem.getName(), list);
		}
		if (itemsById.containsKey(newItem.getId())) {
			itemsById.get(newItem.getId()).add(newItem);
		}
		else {
			Vector<InvTweaksItem> list = 
				new Vector<InvTweaksItem>();
			list.add(newItem);
			itemsById.put(newItem.getId(), list);
		}
	}
	
	/**
	 * For debug purposes.
	 * Call log(getRootCategory(), 0) to log the whole tree.
	 */
	@SuppressWarnings("unused")
	private void log(InvTweaksCategory category, int indentLevel) {
		
		String logIdent = "";
		for (int i = 0; i < indentLevel; i++) {
			logIdent += "  ";
		}
		log.info(logIdent + category.getName());
		
		for (InvTweaksCategory subCategory : category.getSubCategories()) {
			log(subCategory, indentLevel + 1);
		}
		
		for (List<InvTweaksItem> itemList : category.getItems()) {
			for (InvTweaksItem item : itemList) {
				log.info(logIdent + "  " + item + " " + 
						item.getId() + " " + item.getDamage());
			}
		}
		
	}
	
}