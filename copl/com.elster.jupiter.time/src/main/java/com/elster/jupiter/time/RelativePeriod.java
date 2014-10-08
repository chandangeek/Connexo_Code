package com.elster.jupiter.time;

import com.google.common.collect.Range;

import java.time.ZonedDateTime;

public interface RelativePeriod extends Entity {
    String getName();
    RelativeDate getRelativeDateFrom();
    RelativeDate getRelativeDateTo();
    Range<ZonedDateTime> getInterval(ZonedDateTime referenceDate);
}
