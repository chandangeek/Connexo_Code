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
 * Tests the {@link VirtualReadingTypeRelativeComparator} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-03 (10:16)
 */
public class VirtualReadingTypeRelativeComparatorTest {

    @Test
    public void identical() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isEqualTo(0);
    }

    @Test
    public void identicalToTargetIsAlwaysSmaller() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
    }

    @Test
    public void sameUnitAndMultiplier() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.DAY1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.DAY1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.DAY1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MONTH1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MONTH1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.DAY1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isGreaterThan(0);
    }

    @Test
    public void sameUnitDifferentMultiplier() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.MEGA, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.MEGA, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.MILLI, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.MILLI, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isGreaterThan(0);
    }

    @Test
    public void favourPrimaryMeteredOverSecondaryMeteredWithPrimaryTarget() {
        VirtualReadingType kWh_60minPrimary = VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60minPrimary);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED)))
                .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED)))
                .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isEqualTo(0);
    }

    @Test
    public void favourPrimaryMeteredOverSecondaryMeteredWithSecondaryTarget() {
        VirtualReadingType kWh_60minSecondary = VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60minSecondary);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED)))
                .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED)))
                .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isEqualTo(0);
    }

    @Test
    public void favourPrimaryMeteredOverSecondaryMeteredWithNotApplicable() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED)))
                .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_SECONDARY_METERED)))
                .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isEqualTo(0);
    }

    @Test
    public void flowToVolumeConversion() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.MONTH1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isGreaterThan(0);
    }

    @Test
    public void volumeToFlowConversion() {
        VirtualReadingType kWh_60min = VirtualReadingType.from(IntervalLength.MONTH1, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kWh_60min);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE10, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isGreaterThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE)))
                .isGreaterThan(0);
    }

    @Test
    public void pressure() {
        VirtualReadingType pascal = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.PASCAL, Accumulation.BULKQUANTITY, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(pascal);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.BAR, Accumulation.BULKQUANTITY, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.MMMERCURY, Accumulation.BULKQUANTITY, Commodity.NOTAPPLICABLE)))
                .isEqualTo(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.BAR, Accumulation.BULKQUANTITY, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.MMMERCURY, Accumulation.BULKQUANTITY, Commodity.NOTAPPLICABLE)))
                .isLessThan(0);
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.BAR, Accumulation.BULKQUANTITY, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.MMMERCURY, Accumulation.BULKQUANTITY, Commodity.NOTAPPLICABLE)))
                .isGreaterThan(0);
    }

    @Test
    public void temperature() {
        VirtualReadingType kelvin = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.KELVIN, Accumulation.BULKQUANTITY, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(kelvin);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.DEGREESCELSIUS, Accumulation.BULKQUANTITY, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.DEGREESFAHRENHEIT, Accumulation.BULKQUANTITY, Commodity.NOTAPPLICABLE)))
                .isEqualTo(0);
    }

    @Test
    public void powerFromCurrentAndVoltage() {
        VirtualReadingType wattHour = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);
        VirtualReadingTypeRelativeComparator comparator = new VirtualReadingTypeRelativeComparator(wattHour);

        // Business methods & asserts
        assertThat(comparator.compare(
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.VOLT, Accumulation.BULKQUANTITY, Commodity.NOTAPPLICABLE),
                VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.AMPERE, Accumulation.BULKQUANTITY, Commodity.NOTAPPLICABLE)))
                .isEqualTo(0);
    }

}