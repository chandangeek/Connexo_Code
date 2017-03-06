package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.obis.ObisCode;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.streams.Currying.use;

/**
 * Provides an implementation for the {@link LogBookService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:06)
 */
public class LogBookServiceImpl implements ServerLogBookService {

    private final DeviceDataModelService deviceDataModelService;

    @Inject
    public LogBookServiceImpl(DeviceDataModelService deviceDataModelService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
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
    public Optional<LogBook> findByIdentifier(LogBookIdentifier identifier) {
        try {
            return this.doFind(identifier);
        } catch (UnsupportedLogBookIdentifierTypeName | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<LogBook> doFind(LogBookIdentifier identifier) throws UnsupportedLogBookIdentifierTypeName {
        Introspector introspector = identifier.forIntrospection();
        switch (introspector.getTypeName()) {
            case "Null": {
                throw new UnsupportedOperationException("NullLogBookIdentifier is not capable of finding a log book because it serves as a marker for a missing log book");
            }
            case "Actual": {
                return Optional.of((LogBook) introspector.getValue("actual"));
            }
            case "Other": {
                return this.findByIdentifier((LogBookIdentifier) introspector.getValue("other"));
            }
            case "DatabaseId": {
                return this.findById(Long.valueOf(introspector.getValue("databaseValue").toString()));
            }
            case "DeviceIdentifierAndObisCode": {
                DeviceIdentifier deviceIdentifier = (DeviceIdentifier) introspector.getValue("device");
                ObisCode logBookObisCode = (ObisCode) introspector.getValue("obisCode");
                return this.deviceDataModelService.deviceService()
                            .findDeviceByIdentifier(deviceIdentifier)
                            .flatMap(use(this::findByDeviceAndObisCode).with(logBookObisCode));
            }
            default: {
                throw new UnsupportedLogBookIdentifierTypeName();
            }
        }
    }

    private Optional<LogBook> findByDeviceAndObisCode(Device device, ObisCode obisCode) {
        return device
                .getLogBooks()
                .stream()
                .filter(logBook -> logBook.getLogBookSpec().getDeviceObisCode().equals(obisCode))
                .findAny();
    }

    private static class UnsupportedLogBookIdentifierTypeName extends RuntimeException {
    }

}