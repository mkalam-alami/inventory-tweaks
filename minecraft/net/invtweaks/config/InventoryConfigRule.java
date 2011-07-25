package net.invtweaks.config;

import java.awt.Point;
import java.util.logging.Logger;

import net.invtweaks.tree.ItemTree;


public class InventoryConfigRule implements Comparable<InventoryConfigRule> {
	
	public enum RuleType {

		RECTANGLE(1),
		ROW(2),
		COLUMN(3),
		TILE(4);

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
	private static final Logger log = Logger.getLogger("InvTweaks");

	private String constraint;
	private int[] preferredPositions;
	private String keyword;
	private RuleType type;
	private int priority;
	private int containerSize;
	private int containerRowSize;
	
	public InventoryConfigRule(ItemTree tree, 
			String constraint, String keyword,
			int containerSize, int containerRowSize) {

		this.keyword = keyword;
		this.constraint = constraint;
		this.type = getRuleType(constraint);
		this.containerSize = containerSize;
		this.containerRowSize = containerRowSize;
		this.preferredPositions = getRulePreferredPositions(constraint);
		
		// Compute priority
		// 1st criteria : the rule type
		// 2st criteria : the keyword category depth
		// 3st criteria : the item order in a same category
		
		priority = type.getLowestPriority() + 100000 +
				tree.getKeywordDepth(keyword)*1000 -
				tree.getKeywordOrder(keyword);
		
	}
	
	public RuleType getType() {
		return type;
	}
	
	/**
	 * An array of preferred positions (from the most to the less preferred).
	 * @return
	 */
	public int[] getPreferredSlots() {
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
	public int compareTo(InventoryConfigRule o) {
		return getPriority() - o.getPriority();
	}
	
	private static int index(int rowSize, int row, int column) {
		return row*rowSize+column;
	}

	public int[] getRulePreferredPositions(String constraint) {
		return InventoryConfigRule.getRulePreferredPositions(constraint, containerSize, containerRowSize);
	}
	
	public static int[] getRulePreferredPositions(String constraint,
			int containerSize, int containerRowSize) {

		int[] result = null;
		int containerColumnSize = containerSize/containerRowSize;
		
		// Rectangle rules
		if (constraint.length() >= 5) {
			
			boolean vertical = false;
			if (constraint.contains("v")) {
				vertical = true;
				constraint = constraint.replaceAll("v", "");
			}
			String[] elements = constraint.split("-");
			if (elements.length == 2) {
				
				int[] slots1 = getRulePreferredPositions(elements[0],
						containerSize, containerRowSize);
				int[] slots2 = getRulePreferredPositions(elements[1],
						containerSize, containerRowSize);
				if (slots1.length == 1 && slots2.length == 1) {
					
					int slot1 = slots1[0], slot2 = slots2[0];
					
					Point point1 = new Point(slot1%containerRowSize, slot1/containerRowSize),
						point2 = new Point(slot2%containerRowSize, slot2/containerRowSize);
					
					result = new int[(Math.abs(point2.y-point1.y)+1)*
					                 (Math.abs(point2.x-point1.x)+1)];
					int resultIndex = 0;
					
					// Swap coordinates for vertical ordering
					if (vertical) {
						for (Point p : new Point[]{point1, point2}) {
							int buffer = p.x;
							p.x = p.y;
							p.y = buffer;
						}
					}
					
					int y = point1.y;
					while ((point1.y < point2.y) ? 
							y <= point2.y : y >= point2.y) {
						int x = point1.x;
						while ((point1.x < point2.x) ? 
								x <= point2.x : x >= point2.x) {
							result[resultIndex++] = (vertical) ?
									index(containerRowSize, x, y) :
									index(containerRowSize, y, x);
							x += (point1.x < point2.x) ? 1 : -1;
						}
						y += (point1.y < point2.y) ? 1 : -1;
					}
					
				}
			}
		}
		
		else {
		
			// Default values
			int column = -1, row = -1;
			boolean reverse = false;
			
			// Extract chars
			for (int i = 0; i < constraint.length(); i++) {
				char c = constraint.charAt(i);
				if (c <= '9') {
					// 1 column = 0, 9 column = 8
					column = c - '1';
				}
				else if (c == 'r') {
					reverse = true;
				}
				else {
					// A row = 0, D row = 3, H row = 7
					row = c - 'a';
				}
			}
			
			// Tile case
			if (column != -1 && row != -1) {
				result = new int[]{
						index(containerRowSize, row, column)
					};
			}
			// Row case
			else if (row != -1) {
				result = new int[containerRowSize];
				for (int i = 0; i < containerRowSize; i++) {
					result[i] = 
						index(containerRowSize, row, reverse ? containerRowSize-1-i : i);
				}
			}
			// Column case
			else {
				result = new int[containerColumnSize];
				for (int i = 0; i < containerColumnSize; i++) {
					result[i] = 
						index(containerRowSize, reverse ? i : containerColumnSize-1-i, column);
				}
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
		else if (constraint.length() > 4) {
			result = RuleType.RECTANGLE;
		}
		
		return result;
		
	}
	
	public String toString() {
		return constraint + " " + keyword;
	}

}
