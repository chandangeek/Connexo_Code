package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DevicesForConfigChangeSearch;
import com.fasterxml.jackson.databind.JsonNode;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;

/**
 * Factory which allows easy creation of a DevicesForConfigChangeSearch object based on a JsonQueryFilter
 */
public class DevicesForConfigChangeSearchFactory {

    private static final Function<JsonNode, String> AS_CRITERIA = node -> {
        if (node != null) {
            return node.findValue("criteria").asText();
        }
        return null;
    };
    private static final Function<JsonNode, List<String>> AS_LIST_CRITERIA = node -> {
        if (node != null) {
            JsonNode criteria = node.findValue("criteria");
            if(criteria.isArray()){
                List<String> values = new ArrayList<>();
                for (JsonNode aCriteria : criteria) {
                    values.add(aCriteria.asText());
                }
                return values;
            } else {
                return Collections.singletonList(criteria.asText());
            }
        }
        return null;
    };

    private SearchService searchService;

    @Inject
    public DevicesForConfigChangeSearchFactory(SearchService searchService) {
        this.searchService = searchService;
    }

    public DevicesForConfigChangeSearch fromQueryFilter(JsonQueryFilter filter) {
        Optional<SearchDomain> deviceSearchDomain = searchService.findDomain(Device.class.getName());
        DevicesForConfigChangeSearch devicesForConfigChangeSearch = new DevicesForConfigChangeSearch();
        if (filter.hasFilters() && deviceSearchDomain.isPresent()) {
            deviceSearchDomain.get().getProperties().stream().
                    filter(p -> filter.hasProperty(p.getName())).
                    forEach(searchableProperty -> {
                        if (searchableProperty.getSelectionMode() == SearchableProperty.SelectionMode.MULTI) {
                            devicesForConfigChangeSearch.searchItems.add(new DevicesForConfigChangeSearch.DeviceSearchItem(searchableProperty.getName(), DevicesForConfigChangeSearch.Operator.IN, filter.getProperty(searchableProperty.getName(), AS_LIST_CRITERIA)));
                        } else if (searchableProperty.getSpecification().getValueFactory().getValueType().equals(String.class)) {
                            devicesForConfigChangeSearch.searchItems.add(new DevicesForConfigChangeSearch.DeviceSearchItem(searchableProperty.getName(), DevicesForConfigChangeSearch.Operator.LIKE, filter.getProperty(searchableProperty.getName(), AS_CRITERIA)));
                        } else {
                            devicesForConfigChangeSearch.searchItems.add(new DevicesForConfigChangeSearch.DeviceSearchItem(searchableProperty.getName(), DevicesForConfigChangeSearch.Operator.EQUALS, filter.getProperty(searchableProperty.getName(), AS_CRITERIA)));
                        }
                    });
        }
        return devicesForConfigChangeSearch;
    }
}