/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link ServerCalendar.ZonedView} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-03 (10:53)
 */
public class ZonedCalenderViewImpl implements ServerCalendar.ZonedView {
    private final ServerCalendar calendar;
    private final ZoneId zoneId;
    private final int startYear;
    private final int endYear;
    private Set<FixedDayTypeOccurrence> exceptionalOccurrences;
    private Set<FixedDayTypeOccurrence> dayTypeOccurrences;

    public ZonedCalenderViewImpl(ServerCalendar calendar, Clock clock, ZoneId zoneId, Year year) {
        this.calendar = calendar;
        this.zoneId = zoneId;
        this.startYear = year.getValue();
        this.endYear = Year.now(clock).getValue();
        this.fixRecurringExceptions();
        this.fixPeriodTransitions();
    }

    /**
     * Converts all {@link RecurrentExceptionalOccurrence}s of the Calendar
     * to fixed occurrences within the year that was specified at construction time.
     */
    private void fixRecurringExceptions() {
        this.exceptionalOccurrences = this.calendar
                .getExceptionalOccurrences()
                .stream()
                .flatMap(this::toZonedFixedOccurrence)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private Stream<FixedDayTypeOccurrence> toZonedFixedOccurrence(ExceptionalOccurrence exceptionalOccurrence) {
        return IntStream
                    .range(this.startYear, this.endYear + 1)
                    .mapToObj(year -> this.toZonedFixedOccurrence(exceptionalOccurrence, year));
    }

    private FixedDayTypeOccurrence toZonedFixedOccurrence(ExceptionalOccurrence exceptionalOccurrence, int year) {
        ZonedDateTime zonedDateTime;
        if (exceptionalOccurrence instanceof RecurrentExceptionalOccurrence) {
            RecurrentExceptionalOccurrence occurrence = (RecurrentExceptionalOccurrence) exceptionalOccurrence;
            zonedDateTime = occurrence.getOccurrence().atYear(year).atStartOfDay(this.zoneId);
        } else {
            FixedExceptionalOccurrence occurrence = (FixedExceptionalOccurrence) exceptionalOccurrence;
            zonedDateTime = occurrence.getOccurrence().atStartOfDay(this.zoneId);
        }
        return new FixedDayTypeOccurrence(zonedDateTime, exceptionalOccurrence.getDayType());
    }

    /**
     * Converts all {@link PeriodTransition}s of the Calendar
     * to fixed occurrences of a DayType within the year
     * that was specified at construction time.
     */
    private void fixPeriodTransitions() {
        ZonedDateTime defaultEnd = Year.of(this.endYear).atDay(1).plusYears(1).atStartOfDay(this.zoneId);
        List<FixedPeriodOccurrence> builder = new ArrayList<>();
        ZonedDateTime fromJan1stToFirstStart = null;   // We may not need it if the first occurrence starts on Jan 1st
        boolean first = true;
        FixedPeriodOccurrence previous = null;
        for (PeriodTransition transition : this.calendar.getTransitions()) {
            ZonedDateTime start = transition.getOccurrence().atStartOfDay(this.zoneId);
            if (first && transition.getOccurrence().getDayOfYear() != 1) {
                /* First entry does not start on Jan 1st,
                 * add an additional one that will get the same code
                 * as the last event occurrence. */
                fromJan1stToFirstStart = start;
            }
            first = false;
            FixedPeriodOccurrence current = new FixedPeriodOccurrence(this.toZonedPeriod(transition.getPeriod()), start, defaultEnd);
            builder.add(current);
            if (previous != null) {
                previous.setEnd(start);
            }
            previous = current;
        }
        if (fromJan1stToFirstStart != null && previous != null) {
            builder.add(
                    0,
                    new FixedPeriodOccurrence(
                            previous.period,
                            this.calendar.getStartYear().atDay(1).atStartOfDay(this.zoneId),
                            fromJan1stToFirstStart));
        }
        this.dayTypeOccurrences =
                builder.stream()
                        .map(FixedPeriodOccurrence::occurrences)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toCollection(TreeSet::new));
    }

    private ZonedPeriod toZonedPeriod(Period period) {
        return new ZonedPeriod(
                period.getDayType(DayOfWeek.SUNDAY),
                period.getDayType(DayOfWeek.MONDAY),
                period.getDayType(DayOfWeek.TUESDAY),
                period.getDayType(DayOfWeek.WEDNESDAY),
                period.getDayType(DayOfWeek.THURSDAY),
                period.getDayType(DayOfWeek.FRIDAY),
                period.getDayType(DayOfWeek.SATURDAY));
    }

    @Override
    public Event eventFor(Instant instant) {
        return this.exceptionalEvent(instant).orElseGet(() -> this.fixedEvent(instant));
    }

    private Optional<Event> exceptionalEvent(Instant instant) {
        return this.exceptionalOccurrences
                    .stream()
                    .filter(occurrence -> occurrence.contains(instant))
                    .map(occurrence -> occurrence.eventFor(instant))
                    .findFirst();
    }

    private Event fixedEvent(Instant instant) {
        return this.dayTypeOccurrences
                .stream()
                .filter(occurrence -> occurrence.contains(instant))
                .map(occurrence -> occurrence.eventFor(instant))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Specification for calendar(id=" + this.calendar.getId() + ", name=" + this.calendar.getName() + ") is not conclusive for instant " + instant));
    }

    private static class FixedEventOccurrence {
        private final Event event;
        private final Instant from;
        private Range<Instant> range;

        private FixedEventOccurrence(Event event, Instant from, Instant to) {
            this.event = event;
            this.from = from;
            this.initializeRange(to);
        }

