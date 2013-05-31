package com.elster.jupiter.util.conditions;

import java.util.Collection;

public class Contains extends Leaf {
	private final String fieldName;
	private final ListOperator operator;
	private final Collection<?> collection;
	
	Contains(String fieldName , ListOperator operator , Collection<?> collection) {
		this.fieldName = fieldName;
		this.operator = operator;
		this.collection = collection;
	}

	public String getFieldName() {
		return fieldName;
	}

	public ListOperator getOperator() {
		return operator;
	}

	public Collection<?> getCollection() {
		return collection;
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitContains(this);		
	}

}
