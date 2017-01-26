package com.elster.jupiter.search;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.search.impl.MessageSeeds;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public enum SearchablePropertyOperator {
    /**
     * The operator less than
     */
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
    /**
     * The operator less than or equal
     */
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
    /**
     * The operator greater than
     */
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
    /**
     * The operator greater than or equal to
     */
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
    /**
     * The operator greater is equal to
     */
    EQUAL("==") {
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            Class valueType = searchableProperty.getSpecification().getValueFactory().getValueType();
            if (String.class.isAssignableFrom(valueType)) {
                criterionBuilder.likeIgnoreCase((String) value);
            } else if (Boolean.class.isAssignableFrom(valueType)) {
                criterionBuilder.is((Boolean) value);
            } else if (Instant.class.isAssignableFrom(valueType)) {
                Instant lowerBound = roundToMinutes((Instant) value);
                criterionBuilder.isBetween(lowerBound, lowerBound.plus(1, ChronoUnit.MINUTES));
            } else if (Date.class.isAssignableFrom(valueType)){
                Instant lowerBound = roundToMinutes(((Date) value).toInstant());
                criterionBuilder.isBetween(Date.from(lowerBound), Date.from(lowerBound.plus(1, ChronoUnit.MINUTES)));
            } else {
                criterionBuilder.isEqualTo(value);
            }
        }

        private Instant roundToMinutes(Instant original){
            return ZonedDateTime.ofInstant(original, ZoneId.systemDefault())
                    .withSecond(0)
                    .withNano(0)
                    .with(ChronoField.MILLI_OF_SECOND, 0)
                    .toInstant();
        }

        @Override
        protected void appendList(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            criterionBuilder.in(values);
        }
    },
    /**
     * The operator is not equal to
     */
    NOT_EQUAL("!=") {
        @Override
        protected <T> void appendSingle(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, T value) throws InvalidValueException {
            if (Boolean.class.isAssignableFrom(searchableProperty.getSpecification().getValueFactory().getValueType())) {
                criterionBuilder.is(!(Boolean) value);
            } else {
                criterionBuilder.isNotEqualTo(value);
            }
        }

        @Override
        protected void appendList(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            criterionBuilder.notIn(values);
        }
    },
    /**
     * The operator 'between'
     */
    BETWEEN("BETWEEN") {
        @Override
        public void appendCriteria(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            if (values == null || values.size() != 2){ // min and max value
                throw new InvalidValueException(MessageSeeds.INVALID_VALUE.getKey(), MessageSeeds.INVALID_VALUE.getDefaultFormat(), searchableProperty.getName());
            }
            criterionBuilder.isBetween(values.get(0), values.get(1));
        }
    },
    /**
     * The operator 'is not defined'
     */
    ISDEFINED("ISDEFINED") {
        @Override
        public void appendCriteria(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            if (values != null && !values.isEmpty()){ // min and max value
                throw new InvalidValueException(MessageSeeds.INVALID_VALUE.getKey(), MessageSeeds.INVALID_VALUE.getDefaultFormat(), searchableProperty.getName());
            }
            criterionBuilder.isDefined();
        }

        @Override
        public boolean isUnary() {
            return true;
        }
    },
    /**
     * The operator 'is not defined'
     */
    ISNOTDEFINED("ISNOTDEFINED") {
        @Override
        public void appendCriteria(SearchableProperty searchableProperty, SearchBuilder.CriterionBuilder<?> criterionBuilder, List<Object> values) throws InvalidValueException {
            if (values != null && !values.isEmpty()){ // min and max value
                throw new InvalidValueException(MessageSeeds.INVALID_VALUE.getKey(), MessageSeeds.INVALID_VALUE.getDefaultFormat(), searchableProperty.getName());
            }
            criterionBuilder.isNotDefined();
        }

        @Override
        public boolean isUnary() {
            return true;
        }
    }
    ;

    private String code;

    SearchablePropertyOperator(String code) {
        this.code = code;
    }

    public static SearchablePropertyOperator getFromCode(String searchOperator) {
        return Arrays.stream(SearchablePropertyOperator.values())
                .filter(so -> so.code.equalsIgnoreCase(searchOperator))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported search operator '" + searchOperator + "'"));
    }

    public String code() {
        return this.code;
    }

    /**
     * Unary operators do not expect arguments
     * @return true if the operator is unary, false if not (=default)
     */
    public boolean isUnary(){
        return false;
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
