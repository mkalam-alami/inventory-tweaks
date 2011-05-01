package net.minecraft.src;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SortButtonConfig {

	private String[] config;
	private int currentLine = 0;
	
	public SortButtonConfig(String file) throws FileNotFoundException, IOException {
		
		// Read file
		File f = new File(file);
		char[] bytes = new char[(int) f.length()];
		FileReader reader = new FileReader(f);
		reader.read(bytes);
		
		// Split lines into an array
		config = String.valueOf(bytes)
				.replace("\r\n", "\n")
				.replace('\r', '\n')
				.split("\n");
	}
	
	public SortButtonEntry nextEntry() {
		
		String lineText;
		
		while (currentLine < config.length) {
			
			lineText = config[currentLine++];
			
			// Parse valid lines only
			if (lineText.matches("^([A-D]|[1-9]|[r]){1,2} [\\w]*$")) {
				String[] words = lineText.split(" ");
				if (words.length == 2) {
					return new SortButtonEntry(words[0], words[1]);
				}
			}
		}
		
		return null;
	}

}
