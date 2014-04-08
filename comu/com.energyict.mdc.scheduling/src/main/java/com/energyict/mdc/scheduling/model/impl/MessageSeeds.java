package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.scheduling.SchedulingService;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    SHOULD_BE_AT_LEAST(1, Constants.MDC_VALUE_TOO_SMALL, "Minimal acceptable value is {min}, was {value}", Level.SEVERE),
    CAN_NOT_BE_EMPTY(2, Constants.MDC_CAN_NOT_BE_EMPTY, "This field can not be empty", Level.SEVERE),
    VALUE_NOT_IN_RANGE(3, Constants.MDC_VALUE_NOT_IN_RANGE, "{value} not in range {min} to {max}", Level.SEVERE),
    INVALID_URL(4, Constants.MDC_INVALID_URL, "{value} is not a valid URL", Level.SEVERE),
    INVALID_CHARS(5, Constants.MDC_INVALID_CHARS, "This field contains invalid chars, should obey {regex}", Level.SEVERE),
    REQUIRED_FOR_HTTPS(6, Constants.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS, "This field is mandatory in case https is chosen", Level.SEVERE),
    NOT_UNIQUE(7, Constants.MDC_NOT_UNIQUE , "the element is not unique", Level.SEVERE);

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = stripComponentNameIfPresent(key);
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    private String stripComponentNameIfPresent(String key) {
        if (key.startsWith(SchedulingService.COMPONENT_NAME+".")) {
            return key.substring(SchedulingService.COMPONENT_NAME.length()+1);
        } else {
            return key;
        }
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
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getModule() {
        return SchedulingService.COMPONENT_NAME;
    }

}

final class Constants {

    public static final String MDC_VALUE_TOO_SMALL = SchedulingService.COMPONENT_NAME+".ValueTooSmall";
    public static final String MDC_CAN_NOT_BE_EMPTY = SchedulingService.COMPONENT_NAME+".CanNotBeEmpty";
    public static final String MDC_VALUE_NOT_IN_RANGE = SchedulingService.COMPONENT_NAME+".ValueNotInRange";
    public static final String MDC_INVALID_URL = SchedulingService.COMPONENT_NAME+".InvalidURL";
    public static final String MDC_INVALID_CHARS = SchedulingService.COMPONENT_NAME+".InvalidChars";
    public static final String MDC_CAN_NOT_BE_EMPTY_IF_HTTPS = SchedulingService.COMPONENT_NAME+".CanNotBeEmptyIfHttps";

    public static final String MDC_NOT_UNIQUE = SchedulingService.COMPONENT_NAME+".notUnique";
}


