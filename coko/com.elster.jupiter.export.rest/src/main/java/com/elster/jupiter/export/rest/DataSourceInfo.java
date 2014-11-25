package com.elster.jupiter.export.rest;

import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingContainer;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Created by igh on 24/11/2014.
 */
public class DataSourceInfo {

    public String mRID;
    public boolean active;
    public String serialNumber;
    public String readingType;
    public Long lastRun;
    public Long lastExportedDate;

    public DataSourceInfo(ReadingTypeDataExportItem item) {
            active = item.isActive();
            ReadingContainer readingContainer = item.getReadingContainer();
            if (readingContainer instanceof Meter) {
                Meter meter = (Meter) readingContainer;
                mRID = meter.getMRID();
                serialNumber = meter.getSerialNumber();
            }

            readingType = item.getReadingType().getAliasName();

            Optional<Instant> lastRunOptional = item.getLastRun();
            if (lastRunOptional.isPresent()) {
                lastRun = lastRunOptional.get().toEpochMilli();
            }

            Optional<Instant> lastExportedDateOptional = item.getLastExportedDate();
            if (lastExportedDateOptional.isPresent()) {
                lastExportedDate = lastExportedDateOptional.get().toEpochMilli();
            }

    }
}
