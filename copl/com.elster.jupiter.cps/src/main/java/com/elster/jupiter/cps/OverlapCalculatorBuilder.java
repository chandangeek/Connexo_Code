package com.elster.jupiter.cps;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

public interface OverlapCalculatorBuilder {
    List<ValuesRangeConflict> whenCreating(Range<Instant> newRange);

    List<ValuesRangeConflict> whenUpdating(Instant existingRangeStart, Range<Instant> newRange);
}

