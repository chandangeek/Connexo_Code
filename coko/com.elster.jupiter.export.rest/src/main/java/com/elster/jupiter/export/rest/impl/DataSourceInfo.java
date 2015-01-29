package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.ReadingTypeDataExportItem;

import java.time.Instant;
import java.util.Optional;

/**
 * Created by igh on 24/11/2014.
 */
public class DataSourceInfo {

    public String mRID;
    public String serialNumber;
    public String readingType;
    public Long lastRun;
    public Long lastExportedDate;
    public Long occurrenceId;

    public DataSourceInfo(ReadingTypeDataExportItem item) {
        occurrenceId = item.getLastOccurrence().map(DataExportOccurrence::getId).orElse(null);

        Optional<Instant> lastRunOptional = item.getLastRun();
        if (lastRunOptional.isPresent()) {
            lastRun = lastRunOptional.get().toEpochMilli();
        }
        item.getLastRun().ifPresent(instant -> {
            item.getReadingContainer().getMeter(instant)
                    .ifPresent(meter -> {
                        mRID = meter.getMRID();
                        serialNumber = meter.getSerialNumber();
                    });
            lastRun = instant.toEpochMilli();
        });
        readingType = item.getReadingType().getAliasName();
        item.getLastExportedDate().ifPresent(instant -> {
            lastExportedDate = instant.toEpochMilli();
        });
    }
}
