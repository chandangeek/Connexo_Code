package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceQueryProvider;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

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
    public List<EndDevice> findEndDevices(Instant date, Condition conditions) {
        // TODO it will be better to rewrite it using sub-queries, so we will have only one request
        List<Device> devices = deviceService.findAllDevices(conditions).find();
        List<Long> deviceIds = devices.stream().map(Device::getId).collect(Collectors.toList());
        Condition condition = Operator.EQUAL.compare("amrSystemId", KnownAmrSystem.MDC.getId())
                .and(getSplittedInCondition("amrId", deviceIds));
        return meteringService.getEndDeviceQuery().select(condition, Order.ascending("mRID"));
    }

    private Condition getSplittedInCondition(String field, List<?> values){
        if (values.size() >= ORACLE_IN_LIMIT){
            Logger.getLogger(DeviceEndDeviceQueryProvider.class.getSimpleName()).warning("We have more than " + ORACLE_IN_LIMIT + " devices in group, it can slow down a select query");
            Condition condition = where(field).in(values.subList(0, ORACLE_IN_LIMIT));
            int i = ORACLE_IN_LIMIT;
            while (i < values.size()){
                int lastPartIdx = i + ORACLE_IN_LIMIT;
                condition = condition.or(where(field).in(values.subList(i,  values.size() > lastPartIdx ? lastPartIdx : values.size())));
                i = lastPartIdx;
            }
            return condition;
        }
        return where(field).in(values);
    }
}

