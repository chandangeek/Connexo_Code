package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UniqueConstraint;

public class UniqueConstraintImpl extends TableConstraintImpl implements UniqueConstraint {
	
	@SuppressWarnings("unused")
	private UniqueConstraintImpl() {
	}
	
	UniqueConstraintImpl(Table table , String name) {
		super(table,name);
	}
	
	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	String getTypeString() {
		return "unique";
	}
}
