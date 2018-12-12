/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.units.Quantity;

public class QuantityComparisonFragment extends MultiColumnFragment {
	
	private final Comparison comparison;
	private Column valueColumn;
	private Column multiplierColumn;
	private Column unitColumn;
	
	public QuantityComparisonFragment(MultiColumnMapping fieldMapping , Comparison comparison , String alias) {
		super(fieldMapping, alias);
		this.comparison = comparison;
		for (Column each : getFieldMapping().getColumns()) {
			setColumn(each);
		}
	}
	
	private void setColumn(Column column) {
		String[] parts = column.getFieldName().split("\\.");
		switch (parts[parts.length - 1]) {
			case "value":
				valueColumn = column;
				break;
			case "multiplier":
				multiplierColumn = column;
				break;
			case "unit":
				unitColumn = column;
				break;
			default:
				throw new IllegalStateException("Invalid column field " + column.getFieldName());
		}
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {	
		for (Object value : comparison.getValues()) {
			Quantity quantity = (Quantity) value;
			statement.setBigDecimal(position++, quantity.getValue());
			statement.setInt(position++, quantity.getMultiplier());
		}
		for (Object value : comparison.getValues()) {
			Quantity quantity = (Quantity) value;
			statement.setString(position++, quantity.getUnit().getAsciiSymbol());			
		}
		return position;
	}
	
	public String getText() {
		switch (comparison.getOperator()) {
			case EQUAL:
			case BETWEEN:
			case GREATERTHAN:
			case GREATERTHANOREQUAL:
			case LESSTHAN:
			case LESSTHANOREQUAL:
				return getText(" AND " , " = ");
			case NOTEQUAL:
				return getText(" OR " , " != ");			
			default:
				throw new IllegalArgumentException("Can not generate SQL on complex objects for " + comparison.getOperator());
		}
	}
	
	public String getText(String keySeparator , String unitTest) {
		StringBuilder builder = new StringBuilder("(");
		builder.append(valueColumn.getName(getAlias()));
		builder.append(" * power(10,");
		builder.append(multiplierColumn.getName(getAlias()));
		builder.append(") ");
		builder.append(comparison.getOperator().getSymbol());
		builder.append(" ? * power(10,?) "); 
		if (comparison.getOperator() == Operator.BETWEEN) {
			builder.append(" and ? * power(10,?) ");
		}
		for (int i = 0 ; i < comparison.getValues().length ; i++) {
			builder.append(keySeparator);
			builder.append(unitColumn.getName(getAlias()));
			builder.append(unitTest);
			builder.append(" ? ");
		}
		builder.append(")");
		return builder.toString();
	}


}


