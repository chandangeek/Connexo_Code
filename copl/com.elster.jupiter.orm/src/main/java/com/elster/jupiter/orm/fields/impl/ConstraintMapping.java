package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.orm.ForeignKeyConstraint;

abstract class ConstraintMapping extends FieldMapping {
	private final ForeignKeyConstraint constraint;
	
	ConstraintMapping(ForeignKeyConstraint constraint) {
		this.constraint = constraint;
	}

	ForeignKeyConstraint getConstraint() {
		return constraint;
	}
	
}
