/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

    MissingDataOption getMissingDataOption();

    ValidatedDataOption getValidatedDataOption();

    Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item);
}
