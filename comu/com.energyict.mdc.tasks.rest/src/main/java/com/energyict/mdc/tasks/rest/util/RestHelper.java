package com.energyict.mdc.tasks.rest.util;

import com.elster.jupiter.time.TimeDuration;

public class RestHelper {
    public String titleize(String s) {
        StringBuilder sb = new StringBuilder(s);
        sb.replace(0, 1, s.substring(0, 1).toUpperCase());
        return sb.toString();
    }

    public TimeDuration getTimeDuration(String units, int count) {
        return TimeDuration.TimeUnit.forDescription(units).during(count);
    }
}