package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.impl.events.EventPublisher;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link Request} interface
 * that represents a request to register interest
 * in events that relate to a single {@link com.energyict.mdc.upl.meterdata.Device device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (09:59)
 */
class DeviceRequest extends IdBusinessObjectRequest {

    private final DeviceService deviceService;
    private List<Device> devices;

    DeviceRequest(DeviceService deviceService, Set<Long> deviceIds) {
        super(deviceIds);
        this.deviceService = deviceService;
        this.validateDeviceIds();
    }

    DeviceRequest(DeviceService deviceService, String... deviceMRIDs) {
        super(null);
        if (deviceMRIDs == null) {
            throw new IllegalArgumentException("deviceMRID cannot be null");
        }
        this.deviceService = deviceService;
        this.validateDeviceMRIDs(Arrays.asList(deviceMRIDs));
    }

    @Override
    public Set<Long> getBusinessObjectIds() {
        Set<Long> ids = super.getBusinessObjectIds();
        if (super.getBusinessObjectIds() == null) {
            ids = this.devices.stream().map(Device::getId).distinct().collect(Collectors.toSet());
        }
        return ids;
    }

    private void validateDeviceIds() {
        this.devices = this.getBusinessObjectIds()
                .stream()
                .map(deviceService::findDeviceById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void validateDeviceMRIDs(List<String> deviceMRIDs) {
        this.devices = deviceMRIDs
                .stream()
                .map(deviceService::findDeviceByMrid)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public void applyTo(EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToDevices(null, this.devices);
    }
}