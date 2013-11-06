package com.energyict.mdc.rest.impl;

import com.energyict.cbo.TimeDuration;

public class TimeDurationInfo {
    public int count;
    public String timeUnit;

    public TimeDurationInfo() {
    }

    public TimeDurationInfo(TimeDuration timeDuration) {
        this.count=timeDuration.getCount();
        this.timeUnit=TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode());
    }

}
