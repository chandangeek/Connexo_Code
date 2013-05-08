package com.elster.jupiter.util.conditions;

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
