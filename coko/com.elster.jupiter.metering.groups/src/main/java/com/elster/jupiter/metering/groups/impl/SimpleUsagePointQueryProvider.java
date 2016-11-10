package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.spi.QueryProvider;

import org.osgi.service.component.annotations.Component;

@Component(
        name = "com.elster.jupiter.metering.groups.impl.SimpleUsagePointQueryProvider",
        service = {QueryProvider.class},
        property = "name=" + MeteringGroupsService.COMPONENTNAME,
        immediate = true)
public class SimpleUsagePointQueryProvider extends SimpleQueryProvider<UsagePoint> implements QueryProvider<UsagePoint> {

    public static final String SIMPLE_USAGE_POINT_QUERY_PROVIDER = SimpleUsagePointQueryProvider.class.getName();

    @Override
    public String getName() {
        return SIMPLE_USAGE_POINT_QUERY_PROVIDER;
    }
}
