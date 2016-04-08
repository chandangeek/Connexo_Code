package com.elster.jupiter.calendar;

import aQute.bnd.annotation.ProviderType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Provides services for {@link Calendar}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-08 (09:50)
 */
@ProviderType
public interface CalendarService {

    /**
     * Starts the building process for a new {@link Calendar}.
     *
     * @param name The required name for the new Calendar
     * @param timeZone The required TimeZone
     * @param start The year from which any timeline will start
     * @return The CalendarBuilder
     */
    CalendarBuilder newCalendar(String name, TimeZone timeZone, Year start);

    Optional<Calendar> findCalendar(long id);

    Optional<Calendar> findCalendarByName(String name);

    @ProviderType
    interface CalendarBuilder {
        CalendarBuilder mRID(String mRID);
        CalendarBuilder description(String description);
        CalendarBuilder addEvent(String name, int code);
        DayTypeBuilder newDayType(String name);
        CalendarBuilder addPeriod(String name, MonthDay start, String mondayDayTypeName, String tuesdayDayTypeName, String wednesdayDayTypeName, String thursdayDayTypeName, String fridayDayTypeName, String saturdayDayTypeName, String sundayDayTypeName);
        ExceptionBuilder except(String dayTypeName);
        Calendar add();
    }

    @ProviderType
    interface DayTypeBuilder {
        DayTypeEventOccurrenceBuilder event(String eventName);
        DayTypeEventOccurrenceBuilder eventWithCode(int code);
        CalendarBuilder add();
    }

    @ProviderType
    interface DayTypeEventOccurrenceBuilder {
        DayTypeBuilder startsFrom(LocalTime localTime);
    }

    @ProviderType
    interface ExceptionBuilder {
        ExceptionBuilder occursOnceOn(LocalDate date);
        ExceptionBuilder occursAlwaysOn(MonthDay recurringDay);
        CalendarBuilder add();
    }

}