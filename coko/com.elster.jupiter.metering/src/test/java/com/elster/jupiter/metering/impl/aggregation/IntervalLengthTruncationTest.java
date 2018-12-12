/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.Locale;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link IntervalLength#truncate(Instant, ZoneId)} method.
 */
public class IntervalLengthTruncationTest {

    private static final long TWELVE_MINUTES_AS_SECONDS = 720L;
    private static final long FIFTEEN_MINUTES_AS_SECONDS = 900L;
    private static final long SIXTEEN_MINUTES_AS_SECONDS = 960L;
    private static final long EIGHTTEEN_MINUTES_AS_SECONDS = 1080L;
    private static final long TWENTY_MINUTES_AS_SECONDS = 1200L;
    private static final long TWENTY_FOUR_MINUTES_AS_SECONDS = 1440;
    private static final long THIRTY_MINUTES_AS_SECONDS = 1800L;
    private static final long FOURTY_MINUTES_AS_SECONDS = 2400L;
    private static final long SIXTY_MINUTES_AS_SECONDS = 3600L;
    private static final long DAY_AS_SECONDS = 86400L;
    private static final long WINTER_TO_SUMMER_MILLIS = 1459040400000L; // Zone Europe/Brussels: 2016-03-27 02:00:00
    private static final long SUMMER_TO_WINTER_MILLIS = 1445734800000L; // Zone Europe/Brussels: 2015-10-25 03:00:00

