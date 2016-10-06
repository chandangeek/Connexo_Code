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
}
