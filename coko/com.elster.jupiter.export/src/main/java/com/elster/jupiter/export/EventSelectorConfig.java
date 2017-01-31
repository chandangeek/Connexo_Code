/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.time.RelativePeriod;

import java.util.List;
import java.util.function.Predicate;

public interface EventSelectorConfig extends DataSelectorConfig {

    EndDeviceGroup getEndDeviceGroup();

    EventDataExportStrategy getStrategy();

    List<EndDeviceEventTypeFilter> getEventTypeFilters();

    Predicate<? super EndDeviceEventRecord> getFilterPredicate();

    Updater startUpdate();

    interface Updater extends DataSelectorConfig.Updater {

        Updater setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

        Updater addEventTypeFilter(String code);

        Updater removeEventTypeFilter(String code);

        @Override
        Updater setExportPeriod(RelativePeriod relativePeriod);

        @Override
        Updater setExportContinuousData(boolean exportContinuousData);

        @Override
        EventSelectorConfig complete();
    }
}
