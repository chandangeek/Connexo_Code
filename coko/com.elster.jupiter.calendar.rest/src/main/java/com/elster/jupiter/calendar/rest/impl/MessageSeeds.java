/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    //MESSAGES HERE
    NO_SUCH_TIME_OF_USE_CALENDAR(1, "NoSuchTimeOfUseCalendar", "Time of use calendar does not exist"),
    TIME_OF_USE_CALENDAR_IN_USE(2, "CalendarInUse", "Calendar is still in use"),
    NO_SUCH_CATEGORY(3, "NoSuchCategory", "Category does not exist"),
    ACTIVE_CALENDAR_CANT_BE_REMOVED(4, "CalendarInActiveState", "You can''t remove an active calendar");

    private final int number;
    private final String key;
    private final String defaultFormat;

    MessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return CalendarApplication.COMPONENT_NAME;
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
        return Level.SEVERE;
    }
}
