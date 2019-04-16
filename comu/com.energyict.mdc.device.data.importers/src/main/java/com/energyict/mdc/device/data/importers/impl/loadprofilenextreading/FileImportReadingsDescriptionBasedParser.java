package com.energyict.mdc.device.data.importers.impl.loadprofilenextreading;

import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.FileImportDescriptionBasedParser;
import org.apache.commons.csv.CSVRecord;

public class FileImportReadingsDescriptionBasedParser  extends FileImportDescriptionBasedParser<DeviceLoadProfileNextReadingRecord> {

    public FileImportReadingsDescriptionBasedParser(FileImportDescription<DeviceLoadProfileNextReadingRecord> descriptor) {
        super(descriptor);
    }

    @Override
    protected void checkRecordConsictency(CSVRecord csvRecord) {
        // do nothing
    }
}
