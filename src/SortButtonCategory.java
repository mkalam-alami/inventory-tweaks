package net.minecraft.src;

import java.util.Vector;

public class SortButtonCategory {
    
	private final Vector<Integer> items = new Vector<Integer>();
	private final Vector<SortButtonCategory> categories = new Vector<SortButtonCategory>();
	
    public SortButtonCategory(int... ids) {
    	for (int id : ids) {
    		items.add(id);
    	}
	}
    
	public boolean contains(int itemID) {
		if (items.contains(itemID)) {
			return true;
		}
		for (SortButtonCategory category : categories) {
			if (category.contains(itemID)) {
				return true;
			}
		}
		return false;
	}

	public SortButtonCategory addCategory(SortButtonCategory category) {
		categories.add(category);
		return this;
	}
	
}
