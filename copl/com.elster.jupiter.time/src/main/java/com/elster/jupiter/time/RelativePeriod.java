package com.elster.jupiter.time;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

public interface RelativePeriod extends Entity {
    String getName();
    RelativeDate getRelativeDateFrom();
    RelativeDate getRelativeDateTo();
    Range<ZonedDateTime> getClosedZonedInterval(ZonedDateTime referenceDate);
    Range<Instant> getClosedInterval(ZonedDateTime referenceDate);
    Range<ZonedDateTime> getOpenClosedZonedInterval(ZonedDateTime referenceDate);
    Range<Instant> getOpenClosedInterval(ZonedDateTime referenceDate);
    Range<ZonedDateTime> getClosedOpenZonedInterval(ZonedDateTime referenceDate);
    Range<Instant> getClosedOpenInterval(ZonedDateTime referenceDate);
    Range<ZonedDateTime> getOpenZonedInterval(ZonedDateTime referenceDate);
    Range<Instant> getOpenInterval(ZonedDateTime referenceDate);

    List<RelativePeriodCategory> getRelativePeriodCategories();
    void addRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory);
    void removeRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory);
}
