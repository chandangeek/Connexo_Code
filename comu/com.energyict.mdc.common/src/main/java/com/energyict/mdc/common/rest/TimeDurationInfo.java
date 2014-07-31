package com.energyict.mdc.common.rest;

import com.energyict.mdc.common.TimeDuration;

public class TimeDurationInfo {
    public int count;
    public String timeUnit = TimeDuration.getTimeUnitDescription(TimeDuration.SECONDS);

    public TimeDurationInfo() {
    }

    public TimeDurationInfo(String bogus) {
        // Constructor to allow ExtJS empty string TimeDurations
    }

    public TimeDurationInfo(TimeDuration timeDuration) {
        this.count=timeDuration.getCount();
        this.timeUnit=TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode());
    }

    public TimeDurationInfo(int seconds) {
        this.count=seconds;
        this.timeUnit=TimeDuration.getTimeUnitDescription(TimeDuration.SECONDS);
    }

    public TimeDuration asTimeDuration() {
        return new TimeDuration(this.count+" "+this.timeUnit);
    }

}
