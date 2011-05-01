package net.minecraft.src;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

public class SortButtonConfig {

	private String file;
	Vector<SortButtonRule> rules = new Vector<SortButtonRule>();
	
	/**
	 * Creates a new configuration holder.
	 * The configuration is not yet loaded.
	 */
	public SortButtonConfig(String file) {
		this.file = file;
	}
	
	public Vector<SortButtonRule> getRules() {
		return rules;
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
		String lineText;
		SortButtonRule newRule;
		
		int currentLine = 0;
		while (currentLine < config.length) {
			lineText = config[currentLine++];
			
			// Parse valid lines only
			if (lineText.matches("^([A-D]|[1-9]|[r]){1,2} [\\w]*$")) {
				String[] words = lineText.split(" ");
				if (words.length == 2) {
					newRule = new SortButtonRule(words[0], words[1]);
						rules.add(newRule);
				}
			}
		}
		
		// Sort rules by priority (higher = greater)
		Collections.reverseOrder();
		
	}

}
