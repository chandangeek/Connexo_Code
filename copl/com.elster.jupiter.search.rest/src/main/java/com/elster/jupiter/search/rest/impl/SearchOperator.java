package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.rest.MessageSeeds;

import java.util.Arrays;
import java.util.List;

// TODO different handling for string, number, boolean
public enum SearchOperator {
    LESS_THAN("<") {
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            criterionBuilder.isLessThan(value);
        }

        @Override
        protected void appendList(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            throw new InvalidValueException(MessageSeeds.INVALID_VALUE.getKey(), MessageSeeds.INVALID_VALUE.getDefaultFormat(), searchableProperty.getName());
        }
    },
    LESS_OR_EQUAL_TO("<=") {
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            criterionBuilder.isLessThanOrEqualTo(value);
        }

        @Override
        protected void appendList(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            throw new InvalidValueException(MessageSeeds.INVALID_VALUE.getKey(), MessageSeeds.INVALID_VALUE.getDefaultFormat(), searchableProperty.getName());
        }
    },
    GREATER_THAN(">") {
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            criterionBuilder.isGreaterThan(value);
        }

        @Override
        protected void appendList(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            throw new InvalidValueException(MessageSeeds.INVALID_VALUE.getKey(), MessageSeeds.INVALID_VALUE.getDefaultFormat(), searchableProperty.getName());
        }
    },
    GREATER_OR_EQUAL_TO(">=") {
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            criterionBuilder.isGreaterThanOrEqualTo(value);
        }

        @Override
        protected void appendList(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            throw new InvalidValueException(MessageSeeds.INVALID_VALUE.getKey(), MessageSeeds.INVALID_VALUE.getDefaultFormat(), searchableProperty.getName());
        }
    },
    EQUAL("==") {
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            criterionBuilder.isEqualTo(value);
        }

        @Override
        protected void appendList(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            criterionBuilder.in(values);
        }
    },
    NOT_EQUAL("!=") {
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            criterionBuilder.isNotEqualTo(value);
        }

        @Override
        protected void appendList(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            criterionBuilder.notIn(values);
        }
    },
    BETWEEN("BETWEEN"){
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            throw new InvalidValueException(MessageSeeds.INVALID_VALUE.getKey(), MessageSeeds.INVALID_VALUE.getDefaultFormat(), searchableProperty.getName());
        }

        @Override
        protected void appendList(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            // TODO some logic here
        }
    },

    ;

    private String code;

    SearchOperator(String code) {
        this.code = code;
    }

    public static SearchOperator getOperatorForCode(String searchOperator) {
        return Arrays.stream(SearchOperator.values())
                .filter(so -> so.code.equalsIgnoreCase(searchOperator))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported search operator " + searchOperator));
    }

    public String code() {
        return this.code;
    }

    public void appendCriteria(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
        if (values == null || values.isEmpty()) {
            throw new InvalidValueException(MessageSeeds.INVALID_VALUE.getKey(), MessageSeeds.INVALID_VALUE.getDefaultFormat(), searchableProperty.getName());
        }
        if (searchableProperty.getSelectionMode() == SearchableProperty.SelectionMode.SINGLE) {
            if (values.size() != 1) {
                throw new InvalidValueException(MessageSeeds.INVALID_VALUE.getKey(), MessageSeeds.INVALID_VALUE.getDefaultFormat(), searchableProperty.getName());
            }
            appendSingle(searchableProperty, criterionBuilder, values.get(0));
        } else {
            appendList(searchableProperty, criterionBuilder, values);
        }
    }

    protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
        // do nothing by default
    }

    protected void appendList(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
        // do nothing by default
    }
}
