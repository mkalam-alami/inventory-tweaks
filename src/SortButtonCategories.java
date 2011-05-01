package net.minecraft.src;

import static net.minecraft.src.SortButtonKeywords.*;
import java.util.HashMap;
import java.util.Map;

public class SortButtonCategories {

	private final Map<String, SortButtonCategory> categories =
		new HashMap<String, SortButtonCategory>();
	
	public SortButtonCategories() {
		initCategories();
	}
	
	/**
	 * Checks it given item ID matches a given keyword
	 * (either the item's name is the keyword, or it is
	 * in the keyword category)
	 * @param itemID
	 * @param keyword
	 * @return
	 */
	public final boolean matches(int itemID, String keyword) {
		
		Integer keywordItem = SortButtonKeywords.getItemValue(keyword);
		
		// The keyword is an item
		if (keywordItem != null) {
			return itemID == keywordItem;
		}
		
		// The keyword is a category
		else {
			SortButtonCategory category = categories.get(keyword);
			if (category != null) {
				return category.contains(itemID);
			}
			else {
				return keyword.equals(STUFF);
			}
		}
		
	}
	
	private void initCategories() {

		categories.put(SWORDS, 
				new SortButtonCategory(
						Item.swordDiamond.shiftedIndex,
						Item.swordGold.shiftedIndex,
						Item.swordSteel.shiftedIndex,
						Item.swordStone.shiftedIndex,
						Item.swordWood.shiftedIndex));

		categories.put(WEAPONS,
				new SortButtonCategory()
				.addCategory(categories.get(SWORDS)));		
		
		categories.put(ITEMS,
				new SortButtonCategory()
				.addCategory(categories.get(WEAPONS)));
		
	}
	
	
}