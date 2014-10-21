package com.elster.jupiter.time;

import com.google.common.collect.Range;

import java.time.ZonedDateTime;
import java.util.List;

public interface RelativePeriod extends Entity {
    String getName();
    RelativeDate getRelativeDateFrom();
    RelativeDate getRelativeDateTo();
    Range<ZonedDateTime> getInterval(ZonedDateTime referenceDate);

    List<RelativePeriodCategory> getRelativePeriodCategories();
    void addRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory);
    void removeRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory) throws Exception;
}
