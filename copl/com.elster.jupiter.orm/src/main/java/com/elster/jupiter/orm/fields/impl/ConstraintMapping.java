package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;

abstract class ConstraintMapping extends FieldMapping {
	private final ForeignKeyConstraintImpl constraint;
	
	ConstraintMapping(ForeignKeyConstraintImpl constraint) {
		this.constraint = constraint;
	}

	ForeignKeyConstraintImpl getConstraint() {
		return constraint;
	}
	
}
