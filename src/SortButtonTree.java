package net.minecraft.src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SortButtonTree {
	
    private static final Logger log = Logger.getLogger("ModSortButton SortButtonTree");

    private static final int DEFAULT_ITEM_COUNT = 500;
    private static final int DEFAULT_LEVEL_DEPTH = 5;
    
	private static final Map<String, SortButtonCategory> categories =
		new HashMap<String, SortButtonCategory>();
	private static final Map<String, Integer> items =
		new HashMap<String, Integer>();
	private static final Map<Integer, String> itemsIds = 
		new HashMap<Integer, String>(DEFAULT_ITEM_COUNT);

	private static String rootName;
	
	/**
	 * Note: Categories and items won't be available until
	 * they are loaded successfully
	 */
	public static void loadTreeFromFile(String file) throws FileNotFoundException, IOException {
		
		// Reset tree
		categories.clear();
		items.clear();
		itemsIds.clear();
		
		// Read file
		File f = new File(file);
		char[] bytes = new char[(int) f.length()];
		FileReader reader = new FileReader(f);
		reader.read(bytes);
		
		// Split lines into an array
		String[] config = String.valueOf(bytes)
				.replace("\r\n", "\n")
				.replace('\r', '\n')
				.split("\n");

		// Init
		SortButtonCategory parentCat, newCat;
		String lineText, label;
		int currentLine = 0, level, id;
		Map<Integer, SortButtonCategory> context =
			new HashMap<Integer, SortButtonCategory>(DEFAULT_LEVEL_DEPTH);
		
		while (currentLine < config.length) {

			lineText = config[currentLine++];
			String[] parts = lineText.split(" ");
			
			if (parts.length >= 0 && lineText.matches("[\\w ]*[\\w]")) {
				
				// Line parsing
				level = (parts.length-1)/2;
				label = parts[parts.length-1].toLowerCase();
				id = -1;
				if (label.matches("^[0-9]*$")) {
					if (parts.length >= 2) {
						label = parts[parts.length-2];
						id = Integer.valueOf(parts[parts.length-1]);
						level = (parts.length-2)/2;
					}
					else {
						continue;
					}
				}
				
				// Add item
				if (id != -1) {
					parentCat = context.get(level-1);
					if (parentCat != null) {
						parentCat.addItem(label, id);
					}
					items.put(label, id);
					itemsIds.put(id, label);
				}
				
				// Add category
				else {
					newCat = new SortButtonCategory(label);
					if (level == 0) {
						rootName = label;
					}
					else {
						parentCat = context.get(level-1);
						if (parentCat != null) {
							parentCat.addCategory(newCat);	
						}
					}
					context.put(level, newCat);
					categories.put(label, newCat);
				}
			}
		}
	}

	/**
	 * Checks if the given keyword is valid (i.e. represents either
	 * a registered item or a registered category)
	 * @param keyword
	 * @return
	 */
	public static final boolean isKeywordValid(String keyword) {
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
		if (item.equals(keyword)) {
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
			return SortButtonTree.getRootCategory().getKeywordPriority(keyword);
		}
		catch (NullPointerException e) {
			log.severe("The root category is missing: " + e.getMessage());
			return -1;
		}
	}
	
	public static SortButtonCategory getRootCategory() {
		return categories.get(rootName);
	}
	
	public static SortButtonCategory getCategory(String keyword) {
		return categories.get(keyword);
	}

	/**
	 * Returns a reference to all categories.
	 */
	public static Collection<SortButtonCategory> getAllCategories() {
		return categories.values();
	}

	public static boolean containsItem(String name) {
		return items.containsKey(name);
	}

	public static Integer getItemValue(String name) {
		return items.get(name);
	}

	public static String getItemName(int itemID) {
		return itemsIds.get(itemID);
	}	
	
	/**
	 * For debug purposes.
	 * Call log(getRootCategory(), 0) to log the whole tree.
	 */
	@SuppressWarnings("unused")
	private static void log(SortButtonCategory category, int indentLevel) {
		
		String logIdent = "";
		for (int i = 0; i < indentLevel; i++) {
			logIdent += "  ";
		}
		log.info(logIdent + category.getName());
		
		for (SortButtonCategory subCategory : category.getSubCategories()) {
			log(subCategory, indentLevel + 1);
		}
		
		for (String item : category.getItems()) {
			log.info(logIdent + "  " + item + " " + getItemValue(item));
		}
		
	}
	
}