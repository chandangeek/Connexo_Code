/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import com.google.common.collect.Range;

import java.time.Instant;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RangeComparatorFactoryTest {

    private static final Instant T1 = Instant.ofEpochMilli(1473329220000L);
    private static final Instant T2 = Instant.ofEpochMilli(1476329720000L);
    private static final Instant T3 = Instant.ofEpochMilli(1576329720000L);
    private static final Instant T4 = Instant.ofEpochMilli(1696329720000L);

    @Test
    public void testDefaultComparatorNullStartFirst() {
        assertThat(RangeComparatorFactory.INSTANT_DEFAULT.compare(Range.atMost(T3), Range.atMost(T1))).isEqualTo(1);
    }

    @Test
    public void testDefaultComparatorNullStartSecond() {
        assertThat(RangeComparatorFactory.INSTANT_DEFAULT.compare(Range.atMost(T1), Range.atMost(T4))).isEqualTo(-1);
    }

    @Test
    public void testDefaultComparatorNullStartEqual() {
        assertThat(RangeComparatorFactory.INSTANT_DEFAULT.compare(Range.atMost(T3), Range.atMost(T3))).isEqualTo(0);
    }

    @Test
    public void testDefaultComparatorNullEndFirst() {
        assertThat(RangeComparatorFactory.INSTANT_DEFAULT.compare(Range.atLeast(T3), Range.atLeast(T1))).isEqualTo(1);
    }

    @Test
    public void testDefaultComparatorNullEndSecond() {
        assertThat(RangeComparatorFactory.INSTANT_DEFAULT.compare(Range.atLeast(T1), Range.atLeast(T4))).isEqualTo(-1);
    }

    @Test
    public void testDefaultComparatorNullEndEqual() {
        assertThat(RangeComparatorFactory.INSTANT_DEFAULT.compare(Range.atLeast(T1), Range.atLeast(T1))).isEqualTo(0);
    }

    @Test
    public void testDefaultComparatorSimpleRanges() {
        assertThat(RangeComparatorFactory.INSTANT_DEFAULT.compare(Range.openClosed(T1, T2), Range.openClosed(T3, T4))).isEqualTo(-1);
    }

    @Test
    public void testDefaultComparatorSimpleCrossedRanges() {
        assertThat(RangeComparatorFactory.INSTANT_DEFAULT.compare(Range.openClosed(T1, T3), Range.openClosed(T2, T4))).isEqualTo(-1);
    }

    @Test
    public void testDefaultComparatorClosedOpenToOpenClose() {
        assertThat(RangeComparatorFactory.INSTANT_DEFAULT.compare(Range.closedOpen(T1, T2), Range.openClosed(T1, T2))).isEqualTo(0);
    }
}
