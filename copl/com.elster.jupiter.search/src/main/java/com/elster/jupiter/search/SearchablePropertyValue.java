package com.elster.jupiter.search;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.util.streams.Predicates;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class SearchablePropertyValue {
    private final SearchableProperty property;
    private ValueBean valueBean; // Just a single value for 10.1 version

    public SearchablePropertyValue(SearchableProperty property) {
        this.property = property;
    }

    public SearchablePropertyValue(SearchableProperty property, ValueBean valueBean) {
        this(property);
        this.valueBean = valueBean;
    }

    public void setValueBean(ValueBean valueBean){
        this.valueBean = valueBean;
        this.valueBean.propertyName = this.property.getName();
    }

    public List<Object> getValues() {
        return valueBean.values == null ? Collections.emptyList() : valueBean.values
                .stream()
                .map(str -> this.property.getSpecification().getValueFactory().fromStringValue(str))
                .collect(Collectors.toList());
    }

    public SearchablePropertyConstriction asConstriction(){
        return SearchablePropertyConstriction.withValues(this.property, getValues());
    }

    public void addAsCondition(SearchBuilder searchBuilder) throws InvalidValueException {
        SearchBuilder.CriterionBuilder criterionBuilder = searchBuilder.where(this.property);
        this.valueBean.operator.appendCriteria(this.property, criterionBuilder, getValues());
    }

    public SearchableProperty getProperty() {
        return property;
    }

    public ValueBean getValueBean() {
        return valueBean;
    }

    /**
     * Serializable representation of searchable property value
     */
    public static class ValueBean {
        private String propertyName;
        private SearchablePropertyOperator operator;
        private List<String> values = new ArrayList<>();

        public ValueBean() {}

        public ValueBean(String propertyName, SearchablePropertyOperator operator, String... values){
            this(propertyName, operator, (values == null ? new ArrayList<>() : Arrays.asList(values)));
        }

        public ValueBean(String propertyName, SearchablePropertyOperator operator, List<String> values){
            if (operator == null || values == null){
                throw new IllegalArgumentException();
            }
            this.propertyName = propertyName;
            this.operator = operator;
            this.setValues(values);
        }

        public boolean isValid(){
            return (this.operator != null)  && (this.getOperator().isUnary() || !values.isEmpty());
        }

        @JsonGetter
        public String getPropertyName() {
            return propertyName;
        }

        @JsonGetter
        public SearchablePropertyOperator getOperator() {
            return operator;
        }

        public List<String> getValues() {
            return values;
        }

        @JsonSetter
        public void setValues(String... values) {
            setValues(values == null ? Collections.emptyList() : Arrays.asList(values));
        }

        @JsonSetter
        public void setValues(List<String> values) {
            values.stream().filter(Predicates.not(String::isEmpty)).forEach(this.values::add);
        }

    }
}
