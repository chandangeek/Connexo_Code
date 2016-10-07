package com.energyict.mdc.device.data.importers.impl.devices.shipment;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.NoLifeCycleActiveAt;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.FileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.time.Instant;

import static com.elster.jupiter.util.Checks.is;

public class DeviceShipmentImportProcessor implements FileImportProcessor<DeviceShipmentImportRecord> {

    private final DeviceDataImporterContext context;

    public DeviceShipmentImportProcessor(DeviceDataImporterContext context) {
        this.context = context;
    }

    @Override
    public void process(DeviceShipmentImportRecord data, FileImportLogger logger) throws
            ProcessorException {
        if (this.context.getDeviceService().findByUniqueMrid(data.getDeviceMRID()).isPresent()) {
            throw new ProcessorException(MessageSeeds.DEVICE_ALREADY_EXISTS, data.getLineNumber(), data.getDeviceMRID());
        }
        DeviceType deviceType = getDeviceTypeOrThrowException(data);
        DeviceConfiguration deviceConfiguration = getDeviceConfigurationOrThrowException(deviceType, data);
        Device device;
        try {
            Connection connection = context.getConnection();
            Savepoint savepoint = connection.setSavepoint();
            try {
                if (!is(data.getBatch()).emptyOrOnlyWhiteSpace()) {
                    device = this.context.getDeviceService()
                            .newDevice(deviceConfiguration, data.getDeviceMRID(), data.getDeviceMRID(), data.getBatch(), Instant.from(data.getShipmentDate()));
                } else {
                    device = this.context.getDeviceService()
                            .newDevice(deviceConfiguration, data.getDeviceMRID(), data.getDeviceMRID(), Instant.from(data.getShipmentDate()));
                }
            } catch (NoLifeCycleActiveAt e) {
                connection.rollback(savepoint);
                throw e;
            }
        }catch (SQLException e){
            throw new ProcessorException(MessageSeeds.PROCESS_SQL_EXCEPTION, data.getLineNumber()).andStopImport();
        }

        device.setSerialNumber(data.getSerialNumber());
        device.setYearOfCertification(data.getYearOfCertification());
        device.save();
        setShipmentDate(device, data);
    }

    @Override
    public void complete(FileImportLogger logger) {
        // do nothing
    }

    private DeviceType getDeviceTypeOrThrowException(DeviceShipmentImportRecord data) {
        return this.context.getDeviceConfigurationService().findDeviceTypeByName(data.getDeviceType())
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
