package com.elster.jupiter.orm.fields.impl;

import java.util.ArrayList;
import java.util.List;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.units.Quantity;

public class MultiColumnMapping extends FieldMapping {
	private final String fieldName;
	private final List<Column> columns;
	
	public MultiColumnMapping(String fieldName , List<Column> allColumns) {
		this.fieldName = fieldName;
		this.columns = new ArrayList<>();
		for (Column column : allColumns) {
			if (column.getFieldName().startsWith(fieldName + ".")) {
				columns.add(column);
			}
		}
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}
	
	public List<Column> getColumns() {
		return columns;
	}

	@Override
	public SqlFragment asEqualFragment(Object value, String alias) {
		return new MultiColumnEqualFragment(this, value, alias);
	}

	@Override
	public SqlFragment asComparisonFragment(Comparison comparison, String alias) {
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
	public SqlFragment asContainsFragment(Contains contains, String alias) {
		return new MultiColumnContainsFragment(this, contains, alias);		
	}
	
}
