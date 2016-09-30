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
}
