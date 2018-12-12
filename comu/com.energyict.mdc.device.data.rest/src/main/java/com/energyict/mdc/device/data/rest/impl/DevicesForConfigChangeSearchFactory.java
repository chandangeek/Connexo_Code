/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.rest.SearchablePropertyValueConverter;
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
            deviceSearchDomain.get().getPropertiesValues(searchableProperty -> SearchablePropertyValueConverter.convert(searchableProperty, filter))
                    .stream()
                    .forEach(propertyValue -> {
                        devicesForConfigChangeSearch.searchItems.put(propertyValue.getProperty().getName(), propertyValue.getValueBean());
                    });
        }
        return devicesForConfigChangeSearch;
    }

}