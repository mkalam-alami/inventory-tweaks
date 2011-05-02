package net.minecraft.src;

import java.util.logging.Logger;


public class SortButtonRule implements Comparable<SortButtonRule> {
	
	// A -> D = 65 -> 68 in ascii
	// 1 -> 4 = 49 -> 57 in ascii

	public enum RuleType {
		
		TILE(2),
		COLUMN(1),
		ROW(1);
		
		private int priority;

		RuleType(int priority) {
			this.priority = priority;
		}

		public int getPriority() {
			return priority;
		}
	}
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("ModSortButton SortButtonRule");

	private String constraint;
	private int[] preferredPositions;
	private String keyword;
	private RuleType type;
	private int priority;
	
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
		
		//// Compute priority
		
		// 1st criteria : the rule type
		// 2st criteria : the keyword category depth
		// 3st criteria : the item order in a same category
		priority = type.priority*10000 + 
			SortButtonTree.getKeywordPriority(keyword);
		
	}
	
	public RuleType getType() {
		return type;
	}
	
	/**
	 * An array of preferred positions (from the most to the less preferred).
	 * @return
	 */
	public int[] getPreferredPositions() {
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
	
	/**
	 * Returns rule priority (for rule sorting)
	 * @return
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Compares rules priority : positive value
	 * means 'this' is of greater priority than o
	 */
	@Override
	public int compareTo(SortButtonRule o) {
		return getPriority() - o.getPriority();
	}

}
