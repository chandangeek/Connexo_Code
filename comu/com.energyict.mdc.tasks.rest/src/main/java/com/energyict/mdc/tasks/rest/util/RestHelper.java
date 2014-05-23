package com.energyict.mdc.tasks.rest.util;

import com.energyict.mdc.common.TimeDuration;

public class RestHelper {
    public static String titleize(String s) {
        StringBuilder sb = new StringBuilder(s);
        sb.replace(0, 1, s.substring(0, 1).toUpperCase());
        return sb.toString();
    }

    public static TimeDuration getTimeDuration(String units, int count) {
        TimeDuration timeDuration = null;
        if (units.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MILLISECONDS))) {
            timeDuration = TimeDuration.millis(count);
        } else if (units.equals(TimeDuration.getTimeUnitDescription(TimeDuration.SECONDS))) {
            timeDuration = TimeDuration.seconds(count);
        } else if (units.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MINUTES))) {
            timeDuration = TimeDuration.minutes(count);
        } else if (units.equals(TimeDuration.getTimeUnitDescription(TimeDuration.HOURS))) {
            timeDuration = TimeDuration.hours(count);
        } else if (units.equals(TimeDuration.getTimeUnitDescription(TimeDuration.DAYS))) {
            timeDuration = TimeDuration.days(count);
        } else if (units.equals(TimeDuration.getTimeUnitDescription(TimeDuration.WEEKS))) {
            timeDuration = TimeDuration.weeks(count);
        } else if (units.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MONTHS))) {
            timeDuration = TimeDuration.months(count);
//        } else if (units.equals(TimeDuration.getTimeUnitDescription(TimeDuration.YEARS))) {
//            timeDuration = TimeDuration.years(count);
        }
        return timeDuration;
    }
}