package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;

import java.time.Instant;
import java.util.Optional;

public class DataSourceInfo {

    public String mRID;
    public String serialNumber;
    public ReadingTypeInfo readingType;
    public Long lastExportedDate;
    public Long occurrenceId;

    public DataSourceInfo(ReadingTypeDataExportItem item) {
        occurrenceId = item.getLastOccurrence().map(DataExportOccurrence::getId).orElse(null);

        item.getLastRun().ifPresent(instant -> {
            item.getReadingContainer().getMeter(instant)
                    .ifPresent(meter -> {
                        mRID = meter.getMRID();
                        serialNumber = meter.getSerialNumber();
                    });
        });
        readingType = new ReadingTypeInfo(item.getReadingType());
        item.getLastExportedDate().ifPresent(instant -> {
            lastExportedDate = instant.toEpochMilli();
        });
    }
}
