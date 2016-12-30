package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.properties.TariffCalendar;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Extracts information that pertains to {@link TariffCalendar}s
 * from message related objects for the purpose
 * of formatting it as an old-style device message.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-30 (15:05)
 */
public interface TariffCalendarExtractor {
    /**
     * Extracts the unique identifier of a {@link TariffCalendar}
     * and returns it as a String for easy formatting in XML based content.
     *
     * @param calender The TariffCalendar
     * @return The String representation of the TariffCalendar's identifier
     */
    String id(TariffCalendar calender);

    String name(TariffCalendar calender);

    String seasonSetId(TariffCalendar calender);

    TimeZone definitionTimeZone(TariffCalendar calender);

    TimeZone destinationTimeZone(TariffCalendar calender);

    int intervalInSeconds(TariffCalendar calender);

    Range<Year> range(TariffCalendar calender);

    Optional<CalendarSeasonSet> season(TariffCalendar calender);

    List<CalendarDayType> dayTypes(TariffCalendar calender);

    List<CalendarRule> rules(TariffCalendar calender);

    interface CalendarSeasonSet {
        String id();
        String name();
        List<CalendarSeason> seasons();
    }

    interface CalendarSeason {
        String id();
        String name();
        List<CalendarSeasonTransition> transistions();
    }

    interface CalendarSeasonTransition {
        String id();
        Optional<Instant> start();
    }

    interface CalendarDayType {
        String id();
        String name();
        List<CalendarDayTypeSlice> slices();
    }

    interface CalendarDayTypeSlice {
        String tariffCode();
        LocalTime start();
    }

    interface CalendarRule {
        String dayTypeId();
        String dayTypeName();
        Optional<String> seasonId();
        int year();
        int month();
        int day();
        int dayOfWeek();
    }
}