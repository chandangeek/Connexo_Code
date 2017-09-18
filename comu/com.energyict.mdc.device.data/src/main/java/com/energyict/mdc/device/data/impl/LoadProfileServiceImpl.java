/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.streams.Currying;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.obis.ObisCode;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Provides an implementation for the {@link LoadProfileService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:06)
 */
@Component(name = "com.energyict.mdc.device.data.impl.LoadProfileServiceImpl", service = LoadProfileService.class, immediate = true)
public class LoadProfileServiceImpl implements ServerLoadProfileService {

    private volatile DeviceDataModelService deviceDataModelService;

    // For OSGi purpose
    public LoadProfileServiceImpl() {
        Services.loadProfileFinder(this);
    }

    // For testing purposes
    @Inject
    public LoadProfileServiceImpl(DeviceDataModelService deviceDataModelService) {
        this();
        this.setDataModelService(deviceDataModelService);
    }

    @Reference
    public void setDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public Optional<LoadProfile> findById(long id) {
        return this.deviceDataModelService.dataModel().mapper(LoadProfile.class).getOptional(id);
    }

    @Override
    public Optional<LoadProfile> findAndLockLoadProfileByIdAndVersion(long id, long version) {
        return this.deviceDataModelService.dataModel().mapper(LoadProfile.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<com.energyict.mdc.upl.meterdata.LoadProfile> find(LoadProfileIdentifier identifier) {
        return this.findByIdentifier(identifier).map(com.energyict.mdc.upl.meterdata.LoadProfile.class::cast);
    }

    @Override
    public Optional<LoadProfile> findByIdentifier(LoadProfileIdentifier identifier) {
        try {
            return this.doFind(identifier);
        } catch (UnsupportedLoadProfileIdentifierTypeName | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<LoadProfile> doFind(LoadProfileIdentifier identifier) throws UnsupportedLoadProfileIdentifierTypeName {
        Introspector introspector = identifier.forIntrospection();
        switch (introspector.getTypeName()) {
            case "Null": {
                throw new UnsupportedOperationException("NullLoadProfileIdentifier is not capable of finding a load profile because it is a marker for a missing load profile");
            }
            case "Actual": {
                return Optional.of((LoadProfile) introspector.getValue("actual"));
            }
            case "DatabaseId": {
                return this.findById(Long.valueOf(introspector.getValue("databaseValue").toString()));
            }
            case "DeviceIdentifierAndObisCode": {
                DeviceIdentifier deviceIdentifier = (DeviceIdentifier) introspector.getValue("device");
                ObisCode loadProfileObisCode = (ObisCode) introspector.getValue("obisCode");
                return this.deviceDataModelService.deviceService()
                        .findDeviceByIdentifier(deviceIdentifier)
                        .map(Currying.use(this::findByDeviceAndObisCode).with(loadProfileObisCode))
                        .orElse(Optional.empty());
            }
            case "FirstLoadProfileOnDevice": {
                DeviceIdentifier deviceIdentifier = (DeviceIdentifier) introspector.getValue("device");
                return this.deviceDataModelService.deviceService()
                        .findDeviceByIdentifier(deviceIdentifier)
                        .map(this::findFirstOnDevice)
                        .orElse(Optional.empty());
            }
            default: {
                throw new UnsupportedLoadProfileIdentifierTypeName();
            }
        }
    }

    private Optional<LoadProfile> findByDeviceAndObisCode(Device device, ObisCode obisCode) {
        return device
                .getLoadProfiles()
                .stream()
                .filter(loadProfile -> loadProfile.getDeviceObisCode().equals(obisCode))
                .findAny();
    }

    private Optional<LoadProfile> findFirstOnDevice(Device device) {
        return device
                .getLoadProfiles()
                .stream()
                .findFirst();
    }

    private static class UnsupportedLoadProfileIdentifierTypeName extends RuntimeException {

    }

}