package net.minecraft.src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InvTweaksConfig {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("InvTweaks");
	
	private static final String LOCKED = "LOCKED";
	private static final String AUTOREPLACE = "AUTOREPLACE";
	private static final String AUTOREPLACE_NOTHING = "nothing";
	private static final String DISABLEMIDDLECLICK = "DISABLEMIDDLECLICK";
	private static final String DEBUG = "DEBUG";
	private static final boolean DEFAULT_AUTOREPLACE_BEHAVIOUR = true;
	
	private String file;
	private int[] lockPriorities;
	private Vector<Integer> lockedSlots;
	private Vector<InvTweaksRule> rules;
	private Vector<String> invalidKeywords;
	private Vector<String> autoReplaceRules;
	private boolean middleClickEnabled;
	private boolean debugEnabled;
	
	/**
	 * Creates a new configuration holder.
	 * The configuration is not yet loaded.
	 */
	public InvTweaksConfig(String file) {
		this.file = file;
		init();
	}
	
	public Vector<InvTweaksRule> getRules() {
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
	 * TODO ordered by decreasing priority
	 */
	public Vector<Integer> getLockedSlots() {
		return lockedSlots;
	}
	
	public boolean isMiddleClickEnabled() {
		return middleClickEnabled;
	}

	public Level getLogLevel() {
		return (this.debugEnabled) ? Level.INFO : Level.WARNING;
	}

	public boolean canBeAutoReplaced(int itemID, int itemDamage) {
		List<InvTweaksItem> items = InvTweaksTree.getItems(itemID, itemDamage);
		for (String keyword : autoReplaceRules) {
			if (keyword.equals(AUTOREPLACE_NOTHING))
				return false;
			if (InvTweaksTree.matches(items, keyword))
				return true;
		}
		return DEFAULT_AUTOREPLACE_BEHAVIOUR;
	}
	
	public void load() throws FileNotFoundException, IOException, Exception{

		synchronized (this) {
		
		// Reset all
		init();
		
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
		
		// Parse and sort rules (specific tiles first, then in appearing order)
		String lineText;
		InvTweaksRule newRule;
		
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
						int[] newLockedSlots = InvTweaksRule.
								getRulePreferredPositions(words[0]);
						int lockPriority = InvTweaksRule.getRuleType(words[0]).getHighestPriority();
						for (int i : newLockedSlots) {
							lockPriorities[i] = lockPriority;
						}
					}
					
					// Standard rule
					else {
						String keyword = words[1];
						boolean isValidKeyword = InvTweaksTree.isKeywordValid(keyword.toLowerCase());
						
						// If invalid keyword, guess something similar
						if (!isValidKeyword) {
							Vector<String> wordVariants = getKeywordVariants(keyword);
							for (String wordVariant : wordVariants) {
								if (InvTweaksTree.isKeywordValid(wordVariant.toLowerCase())) {
									isValidKeyword = true;
									keyword = wordVariant;
									break;
								}
							}
						}
						
						if (isValidKeyword) {
							newRule = new InvTweaksRule(words[0], keyword.toLowerCase());
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
					if (InvTweaksTree.isKeywordValid(words[1]) || 
							words[1].equals(AUTOREPLACE_NOTHING)) {
						autoReplaceRules.add(words[1]);
					}
				}
			
			}
			
			else if (words.length == 1) {
				
				// Disable middle click
				if (words[0].equals(DISABLEMIDDLECLICK)) {
					middleClickEnabled = false;
				}
				else if (words[0].equals(DEBUG)) {
					debugEnabled = true;
				}
				
			}
			
		}
		
		// Default Autoreplace behavior
		if (autoReplaceRules.isEmpty()) {
			try {
				autoReplaceRules.add(InvTweaksTree.getRootCategory().getName());
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

	private void init() {
		lockPriorities = new int[InvTweaks.INVENTORY_SIZE];
		for (int i = 0; i < lockPriorities.length; i++) {
			lockPriorities[i] = 0;
		}
		lockedSlots = new Vector<Integer>();
		rules = new Vector<InvTweaksRule>();
		invalidKeywords = new Vector<String>();
		autoReplaceRules = new Vector<String>();
		middleClickEnabled = true;
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
}
