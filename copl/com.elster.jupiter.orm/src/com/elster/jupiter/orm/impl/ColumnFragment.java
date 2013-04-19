package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.sql.util.SqlBuilder;
import com.elster.jupiter.sql.util.SqlFragment;

class ColumnFragment implements SqlFragment {

	private final ColumnImpl column;
	private final Object value;
	private final String alias;
	
	public ColumnFragment(Column column , Object value) {
		this(column,value,null);	
	}
	
	public ColumnFragment(Column column , Object value , String alias) {
		this.value = value;
		this.column = (ColumnImpl) column;
		this.alias = alias;
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {		
		statement.setObject(position++ , column.convertToDb(value));
		return position;
	}
	
	@Override
	public String getText() {
		return "?";
	}
	
	ColumnImpl getColumn() {
		return column;
	}
	
	void appendEqualsOn(SqlBuilder sqlBuilder) {		
		appendEqualsOn(sqlBuilder,alias);
	}

	void appendEqualsOn(SqlBuilder sqlBuilder , String aliasName) {		
		sqlBuilder.append(column.getName(aliasName));
		sqlBuilder.append(" = ");
		sqlBuilder.add(this);
	}
}
