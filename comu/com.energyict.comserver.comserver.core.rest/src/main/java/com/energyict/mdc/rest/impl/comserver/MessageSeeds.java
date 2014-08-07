package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.rest.impl.MdcApplication;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    MILLISECONDS(1, TimeDuration.getTimeUnitDescription(TimeDuration.MILLISECONDS), "millisecond(s)"),
    SECONDS(2, TimeDuration.getTimeUnitDescription(TimeDuration.SECONDS), "second(s)"),
    MINUTES(3, TimeDuration.getTimeUnitDescription(TimeDuration.MINUTES), "minute(s)"),
    HOURS(4, TimeDuration.getTimeUnitDescription(TimeDuration.HOURS), "hour(s)"),
    DAYS(5, TimeDuration.getTimeUnitDescription(TimeDuration.DAYS), "day(s)"),
    WEEKS(6, TimeDuration.getTimeUnitDescription(TimeDuration.WEEKS), "week(s)"),
    MONTHS(7, TimeDuration.getTimeUnitDescription(TimeDuration.MONTHS), "months(s)"),
    YEARS(8, TimeDuration.getTimeUnitDescription(TimeDuration.YEARS), "year(s)"),
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
