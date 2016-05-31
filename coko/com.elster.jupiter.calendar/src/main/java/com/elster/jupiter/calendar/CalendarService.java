package com.elster.jupiter.calendar;

import aQute.bnd.annotation.ProviderType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.List;
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

    String COMPONENTNAME = "CAL";

    /**
     * Starts the building process for a new {@link Calendar}.
     *
     * @param name The required name for the new Calendar
     * @param timeZone The required TimeZone
     * @param start The year from which any timeline will start
     * @return The CalendarBuilder
     */
    CalendarBuilder newCalendar(String name, TimeZone timeZone, Year start);

    List<Calendar> findAllCalendars();

    Optional<Category> findCategoryByName(String name);

    Optional<Calendar> findCalendar(long id);

    Optional<Calendar> findCalendarByName(String name);

    Optional<Calendar> findCalendarByMRID(String mRID);

    @ProviderType
    interface CalendarBuilder {
        CalendarBuilder name(String name);
        CalendarBuilder timeZone(TimeZone timeZone);
        CalendarBuilder startYear(Year start);
        CalendarBuilder endYear(Year setStartYear);
        CalendarBuilder mRID(String mRID);
        CalendarBuilder description(String description);
        CalendarBuilder addEvent(String name, int code);
        DayTypeBuilder newDayType(String name);
        CalendarBuilder addPeriod(String name, String mondayDayTypeName, String tuesdayDayTypeName, String wednesdayDayTypeName, String thursdayDayTypeName, String fridayDayTypeName, String saturdayDayTypeName, String sundayDayTypeName);
        TransitionBuilder on(MonthDay occurrence);
        TransitionBuilder on(LocalDate occurrence);
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
    interface TransitionBuilder {
        CalendarBuilder transitionTo(String name);
    }

    @ProviderType
    interface ExceptionBuilder {
        ExceptionBuilder occursOnceOn(LocalDate date);
        ExceptionBuilder occursAlwaysOn(MonthDay recurringDay);
        CalendarBuilder add();
    }

}