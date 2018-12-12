/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import java.util.Collections;
import java.util.List;

import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlFragment;

public class ReverseConstraintMapping extends ConstraintMapping {
	
	public ReverseConstraintMapping(ForeignKeyConstraintImpl constraint) {
		super(constraint);
	}

	@Override
	public String getFieldName() {
		return getConstraint().getReverseFieldName();
	}

	@Override
	public SqlFragment asEqualFragment(Object value, String alias) {
		throw new UnsupportedOperationException();
	}
	@Override
	public SqlFragment asComparisonFragment(Comparison comparison, String alias) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SqlFragment asContainsFragment(Contains contains, String alias) {
		throw new UnsupportedOperationException();		
	}
	
	@Override
	public List<ColumnImpl> getColumns() {
		return Collections.emptyList();
	}
	

}
