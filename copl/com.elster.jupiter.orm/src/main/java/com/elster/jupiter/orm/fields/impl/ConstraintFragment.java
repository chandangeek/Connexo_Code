/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.orm.impl.KeyValue;
 
abstract class ConstraintFragment extends AliasFragment {

	private final ForeignKeyConstraintImpl constraint;

	ConstraintFragment(ForeignKeyConstraintImpl constraint , String alias) {
		super(alias);
		this.constraint = constraint;
	}
	
	ForeignKeyConstraintImpl getConstraint() {
		return constraint;
	}
	
	int bind(PreparedStatement statement, int position, Object value) throws SQLException {
		KeyValue columnValues = getConstraint().getReferencedTable().getPrimaryKeyConstraint().getColumnValues(value);
		for (int i = 0 ; i < getConstraint().getColumns().size(); i++) {
			statement.setObject(position++ , getConstraint().getColumns().get(i).convertToDb(columnValues.get(i)));
		}
		return position;
	}
}
	
