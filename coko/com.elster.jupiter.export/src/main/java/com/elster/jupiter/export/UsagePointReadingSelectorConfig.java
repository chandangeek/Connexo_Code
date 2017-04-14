/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.time.RelativePeriod;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface UsagePointReadingSelectorConfig extends ReadingDataSelectorConfig {

    UsagePointGroup getUsagePointGroup();

    Optional<MetrologyPurpose> getMetrologyPurpose();

    @Override
    Updater startUpdate();

    @ProviderType
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

        Updater setMetrologyPurpose(MetrologyPurpose metrologyPurpose);

        @Override
        UsagePointReadingSelectorConfig complete();
    }
}
