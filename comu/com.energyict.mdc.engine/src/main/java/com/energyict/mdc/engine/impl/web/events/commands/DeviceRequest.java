package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.comserver.collections.Collections;
import com.energyict.comserver.eventsimpl.EventPublisher;
import com.energyict.mdc.engine.impl.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link Request} interface
 * that represents a request to register interest
 * in events that relate to a single {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (09:59)
 */
public class DeviceRequest extends IdBusinessObjectRequest {

    private List<BaseDevice> devices;

    public DeviceRequest (int deviceId) {
        this(Collections.toSet(deviceId));
    }

    public DeviceRequest (Set<Integer> deviceIds) {
        super(deviceIds);
        this.validateDeviceIds();
    }

    private void validateDeviceIds () {
        this.devices = new ArrayList<BaseDevice>(this.getBusinessObjectIds().size());
        for (Integer deviceId : this.getBusinessObjectIds()) {
            this.devices.add(new DeviceIdentifierById(deviceId, deviceDataService).findDevice());
        }
    }

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToDevices(null, this.devices);
    }

}