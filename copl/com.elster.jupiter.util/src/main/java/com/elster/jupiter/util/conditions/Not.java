package com.elster.jupiter.util.conditions;

/**
 * Condition decorator that negates the decorated Condition.
 */
public class Not implements Condition {

	private final Condition negated;
	
	Not(Condition negated) {
		this.negated = negated;
	}
	
	@Override
	public Condition and(Condition condition) {
		return new And(this,condition);
	}

	@Override
	public Condition or(Condition condition) {
		return new Or(this,condition);
	}

	@Override
	public Condition not() {
		return negated;		
	}

	public Condition getNegated() {
		return negated;
	}
	
	@Override
	public void visit(Visitor visitor) {
		visitor.visitNot(this);		
	}

}
