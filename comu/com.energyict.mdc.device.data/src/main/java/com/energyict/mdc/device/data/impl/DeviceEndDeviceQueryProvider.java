package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.spi.EndDeviceQueryProvider;
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

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider", service = {EndDeviceQueryProvider.class}, property = "name=" + DeviceDataServices.COMPONENT_NAME, immediate = true)
public class DeviceEndDeviceQueryProvider implements EndDeviceQueryProvider {

    public static final String DEVICE_ENDDEVICE_QUERYPROVIDER = DeviceEndDeviceQueryProvider.class.getName();

    private volatile MeteringService meteringService;
    private volatile SearchService searchService;

    // For OSGi purposes
    public DeviceEndDeviceQueryProvider() {
    }

    // For testing purposes
    @Inject
    public DeviceEndDeviceQueryProvider(MeteringService meteringService, SearchService searchService) {
        this();
        setMeteringService(meteringService);
        setSearchService(searchService);
    }

    @Override
    public String getName() {
        return DEVICE_ENDDEVICE_QUERYPROVIDER;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public List<EndDevice> findEndDevices(Instant instant, List<SearchablePropertyCondition> conditions) {
        return this.findEndDevices(instant, conditions, -1, 0);//no pagination
    }

    @Override
    public List<EndDevice> findEndDevices(Instant instant, List<SearchablePropertyCondition> conditions, int start, int limit) {
        SearchDomain deviceSearchDomain = searchService.findDomain(Device.class.getName()).get();
        Subquery subQuery = () -> deviceSearchDomain.finderFor(conditions).asFragment("id");
        Condition amrCondition = where("amrSystemId").isEqualTo(KnownAmrSystem.MDC.getId()).and(ListOperator.IN.contains(subQuery, "amrId"));
        Order order = Order.ascending("mRID");
        if (start > -1) {
            return meteringService.getEndDeviceQuery().select(amrCondition, start + 1, start + limit + 1, order);
        } else {
            return meteringService.getEndDeviceQuery().select(amrCondition, order);
        }
    }

    @Override
    public Query<EndDevice> getEndDeviceQuery(List<SearchablePropertyCondition> conditions) {
        SearchDomain deviceSearchDomain = searchService.findDomain(Device.class.getName()).get();
        Subquery subQuery = () -> deviceSearchDomain.finderFor(conditions).asFragment("id");
        Condition amrCondition = where("amrSystemId").isEqualTo(KnownAmrSystem.MDC.getId()).and(ListOperator.IN.contains(subQuery, "amrId"));
        Query<EndDevice> endDeviceQuery = meteringService.getEndDeviceQuery();
        endDeviceQuery.setRestriction(amrCondition);
        return endDeviceQuery;
    }

}
