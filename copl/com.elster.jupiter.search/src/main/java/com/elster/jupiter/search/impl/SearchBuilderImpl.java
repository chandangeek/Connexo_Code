package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
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
import java.util.stream.Collectors;

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
    private List<CriterionBuilder<T>> incompleteBuilders = new ArrayList<>();

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
        CriterionBuilderImpl builder = new CriterionBuilderImpl(property);
        this.incompleteBuilders.add(builder);
        return builder;
    }

    private void addCondition(CriterionBuilder<T> builder, SearchablePropertyCondition condition) {
        this.conditions.add(condition);
        this.incompleteBuilders.remove(builder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Finder<T> toFinder() {
        this.validateNoIncompleteBuilders();
        return (Finder<T>) this.searchDomain.finderFor(Collections.unmodifiableList(this.conditions));
    }

    private void validateNoIncompleteBuilders() {
        if (!this.incompleteBuilders.isEmpty()) {
            throw new IllegalStateException("At least one criterion remains unspecified, please call one of the CriterionBuilder methods");
        }
    }

    private class CriterionBuilderImpl implements CriterionBuilder<T> {
        private final SearchableProperty property;

        private CriterionBuilderImpl(SearchableProperty property) {
            super();
            this.property = property;
        }

        private List<Object> attemptConvertToValidValues(List<Object> values) {
            return values
                    .stream()
                    .map(this::attemptConvertToValidValue)
                    .collect(Collectors.toList());
        }

        private Object attemptConvertToValidValue(Object value) {
            try {
                if (this.property.getSpecification().isReference()) {
                    if (value instanceof String) {
                        String id = (String) value;
                        return this.property.getSpecification().getValueFactory().fromStringValue(id);
                    }
                    else {
                        return this.property.getSpecification().getValueFactory().valueFromDatabase(value);
                    }
                }
                else if (value instanceof String) {
                    return this.property.getSpecification().getValueFactory().fromStringValue((String) value);
                }
                else {
                    return value;
                }
            }
            catch (RuntimeException e) {
                return value;
            }
        }

        @Override
        public SearchBuilder<T> in(List<Object> values) throws InvalidValueException {
            this.validateValues(this.attemptConvertToValidValues(values), this.property.getSpecification());
            addCondition(
                    this,
                    new SearchablePropertyContains(
                            ListOperator.IN.contains(this.property.getSpecification().getName(), values),
                            this.property));
            return SearchBuilderImpl.this;
        }

        @Override
        public SearchBuilder<T> isEqualTo(Object value) throws InvalidValueException {
            Object actualValue = this.attemptConvertToValidValue(value);
            this.validateValue(actualValue, this.property.getSpecification());
            Operator operator;
            if (actualValue instanceof String) {
                operator = Operator.EQUALIGNORECASE;
            }
            else {
                operator = Operator.EQUAL;
            }
            addCondition(
                    this,
                    new SearchablePropertyComparison(
                            operator.compare(this.property.getSpecification().getName(), actualValue),
                            this.property));
            return SearchBuilderImpl.this;
        }

        @Override
        public SearchBuilder<T> isEqualToIgnoreCase(String value) throws InvalidValueException {
            this.validateValue(value, this.property.getSpecification());
            addCondition(
                    this,
                    new SearchablePropertyComparison(
                            Operator.EQUALIGNORECASE.compare(this.property.getSpecification().getName(), value),
                            this.property));
            return SearchBuilderImpl.this;
        }

        @Override
        public SearchBuilder<T> isNotEqualTo(Object value) throws InvalidValueException {
            return this.addConditionWithOperator(Operator.NOTEQUAL, value);
        }

        @Override
        public SearchBuilder<T> isLessThan(Object value) throws InvalidValueException {
            return this.addConditionWithOperator(Operator.LESSTHAN, value);
        }

        @Override
        public SearchBuilder<T> isLessThanOrEqualTo(Object value) throws InvalidValueException {
            return this.addConditionWithOperator(Operator.LESSTHANOREQUAL, value);
        }

        @Override
        public SearchBuilder<T> isGreaterThan(Object value) throws InvalidValueException {
            return this.addConditionWithOperator(Operator.GREATERTHAN, value);
        }

        @Override
        public SearchBuilder<T> isGreaterThanOrEqualTo(Object value) throws InvalidValueException {
            return this.addConditionWithOperator(Operator.GREATERTHANOREQUAL, value);
        }

        private SearchBuilder<T> addConditionWithOperator(Operator operator, Object value) throws InvalidValueException {
            Object actualValue = this.attemptConvertToValidValue(value);
            this.validateValue(actualValue, this.property.getSpecification());
            addCondition(
                    this,
                    new SearchablePropertyComparison(
                            operator.compare(this.property.getSpecification().getName(), actualValue),
                            this.property));
            return SearchBuilderImpl.this;
        }

        @Override
        public SearchBuilder<T> like(String wildCardPattern) throws InvalidValueException {
            this.validateValue(wildCardPattern, this.property.getSpecification());
            addCondition(
                    this,
                    new SearchablePropertyComparison(
                            Operator.LIKE.compare(this.property.getSpecification().getName(), toOracleSql(wildCardPattern)),
                            this.property));
            return SearchBuilderImpl.this;
        }

        @Override
        public SearchBuilder<T> likeIgnoreCase(String wildCardPattern) throws InvalidValueException {
            this.validateValue(wildCardPattern, this.property.getSpecification());
            addCondition(
                    this,
                    new SearchablePropertyComparison(
                            Operator.LIKEIGNORECASE.compare(this.property.getSpecification().getName(), toOracleSql(wildCardPattern)),
                            this.property));
            return SearchBuilderImpl.this;
        }

        private void validateValues(List<Object> values, PropertySpec specification) throws InvalidValueException {
            for (Object value : values) {
                this.validateValue(value, specification);
            }
        }

        private void validateValue(Object value, PropertySpec specification) throws InvalidValueException {
            specification.validateValueIgnoreRequired(value);
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