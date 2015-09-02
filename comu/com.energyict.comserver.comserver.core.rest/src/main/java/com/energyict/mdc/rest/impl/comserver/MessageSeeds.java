package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.time.TimeDuration;

public enum MessageSeeds implements TranslationKey {

    MILLISECONDS(TimeDuration.TimeUnit.MILLISECONDS.getDescription(), "millisecond(s)"),
    SECONDS(TimeDuration.TimeUnit.SECONDS.getDescription(), "second(s)"),
    MINUTES(TimeDuration.TimeUnit.MINUTES.getDescription(), "minute(s)"),
    HOURS(TimeDuration.TimeUnit.HOURS.getDescription(), "hour(s)"),
    DAYS(TimeDuration.TimeUnit.DAYS.getDescription(), "day(s)"),
    WEEKS(TimeDuration.TimeUnit.WEEKS.getDescription(), "week(s)"),
    MONTHS(TimeDuration.TimeUnit.MONTHS.getDescription(), "months(s)"),
    YEARS(TimeDuration.TimeUnit.YEARS.getDescription(), "year(s)"),
    SERVLET("SERVLET", "Servlet"),
    TCP("TCP", "TCP"),
    UDP("TCP", "UDP"),
    SERIAL("SERIAL", "Serial")
    ;

    private final String key;
    private final String format;

    private MessageSeeds(String key, String format) {
        this.key = key;
        this.format = format;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }

}
