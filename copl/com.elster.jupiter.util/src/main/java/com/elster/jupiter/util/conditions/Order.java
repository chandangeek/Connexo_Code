package com.elster.jupiter.util.conditions;

import java.util.Objects;

public final class Order {

	public static final Order[] NOORDER = new Order[0];

	private final String name;
	private final boolean ascending;
	private String function;
	private NullStrategy nullStrategy = NullStrategy.NONE;

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

	public Order apply(String name) {
		this.function = name;
		return this;
	}

	public Order toUpperCase() {
		return apply("upper");
	}

	public Order toLowerCase() {
		return apply("lower");
	}

	public String getClause(String resolvedField) {
		return getBaseClause(resolvedField) + nullStrategy.getClause();
	}

	private String getBaseClause(String resolvedField) {
		if (function == null) {
			return resolvedField + " " + ordering();
		} else {
			return function + "(" + resolvedField + ")" + " " + ordering();
 		}
	}

	public Order nullsFirst() {
		this.nullStrategy = NullStrategy.NULLSFIRST;
		return this;
	}

	public Order nullsLast() {
		this.nullStrategy = NullStrategy.NULLSLAST;
		return this;
	}

	public  static Order ascending(String name) {
		return new Order(Objects.requireNonNull(name),true);
	}

	public static Order descending(String name) {
		return new Order(Objects.requireNonNull(name),false);
	}

	@Deprecated
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

	@Deprecated
	public static Order[] from(String order , String[] orders) {
		Order[] result = new Order[orders.length + 1];
		result[0] = Order.ascending(order);
		for (int i = 0 ; i < orders.length; i++) {
			result[i+1] = Order.ascending(orders[i]);
		}
		return result;
	}

	private enum NullStrategy {
		NONE (""),
		NULLSFIRST ("NULLS FIRST"),
		NULLSLAST ("NULLS LAST");

		private final String clause;

		NullStrategy(String clause) {
			this.clause = clause;
		}

		String getClause() {
			return " " + clause + " ";
		}
	}

}
