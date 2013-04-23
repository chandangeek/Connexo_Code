package com.elster.jupiter.domain.util.impl;

import java.util.List;

import com.elster.jupiter.conditions.Club;
import com.elster.jupiter.conditions.Condition;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.orm.QueryExecutor;

class QueryImpl<T> implements Query<T> {
	private final QueryExecutor<T> queryExecutor;
	private boolean eager = false;
	private String[] exceptions;
	
	QueryImpl(QueryExecutor<T> queryExecutor) {
		this.queryExecutor = queryExecutor;
	}
	
	@Override
	public List<T> select(Condition condition,String ... orderBy) {	
		return queryExecutor.select(condition,orderBy,eager, exceptions);
	}
	
	@Override
	public List<T> select(Condition condition, int from, int to,String... orderBy) {
		return queryExecutor.select(condition,orderBy,eager,exceptions,from,to);
	}
	
	@Override
	public T get(Object... key) {
		return queryExecutor.get(key);
	}

	@Override
	public Club toClub(Condition condition, String ... fieldNames) {
		return queryExecutor.toClub(condition,fieldNames);
	}

	@Override
	public boolean hasField(String key) {
		return queryExecutor.hasField(key);
	}

	@Override
	public Object convert(String fieldName, String value) {
		return queryExecutor.convert(fieldName, value);
	}
			
	@Override
	public void setLazy(String... includes) {
		this.eager = false;
		this.exceptions = includes;
		
	}

	@Override
	public void setEager(String... excludes) {
		this.eager = true;
		this.exceptions = excludes;
	}


}
