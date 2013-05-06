package com.elster.jupiter.orm.fields.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.impl.ColumnImpl;
 
abstract class ConstraintFragment extends AliasFragment {

	private final ForeignKeyConstraint constraint;

	ConstraintFragment(ForeignKeyConstraint constraint , String alias) {
		super(alias);
		this.constraint = constraint;
	}
	
	ForeignKeyConstraint getConstraint() {
		return constraint;
	}
	
	int bind(PreparedStatement statement, int position, Object value) throws SQLException {
		Object[] columnValues = getConstraint().getReferencedTable().getPrimaryKeyConstraint().getColumnValues(value);
		for (int i = 0 ; i < getConstraint().getColumns().size(); i++) {
			statement.setObject(position++ , ((ColumnImpl) getConstraint().getColumns().get(i)).convertToDb(columnValues[i]));
		}
		return position;
	}
}
	
