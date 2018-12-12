/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.time.RelativePeriod;

import java.util.Optional;

public interface MeterReadingSelectorConfig extends ReadingDataSelectorConfig {

    EndDeviceGroup getEndDeviceGroup();
    default long getEndDeviceGroupId(){
         return getEndDeviceGroup().getId();
     }

    @Override
    Updater startUpdate();

    interface Updater extends ReadingDataSelectorConfig.Updater {
        @Override
        Updater addReadingType(ReadingType readingType);

        @Override
        Updater removeReadingType(ReadingType readingType);

        @Override
        Updater setValidatedDataOption(ValidatedDataOption option);

        @Override
        Updater setExportContinuousData(boolean exportContinuousData);

        @Override
        Updater setExportOnlyIfComplete(MissingDataOption missingDataOption);

        @Override
        Updater setExportPeriod(RelativePeriod relativePeriod);

        Updater setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

        Updater setExportUpdate(boolean exportUpdate);

        Updater setUpdatePeriod(RelativePeriod updatePeriod);

        Updater setUpdateWindow(RelativePeriod updateWindow);

        @Override
        MeterReadingSelectorConfig complete();
    }
}
