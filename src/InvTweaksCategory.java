package net.minecraft.src;

import java.util.Collection;
import java.util.Vector;
import java.util.logging.Logger;

public class InvTweaksCategory {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("ModSortButton SortButtonRule");
	
	private final Vector<String> items = new Vector<String>();
	private final Vector<InvTweaksCategory> subCategories = new Vector<InvTweaksCategory>();
	private String name;
	
    public InvTweaksCategory(String name) {
    	this.name = name;
	}
    
    public boolean contains(String item) {
		if (items.contains(item)) {
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
			for (InvTweaksCategory category : subCategories) {
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
	public Collection<InvTweaksCategory> getSubCategories() {
		return subCategories;
	}
	
	public Collection<String> getItems() {
		return items;
	}

	public String getName() {
		return name;
	}
}
