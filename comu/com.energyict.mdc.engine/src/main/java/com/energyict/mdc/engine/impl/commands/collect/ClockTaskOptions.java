/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;

import java.util.Optional;

/**
 * @author sva
 * @since 11/06/2015 - 9:22
 */
public class ClockTaskOptions {

    private ClockTaskType clockTaskType;
    private Optional<TimeDuration> minimumClockDifference;
    private Optional<TimeDuration> maximumClockDifference;
    private Optional<TimeDuration> maximumClockShift;

    public ClockTaskOptions(ClockTask clockTask) {
        this.clockTaskType = clockTask.getClockTaskType();
        this.minimumClockDifference = clockTask.getMinimumClockDifference();
        this.maximumClockDifference = clockTask.getMaximumClockDifference();
        this.maximumClockShift = clockTask.getMaximumClockShift();
    }

    public ClockTaskType getClockTaskType() {
        return clockTaskType;
    }

    public void setClockTaskType(ClockTaskType clockTaskType) {
        this.clockTaskType = clockTaskType;
    }

    public Optional<TimeDuration> getMinimumClockDifference() {
        return minimumClockDifference;
    }

    public void setMinimumClockDifference(Optional<TimeDuration> minimumClockDifference) {
        this.minimumClockDifference = minimumClockDifference;
    }

    public Optional<TimeDuration> getMaximumClockDifference() {
        return maximumClockDifference;
    }

    public void setMaximumClockDifference(Optional<TimeDuration> maximumClockDifference) {
        this.maximumClockDifference = maximumClockDifference;
    }

    public Optional<TimeDuration> getMaximumClockShift() {
        return maximumClockShift;
    }

    public void setMaximumClockShift(Optional<TimeDuration> maximumClockShift) {
        this.maximumClockShift = maximumClockShift;
    }
}