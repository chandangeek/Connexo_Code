package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.rest.MessageSeeds;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class SearchPropertyValue {
    private final SearchableProperty property;
    private SearchOperator searchOperator;
    private List<Object> values;

    SearchPropertyValue(SearchableProperty property) {
        this.property = property;
    }

    public static List<SearchPropertyValue> convert(SearchableProperty property, JsonQueryFilter filter) {
        return filter.getPropertyList(property.getName(), new SearchPropertyValueMapper(property));
    }

    public SearchOperator getSearchOperator() {
        return this.searchOperator;
    }

    public void setSearchOperator(SearchOperator searchOperator) {
        this.searchOperator = searchOperator;
    }

    public List<?> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }
    private void validate() {
        if (this.searchOperator == null || this.values == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, "value");
        }
    }

    public void addAsCriteria(SearchBuilder.CriterionBuilder<?> criterionBuilder) throws InvalidValueException {
        this.searchOperator.appendCriteria(this.property, criterionBuilder, this.values);
    }

    static class SearchPropertyValueMapper implements Function<JsonNode, SearchPropertyValue> {
        static String OPERATOR_FIELD = "operator";
        static String CRITERIA_FIELD = "criteria";

        private final Function<String, ?> domainObjectMapper;
        private final SearchableProperty property;

        public SearchPropertyValueMapper(SearchableProperty property){
            this(property, p -> property.getSpecification().getValueFactory().fromStringValue(p));
        }

        public SearchPropertyValueMapper(SearchableProperty property, Function<String, ?> domainObjectMapper) {
            this.domainObjectMapper = domainObjectMapper;
            this.property = property;
        }

        @Override
        public SearchPropertyValue apply(JsonNode node) {
            if (node != null) {
                SearchPropertyValue searchPropertyValue = new SearchPropertyValue(this.property);
                mapOperatorField(node, searchPropertyValue);
                mapCriteriaField(node, searchPropertyValue);
                searchPropertyValue.validate();
                return searchPropertyValue;
            }
            return null;
        }

        private void mapOperatorField(JsonNode node, SearchPropertyValue searchPropertyValue) {
            JsonNode operatorNode = node.get(OPERATOR_FIELD);
            if (operatorNode != null && operatorNode.isTextual()) {
                searchPropertyValue.setSearchOperator(SearchOperator.getOperatorForCode(operatorNode.textValue()));
            }
        }

        private void mapCriteriaField(JsonNode node, SearchPropertyValue searchPropertyValue) {
            JsonNode criteriaField = node.get(CRITERIA_FIELD);
            if (criteriaField != null) {
                List<Object> values;
                if (criteriaField.isArray()) {
                    values = new ArrayList<>();
                    for (JsonNode singleCriteria : criteriaField) {
                        values.add(getSingleCriteriaObject(singleCriteria));
                    }
                } else {
                    values = Collections.singletonList(getSingleCriteriaObject(criteriaField));
                }
                searchPropertyValue.setValues(values);
            }
        }

        private Object getSingleCriteriaObject(JsonNode singleCriteria) {
            return this.domainObjectMapper.apply(getSingleCriteriaAsString(singleCriteria));
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
}
