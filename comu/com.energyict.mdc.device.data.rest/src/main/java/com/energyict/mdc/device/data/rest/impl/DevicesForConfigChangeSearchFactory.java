package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DevicesForConfigChangeSearch;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Factory which allows easy creation of a DevicesForConfigChangeSearch object based on a JsonQueryFilter
 */
public class DevicesForConfigChangeSearchFactory {

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
                            devicesForConfigChangeSearch.searchItems.add(new DevicesForConfigChangeSearch.DeviceSearchItem(searchableProperty.getName(), DevicesForConfigChangeSearch.Operator.IN, filter.getStringList(searchableProperty.getName())));
                        } else if (searchableProperty.getSpecification().getValueFactory().getValueType().equals(String.class)) {
                            devicesForConfigChangeSearch.searchItems.add(new DevicesForConfigChangeSearch.DeviceSearchItem(searchableProperty.getName(), DevicesForConfigChangeSearch.Operator.LIKE, filter.getString(searchableProperty.getName())));
                        } else {
                            devicesForConfigChangeSearch.searchItems.add(new DevicesForConfigChangeSearch.DeviceSearchItem(searchableProperty.getName(), DevicesForConfigChangeSearch.Operator.EQUALS, filter.getString(searchableProperty.getName())));
                        }
                    });
        }
        return devicesForConfigChangeSearch;
    }
}
