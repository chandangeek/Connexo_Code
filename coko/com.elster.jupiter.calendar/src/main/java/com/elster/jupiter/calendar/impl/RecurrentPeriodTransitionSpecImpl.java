package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.RecurrentPeriodTransitionSpec;

import javax.inject.Inject;
import java.time.MonthDay;

/**
 * Created by igh on 19/04/2016.
 */
public class RecurrentPeriodTransitionSpecImpl extends PeriodTransitionSpecImpl implements RecurrentPeriodTransitionSpec {

    static final String TYPE_IDENTIFIER = "REC";

    public RecurrentPeriodTransitionSpecImpl init(Calendar calendar, int day, int month) {
        return (RecurrentPeriodTransitionSpecImpl) super.init(calendar, day, month);
    }

    @Inject
    RecurrentPeriodTransitionSpecImpl(ServerCalendarService calendarService) {
        super(calendarService);
    }

    @Override
    public MonthDay getOccurrence() {
        return MonthDay.of(getMonth(), getDay());
    }
}

