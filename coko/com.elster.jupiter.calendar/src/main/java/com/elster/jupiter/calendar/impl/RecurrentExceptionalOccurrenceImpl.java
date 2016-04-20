package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;

import javax.inject.Inject;
import java.time.MonthDay;

public class RecurrentExceptionalOccurrenceImpl extends ExceptionalOccurrenceImpl implements RecurrentExceptionalOccurrence {

    static final String TYPE_IDENTIFIER = "REC";

    public RecurrentExceptionalOccurrenceImpl init(Calendar calendar, DayType dayType, int day, int month) {
        return (RecurrentExceptionalOccurrenceImpl) super.init(calendar, dayType, day, month);
    }

    @Inject
    RecurrentExceptionalOccurrenceImpl(CalendarService calendarService) {
        super(calendarService);
    }

    @Override
    public MonthDay getOccurrence() {
        return MonthDay.of(getMonth(), getDay());
    }
}

