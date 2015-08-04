package com.energyict.mdc.device.data.importers.impl.devices.shipment;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.imp.Batch;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.FileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;

public class DeviceShipmentImportProcessor implements FileImportProcessor<DeviceShipmentImportRecord> {

    private final DeviceDataImporterContext context;

    public DeviceShipmentImportProcessor(DeviceDataImporterContext context) {
        this.context = context;
    }

    @Override
    public void process(DeviceShipmentImportRecord data, FileImportLogger logger) throws ProcessorException {
        if (this.context.getDeviceService().findByUniqueMrid(data.getDeviceMRID()).isPresent()){
            throw new ProcessorException(MessageSeeds.DEVICE_ALREADY_EXISTS, data.getLineNumber(), data.getDeviceMRID());
        }
        DeviceType deviceType = getDeviceTypeOrThrowException(data);
        DeviceConfiguration deviceConfiguration = getDeviceConfigurationOrThrowException(deviceType, data);
        Device device = this.context.getDeviceService().newDevice(deviceConfiguration, data.getDeviceMRID(), data.getDeviceMRID());
        device.setSerialNumber(data.getSerialNumber());
        device.setYearOfCertification(data.getYearOfCertification());
        device.save();
        setShipmentDate(device, data);
        addDeviceInBatch(device, data);
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

    private void addDeviceInBatch(Device device, DeviceShipmentImportRecord data) {
        if (data.getBatch() != null) {
            Batch batch = this.context.getDeviceImportService().findOrCreateBatch(data.getBatch());
            batch.addDevice(device);
            batch.save();
        }
    }

    private void setShipmentDate(Device device, DeviceShipmentImportRecord data) {
        CIMLifecycleDates lifecycleDates = device.getLifecycleDates();
        lifecycleDates.setReceivedDate(data.getShipmentDate().toInstant());
        lifecycleDates.save();
    }
}
