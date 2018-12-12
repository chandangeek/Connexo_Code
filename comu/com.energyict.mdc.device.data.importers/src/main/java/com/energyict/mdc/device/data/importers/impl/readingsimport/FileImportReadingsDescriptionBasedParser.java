package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.fileimport.csvimport.exceptions.FileImportLineException;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.FileImportDescriptionBasedParser;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.readingsimport.DeviceReadingsImportDescription;
import com.energyict.mdc.device.data.importers.impl.readingsimport.DeviceReadingsImportRecord;
import org.apache.commons.csv.CSVRecord;

public class FileImportReadingsDescriptionBasedParser  extends FileImportDescriptionBasedParser<DeviceReadingsImportRecord> {

    public FileImportReadingsDescriptionBasedParser(FileImportDescription<DeviceReadingsImportRecord> descriptor) {
        super(descriptor);
    }

    @Override
    protected void checkRecordConsictency(CSVRecord csvRecord) {
        // do nothing
    }
}
