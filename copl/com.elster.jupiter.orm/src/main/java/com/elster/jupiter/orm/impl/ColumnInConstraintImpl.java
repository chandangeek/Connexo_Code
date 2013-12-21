package com.elster.jupiter.orm.impl;

import java.util.Objects;

import javax.inject.Inject;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class ColumnInConstraintImpl {
	private String columnName;
	private int position;
	
	// associations
	private final Reference<TableConstraintImpl> constraint = ValueReference.absent();
	private ColumnImpl column;
	
	@Inject
	private ColumnInConstraintImpl() {	
	}

	ColumnInConstraintImpl(TableConstraintImpl constraint, ColumnImpl column, int position) {
		this.position = position;
		this.constraint.set(constraint);
		this.columnName = column.getName();
	}		
	
	TableConstraintImpl getConstraint() {
		return constraint.get();
	}
		
	ColumnImpl getColumn() {
		if (column == null) {
			column = Objects.requireNonNull(getConstraint().getTable().getColumn(columnName));
		}
		return column;
	}
	
	@Override
	public String toString() {
		return "Column " + getColumn().getName()
				+ " is column " + position + " in " + getConstraint();
	}
	
}