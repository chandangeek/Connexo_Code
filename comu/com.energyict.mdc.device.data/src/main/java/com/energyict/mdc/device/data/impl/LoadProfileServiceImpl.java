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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Provides an implementation for the {@link LoadProfileService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:06)
 */
@Component(name = "com.energyict.mdc.device.data.impl.LoadProfileServiceImpl", service = LoadProfileService.class, immediate = true)
public class LoadProfileServiceImpl implements ServerLoadProfileService {

    /**
     * Enum listing up all different Introspector types that can be used in method LoadProfileServiceImpl#findByIdentifier(LoadProfileIdentifier)
     */
    public enum IntrospectorTypes {
        DatabaseId("databaseValue", "device", "obisCode"),
        DeviceIdentifierAndObisCode("device", "obisCode"),
        FirstLoadProfileOnDevice("device", "obisCode"),
        Actual("actual", "databaseValue");

        private final String[] roles;

        IntrospectorTypes(String... roles) {
            this.roles = roles;
        }

        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList(roles));
        }

        public static Optional<IntrospectorTypes> forName(String name) {
            return Arrays.stream(values()).filter(type -> type.name().equals(name)).findFirst();
        }
    }

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
        if (introspector.getTypeName().equals(IntrospectorTypes.Actual.name())) {
            return Optional.of((LoadProfile) introspector.getValue(IntrospectorTypes.Actual.roles[0]));
        } else if (introspector.getTypeName().equals(IntrospectorTypes.DatabaseId.name())) {
            return this.findById(Long.valueOf(introspector.getValue(IntrospectorTypes.DatabaseId.roles[0]).toString()));
        } else if (introspector.getTypeName().equals(IntrospectorTypes.DeviceIdentifierAndObisCode.name())) {
            DeviceIdentifier deviceIdentifier = (DeviceIdentifier) introspector.getValue(IntrospectorTypes.DeviceIdentifierAndObisCode.roles[0]);
            ObisCode loadProfileObisCode = (ObisCode) introspector.getValue(IntrospectorTypes.DeviceIdentifierAndObisCode.roles[1]);
            return this.deviceDataModelService.deviceService()
                    .findDeviceByIdentifier(deviceIdentifier)
                    .map(Currying.use(this::findByDeviceAndObisCode).with(loadProfileObisCode))
                    .orElse(Optional.empty());
        } else if (introspector.getTypeName().equals(IntrospectorTypes.FirstLoadProfileOnDevice.name())) {
            DeviceIdentifier deviceIdentifier = (DeviceIdentifier) introspector.getValue(IntrospectorTypes.FirstLoadProfileOnDevice.roles[0]);
            return this.deviceDataModelService.deviceService()
                    .findDeviceByIdentifier(deviceIdentifier).flatMap(this::findFirstOnDevice);
        } else {
            throw new UnsupportedLoadProfileIdentifierTypeName();
        }
    }

    protected Optional<LoadProfile> findByDeviceAndObisCode(Device device, ObisCode obisCode) {
        return device
                .getLoadProfiles()
                .stream()
                .filter(loadProfile -> loadProfile.getDeviceObisCode().equals(obisCode))
                .findAny();
    }

    protected Optional<LoadProfile> findFirstOnDevice(Device device) {
        return device
                .getLoadProfiles()
                .stream()
                .findFirst();
    }

    private static class UnsupportedLoadProfileIdentifierTypeName extends RuntimeException {

    }

}