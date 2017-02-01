package com.energyict.mdc.device.data.importers.impl.devices.shipment;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.NoLifeCycleActiveAt;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.time.Instant;

import static com.elster.jupiter.util.Checks.is;

public class DeviceShipmentImportProcessor extends AbstractDeviceDataFileImportProcessor<DeviceShipmentImportRecord> {

    public DeviceShipmentImportProcessor(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    public void process(DeviceShipmentImportRecord data, FileImportLogger logger) throws
            ProcessorException {
        if (getContext().getDeviceService().findDeviceByName(data.getDeviceIdentifier()).isPresent()) {
            throw new ProcessorException(MessageSeeds.DEVICE_ALREADY_EXISTS, data.getLineNumber(), data.getDeviceIdentifier());
        }
        DeviceType deviceType = getDeviceTypeOrThrowException(data);
        DeviceConfiguration deviceConfiguration = getDeviceConfigurationOrThrowException(deviceType, data);
        Device device;
        try {
            Connection connection = getContext().getConnection();
            Savepoint savepoint = connection.setSavepoint();
            try {
                if (!is(data.getBatch()).emptyOrOnlyWhiteSpace()) {
                    device = getContext().getDeviceService()
                            .newDevice(deviceConfiguration, data.getDeviceIdentifier(), data.getBatch(), Instant.from(data.getShipmentDate()));
                } else {
                    device = getContext().getDeviceService()
                            .newDevice(deviceConfiguration, data.getDeviceIdentifier(), Instant.from(data.getShipmentDate()));
                }
            } catch (NoLifeCycleActiveAt e) {
                connection.rollback(savepoint);
                throw e;
            }
        }catch (SQLException e){
            throw new ProcessorException(MessageSeeds.PROCESS_SQL_EXCEPTION, data.getLineNumber()).andStopImport();
        }

        if (data.getSerialNumber() != null)
            device.setSerialNumber(data.getSerialNumber());
        if (data.getManufacturer() != null)
            device.setManufacturer(data.getManufacturer());
        if (data.getModelNbr() != null)
            device.setModelNumber(data.getModelNbr());
        if (data.getModelVersion() != null)
            device.setModelVersion(data.getModelVersion());
        if (data.getYearOfCertification() != null)
            device.setYearOfCertification(data.getYearOfCertification());

        setShipmentDate(device, data);
        device.save();
    }

    @Override
    public void complete(FileImportLogger logger) {
        // do nothing
    }

    private DeviceType getDeviceTypeOrThrowException(DeviceShipmentImportRecord data) {
        return getContext().getDeviceConfigurationService().findDeviceTypeByName(data.getDeviceType())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE_TYPE, data.getLineNumber(), data.getDeviceType()));
    }

    private DeviceConfiguration getDeviceConfigurationOrThrowException(DeviceType deviceType, DeviceShipmentImportRecord data) {
        return deviceType.getConfigurations()
                .stream()
                .filter(candidate -> candidate.getName().equals(data.getDeviceConfiguration()))
                .findFirst()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE_CONFIGURATION, data.getLineNumber(), data.getDeviceConfiguration()));
    }

    private void setShipmentDate(Device device, DeviceShipmentImportRecord data) {
        CIMLifecycleDates lifecycleDates = device.getLifecycleDates();
        lifecycleDates.setReceivedDate(data.getShipmentDate().toInstant());
        lifecycleDates.save();
    }
}
