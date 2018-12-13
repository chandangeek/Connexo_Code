/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;

public class ColumnInConstraintImpl {
	private String columnName;
	@SuppressWarnings("unused")
	private int position;
	
	// associations
	private final Reference<TableConstraintImpl> constraint = ValueReference.absent();
	private ColumnImpl column;
	
	@Inject
	private ColumnInConstraintImpl() {	
	}

	ColumnInConstraintImpl init(TableConstraintImpl constraint, ColumnImpl column) {
		this.constraint.set(constraint);
		this.columnName = column.getName();
		this.column = column;
		return this;
	}		
	
	static ColumnInConstraintImpl from(TableConstraintImpl constraint, ColumnImpl column) {
		return new ColumnInConstraintImpl().init(constraint, column);
	}
	
	TableConstraintImpl<?> getConstraint() {
		return constraint.get();
	}
		
	ColumnImpl getColumn() {
		if (column == null) {
			column = getConstraint().getTable().getColumn(columnName).get();
		}
		return column;
	}
	
	@Override
	public String toString() {
		return "Column " + getColumn().getName() + " in " + getConstraint();
	}
	
}