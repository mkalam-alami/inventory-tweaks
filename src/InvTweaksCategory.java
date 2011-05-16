package net.minecraft.src;

import java.util.Collection;
import java.util.Vector;
import java.util.logging.Logger;

public class InvTweaksCategory {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("InvTweaksCategory");

	private final Vector<Integer> itemIds = new Vector<Integer>();
	private final Vector<InvTweaksItem> items = new Vector<InvTweaksItem>();
	private final Vector<InvTweaksCategory> subCategories = new Vector<InvTweaksCategory>();
	private String name;
	
    public InvTweaksCategory(String name) {
    	this.name = name;
	}
    
    public boolean contains(InvTweaksItem item) {
		if (itemIds.contains(item.getId())) {
			return true;
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
		items.add(item);
		itemIds.add(item.getId());
	}

	public int getCategoryOrder() {

		if (items.size() > 0) {
			return items.get(0).getOrder();
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
	
	public Collection<InvTweaksItem> getItems() {
		return items;
	}

	public String getName() {
		return name;
	}
}
