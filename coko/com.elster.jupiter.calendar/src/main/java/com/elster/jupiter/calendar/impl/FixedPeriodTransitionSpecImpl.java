package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedPeriodTransitionSpec;
import com.elster.jupiter.calendar.Period;

import javax.inject.Inject;
import java.time.LocalDate;

/**
 * Created by igh on 19/04/2016.
 */
public class FixedPeriodTransitionSpecImpl extends PeriodTransitionSpecImpl implements FixedPeriodTransitionSpec {

    static final String TYPE_IDENTIFIER = "FIX";

    private int year;

    @Inject
    FixedPeriodTransitionSpecImpl(CalendarService calendarService) {
        super(calendarService);
    }

    @Override
    public LocalDate getOccurrence() {
        return LocalDate.of(year, getMonth(), getDay());
    }


    public FixedPeriodTransitionSpecImpl init(Calendar calendar, int day, int month, int year) {
        FixedPeriodTransitionSpecImpl fixedPeriodTransitionSpecImpl =
                (FixedPeriodTransitionSpecImpl) super.init(calendar, day, month);
        fixedPeriodTransitionSpecImpl.year = year;
        return fixedPeriodTransitionSpecImpl;
    }


    @Override
    public Period getPeriod() {
        return null;
    }
}
