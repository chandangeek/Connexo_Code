/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import java.time.Instant;
import java.time.ZoneId;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link IntervalLength#addTo(Instant, ZoneId)} method.
 */
public class IntervalLengthAdditionTest {

    @Test
    public void testOneMinute() {
        // Business method
        Instant instant = IntervalLength.MINUTE1.addTo(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(960L));
    }

    @Test
    public void testTwoMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE2.addTo(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(1020L));
    }

    @Test
    public void testThreeMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE3.addTo(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(1080L));
    }

    @Test
    public void testFourMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE4.addTo(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(1140L));
    }

    @Test
    public void testFiveMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE5.addTo(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(1200L));
    }

    @Test
    public void testSixMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE6.addTo(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(1260L));
    }

    @Test
    public void testTenMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE10.addTo(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(1500L));
    }

    @Test
    public void testTwelveMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE12.addTo(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(1620L));
    }

    @Test
    public void testFifteenMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE15.addTo(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(87300L));
    }

    @Test
    public void testThirtyMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE30.addTo(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(88200L));
    }

    @Test
    public void testOneHour() {
        // Business method
        Instant instant = IntervalLength.HOUR1.addTo(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(86400L + 3600L));
    }

    @Test
    public void testTwoHours() {
        // Business method
        Instant instant = IntervalLength.HOUR2.addTo(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(86400L + 7200L));
    }

    @Test
    public void testThreeHours() {
        // Business method
        Instant instant = IntervalLength.HOUR3.addTo(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(86400L + 10800L));
    }

    @Test
    public void testFourHours() {
        // Business method
        Instant instant = IntervalLength.HOUR4.addTo(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(86400L + 14400L));
    }

    @Test
    public void testSixHours() {
        // Business method
        Instant instant = IntervalLength.HOUR6.addTo(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(86400L + 21600L));
    }

    @Test
    public void testTwelveHours() {
        // Business method
        Instant instant = IntervalLength.HOUR12.addTo(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(129600L));
    }

    @Test
    public void testOneDay() {
        // Business method
        Instant instant = IntervalLength.DAY1.addTo(Instant.ofEpochSecond(172800L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(259200L));
    }

    @Test
    public void testOneDayOnLastDayOfMonth() {
        // Business method
        Instant instant = IntervalLength.DAY1.addTo(Instant.ofEpochMilli(1472594400000L), testZoneId());  // In Europe/Brussels: 2016-08-31 00:00:00

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochMilli(1472680800000L));  // In Europe/Brussels: 2016-09-01 00:00:00
    }

    @Test
    public void testOneWeek() {
        // Business method
        Instant instant = IntervalLength.WEEK1.addTo(Instant.ofEpochMilli(1459807200000L), testZoneId());  // In Europe/Brussels: 2016-04-05 00:00:00

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochMilli(1460412000000L));  // In Europe/Brussels: 2016-04-12 00:00:00
    }

    @Test
    public void testOneWeekInLastWeekOfYear() {
        // Business method
        Instant instant = IntervalLength.WEEK1.addTo(Instant.ofEpochMilli(1451343600000L), testZoneId());  // In Europe/Brussels: 2015-12-29 00:00:00

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochMilli(1451948400000L));  // In Europe/Brussels: 2016-01-05 00:00:00
    }

    @Test
    public void testOneMonth() {
        // Business method
        Instant instant = IntervalLength.MONTH1.addTo(Instant.ofEpochMilli(1459807200000L), testZoneId());  // In Europe/Brussels: 2016-04-05 00:00:00

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochMilli(1462399200000L));  // In Europe/Brussels: 2016-05-05 00:00:00
    }

    @Test
    public void testOneMonthFromDecember() {
        // Business method
        Instant instant = IntervalLength.MONTH1.addTo(Instant.ofEpochMilli(1450134000000L), testZoneId());  // In Europe/Brussels: 2015-12-15 00:00:00

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochMilli(1452812400000L));  // In Europe/Brussels: 2016-01-15 00:00:00
    }

    @Test
    public void testOneMonthOnLastDayOfYear() {
        // Business method
        Instant instant = IntervalLength.MONTH1.addTo(Instant.ofEpochMilli(1451516400000L), testZoneId());  // In Europe/Brussels: 2015-12-31 00:00:00

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochMilli(1454194800000L));  // In Europe/Brussels: 2016-01-31 00:00:00
    }

    @Test
    public void testOneYear() {
        // Business method
        Instant instant = IntervalLength.YEAR1.addTo(Instant.ofEpochMilli(1459807200000L), testZoneId());  // In Europe/Brussels: 2016-04-05 00:00:00

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochMilli(1491343200000L));  // In Europe/Brussels: 2017-04-05 00:00:00
    }

    protected ZoneId testZoneId() {
        return ZoneId.of("Europe/Brussels");
    }

}