/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.rest;

import com.elster.jupiter.calendar.Calendar;

import aQute.bnd.annotation.ProviderType;

import java.time.LocalDate;

@ProviderType
public interface CalendarInfoFactory {
    CalendarInfo summaryFromCalendar(Calendar calendar);

    CalendarInfo summaryForOverview(Calendar calendar, boolean calendarInUse);

    CalendarInfo nameOnly(String name);

    CalendarInfo detailedFromCalendar(Calendar calendar);

    CalendarInfo detailedWeekFromCalendar(Calendar calendar, LocalDate localDate);
}
