package com.elster.jupiter.util.conditions;

import java.text.MessageFormat;

public final class Comparison extends Leaf {
	private final String fieldName;
	private final Operator operator;
	private final Object[] values;
	
	Comparison(String fieldName , Operator operator , Object[] values) {
		this.fieldName = fieldName;
		this.operator = operator;
		this.values = values;
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
		return values;
	}
	
	public String getText(String fieldText) {
		return MessageFormat.format(getOperator().getFormat(),fieldText);
	}
	
}
