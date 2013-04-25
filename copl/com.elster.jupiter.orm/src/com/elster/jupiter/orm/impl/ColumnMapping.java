package com.elster.jupiter.orm.impl;

import com.elster.jupiter.conditions.Comparison;
import com.elster.jupiter.conditions.Contains;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.sql.util.SqlFragment;
 
class ColumnMapping extends FieldMapping {
	private final Column column;
	
	ColumnMapping(Column column) {
		this.column = column;
	}

	Column getColumn() {
		return column;
	}

	@Override
	String getFieldName() {
		return column.getFieldName();
	}

	@Override
	SqlFragment asEqualFragment(Object value, String alias) {
		return new ColumnEqualsFragment(column, value, alias);
	}

	@Override
	SqlFragment asComparisonFragment(Comparison comparison, String alias) {
		return new ColumnComparisonFragment(column, comparison, alias);
	}

	@Override
	SqlFragment asContainsFragment(Contains contains, String alias) {
		return new ColumnContainsFragment(column, contains, alias);
	}
	
	
	
}
