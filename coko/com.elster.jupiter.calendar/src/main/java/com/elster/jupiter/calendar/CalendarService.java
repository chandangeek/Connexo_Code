/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.util.conditions.Condition;

import aQute.bnd.annotation.ProviderType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.List;
import java.util.Optional;

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
     * @param start The year from which any timeline will start
     * @return The CalendarBuilder
     */
    CalendarBuilder newCalendar(String name, Year start, EventSet eventSet);

    EventSetBuilder newEventSet(String name);

    List<Calendar> findAllCalendars();

    Finder<Calendar> getCalendarFinder(Condition condition);

    CalendarFilter newCalendarFilter();

    List<Category> findAllCategories();

    List<Category> findUsedCategories();

    Optional<Category> findCategoryByName(String name);

    Optional<Category> findCategory(long id);

    Optional<Calendar> findCalendar(long id);

    Optional<Calendar> findCalendarByName(String name);

    Optional<Calendar> findCalendarByMRID(String mRID);

    boolean isCalendarInUse(Calendar calendar);

    Optional<Calendar> lockCalendar(long id, long version);

    Optional<EventSet> findEventSetByName(String name);

    Optional<EventSet> findEventSet(long id);

    List<EventSet> findEventSets();

    @ProviderType
    interface CalendarBuilder {
        CalendarBuilder category(Category category);
        CalendarBuilder name(String name);
        CalendarBuilder startYear(Year start);
        CalendarBuilder endYear(Year setStartYear);
        CalendarBuilder mRID(String mRID);
        CalendarBuilder description(String description);
        DayTypeBuilder newDayType(String name);
        CalendarBuilder addPeriod(String name, String mondayDayTypeName, String tuesdayDayTypeName, String wednesdayDayTypeName, String thursdayDayTypeName, String fridayDayTypeName, String saturdayDayTypeName, String sundayDayTypeName);
        TransitionBuilder on(MonthDay occurrence);
        TransitionBuilder on(LocalDate occurrence);
        ExceptionBuilder except(String dayTypeName);
        Calendar add();
    }

    @ProviderType
    interface StrictCalendarBuilder {
        StrictExceptionBuilder except(String dayTypeName);
        Calendar add();
    }

    @ProviderType
    interface EventSetBuilder {
        EventBuilder addEvent(String name);
        EventSet add();

        interface EventBuilder {
            EventSetBuilder withCode(int code);
        }
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

    @ProviderType
    interface StrictExceptionBuilder {
        StrictExceptionBuilder occursOnceOn(LocalDate date);
        StrictCalendarBuilder add();
    }

}