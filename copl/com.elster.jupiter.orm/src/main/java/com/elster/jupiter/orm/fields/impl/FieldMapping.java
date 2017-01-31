/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import java.util.List;

import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlFragment;

public abstract class FieldMapping {	
	public abstract String getFieldName();
	public abstract SqlFragment asEqualFragment(Object value , String alias);
	public abstract SqlFragment asComparisonFragment(Comparison comparison , String alias);
	public abstract SqlFragment asContainsFragment(Contains contains, String alias);
	public abstract List<ColumnImpl> getColumns();
}
