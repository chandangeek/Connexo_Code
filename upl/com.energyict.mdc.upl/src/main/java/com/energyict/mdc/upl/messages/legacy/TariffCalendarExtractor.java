package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.google.common.collect.Range;

import java.time.LocalDate;
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
     * @param calendar The TariffCalendar
     * @return The String representation of the TariffCalendar's identifier
     */
    String id(TariffCalendar calendar);

    String name(TariffCalendar calendar);

    Optional<String> seasonSetId(TariffCalendar calendar);

    TimeZone definitionTimeZone(TariffCalendar calendar);

    TimeZone destinationTimeZone(TariffCalendar calendar);

    int intervalInSeconds(TariffCalendar calendar);

    Range<Year> range(TariffCalendar calendar);

    Optional<CalendarSeasonSet> season(TariffCalendar calendar);

    List<CalendarDayType> dayTypes(TariffCalendar calendar);

    List<CalendarRule> rules(TariffCalendar calendar);

    /**
     * Gets (or creates) the {@link ThreadContext} for the current Thread.
     * Each Thread will get its own ThreadContext.
     *
     * @return The ThreadContext
     */
    ThreadContext threadContext();

    /**
     * Models contextual information that can be different
     * for each Thread that uses this TariffCalendarExtractor.
     */
    interface ThreadContext {
        OfflineDevice getDevice();
        void setDevice(OfflineDevice offlineDevice);
        OfflineDeviceMessage getMessage();
        void setMessage(OfflineDeviceMessage offlineDeviceMessage);
    }

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
        Optional<LocalDate> start();
    }

    interface CalendarDayType {
        String id();
        String name();
        List<CalendarDayTypeSlice> slices();
    }

    interface CalendarDayTypeSlice {
        String dayTypeId();
        String dayTypeName();
        String tariffCode();
        LocalTime start();
    }

    interface CalendarRule {
        String calendarId();
        String dayTypeId();
        String dayTypeName();
        Optional<String> seasonId();
        int year();
        int month();
        int day();
        int dayOfWeek();
    }
}