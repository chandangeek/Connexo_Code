package com.elster.jupiter.util.conditions;

public final class Order {
	
	private final String name;
	private final boolean ascending;
	
	private Order(String name, boolean ascending) {
		this.name = name;
		this.ascending = ascending;
	}
	
	public boolean ascending() {
		return ascending;
	}
	
	public String getName()  {
		return name;
	}
	
	public String ordering() {
		return ascending ? "ASC" : "DESC";
	}
	
	public  static Order ascending(String name) {
		return new Order(name,true);
	}
	
	public static Order descending(String name) {
		return new Order(name,false);
	}
	
	public static Order[] from(String[] orderBy) {
		if (orderBy == null) {
			return new Order[0];
		}
		Order[] result = new Order[orderBy.length];
		for (int i = 0 ; i < orderBy.length ; i++) {
			result[i] = Order.ascending(orderBy[i]);
		}
		return result;
	}
	
	public static Order[] from(Order order , Order[] orders) {
		Order[] result = new Order[orders.length + 1];
		result[0] = order;
		for (int i = 0 ; i < orders.length; i++) {
			result[i+1] = orders[i];
		}
		return result;
	}
	
}
