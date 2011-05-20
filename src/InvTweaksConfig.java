package net.minecraft.src;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;
import java.util.logging.Logger;

public class InvTweaksConfig {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("InvTweaksConfig");
	private static final String LOCKED = "LOCKED";
	
	private String file;
	private int[] lockedSlots;
	private Vector<InvTweaksRule> rules = new Vector<InvTweaksRule>();
	private Vector<String> invalidKeywords = new Vector<String>();
	
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
	public int[] getLockedSlots() {
		return lockedSlots;
	}
	
	public boolean isAutoReplaceEnabled() {
		return true; // TODO
	}

	/**
	 * WARNING: Currently not thread-safe
	 */
	public void load() throws FileNotFoundException, IOException {

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
		String lineText, keyword;
		InvTweaksRule newRule;
		
		int currentLine = 0;
		while (currentLine < config.length) {
			lineText = config[currentLine++];
			
			// Parse valid lines only
			if (lineText.matches("^([A-D]|[1-9]|[r]){1,2} [\\w]*$")) {
				String[] words = lineText.split(" ");
				if (words.length == 2) {
					
					// Locking rule
					if (words[1].equals(LOCKED)) {
						int[] newLockedSlots = InvTweaksRule.
								getRulePreferredPositions(words[0]);
						int lockPriority = InvTweaksRule.getRuleType(words[0]).getPriority();
						for (int i : newLockedSlots) {
							lockedSlots[i] = lockPriority;
						}
					}
					
					// Standard rule
					else {
						keyword = words[1].toLowerCase();
						if (InvTweaksTree.isKeywordValid(keyword)) {
							newRule = new InvTweaksRule(words[0], keyword);
							rules.add(newRule);
						}
						else if (keyword.endsWith("s")) { // Tolerate plurals
							
							String keyword2 = keyword.substring(0, keyword.length()-1);
							if (InvTweaksTree.isKeywordValid(keyword2)) {
								newRule = new InvTweaksRule(words[0], keyword2);
								rules.add(newRule);
							}
							else {
								invalidKeywords.add(words[1]);
							}
						}
						else {
							invalidKeywords.add(words[1]);
						}
					}
				}
			}
		}
		
		// Sort rules by priority, highest first
		Collections.sort(rules, Collections.reverseOrder());
		
	}

	private void init() {
		this.lockedSlots = new int[InvTweaksInventory.SIZE];
		for (int i = 0; i < this.lockedSlots.length; i++) {
			this.lockedSlots[i] = 0;
		}
		rules.clear();
		invalidKeywords.clear();
	}

}
