package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.rest.MessageSeeds;

import java.util.Arrays;
import java.util.List;

public enum SearchablePropertyOperator {
    LESS_THAN("<") {
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            if (Number.class.isAssignableFrom(searchableProperty.getSpecification().getValueFactory().getValueType())) {
                criterionBuilder.isLessThan(value);
            } else {
                super.appendSingle(searchableProperty, criterionBuilder, value);
            }
        }
    },
    LESS_OR_EQUAL_TO("<=") {
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            if (Number.class.isAssignableFrom(searchableProperty.getSpecification().getValueFactory().getValueType())) {
                criterionBuilder.isLessThanOrEqualTo(value);
            } else {
                super.appendSingle(searchableProperty, criterionBuilder, value);
            }
        }
    },
    GREATER_THAN(">") {
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            if (Number.class.isAssignableFrom(searchableProperty.getSpecification().getValueFactory().getValueType())) {
                criterionBuilder.isGreaterThan(value);
            } else {
                super.appendSingle(searchableProperty, criterionBuilder, value);
            }
        }
    },
    GREATER_OR_EQUAL_TO(">=") {
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            if (Number.class.isAssignableFrom(searchableProperty.getSpecification().getValueFactory().getValueType())) {
                criterionBuilder.isGreaterThanOrEqualTo(value);
            } else {
                super.appendSingle(searchableProperty, criterionBuilder, value);
            }
        }
    },
    EQUAL("==") {
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            if (String.class.isAssignableFrom(searchableProperty.getSpecification().getValueFactory().getValueType())) {
                criterionBuilder.likeIgnoreCase((String) value);
            } else if (Boolean.class.isAssignableFrom(searchableProperty.getSpecification().getValueFactory().getValueType())) {
                criterionBuilder.is((Boolean) value);
            } else {
                criterionBuilder.isEqualTo(value);
            }
        }

        @Override
        protected void appendList(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            criterionBuilder.in(values);
        }
    },
    NOT_EQUAL("!=") {
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            if (Boolean.class.isAssignableFrom(searchableProperty.getSpecification().getValueFactory().getValueType())) {
                criterionBuilder.is(!(Boolean) value);
            } else {
                criterionBuilder.isEqualTo(value);
            }
        }

        @Override
        protected void appendList(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            criterionBuilder.notIn(values);
        }
    },
    BETWEEN("BETWEEN") {
        @Override
        protected void appendList(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            // TODO some logic here
        }
    },;

    private String code;

    SearchablePropertyOperator(String code) {
        this.code = code;
    }

    public static SearchablePropertyOperator getOperatorForCode(String searchOperator) {
        return Arrays.stream(SearchablePropertyOperator.values())
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
        throw new InvalidValueException(MessageSeeds.INVALID_VALUE.getKey(), MessageSeeds.INVALID_VALUE.getDefaultFormat(), searchableProperty.getName());
    }

    protected void appendList(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
        throw new InvalidValueException(MessageSeeds.INVALID_VALUE.getKey(), MessageSeeds.INVALID_VALUE.getDefaultFormat(), searchableProperty.getName());
    }
}
