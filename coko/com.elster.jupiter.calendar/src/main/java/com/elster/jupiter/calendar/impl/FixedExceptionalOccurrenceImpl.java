package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Created by igh on 18/04/2016.
 */
public class FixedExceptionalOccurrenceImpl extends ExceptionalOccurrenceImpl implements FixedExceptionalOccurrence  {

    static final String TYPE_IDENTIFIER = "FIX";

    private int year;

    @Inject
    FixedExceptionalOccurrenceImpl(ServerCalendarService calendarService) {
        super(calendarService);
    }

    @Override
    public LocalDate getOccurrence() {
        return LocalDate.of(year, getMonth(), getDay());
    }


    public FixedExceptionalOccurrenceImpl init(Calendar calendar, DayType dayType, int day, int month, int year) {
        FixedExceptionalOccurrenceImpl fixedExceptionalOccurrenceImpl =
                (FixedExceptionalOccurrenceImpl) super.init(calendar, dayType, day, month);
        fixedExceptionalOccurrenceImpl.year = year;
        return fixedExceptionalOccurrenceImpl;
    }


}
