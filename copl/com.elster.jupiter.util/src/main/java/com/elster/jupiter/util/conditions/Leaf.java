package com.elster.jupiter.util.conditions;

public abstract class Leaf implements Condition {

	@Override
	final public Condition and(Condition condition) {		
		return new And(this,condition);
	}

	@Override
	final public Condition or(Condition condition) {
		return new Or(this,condition);
	}

	@Override
	final public Condition not() {
		return new Not(this);
	}
}
