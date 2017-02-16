/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.AggregationLevel;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link IntervalLength} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-09 (11:22)
 */
public class IntervalLengthTest {

    @Test
    public void toTemporalAmountIsNotNull() {
        List<IntervalLength> valuesWithNullTemporalAmount = Stream.of(IntervalLength.values()).filter(each -> each.toTemporalAmount() == null).collect(Collectors.toList());

        if (!valuesWithNullTemporalAmount.isEmpty()) {
            fail(valuesWithNullTemporalAmount.stream().map(IntervalLength::name).collect(Collectors.joining(", ")) + " all produce null as TemporalAmount");
        }
    }

    @Test
    public void oneMinute() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE1);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE1);
    }

    @Test
    public void twoMinutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE2);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE2);
    }

    @Test
    public void threeMinutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE3);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE3);
    }

    @Test
    public void fiveMinutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE5);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE5);
    }

    @Test
    public void tenMinutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE10);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE10);
    }

    @Test
    public void twelveMinutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE12);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE12);
    }

    @Test
    public void fifteenMinutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE15);
    }

    @Test
    public void twentyMinutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE20);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE20);
    }

    @Test
    public void halfHourMinutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE30);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE30);
    }

    @Test
    public void hourly() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.HOUR1);
    }

    @Test
    public void twoHours() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.HOUR2);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.HOUR2);
    }

    @Test
    public void threeHours() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.HOUR3);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.HOUR3);
    }

    @Test
    public void fourHours() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.HOUR4);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.HOUR4);
    }

    @Test
    public void sixHours() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.HOUR6);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.HOUR6);
    }

    @Test
    public void twelveHours() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.HOUR12);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.HOUR12);
    }

    @Test
    public void fixedBlock1Minute() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK1MIN);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE1);
    }

    @Test
    public void fixedBlock5Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK5MIN);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE5);
    }

    @Test
    public void fixedBlock10Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK10MIN);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE10);
    }

    @Test
    public void fixedBlock15Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK15MIN);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE15);
    }

    @Test
    public void fixedBlock20Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK20MIN);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE20);
    }

    @Test
    public void fixedBlock30Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK30MIN);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE30);
    }

    @Test
    public void fixedBlock60Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK60MIN);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.HOUR1);
    }

    @Test
    public void rolling60MinutesFrom30Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING60_30);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE30);
    }

    @Test
    public void rolling60MinutesFrom20Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING60_20);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE20);
    }

    @Test
    public void rolling60MinutesFrom15Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING60_15);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE15);
    }

    @Test
    public void rolling60MinutesFrom12Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING60_12);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE12);
    }

    @Test
    public void rolling60MinutesFrom10Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING60_10);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE10);
    }

    @Test
    public void rolling60MinutesFrom6Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING60_6);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE6);
    }

    @Test
    public void rolling50MinutesFrom5Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING60_5);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE5);
    }

    @Test
    public void rolling40MinutesFrom4Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING60_4);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE4);
    }

    @Test
    public void rolling30MinutesFrom15Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING30_15);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE15);
    }

    @Test
    public void rolling30MinutesFrom10Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING30_10);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE10);
    }

    @Test
    public void rolling30MinutesFrom6Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING30_6);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE6);
    }

    @Test
    public void rolling30MinutesFrom5Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING30_5);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE5);
    }

    @Test
    public void rolling30MinutesFrom3Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING30_3);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE3);
    }

    @Test
    public void rolling30MinutesFrom2Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING30_2);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE2);
    }

    @Test
    public void rolling15MinutesFrom5Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING15_5);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE5);
    }

    @Test
    public void rolling15MinutesFrom3Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING15_3);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE3);
    }

    @Test
    public void rolling15MinutesFrom1Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING15_1);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE1);
    }

    @Test
    public void rolling10MinutesFrom5Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING10_5);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE5);
    }

    @Test
    public void rolling10MinutesFrom2Minutes() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING10_2);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE2);
    }

    @Test
    public void rolling10MinutesFrom1Minute() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING10_1);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE1);
    }

    @Test
    public void rolling5() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.ROLLING5_1);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE1);
    }

    @Test
    public void dailyFromMacroPeriod() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.DAY1);
    }

    @Test
    public void dailyFromMeasurementPeriod() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.HOUR24);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.DAY1);
    }

    @Test
    public void weekly() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.WEEKLYS);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.WEEK1);
    }

    @Test
    public void monthly() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MONTH1);
    }

    public void seasonIsNotRegular() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.SEASONAL);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isNotNull();
        assertThat(intervalLength).isEqualTo(IntervalLength.NOT_SUPPORTED);
    }

    public void billingPeriodIsNotRegular() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.BILLINGPERIOD);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isNotNull();
        assertThat(intervalLength).isEqualTo(IntervalLength.NOT_SUPPORTED);
    }

    public void specifiedPeriodIsNotRegular() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.SPECIFIEDPERIOD);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isNotNull();
        assertThat(intervalLength).isEqualTo(IntervalLength.NOT_SUPPORTED);
    }

    public void specifiedIntervalIsNotSupported() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.SPECIFIEDINTERVAL);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isNotNull();
        assertThat(intervalLength).isEqualTo(IntervalLength.NOT_SUPPORTED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void specifiedFixedBlockIsNotSupported() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.SPECIFIEDFIXEDBLOCK);

        // Business method
        IntervalLength.from(readingType);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void specifiedRollingBlockIsNotSupported() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.SPECIFIEDROLLINGBLOCK);

        // Business method
        IntervalLength.from(readingType);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void presentIsNotSupported() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.PRESENT);

        // Business method
        IntervalLength.from(readingType);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void previousIsNotSupported() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.PREVIOUS);

        // Business method
        IntervalLength.from(readingType);

        // Asserts: see expected exception rule
    }

    public void register() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);

        // Business method
        IntervalLength intervalLength = IntervalLength.from(readingType);

        // Asserts
        assertThat(intervalLength).isNotNull();
        assertThat(intervalLength).isEqualTo(IntervalLength.NOT_SUPPORTED);
    }

    @Test
    public void isMultipleOfIsReflective() {
        List<IntervalLength> notMultipleOfSelf = allButNotSupported().filter(each -> !each.isMultipleOf(each)).collect(Collectors.toList());
        if (!notMultipleOfSelf.isEmpty()) {
            fail(notMultipleOfSelf.stream().map(IntervalLength::name).collect(Collectors.joining(", ")) + " are not multiples of themselves");
        }
    }

    @Test
    public void multipliesToIsReflective() {
        List<IntervalLength> notMultipleOfSelf = allButNotSupported().filter(each -> !each.multipliesTo(each)).collect(Collectors.toList());
        if (!notMultipleOfSelf.isEmpty()) {
            fail(notMultipleOfSelf.stream().map(IntervalLength::name).collect(Collectors.joining(", ")) + " are not multiples of themselves");
        }
    }

    @Test
    public void hourlyIntervalsMultiplyIntoDay() {
        List<IntervalLength> doNotMultiplyToWeek = hourlyIntervals().filter(each -> !each.multipliesTo(IntervalLength.DAY1)).collect(Collectors.toList());
        if (!doNotMultiplyToWeek.isEmpty()) {
            fail(doNotMultiplyToWeek.stream().map(IntervalLength::name).collect(Collectors.joining(", ")) + " do not multiply to one day");
        }
    }

    @Test
    public void hourlyIntervalsMultiplyIntoWeek() {
        List<IntervalLength> doNotMultiplyToWeek = hourlyIntervals().filter(each -> !each.multipliesTo(IntervalLength.WEEK1)).collect(Collectors.toList());
        if (!doNotMultiplyToWeek.isEmpty()) {
            fail(doNotMultiplyToWeek.stream().map(IntervalLength::name).collect(Collectors.joining(", ")) + " do not multiply to one week");
        }
    }

    @Test
    public void hourlyIntervalsMultiplyIntoMonth() {
        List<IntervalLength> doNotMultiplyToMonth = hourlyIntervals().filter(each -> !each.multipliesTo(IntervalLength.MONTH1)).collect(Collectors.toList());
        if (!doNotMultiplyToMonth.isEmpty()) {
            fail(doNotMultiplyToMonth.stream().map(IntervalLength::name).collect(Collectors.joining(", ")) + " do not multiply to one month");
        }
    }

    @Test
    public void dailyIntervalsMultiplyIntoWeek() {
        List<IntervalLength> doNotMultiplyToWeek = dailyIntervals().filter(each -> !each.multipliesTo(IntervalLength.WEEK1)).collect(Collectors.toList());
        if (!doNotMultiplyToWeek.isEmpty()) {
            fail(doNotMultiplyToWeek.stream().map(IntervalLength::name).collect(Collectors.joining(", ")) + " do not multiply to one week");
        }
    }

    @Test
    public void dailyIntervalsMultiplyIntoMonth() {
        List<IntervalLength> doNotMultiplyToMonth = dailyIntervals().filter(each -> !each.multipliesTo(IntervalLength.MONTH1)).collect(Collectors.toList());
        if (!doNotMultiplyToMonth.isEmpty()) {
            fail(doNotMultiplyToMonth.stream().map(IntervalLength::name).collect(Collectors.joining(", ")) + " do not multiply to one month");
        }
    }

    @Test
    public void dailyIntervalsMultiplyIntoYear() {
        List<IntervalLength> doNotMultiplyToYear = dailyIntervals().filter(each -> !each.multipliesTo(IntervalLength.YEAR1)).collect(Collectors.toList());
        if (!doNotMultiplyToYear.isEmpty()) {
            fail(doNotMultiplyToYear.stream().map(IntervalLength::name).collect(Collectors.joining(", ")) + " do not multiply to one year");
        }
    }

    @Test
    public void flowVolumeConversionFactorForHourOrLess() {
        hourlyIntervals().forEach(this::doFlowVolumeConversionFactorForHourOrLess);
        EnumSet.of(
                IntervalLength.HOUR1,
                IntervalLength.HOUR2,
                IntervalLength.HOUR3,
                IntervalLength.HOUR4,
                IntervalLength.HOUR6,
                IntervalLength.HOUR12)
                .stream()
                .forEach(this::doFlowVolumeConversionFactorForHourOrLess);
    }

    private void doFlowVolumeConversionFactorForHourOrLess(IntervalLength intervalLength) {
        assertThat(intervalLength.getVolumeFlowConversionFactor())
                .as("flow volume conversion factor or " + intervalLength.name() + " was not expected to be null")
                .isNotNull();
    }

    @Test
    public void volumeFlowConversionFactor_1min() {
        assertThat(IntervalLength.MINUTE1.getVolumeFlowConversionFactor()).isEqualTo(BigDecimal.valueOf(60L));
    }

    @Test
    public void volumeFlowConversionFactor_2min() {
        assertThat(IntervalLength.MINUTE2.getVolumeFlowConversionFactor()).isEqualTo(BigDecimal.valueOf(30L));
    }

    @Test
    public void volumeFlowConversionFactor_3min() {
        assertThat(IntervalLength.MINUTE3.getVolumeFlowConversionFactor()).isEqualTo(BigDecimal.valueOf(20L));
    }

    @Test
    public void volumeFlowConversionFactor_4min() {
        assertThat(IntervalLength.MINUTE4.getVolumeFlowConversionFactor()).isEqualTo(BigDecimal.valueOf(15L));
    }

    @Test
    public void volumeFlowConversionFactor_5min() {
        assertThat(IntervalLength.MINUTE5.getVolumeFlowConversionFactor()).isEqualTo(BigDecimal.valueOf(12L));
    }

    @Test
    public void volumeFlowConversionFactor_6min() {
        assertThat(IntervalLength.MINUTE6.getVolumeFlowConversionFactor()).isEqualTo(BigDecimal.valueOf(10L));
    }

    @Test
    public void volumeFlowConversionFactor_10min() {
        assertThat(IntervalLength.MINUTE10.getVolumeFlowConversionFactor()).isEqualTo(BigDecimal.valueOf(6L));
    }

    @Test
    public void volumeFlowConversionFactor_12min() {
        assertThat(IntervalLength.MINUTE12.getVolumeFlowConversionFactor()).isEqualTo(BigDecimal.valueOf(5L));
    }

    @Test
    public void volumeFlowConversionFactor_15min() {
        assertThat(IntervalLength.MINUTE15.getVolumeFlowConversionFactor()).isEqualTo(BigDecimal.valueOf(4L));
    }

    @Test
    public void volumeFlowConversionFactor_30min() {
        assertThat(IntervalLength.MINUTE30.getVolumeFlowConversionFactor()).isEqualTo(BigDecimal.valueOf(2L));
    }

    @Test
    public void volumeFlowConversionFactor_1hour() {
        assertThat(IntervalLength.HOUR1.getVolumeFlowConversionFactor()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    public void volumeFlowConversionFactor_2hour() {
        assertThat(IntervalLength.HOUR2.getVolumeFlowConversionFactor().toString()).isEqualTo("0.5");
    }

    @Test
    public void volumeFlowConversionFactor_3hour() {
        assertThat(IntervalLength.HOUR3.getVolumeFlowConversionFactor().toString()).matches("0\\.3*");
    }

    @Test
    public void volumeFlowConversionFactor_4hour() {
        assertThat(IntervalLength.HOUR4.getVolumeFlowConversionFactor().toString()).isEqualTo("0.25");
    }

    @Test
    public void volumeFlowConversionFactor_6hour() {
        assertThat(IntervalLength.HOUR6.getVolumeFlowConversionFactor().toString()).matches("0\\.16*");
    }

    @Test
    public void volumeFlowConversionFactor_12hour() {
        assertThat(IntervalLength.HOUR12.getVolumeFlowConversionFactor().toString()).matches("0\\.083*");
    }

    private Stream<IntervalLength> hourlyIntervals() {
        return Stream.of(
                IntervalLength.MINUTE1,
                IntervalLength.MINUTE2,
                IntervalLength.MINUTE3,
                IntervalLength.MINUTE4,
                IntervalLength.MINUTE5,
                IntervalLength.MINUTE6,
                IntervalLength.MINUTE10,
                IntervalLength.MINUTE12,
                IntervalLength.MINUTE15,
                IntervalLength.MINUTE20,
                IntervalLength.MINUTE30);
    }

    private Stream<IntervalLength> dailyIntervals() {
        return Stream.of(
                IntervalLength.MINUTE1,
                IntervalLength.MINUTE2,
                IntervalLength.MINUTE3,
                IntervalLength.MINUTE4,
                IntervalLength.MINUTE5,
                IntervalLength.MINUTE6,
                IntervalLength.MINUTE10,
                IntervalLength.MINUTE12,
                IntervalLength.MINUTE15,
                IntervalLength.MINUTE20,
                IntervalLength.MINUTE30,
                IntervalLength.HOUR1,
                IntervalLength.HOUR2,
                IntervalLength.HOUR3,
                IntervalLength.HOUR4,
                IntervalLength.HOUR6,
                IntervalLength.HOUR12);
    }

    @Test
    public void allMultiplyIntoYear() {
        List<IntervalLength> doNotMultiplyToYear = allButNotSupported().filter(each -> !each.multipliesTo(IntervalLength.YEAR1)).collect(Collectors.toList());
        if (!doNotMultiplyToYear.isEmpty()) {
            fail(doNotMultiplyToYear.stream().map(IntervalLength::name).collect(Collectors.joining(", ")) + " do not multiply to one year");
        }
    }

    @Test
    public void aggregationLevelHour() {
        assertThat(IntervalLength.from(AggregationLevel.HOUR)).isEqualTo(IntervalLength.HOUR1);
    }

    @Test
    public void aggregationLevelDay() {
        assertThat(IntervalLength.from(AggregationLevel.DAY)).isEqualTo(IntervalLength.DAY1);
    }

    @Test
    public void aggregationLevelWeek() {
        assertThat(IntervalLength.from(AggregationLevel.WEEK)).isEqualTo(IntervalLength.WEEK1);
    }

    @Test
    public void aggregationLevelMonth() {
        assertThat(IntervalLength.from(AggregationLevel.MONTH)).isEqualTo(IntervalLength.MONTH1);
    }

    @Test
    public void aggregationLevelYear() {
        assertThat(IntervalLength.from(AggregationLevel.YEAR)).isEqualTo(IntervalLength.YEAR1);
    }

    private Stream<IntervalLength> allButNotSupported() {
        return EnumSet.complementOf(EnumSet.of(IntervalLength.NOT_SUPPORTED)).stream();
    }

}