package com.elster.jupiter.export;

import com.google.common.collect.Range;

import java.time.Instant;

public interface DataExportStrategy {

    boolean isExportUpdate();

    boolean isExportContinuousData();

    ValidatedDataOption getValidatedDataOption();

    Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item);
}
