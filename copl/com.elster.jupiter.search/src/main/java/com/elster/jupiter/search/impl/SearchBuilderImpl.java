package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.toOracleSql;

/**
 * Provides an implementation for the {@link SearchBuilder} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-28 (09:05)
 */
public class SearchBuilderImpl<T> implements SearchBuilder<T> {

    private final SearchDomain searchDomain;
    private final List<SearchablePropertyCondition> conditions = new ArrayList<>();

    public SearchBuilderImpl(SearchDomain searchDomain) {
        super();
        this.searchDomain = searchDomain;
    }

    @Override
    public SearchDomain getDomain() {
        return this.searchDomain;
    }

    @Override
    public CriterionBuilder<T> where(SearchableProperty property) {
        return new CriterionBuilderImpl(property);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Finder<T> complete() {
        return (Finder<T>) this.searchDomain.finderFor(Collections.unmodifiableList(this.conditions));
    }

    private class CriterionBuilderImpl implements CriterionBuilder<T> {
        private final SearchableProperty property;

        private CriterionBuilderImpl(SearchableProperty property) {
            super();
            this.property = property;
        }

        @Override
        public SearchBuilder<T> in(List<Object> values) {
            conditions.add(
                    new SearchablePropertyContains(
                            ListOperator.IN.contains(this.property.getSpecification().getName(), values),
                            this.property));
            return SearchBuilderImpl.this;
        }

        @Override
        public SearchBuilder<T> isEqualTo(Object value) {
            conditions.add(
                    new SearchablePropertyComparison(
                            Operator.EQUAL.compare(this.property.getSpecification().getName(), value),
                            this.property));
            return SearchBuilderImpl.this;
        }

        @Override
        public SearchBuilder<T> isEqualToIgnoreCase(String value) {
            conditions.add(
                    new SearchablePropertyComparison(
                            Operator.EQUALIGNORECASE.compare(this.property.getSpecification().getName(), value),
                            this.property));
            return SearchBuilderImpl.this;
        }

        @Override
        public SearchBuilder<T> like(String wildCardPattern) {
            conditions.add(
                    new SearchablePropertyComparison(
                            Operator.LIKE.compare(this.property.getSpecification().getName(), toOracleSql(wildCardPattern)),
                            this.property));
            return SearchBuilderImpl.this;
        }

        @Override
        public SearchBuilder<T> likeIgnoreCase(String wildCardPattern) {
            conditions.add(
                    new SearchablePropertyComparison(
                            Operator.LIKEIGNORECASE.compare(this.property.getSpecification().getName(), toOracleSql(wildCardPattern)),
                            this.property));
            return SearchBuilderImpl.this;
        }

    }

    /**
     * Provides an implementation for the {@link SearchablePropertyCondition} interface
     * that compares a {@link SearchableProperty} against a single value using an {@link Operator}.
     */
    private class SearchablePropertyComparison implements SearchablePropertyCondition {
        private final Comparison comparison;
        private final SearchableProperty property;

        private SearchablePropertyComparison(Comparison comparison, SearchableProperty property) {
            super();
            this.comparison = comparison;
            this.property = property;
        }

        @Override
        public SearchableProperty getProperty() {
            return this.property;
        }

        @Override
        public Condition getCondition() {
            return this.comparison;
        }

    }

    /**
     * Provides an implementation for the {@link SearchablePropertyCondition} interface
     * that compares a {@link SearchableProperty} against a list of values using
     * the {@link ListOperator#IN in operator}.
     */
    private class SearchablePropertyContains implements SearchablePropertyCondition {
        private final Contains contains;
        private final SearchableProperty property;

        private SearchablePropertyContains(Contains contains, SearchableProperty property) {
            super();
            this.contains = contains;
            this.property = property;
        }

        @Override
        public SearchableProperty getProperty() {
            return this.property;
        }

        @Override
        public Condition getCondition() {
            return this.contains;
        }

    }

}