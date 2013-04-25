package com.elster.jupiter.orm.impl;

import java.util.ArrayList;
import java.util.List;

import com.elster.jupiter.conditions.Comparison;
import com.elster.jupiter.conditions.Contains;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.sql.util.SqlFragment;
import com.elster.jupiter.units.Quantity;

class MultiColumnMapping extends FieldMapping {
	private final String fieldName;
	private final List<Column> columns;
	
	MultiColumnMapping(String fieldName , List<Column> allColumns) {
		this.fieldName = fieldName;
		this.columns = new ArrayList<>();
		for (Column column : allColumns) {
			if (column.getFieldName().startsWith(fieldName + ".")) {
				columns.add(column);
			}
		}
	}

	@Override
	String getFieldName() {
		return fieldName;
	}
	
	List<Column> getColumns() {
		return columns;
	}

	@Override
	SqlFragment asEqualFragment(Object value, String alias) {
		return new MultiColumnEqualFragment(this, value, alias);
	}

	@Override
	SqlFragment asComparisonFragment(Comparison comparison, String alias) {
		boolean isQuantity = false;
		if (getColumns().size() == 3) {
			for (Object value : comparison.getValues()) {
				isQuantity = value instanceof Quantity;
				if (!isQuantity) {
					break;
				}
			}
		}
		return isQuantity ?
			new QuantityComparisonFragment(this, comparison, alias) :
			new MultiColumnComparisonFragment(this, comparison, alias);
	}

	@Override
	SqlFragment asContainsFragment(Contains contains, String alias) {
		return new MultiColumnContainsFragment(this, contains, alias);		
	}
	
}
