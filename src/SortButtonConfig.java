package net.minecraft.src;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;
import java.util.logging.Logger;

public class SortButtonConfig {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("ModSortButton SortButtonConfig");
	private static final String LOCKED = "LOCKED";
	
	private String file;
	private int[] lockedSlots;
	private Vector<SortButtonRule> rules = new Vector<SortButtonRule>();
	private Vector<String> invalidKeywords = new Vector<String>();
	
	/**
	 * Creates a new configuration holder.
	 * The configuration is not yet loaded.
	 */
	public SortButtonConfig(String file) {
		this.file = file;
		this.lockedSlots = new int[SortButton.INV_SIZE];
		for (int i = 0; i < this.lockedSlots.length; i++) {
			this.lockedSlots[i] = 0;
		}
	}
	
	public Vector<SortButtonRule> getRules() {
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

	/**
	 * WARNING: Currently not thread-safe
	 */
	public void load() throws FileNotFoundException, IOException {

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
		rules.clear();
		String lineText, keyword;
		SortButtonRule newRule;
		
		int currentLine = 0;
		while (currentLine < config.length) {
			lineText = config[currentLine++];
			
			// Parse valid lines only
			if (lineText.matches("^([A-D]|[1-9]|[r]){1,2} [\\w]*$")) {
				String[] words = lineText.split(" ");
				if (words.length == 2) {
					
					// Locking rule
					if (words[1].equals(LOCKED)) {
						int[] newLockedSlots = SortButtonRule.
								getRulePreferredPositions(words[0]);
						int lockPriority = SortButtonRule.getRuleType(words[0]).getPriority();
						for (int i = 0; i < newLockedSlots.length; i++) {
							lockedSlots[i] = lockPriority;
						}
					}
					
					// Standard rule
					else {
						keyword = words[1].toLowerCase();
						if (SortButtonTree.isKeywordValid(keyword)) {
							newRule = new SortButtonRule(words[0], keyword);
							rules.add(newRule);
						}
						else if (keyword.endsWith("s") // Tolerate plurals
								&& SortButtonTree.isKeywordValid(
										keyword.substring(0, keyword.length()-2))) {
							newRule = new SortButtonRule(
									words[0],
									keyword.substring(0, keyword.length()-2));
							rules.add(newRule);
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
		
		/*for (SortButtonRule rule : rules) {
			log.info(rule.getKeyword()+" "+rule.getPriority());
		}*/
		
	}

}
