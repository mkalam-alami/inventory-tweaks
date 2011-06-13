package net.minecraft.src;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

public class InvTweaksCategory {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("InvTweaksCategory");

	private final Map<Integer, List<InvTweaksItem>> items = new HashMap<Integer, List<InvTweaksItem>>();
	private final Vector<String> matchingItems = new Vector<String>();
	private final Vector<InvTweaksCategory> subCategories = new Vector<InvTweaksCategory>();
	private String name;
	private int order = -1;
	
    public InvTweaksCategory(String name) {
    	this.name = name;
	}
    
    public boolean contains(InvTweaksItem item) {
    	List<InvTweaksItem> storedItems = items.get(item.getId());
		if (storedItems != null) {
			for (InvTweaksItem storedItem : storedItems) {
				if (storedItem.equals(item))
					return true;
			}
		}
		for (InvTweaksCategory category : subCategories) {
			if (category.contains(item)) {
				return true;
			}
		}
		return false;
	}

	public void addCategory(InvTweaksCategory category) {
		subCategories.add(category);
	}
	
	public void addItem(InvTweaksItem item) {
		
		// Add item to category
		if (items.get(item.getId()) == null) {
			List<InvTweaksItem> itemList = new ArrayList<InvTweaksItem>();
			itemList.add(item);
			items.put(item.getId(), itemList);
		}
		else {
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
		}
		else {
			int order;
			for (InvTweaksCategory category : subCategories) {
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
		}
		else {
			int result;
			for (InvTweaksCategory category : subCategories) {
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
		}
		else if (matchingItems.contains(keyword)) {
			return 1;
		}
		else {
			int result;
			for (InvTweaksCategory category : subCategories) {
				result = category.findKeywordDepth(keyword);
				if (result != -1) {
					return result+1;
				}
			}
			return -1;
		}
	}
	
	/**
	 * Returns a references to all categories contained in this one.
	 * @return
	 */
	public Collection<InvTweaksCategory> getSubCategories() {
		return subCategories;
	}
	
	public Collection<List<InvTweaksItem>> getItems() {
		return items.values();
	}

	public String getName() {
		return name;
	}
}
