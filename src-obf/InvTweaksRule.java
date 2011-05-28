import java.util.logging.Logger;

public class InvTweaksRule implements Comparable<InvTweaksRule> {
	
	public enum RuleType {
		
		ROW(1),
		COLUMN(2),
		TILE(3);

		private int lowestPriority;
		private int highestPriority;

		RuleType(int priorityLevel) {
			lowestPriority = priorityLevel*1000000;
			highestPriority = (priorityLevel+1)*1000000-1;
		}

		// Used for computing rule priorities
		public int getLowestPriority() {
			return lowestPriority;
		}
		
		// Used for computing lock levels
		public int getHighestPriority() {
			return highestPriority;
		}
	}
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("InvTweaksRule");

	private String constraint;
	private int[] preferredPositions;
	private String keyword;
	private RuleType type;
	private int priority;
	
	public InvTweaksRule(String constraint, String keyword) {

		this.keyword = keyword;
		this.constraint = constraint;
		this.type = getRuleType(constraint);
		this.preferredPositions = getRulePreferredPositions(constraint);
		
		// Compute priority
		// 1st criteria : the rule type
		// 2st criteria : the keyword category depth
		// 3st criteria : the item order in a same category
		priority = type.getLowestPriority() + 
			InvTweaksTree.getKeywordDepth(keyword)*10000 -
			InvTweaksTree.getKeywordOrder(keyword);
		
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
	public int compareTo(InvTweaksRule o) {
		return getPriority() - o.getPriority();
	}
	
	/**
	 * Inventory index (from 0 to 35), given a row and column
	 */
	private static int index(int row, int column) {
		return row*9+column;
	}
	
	public static int[] getRulePreferredPositions(String constraint) {

		// Default values
		int[] result;
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
					case 'a': row = 1; break;
					case 'b': row = 2; break;
					case 'c': row = 3; break;
					case 'd': row = 0;
				}
			}
		}
		
		// Tile case
		if (column != -1 && row != -1) {
			result = new int[]{
					index(row, column)
				};
		}
		// Row case
		else if (row != -1) {
			result = new int[9];
			for (int i = 0; i < 9; i++) {
				result[i] = 
					index(row, reverse ? 8-i : i);
			}
		}
		// Column case
		else {
			if (reverse) {
				result = new int[]{
					index(1, column),
					index(2, column),
					index(3, column),
					index(0, column)
				};
			}
			else {
				result = new int[]{
					index(0, column),
					index(3, column),
					index(2, column),
					index(1, column)
				};
			}
		}
		
		return result;
	}
	
	public static RuleType getRuleType(String constraint) {
		
		RuleType result = RuleType.TILE;
		
		if (constraint.length() == 1 ||
				(constraint.length() == 2 && constraint.contains("r"))) {
			constraint = constraint.replace("r", "");
			if (constraint.getBytes()[0] <= '9')
				result = RuleType.COLUMN; 
			else {
				result = RuleType.ROW; 
			}
		}
		
		return result;
		
	}

}
