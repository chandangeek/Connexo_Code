package com.elster.jupiter.schema.oracle.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class UserConstraintColumnImpl  {

	private String columnName;
	@SuppressWarnings("unused")
	private int position;
	
	private Reference<UserConstraintImpl> constraint = ValueReference.absent();
	
	UserColumnImpl getColumn() {
		return constraint.get().getTable().getColumn(columnName);
	}
	
	String getColumnName() {
		return columnName;
	}

}
