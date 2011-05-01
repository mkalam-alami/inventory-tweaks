package net.minecraft.src;

import java.util.Collection;
import java.util.Vector;

public class SortButtonCategory {
    
	private final Vector<String> items = new Vector<String>();
	private final Vector<SortButtonCategory> categories = new Vector<SortButtonCategory>();
	private String name;
	
    public SortButtonCategory(String name) {
    	this.name = name;
	}
    
    public boolean contains(String item) {
		if (items.contains(item)) {
			return true;
		}
		for (SortButtonCategory category : categories) {
			if (category.contains(item)) {
				return true;
			}
		}
		return false;
	}

	public SortButtonCategory addCategory(SortButtonCategory category) {
		categories.add(category);
		return this;
	}

	public int getKeywordPriority(String keyword) {
		if (items.contains(keyword)) {
			return items.indexOf(keyword);
		}
		else if (categories.contains(keyword)) {
			return 100;
		}
		else {
			int result;
			for (SortButtonCategory category : categories) {
				result = category.getKeywordPriority(keyword);
				if (result != -1)
					return result + 100;
			}
			return -1;
		}
	}
	
	/**
	 * Returns a references to all categories contained in this one.
	 * @return
	 */
	public Collection<SortButtonCategory> getSubCategories() {
		return categories;
	}

	public void addItem(String label, int id) {
		items.add(label);
	}
	
	public String getName() {
		return name;
	}
}
