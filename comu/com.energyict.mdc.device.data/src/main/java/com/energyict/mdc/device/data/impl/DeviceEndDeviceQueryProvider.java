package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceQueryProvider;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.List;

@Component(name = "com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider", service = {EndDeviceQueryProvider.class}, property = "name=" + DeviceDataServices.COMPONENT_NAME, immediate = true)
public class DeviceEndDeviceQueryProvider implements EndDeviceQueryProvider {
    private static final int ORACLE_IN_LIMIT = 1000; // 1000 is the Oracle default limit for static 'IN' condition
    
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MeteringService meteringService;
    private volatile DeviceService deviceService;

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }


    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public static final String DEVICE_ENDDEVICE_QUERYPROVIDER = DeviceEndDeviceQueryProvider.class.getName();

    @Override
    public String getName() {
        return DEVICE_ENDDEVICE_QUERYPROVIDER;
    }

    @Override
    public List<EndDevice> findEndDevices(Condition conditions) {
        return findEndDevices(Instant.now(), conditions);
    }
    
    @Override
    public List<EndDevice> findEndDevices(Instant instant, Condition conditions) {
        return this.findEndDevices(instant, conditions, -1, 0);//no pagination
    }

    @Override
    public List<EndDevice> findEndDevices(Instant instant, Condition conditions, int start, int limit) {
    	Subquery subQuery = deviceService.deviceQuery().asSubquery(conditions, "id");
    	Condition amrCondition = Where.where("amrSystemId").isEqualTo(KnownAmrSystem.MDC.getId());
    	amrCondition = amrCondition.and(ListOperator.IN.contains(subQuery, "amrId"));
        Order order = Order.ascending("mRID");
        if (start > -1) {
            return meteringService.getEndDeviceQuery().select(amrCondition , start + 1, start + limit  + 1, order);
        } else {
        	return meteringService.getEndDeviceQuery().select(amrCondition, order);
        }        
    }

    @Override
    public Condition getQueryCondition(Condition conditions) {
        Subquery subQuery = deviceService.deviceQuery().asSubquery(conditions, "id");
        Condition amrCondition = Where.where("amrSystemId").isEqualTo(KnownAmrSystem.MDC.getId());
        amrCondition = amrCondition.and(ListOperator.IN.contains(subQuery, "amrId"));
        return amrCondition;
    }



}

