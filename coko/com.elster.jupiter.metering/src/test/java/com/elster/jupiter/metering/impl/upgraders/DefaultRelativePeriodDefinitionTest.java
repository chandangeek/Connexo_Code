/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.devtools.tests.rules.LocaleNeutral;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.util.time.DayMonthTime;

import com.google.common.collect.Range;

import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.rules.Using.localeOfMalta;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DefaultRelativePeriodDefinition} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultRelativePeriodDefinitionTest {

    @Rule
    public LocaleNeutral timeZoneRule = localeOfMalta();

    @Mock
    private GasDayOptions gasDayOptions;

    @Before
    public void initializeGasDayOptions() {
        // Configure gas day to start at Nov first, 4 am
        when(this.gasDayOptions.getYearStart()).thenReturn(DayMonthTime.from(MonthDay.of(Month.NOVEMBER, 1), LocalTime.of(4, 0)));
    }

    @Test
    public void last7DaysJustBeforeGasDayStart() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 23, 3, 59, 59, 999, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.LAST_7_DAYS, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 16, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 22, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void last7DaysAtGasDayStart() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 23, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.LAST_7_DAYS, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 16, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 23, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void previousMonthInMiddleOfMonth() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 22, 15, 16, 12, 212551252, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.PREVIOUS_MONTH, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 10, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void previousMonthJustAfterStartOfMonth() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 1, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.PREVIOUS_MONTH, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 10, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void previousMonthAtStartOfMonth() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.PREVIOUS_MONTH, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 10, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void previousMonthJustBeforeEndOfMonth() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 12, 1, 3, 59, 59, 999, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.PREVIOUS_MONTH, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 10, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void thisMonthInMiddleOfMonth() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 22, 15, 16, 12, 212551252, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.THIS_MONTH, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 23, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void thisMonthJustAfterStartOfMonth() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 1, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.THIS_MONTH, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 2, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void thisMonthAtStartOfMonth() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.THIS_MONTH, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 2, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void thisMonthJustBeforeEndOfMonth() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 12, 1, 3, 59, 59, 999, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.THIS_MONTH, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 12, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void previousWeekInMiddleOfWeek() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 22, 15, 16, 12, 212551252, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.PREVIOUS_WEEK, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 13, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 20, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void previousWeekJustAfterStartOfWeek() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 20, 4, 0, 0, 1, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.PREVIOUS_WEEK, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 13, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 20, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void previousWeekAtStartOfWeek() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 20, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.PREVIOUS_WEEK, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 13, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 20, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void previousWeekJustBeforeEndOfWeek() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 27, 3, 59, 59, 999, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.PREVIOUS_WEEK, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 13, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 20, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void thisWeekInMiddleOfWeek() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 22, 15, 16, 12, 212551252, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.THIS_WEEK, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 20, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 23, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void thisWeekJustAfterStartOfWeek() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 20, 4, 0, 0, 1, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.THIS_WEEK, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 20, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 21, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void thisWeekAtStartOfWeek() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 27, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.THIS_WEEK, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 27, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 28, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void thisWeekJustBeforeEndOfWeek() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 27, 3, 59, 59, 999, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.THIS_WEEK, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 20, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 27, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void yesterDayInTheMiddleOfTheDay() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 22, 15, 16, 12, 212551252, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.YESTERDAY, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 21, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 22, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void yesterDayAtTheStartTheGasDay() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 22, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.YESTERDAY, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 21, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 22, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void yesterDayJustAfterTheStartTheGasDay() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 22, 4, 0, 0, 1, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.YESTERDAY, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 21, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 22, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void yesterDayJustBeforeTheStartTheGasDay() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 22, 3, 59, 59, 999, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.YESTERDAY, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 20, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 21, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void todayInTheMiddleOfTheDay() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 22, 15, 16, 12, 212551252, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.TODAY, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 22, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 23, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void todayAtTheStartTheGasDay() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 22, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.TODAY, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 22, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 23, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void todayJustAfterTheStartTheGasDay() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 22, 4, 0, 0, 1, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.TODAY, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 22, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 23, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void todayJustBeforeTheStartTheGasDay() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 22, 3, 59, 59, 999, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.TODAY, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 21, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 22, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void thisYearInMiddleOfYear() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 22, 15, 16, 12, 212551252, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.THIS_YEAR, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 23, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void thisYearJustAfterStartOfYear() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 1, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.THIS_YEAR, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 2, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void thisYearAtStartOfYear() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.THIS_YEAR, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 2, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    @Test
    public void thisYearJustBeforeEndOfYear() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2016, 11, 1, 3, 59, 59, 999, TimeZoneNeutral.getMcMurdo());

        // Business method
        Range<ZonedDateTime> interval = this.getClosedZonedInterval(DefaultRelativePeriodDefinition.THIS_YEAR, zonedDateTime);

        // Asserts
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2015, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2016, 11, 1, 4, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
    }

    private Range<ZonedDateTime> getClosedZonedInterval(DefaultRelativePeriodDefinition definition, ZonedDateTime referenceDate) {
        RelativeDate from = definition.fromWith(this.gasDayOptions);
        RelativeDate to = definition.toWith(this.gasDayOptions);
        return Range.closed(from.getRelativeDate(referenceDate), to.getRelativeDate(referenceDate));
    }

}