/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import java.util.List;

import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlFragment;
import com.google.common.collect.ImmutableList;
 
public class ColumnMapping extends FieldMapping {
	private final ColumnImpl column;
	
	public ColumnMapping(ColumnImpl column) {
		this.column = column;
	}

	public ColumnImpl getColumn() {
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
	
	@Override
	public List<ColumnImpl> getColumns() {
		return ImmutableList.of(column);
	}
	
	
}
