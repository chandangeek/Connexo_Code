package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.plumbing.Bus;
import com.elster.jupiter.orm.plumbing.OrmClient;

public class ColumnInConstraintImpl {
	// persistent fields	
	private String componentName;
	private String tableName;
	private String constraintName;
	private String columnName;
	private int position;
	
	// associations
	private TableConstraint constraint;
	
	@SuppressWarnings("unused")
	private ColumnInConstraintImpl() {		
	}

	ColumnInConstraintImpl(TableConstraintImpl constraint, Column column, int position) {
		this.componentName = constraint.getComponentName();
		this.tableName = constraint.getTableName();
		this.constraintName = constraint.getName();
		this.columnName = column.getName();
		this.position = position;
		this.constraint = constraint;		
	}

	void doSetConstraint(TableConstraint constraint) {
		this.constraint = constraint;		
	}		
	
	TableConstraint getConstraint() {
		if (constraint == null) {
			constraint = getOrmClient().getTableConstraintFactory().get(componentName,tableName,constraintName);
		}
		return constraint;
	}
		
	Column getColumn() {
		return getConstraint().getTable().getColumn(columnName);						
	}
	
	void persist() {
		getOrmClient().getColumnInConstraintFactory().persist(this);
	}
	
	@Override
	public String toString() {
		return "Column " + getColumn().getName()
				+ " is column " + position + " in " + getConstraint();
	}
	
	private OrmClient getOrmClient() {
		return Bus.getOrmClient();
	}
}