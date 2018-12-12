/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;

import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlFragment;

public class SubqueryImpl implements Subquery {
	
	private final SqlFragment fragment;
	
	SubqueryImpl(SqlFragment fragment) {
		this.fragment = fragment;
	}

	@Override
	public SqlFragment toFragment() {
		return fragment;
	}
	
}
