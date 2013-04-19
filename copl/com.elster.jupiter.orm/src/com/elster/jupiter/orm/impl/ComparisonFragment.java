package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.conditions.Comparison;
import com.elster.jupiter.conditions.Operator;
import com.elster.jupiter.sql.SqlFragment;

class ComparisonFragment implements SqlFragment {

	private final ColumnAndAlias columnAndAlias;
	private final Comparison comparison;
	
	public ComparisonFragment(ColumnAndAlias columnAndAlias , Comparison comparison) {
		this.columnAndAlias = columnAndAlias;
		this.comparison = comparison;
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		ColumnImpl column = (ColumnImpl) columnAndAlias.getColumn();
		for (Object value : comparison.getValues()) {
			statement.setObject(position++ , column.convertToDb(value));
		}
		return position;
	}
	
	@Override
	public String getText() {
		StringBuilder builder = new StringBuilder(columnAndAlias.toString());
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
		}
		String separator = "";
		builder.append("(");
		for (int i = 0 ; i < comparison.getValues().length;i++) {
			builder.append(separator);
			builder.append("?");
			separator = ", ";
		}
		builder.append(")");
		return builder.toString();
	}

}
