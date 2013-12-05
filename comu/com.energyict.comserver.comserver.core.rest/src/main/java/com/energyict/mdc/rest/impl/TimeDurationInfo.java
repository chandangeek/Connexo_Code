package com.energyict.mdc.rest.impl;

import com.energyict.mdc.common.TimeDuration;

public class TimeDurationInfo {
    public int count;
    public String timeUnit;

    public TimeDurationInfo() {
    }

    public TimeDurationInfo(String bogus) {
        // Constructor to allow ExtJS empty string TimeDurations
    }

    public TimeDurationInfo(TimeDuration timeDuration) {
        this.count=timeDuration.getCount();
        this.timeUnit=TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode());
    }

    public TimeDuration asTimeDuration() {
        return new TimeDuration(this.count+" "+this.timeUnit);
    }

}
