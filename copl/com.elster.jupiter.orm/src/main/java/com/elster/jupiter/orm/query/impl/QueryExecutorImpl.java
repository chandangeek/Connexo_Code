/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;

import com.elster.jupiter.orm.NotUniqueException;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlFragment;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class QueryExecutorImpl<T> implements QueryExecutor<T> {

    private final JoinTreeNode<T> root;
    private final AliasFactory aliasFactory = new AliasFactory();
    private Condition restriction = Condition.TRUE;
    private Instant effectiveInstant;

    public QueryExecutorImpl(DataMapperImpl<T> mapper) {
        RootDataMapper<T> rootDataMapper = new RootDataMapper<>(mapper);
        aliasFactory.setBase(rootDataMapper.getAlias());
        aliasFactory.getAlias();
        this.root = new JoinTreeNode<>(rootDataMapper);
    }

    public <R> void add(DataMapperImpl<R> newMapper) {
        aliasFactory.setBase(newMapper.getAlias());
        boolean result = root.addMapper(newMapper, aliasFactory);
        if (!result) {
            throw new IllegalArgumentException("No referential key match for " + newMapper.getTable().getName());
        }
    }

    public List<T> select(Condition condition, Order[] ordering, boolean eager, String[] exceptions, int from, int to) {
        try {
            return new JoinExecutor<>(root.copy(), getEffectiveDate(), from, to).select(restriction.and(condition), ordering, eager, exceptions);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    public long count(Condition condition) {
        try {
            return new JoinExecutor<>(root.copy(), getEffectiveDate()).count(restriction.and(condition));
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    @Override
    public boolean hasField(String fieldName) {
        return root.hasWhereField(fieldName);
    }

    @Override
    public Class<?> getType(String fieldName) {
        return root.getType(fieldName);
    }

    @Override
    public Subquery asSubquery(Condition condition, String... fieldNames) {
        return new SubqueryImpl(asFragment(condition, fieldNames));
    }

    @Override
    public SqlFragment asFragment(Condition condition, String... fieldNames) {
        return new JoinExecutor<>(root.copy(), getEffectiveDate()).getSqlBuilder(restriction.and(condition), fieldNames, Order.NOORDER);
    }

    @Override
    public SqlFragment asFragment(Condition condition, String[] fieldNames, Order[] orderBy) {
        return new JoinExecutor<>(root.copy(), getEffectiveDate()).getSqlBuilder(restriction.and(condition), fieldNames, orderBy);
    }

    public SqlFragment asFragment(Condition condition, int from, int to, String[] fieldNames, Order[] orderBy) {
        return new JoinExecutor<>(root.copy(), getEffectiveDate(), from, to).getSqlBuilder(restriction.and(condition), fieldNames, orderBy);
    }

    public Object convert(String fieldName, String value) {
        ColumnImpl column = root.getColumnForField(fieldName);
        if (column != null) {
            return column.convert(value);
        }
        throw new IllegalArgumentException("No mapper or column for " + fieldName);
    }

    @Override
    public Optional<T> get(Object[] key, boolean eager, String[] exceptions) {
        List<ColumnImpl> primaryKeyColumns = this.root.getTable().getPrimaryKeyColumns();
        if (primaryKeyColumns.size() != key.length) {
            throw new IllegalArgumentException("Key mismatch");
        }
        Condition condition = Condition.TRUE;
        int i = 0;
        for (ColumnImpl column : primaryKeyColumns) {
            String fieldName = column.getFieldName() == null ? column.getName() : column.getFieldName();
            condition = condition.and(Operator.EQUAL.compare(fieldName, key[i++]));
        }
        List<T> result = this.select(condition, new Order[]{}, eager, exceptions);
        if (result.size() > 1) {
            throw new NotUniqueException(Arrays.toString(key));
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<String> getQueryFieldNames() {
        return root.getQueryFields();
    }

    @Override
    public void setRestriction(Condition condition) {
        restriction = restriction.and(condition);
    }

    @Override
    public Optional<T> getOptional(Object... values) {
        return get(values, true, new String[0]);
    }

    @Override
    public T getExisting(Object... values) {
        return getOptional(values).get();
    }

    @Override
    public Instant getEffectiveDate() {
        if (effectiveInstant == null) {
            effectiveInstant = this.root.getTable().getDataModel().getClock().instant();
        }
        return effectiveInstant;
    }

    @Override
    public void setEffectiveDate(Instant instant) {
        this.effectiveInstant = instant;
    }

    @Override
    public List<T> select(Condition condition, Order... orders) {
        return select(condition, orders, true, new String[0]);
    }

    @Override
    public List<T> select(Condition condition, Order[] orders, boolean eager, String[] exceptions) {
        return select(condition, orders, eager, exceptions, 0, 0);
    }
}
