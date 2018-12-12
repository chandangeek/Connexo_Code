/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import java.util.List;

import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlFragment;

public class ForwardConstraintMapping extends ConstraintMapping {
	
	public ForwardConstraintMapping(ForeignKeyConstraintImpl constraint) {
		super(constraint);
	}

	@Override
	public String getFieldName() {
		return getConstraint().getFieldName();
	}

	@Override
	public SqlFragment asEqualFragment(Object value, String alias) {
		return new ConstraintEqualFragment(getConstraint(), value, alias);
	}

	@Override
	public SqlFragment asComparisonFragment(Comparison comparison, String alias) {
		return new ConstraintComparisonFragment(getConstraint(), comparison, alias);
	}

	@Override
	public SqlFragment asContainsFragment(Contains contains, String alias) {
		return new ConstraintContainsFragment(getConstraint(), contains, alias);
	}
	
	@Override
	public List<ColumnImpl> getColumns() {
		return getConstraint().getColumns();
	}
	

}
