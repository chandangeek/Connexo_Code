package com.elster.jupiter.orm.impl;

import com.elster.jupiter.conditions.Comparison;
import com.elster.jupiter.conditions.Contains;
import com.elster.jupiter.sql.util.SqlFragment;

abstract class FieldMapping {	
	abstract String getFieldName();
	abstract SqlFragment asEqualFragment(Object value , String alias);
	abstract SqlFragment asComparisonFragment(Comparison comparison , String alias);
	abstract SqlFragment asContainsFragment(Contains contains, String alias);
}
