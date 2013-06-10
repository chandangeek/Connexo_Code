package com.elster.jupiter.util.conditions;

import java.util.ArrayList;
import java.util.List;

abstract public class Composite implements Condition {
	
	private final List<Condition> conditions = new ArrayList<>();
	
	Composite(Condition condition1 , Condition condition2) {
		this.conditions.add(condition1);
		this.conditions.add(condition2);
	}
	
	@Override
	final public Condition not() {
		return new Not(this);		
	}
	
	public List<Condition> getConditions() {
		return conditions;
	}
	
	void add(Condition condition) {
		this.conditions.add(condition);
	}
}
