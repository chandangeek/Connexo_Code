package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.fields.impl.ColumnComparisonFragment;
import com.elster.jupiter.orm.fields.impl.ColumnContainsFragment;
import com.elster.jupiter.orm.fields.impl.ColumnEqualsFragment;
import com.elster.jupiter.orm.fields.impl.FieldMapping;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlFragment;
 
class ColumnMapping extends FieldMapping {
	private final Column column;
	
	ColumnMapping(Column column) {
		this.column = column;
	}

	Column getColumn() {
		return column;
	}

	@Override
	public String getFieldName() {
		return column.getFieldName();
	}

	@Override
	public SqlFragment asEqualFragment(Object value, String alias) {
		return new ColumnEqualsFragment(column, value, alias);
	}

	@Override
	public SqlFragment asComparisonFragment(Comparison comparison, String alias) {
		return new ColumnComparisonFragment(column, comparison, alias);
	}

	@Override
	public SqlFragment asContainsFragment(Contains contains, String alias) {
		return new ColumnContainsFragment(column, contains, alias);
	}
	
	
	
}
