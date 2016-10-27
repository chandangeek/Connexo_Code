package com.elster.jupiter.export;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.time.RelativePeriod;

import java.util.Optional;

public interface MeterReadingSelectorConfig extends ReadingDataSelectorConfig {

    EndDeviceGroup getEndDeviceGroup();

    boolean isExportUpdate();

    Optional<RelativePeriod> getUpdatePeriod();

    Optional<RelativePeriod> getUpdateWindow();

    @Override
    Updater startUpdate();

    interface Updater extends ReadingDataSelectorConfig.Updater {

        @Override
        Updater setExportPeriod(RelativePeriod relativePeriod);

        @Override
        Updater setExportContinuousData(boolean exportContinuousData);

        @Override
        Updater setExportOnlyIfComplete(boolean exportOnlyIfComplete);

        @Override
        Updater setValidatedDataOption(ValidatedDataOption option);

        Updater setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

        Updater setExportUpdate(boolean exportUpdate);

        Updater setUpdatePeriod(RelativePeriod updatePeriod);

        Updater setUpdateWindow(RelativePeriod updateWindow);

        @Override
        MeterReadingSelectorConfig complete();
    }
}
