package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.groups.QueryGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

abstract class QueryGroupCondition {

    enum Fields {
        GROUP("group"),
        PROPERTY("searchableProperty"),
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

    private final DataModel dataModel;

    @IsPresent
    private Reference<QueryGroup<?>> group = ValueReference.absent();
    @NotNull
    @Size(min = 1, max = Table.SHORT_DESCRIPTION_LENGTH)
    private String searchableProperty;
    @NotNull
    private SearchablePropertyOperator operator;
    @Valid
    private List<QueryGroupConditionValue> conditionValues = new ArrayList<>();
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    QueryGroupCondition(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    QueryGroupCondition init(QueryGroup<?> queryGroup, String searchableProperty,
                             SearchablePropertyOperator searchablePropertyOperator, List<String> values) {
        this.group.set(queryGroup);
        this.searchableProperty = searchableProperty;
        this.operator = searchablePropertyOperator;
        this.initConditionValues(values);
        return this;
    }

    private void initConditionValues(List<String> conditionValues) {
        List<QueryGroupConditionValue> values = conditionValues.stream()
                .map(v -> dataModel.getInstance(getConditionValueApiClass()).init(this, v))
                .collect(Collectors.toList());
        this.conditionValues.addAll(values);
    }

    SearchablePropertyValue.ValueBean toValueBean() {
        return new SearchablePropertyValue.ValueBean(searchableProperty, operator, conditionValues.stream().map(QueryGroupConditionValue::getValue).collect(Collectors.toList()));
    }

    String getSearchableProperty() {
        return searchableProperty;
    }

    SearchablePropertyOperator getOperator() {
        return operator;
    }

    List<QueryGroupConditionValue> getConditionValues() {
        return Collections.unmodifiableList(conditionValues);
    }

    void delete() {
        this.conditionValues.clear();
        this.dataModel.remove(this);
    }

    abstract Class<? extends QueryGroupConditionValue> getConditionValueApiClass();
}
