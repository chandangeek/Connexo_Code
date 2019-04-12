package com.energyict.mdc.device.data.importers.impl.deviceeventsimport;

import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.FileImportDescriptionBasedParser;
import com.energyict.mdc.device.data.importers.impl.deviceeventsimport.DeviceEventsImportDescription;
import org.apache.commons.csv.CSVRecord;

public class FileImportReadingsDescriptionBasedParser  extends FileImportDescriptionBasedParser<DeviceEventsImportRecord> {

    public FileImportReadingsDescriptionBasedParser(FileImportDescription<DeviceEventsImportRecord> descriptor) {
        super(descriptor);
    }

    @Override
    protected void checkRecordConsictency(CSVRecord csvRecord) {
        // do nothing
    }
}
