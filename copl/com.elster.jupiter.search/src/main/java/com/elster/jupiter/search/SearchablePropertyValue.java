/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search;

import com.elster.jupiter.properties.InvalidValueException;

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
        public String propertyName;
        public SearchablePropertyOperator operator;
        public List<String> values;
    }
}
