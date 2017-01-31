/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.time.RelativePeriod;

public interface UsagePointReadingSelectorConfig extends ReadingDataSelectorConfig {

    UsagePointGroup getUsagePointGroup();

    @Override
    Updater startUpdate();

    interface Updater extends ReadingDataSelectorConfig.Updater {

        @Override
        Updater setExportPeriod(RelativePeriod relativePeriod);

        @Override
        Updater setValidatedDataOption(ValidatedDataOption option);

        @Override
        Updater setExportOnlyIfComplete(boolean exportOnlyIfComplete);

        @Override
        Updater setExportContinuousData(boolean exportContinuousData);

        Updater setUsagePointGroup(UsagePointGroup usagePointGroup);

        @Override
        UsagePointReadingSelectorConfig complete();
    }
}
