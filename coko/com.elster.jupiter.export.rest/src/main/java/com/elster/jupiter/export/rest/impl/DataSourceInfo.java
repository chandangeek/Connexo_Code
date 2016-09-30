package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;

public class DataSourceInfo {

    public String name;
    public String serialNumber;
    public ReadingTypeInfo readingType;
    public Long lastExportedDate;
    public Long occurrenceId;

    public DataSourceInfo(ReadingTypeDataExportItem item) {
        occurrenceId = item.getLastOccurrence().map(DataExportOccurrence::getId).orElse(null);

        item.getLastRun().ifPresent(instant -> item.getReadingContainer().getMeter(instant)
                .ifPresent(meter -> {
                    name = meter.getName();
                    serialNumber = meter.getSerialNumber();
                }));
        readingType = new ReadingTypeInfo(item.getReadingType());
        item.getLastExportedDate().ifPresent(instant -> lastExportedDate = instant.toEpochMilli());
    }
}
