/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface ReadingDataSelectorConfig extends DataSelectorConfig {

    DataExportStrategy getStrategy();

    Set<ReadingType> getReadingTypes();

    Set<ReadingType> getReadingTypes(Instant at);

    List<? extends ReadingTypeDataExportItem> getExportItems();

    ValidatedDataOption getValidatedDataOption();

    boolean isExportOnlyIfComplete();

    Updater startUpdate();

    interface Updater extends DataSelectorConfig.Updater {

        Updater addReadingType(ReadingType readingType);

        Updater removeReadingType(ReadingType readingType);

        Updater setValidatedDataOption(ValidatedDataOption option);

        Updater setExportContinuousData(boolean exportContinuousData);

        Updater setExportOnlyIfComplete(boolean exportOnlyIfComplete);

    }
}
