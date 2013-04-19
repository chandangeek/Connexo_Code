package com.elster.jupiter.orm.impl;

import com.elster.jupiter.conditions.Club;
import com.elster.jupiter.conditions.Condition;
import com.elster.jupiter.sql.util.SqlBuilder;

class SubQueryExecutor implements Club {
	private final QueryExecutorImpl<?> base;
	private final Condition condition;
	private final String[] fieldNames;
	
	public SubQueryExecutor(QueryExecutorImpl<?> base , Condition condition , String [] fieldNames) {
		this.base = base;
		this.condition = condition;
		this.fieldNames = fieldNames;
	}
	
	SqlBuilder getSqlBuilder() {
		return base.getSqlBuilder(condition , fieldNames);
	}
	
}
