package com.elster.jupiter.orm.impl;

import com.elster.jupiter.conditions.Comparison;
import com.elster.jupiter.conditions.Contains;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.sql.util.SqlFragment;

class ForwardConstraintMapping extends ConstraintMapping {
	
	ForwardConstraintMapping(TableConstraint constraint) {
		super(constraint);
	}

	@Override
	String getFieldName() {
		return getConstraint().getFieldName();
	}

	@Override
	SqlFragment asEqualFragment(Object value, String alias) {
		return new ConstraintEqualFragment(getConstraint(), value, alias);
	}

	@Override
	SqlFragment asComparisonFragment(Comparison comparison, String alias) {
		return new ConstraintComparisonFragment(getConstraint(), comparison, alias);
	}

	@Override
	SqlFragment asContainsFragment(Contains contains, String alias) {
		return new ConstraintContainsFragment(getConstraint(), contains, alias);
	}

}
