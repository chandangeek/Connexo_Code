/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.rest;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.search.rest.impl.PropertyInfo;
import com.elster.jupiter.search.rest.impl.SearchCriterionInfoFactory;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Info object for displaying saved search criterias.
 */
public class SearchCriteriaVisualizationInfo extends PropertyInfo {
    public List<ValueInfo> value;

    public static class ValueInfo {
        public String operator;
        public List<String> criteria;
    }

    public static SearchCriteriaVisualizationInfo from(SearchableProperty searchableProperty, SearchablePropertyValue.ValueBean valueBean) {
        SearchCriteriaVisualizationInfo info = new SearchCriteriaVisualizationInfo();
        if (searchableProperty != null) {
            PropertySpec propertySpec = searchableProperty.getSpecification();
            info.name = searchableProperty.getName();
            info.displayValue = searchableProperty.getDisplayName();
            if (searchableProperty.getGroup().isPresent()) {
                info.group = new IdWithDisplayValueInfo();
                info.group.id = searchableProperty.getGroup().get().getId();
                info.group.displayValue = searchableProperty.getGroup().get().getDisplayName();
            }
            info.selectionMode = searchableProperty.getSelectionMode();
            info.visibility = searchableProperty.getVisibility();
            info.type = propertySpec.getValueFactory().getValueType().getSimpleName();
            info.factoryName = propertySpec.getValueFactory().getClass().getName();
            PropertySpecPossibleValues possibleValuesOrNull = propertySpec.getPossibleValues();
            if (possibleValuesOrNull != null) {
                List<?> possibleValues = possibleValuesOrNull.getAllValues();
                info.values = possibleValues.stream()
                        .map(v -> SearchCriterionInfoFactory.asJsonValueObject(searchableProperty.toDisplay(v), v))
                        .sorted((v1, v2) -> v1.displayValue.compareToIgnoreCase(v2.displayValue))
                        .collect(toList());
            }
        }
        if (valueBean != null) {
            SearchCriteriaVisualizationInfo.ValueInfo value = new SearchCriteriaVisualizationInfo.ValueInfo();
            value.operator = valueBean.operator.code();
            value.criteria = valueBean.values;
            info.value = Collections.singletonList(value);
        }
        return info;
    }
}
