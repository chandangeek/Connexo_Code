/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

import java.util.Arrays;

public class Membership extends Leaf {
	
	private final Subquery subquery;
	private final ListOperator operator;
	private final String[] fieldNames;
	
	Membership(Subquery subquery , ListOperator operator , String... fieldNames) {
		this.fieldNames = Arrays.copyOf(fieldNames, fieldNames.length);
		this.operator = operator;
		this.subquery = subquery;
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitMembership(this);
	}

	public String[] getFieldNames() {
		return Arrays.copyOf(fieldNames, fieldNames.length);
	}

	public ListOperator getOperator() {
		return operator;
	}

	public Subquery getSubquery() {
		return subquery;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(fieldNames.toString());
		sb.append(" ");
		sb.append(operator.getSymbol());
		sb.append(" ");
		sb.append(subquery);
		return sb.toString();
	}
}
