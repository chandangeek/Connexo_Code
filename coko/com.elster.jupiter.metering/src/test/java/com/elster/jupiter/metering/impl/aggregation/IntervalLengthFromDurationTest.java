/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import java.time.Duration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests the {@link IntervalLength#from(Duration)} factory method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-13 (13:06)
 */
public class IntervalLengthFromDurationTest {

    @Test
    public void oneMinute() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofMinutes(1));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE1);
    }

    @Test
    public void twoMinutes() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofMinutes(2));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE2);
    }

    @Test
    public void threeMinutes() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofMinutes(3));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE3);
    }

    @Test
    public void fourMinutes() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofMinutes(4));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE4);
    }

    @Test
    public void fiveMinutes() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofMinutes(5));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE5);
    }

    @Test
    public void sixMinutes() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofMinutes(6));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE6);
    }

    @Test
    public void tenMinutes() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofMinutes(10));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE10);
    }

    @Test
    public void twelveMinutes() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofMinutes(12));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE12);
    }

    @Test
    public void fifteenMinutes() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofMinutes(15));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE15);
    }

    @Test
    public void twentyMinutes() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofMinutes(20));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE20);
    }

    @Test
    public void halfHourMinutes() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofMinutes(30));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE30);
    }

    @Test
    public void oneHour() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofHours(1));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.HOUR1);
    }

    @Test
    public void twoHours() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofHours(2));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.HOUR2);
    }

    @Test
    public void threeHours() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofHours(3));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.HOUR3);
    }

    @Test
    public void fourHours() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofHours(4));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.HOUR4);
    }

    @Test
    public void sixHours() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofHours(6));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.HOUR6);
    }

    @Test
    public void twelveHours() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofHours(12));

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.HOUR12);
    }

    @Test
    public void subMinute() {
        for (int s = 0; s < 60; s++) {
            try {
                // Business method
                IntervalLength intervalLength = IntervalLength.from(Duration.ofSeconds(s));
                fail("Was expected that conversion from " + s + " seconds would throw " + IllegalArgumentException.class.getSimpleName() + " but got " + intervalLength);
            } catch (IllegalArgumentException e) {
                // Expected
            }
        }

    }

    @Test
    public void subSecond() {
        for (int m = 0; m < 1000; m++) {
            try {
                // Business method
                IntervalLength intervalLength = IntervalLength.from(Duration.ofMillis(m));
                fail("Was expected that conversion from " + m + " millis would throw " + IllegalArgumentException.class.getSimpleName() + " but got " + intervalLength);
            } catch (IllegalArgumentException e) {
                // Expected
            }
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void oneSecond() {
        // Business method
        IntervalLength intervalLength = IntervalLength.from(Duration.ofSeconds(1));

        // Asserts: see expected exception rule
    }

}