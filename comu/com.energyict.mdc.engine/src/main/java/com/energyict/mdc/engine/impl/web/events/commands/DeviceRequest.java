/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.util.Arrays;
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
class DeviceRequest extends IdBusinessObjectRequest {

    private final IdentificationService identificationService;
    private List<Device> devices;

    DeviceRequest(IdentificationService identificationService, Set<Long> deviceIds) {
        super(deviceIds);
        this.identificationService = identificationService;
        this.validateDeviceIds();
    }

    DeviceRequest(IdentificationService identificationService, String... deviceMRIDs) {
        super(null);
        if (deviceMRIDs == null) {
            throw new IllegalArgumentException("deviceMRID cannot be null");
        }
        this.identificationService = identificationService;
        this.validateDeviceMRIDs(Arrays.asList(deviceMRIDs));
    }

    @Override
    public Set<Long> getBusinessObjectIds () {
        Set<Long> ids = super.getBusinessObjectIds();
        if (super.getBusinessObjectIds()== null){
            ids = this.devices.stream().map(Device::getId).distinct().collect(Collectors.toSet());
        }
        return ids;
    }

    private void validateDeviceIds() {
         this.devices = this.getBusinessObjectIds()
                    .stream()
                    .map(identificationService::createDeviceIdentifierByDatabaseId)
                    .map(DeviceIdentifier::findDevice)
                    .map(Device.class::cast)
                .collect(Collectors.toList());
    }

    private void validateDeviceMRIDs(List<String> deviceMRIDs){
        this.devices = deviceMRIDs
                .stream()
                .map(identificationService::createDeviceIdentifierByMRID)
                .map(DeviceIdentifier::findDevice)
                .map(Device.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public void applyTo(EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToDevices(null, this.devices);
    }

}