package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link Request} interface
 * that represents a request to register interest
 * in events that relate to a single {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (09:59)
 */
public class DeviceRequest extends IdBusinessObjectRequest {

    private final IdentificationService identificationService;
    private List<Device> devices;

    public DeviceRequest(IdentificationService identificationService, Set<Long> deviceIds) {
        super(deviceIds);
        this.identificationService = identificationService;
        this.validateDeviceIds();
    }

    private void validateDeviceIds() {
        this.devices = this.getBusinessObjectIds()
                .stream()
                .map(identificationService::createDeviceIdentifierByDatabaseId)
                .map(DeviceIdentifier::findDevice)
                .map(Device.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public void applyTo(EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToDevices(null, this.devices);
    }

}