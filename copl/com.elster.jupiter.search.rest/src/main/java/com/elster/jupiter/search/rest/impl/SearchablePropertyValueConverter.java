package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.search.rest.MessageSeeds;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Converters user's input data (in JSON format) to {@link SearchablePropertyValue} instance. <br />
 * JSON format: <br />
 * <code>
 * { <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;"property": "specification_name", <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;"value": [<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{"operator": "==", "criteria": "*"} <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;]<br/>
 * }
 * </code><br/>
 * In version 10.1 we assume that the 'value' can contain only one object, see ({@link #convert(SearchableProperty, JsonQueryFilter)}).
 */
public class SearchablePropertyValueConverter implements Function<JsonNode, SearchablePropertyValue.ValueBean> {
    static String OPERATOR_FIELD = "operator";
    static String CRITERIA_FIELD = "criteria";

    public static SearchablePropertyValue convert(SearchableProperty property, JsonQueryFilter filter) {
        SearchablePropertyValue propertyValue = new SearchablePropertyValue(property);
        // here we expect that user send just one operator-value pair
        propertyValue.setValueBean(filter.getPropertyList(property.getName(), new SearchablePropertyValueConverter())
                .stream()
                .findFirst()
                .orElse(new SearchablePropertyValue.ValueBean()));
        return propertyValue;
    }

    @Override
    public SearchablePropertyValue.ValueBean apply(JsonNode node) {
        if (node != null) {
            SearchablePropertyValue.ValueBean propertyValue = new SearchablePropertyValue.ValueBean();
            mapOperatorField(node, propertyValue);
            mapCriteriaField(node, propertyValue);
            if (propertyValue.operator == null || propertyValue.values == null) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, "value");
            }
            return propertyValue;
        }
        return null;
    }

    private void mapOperatorField(JsonNode node, SearchablePropertyValue.ValueBean propertyBean) {
        JsonNode operatorNode = node.get(OPERATOR_FIELD);
        if (operatorNode != null && operatorNode.isTextual()) {
            propertyBean.operator = SearchablePropertyOperator.getFromCode(operatorNode.textValue());
        }
    }

    private void mapCriteriaField(JsonNode node, SearchablePropertyValue.ValueBean propertyBean) {
        JsonNode criteriaField = node.get(CRITERIA_FIELD);
        if (criteriaField != null) {
            List<String> values;
            if (criteriaField.isArray()) {
                values = new ArrayList<>();
                for (JsonNode singleCriteria : criteriaField) {
                    values.add(getSingleCriteriaAsString(singleCriteria));
                }
            } else {
                values = Collections.singletonList(getSingleCriteriaAsString(criteriaField));
            }
            propertyBean.values = values;
        }
    }

    private String getSingleCriteriaAsString(JsonNode singleCriteria) {
        if (singleCriteria.isTextual()) {
            return singleCriteria.textValue();
        } else if (singleCriteria.isNumber()) {
            return String.valueOf(singleCriteria.numberValue().longValue());
        }
        return singleCriteria.toString();
    }
}
