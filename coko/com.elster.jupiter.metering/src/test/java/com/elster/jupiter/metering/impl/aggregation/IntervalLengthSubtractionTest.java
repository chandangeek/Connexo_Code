/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import java.time.Instant;
import java.time.ZoneId;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link IntervalLength#subtractFrom(Instant, ZoneId)} method.
 */
public class IntervalLengthSubtractionTest {

    @Test
    public void testOneMinute() {
        // Business method
        Instant instant = IntervalLength.MINUTE1.subtractFrom(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(840L));
    }

    @Test
    public void testTwoMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE2.subtractFrom(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(780L));
    }

    @Test
    public void testThreeMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE3.subtractFrom(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(720L));
    }

    @Test
    public void testFourMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE4.subtractFrom(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(660L));
    }

    @Test
    public void testFiveMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE5.subtractFrom(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(600L));
    }

    @Test
    public void testSixMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE6.subtractFrom(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(540L));
    }

    @Test
    public void testTenMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE10.subtractFrom(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(300L));
    }

    @Test
    public void testTwelveMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE12.subtractFrom(Instant.ofEpochSecond(900L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(180L));
    }

    @Test
    public void testFifteenMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE15.subtractFrom(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(85500L));
    }

    @Test
    public void testThirtyMinutes() {
        // Business method
        Instant instant = IntervalLength.MINUTE30.subtractFrom(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(84600L));
    }

    @Test
    public void testOneHour() {
        // Business method
        Instant instant = IntervalLength.HOUR1.subtractFrom(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(86400L - 3600L));
    }

    @Test
    public void testTwoHours() {
        // Business method
        Instant instant = IntervalLength.HOUR2.subtractFrom(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(86400L - 7200L));
    }

    @Test
    public void testThreeHours() {
        // Business method
        Instant instant = IntervalLength.HOUR3.subtractFrom(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(86400L - 10800L));
    }

    @Test
    public void testFourHours() {
        // Business method
        Instant instant = IntervalLength.HOUR4.subtractFrom(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(86400L - 14400L));
    }

    @Test
    public void testSixHours() {
        // Business method
        Instant instant = IntervalLength.HOUR6.subtractFrom(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(86400L - 21600L));
    }

    @Test
    public void testTwelveHours() {
        // Business method
        Instant instant = IntervalLength.HOUR12.subtractFrom(Instant.ofEpochSecond(86400L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(43200L));
    }

    @Test
    public void testOneDay() {
        // Business method
        Instant instant = IntervalLength.DAY1.subtractFrom(Instant.ofEpochSecond(172800L), testZoneId());

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochSecond(86400L));
    }

    @Test
    public void testOneDayOnFirstOfMonth() {
        // Business method
        Instant instant = IntervalLength.DAY1.subtractFrom(Instant.ofEpochMilli(1470002400000L), testZoneId());  // In Europe/Brussels: 2016-08-01 00:00:00

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochMilli(1469916000000L));  // In Europe/Brussels: 2016-07-31 00:00:00
    }

    @Test
    public void testOneDayOnFirstOfJanuari() {
        // Business method
        Instant instant = IntervalLength.DAY1.subtractFrom(Instant.ofEpochMilli(1451602800000L), testZoneId());  // In Europe/Brussels: 2016-01-01 00:00:00

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochMilli(1451516400000L));  // In Europe/Brussels: 2015-12-31 00:00:00
    }

    @Test
    public void testOneWeek() {
        // Business method
        Instant instant = IntervalLength.WEEK1.subtractFrom(Instant.ofEpochMilli(1459807200000L), testZoneId());  // In Europe/Brussels: 2016-04-05 00:00:00

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochMilli(1459202400000L));  // In Europe/Brussels: 2016-03-29 00:00:00
    }

    @Test
    public void testOneWeekInFirstWeekOfYear() {
        // Business method
        Instant instant = IntervalLength.WEEK1.subtractFrom(Instant.ofEpochMilli(1452034800000L), testZoneId());  // In Europe/Brussels: 2016-01-06 00:00:00

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochMilli(1451430000000L));  // In Europe/Brussels: 2015-12-30 00:00:00
    }

    @Test
    public void testOneMonth() {
        // Business method
        Instant instant = IntervalLength.MONTH1.subtractFrom(Instant.ofEpochMilli(1459807200000L), testZoneId());  // In Europe/Brussels: 2016-04-05 00:00:00

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochMilli(1457132400000L));  // In Europe/Brussels: 2016-03-05 00:00:00
    }

    @Test
    public void testOneMonthFromJanuari() {
        // Business method
        Instant instant = IntervalLength.MONTH1.subtractFrom(Instant.ofEpochMilli(1451775600000L), testZoneId());  // In Europe/Brussels: 2016-01-00 00:00:00

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochMilli(1449097200000L));  // In Europe/Brussels: 2015-12-03 00:00:00
    }

    @Test
    public void testOneYear() {
        // Business method
        Instant instant = IntervalLength.YEAR1.subtractFrom(Instant.ofEpochMilli(1459807200000L), testZoneId());  // In Europe/Brussels: 2016-04-05 00:00:00

        // Asserts
        assertThat(instant).isEqualTo(Instant.ofEpochMilli(1428184800000L));  // In Europe/Brussels: 2015-04-05 00:00:00
    }

    private ZoneId testZoneId() {
        return ZoneId.of("Europe/Brussels");
    }

}