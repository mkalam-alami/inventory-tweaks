package net.minecraft.src;


public class SortButtonRule {
	
	// A -> D = 65 -> 68 in ascii
	// 1 -> 4 = 49 -> 57 in ascii
	
	public enum RuleType {
		TILE,
		ROW,
		COLUMN
	}

	private String constraint;
	private int[] preferredPositions;
	private String keyword;
	private RuleType type;
	
	public SortButtonRule(String constraint, String keyword) {

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
				switch (c) {
					case 'A': row = 1; break;
					case 'B': row = 2; break;
					case 'C': row = 3; break;
					case 'D': row = 0;
				}
			}
		}
		
		// Tile case
		if (column != -1 && row != -1) {
			type = RuleType.TILE;
			preferredPositions = new int[]{
					index(row, column)
				};
		}
		// Row case
		else if (row != -1) {
			type = RuleType.ROW;
			preferredPositions = new int[9];
			for (int i = 0; i < 9; i++) {
				preferredPositions[i] = 
					index(row, reverse ? 8-i : i);
			}
		}
		// Column case
		else {
			type = RuleType.COLUMN;
			preferredPositions = new int[]{
				index(0, column),
				index(3, column),
				index(2, column),
				index(1, column)
			};
		}
	}
	
	public RuleType getType() {
		return type;
	}
	
	/**
	 * An array of preferred positions (from the most to the less preferred).
	 * @return
	 */
	public int[] getPreferedPositions() {
		return preferredPositions;
	}

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
	 * Inventory index (from 0 to 35), given a row and column
	 */
	private int index(int row, int column) {
		return row*9+column;
	}

}
