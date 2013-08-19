package com.elster.jupiter.util.conditions;

public class Membership extends Leaf {
	
	private final Subquery subquery;
	private final ListOperator operator;
	private final String[] fieldNames;
	
	Membership(Subquery subquery , ListOperator operator , String[] fieldNames) {
		this.fieldNames = fieldNames;
		this.operator = operator;
		this.subquery = subquery;
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitMembership(this);
	}

	public String[] getFieldNames() {
		return fieldNames;
	}

	public ListOperator getOperator() {
		return operator;
	}

	public Subquery getSubquery() {
		return subquery;
	}

}
