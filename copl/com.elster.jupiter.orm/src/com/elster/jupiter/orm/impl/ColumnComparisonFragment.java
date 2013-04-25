package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.conditions.Comparison;
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
		return comparison.getText(getColumn().getName(getAlias()));		
	}

}
