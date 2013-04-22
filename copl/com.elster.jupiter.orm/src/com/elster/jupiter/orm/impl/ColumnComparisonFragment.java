package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.conditions.Comparison;
import com.elster.jupiter.conditions.Operator;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.sql.util.SqlFragment;

class ColumnComparisonFragment implements SqlFragment {

	private final Column column;
	private final Comparison comparison;
	private final String alias;
	
	public ColumnComparisonFragment(Column column , Comparison comparison, String alias) {
		this.column = column;
		this.comparison = comparison;
		this.alias = alias;
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		for (Object value : comparison.getValues()) {
			statement.setObject(position++ , ((ColumnImpl) column).convertToDb(value));
		}
		return position;
	}
	
	@Override
	public String getText() {
		StringBuilder builder = new StringBuilder();
		builder.append(column.getName(alias));
		builder.append(" ");
		builder.append(comparison.getOperator().getSymbol());
		builder.append("  ");
		switch (comparison.getValues().length) {
			case 0:
				return builder.toString();
			case 1:
				builder.append("? ");
				return builder.toString();
			case 2:
				if (comparison.getOperator() == Operator.BETWEEN) {
					builder.append(" ? and ? ");
					return builder.toString();
				}
			default:
				throw new IllegalArgumentException("Operator has too many arguments");
		}
	}

}
