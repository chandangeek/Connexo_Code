package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.*;

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

    @Test(expected = IllegalArgumentException.class)
    public void seasonIsNotSupported() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.SEASONAL);

        // Business method
        IntervalLength.from(readingType);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void billingPeriodIsNotSupported() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.BILLINGPERIOD);

        // Business method
        IntervalLength.from(readingType);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void specifiedPeriodIsNotSupported() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.SPECIFIEDPERIOD);

        // Business method
        IntervalLength.from(readingType);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void specifiedIntervalIsNotSupported() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.SPECIFIEDINTERVAL);

        // Business method
        IntervalLength.from(readingType);

        // Asserts: see expected exception rule
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

    @Test(expected = IllegalArgumentException.class)
    public void notEnoughDetails() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);

        // Business method
        IntervalLength.from(readingType);

        // Asserts: see expected exception rule
    }

}