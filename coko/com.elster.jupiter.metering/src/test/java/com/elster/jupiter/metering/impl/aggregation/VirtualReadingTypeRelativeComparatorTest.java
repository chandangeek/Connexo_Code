package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Commodity;
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
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isEqualTo(0);
    }

    @Test
    public void identicalToTargetIsAlwaysSmaller() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
    }

    @Test
    public void sameUnitAndMultiplier() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.DAY1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.DAY1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.DAY1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MONTH1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MONTH1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.DAY1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isGreaterThan(0);
    }

    @Test
    public void sameUnitDifferentMultiplier() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.MEGA, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.MEGA, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.MILLI, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.MILLI, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isGreaterThan(0);
    }

    @Test
    public void favourPrimaryMeteredOverSecondaryMeteredWithPrimaryTarget() {
        VirtualReadingType kWh_60minPrimary = VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60minPrimary);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_SECONDARY_METERED)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_SECONDARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED)))
            .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_SECONDARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_SECONDARY_METERED)))
            .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isEqualTo(0);
    }

    @Test
    public void favourPrimaryMeteredOverSecondaryMeteredWithSecondaryTarget() {
        VirtualReadingType kWh_60minSecondary = VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_SECONDARY_METERED);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60minSecondary);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_SECONDARY_METERED)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_SECONDARY_METERED)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_SECONDARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED)))
            .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_SECONDARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_SECONDARY_METERED)))
            .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isEqualTo(0);
    }

    @Test
    public void favourPrimaryMeteredOverSecondaryMeteredWithNotApplicable() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_SECONDARY_METERED)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_SECONDARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED)))
            .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_SECONDARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_SECONDARY_METERED)))
            .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isEqualTo(0);
    }

    @Test
    public void flowToVolumeConversion() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.MONTH1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isGreaterThan(0);
    }

    @Test
    public void volumeToFlowConversion() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.MONTH1, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Commodity.NOTAPPLICABLE)))
            .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Commodity.NOTAPPLICABLE)))
            .isGreaterThan(0);
    }

    @Test
    public void pressure() {
        VirtualReadingType pascal = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.PASCAL, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(pascal);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.BAR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.MMMERCURY, Commodity.NOTAPPLICABLE)))
            .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.BAR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.MMMERCURY, Commodity.NOTAPPLICABLE)))
            .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.BAR, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.MMMERCURY, Commodity.NOTAPPLICABLE)))
            .isGreaterThan(0);
    }

    @Test
    public void temperature() {
        VirtualReadingType kelvin = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.KELVIN, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kelvin);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.DEGREESCELSIUS, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.DEGREESFAHRENHEIT, Commodity.NOTAPPLICABLE)))
            .isEqualTo(0);
    }

    @Test
    public void powerFromCurrentAndVoltage() {
        VirtualReadingType wattHour = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(wattHour);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.VOLT, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.AMPERE, Commodity.NOTAPPLICABLE)))
            .isEqualTo(0);
    }

}