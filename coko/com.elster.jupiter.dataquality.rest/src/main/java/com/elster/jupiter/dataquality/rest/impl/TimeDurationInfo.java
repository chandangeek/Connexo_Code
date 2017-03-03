/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.rest.impl;

import com.elster.jupiter.time.TimeDuration;

import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;

public class TimeDurationInfo {
    public long count;
    public String timeUnit = TimeDuration.TimeUnit.SECONDS.getDescription();

    public TimeDurationInfo() {
    }

    public TimeDurationInfo(String bogus) {
        // Constructor to allow ExtJS empty string TimeDurations
    }

    public TimeDuration asTimeDuration() {
        return new TimeDuration(this.count + " " + this.timeUnit);
    }

    static TimeDurationInfo fromTemporalAmount(TemporalAmount temporalAmount) {
        TimeDurationInfo durationInfo = new TimeDurationInfo();
        for (TemporalUnit unit : temporalAmount.getUnits()) {
            fromTemporalAmount(durationInfo, temporalAmount, unit);
        }
        return durationInfo;
    }

    private static void fromTemporalAmount(TimeDurationInfo info, TemporalAmount temporalAmount, TemporalUnit unit) {
        if (info != null && info.count == 0) {
            info.count = temporalAmount.get(unit);
            info.timeUnit = unit.toString().toLowerCase();
        }
    }
}

