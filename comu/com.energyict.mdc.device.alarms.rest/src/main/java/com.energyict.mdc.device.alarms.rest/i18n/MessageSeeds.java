/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.i18n;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Created by albertv on 11/29/2016.
 */
public enum MessageSeeds implements MessageSeed {

    NO_APPSERVER(1, "NoAppServer", "There is no active application server that can handle this request"),
    ALARM_DOES_NOT_EXIST(2, "AlarmDoesNotExist", "Alarm doesn't exist", Level.SEVERE),
    INVALID_VALUE(5, "InvalidValue", "Invalid value", Level.SEVERE),
    ACTION_ALARM_PRIORITY_WAS_CHANGED(1000, "action.alarm.priority.was.changed", "Alarm priority has been changed" , Level.INFO),
    SNOOZE_TIME_BEFORE_CURRENT_TIME(0005, "InvalidTime", "Snooze date should be after current date.", Level.WARNING)
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return "DAL";
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }
}
