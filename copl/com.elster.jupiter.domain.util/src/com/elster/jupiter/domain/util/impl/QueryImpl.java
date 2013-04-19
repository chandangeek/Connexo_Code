package com.elster.jupiter.domain.util.impl;

import java.util.List;

import com.elster.jupiter.conditions.Club;
import com.elster.jupiter.conditions.Condition;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.orm.QueryExecutor;

class QueryImpl<T> implements Query<T> {
	private final QueryExecutor<T> queryExecutor;
	
	QueryImpl(QueryExecutor<T> queryExecutor) {
		this.queryExecutor = queryExecutor;
	}
	
	@Override
	public List<T> where(Condition condition,String ... includes) {	
		return queryExecutor.where(condition,includes);
	}

	@Override
	public List<T> where(Condition condition,int from , int to , String ... includes) {	
		return queryExecutor.where(condition,from, to,includes);
	}

	@Override
	public List<T> eagerWhere(Condition condition,String ... excludes) {	
		return queryExecutor.eagerWhere(condition,excludes);
	}
	
	@Override
	public List<T> eagerWhere(Condition condition,int from , int to , String ... excludes) {	
		return queryExecutor.eagerWhere(condition,from, to, excludes);
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
	public Club toClub(Condition condition, String ... fieldNames) {
		return queryExecutor.toClub(condition,fieldNames);
	}

}
