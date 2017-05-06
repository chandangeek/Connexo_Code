/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.RelativePeriod;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReadingDataSelectorConfig extends DataSelectorConfig {

    DataExportStrategy getStrategy();

    Set<ReadingType> getReadingTypes();

    Set<ReadingType> getReadingTypes(Instant at);

    List<? extends ReadingTypeDataExportItem> getExportItems();

    boolean isExportUpdate();

    Optional<RelativePeriod> getUpdatePeriod();

    Optional<RelativePeriod> getUpdateWindow();

    ValidatedDataOption getValidatedDataOption();

    MissingDataOption isExportOnlyIfComplete();

    Updater startUpdate();

    interface Updater extends DataSelectorConfig.Updater {

        Updater addReadingType(ReadingType readingType);

        Updater removeReadingType(ReadingType readingType);

        Updater setValidatedDataOption(ValidatedDataOption option);

        Updater setExportContinuousData(boolean exportContinuousData);

        Updater setExportOnlyIfComplete(MissingDataOption missingDataOption);

        Updater setExportUpdate(boolean exportUpdate);

        Updater setUpdatePeriod(RelativePeriod updatePeriod);

        Updater setUpdateWindow(RelativePeriod updateWindow);
    }
}
