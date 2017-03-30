/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

public class Effective extends Leaf {
	private final String fieldName;
	
	Effective(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitEffective(this);
	}

}
