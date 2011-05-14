package net.minecraft.src;

public class InvTweaksItem {

	private String name;
	private int id;
	private int order;
	
	public InvTweaksItem(String name, int id, int order) {

		this.name = name;
		this.id = id;
		this.order = order;
		
	}
	
	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public int getOrder() {
		return order;
	}
	
	public String toString() {
		return name;
	}

}
