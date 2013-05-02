package com.elster.jupiter.orm.fields.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.sql.util.SqlFragment;

public class ColumnEqualsFragment extends ColumnFragment implements SqlFragment {

	private final Object value;
	
	public ColumnEqualsFragment(Column column , Object value , String alias) {
		super(column , alias);
		this.value = value;		
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {		
		return bind(statement,position,value);
	}
	
	@Override
	public String getText() {
		StringBuilder builder = new StringBuilder(" ");
		builder.append(getColumn().getName(getAlias()));
		builder.append(" = ? " );
		return builder.toString();
	}
	
	
}
