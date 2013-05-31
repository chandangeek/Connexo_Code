package com.elster.jupiter.util.conditions;

import java.util.List;

public enum ListOperator {
	IN ("IN") ,
	NOTIN("NOT IN");
	
	private final String symbol;
	
	ListOperator(String symbol) {
		this.symbol = symbol;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public Contains contains(String fieldName , List<?> values) {
		return new Contains(fieldName, this , values);
	}
	
	public Membership contains(Subquery subquery, String ... fieldNames) {
		return new Membership(subquery , this , fieldNames);
	}
	
	public static Exists exists(Subquery subquery) {
		return new Exists(subquery);
	}
}
