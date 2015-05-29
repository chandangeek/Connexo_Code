package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.MeterReadingData;
import com.google.common.collect.Range;

import java.time.Instant;

public interface ItemExporter {

    Range<Instant> exportItem(DataExportOccurrence occurrence, MeterReadingData item);

    void done();

}
