/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;

import javax.inject.Inject;
import java.time.MonthDay;

class RecurrentExceptionalOccurrenceImpl extends ExceptionalOccurrenceImpl implements RecurrentExceptionalOccurrence {

    static final String TYPE_IDENTIFIER = "REC";

    RecurrentExceptionalOccurrenceImpl init(Calendar calendar, DayType dayType, MonthDay monthDay) {
        return (RecurrentExceptionalOccurrenceImpl) super.init(calendar, dayType, monthDay);
    }

    @Inject
    RecurrentExceptionalOccurrenceImpl(ServerCalendarService calendarService) {
        super(calendarService);
    }

    @Override
    public MonthDay getOccurrence() {
        return MonthDay.of(getMonth(), getDay());
    }

}