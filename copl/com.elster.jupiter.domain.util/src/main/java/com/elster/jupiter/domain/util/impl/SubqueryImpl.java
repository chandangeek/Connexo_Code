package com.elster.jupiter.domain.util.impl;

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
