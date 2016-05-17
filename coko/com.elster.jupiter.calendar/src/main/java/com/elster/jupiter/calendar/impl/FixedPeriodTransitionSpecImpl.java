package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedPeriodTransitionSpec;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.calendar.Period;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Created by igh on 19/04/2016.
 */
public class FixedPeriodTransitionSpecImpl extends PeriodTransitionSpecImpl implements FixedPeriodTransitionSpec {

    static final String TYPE_IDENTIFIER = "FIX";

    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Integer year;

    @Inject
    FixedPeriodTransitionSpecImpl(ServerCalendarService calendarService) {
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

}
