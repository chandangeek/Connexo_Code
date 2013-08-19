package com.elster.jupiter.util.conditions;

/**
 * Composite Condition that evaluates to the logical disjunction of its two composing Conditions.
 */
public class Or extends Composite {

	Or(Condition condition1, Condition condition2) {
		super(condition1, condition2);
	}

	@Override
	public Condition and(Condition condition) {
		return new And(this,condition);
	}

	@Override
	public Condition or(Condition condition) {
		add(condition);
		return this;
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitOr(this);		
	}

}
