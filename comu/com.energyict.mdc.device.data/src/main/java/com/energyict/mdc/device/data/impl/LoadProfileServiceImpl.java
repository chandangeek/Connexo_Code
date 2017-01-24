package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.streams.Currying;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.obis.ObisCode;
import org.osgi.service.component.annotations.Component;

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

    //Only testing purposes
    public LoadProfileServiceImpl() {
    }

    @Inject
    public LoadProfileServiceImpl(DeviceDataModelService deviceDataModelService) {
        super();
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
                return this.findById((long) introspector.getValue("databaseValue"));
            }
            case "DeviceIdentifierAndObisCode": {
                DeviceIdentifier deviceIdentifier = (DeviceIdentifier) introspector.getValue("device");
                ObisCode loadProfileObisCode = (ObisCode) introspector.getValue("obisCode");
                this.deviceDataModelService.deviceService()
                        .findDeviceByIdentifier(deviceIdentifier)
                        .map(Currying.use(this::findByDeviceAndObisCode).with(loadProfileObisCode));
            }
            case "FirstLoadProfileOnDevice": {
                DeviceIdentifier deviceIdentifier = (DeviceIdentifier) introspector.getValue("device");
                this.deviceDataModelService.deviceService()
                        .findDeviceByIdentifier(deviceIdentifier)
                        .map(this::findFirstOnDevice);
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