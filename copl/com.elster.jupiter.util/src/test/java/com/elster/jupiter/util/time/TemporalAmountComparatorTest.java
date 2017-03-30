/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import java.time.Duration;
import java.time.Period;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link TemporalAmountComparator} component.
 */
public class TemporalAmountComparatorTest {

    @Test
    public void compareDurations() {
        TemporalAmountComparator comparator = new TemporalAmountComparator();

        // Business method
        int result = comparator.compare(Duration.ofMillis(100), Duration.ofMillis(1000));

        // Asserts
        assertThat(result).isEqualTo(-1);
    }

    @Test
    public void compareDurationAndPeriod() {
        TemporalAmountComparator comparator = new TemporalAmountComparator();

        // Business method
        int result = comparator.compare(Duration.ofMillis(100), Period.ofDays(1));

        // Asserts
        assertThat(result).isEqualTo(-1);
    }

    @Test
    public void comparePeriodAndDuration() {
        TemporalAmountComparator comparator = new TemporalAmountComparator();

        // Business method
        int result = comparator.compare(Period.ofDays(1), Duration.ofMillis(100));

        // Asserts
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void compareEqualPeriods() {
        TemporalAmountComparator comparator = new TemporalAmountComparator();

        // Business method
        int result = comparator.compare(Period.ofDays(1), Period.ofDays(1));

        // Asserts
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void compareBigToSmallPeriod() {
        TemporalAmountComparator comparator = new TemporalAmountComparator();

        // Business method
        int result = comparator.compare(Period.ofDays(2), Period.ofDays(1));

        // Asserts
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void compareSmallToBigPeriod() {
        TemporalAmountComparator comparator = new TemporalAmountComparator();

        // Business method
        int result = comparator.compare(Period.ofDays(1), Period.ofDays(2));

        // Asserts
        assertThat(result).isEqualTo(-1);
    }

}