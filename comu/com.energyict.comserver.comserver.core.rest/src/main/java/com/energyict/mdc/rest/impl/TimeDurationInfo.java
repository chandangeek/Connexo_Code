package com.energyict.mdc.rest.impl;

import com.energyict.cbo.TimeDuration;

public class TimeDurationInfo {
    private int count;
    private String timeUnit;

    public TimeDurationInfo() {
    }

    public TimeDurationInfo(TimeDuration timeDuration) {
        this.count=timeDuration.getCount();
        this.timeUnit=TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode());
    }

}
