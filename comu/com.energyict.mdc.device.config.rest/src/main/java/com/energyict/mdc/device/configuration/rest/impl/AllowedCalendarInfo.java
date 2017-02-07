/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.calendar.rest.CalendarInfo;
import com.energyict.mdc.device.config.AllowedCalendar;

public class AllowedCalendarInfo {

    public long id;
    public String name;
    public boolean ghost;
    public CalendarInfo calendar;

    public AllowedCalendarInfo () {
    }

    public AllowedCalendarInfo (AllowedCalendar allowedCalendar) {
        this.name = allowedCalendar.getName();
        this.ghost = allowedCalendar.isGhost();
        this.id = allowedCalendar.getId();
    }

    public AllowedCalendarInfo (AllowedCalendar allowedCalendar, CalendarInfo calendarInfo) {
        this(allowedCalendar);
        this.calendar = calendarInfo;
    }

}
