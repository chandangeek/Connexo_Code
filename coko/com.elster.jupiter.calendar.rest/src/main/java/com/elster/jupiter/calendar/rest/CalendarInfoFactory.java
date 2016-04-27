package com.elster.jupiter.calendar.rest;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.rest.CalendarInfo;

import aQute.bnd.annotation.ProviderType;

import java.time.LocalDate;

@ProviderType
public interface CalendarInfoFactory {
    CalendarInfo fromCalendar(Calendar calendar);

    CalendarInfo fromCalendar(Calendar calendar, LocalDate localDate);
}
