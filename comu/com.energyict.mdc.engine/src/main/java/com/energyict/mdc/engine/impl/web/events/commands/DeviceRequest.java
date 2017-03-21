package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.engine.impl.DeviceIdentifierById;
import com.energyict.mdc.engine.impl.DeviceIdentifierByMRID;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.events.EventPublisher;

import java.util.Arrays;
import java.util.List;
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
                .map(this::findDeviceByIdOrThrowException)
                .collect(Collectors.toList());
    }

    private void validateDeviceMRIDs(List<String> deviceMRIDs) {
        this.devices = deviceMRIDs
                .stream()
                .map(this::findDeviceByMRIDOrThrowException)
                .collect(Collectors.toList());
    }

    private Device findDeviceByIdOrThrowException(long id) {
        return this.deviceService
                .findDeviceById(id)
                .orElseThrow(() -> CanNotFindForIdentifier.device(new DeviceIdentifierById(id), MessageSeeds.CAN_NOT_FIND_FOR_DEVICE_IDENTIFIER));
    }

    private Device findDeviceByMRIDOrThrowException(String mRID) {
        return this.deviceService
                .findDeviceByMrid(mRID)
                .orElseThrow(() -> CanNotFindForIdentifier.device(new DeviceIdentifierByMRID(mRID), MessageSeeds.CAN_NOT_FIND_FOR_DEVICE_IDENTIFIER));
    }

    @Override
    public void applyTo(EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToDevices(null, this.devices);
    }
}