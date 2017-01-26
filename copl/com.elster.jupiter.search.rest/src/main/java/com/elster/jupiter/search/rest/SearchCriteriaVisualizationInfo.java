package com.elster.jupiter.search.rest;

import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.search.rest.impl.PropertyInfo;

import java.util.Collections;
import java.util.List;

/**
 * Info object for displaying saved search criterias.
 */
public class SearchCriteriaVisualizationInfo extends PropertyInfo {

    public List<ValueInfo> value;

    public static class ValueInfo {
        public String operator;
        public List<String> criteria;
    }

    public SearchCriteriaVisualizationInfo(SearchableProperty property, SearchablePropertyValue.ValueBean valueBean) {
        super(property);
        super.withSpecDetails();  // Initialize spec details
        super.withPossibleValues(); // Initialize possible values
        if (valueBean != null) {
            SearchCriteriaVisualizationInfo.ValueInfo value = new SearchCriteriaVisualizationInfo.ValueInfo();
            value.operator = valueBean.getOperator().code();
            if (!valueBean.getOperator().isUnary()) {
                value.criteria = valueBean.getValues();
            }
            this.value = Collections.singletonList(value);
        }
    }
}
