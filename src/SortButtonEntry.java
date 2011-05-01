package net.minecraft.src;

public class SortButtonEntry {
	
	// A -> D = 65 -> 68 in ascii
	// 1 -> 4 = 49 -> 57 in ascii
	
	private String constraint;
	private int[] preferredPositions;
	private String keyword;
	
	public SortButtonEntry(String constraint, String keyword) {

		this.keyword = keyword;
		this.constraint = constraint;
		
		//// Define preferred positions 
		
		// Default values
		int column = -1, row = -1;
		boolean reverse = false;
		
		// Extract chars
		for (int i = 0; i < constraint.length(); i++) {
			char c = constraint.charAt(i);
			if (c <= '9')
				column = c-'1';
			else if (c == 'r') {
				reverse = true;
			}
			else {
				row = c-'A';
			}
		}
		
		// Pattern: [row][column]
		if (column != -1 && row != -1) {
			preferredPositions = new int[]{
					index(row, column)
				};
		}
		// Pattern: [column]
		else if (column != -1) {
			preferredPositions = new int[4];
			for (int i = 0; i < 4; i++) {
				preferredPositions[i] = 
					index(reverse ? 3-i : i, column);
			}
		}
		// Pattern: [row]
		else {
			preferredPositions = new int[9];
			for (int i = 0; i < 9; i++) {
				preferredPositions[i] = 
					index(row, reverse ? 8-i : i);
			}
		}
	}

	/**
	 * An array of preferred positions (from the most to the less preferred).
	 * @return
	 */
	public int[] getPreferedPositions() {
		return preferredPositions;
	}

	/**
	 * The entry keyword.
	 */
	public String getKeyword() {
		return keyword;
	}
	
	/**
	 * Raw constraint name, for debug purposes
	 */
	public String getRawConstraint() {
		return constraint;
	}
	
	/**
	 * Inventory index, given a row and column
	 */
	private int index(int row, int column) {
		return row*9+column;
	}

}
