package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.command.CommandRuleService;

import java.util.logging.Level;

import static java.util.logging.Level.SEVERE;


public enum MessageSeeds implements MessageSeed {
    FIELD_TOO_LONG(1, Keys.FIELD_TOO_LONG, "Field length must not exceed {max} characters"),
    FIELD_IS_REQUIRED(2, Keys.FIELD_REQUIRED, "This field is required"),
    DUPLICATE_NAME(3, Keys.DUPLICATE_NAME, "Name must be unique"),
    DAY_LIMIT_SMALLER_THAN_WEEK_AND_MONTH(4, Keys.DAY_LIMIT_SMALLER_THAN_WEEK_AND_MONTH, "Day limit must be lower than or equal to the week limit and the month limit"),
    DAY_LIMIT_SMALLER_THAN_WEEK(5, Keys.DAY_LIMIT_SMALLER_THAN_WEEK, "Day limit must be lower than or equal to the week limit"),
    DAY_LIMIT_SMALLER_THAN_MONTH(6, Keys.DAY_LIMIT_SMALLER_THAN_MONTH, "Day limit must be lower than or equal to the month limit"),
    WEEK_LIMIT_BIGGER_THAN_DAY_SMALLER_THAN_MONTH(7, Keys.WEEK_LIMIT_BIGGER_THAN_DAY_SMALLER_THAN_MONTH, "Week limit must be higher than or equal to the day limit and lower than or equal to the month limit"),
    WEEK_LIMIT_BIGGER_THAN_DAY(8, Keys.WEEK_LIMIT_BIGGER_THAN_DAY, "Week limit must be higher than or equal to the day limit"),
    WEEK_LIMIT_SMALLER_THAN_MONTH(9, Keys.WEEK_LIMIT_SMALLER_THAN_MONTH, "Week limit must be lower than or equal to the month limit"),
    MONTH_LIMIT_BIGGER_THAN_DAY_AND_WEEK(10, Keys.MONTH_LIMIT_BIGGER_THAN_DAY_AND_WEEK, "Month limit must be higher than or equal to the day limit and the week limit"),
    MONTH_LIMIT_BIGGER_THAN_DAY(11, Keys.MONTH_LIMIT_BIGGER_THAN_DAY, "Month limit must be higher than or equal to the day limit"),
    MONTH_LIMIT_BIGGER_THAN_WEEK(12, Keys.MONTH_LIMIT_BIGGER_THAN_WEEK, "Month limit must be higher than or equal to the week limit"),
    AT_LEAST_ONE_COMMAND_REQUIRED(13, Keys.AT_LEAST_ONE_COMMAND_REQUIRED, "At least one command is required"),
    DUPLICATE_COMMAND(14, Keys.DUPLICATE_COMMAND, "Duplicate commands are not allowed in a command limitation rule"),
    LIMITS_EXCEEDED(15, Keys.LIMITS_EXCEEDED, "The command cannot be added on this release date as this would exceed the {0} of ''{1}''"),
    INVALID_STATS(16, Keys.INVALID_STATS, "The counters for command limitation rules have been tampered with"),
    MAC_COMMAND_RULES_FAILED(17, Keys.MAC_COMMAND_RULES_FAILED, "Message authentication check on one or more command limitation rules failed. Please contact your system administrator.")
    ;


    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
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
        return CommandRuleService.COMPONENT_NAME;
    }

    public static class Keys {
        public static final String FIELD_REQUIRED = "X.field.required";
        public static final String FIELD_TOO_LONG = "invalidFieldLength";
        public static final String DUPLICATE_NAME = "duplicateName";
        public static final String DAY_LIMIT_SMALLER_THAN_WEEK_AND_MONTH = "dayLimitSmallerThanWeekAndMonth";
        public static final String DAY_LIMIT_SMALLER_THAN_WEEK = "dayLimitSmallerThanWeek";
        public static final String DAY_LIMIT_SMALLER_THAN_MONTH = "dayLimitSmallerThanMonth";
        public static final String WEEK_LIMIT_BIGGER_THAN_DAY_SMALLER_THAN_MONTH = "weekLimitBiggerThanDaySmallerThanMonth";
        public static final String WEEK_LIMIT_BIGGER_THAN_DAY = "weekLimitBiggerThanDay";
        public static final String WEEK_LIMIT_SMALLER_THAN_MONTH = "weekLimitSmallerThanMonth";
        public static final String MONTH_LIMIT_BIGGER_THAN_DAY_AND_WEEK = "monthLimitBiggerThanDayAndWeek";
        public static final String MONTH_LIMIT_BIGGER_THAN_DAY = "monthLimitBiggerThanDay";
        public static final String MONTH_LIMIT_BIGGER_THAN_WEEK = "monthLimitBiggerThanWeek";
        public static final String AT_LEAST_ONE_COMMAND_REQUIRED = "atLeastOneCommandRequired";
        public static final String DUPLICATE_COMMAND = "duplicateCommand";
        public static final String LIMITS_EXCEEDED = "limitsExceeded";
        public static final String INVALID_STATS = "invalidStats";
        public static final String MAC_COMMAND_RULES_FAILED = "macCommandRulesFailed";
        public static final String THE_X_OF_Y = "theXOfY";
    }
    }
