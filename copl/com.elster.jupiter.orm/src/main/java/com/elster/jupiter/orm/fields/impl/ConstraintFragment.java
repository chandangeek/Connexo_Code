package com.elster.jupiter.orm.fields.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
 
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
		Object[] columnValues = getConstraint().getReferencedTable().getPrimaryKeyConstraint().getColumnValues(value);
		for (int i = 0 ; i < getConstraint().getColumns().size(); i++) {
			statement.setObject(position++ , getConstraint().getColumns().get(i).convertToDb(columnValues[i]));
		}
		return position;
	}
}
	
