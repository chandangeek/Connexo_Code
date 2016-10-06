package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.spi.EndDeviceQueryProvider;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.conditions.Condition;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component(name = "com.elster.jupiter.metering.groups.impl.SimpleEndDeviceQueryProvider", service = {EndDeviceQueryProvider.class}, property = "name=" + MeteringGroupsService.COMPONENTNAME, immediate = true)
public class SimpleEndDeviceQueryProvider implements EndDeviceQueryProvider {

    public static final String SIMPLE_ENDDEVICE_QUERYPRVIDER = SimpleEndDeviceQueryProvider.class.getName();

    private volatile MeteringService meteringService;

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public String getName() {
        return SIMPLE_ENDDEVICE_QUERYPRVIDER;
    }

    @Override
    public List<EndDevice> findEndDevices(Instant instant, List<SearchablePropertyCondition> conditions) {
        return findEndDevices(instant, conditions, -1, 0);
    }

    @Override
    public List<EndDevice> findEndDevices(Instant instant, List<SearchablePropertyCondition> conditions, int start, int limit) {
        int from = start + 1;
        int to = from + limit;
        Optional<Condition> condition = conditions.stream().map(SearchablePropertyCondition::getCondition).reduce(Condition::and);
        if (start > -1) {
            return meteringService.getEndDeviceQuery().select(condition.orElse(Condition.TRUE), from, to);
        } else {
            return meteringService.getEndDeviceQuery().select(condition.orElse(Condition.TRUE));
        }
    }

    @Override
    public Query<EndDevice> getEndDeviceQuery(List<SearchablePropertyCondition> conditions) {
        Optional<Condition> condition = conditions.stream().map(SearchablePropertyCondition::getCondition).reduce(Condition::and);
        Query<EndDevice> endDeviceQuery = meteringService.getEndDeviceQuery();
        endDeviceQuery.setRestriction(condition.orElse(Condition.TRUE));
        return endDeviceQuery;
    }
}
