package com.elster.jupiter.export;

import com.google.common.collect.Range;

import java.time.Instant;

public interface EventDataExportStrategy {

    boolean isExportContinuousData();

    Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item);
}
