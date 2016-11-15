package com.energyict.mdc.masterdata;

import com.elster.jupiter.time.TimeDuration;

public enum LoadProfileIntervals {

    MINUTES_FIVE(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES)),
    MINUTES_TEN(new TimeDuration(10, TimeDuration.TimeUnit.MINUTES)),
    MINUTES_FIFTEEN(new TimeDuration(15, TimeDuration.TimeUnit.MINUTES)),
    MINUTES_THIRTY(new TimeDuration(30, TimeDuration.TimeUnit.MINUTES)),
    HOURS_ONE(new TimeDuration(1, TimeDuration.TimeUnit.HOURS)),
    DAYS_ONE(new TimeDuration(1, TimeDuration.TimeUnit.DAYS)),
    MONTHS_ONE(new TimeDuration(1, TimeDuration.TimeUnit.MONTHS)),;

    private final TimeDuration timeDuration;

    LoadProfileIntervals(TimeDuration timeDuration) {
        this.timeDuration = timeDuration;
    }

    public TimeDuration getTimeDuration() {
        return timeDuration;
    }
}
