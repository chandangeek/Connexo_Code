package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.conditions.Contains;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.sql.util.SqlFragment;

class ColumnContainsFragment implements SqlFragment {

	private final Column column;
	private final Contains contains;
	private final String alias;
	
	public ColumnContainsFragment(Column column , Contains contains, String alias) {
		this.column = column;
		this.contains = contains;
		this.alias = alias;
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		for (Object value : contains.getCollection()) {
			statement.setObject(position++ , ((ColumnImpl) column).convertToDb(value));
		}
		return position;
	}
	
	@Override
	public String getText() {
		StringBuilder builder = new StringBuilder();
		builder.append(column.getName(alias));
		builder.append(" ");
		builder.append(contains.getOperator().getSymbol());
		builder.append("  (");
		String separator = "";
		for (@SuppressWarnings("unused") Object each : contains.getCollection()) {
			builder.append(separator);
			builder.append("?");
			separator = ",";
		}
		builder.append(") ");
		return builder.toString();
	}

}
