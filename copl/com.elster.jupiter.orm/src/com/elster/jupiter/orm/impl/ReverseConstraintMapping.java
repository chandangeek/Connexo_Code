package com.elster.jupiter.orm.impl;

import com.elster.jupiter.conditions.Comparison;
import com.elster.jupiter.conditions.Contains;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.sql.util.SqlFragment;

class ReverseConstraintMapping extends ConstraintMapping {
	
	ReverseConstraintMapping(TableConstraint constraint) {
		super(constraint);
	}

	@Override
	String getFieldName() {
		return getConstraint().getReverseFieldName();
	}

	@Override
	SqlFragment asEqualFragment(Object value, String alias) {
		throw new UnsupportedOperationException();
	}
	@Override
	SqlFragment asComparisonFragment(Comparison comparison, String alias) {
		throw new UnsupportedOperationException();
	}

	@Override
	SqlFragment asContainsFragment(Contains contains, String alias) {
		throw new UnsupportedOperationException();		
	}
	

}
