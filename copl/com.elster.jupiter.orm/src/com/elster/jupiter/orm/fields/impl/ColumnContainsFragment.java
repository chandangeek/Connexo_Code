package com.elster.jupiter.orm.fields.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.conditions.Contains;
import com.elster.jupiter.orm.Column;

public class ColumnContainsFragment extends ColumnFragment {

	private final Contains contains;

	public ColumnContainsFragment(Column column , Contains contains, String alias) {
		super(column,alias);
		this.contains = contains;
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		for (Object value : contains.getCollection()) {
			position = bind(statement,position,value);
		}
		return position;
	}
	
	@SuppressWarnings("unused")
	@Override
	public String getText() {
		StringBuilder builder = new StringBuilder();
		builder.append(getColumn().getName(getAlias()));
		builder.append(" ");
		builder.append(contains.getOperator().getSymbol());
		builder.append("  (");
		String separator = "";
		for (Object each : contains.getCollection()) {
			builder.append(separator);
			builder.append("?");
			separator = ",";
		}
		builder.append(") ");
		return builder.toString();
	}

}
