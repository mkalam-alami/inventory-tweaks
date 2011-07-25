package net.invtweaks.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.invtweaks.tree.ItemTree;
import net.invtweaks.tree.ItemTreeItem;
import net.invtweaks.tree.ItemTreeLoader;
import net.minecraft.src.InvTweaks;

public class InventoryConfig {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("InvTweaks");

	public static final String PROP_ENABLEMIDDLECLICK = "enableMiddleClick";
	public static final String PROP_SHOWCHESTBUTTONS = "showChestButtons";
	
	private static final String LOCKED = "LOCKED";
	private static final String AUTOREPLACE = "AUTOREPLACE";
	private static final String AUTOREPLACE_NOTHING = "nothing";
	private static final String DEBUG = "DEBUG";
	private static final boolean DEFAULT_AUTOREPLACE_BEHAVIOUR = true;
	
	private String rulesFile;
	private String treeFile;
	
	private Properties properties;
	private ItemTree tree;
	private int[] lockPriorities;
	private Vector<Integer> lockedSlots;
	private Vector<InventoryConfigRule> rules;
	private Vector<String> invalidKeywords;
	private Vector<String> autoReplaceRules;
	private boolean debugEnabled;
	
	/**
	 * Creates a new configuration holder.
	 * The configuration is not yet loaded.
	 */
	public InventoryConfig(String rulesFile, String treeFile) {
		this.rulesFile = rulesFile;
		this.treeFile = treeFile;
		init();
	}
	
	public void load() throws Exception {
	
		synchronized (this) {
		
		// Reset all
		init();
		
		// Load properties
		File configPropsFile = getPropertyFile();
		if (configPropsFile != null) {
			FileInputStream fis = new FileInputStream(configPropsFile);
			properties.load(fis);
		}
		save(); // Needed to append non-saved properties to the file
		
		// Load tree
		tree = new ItemTreeLoader().load(treeFile);
		
		// Read file
		File f = new File(rulesFile);
		char[] bytes = new char[(int) f.length()];
		FileReader reader = new FileReader(f);
		reader.read(bytes);
		
		// Split lines into an array
		String[] config = String.valueOf(bytes)
				.replace("\r\n", "\n")
				.replace('\r', '\n')
				.split("\n");
		
		// Parse and sort rules (specific tiles first, then in appearing order)
		String lineText;
		InventoryConfigRule newRule;
		
		int currentLine = 0;
		while (currentLine < config.length) {
	
			String[] words = config[currentLine].split(" ");
			lineText = config[currentLine].toLowerCase();
			currentLine++;
	
			// Parse valid lines only
			if (words.length == 2) {
	
				// Standard rules format
				if (lineText.matches("^([a-d]|[1-9]|[r]){1,2} [\\w]*$")
						|| lineText.matches("^[a-d][1-9]-[a-d][1-9]v? [\\w]*$")) {
					
					words[0] = words[0].toLowerCase();
					
					// Locking rule
					if (words[1].equals(LOCKED)) {
						int[] newLockedSlots = InventoryConfigRule.getRulePreferredPositions(
										words[0], InvTweaks.INVENTORY_SIZE,
										InvTweaks.INVENTORY_ROW_SIZE);
						int lockPriority = InventoryConfigRule.getRuleType(words[0]).getHighestPriority();
						for (int i : newLockedSlots) {
							lockPriorities[i] = lockPriority;
						}
					}
					
					// Standard rule
					else {
						String keyword = words[1];
						boolean isValidKeyword = tree.isKeywordValid(keyword.toLowerCase());
						
						// If invalid keyword, guess something similar
						if (!isValidKeyword) {
							Vector<String> wordVariants = getKeywordVariants(keyword);
							for (String wordVariant : wordVariants) {
								if (tree.isKeywordValid(wordVariant.toLowerCase())) {
									isValidKeyword = true;
									keyword = wordVariant;
									break;
								}
							}
						}
						
						if (isValidKeyword) {
							newRule = new InventoryConfigRule(tree, words[0], 
									keyword.toLowerCase(), InvTweaks.INVENTORY_SIZE,
									InvTweaks.INVENTORY_ROW_SIZE);
							rules.add(newRule);
						}
						else {
							invalidKeywords.add(keyword.toLowerCase());
						}
					}
				}
	
				// Autoreplace rule
				else if (words[0].equals(AUTOREPLACE)) {
					words[1] = words[1].toLowerCase();
					if (tree.isKeywordValid(words[1]) || 
							words[1].equals(AUTOREPLACE_NOTHING)) {
						autoReplaceRules.add(words[1]);
					}
				}
			
			}
			
			else if (words.length == 1) {
				
				if (words[0].equals(DEBUG)) {
					debugEnabled = true;
				}
				
			}
			
		}
		
		// Default Autoreplace behavior
		if (autoReplaceRules.isEmpty()) {
			try {
				autoReplaceRules.add(tree.getRootCategory().getName());
			}
			catch (NullPointerException e) {
				throw new NullPointerException("No root category is defined.");
			}
		}
		
		// Sort rules by priority, highest first
		Collections.sort(rules, Collections.reverseOrder());
		
		// Compute ordered locked slots
		for (int i = 0; i < lockPriorities.length; i++) {
			if (lockPriorities[i] > 0) {
				lockedSlots.add(i);
			}
		}
		
		}
		
	}

