package com.energyict.mdc.scheduling.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.scheduling.SchedulingService;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    INVALID_VALUE(1, "SCR.InvalidValue", "Invalid value"),
    REPEAT_EVERY(2, Constants.PREVIEW_MESSAGE, "Repeat every {0}"),
    NOW(3, Constants.NOW, "now"),
    FROM(4, "from", "from"),
    DAYS(5, "days", "day(s)"),
    HOURS(6, "hours", "hour(s)"),
    MINUTES(7, "minutes", "minute(s)"),
    SECONDS(8, "seconds", "second(s)"),
    WEEKS(9, "weeks", "week(s)"),
    MONTHS(10, "months", "month(s)"),
    AT(11, "at", "at");

    public static final String COMPONENT_NAME = "SCR";

    private final int number;
    private final String key;
    private final String format;
    private final Level level;

    private MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = stripComponentNameIfPresent(key);
        this.format = format;
        this.level = Level.SEVERE;
    }

    private String stripComponentNameIfPresent(String key) {
        if (key.startsWith(SchedulingService.COMPONENT_NAME+".")) {
            return key.substring(SchedulingService.COMPONENT_NAME.length()+1);
        } else {
            return key;
        }
    }

    @Override
    public String getModule() {
        return COMPONENT_NAME;
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
        return level;
    }


    public static class Constants {
        public static final String PREVIEW_MESSAGE = SchedulingService.COMPONENT_NAME+".PreviewMessage_starting_at";
        public static final String NOW = SchedulingService.COMPONENT_NAME+".now";
        public static final String FROM = SchedulingService.COMPONENT_NAME+".from";
    }
}
