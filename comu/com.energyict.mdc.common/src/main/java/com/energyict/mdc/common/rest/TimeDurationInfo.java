package com.energyict.mdc.common.rest;

import com.elster.jupiter.time.TimeDuration;

public class TimeDurationInfo {
    public int count;
    public String timeUnit = TimeDuration.TimeUnit.SECONDS.getDescription();

    public TimeDurationInfo() {
    }

    public TimeDurationInfo(String bogus) {
        // Constructor to allow ExtJS empty string TimeDurations
    }

    public TimeDurationInfo(TimeDuration timeDuration) {
        this.count=timeDuration.getCount();
        this.timeUnit=TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode());
    }

    public TimeDurationInfo(long seconds) {
        this.count=seconds;
        this.timeUnit=TimeDuration.TimeUnit.SECONDS.getDescription();
    }

    public TimeDuration asTimeDuration() {
        return new TimeDuration(this.count+" "+this.timeUnit);
    }

}
