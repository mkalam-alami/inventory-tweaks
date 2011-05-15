package net.minecraft.src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class InvTweaksTree {

    public static final int MAX_LEVEL_DEPTH = 7;
	
    private static final Logger log = Logger.getLogger("InvTweaksTree");
    
	private static final Map<String, InvTweaksCategory> categories =
		new HashMap<String, InvTweaksCategory>();
	private static final Map<String, InvTweaksItem> itemsByName =
		new HashMap<String, InvTweaksItem>(500);
	private static final Map<Integer, InvTweaksItem> itemsById = 
		new HashMap<Integer, InvTweaksItem>(500);

	private static String rootName;
	
	/**
	 * Note: Categories and items won't be available until
	 * they are loaded successfully
	 */
	public static void loadTreeFromFile(String file) throws FileNotFoundException, IOException {
		
		// Reset tree
		categories.clear();
		itemsByName.clear();
		itemsById.clear();
		
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
		InvTweaksCategory parentCat, newCat;
		String lineText, label;
		int level, id;
		Map<Integer, InvTweaksCategory> context =
			new HashMap<Integer, InvTweaksCategory>(MAX_LEVEL_DEPTH);
		
		for (int currentLine = 0; currentLine < config.length; currentLine++) {
			
			lineText = config[currentLine];
			String[] parts = lineText.split(" ");
			
			if (parts.length >= 0 && lineText.matches("^[\\w ]*[\\w]$")) {
				
				// Item
				if (parts[parts.length-1].matches("^[0-9]*$")) { 
					
					if (parts.length >= 2) {
						// Parsing
						label = parts[parts.length-2];
						id = Integer.valueOf(parts[parts.length-1]);
						level = (parts.length-2)/2;
						
						// Adding
						parentCat = context.get(level-1);
						if (parentCat != null) {
							InvTweaksItem newItem = new InvTweaksItem(label, id, currentLine);
							parentCat.addItem(newItem);
							itemsByName.put(label, newItem);
							itemsById.put(id, newItem);
						}
					}
					
				}
				
				// Category
				else {
					
					// Parsing
					level = (parts.length-1)/2;
					label = parts[parts.length-1].toLowerCase();
					id = -1;
					
					// Adding
					newCat = new InvTweaksCategory(label);
					if (level == 0 && rootName == null) {
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
		if (containsItem(keyword)) {
			return true;
		}
		
		// Or maybe a category ?
		else {
			InvTweaksCategory category = InvTweaksTree.getCategory(keyword);
			return category != null;
		}
	}
	
	/**
	 * Checks it given item ID matches a given keyword
	 * (either the item's name is the keyword, or it is
	 * in the keyword category)
	 * @param item
	 * @param keyword
	 * @return
	 */
	public static final boolean matches(InvTweaksItem item, String keyword) {

		if (item == null)
			return false;
		
		// The keyword is an item
		if (item.getName().equals(keyword)) {
			return true;
		}
		
		// The keyword is a category
		else {
			InvTweaksCategory category = InvTweaksTree.getCategory(keyword);
			if (category != null) {
				return category.contains(item);
			}
			else {
				return false;
			}
		}
		
	}

	public static int getKeywordDepth(String keyword) {
		try {
			return InvTweaksTree.getRootCategory().findKeywordDepth(keyword);
		}
		catch (NullPointerException e) {
			log.severe("The root category is missing: " + e.getMessage());
			return -1;
		}
	}
	
	public static int getKeywordOrder(String keyword) {
		InvTweaksItem item = InvTweaksTree.getItem(keyword);
		if (item != null)
			return item.getOrder();
		else {
			try {
				return InvTweaksTree.getRootCategory().findCategoryOrder(keyword);
			}
			catch (NullPointerException e) {
				log.severe("The root category is missing: " + e.getMessage());
				return -1;
			}
		}
	}
	
	public static InvTweaksCategory getRootCategory() {
		return categories.get(rootName);
	}
	
	public static InvTweaksCategory getCategory(String keyword) {
		return categories.get(keyword);
	}

	/**
	 * Returns a reference to all categories.
	 */
	public static Collection<InvTweaksCategory> getAllCategories() {
		return categories.values();
	}

	public static boolean containsItem(String name) {
		return itemsByName.containsKey(name);
	}
	
	public static boolean containsCategory(String name) {
		return categories.containsKey(name);
	}

	public static InvTweaksItem getItem(int id) {
		return itemsById.get(id);
	}
	
	public static InvTweaksItem getItem(String name) {
		return itemsByName.get(name);
	}
	
	/**
	 * For debug purposes.
	 * Call log(getRootCategory(), 0) to log the whole tree.
	 */
	@SuppressWarnings("unused")
	private static void log(InvTweaksCategory category, int indentLevel) {
		
		String logIdent = "";
		for (int i = 0; i < indentLevel; i++) {
			logIdent += "  ";
		}
		log.info(logIdent + category.getName());
		
		for (InvTweaksCategory subCategory : category.getSubCategories()) {
			log(subCategory, indentLevel + 1);
		}
		
		for (InvTweaksItem item : category.getItems()) {
			log.info(logIdent + "  " + item + " " + item.getId());
		}
		
	}
	
}