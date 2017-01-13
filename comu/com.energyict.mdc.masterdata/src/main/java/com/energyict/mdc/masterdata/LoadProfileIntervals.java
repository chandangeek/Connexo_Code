package com.energyict.mdc.masterdata;

import com.elster.jupiter.time.TimeDuration;

public enum LoadProfileIntervals {

    MINUTE_ONE("MINUTE", new TimeDuration(1, TimeDuration.TimeUnit.MINUTES)),
    MINUTES_TWO("MINUTES", new TimeDuration(2, TimeDuration.TimeUnit.MINUTES)),
    MINUTES_THREE("MINUTES", new TimeDuration(3, TimeDuration.TimeUnit.MINUTES)),
    MINUTES_FIVE("MINUTES", new TimeDuration(5, TimeDuration.TimeUnit.MINUTES)),
    MINUTES_TEN("MINUTES", new TimeDuration(10, TimeDuration.TimeUnit.MINUTES)),
    MINUTES_FIFTEEN("MINUTES", new TimeDuration(15, TimeDuration.TimeUnit.MINUTES)),
    MINUTES_TWENTY("MINUTES", new TimeDuration(20, TimeDuration.TimeUnit.MINUTES)),
    MINUTES_THIRTY("MINUTES", new TimeDuration(30, TimeDuration.TimeUnit.MINUTES)),
    HOURS_ONE("HOUR", new TimeDuration(1, TimeDuration.TimeUnit.HOURS)),
    HOURS_TWO("HOURS", new TimeDuration(2, TimeDuration.TimeUnit.HOURS)),
    HOURS_THREE("HOURS", new TimeDuration(3, TimeDuration.TimeUnit.HOURS)),
    HOURS_FOUR("HOURS", new TimeDuration(4, TimeDuration.TimeUnit.HOURS)),
    HOURS_SIX("HOURS", new TimeDuration(6, TimeDuration.TimeUnit.HOURS)),
    HOURS_TWELVE("HOURS", new TimeDuration(12, TimeDuration.TimeUnit.HOURS)),
    DAYS_ONE("DAY", new TimeDuration(1, TimeDuration.TimeUnit.DAYS)),
    MONTHS_ONE("MONTHS", new TimeDuration(1, TimeDuration.TimeUnit.MONTHS));

    private final TimeDuration timeDuration;
    private final String unitName;

    LoadProfileIntervals(String unitName, TimeDuration timeDuration) {
        this.unitName = unitName;
        this.timeDuration = timeDuration;
    }

    public TimeDuration getTimeDuration() {
        return timeDuration;
    }

    public String unitName(){
        return unitName;
    }

}
