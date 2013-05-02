package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.conditions.Comparison;
import com.elster.jupiter.conditions.Contains;
import com.elster.jupiter.sql.util.SqlFragment;

public abstract class FieldMapping {	
	public abstract String getFieldName();
	public abstract SqlFragment asEqualFragment(Object value , String alias);
	public abstract SqlFragment asComparisonFragment(Comparison comparison , String alias);
	public abstract SqlFragment asContainsFragment(Contains contains, String alias);
}
