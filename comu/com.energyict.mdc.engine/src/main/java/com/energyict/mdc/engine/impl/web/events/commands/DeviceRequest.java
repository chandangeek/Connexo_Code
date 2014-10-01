package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.DeviceIdentifierById;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singleton;

/**
 * Provides an implementation for the {@link Request} interface
 * that represents a request to register interest
 * in events that relate to a single {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (09:59)
 */
public class DeviceRequest extends IdBusinessObjectRequest {

    private final DeviceService deviceService;
    private List<BaseDevice> devices;

    public DeviceRequest(DeviceService deviceService, long deviceId) {
        this(deviceService, singleton(deviceId));
    }

    public DeviceRequest(DeviceService deviceService, Set<Long> deviceIds) {
        super(deviceIds);
        this.deviceService = deviceService;
        this.validateDeviceIds();
    }

    private void validateDeviceIds () {
        this.devices = new ArrayList<>(this.getBusinessObjectIds().size());
        for (Long deviceId : this.getBusinessObjectIds()) {
            this.devices.add(new DeviceIdentifierById(deviceId, deviceService).findDevice());
        }
    }

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToDevices(null, this.devices);
    }

}