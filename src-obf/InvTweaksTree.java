import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

public class InvTweaksTree {

    public static final int MAX_CATEGORY_RANGE = 1000;
	
    private static final Logger log = Logger.getLogger("InvTweaks");
    
    /** All categories, stored by name */
	private static final Map<String, InvTweaksCategory> categories =
		new HashMap<String, InvTweaksCategory>();

	/** Items stored by ID. A same ID can hold several names. */
	private static final Map<Integer, Vector<InvTweaksItem>> itemsById = 
		new HashMap<Integer, Vector<InvTweaksItem>>(500);
	private static Vector<InvTweaksItem> defaultItems = null;
	
	/** Items stored by name. A same name can match several IDs. */
	private static final Map<String, Vector<InvTweaksItem>> itemsByName =
		new HashMap<String, Vector<InvTweaksItem>>(500);

	private static String rootName;
	
	
	/**
	 * Note: Categories and items won't be available until
	 * they are loaded successfully
	 */
	public static void loadTreeFromFile(String file) throws FileNotFoundException, IOException {
		
		if (defaultItems == null) {
			defaultItems = new Vector<InvTweaksItem>();
			defaultItems.add(
					new InvTweaksItem("unknown", -1, -1, Integer.MAX_VALUE));
		}
		
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
		int level, id, damage, order = 0;
		Map<Integer, InvTweaksCategory> context =
			new HashMap<Integer, InvTweaksCategory>();
		
		for (int currentLine = 0; currentLine < config.length; currentLine++) {
			
			try {
				
				lineText = config[currentLine].toLowerCase();
				if (lineText.trim().isEmpty())
					continue;
				
				String[] parts = lineText.split(" ");
				if (parts.length >= 0 && lineText.matches("^[\\w -]+$")) {
					
					// Category
					if (!parts[parts.length-1].matches("^[0-9-]*$")
							|| parts[parts.length-2].equals("to")) {
						
						int rangeLow = 0, rangeHigh = -1;
						
						// Parsing
						if (parts.length > 3 && parts[parts.length-2].equals("to")) {
							rangeLow = Integer.parseInt(parts[parts.length-3]);
							rangeHigh = Integer.parseInt(parts[parts.length-1]);
							label = parts[parts.length-4];
							level = (parts.length-4)/2;
						}
						else {
							label = parts[parts.length-1];
							level = (parts.length-1)/2;
						}
						
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
						
						// Adding item ranges
						if (rangeLow < rangeHigh) {
							
							// Too high ranges will reduce performance
							if (rangeHigh - rangeLow > MAX_CATEGORY_RANGE)
								throw new Exception();
							
							// Add unnamed items
							for (int i = rangeLow; i <= rangeHigh; i++) {
								InvTweaksItem newItem = new InvTweaksItem("", i, -1, order++);
								newCat.addItem(newItem);
								registerItem(newItem);
							}
							
						}
						
					}
					
					// Item
					else { 
						
						if (parts.length >= 2) {
							// Parsing
							label = parts[parts.length-2];
							level = (parts.length-2)/2;
							if (parts[parts.length-1].contains("-")) {
								String[] idPlusDamage = parts[parts.length-1].split("-");
								id = Integer.valueOf(idPlusDamage[0]);
								damage = Integer.valueOf(idPlusDamage[1]);
							}
							else {
								id = Integer.valueOf(parts[parts.length-1]);
								damage = -1;
							}
							
							// Adding
							parentCat = context.get(level-1);
							if (parentCat != null) {
								InvTweaksItem newItem = new InvTweaksItem(
										label, id, damage, order++);
								parentCat.addItem(newItem);
								registerItem(newItem);
							}
						}
						
					}
				}
			}
			catch (Exception e) {
				InvTweaks.getInstance().logInGame(
						"Line "+(currentLine+1)+" of the item tree is invalid.");
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
	public static final boolean matches(List<InvTweaksItem> items, String keyword) {

		if (items == null)
			return false;
		
		// The keyword is an item
		for (InvTweaksItem item : items) {
			if (item.getName().equals(keyword)) {
				return true;
			}
		}
		
		// The keyword is a category
		InvTweaksCategory category = InvTweaksTree.getCategory(keyword);
		if (category != null) {
			for (InvTweaksItem item : items) {
				if (category.contains(item)) {
					return true;
				}
			}
		}
		
		return false;
	}

	public static int getKeywordDepth(String keyword) {
		try {
			return InvTweaksTree.getRootCategory().findKeywordDepth(keyword);
		}
		catch (NullPointerException e) {
			log.severe("The root category is missing: " + e.getMessage());
			return 0;
		}
	}
	
	public static int getKeywordOrder(String keyword) {
		List<InvTweaksItem> items = InvTweaksTree.getItems(keyword);
		if (items != null && items.size() != 0) {
			return items.get(0).getOrder();
		}
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

	public static List<InvTweaksItem> getItems(int id, int damage) {
		List<InvTweaksItem> items = itemsById.get(id);
		List<InvTweaksItem> filteredItems = null;
		if (items != null) {
			// Filter items of same ID, but different damage value
			for (InvTweaksItem item : items) {
				if (item.getDamage() != -1 && item.getDamage() != damage) {
					if (filteredItems == null) {
						filteredItems = new ArrayList<InvTweaksItem>(items);
					}
					filteredItems.remove(item);
				}
			}
			return (filteredItems != null) ? filteredItems : items;
		}
		else {
			log.warning("Unknown item id: "+id);
			return defaultItems;
		}
	}

	public static InvTweaksItem getRandomItem(Random r) {
		return (InvTweaksItem) itemsByName.values().
				toArray()[r.nextInt(itemsByName.size())];
	}
	
	public static List<InvTweaksItem> getItems(String name) {
		return itemsByName.get(name);
	}
	
	private static void registerItem(InvTweaksItem item) {
		if (itemsByName.containsKey(item.getName())) {
			itemsByName.get(item.getName()).add(item);
		}
		else {
			Vector<InvTweaksItem> list = 
				new Vector<InvTweaksItem>();
			list.add(item);
			itemsByName.put(item.getName(), list);
		}
		if (itemsById.containsKey(item.getId())) {
			itemsById.get(item.getId()).add(item);
		}
		else {
			Vector<InvTweaksItem> list = 
				new Vector<InvTweaksItem>();
			list.add(item);
			itemsById.put(item.getId(), list);
		}
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
		
		for (List<InvTweaksItem> itemList : category.getItems()) {
			for (InvTweaksItem item : itemList) {
				log.info(logIdent + "  " + item + " " + 
						item.getId() + " " + item.getDamage());
			}
		}
		
	}
	
}