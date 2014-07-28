package com.energyict.mdc.common.rest;

import com.energyict.mdc.common.TimeDuration;

public class TimeDurationInfo {
    public int count;
    public int timeUnit=TimeDuration.SECONDS; // Default timeUnit is now Seconds in case deserialized REST body does not contain a timeUnit

    public TimeDurationInfo() {
    }

    public TimeDurationInfo(String bogus) {
        // Constructor to allow ExtJS empty string TimeDurations
    }

    public TimeDurationInfo(TimeDuration timeDuration) {
        this.count=timeDuration.getCount();
        this.timeUnit=timeDuration.getTimeUnitCode();
    }

    public TimeDurationInfo(int seconds) {
        this.count=seconds;
        this.timeUnit=TimeDuration.SECONDS;
    }

    public TimeDuration asTimeDuration() {
        return new TimeDuration(this.count, this.timeUnit);
    }

}