    @Test
    public void truncateMinuteWithoutSmallerDetails() {
        Instant testInstance = Instant.ofEpochSecond(FIFTEEN_MINUTES_AS_SECONDS);  // 15 minutes

        // Business method
        Instant truncated = IntervalLength.MINUTE1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateMinute_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateMinute_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459040340000L));
    }

    @Test
    public void truncateMinute_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459040400000L));
    }

    @Test
    public void truncateMinute_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateMinute_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445734740000L));
    }

    @Test
    public void truncateMinute_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445734800000L));
    }

    @Test
    public void truncateMinuteWithMillis() {
        Instant testInstance = Instant.ofEpochSecond(FIFTEEN_MINUTES_AS_SECONDS).plusMillis(3000L);  // 15 minutes and 3000 millis

        // Business method
        Instant truncated = IntervalLength.MINUTE1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated.toEpochMilli()).isEqualTo(900_000L);
    }

    @Test
    public void truncateExactMultipleOfTwoMinutes() {
        Instant testInstance = Instant.ofEpochSecond(SIXTEEN_MINUTES_AS_SECONDS);  // 16 minutes

        // Business method
        Instant truncated = IntervalLength.MINUTE2.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateTwoMinutes() {
        Instant testInstance = Instant.ofEpochSecond(FIFTEEN_MINUTES_AS_SECONDS).plusMillis(3000L);  // 15 minutes and 3000 millis

        // Business method
        Instant truncated = IntervalLength.MINUTE2.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated.getEpochSecond()).isEqualTo(840L); // 14 minutes
    }

    @Test
    public void truncateTwoMinutes_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE2.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateTwoMinutes_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE2.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459040280000L));
    }

    @Test
    public void truncateTwoMinutes_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE2.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS));
    }

    @Test
    public void truncateTwoMinutes_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE2.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateTwoMinutes_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE2.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445734680000L));
    }

    @Test
    public void truncateTwoMinutes_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE2.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS));
    }

    @Test
    public void truncateExactMultipleOfThreeMinutes() {
        Instant testInstance = Instant.ofEpochSecond(FIFTEEN_MINUTES_AS_SECONDS);  // 15 minutes

        // Business method
        Instant truncated = IntervalLength.MINUTE3.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateThreeMinutes_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE3.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateThreeMinutes_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE3.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459040220000L));
    }

    @Test
    public void truncateThreeMinutes_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE3.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS));
    }

    @Test
    public void truncateThreeMinutes_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE3.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateThreeMinutes_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE3.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445734620000L));
    }

    @Test
    public void truncateThreeMinutes_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE3.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS));
    }

    @Test
    public void truncateThreeMinutes() {
        Instant testInstance = Instant.ofEpochSecond(960).plusMillis(3000L);  // 16 minutes and 3000 millis

        // Business method
        Instant truncated = IntervalLength.MINUTE3.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated.getEpochSecond()).isEqualTo(900L); // 15 minutes
    }

    @Test
    public void truncateExactMultipleOfFourMinutes() {
        Instant testInstance = Instant.ofEpochSecond(SIXTEEN_MINUTES_AS_SECONDS);  // 16 minutes

        // Business method
        Instant truncated = IntervalLength.MINUTE4.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateFourMinutes_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE4.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateFourMinutes_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE4.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459040160000L));
    }

    @Test
    public void truncateFourMinutes_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE4.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS));
    }

    @Test
    public void truncateFourMinutes_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE4.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateFourMinutes_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE4.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445734560000L));
    }

    @Test
    public void truncateFourMinutes_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE4.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS));
    }

    @Test
    public void truncateFourMinutes() {
        Instant testInstance = Instant.ofEpochSecond(960).minusMillis(1L);  // 16 minutes minus 1 milli

        // Business method
        Instant truncated = IntervalLength.MINUTE4.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated.getEpochSecond()).isEqualTo(TWELVE_MINUTES_AS_SECONDS);
    }

    @Test
    public void truncateExactMultipleOfFiveMinutes() {
        Instant testInstance = Instant.ofEpochSecond(FIFTEEN_MINUTES_AS_SECONDS);  // 15 minutes

        // Business method
        Instant truncated = IntervalLength.MINUTE5.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateFiveMinutes_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE5.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateFiveMinutes_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE5.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459040100000L));
    }

    @Test
    public void truncateFiveMinutes_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE5.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS));
    }

    @Test
    public void truncateFiveMinutes_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE5.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateFiveMinutes_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE5.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445734500000L));
    }

    @Test
    public void truncateFiveMinutes_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE5.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS));
    }

    @Test
    public void truncateFiveMinutes() {
        Instant testInstance = Instant.ofEpochSecond(FIFTEEN_MINUTES_AS_SECONDS).minusMillis(1L);  // 15 minutes minus 1 milli

        // Business method
        Instant truncated = IntervalLength.MINUTE5.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated.getEpochSecond()).isEqualTo(600L); // 10 minutes
    }

    @Test
    public void truncateExactMultipleOfSixMinutes() {
        Instant testInstance = Instant.ofEpochSecond(EIGHTTEEN_MINUTES_AS_SECONDS);

        // Business method
        Instant truncated = IntervalLength.MINUTE6.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateSixMinutes_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE6.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateSixMinutes_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE6.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459040040000L));
    }

    @Test
    public void truncateSixMinutes_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE6.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS));
    }

    @Test
    public void truncateSixMinutes_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE6.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateSixMinutes_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE6.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445734440000L));
    }

    @Test
    public void truncateSixMinutes_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE6.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS));
    }

    @Test
    public void truncateSixMinutes() {
        Instant testInstance = Instant.ofEpochSecond(EIGHTTEEN_MINUTES_AS_SECONDS).minusMillis(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE6.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated.getEpochSecond()).isEqualTo(TWELVE_MINUTES_AS_SECONDS);
    }

    @Test
    public void truncateExactMultipleOfTenMinutes() {
        Instant testInstance = Instant.ofEpochSecond(TWENTY_MINUTES_AS_SECONDS);

        // Business method
        Instant truncated = IntervalLength.MINUTE10.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateTenMinutes_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE10.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateTenMinutes_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE10.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459039800000L));
    }

    @Test
    public void truncateTenMinutes_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE10.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS));
    }

    @Test
    public void truncateTenMinutes_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE10.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateTenMinutes_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE10.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445734200000L));
    }

    @Test
    public void truncateTenMinutes_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE10.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS));
    }

    @Test
    public void truncateTenMinutes() {
        Instant testInstance = Instant.ofEpochSecond(FIFTEEN_MINUTES_AS_SECONDS).minusMillis(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE10.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated.getEpochSecond()).isEqualTo(600L); // 10 minutes
    }

    @Test
    public void truncateExactMultipleOfTwelveMinutes() {
        Instant testInstance = Instant.ofEpochSecond(TWENTY_FOUR_MINUTES_AS_SECONDS);

        // Business method
        Instant truncated = IntervalLength.MINUTE12.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateTwelveMinutes_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE12.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateTwelveMinutes_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE12.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459039680000L));
    }

    @Test
    public void truncateTwelveMinutes_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE12.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS));
    }

    @Test
    public void truncateTwelveMinutes_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE12.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateTwelveMinutes_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE12.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445734080000L));
    }

    @Test
    public void truncateTwelveMinutes_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE12.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS));
    }

    @Test
    public void truncateTwelveMinutes() {
        Instant testInstance = Instant.ofEpochSecond(TWENTY_FOUR_MINUTES_AS_SECONDS).minusMillis(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE12.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated.getEpochSecond()).isEqualTo(TWELVE_MINUTES_AS_SECONDS);
    }

    @Test
    public void truncateExactMultipleOfFifteenMinutes() {
        Instant testInstance = Instant.ofEpochSecond(THIRTY_MINUTES_AS_SECONDS);

        // Business method
        Instant truncated = IntervalLength.MINUTE15.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateFifTeenMinutes_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE15.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateFifteenMinutes_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE15.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459039500000L));
    }

    @Test
    public void truncateFifteenMinutes_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE15.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS));
    }

    @Test
    public void truncateFifteenMinutes_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE15.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateFifteenMinutes_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE15.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445733900000L));
    }

    @Test
    public void truncateFifteenMinutes_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE15.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS));
    }

    @Test
    public void truncateFifteenMinutes() {
        Instant testInstance = Instant.ofEpochSecond(THIRTY_MINUTES_AS_SECONDS).minusMillis(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE15.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated.getEpochSecond()).isEqualTo(900); // 15 minutes
    }

    @Test
    public void truncateExactMultipleOfTwentyMinutes() {
        Instant testInstance = Instant.ofEpochSecond(TWENTY_MINUTES_AS_SECONDS);

        // Business method
        Instant truncated = IntervalLength.MINUTE20.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateTwentyMinutes_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE20.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateTwentyMinutes_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE20.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459039200000L));
    }

    @Test
    public void truncateTwentyMinutes_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE20.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS));
    }

    @Test
    public void truncateTwentyMinutes_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE20.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateTwentyMinutes_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE20.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445733600000L));
    }

    @Test
    public void truncateTwentyMinutes_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE20.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS));
    }

    @Test
    public void truncatenTwentyMinutes() {
        Instant testInstance = Instant.ofEpochSecond(FOURTY_MINUTES_AS_SECONDS).minusMillis(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE20.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated.getEpochSecond()).isEqualTo(TWENTY_MINUTES_AS_SECONDS);
    }

    @Test
    public void truncateExactMultipleOfThirtyMinutes() {
        Instant testInstance = Instant.ofEpochSecond(THIRTY_MINUTES_AS_SECONDS);

        // Business method
        Instant truncated = IntervalLength.MINUTE30.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateThirtyMinutes_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE30.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateThirtyMinutes_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE30.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459038600000L));
    }

    @Test
    public void truncateThirtyMinutes_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE30.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS));
    }

    @Test
    public void truncateThirtyMinutes_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MINUTE30.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateThirtyMinutes_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE30.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445733000000L));
    }

    @Test
    public void truncateThirtyMinutes_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE30.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS));
    }

    @Test
    public void truncateThirtyMinutes() {
        Instant testInstance = Instant.ofEpochSecond(THIRTY_MINUTES_AS_SECONDS).minusMillis(1L);

        // Business method
        Instant truncated = IntervalLength.MINUTE30.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated.getEpochSecond()).isZero();
    }

    @Test
    public void truncateExactMultipleOfHour() {
        Instant testInstance = Instant.ofEpochSecond(SIXTY_MINUTES_AS_SECONDS);

        // Business method
        Instant truncated = IntervalLength.HOUR1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateHour_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.HOUR1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateHour_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.HOUR1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459036800000L));
    }

    @Test
    public void truncateHour_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.HOUR1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS));
    }

    @Test
    public void truncateHour_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.HOUR1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateHour_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.HOUR1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445731200000L));
    }

    @Test
    public void truncateHour_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.HOUR1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS));
    }

    @Test
    public void truncateHour() {
        Instant testInstance = Instant.ofEpochSecond(SIXTEEN_MINUTES_AS_SECONDS).minusMillis(1L);

        // Business method
        Instant truncated = IntervalLength.HOUR1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated.getEpochSecond()).isZero();
    }

    @Test
    public void truncateExactMultipleOfDay() {
        Instant testInstance = Instant.ofEpochSecond(DAY_AS_SECONDS);

        // Business method
        Instant truncated = IntervalLength.DAY1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated.getEpochSecond()).isEqualTo(82800L);    // In Europe/Brussels: 1970-001-02 00:00:00
    }

    @Test
    public void truncateDay_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.DAY1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459033200000L));
    }

    @Test
    public void truncateDay_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.DAY1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459033200000L));
    }

    @Test
    public void truncateDay_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.DAY1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459033200000L));
    }

    @Test
    public void truncateDay_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.DAY1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445724000000L));
    }

    @Test
    public void truncateDay_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.DAY1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445724000000L));
    }

    @Test
    public void truncateDay_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.DAY1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445724000000L));
    }

    @Test
    public void truncateDay() {
        Instant testInstance = Instant.ofEpochSecond(DAY_AS_SECONDS).minusMillis(1L);   // In Europe/Brussels: 1970-01-02 00:59:59:999

        // Business method
        Instant truncated = IntervalLength.DAY1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated.getEpochSecond()).isEqualTo(82800L);    // In Europe/Brussels: 1970-001-02 00:00:00
    }

    @Test
    public void truncateExactMultipleOfWeek() {
        Instant testInstance = Instant.ofEpochMilli(1459720800000L);    // In Europe/Brussels: 2016-04-04 00:00:00, start of week 14

        // Business method
        Instant truncated = IntervalLength.WEEK1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateWeek_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.WEEK1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1458514800000L));
    }

    @Test
    public void truncateWeek_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.WEEK1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1458514800000L));
    }

    @Test
    public void truncateWeek_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.WEEK1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1458514800000L));
    }

    @Test
    public void truncateWeek_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.WEEK1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445205600000L));
    }

    @Test
    public void truncateWeek_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.WEEK1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445205600000L));
    }

    @Test
    public void truncateWeek_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.WEEK1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1445205600000L));
    }

    @Test
    public void truncateWeek() {
        Instant testInstance = Instant.ofEpochMilli(1459530923000L);   // In Europe/Brussels: 2016-04-01 19:15:23

        // Business method
        Instant truncated = IntervalLength.WEEK1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459116000000L));
    }

    @Test
    public void truncateExactMultipleOfMonth() {
        Instant testInstance = Instant.ofEpochMilli(1454281200000L);    // In Europe/Brussels: 2016-04-01 00:00:00

        // Business method
        Instant truncated = IntervalLength.MONTH1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(testInstance);
    }

    @Test
    public void truncateMonth_OnWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MONTH1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1456786800000L));
    }

    @Test
    public void truncateMonth_OneSecondBeforeWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MONTH1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1456786800000L));
    }

    @Test
    public void truncateMonth_OneSecondAfterWinterToSummer() {
        Instant testInstance = Instant.ofEpochMilli(WINTER_TO_SUMMER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MONTH1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1456786800000L));
    }

    @Test
    public void truncateMonth_OnSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS);

        // Business method
        Instant truncated = IntervalLength.MONTH1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1443650400000L));
    }

    @Test
    public void truncateMonth_OneSecondBeforeSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).minusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MONTH1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1443650400000L));
    }

    @Test
    public void truncateMonth_OneSecondAfterSummerToWinter() {
        Instant testInstance = Instant.ofEpochMilli(SUMMER_TO_WINTER_MILLIS).plusSeconds(1L);

        // Business method
        Instant truncated = IntervalLength.MONTH1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1443650400000L));
    }

    @Test
    public void truncateMonth() {
        Instant testInstance = Instant.ofEpochMilli(1459773408000L);   // In Europe/Brussels: 2016-04-04 14:36:48

        // Business method
        Instant truncated = IntervalLength.MONTH1.truncate(testInstance, testZoneId());

        // Asserts
        assertThat(truncated).isEqualTo(Instant.ofEpochMilli(1459461600000L));    // In Europe/Brussels: 2016-04-01 00:00:00
    }

    private ZoneId testZoneId() {
        return ZoneId.of("Europe/Brussels");
    }

}