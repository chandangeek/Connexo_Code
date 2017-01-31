/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;


import com.elster.jupiter.orm.Column;
import com.elster.jupiter.util.conditions.Comparison;


public class MultiColumnComparisonFragment extends MultiColumnFragment {
	
	private final Comparison comparison;
	
	public MultiColumnComparisonFragment(MultiColumnMapping fieldMapping , Comparison comparison , String alias) {
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
			case EQUALORBOTHNULL:
			case ISNULL:
				return getText(" AND " );
			case NOTEQUAL:
			case NOTEQUALANDNOTBOTHNULL:
			case ISNOTNULL:
				return getText(" OR " );	
			default:
				throw new IllegalArgumentException("Can not generate SQL on complex objects for " + comparison.getOperator());
		}
	}
	
	public String getText(String keySeparator) {
		StringBuilder builder = new StringBuilder("(");
		String separator = "";
		for (Column each : getFieldMapping().getColumns()) {
			builder.append(separator);
			builder.append(comparison.getText(each.getName(getAlias())));
			separator = keySeparator;
		} 
		builder.append(")");
		return builder.toString();
	}


}


