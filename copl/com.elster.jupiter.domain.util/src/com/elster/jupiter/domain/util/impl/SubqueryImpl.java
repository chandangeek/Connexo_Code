package com.elster.jupiter.domain.util.impl;

import com.elster.jupiter.conditions.Subquery;
import com.elster.jupiter.sql.util.SqlFragment;

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
