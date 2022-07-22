/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.spi.QueryProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.search.SearchService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.metering.groups.impl.SimpleEndDeviceQueryProvider",
        service = {QueryProvider.class},
        immediate = true)
public class SimpleEndDeviceQueryProvider extends SimpleQueryProvider<EndDevice> {

    public static final String SIMPLE_END_DEVICE_QUERY_PROVIDER = SimpleEndDeviceQueryProvider.class.getName();

    // For OSGI
    public SimpleEndDeviceQueryProvider() {
        super(EndDevice.class);
    }

    // For testing
    @Inject
    public SimpleEndDeviceQueryProvider(SearchService searchService, NlsService nlsService) {
        this();
        setSearchService(searchService);
        setNlsService(nlsService);
    }

    // For OSGi
    @Reference
    @Override
    public void setNlsService(NlsService nlsService) {
        super.setNlsService(nlsService);
    }

    // For OSGi
    @Reference
    @Override
    public void setSearchService(SearchService searchService) {
        super.setSearchService(searchService);
    }

    @Override
    public String getName() {
        return SIMPLE_END_DEVICE_QUERY_PROVIDER;
    }
}
