package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.FileImportRecordContext;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;

public class DeviceReadingsImportProcessor implements FileImportProcessor<DeviceReadingsImportRecord> {

    private final DeviceDataImporterContext context;

    DeviceReadingsImportProcessor(DeviceDataImporterContext context) {
        this.context = context;
    }

    @Override
    public void process(DeviceReadingsImportRecord data, FileImportRecordContext recordContext) throws ProcessorException {

    }
}
