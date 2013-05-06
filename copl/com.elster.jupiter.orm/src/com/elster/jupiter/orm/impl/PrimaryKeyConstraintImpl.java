package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.PrimaryKeyConstraint;
import com.elster.jupiter.orm.Table;

public class PrimaryKeyConstraintImpl extends TableConstraintImpl implements PrimaryKeyConstraint {
	
	@SuppressWarnings("unused")
	private PrimaryKeyConstraintImpl() {
	}
	
	PrimaryKeyConstraintImpl(Table table , String name) {
		super(table,name);
	}
	
	@Override
	public boolean isPrimaryKey() {
		return true;
	}

	@Override
	public String getTypeString() {
		return "primary key";
	}
}
