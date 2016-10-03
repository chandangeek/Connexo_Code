package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import java.util.Optional;

public abstract class AbstractDeviceDataFileImportProcessor<T extends FileImportRecord> implements FileImportProcessor<T> {

    private final DeviceDataImporterContext deviceDataImporterContext;

    public AbstractDeviceDataFileImportProcessor(DeviceDataImporterContext deviceDataImporterContext) {
        this.deviceDataImporterContext = deviceDataImporterContext;
    }

    protected DeviceDataImporterContext getContext() {
        return deviceDataImporterContext;
    }

    protected Optional<Device> findDeviceByIdentifier(String deviceIdentifier) {
        DeviceService deviceService = getContext().getDeviceService();
        Optional<Device> deviceByMrid = deviceService.findDeviceByMrid(deviceIdentifier);
        return deviceByMrid.isPresent() ? deviceByMrid : deviceService.findDeviceByName(deviceIdentifier);
    }

    protected Optional<EndDevice> findEndDeviceByIdentifier(String deviceIdentifier) {
        MeteringService meteringService = getContext().getMeteringService();
        Optional<EndDevice> endDeviceByMrid = meteringService.findEndDeviceByMRID(deviceIdentifier);
        return endDeviceByMrid.isPresent() ? endDeviceByMrid : meteringService.findEndDeviceByName(deviceIdentifier);
    }

    protected Optional<UsagePoint> findUsagePointByIdentifier(String usagePointIdentifier) {
        MeteringService meteringService = getContext().getMeteringService();
        Optional<UsagePoint> usagePointByMrid = meteringService.findUsagePointByMRID(usagePointIdentifier);
        return usagePointByMrid.isPresent() ? usagePointByMrid : meteringService.findUsagePointByName(usagePointIdentifier);
    }
}
