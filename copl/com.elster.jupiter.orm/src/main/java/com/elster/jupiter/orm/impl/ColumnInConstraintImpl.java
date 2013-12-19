package com.elster.jupiter.orm.impl;

import java.util.Objects;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class ColumnInConstraintImpl {
	private String columnName;
	private int position;
	
	// associations
	private final Reference<TableConstraint> constraint = ValueReference.absent();
	
	@SuppressWarnings("unused")
	private ColumnInConstraintImpl() {	
	}

	ColumnInConstraintImpl(TableConstraintImpl constraint, Column column, int position) {
		this.position = position;
		this.constraint.set(constraint);
		this.columnName = column.getName();
	}		
	
	TableConstraint getConstraint() {
		return constraint.get();
	}
		
	Column getColumn() {
		return Objects.requireNonNull(getConstraint().getTable().getColumn(columnName));						
	}
	
	@Override
	public String toString() {
		return "Column " + getColumn().getName()
				+ " is column " + position + " in " + getConstraint();
	}
	
}