/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

import java.text.MessageFormat;
import java.util.Arrays;

public final class Comparison extends Leaf {
	private final String fieldName;
	private final Operator operator;
	private final Object[] values;
	
	Comparison(String fieldName , Operator operator , Object[] values) {
		this.fieldName = fieldName;
		this.operator = operator;
		this.values = Arrays.copyOf(values, values.length);
	}
	

	@Override
	public void visit(Visitor visitor) {
		visitor.visitComparison(this);
		
	}

	public String getFieldName() {
		return fieldName;
	}

	public Operator getOperator() {
		return operator;
	}

	public Object[] getValues() {
		return Arrays.copyOf(values, values.length);
	}
	
	public String getText(String fieldText) {
		return MessageFormat.format(getOperator().getFormat(),fieldText);
	}
	
	@Override
	public String toString() {
		return getText(fieldName);
	}
	
}
