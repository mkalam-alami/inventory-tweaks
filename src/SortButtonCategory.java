package net.minecraft.src;

import java.util.Collection;
import java.util.Vector;
import java.util.logging.Logger;

public class SortButtonCategory {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("ModSortButton SortButtonRule");
	
	private final Vector<String> items = new Vector<String>();
	private final Vector<SortButtonCategory> subCategories = new Vector<SortButtonCategory>();
	private String name;
	
    public SortButtonCategory(String name) {
    	this.name = name;
	}
    
    public boolean contains(String item) {
		if (items.contains(item)) {
			return true;
		}
		for (SortButtonCategory category : subCategories) {
			if (category.contains(item)) {
				return true;
			}
		}
		return false;
	}

	public void addCategory(SortButtonCategory category) {
		subCategories.add(category);
	}
	
	public void addItem(String label, int id) {
		items.add(label);
	}

	public int getKeywordPriority(String keyword) {
		if (keyword.equals(name)) {
			return 0;
		}
		else if (items.contains(keyword)) {
			return items.size()-items.indexOf(keyword);
		}
		else {
			
			int result;
			for (SortButtonCategory category : subCategories) {
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
		return subCategories;
	}
	
	public Collection<String> getItems() {
		return items;
	}

	public String getName() {
		return name;
	}
}
