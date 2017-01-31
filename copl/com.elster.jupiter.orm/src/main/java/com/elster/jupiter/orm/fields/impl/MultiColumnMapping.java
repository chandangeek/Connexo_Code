/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.units.Quantity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MultiColumnMapping extends FieldMapping {
	private final String fieldName;
	private final List<ColumnImpl> columns;

	public MultiColumnMapping(String fieldName , List<ColumnImpl> allColumns) {
		this.fieldName = fieldName;
		this.columns =
                allColumns
                        .stream()
                        .filter(column -> column.getFieldName() != null)
                        .filter(column -> column.getFieldName().startsWith(fieldName + "."))
                        .collect(Collectors.toList());
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}

	public List<ColumnImpl> getColumns() {
		return Collections.unmodifiableList(this.columns);
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
