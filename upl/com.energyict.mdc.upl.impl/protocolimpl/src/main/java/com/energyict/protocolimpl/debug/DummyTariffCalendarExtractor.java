package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.google.common.collect.Range;

import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Provides an dummy implementation for the {@link Extractor} interface
 * that returns empty values in all of its methods.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-28 (09:49)
 */
final class DummyTariffCalendarExtractor implements TariffCalendarExtractor {
    @Override
    public String id(TariffCalendar calender) {
        return "";
    }

    @Override
    public String name(TariffCalendar calender) {
        return "";
    }

    @Override
    public String seasonSetId(TariffCalendar calender) {
        return "";
    }

    @Override
    public TimeZone definitionTimeZone(TariffCalendar calender) {
        return TimeZone.getDefault();
    }

    @Override
    public TimeZone destinationTimeZone(TariffCalendar calender) {
        return TimeZone.getDefault();
    }

    @Override
    public int intervalInSeconds(TariffCalendar calender) {
        return 0;
    }

    @Override
    public Range<Year> range(TariffCalendar calender) {
        return Range.singleton(Year.of(Year.MIN_VALUE));
    }

    @Override
    public Optional<CalendarSeasonSet> season(TariffCalendar calender) {
        return Optional.empty();
    }

    @Override
    public List<CalendarDayType> dayTypes(TariffCalendar calender) {
        return Collections.emptyList();
    }

    @Override
    public List<CalendarRule> rules(TariffCalendar calender) {
        return Collections.emptyList();
    }
}