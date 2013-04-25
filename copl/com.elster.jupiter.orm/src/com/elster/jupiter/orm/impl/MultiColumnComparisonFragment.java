package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.conditions.Comparison;
import com.elster.jupiter.orm.Column;


class MultiColumnComparisonFragment extends MultiColumnFragment {
	
	private final Comparison comparison;
	
	MultiColumnComparisonFragment(MultiColumnMapping fieldMapping , Comparison comparison , String alias) {
		super(fieldMapping, alias);
		this.comparison = comparison;
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {	
		Object[] values = comparison.getValues();
		if (values.length == 1) {
			position = bind(statement, position , values[0]);
		}
		return position;
	}
	
	public String getText() {
		switch (comparison.getOperator()) {
			case EQUAL:
			case ISNOTNULL:
				return getText(" AND ");
			case ISNULL:
			case NOTEQUAL:
				return getText(" OR ");			
			default:
				throw new IllegalArgumentException("Can not generate SQL on complex objects for " + comparison.getOperator());
		}
	}
	
	public String getText(String keySeparator) {
		StringBuilder builder = new StringBuilder("(");
		String separator = "";
		for (Column each : getFieldMapping().getColumns()) {
			builder.append(separator);
			builder.append(each.getName(getAlias()));
			builder.append(" ");
			builder.append(comparison.getOperator().getSymbol());
			builder.append("  ");
			switch (comparison.getValues().length) {
				case 0:				
					break;
				case 1:
					builder.append("? ");
					break;					
				default: {
					throw new IllegalArgumentException(" Too many arguments ");
				}
			}
			separator = keySeparator;
		}
		builder.append(")");
		return builder.toString();
	}


}


