package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.spi.QueryProvider;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.conditions.Condition;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

abstract class SimpleQueryProvider<T> implements QueryProvider<T> {

    private Supplier<Query<T>> basicQuerySupplier;

    @Override
    public SimpleQueryProvider<T> init(Supplier<Query<T>> basicQuerySupplier) {
        this.basicQuerySupplier = basicQuerySupplier;
        return this;
    }

    @Override
    public List<T> executeQuery(Instant instant, List<SearchablePropertyCondition> conditions) {
        return executeQuery(instant, conditions, -1, 0);
    }

    @Override
    public List<T> executeQuery(Instant instant, List<SearchablePropertyCondition> conditions, int start, int limit) {
        Optional<Condition> condition = conditions.stream().map(SearchablePropertyCondition::getCondition).reduce(Condition::and);
        if (start > -1) {
            return basicQuerySupplier.get().select(condition.orElse(Condition.TRUE), start + 1, start + limit + 1);
        } else {
            return basicQuerySupplier.get().select(condition.orElse(Condition.TRUE));
        }
    }

    @Override
    public Query<T> getQuery(List<SearchablePropertyCondition> conditions) {
        Optional<Condition> condition = conditions.stream().map(SearchablePropertyCondition::getCondition).reduce(Condition::and);
        Query<T> basicQuery = basicQuerySupplier.get();
        basicQuery.setRestriction(condition.orElse(Condition.TRUE));
        return basicQuery;
    }
}
