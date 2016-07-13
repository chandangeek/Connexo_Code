package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.MessageSeeds;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Provides an implementation for the {@link com.elster.jupiter.calendar.FixedExceptionalOccurrence} interface.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-04-18
 */
class FixedExceptionalOccurrenceImpl extends ExceptionalOccurrenceImpl implements FixedExceptionalOccurrence  {

    static final String TYPE_IDENTIFIER = "FIX";

    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Integer year;

    @Inject
    FixedExceptionalOccurrenceImpl(ServerCalendarService calendarService) {
        super(calendarService);
    }

    @Override
    public LocalDate getOccurrence() {
        return LocalDate.of(year, getMonth(), getDay());
    }

    @Override
    public boolean occursAt(LocalDate localDate) {
        return super.occursAt(localDate) && year == localDate.getYear();
    }

    public FixedExceptionalOccurrenceImpl init(Calendar calendar, DayType dayType, int day, int month, int year) {
        FixedExceptionalOccurrenceImpl fixedExceptionalOccurrenceImpl =
                (FixedExceptionalOccurrenceImpl) super.init(calendar, dayType, day, month);
        fixedExceptionalOccurrenceImpl.year = year;
        return fixedExceptionalOccurrenceImpl;
    }

}