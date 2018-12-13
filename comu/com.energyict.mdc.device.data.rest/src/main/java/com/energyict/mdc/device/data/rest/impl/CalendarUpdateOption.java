/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;


import java.util.Arrays;

public enum CalendarUpdateOption {
    FULL_CALENDAR("fullCalendar"),
    SPECIAL_DAYS("specialDays"),
    ACTIVITY_CALENDAR("activityCalendar");

    private String key;

    CalendarUpdateOption(String key) {
        this.key = key;
    }

    public static CalendarUpdateOption find(String key) {
        return Arrays.stream(values()).filter(o -> o.key.equals(key)).findFirst().orElse(null);
    }
}
