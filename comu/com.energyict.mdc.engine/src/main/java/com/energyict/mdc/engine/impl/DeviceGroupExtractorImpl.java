package com.energyict.mdc.engine.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.upl.DeviceGroupExtractor;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.properties.DeviceGroup;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;
import static java.util.stream.Collectors.toList;

/**
 * Provides an implementation for the {@link DeviceGroupExtractor} interface
 * that assumes the {@link DeviceGroup upl device groups} are actually {@link EndDeviceGroup}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (08:40)
 */
@Component(name = "com.energyict.mdc.device.data.upl.group.extractor", service = {DeviceGroupExtractor.class})
@SuppressWarnings("unused")
public class DeviceGroupExtractorImpl implements DeviceGroupExtractor {

    private volatile Clock clock;
    private volatile DeviceService deviceService;

    // For OSGi purposes
    public DeviceGroupExtractorImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceGroupExtractorImpl(Clock clock, DeviceService deviceService) {
        this.clock = clock;
        this.deviceService = deviceService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Activate
    public void activate() {
        Services.deviceGroupExtractor(this);
    }

    @Override
    public long id(DeviceGroup group) {
        return ((EndDeviceGroup) group).getId();
    }

    @Override
    public List<Device> members(DeviceGroup group) {
        return this.members((EndDeviceGroup) group);
    }

    private List<Device> members(EndDeviceGroup group) {
        if (group instanceof QueryEndDeviceGroup) {
            Condition deviceCondition = ListOperator.IN.contains(((QueryEndDeviceGroup) group)::toFragment, "id");
            return new ArrayList<>(this.deviceService.findAllDevices(deviceCondition).find());
        } else {
            List<EndDevice> endDevices = group.getMembers(this.clock.instant());
            Condition mdcMembers = where("id").in(endDevices.stream().map(EndDevice::getAmrId).collect(toList()));
            return new ArrayList<>(deviceService.findAllDevices(mdcMembers).find());
        }
    }

}