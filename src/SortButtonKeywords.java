package net.minecraft.src;

import java.util.HashMap;
import java.util.Map;

public class SortButtonKeywords {
    
	private static final Map<String, Integer> items = new HashMap<String, Integer>(); 

	public static final String SWORDS = "weapons";
	public static final String WEAPONS = "weapons";
	
	public static final String ITEMS = "items";
	public static final String BLOCKS = "items";
	
	public static final String STUFF = "stuff";
	
	public static Integer getItemValue(String name) {
		if (items.isEmpty()) {
			initItems();
		}
		return items.get(name);
	}
	
	public static void initItems() {
		
		items.put("swordDiamond", Item.swordDiamond.shiftedIndex);
		items.put("swordGold", Item.swordGold.shiftedIndex);
		items.put("swordSteel", Item.swordSteel.shiftedIndex);
		items.put("swordStone", Item.swordStone.shiftedIndex);
		items.put("swordWood", Item.swordWood.shiftedIndex);
		// TODO

	}
	
}