        private void initializeRange(Instant to) {
            this.range = Range.closedOpen(this.from, to);
        }

        void setTo(Instant to) {
            this.initializeRange(to);
        }

        Event getEvent() {
            return event;
        }

        boolean contains(Instant instant) {
            return this.range.contains(instant);
        }

    }

    private static class FixedDayTypeOccurrence implements Comparable<FixedDayTypeOccurrence> {
        private final ZonedDateTime startOfDay;
        private final Range<Instant> range;
        private final List<FixedEventOccurrence> eventOccurrences;

        private FixedDayTypeOccurrence(ZonedDateTime startOfDay, DayType dayType) {
            this.startOfDay = startOfDay;
            Instant startOfNextDay = startOfDay.plusDays(1).toInstant();
            this.range = Range.closedOpen(startOfDay.toInstant(), startOfDay.plusDays(1).toInstant());
            this.eventOccurrences = new ArrayList<>();
            Instant fromMidnightToFirstStart = null;   // We may not need it if the first occurrence starts at midnight
            boolean first = true;
            FixedEventOccurrence previous = null;
            for (EventOccurrence eventOccurrence : dayType.getEventOccurrences()) {
                Instant from = eventOccurrence.getFrom().atDate(startOfDay.toLocalDate()).atZone(startOfDay.getZone()).toInstant();
                if (first && !eventOccurrence.getFrom().equals(LocalTime.MIDNIGHT)) {
                    /* First entry does not start at midnight,
                     * add an additional one that will get the same code
                     * as the last event occurrence. */
                    fromMidnightToFirstStart = from;
                }
                first = false;
                FixedEventOccurrence current = new FixedEventOccurrence(eventOccurrence.getEvent(), from, startOfNextDay);
                this.eventOccurrences.add(current);
                if (previous != null) {
                    previous.setTo(from);
                }
                previous = current;
            }
            if (fromMidnightToFirstStart != null && previous != null) {
                this.eventOccurrences.add(
                        0,
                        new FixedEventOccurrence(
                                previous.getEvent(),
                                LocalTime.MIDNIGHT.atDate(this.startOfDay.toLocalDate()).atZone(this.startOfDay.getZone()).toInstant(),
                                fromMidnightToFirstStart));
            }
        }

        boolean contains(Instant instant) {
            return this.range.contains(instant);
        }

        Event eventFor(Instant instant) {
            return this.eventOccurrences
                    .stream()
                    .filter(o -> o.contains(instant))
                    .findFirst()
                    .map(FixedEventOccurrence::getEvent)
                    .orElseThrow(() -> new IllegalStateException("FixedDayTypeOccurrence should always return an Event for an Instant that it contains"));
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            FixedDayTypeOccurrence that = (FixedDayTypeOccurrence) other;
            return Objects.equals(startOfDay, that.startOfDay);
        }

        @Override
        public int hashCode() {
            return Objects.hash(startOfDay);
        }

        @Override
        public int compareTo(FixedDayTypeOccurrence that) {
            return this.startOfDay.compareTo(that.startOfDay);
        }
    }

    private static class FixedPeriodOccurrence {
        private final ZonedPeriod period;
        private final ZonedDateTime start;
        private ZonedDateTime end;

        private FixedPeriodOccurrence(ZonedPeriod period, ZonedDateTime start, ZonedDateTime end) {
            this.period = period;
            this.start = start;
            this.end = end;
        }

        void setEnd(ZonedDateTime end) {
            this.end = end;
        }

        Collection<FixedDayTypeOccurrence> occurrences() {
            Collection<FixedDayTypeOccurrence> occurrences = new ArrayList<>();
            ZonedDateTime start = this.start;
            do {
                occurrences.add(new FixedDayTypeOccurrence(start, this.period.dayType(start.getDayOfWeek())));
                start = start.plusDays(1);
            } while (start.isBefore(this.end));
            return occurrences;
        }

    }

    private static class ZonedPeriod {
        private final Map<DayOfWeek, DayType> zonedDayTypeMap;

        private ZonedPeriod(DayType sunday, DayType monday, DayType tuesday, DayType wednesday, DayType thursday, DayType friday, DayType saturday) {
            this.zonedDayTypeMap = new HashMap<>();
            this.zonedDayTypeMap.put(DayOfWeek.SUNDAY, sunday);
            this.zonedDayTypeMap.put(DayOfWeek.SUNDAY, sunday);
            this.zonedDayTypeMap.put(DayOfWeek.SUNDAY, sunday);
            this.zonedDayTypeMap.put(DayOfWeek.SUNDAY, sunday);
            this.zonedDayTypeMap.put(DayOfWeek.SUNDAY, sunday);
            this.zonedDayTypeMap.put(DayOfWeek.SUNDAY, sunday);
            this.zonedDayTypeMap.put(DayOfWeek.SUNDAY, sunday);
            this.zonedDayTypeMap.put(DayOfWeek.MONDAY, monday);
            this.zonedDayTypeMap.put(DayOfWeek.TUESDAY, tuesday);
            this.zonedDayTypeMap.put(DayOfWeek.WEDNESDAY, wednesday);
            this.zonedDayTypeMap.put(DayOfWeek.THURSDAY, thursday);
            this.zonedDayTypeMap.put(DayOfWeek.FRIDAY, friday);
            this.zonedDayTypeMap.put(DayOfWeek.SATURDAY, saturday);
        }

        DayType dayType(DayOfWeek dayOfWeek) {
            return this.zonedDayTypeMap.get(dayOfWeek);
        };
    }

}