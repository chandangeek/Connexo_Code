/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

class QueryImpl<T> implements Query<T> {

    private final QueryExecutor<T> queryExecutor;
    private Boolean eager;
    private String[] exceptions;

    QueryImpl(QueryExecutor<T> queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    @Override
    public Optional<T> get(Object... key) {
        // override default eager behavior
        return queryExecutor.get(key, eager == null ? true : eager, exceptions);
    }

    @Override
    public Subquery asSubquery(Condition condition, String... fieldNames) {
        return queryExecutor.asSubquery(condition, fieldNames);
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
    public Instant getEffectiveDate() {
        return queryExecutor.getEffectiveDate();
    }

    @Override
    public void setEffectiveDate(Instant instant) {
        queryExecutor.setEffectiveDate(instant);
    }

    @Override
    public List<T> select(Condition condition, Order... orders) {
        return queryExecutor.select(condition, orders, isEager(), exceptions);
    }

    @Override
    public List<T> select(Condition condition, int from, int to, Order... orders) {
        return queryExecutor.select(condition, orders, isEager(), exceptions, from, to);
    }

    @Override
    public void setRestriction(Condition condition) {
        queryExecutor.setRestriction(condition);
    }
}
