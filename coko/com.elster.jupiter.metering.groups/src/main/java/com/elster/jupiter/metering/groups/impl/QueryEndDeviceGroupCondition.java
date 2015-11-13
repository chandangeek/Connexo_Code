package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QueryEndDeviceGroupCondition {

    enum Fields {
        GROUP("endDeviceGroup"),
        SEARCHABLE_PROPERTY("searchableProperty"),
        OPERATOR("operator"),
        CONDITION_VALUES("conditionValues");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private Reference<QueryEndDeviceGroup> endDeviceGroup = ValueReference.absent();
    @NotNull
    @Size(min = 1, max = Table.SHORT_DESCRIPTION_LENGTH)
    private String searchableProperty;
    @NotNull
    private SearchablePropertyOperator operator;
    @Valid
    private List<QueryEndDeviceGroupConditionValue> conditionValues = new ArrayList<>();

    QueryEndDeviceGroupCondition() {
    }

    QueryEndDeviceGroupCondition init(QueryEndDeviceGroup queryEndDeviceGroup, String searchableProperty, SearchablePropertyOperator searchablePropertyOperator, List<String> values) {
        this.endDeviceGroup.set(queryEndDeviceGroup);
        this.searchableProperty = searchableProperty;
        this.operator = searchablePropertyOperator;
        this.initConditionValues(values);
        return this;
    }

    private void initConditionValues(List<String> conditionValues) {
        List<QueryEndDeviceGroupConditionValue> values = conditionValues.stream()
                .map(v -> new QueryEndDeviceGroupConditionValue().init(this, v))
                .collect(Collectors.toList());
        this.conditionValues.addAll(values);
    }

    public SearchablePropertyValue.ValueBean toValueBean() {
        SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
        valueBean.propertyName = searchableProperty;
        valueBean.operator = operator;
        valueBean.values = conditionValues.stream().map(QueryEndDeviceGroupConditionValue::getValue).collect(Collectors.toList());
        return valueBean;
    }

    String getSearchableProperty() {
        return searchableProperty;
    }

    SearchablePropertyOperator getOperator() {
        return operator;
    }

    List<QueryEndDeviceGroupConditionValue> getConditionValues() {
        return conditionValues;
    }
}
