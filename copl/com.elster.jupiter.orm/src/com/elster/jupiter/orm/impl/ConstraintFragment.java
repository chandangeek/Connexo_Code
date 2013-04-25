package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.TableConstraint;
 
abstract class ConstraintFragment extends AliasFragment {

	private final TableConstraint constraint;

	ConstraintFragment(TableConstraint constraint , String alias) {
		super(alias);
		this.constraint = constraint;
	}
	
	TableConstraint getConstraint() {
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
	
