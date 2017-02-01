/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the various withXXX methods of the {@link VirtualReadingType} component.
 * <ul>
 * <li>{@link VirtualReadingType#withIntervalLength(IntervalLength)}</li>
 * <li>{@link VirtualReadingType#withMetricMultiplier(MetricMultiplier)}</li>
 * <li>{@link VirtualReadingType#withUnit(ReadingTypeUnit)}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-26 (12:21
 */
public class VirtualReadingTypeConversionTest {

    @Test
    public void withSameIntervalLength() {
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        VirtualReadingType withIntervalLength = readingType.withIntervalLength(IntervalLength.MINUTE15);

        // Asserts
        assertThat(withIntervalLength).isEqualTo(readingType);
        assertThat(withIntervalLength.isUnsupported()).isFalse();
        assertThat(withIntervalLength.isDontCare()).isFalse();
    }

    @Test
    public void withSmallerIntervalLength() {
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.DAY1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        VirtualReadingType withIntervalLength = readingType.withIntervalLength(IntervalLength.MINUTE15);

        // Asserts
        assertThat(withIntervalLength).isNotEqualTo(readingType);
        assertThat(withIntervalLength.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        assertThat(withIntervalLength.getUnitMultiplier()).isEqualTo(MetricMultiplier.ZERO);
        assertThat(withIntervalLength.getUnit()).isEqualTo(ReadingTypeUnit.WATTHOUR);
        assertThat(withIntervalLength.getCommodity()).isEqualTo(Commodity.ELECTRICITY_PRIMARY_METERED);
        assertThat(withIntervalLength.isUnsupported()).isFalse();
        assertThat(withIntervalLength.isDontCare()).isFalse();
    }

    @Test
    public void withBiggerIntervalLength() {
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        VirtualReadingType withIntervalLength = readingType.withIntervalLength(IntervalLength.DAY1);

        // Asserts
        assertThat(withIntervalLength).isNotEqualTo(readingType);
        assertThat(withIntervalLength.getIntervalLength()).isEqualTo(IntervalLength.DAY1);
        assertThat(withIntervalLength.getUnitMultiplier()).isEqualTo(MetricMultiplier.ZERO);
        assertThat(withIntervalLength.getUnit()).isEqualTo(ReadingTypeUnit.WATTHOUR);
        assertThat(withIntervalLength.getCommodity()).isEqualTo(Commodity.ELECTRICITY_PRIMARY_METERED);
        assertThat(withIntervalLength.isUnsupported()).isFalse();
        assertThat(withIntervalLength.isDontCare()).isFalse();
    }

    @Test
    public void unsupportedWithIntervalIsStillUnsupported() {
        VirtualReadingType notSupported = VirtualReadingType.notSupported();

        // Business method
        VirtualReadingType withIntervalLength = notSupported.withIntervalLength(IntervalLength.DAY1);

        // Asserts
        assertThat(withIntervalLength.isUnsupported()).isTrue();
        assertThat(withIntervalLength.isDontCare()).isFalse();
    }

    @Test
    public void dontCareWithIntervalIsStillDontCare() {
        VirtualReadingType dontCare = VirtualReadingType.dontCare();

        // Business method
        VirtualReadingType withIntervalLength = dontCare.withIntervalLength(IntervalLength.DAY1);

        // Asserts
        assertThat(withIntervalLength.isDontCare()).isTrue();
        assertThat(withIntervalLength.isUnsupported()).isFalse();
    }

    @Test
    public void withSameMultiplier() {
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        VirtualReadingType withMultiplier = readingType.withMetricMultiplier(MetricMultiplier.ZERO);

        // Asserts
        assertThat(withMultiplier).isEqualTo(readingType);
        assertThat(withMultiplier.isUnsupported()).isFalse();
        assertThat(withMultiplier.isDontCare()).isFalse();
    }

    @Test
    public void withSmallerMultiplier() {
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.DAY1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        VirtualReadingType withMultiplier = readingType.withMetricMultiplier(MetricMultiplier.MILLI);

        // Asserts
        assertThat(withMultiplier).isNotEqualTo(readingType);
        assertThat(withMultiplier.getUnitMultiplier()).isEqualTo(MetricMultiplier.MILLI);
        assertThat(withMultiplier.getUnit()).isEqualTo(ReadingTypeUnit.WATTHOUR);
        assertThat(withMultiplier.getIntervalLength()).isEqualTo(IntervalLength.DAY1);
        assertThat(withMultiplier.getCommodity()).isEqualTo(Commodity.ELECTRICITY_PRIMARY_METERED);
        assertThat(withMultiplier.isUnsupported()).isFalse();
        assertThat(withMultiplier.isDontCare()).isFalse();
    }

    @Test
    public void withBiggerMultiplier() {
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        VirtualReadingType withMultiplier = readingType.withMetricMultiplier(MetricMultiplier.KILO);

        // Asserts
        assertThat(withMultiplier).isNotEqualTo(readingType);
        assertThat(withMultiplier.getUnitMultiplier()).isEqualTo(MetricMultiplier.KILO);
        assertThat(withMultiplier.getUnit()).isEqualTo(ReadingTypeUnit.WATTHOUR);
        assertThat(withMultiplier.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        assertThat(withMultiplier.getCommodity()).isEqualTo(Commodity.ELECTRICITY_PRIMARY_METERED);
        assertThat(withMultiplier.isUnsupported()).isFalse();
        assertThat(withMultiplier.isDontCare()).isFalse();
    }

    @Test
    public void unsupportedWithMultiplierIsStillUnsupported() {
        VirtualReadingType notSupported = VirtualReadingType.notSupported();

        // Business method
        VirtualReadingType withMultiplier = notSupported.withMetricMultiplier(MetricMultiplier.KILO);

        // Asserts
        assertThat(withMultiplier.isUnsupported()).isTrue();
        assertThat(withMultiplier.isDontCare()).isFalse();
    }

    @Test
    public void dontCareWithMultiplierIsStillDontCare() {
        VirtualReadingType dontCare = VirtualReadingType.dontCare();

        // Business method
        VirtualReadingType withMultiplier = dontCare.withMetricMultiplier(MetricMultiplier.KILO);

        // Asserts
        assertThat(withMultiplier.isDontCare()).isTrue();
        assertThat(withMultiplier.isUnsupported()).isFalse();
    }

    @Test
    public void withSameUnit() {
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        VirtualReadingType withUnit = readingType.withUnit(ReadingTypeUnit.WATTHOUR);

        // Asserts
        assertThat(withUnit).isEqualTo(readingType);
        assertThat(withUnit.isUnsupported()).isFalse();
        assertThat(withUnit.isDontCare()).isFalse();
    }

    @Test
    public void withOtherUnit() {
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.LITRE, Accumulation.BULKQUANTITY, Commodity.POTABLEWATER);

        // Business method
        VirtualReadingType withUnit = readingType.withUnit(ReadingTypeUnit.CUBICMETER);

        // Asserts
        assertThat(withUnit).isNotEqualTo(readingType);
        assertThat(withUnit.getUnit()).isEqualTo(ReadingTypeUnit.CUBICMETER);
        assertThat(withUnit.getUnitMultiplier()).isEqualTo(MetricMultiplier.ZERO);
        assertThat(withUnit.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        assertThat(withUnit.getCommodity()).isEqualTo(Commodity.POTABLEWATER);
        assertThat(withUnit.isUnsupported()).isFalse();
        assertThat(withUnit.isDontCare()).isFalse();
    }

    @Test
    public void unsupportedWithUnitIsStillUnsupported() {
        VirtualReadingType notSupported = VirtualReadingType.notSupported();

        // Business method
        VirtualReadingType withMultiplier = notSupported.withUnit(ReadingTypeUnit.AMPERE);

        // Asserts
        assertThat(withMultiplier.isUnsupported()).isTrue();
        assertThat(withMultiplier.isDontCare()).isFalse();
    }

    @Test
    public void dontCareWithUnitIsStillDontCare() {
        VirtualReadingType dontCare = VirtualReadingType.dontCare();

        // Business method
        VirtualReadingType withMultiplier = dontCare.withUnit(ReadingTypeUnit.AMPERE);

        // Asserts
        assertThat(withMultiplier.isDontCare()).isTrue();
        assertThat(withMultiplier.isUnsupported()).isFalse();
    }

    @Test
    public void withSameCommodity() {
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        VirtualReadingType withUnit = readingType.withCommondity(Commodity.ELECTRICITY_PRIMARY_METERED);

        // Asserts
        assertThat(withUnit).isEqualTo(readingType);
        assertThat(withUnit.isUnsupported()).isFalse();
        assertThat(withUnit.isDontCare()).isFalse();
    }

    @Test
    public void withOtherCommodity() {
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        VirtualReadingType withUnit = readingType.withCommondity(Commodity.ELECTRICITY_SECONDARY_METERED);

        // Asserts
        assertThat(withUnit).isNotEqualTo(readingType);
        assertThat(withUnit.getCommodity()).isEqualTo(Commodity.ELECTRICITY_SECONDARY_METERED);
        assertThat(withUnit.getUnit()).isEqualTo(ReadingTypeUnit.WATTHOUR);
        assertThat(withUnit.getUnitMultiplier()).isEqualTo(MetricMultiplier.ZERO);
        assertThat(withUnit.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        assertThat(withUnit.isUnsupported()).isFalse();
        assertThat(withUnit.isDontCare()).isFalse();
    }

    @Test
    public void unsupportedWithCommodityIsStillUnsupported() {
        VirtualReadingType notSupported = VirtualReadingType.notSupported();

        // Business method
        VirtualReadingType withMultiplier = notSupported.withCommondity(Commodity.ELECTRICITY_SECONDARY_METERED);

        // Asserts
        assertThat(withMultiplier.isUnsupported()).isTrue();
        assertThat(withMultiplier.isDontCare()).isFalse();
    }

    @Test
    public void dontCareWithCommodityIsStillDontCare() {
        VirtualReadingType dontCare = VirtualReadingType.dontCare();

        // Business method
        VirtualReadingType withMultiplier = dontCare.withCommondity(Commodity.ELECTRICITY_SECONDARY_METERED);

        // Asserts
        assertThat(withMultiplier.isDontCare()).isTrue();
        assertThat(withMultiplier.isUnsupported()).isFalse();
    }

}