package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.TableConstraint;

abstract class ConstraintMapping extends FieldMapping {
	private final TableConstraint constraint;
	
	ConstraintMapping(TableConstraint constraint) {
		this.constraint = constraint;
	}

	TableConstraint getConstraint() {
		return constraint;
	}
	
}
