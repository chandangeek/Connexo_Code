/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract Condition implementation that evaluates based on two composing Conditions.
 */
public abstract class Composite implements Condition {
	
	private final List<Condition> conditions = new ArrayList<>();
	
	Composite(Condition condition1 , Condition condition2) {
		this.conditions.add(condition1);
		this.conditions.add(condition2);
	}
	
	@Override
    public final Condition not() {
		return new Not(this);		
	}
	
	public List<Condition> getConditions() {
		return Collections.unmodifiableList(conditions);
	}
	
	void add(Condition condition) {
		this.conditions.add(condition);
	}
}
