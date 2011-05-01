package net.minecraft.src;

import java.util.logging.Logger;

public class SortButtonKeywords {

    private static final Logger log = Logger.getLogger(SortButtonKeywords.class.getName());

	/**
	 * Checks if the given keyword is valid (i.e. represents either
	 * a registered item or a registered category)
	 * @param keyword
	 * @return
	 */
	public static final boolean isValid(String keyword) {
		// Is the keyword an item?
		Integer keywordItem = SortButtonTree.getItemValue(keyword);
		if (keywordItem != null) {
			return true;
		}
		
		// Or maybe a category ?
		else {
			SortButtonCategory category = SortButtonTree.getCategory(keyword);
			return category != null;
		}
	}
	
	/**
	 * Checks it given item ID matches a given keyword
	 * (either the item's name is the keyword, or it is
	 * in the keyword category)
	 * @param itemID
	 * @param keyword
	 * @return
	 */
	public static final boolean matches(String item, String keyword) {

		if (item == null)
			return false;
		
		// The keyword is an item
		if (SortButtonTree.contains(item)) {
			return true;
		}
		
		// The keyword is a category
		else {
			SortButtonCategory category = SortButtonTree.getCategory(keyword);
			if (category != null) {
				return category.contains(item);
			}
			else {
				return false;
			}
		}
		
	}
	
	public static int getKeywordPriority(String keyword) {
		try {
			if (keyword.equals(SortButtonTree.getRootCategory().getName())) {
				return 0;
			}
			else {
				return SortButtonTree.getRootCategory().getKeywordPriority(keyword);
			}
		}
		catch (NullPointerException e) {
			log.severe("The root category is missing: " + e.getMessage());
			return -1;
		}
	}
	
}