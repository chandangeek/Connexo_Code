/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.util.conditions.Comparison;

public class ColumnComparisonFragment extends ColumnFragment {

	private final Comparison comparison;
	
	public ColumnComparisonFragment(ColumnImpl column , Comparison comparison, String alias) {
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
		return comparison.getText(getColumn().getName(getAlias()));		
	}

}
