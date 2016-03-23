package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link VirtualReadingTypeRelativeComparator} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-03 (10:16)
 */
public class VirtualReadingTypeRelativeComparatorTest {

    @Test
    public void identical() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR)))
            .isEqualTo(0);
    }

    @Test
    public void identicalToTargetIsAlwaysSmaller() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR)))
            .isLessThan(0);
    }

    @Test
    public void sameUnitAndMultiplier() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.DAY1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.DAY1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.DAY1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MONTH1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MONTH1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.DAY1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR)))
            .isGreaterThan(0);
    }

    @Test
    public void sameUnitDifferentMultiplier() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.MEGA, ReadingTypeUnit.WATTHOUR)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.MEGA, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.MILLI, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.MILLI, ReadingTypeUnit.WATTHOUR)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)))
            .isGreaterThan(0);
    }

    @Test
    public void flowToVolumeConversion() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.MONTH1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATT)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATT),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATT)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATT),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR)))
            .isGreaterThan(0);
    }

    @Test
    public void volumeToFlowConversion() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.MONTH1, MetricMultiplier.ZERO, ReadingTypeUnit.WATT);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATT),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATT)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATT),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATT)))
            .isGreaterThan(0);
    }

    @Test
    public void pressure() {
        VirtualReadingType pascal = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.PASCAL);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(pascal);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.BAR),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.MMMERCURY)))
            .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.BAR),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.MMMERCURY)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.BAR),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.MMMERCURY)))
            .isGreaterThan(0);
    }

    @Test
    public void temperature() {
        VirtualReadingType kelvin = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.KELVIN);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kelvin);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.DEGREESCELSIUS),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.DEGREESFAHRENHEIT)))
            .isEqualTo(0);
    }

}