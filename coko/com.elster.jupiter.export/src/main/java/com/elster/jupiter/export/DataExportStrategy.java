package com.elster.jupiter.export;

import com.elster.jupiter.time.RelativePeriod;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

public interface DataExportStrategy {

    boolean isExportUpdate();

    Optional<RelativePeriod> getUpdatePeriod();

    Optional<RelativePeriod> getUpdateWindow();

    boolean isExportContinuousData();

    ValidatedDataOption getValidatedDataOption();

    Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item);
}
