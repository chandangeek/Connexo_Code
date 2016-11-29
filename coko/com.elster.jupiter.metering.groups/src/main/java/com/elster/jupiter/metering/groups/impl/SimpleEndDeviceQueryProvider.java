package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.spi.QueryProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.search.SearchService;

import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.metering.groups.impl.SimpleEndDeviceQueryProvider",
        service = {QueryProvider.class},
        property = "name=" + MeteringGroupsService.COMPONENTNAME,
        immediate = true)
public class SimpleEndDeviceQueryProvider extends SimpleQueryProvider<EndDevice> implements QueryProvider<EndDevice> {

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

    @Override
    public String getName() {
        return SIMPLE_END_DEVICE_QUERY_PROVIDER;
    }
}
