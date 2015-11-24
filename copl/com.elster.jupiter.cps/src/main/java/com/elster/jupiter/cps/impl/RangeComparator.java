package com.elster.jupiter.cps.impl;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Comparator;

public class RangeComparator implements Comparator<Range<Instant>>{

    @Override
    public int compare(Range<Instant> firstRange, Range<Instant> secondRange) {
        long firstLowerEndpoint = firstRange.hasLowerBound() ? firstRange.lowerEndpoint().toEpochMilli() : 0;
        long firstUpperEndpoint = firstRange.hasUpperBound() ? firstRange.upperEndpoint().toEpochMilli() : Long.MAX_VALUE;
        long secondLowerEndpoint = secondRange.hasLowerBound() ? secondRange.lowerEndpoint().toEpochMilli() : 0;
        long secondUpperEndpoint = secondRange.hasUpperBound() ? secondRange.upperEndpoint().toEpochMilli() : Long.MAX_VALUE;

        return Long.compare(firstLowerEndpoint, secondLowerEndpoint) != 0 ? Long.compare(firstLowerEndpoint, secondLowerEndpoint) : Long.compare(firstUpperEndpoint, secondUpperEndpoint);

    }
}
