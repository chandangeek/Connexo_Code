/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Currying.perform;
import static com.elster.jupiter.util.streams.Currying.test;
import static com.elster.jupiter.util.streams.Functions.map;

public class UsedCalendarsImpl implements UsagePoint.UsedCalendars {

    private final DataModel dataModel;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final UsagePointImpl usagePoint;

    public UsedCalendarsImpl(DataModel dataModel, Clock clock, Thesaurus thesaurus, UsagePointImpl usagePoint) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.usagePoint = usagePoint;
    }

    UsagePoint getUsagePoint() {
        return usagePoint;
    }

    @Override
    public List<UsagePoint.CalendarUsage> getCalendars(Category category) {
        return this.usagePoint.getCalendarUsages()
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
    public UsagePoint.CalendarUsage addCalendar(Calendar calendar, Instant startAt) {
        if (startAt.isBefore(this.clock.instant())) {
            throw new CannotStartCalendarBeforeNow();
        }
        return this.doAddCalendar(startAt, calendar);
    }

    private UsagePoint.CalendarUsage doAddCalendar(Instant startAt, Calendar calendar) {
        if (this.usagePoint.getCalendarUsages().stream()
                .filter(test(this::hasCategory).with(calendar.getCategory()))
                .anyMatch(calendarOnUsagePoint -> calendarOnUsagePoint.getRange().lowerEndpoint().isAfter(startAt))) {
            throw new CannotStartCalendarPriorToLatest();
        }
        this.usagePoint.getCalendarUsages().stream()
                .filter(test(this::hasCategory).with(calendar.getCategory()))
                .max(Comparator.comparing(map(UsagePoint.CalendarUsage::getRange).andThen(Range::lowerEndpoint)))
                .ifPresent(latest -> latest.end(startAt));
        CalendarUsageImpl calendarOnUsagePoint = CalendarUsageImpl.create(this.dataModel, startAt, this.usagePoint, calendar);
        Save.CREATE.validate(this.dataModel, calendarOnUsagePoint);
        calendarOnUsagePoint.save();
        return calendarOnUsagePoint;
    }

    @Override
    public void removeCalendar(Calendar calendar) {
        this.removeCalendar(calendar, this.clock.instant());
    }

    @Override
    public void removeCalendar(Calendar calendar, Instant removeAt) {
        this.validateRemoval(removeAt, calendar);
        this.calendarUsagesActiveOn(removeAt)
                .filter(test(this::hasCategory).with(calendar.getCategory()))
                .forEach(perform(ServerCalendarUsage::end).with(removeAt));
    }

    /**
     * Validates the removal of the specified {@link Calendar} at the specified point in time.
     * Ending the usage of a Calendar potentially creates wholes in the Calendar timeline
     * which is not allowed if the metrology configuration requires
     * {@link com.elster.jupiter.calendar.OutOfTheBoxCategory#TOU time of use} events.
     *
     * @param removeAt The point in time
     * @param calendar The Calendar
     */
    private void validateRemoval(Instant removeAt, Calendar calendar) {
        this.effectiveMetrologyConfigurationsAffectedByRemoval(removeAt, calendar)
                .forEach(each -> this.validateRemoval(each.getMetrologyConfiguration()));
    }

    private List<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfigurationsAffectedByRemoval(Instant removeAt, Calendar calendar) {
        Category category = calendar.getCategory();
        // Is there another Calendar of the same category in use in the future
        Optional<ServerCalendarUsage> nextCategoryUsage =
                this.usagePoint.getCalendarUsages()
                        .stream()
                        .filter(test(this::hasCategory).with(category))
                        .filter(test(ServerCalendarUsage::startsOnOrAfter).with(removeAt))
                        .sorted(Comparator.comparing(map(UsagePoint.CalendarUsage::getRange).andThen(Range::lowerEndpoint)))
                        .findFirst();
        if (nextCategoryUsage.isPresent()) {
            return this.getUsagePoint().getEffectiveMetrologyConfigurations(Range.closedOpen(removeAt, nextCategoryUsage.get().getRange().lowerEndpoint()));
        } else {
            return  this.getUsagePoint().getEffectiveMetrologyConfigurations(Range.atLeast(removeAt));
        }
    }

    private void validateRemoval(UsagePointMetrologyConfiguration affected) {
        Set<Long> requestedEventCodes =
                mandatoryReadingTypes(affected)
                        .map(ReadingType::getTou)
                        .map(Long::new)
                        .collect(Collectors.toSet());
        requestedEventCodes.remove(0L);  // zero is used when time of use in not active on the ReadingType
        if (!requestedEventCodes.isEmpty()) {
            throw new UnsatisfiedTimeOfUseBucketsException(this.thesaurus, requestedEventCodes);
        }
    }

    private Stream<ReadingType> mandatoryReadingTypes(UsagePointMetrologyConfiguration configuration) {
        return mandatoryContracts(configuration)
                .map(MetrologyContract::getDeliverables)
                .flatMap(Collection::stream)
                .map(ReadingTypeDeliverable::getReadingType);
    }

    private Stream<MetrologyContract> mandatoryContracts(UsagePointMetrologyConfiguration configuration) {
        return configuration
                .getContracts()
                .stream()
                .filter(MetrologyContract::isMandatory);
    }

    @Override
    public List<Calendar> getCalendars(Instant instant) {
        return this.calendarUsagesActiveOn(instant)
                    .map(UsagePoint.CalendarUsage::getCalendar)
                    .collect(Collectors.toList());
    }

    private Stream<ServerCalendarUsage> calendarUsagesActiveOn(Instant instant) {
        return this.usagePoint
                .getCalendarUsages()
                .stream()
                .filter(test(this::isActiveOn).with(instant));
    }

    private boolean isActiveOn(UsagePoint.CalendarUsage calendarUsage, Instant instant) {
        return calendarUsage.getRange().contains(instant);
    }

    @Override
    public Optional<Calendar> getCalendar(Instant instant, Category category) {
        return calendarUsagesActiveOn(instant)
                    .map(UsagePoint.CalendarUsage::getCalendar)
                    .filter(calendar -> calendar.getCategory().equals(category))
                    .findAny();
    }

    @Override
    public Map<Category, List<UsagePoint.CalendarUsage>> getCalendars() {
        return this.usagePoint.getCalendarUsages()
                .stream()
                .collect(Collectors.groupingBy(map(UsagePoint.CalendarUsage::getCalendar).andThen(Calendar::getCategory)));
    }

}