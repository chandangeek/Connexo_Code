package com.elster.jupiter.domain.util.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;

class QueryImpl<T> implements Query<T> {

    private final QueryExecutor<T> queryExecutor;
    private Boolean eager;
    private String[] exceptions;

    QueryImpl(QueryExecutor<T> queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    @Override
    @Deprecated
    public List<T> select(Condition condition, String order, String... orders) {
        return select(condition, Order.from(order,orders));
    }

    @Override
    @Deprecated
    public List<T> select(Condition condition, int from, int to, String order, String... orders) {
        return select(condition, from, to, Order.from(order,orders));
    }

    @Override
    public Optional<T> get(Object... key) {
        // override default eager behavior
        return queryExecutor.get(key, eager == null ? true : eager, exceptions);
    }

    @Override
    public Subquery asSubquery(Condition condition, String... fieldNames) {
        return new SubqueryImpl(queryExecutor.asFragment(condition, fieldNames));
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

    private boolean isEager() {
        return eager != null && eager;
    }

    @Override
    public List<String> getQueryFieldNames() {
        return queryExecutor.getQueryFieldNames();
    }

    @Override
    public Class<?> getType(String fieldName) {
        return queryExecutor.getType(fieldName);
    }

    @Override
    public Date getEffectiveDate() {
    	return queryExecutor.getEffectiveDate();
    }
    
    @Override
    public void setEffectiveDate(Date date) {
    	queryExecutor.setEffectiveDate(date);
    }

	@Override
	public List<T> select(Condition condition, Order... orders) {
		return queryExecutor.select(condition, orders, isEager(), exceptions);
	}

	@Override
	public List<T> select(Condition condition, int from, int to, Order... orders) {
		return  queryExecutor.select(condition, orders, isEager(), exceptions, from, to);
	}
	

}
