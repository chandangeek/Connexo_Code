package com.elster.jupiter.util.conditions;

/**
 * Composite Condition that evaluates to the logical conjunction of its two composing Conditions.
 */
public class And extends Composite {

	And(Condition condition1, Condition condition2) {
		super(condition1, condition2);
	}

	@Override
	public Condition and(Condition condition) {
		add(condition);
		return this;
	}

	@Override
	public Condition or(Condition condition) {
		return new Or(this,condition);
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitAnd(this);		
	}

}