	/**
	 * Saves properties
	 */
	public void save() {
		File configPropsFile = getPropertyFile();
		if (configPropsFile.exists()) {
			try {
				FileOutputStream fos = new FileOutputStream(configPropsFile);
				properties.store(fos, "Inventory Tweaks Configuration");
				fos.flush();
				fos.close();
			} catch (IOException e) {
				InvTweaks.logInGameStatic("Failed to save config file "
						+ InvTweaks.CONFIG_PROPS_FILE);
			}
		}
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
		save();
	}

	public ItemTree getTree() {
		return tree;
	}
	
	/**
	 * Returns all sorting rules, themselves sorted by
	 * decreasing priority.
	 * @return
	 */
	public Vector<InventoryConfigRule> getRules() {
		return rules;
	}
	
	/**
	 * Returns all invalid keywords wrote in the config file.
	 */
	public Vector<String> getInvalidKeywords() {
		return invalidKeywords;
	}
	
	/**
	 * @return The locked slots array with locked priorities.
	 * Not a copy.
	 */
	public int[] getLockPriorities() {
		return lockPriorities;
	}
	
	/**
	 * @return The locked slots only
	 * TODO Order by decreasing priority
	 */
	public Vector<Integer> getLockedSlots() {
		return lockedSlots;
	}

	public Level getLogLevel() {
		return (this.debugEnabled) ? Level.INFO : Level.WARNING;
	}

	public boolean autoreplaceEnabled(int itemID, int itemDamage) {
		List<ItemTreeItem> items = tree.getItems(itemID, itemDamage);
		boolean found = false;
		for (String keyword : autoReplaceRules) {
			if (keyword.equals(AUTOREPLACE_NOTHING))
				return false;
			if (tree.matches(items, keyword))
				found = true;
		}
		if (found)
			return true;
		else {
			if (autoReplaceRules.isEmpty()) {
				return DEFAULT_AUTOREPLACE_BEHAVIOUR;
			}
			else {
				return false;
			}	
		}
	}
	
	private void init() {
		lockPriorities = new int[InvTweaks.INVENTORY_SIZE];
		for (int i = 0; i < lockPriorities.length; i++) {
			lockPriorities[i] = 0;
		}
		
		properties = new Properties();
		properties.setProperty(PROP_ENABLEMIDDLECLICK, "true");
		properties.setProperty(PROP_SHOWCHESTBUTTONS, "true");
		
		lockedSlots = new Vector<Integer>();
		rules = new Vector<InventoryConfigRule>();
		invalidKeywords = new Vector<String>();
		autoReplaceRules = new Vector<String>();
		debugEnabled = false;
	}
	
	/**
	 * Compute keyword variants to also match bad keywords.
	 * torches => torch
	 * diamondSword => sworddiamond
	 * woodenPlank => woodPlank plankwooden plankwood
	 */
	private Vector<String> getKeywordVariants(String keyword) {
		Vector<String> variants = new Vector<String>();
		
		if (keyword.endsWith("es")) // ex: torches => torch
			variants.add(keyword.substring(0, keyword.length()-2));
		else if (keyword.endsWith("s")) // ex: wools => wool
			variants.add(keyword.substring(0, keyword.length()-1));
		
		if (keyword.contains("en")) // ex: wooden => wood
			variants.add(keyword.replaceAll("en", ""));
		else {
			if (keyword.contains("wood"))
				variants.add(keyword.replaceAll("wood", "wooden"));
			if (keyword.contains("gold"))
				variants.add(keyword.replaceAll("gold", "golden"));
		}
		
		// Swap words
		if (keyword.matches("\\w*[A-Z]\\w*")) {
			byte[] keywordBytes = keyword.getBytes();
			for (int i = 0; i < keywordBytes.length; i++) {
				if (keywordBytes[i] >= 'A' && keywordBytes[i] <= 'Z') {
					String swapped = (keyword.substring(i) + 
							keyword.substring(0, i)).toLowerCase();
					variants.add(swapped);
					variants.addAll(getKeywordVariants(swapped));
				}
			}
		}
		
		return variants;
	}

	/**
	 * Returns the file when the properties are stored,
	 * after making sure the file exists. 
	 * @return May return null in case of failure while creating the file.
	 */
	private File getPropertyFile() {
		File configPropsFile = new File(InvTweaks.CONFIG_PROPS_FILE);
		if (!configPropsFile.exists()) {
			try {
				configPropsFile.createNewFile();
			} catch (IOException e) {
				InvTweaks.logInGameStatic("Failed to create the config file "
						+ InvTweaks.CONFIG_PROPS_FILE);
				return null;
			}
		}
		return configPropsFile;
	}
	
}
