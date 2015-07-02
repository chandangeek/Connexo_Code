package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceQueryProvider;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.conditions.Condition;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.List;

@Component(name = "com.elster.jupiter.metering.groups.impl.SimpleEndDeviceQueryProvider", service = {EndDeviceQueryProvider.class}, property = "name=" + MeteringGroupsService.COMPONENTNAME, immediate = true)
public class SimpleEndDeviceQueryProvider implements EndDeviceQueryProvider {

    private volatile MeteringService meteringService;

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public static final String SIMPLE_ENDDEVICE_QUERYPRVIDER = SimpleEndDeviceQueryProvider.class.getName();

    @Override
    public String getName() {
        return SIMPLE_ENDDEVICE_QUERYPRVIDER;
    }

    @Override
    public List<EndDevice> findEndDevices(Condition conditions) {
        return findEndDevices(Instant.now(), conditions);
    }

    @Override
    public List<EndDevice> findEndDevices(Instant instant, Condition conditions) {
        return meteringService.getEndDeviceQuery().select(conditions);
    }
    
    @Override
    public List<EndDevice> findEndDevices(Instant instant, Condition conditions, int start, int limit) {
        int from = start + 1;
        int to = from + limit;
        return meteringService.getEndDeviceQuery().select(conditions, from, to);
    }

    @Override
    public Condition getQueryCondition(Condition conditions) {
        return conditions;
    }

}
