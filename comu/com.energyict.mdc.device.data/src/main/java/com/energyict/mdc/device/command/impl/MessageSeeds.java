package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.data.DeviceDataServices;

import java.util.logging.Level;

import static java.util.logging.Level.SEVERE;


public enum MessageSeeds implements MessageSeed {
    FIELD_TOO_LONG(1, Keys.FIELD_TOO_LONG, "Field length must not exceed {max} characters"),
    FIELD_IS_REQUIRED(2, Keys.FIELD_REQUIRED, "This field is required"),
    DUPLICATE_NAME(3, Keys.DUPLICATE_NAME, "Name must be unique"),
    DAY_LIMIT_SMALLER_THAN_WEEK_AND_MONTH(4, Keys.DAY_LIMIT_SMALLER_THAN_WEEK_AND_MONTH, "Day limit should be smaller than week limit and smaller than month limit"),
    DAY_LIMIT_SMALLER_THAN_WEEK(5, Keys.DAY_LIMIT_SMALLER_THAN_WEEK, "Day limit should be smaller than week limit"),
    DAY_LIMIT_SMALLER_THAN_MONTH(6, Keys.DAY_LIMIT_SMALLER_THAN_MONTH, "Day limit should be smaller than month limit"),
    WEEK_LIMIT_BIGGER_THAN_DAY_SMALLER_THAN_MONTH(7, Keys.WEEK_LIMIT_BIGGER_THAN_DAY_SMALLER_THAN_MONTH, "Week limit should be bigger than day limit and smaller than month limit"),
    WEEK_LIMIT_BIGGER_THAN_DAY(8, Keys.WEEK_LIMIT_BIGGER_THAN_DAY, "Week limit should be bigger than day limit"),
    WEEK_LIMIT_SMALLER_THAN_MONTH(9, Keys.WEEK_LIMIT_SMALLER_THAN_MONTH, "Week limit should be smaller than month limit"),
    MONTH_LIMIT_BIGGER_THAN_DAY_AND_WEEK(10, Keys.MONTH_LIMIT_BIGGER_THAN_DAY_AND_WEEK, "Month limit should be bigger than day limit and bigger than week limit"),
    MONTH_LIMIT_BIGGER_THAN_DAY(11, Keys.MONTH_LIMIT_BIGGER_THAN_DAY, "Month limit should be bigger than day limit"),
    MONTH_LIMIT_BIGGER_THAN_WEEK(12, Keys.MONTH_LIMIT_BIGGER_THAN_WEEK, "Month limit should be bigger than week limit");
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
    }
    }
