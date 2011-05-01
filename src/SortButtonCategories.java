package net.minecraft.src;

import static net.minecraft.src.SortButtonKeywords.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SortButtonCategories {

    private static final Logger log = Logger.getLogger(SortButtonCategories.class.getName());
	private static final Map<String, SortButtonCategory> categories =
		new HashMap<String, SortButtonCategory>();
	
	/**
	 * Checks it given item ID matches a given keyword
	 * (either the item's name is the keyword, or it is
	 * in the keyword category)
	 * @param itemID
	 * @param keyword
	 * @return
	 */
	public static final boolean matches(int itemID, String keyword) {
		
		if (categories.isEmpty()) {
			initCategories();
		}
		
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
	
	public static void initCategories() {

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
		categories.put(BLOCKS,
				new SortButtonCategory());

		// Root category, should not be modified or removed
		categories.put(STUFF,
				new SortButtonCategory()
				.addCategory(categories.get(ITEMS))
				.addCategory(categories.get(BLOCKS)));
		
		
	}
	
	public static int getKeywordDepth(String keyword) {
		if (keyword.equals(STUFF)) {
			return 0;
		}
		else {
			try {
				return categories.get(STUFF).getKeywordDepth(keyword);
			}
			catch (NullPointerException e) {
				log.severe("The root category seems to be missing: " + e.getMessage());
				return -1;
			}
		}
	}
	
	
}