/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;


import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.tasks.LoadProfilesTask;

import java.util.List;
import java.util.Optional;

/**
 * @author sva
 * @since 10/06/2015 - 16:42
 */
public class LoadProfilesTaskOptions {

    private List<LoadProfileType> loadProfileTypes;
    private boolean failIfLoadProfileConfigurationMisMatch;
    private boolean markIntervalsAsBadTime;
    private boolean createMeterEventsFromStatusFlags;
    private Optional<TimeDuration> minClockDiffBeforeBadTime;

    public LoadProfilesTaskOptions() {
    }

    public LoadProfilesTaskOptions(LoadProfilesTask loadProfilesTask) {
        this.loadProfileTypes = loadProfilesTask.getLoadProfileTypes();
        this.failIfLoadProfileConfigurationMisMatch = loadProfilesTask.failIfLoadProfileConfigurationMisMatch();
        this.markIntervalsAsBadTime = loadProfilesTask.isMarkIntervalsAsBadTime();
        this.createMeterEventsFromStatusFlags = loadProfilesTask.createMeterEventsFromStatusFlags();
        this.minClockDiffBeforeBadTime = loadProfilesTask.getMinClockDiffBeforeBadTime();
    }

    public List<LoadProfileType> getLoadProfileTypes() {
        return loadProfileTypes;
    }

    public void setLoadProfileTypes(List<LoadProfileType> loadProfileTypes) {
        this.loadProfileTypes = loadProfileTypes;
    }

    public boolean isFailIfLoadProfileConfigurationMisMatch() {
        return failIfLoadProfileConfigurationMisMatch;
    }

    public void setFailIfLoadProfileConfigurationMisMatch(boolean failIfLoadProfileConfigurationMisMatch) {
        this.failIfLoadProfileConfigurationMisMatch = failIfLoadProfileConfigurationMisMatch;
    }

    public boolean isMarkIntervalsAsBadTime() {
        return markIntervalsAsBadTime;
    }

    public void setMarkIntervalsAsBadTime(boolean markIntervalsAsBadTime) {
        this.markIntervalsAsBadTime = markIntervalsAsBadTime;
    }

    public boolean isCreateMeterEventsFromStatusFlags() {
        return createMeterEventsFromStatusFlags;
    }

    public void setCreateMeterEventsFromStatusFlags(boolean createMeterEventsFromStatusFlags) {
        this.createMeterEventsFromStatusFlags = createMeterEventsFromStatusFlags;
    }

    public Optional<TimeDuration> getMinClockDiffBeforeBadTime() {
        return minClockDiffBeforeBadTime;
    }

    public void setMinClockDiffBeforeBadTime(Optional<TimeDuration> minClockDiffBeforeBadTime) {
        this.minClockDiffBeforeBadTime = minClockDiffBeforeBadTime;
    }
}