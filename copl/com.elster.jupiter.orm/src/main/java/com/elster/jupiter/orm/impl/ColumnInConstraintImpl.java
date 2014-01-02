package com.elster.jupiter.orm.impl;

import java.util.Objects;

import javax.inject.Inject;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

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
		return this;
	}		
	
	static ColumnInConstraintImpl from(TableConstraintImpl constraint, ColumnImpl column) {
		return new ColumnInConstraintImpl().init(constraint, column);
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
		return "Column " + getColumn().getName() + " in " + getConstraint();
	}
	
}