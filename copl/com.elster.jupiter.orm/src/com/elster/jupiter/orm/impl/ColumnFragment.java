package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.Column;

abstract class ColumnFragment extends AliasFragment {

	private final Column column;

	public ColumnFragment(Column column , String alias) {
		super(alias);
		this.column = column;
	}
	
	Column getColumn() {
		return column;
	}
	
	int bind(PreparedStatement statement, int position , Object value) throws SQLException {		
		statement.setObject(position++ , ((ColumnImpl) column).convertToDb(value));
		return position;
	}

}