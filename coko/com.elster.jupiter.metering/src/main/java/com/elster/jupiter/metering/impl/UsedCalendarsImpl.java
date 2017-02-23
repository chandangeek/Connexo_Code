/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Currying.test;
import static com.elster.jupiter.util.streams.Functions.map;

public class UsedCalendarsImpl implements UsagePoint.UsedCalendars {

    private final DataModel dataModel;
    private final Clock clock;
    private final UsagePointImpl usagePoint;

    public UsedCalendarsImpl(DataModel dataModel, Clock clock, UsagePointImpl usagePoint) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.usagePoint = usagePoint;
    }

    UsagePoint getUsagePoint() {
        return usagePoint;
    }

    private List<UsagePoint.CalendarUsage> getCalendarUsages() {
        return new ArrayList<>(this.usagePoint.getCalendarUsages());
    }

    @Override
    public List<UsagePoint.CalendarUsage> getCalendars(Category category) {
        return getCalendarUsages()
                .stream()
                .filter(test(this::hasCategory).with(category))
                .collect(Collectors.toList());
    }

    private boolean hasCategory(UsagePoint.CalendarUsage calendarUsage, Category category) {
        return calendarUsage.getCalendar().getCategory().equals(category);
    }

    @Override
    public UsagePoint.CalendarUsage addCalendar(Calendar calendar) {
        return this.doAddCalendar(this.clock.instant(), calendar);
    }

    @Override
    public UsagePoint.CalendarUsage addCalendar(Instant startAt, Calendar calendar) {
        if (startAt.isBefore(this.clock.instant())) {
            throw new CannotStartCalendarBeforeNow();
        }
        return this.doAddCalendar(startAt, calendar);
    }

    private UsagePoint.CalendarUsage doAddCalendar(Instant startAt, Calendar calendar) {
        if (getCalendarUsages().stream()
                .filter(test(this::hasCategory).with(calendar.getCategory()))
                .anyMatch(calendarOnUsagePoint -> calendarOnUsagePoint.getRange().lowerEndpoint().isAfter(startAt))) {
            throw new CannotStartCalendarPriorToLatest();
        }
        getCalendarUsages().stream()
                .filter(test(this::hasCategory).with(calendar.getCategory()))
                .max(Comparator.comparing(map(UsagePoint.CalendarUsage::getRange).andThen(Range::lowerEndpoint)))
                .ifPresent(latest -> latest.end(startAt));
        CalendarUsageImpl calendarOnUsagePoint = CalendarUsageImpl.create(this.dataModel, startAt, this.usagePoint, calendar);
        Save.CREATE.validate(this.dataModel, calendarOnUsagePoint);
        calendarOnUsagePoint.save();
        return calendarOnUsagePoint;
    }

    @Override
    public List<Calendar> getCalendars(Instant instant) {
        return calendarsActiveOn(instant).collect(Collectors.toList());
    }

    private Stream<Calendar> calendarsActiveOn(Instant instant) {
        return getCalendarUsages()
                .stream()
                .filter(test(this::isActiveOn).with(instant))
                .map(UsagePoint.CalendarUsage::getCalendar);
    }

    private boolean isActiveOn(UsagePoint.CalendarUsage calendarUsage, Instant instant) {
        return calendarUsage.getRange().contains(instant);
    }

    @Override
    public Optional<Calendar> getCalendar(Instant instant, Category category) {
        return calendarsActiveOn(instant)
                .filter(calendar -> calendar.getCategory().equals(category))
                .findAny();
    }

    @Override
    public Map<Category, List<UsagePoint.CalendarUsage>> getCalendars() {
        return getCalendarUsages()
                .stream()
                .collect(Collectors.groupingBy(map(UsagePoint.CalendarUsage::getCalendar).andThen(Calendar::getCategory)));
    }

}