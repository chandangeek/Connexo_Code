package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.conditions.Comparison;
import com.elster.jupiter.conditions.Operator;
import com.elster.jupiter.orm.Column;

class ColumnComparisonFragment extends ColumnFragment {

	private final Comparison comparison;
	
	public ColumnComparisonFragment(Column column , Comparison comparison, String alias) {
		super(column,alias);
		this.comparison = comparison;
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		for (Object value : comparison.getValues()) {
			position = bind(statement, position ,value);
		}
		return position;
	}
	
	@Override
	public String getText() {
		StringBuilder builder = new StringBuilder();
		builder.append(getColumn().getName(getAlias()));
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
