package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.calendar.rest.CalendarInfo;
import com.energyict.mdc.device.config.AllowedCalendar;

public class AllowedCalendarInfo {

    public String name;
    public String status;
    public CalendarInfo calendar;

    public AllowedCalendarInfo (AllowedCalendar allowedCalendar) {
        this.name = allowedCalendar.getName();
        this.status = "Active";
    }

    public AllowedCalendarInfo (AllowedCalendar allowedCalendar, CalendarInfo calendarInfo) {
        this(allowedCalendar);
        this.calendar = calendarInfo;
    }

}
