package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlFragment;

public class ReverseConstraintMapping extends ConstraintMapping {
	
	public ReverseConstraintMapping(ForeignKeyConstraint constraint) {
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
	

}
