/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import com.energyict.obis.ObisCode;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.util.streams.Currying.use;

/**
 * Provides an implementation for the {@link LogBookService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:06)
 */
public class LogBookServiceImpl implements ServerLogBookService {

    /**
     * Enum listing up all different Introspector types that can be used in method LogBookServiceImpl#findByIdentifier(LogBookIdentifier)
     */
    public enum IntrospectorTypes {
        DatabaseId("databaseValue"),
        DeviceIdentifierAndObisCode("device", "obisCode"),
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

    private final DeviceDataModelService deviceDataModelService;

    @Inject
    public LogBookServiceImpl(DeviceDataModelService deviceDataModelService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
        Services.logBookFinder(this);
    }

    @Override
    public Optional<LogBook> findById(long id) {
        return this.deviceDataModelService.dataModel().mapper(LogBook.class).getOptional(id);
    }

    @Override
    public List<LogBook> findLogBooksByDevice(Device device) {
        return this.deviceDataModelService.dataModel().mapper(LogBook.class).find("device", device);
    }

    @Override
    public Optional<com.energyict.mdc.upl.meterdata.LogBook> find(LogBookIdentifier identifier) {
        return this.findByIdentifier(identifier).map(com.energyict.mdc.upl.meterdata.LogBook.class::cast);
    }

    @Override
    public Optional<LogBook> findByIdentifier(LogBookIdentifier identifier) {
        try {
            return this.doFind(identifier);
        } catch (UnsupportedLogBookIdentifierTypeName | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<LogBook> doFind(LogBookIdentifier identifier) throws UnsupportedLogBookIdentifierTypeName {
        Introspector introspector = identifier.forIntrospection();
        if (introspector.getTypeName().equals(IntrospectorTypes.Actual.name())) {
            return Optional.of((LogBook) introspector.getValue(IntrospectorTypes.Actual.roles[0]));
        } else if (introspector.getTypeName().equals(IntrospectorTypes.DatabaseId.name())) {
            return this.findById(Long.valueOf(introspector.getValue(IntrospectorTypes.DatabaseId.roles[0]).toString()));
        } else if (introspector.getTypeName().equals(IntrospectorTypes.DeviceIdentifierAndObisCode.name())) {
            DeviceIdentifier deviceIdentifier = (DeviceIdentifier) introspector.getValue(IntrospectorTypes.DeviceIdentifierAndObisCode.roles[0]);
            ObisCode logBookObisCode = (ObisCode) introspector.getValue(IntrospectorTypes.DeviceIdentifierAndObisCode.roles[1]);
            return this.deviceDataModelService.deviceService()
                    .findDeviceByIdentifier(deviceIdentifier)
                    .flatMap(use(this::findByDeviceAndObisCode).with(logBookObisCode));
        } else {
            throw new UnsupportedLogBookIdentifierTypeName();
        }
    }

    @Override
    public Optional<LogBook> findByDeviceAndObisCode(Device device, ObisCode obisCode) {
        return device
                .getLogBooks()
                .stream()
                .filter(logBook -> logBook.getLogBookSpec().getDeviceObisCode().equals(obisCode))
                .findAny();
    }

    private static class UnsupportedLogBookIdentifierTypeName extends RuntimeException {
    }

}