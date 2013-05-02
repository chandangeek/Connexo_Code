package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.conditions.Comparison;
import com.elster.jupiter.conditions.Contains;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.sql.util.SqlFragment;

public class ForwardConstraintMapping extends ConstraintMapping {
	
	public ForwardConstraintMapping(TableConstraint constraint) {
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

}
