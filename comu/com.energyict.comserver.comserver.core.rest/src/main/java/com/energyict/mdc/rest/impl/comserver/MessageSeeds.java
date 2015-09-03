package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.rest.impl.MdcApplication;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    MILLISECONDS(1, TimeDuration.TimeUnit.MILLISECONDS.getDescription(), "millisecond(s)"),
    SECONDS(2, TimeDuration.TimeUnit.SECONDS.getDescription(), "second(s)"),
    MINUTES(3, TimeDuration.TimeUnit.MINUTES.getDescription(), "minute(s)"),
    HOURS(4, TimeDuration.TimeUnit.HOURS.getDescription(), "hour(s)"),
    DAYS(5, TimeDuration.TimeUnit.DAYS.getDescription(), "day(s)"),
    WEEKS(6, TimeDuration.TimeUnit.WEEKS.getDescription(), "week(s)"),
    MONTHS(7, TimeDuration.TimeUnit.MONTHS.getDescription(), "month(s)"),
    YEARS(8, TimeDuration.TimeUnit.YEARS.getDescription(), "year(s)"),
    ;

    private final int number;
    private final String key;
    private final String format;

    private MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return MdcApplication.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

}
