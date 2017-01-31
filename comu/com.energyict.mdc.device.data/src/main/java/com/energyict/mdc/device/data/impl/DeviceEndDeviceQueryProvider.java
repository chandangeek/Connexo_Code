/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.groups.spi.QueryProvider;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider",
        service = {QueryProvider.class},
        property = "name=" + DeviceDataServices.COMPONENT_NAME,
        immediate = true)
public class DeviceEndDeviceQueryProvider implements QueryProvider<EndDevice> {

    public static final String DEVICE_END_DEVICE_QUERY_PROVIDER = DeviceEndDeviceQueryProvider.class.getName();

    private volatile SearchService searchService;
    private Supplier<Query<EndDevice>> basicQuerySupplier;

    // For OSGi purposes
    public DeviceEndDeviceQueryProvider() {
    }

    // For testing purposes
    @Inject
    public DeviceEndDeviceQueryProvider(SearchService searchService) {
        this();
        setSearchService(searchService);
    }

    @Override
    public DeviceEndDeviceQueryProvider init(Supplier<Query<EndDevice>> basicQuerySupplier) {
        this.basicQuerySupplier = basicQuerySupplier;
        return this;
    }

    @Override
    public String getName() {
        return DEVICE_END_DEVICE_QUERY_PROVIDER;
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public List<EndDevice> executeQuery(Instant instant, List<SearchablePropertyCondition> conditions) {
        return this.executeQuery(instant, conditions, -1, 0);//no pagination
    }

    @Override
    public List<EndDevice> executeQuery(Instant instant, List<SearchablePropertyCondition> conditions, int start, int limit) {
        SearchDomain deviceSearchDomain = searchService.findDomain(Device.class.getName()).get();
        Subquery subQuery = () -> deviceSearchDomain.finderFor(conditions).asFragment("id");
        Condition amrCondition = where("amrSystemId").isEqualTo(KnownAmrSystem.MDC.getId()).and(ListOperator.IN.contains(subQuery, "amrId"));
        Order order = Order.ascending("name");
        if (start > -1) {
            return basicQuerySupplier.get().select(amrCondition, start + 1, start + limit + 1, order);
        } else {
            return basicQuerySupplier.get().select(amrCondition, order);
        }
    }

    @Override
    public Query<EndDevice> getQuery(List<SearchablePropertyCondition> conditions) {
        SearchDomain deviceSearchDomain = searchService.findDomain(Device.class.getName()).get();
        Subquery subQuery = deviceSearchDomain.finderFor(conditions).asSubQuery("id");
        Condition amrCondition = where("amrSystemId").isEqualTo(KnownAmrSystem.MDC.getId()).and(ListOperator.IN.contains(subQuery, "amrId"));
        Query<EndDevice> endDeviceQuery = basicQuerySupplier.get();
        endDeviceQuery.setRestriction(amrCondition);
        return endDeviceQuery;
    }

}
