/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.spi.QueryProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.search.SearchService;

import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.metering.groups.impl.SimpleUsagePointQueryProvider",
        service = {QueryProvider.class},
        property = "name=" + MeteringGroupsService.COMPONENTNAME,
        immediate = true)
public class SimpleUsagePointQueryProvider extends SimpleQueryProvider<UsagePoint> implements QueryProvider<UsagePoint> {

    public static final String SIMPLE_USAGE_POINT_QUERY_PROVIDER = SimpleUsagePointQueryProvider.class.getName();

    // For OSGI
    public SimpleUsagePointQueryProvider() {
        super(UsagePoint.class);
    }

    // For testing
    @Inject
    public SimpleUsagePointQueryProvider(SearchService searchService, NlsService nlsService) {
        this();
        setSearchService(searchService);
        setNlsService(nlsService);
    }

    @Override
    public String getName() {
        return SIMPLE_USAGE_POINT_QUERY_PROVIDER;
    }
}
