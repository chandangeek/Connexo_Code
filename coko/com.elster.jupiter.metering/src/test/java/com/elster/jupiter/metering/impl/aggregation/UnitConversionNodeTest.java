/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.util.units.Dimension;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link UnitConversionNode} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class UnitConversionNodeTest {

    @Mock
    private MeterActivationSet meterActivationSet;
    @Mock
    private VirtualFactory virtualFactory;
    @Mock
    private VirtualReadingTypeRequirement virtualReadingTypeRequirement1;
    @Mock
    private VirtualReadingTypeRequirement virtualReadingTypeRequirement2;
    @Mock
    private FullySpecifiedReadingTypeRequirement requirement1;
    @Mock
    private FullySpecifiedReadingTypeRequirement requirement2;
    @Mock
    private Channel channel1;
    @Mock
    private Channel channel2;
    @Mock
    private ReadingTypeDeliverable deliverable;
    private ReadingType fifteenMins_kWh;
    private ReadingType thirtyMins_kWh;

    @Before
    public void initializeMocks() {
        when(this.meterActivationSet.getRange()).thenReturn(Range.atLeast(Instant.EPOCH));
        this.fifteenMins_kWh = this.mock15minReadingType();
        this.thirtyMins_kWh = this.mock30minReadingType();
        ReadingType monthly_kWh = this.mockMonthlyReadingType();
        when(this.meterActivationSet.getMatchingChannelsFor(this.requirement1)).thenReturn(Collections.singletonList(this.channel1));
        when(this.meterActivationSet.getMatchingChannelsFor(this.requirement2)).thenReturn(Collections.singletonList(this.channel2));
        when(this.deliverable.getReadingType()).thenReturn(monthly_kWh);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        this.requirement1,
                        this.deliverable,
                        VirtualReadingType.from(monthly_kWh)))
                .thenReturn(this.virtualReadingTypeRequirement1);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        this.requirement2,
                        this.deliverable,
                        VirtualReadingType.from(monthly_kWh)))
                .thenReturn(this.virtualReadingTypeRequirement2);
    }

    @Test
    public void fifteenMinutesToMonthlyShouldSet15MinsAsSourceIntervalLength() {
        VirtualReadingType fifteenMin_kWhVirtualReadingType = VirtualReadingType.from(this.fifteenMins_kWh);
        when(this.requirement1.getReadingType()).thenReturn(this.fifteenMins_kWh);
        when(this.channel1.getMainReadingType()).thenReturn(this.fifteenMins_kWh);
        when(this.requirement2.getReadingType()).thenReturn(this.fifteenMins_kWh);
        when(this.channel2.getMainReadingType()).thenReturn(this.fifteenMins_kWh);
        when(this.virtualReadingTypeRequirement1.getSourceReadingType()).thenReturn(fifteenMin_kWhVirtualReadingType);
        when(this.virtualReadingTypeRequirement2.getSourceReadingType()).thenReturn(fifteenMin_kWhVirtualReadingType);
        OperationNode operationNode =
                Operator.PLUS.node(
                        this.requirementNode(this.requirement1),
                        this.requirementNode(this.requirement2));
        VirtualReadingType expectedTargetReadingType = VirtualReadingType.from(
                IntervalLength.MONTH1,
                MetricMultiplier.KILO,
                ReadingTypeUnit.WATTHOUR,
                Accumulation.DELTADELTA,
                Commodity.ELECTRICITY_PRIMARY_METERED);
        VirtualReadingType expectedSourceReadingType =
                VirtualReadingType.from(
                        IntervalLength.MINUTE15,
                        MetricMultiplier.KILO,
                        ReadingTypeUnit.WATTHOUR,
                        Accumulation.DELTADELTA,
                        Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        UnitConversionNode unitConversionNode = new UnitConversionNode(
                operationNode,
                Dimension.ENERGY,
                expectedTargetReadingType);

        // Asserts
        assertThat(unitConversionNode.getSourceReadingType()).isEqualTo(expectedSourceReadingType);
        assertThat(unitConversionNode.getTargetReadingType()).isEqualTo(expectedTargetReadingType);
    }

    @Test
    public void fifteenAndThirtyMinutesToMonthlyShouldSetHourlyAsSourceIntervalLength() {
        VirtualReadingType fifteenMin_kWhVirtualReadingType = VirtualReadingType.from(this.fifteenMins_kWh);
        VirtualReadingType thirtyMin_kWhVirtualReadingType = VirtualReadingType.from(this.thirtyMins_kWh);
        when(this.requirement1.getReadingType()).thenReturn(this.fifteenMins_kWh);
        when(this.channel1.getMainReadingType()).thenReturn(this.fifteenMins_kWh);
        when(this.requirement2.getReadingType()).thenReturn(this.thirtyMins_kWh);
        when(this.channel2.getMainReadingType()).thenReturn(this.thirtyMins_kWh);
        when(this.virtualReadingTypeRequirement1.getSourceReadingType()).thenReturn(fifteenMin_kWhVirtualReadingType);
        when(this.virtualReadingTypeRequirement2.getSourceReadingType()).thenReturn(thirtyMin_kWhVirtualReadingType);
        OperationNode operationNode =
                Operator.PLUS.node(
                        this.requirementNode(this.requirement1),
                        this.requirementNode(this.requirement2));
        VirtualReadingType expectedTargetReadingType = VirtualReadingType.from(
                IntervalLength.MONTH1,
                MetricMultiplier.KILO,
                ReadingTypeUnit.WATTHOUR,
                Accumulation.DELTADELTA,
                Commodity.ELECTRICITY_PRIMARY_METERED);
        VirtualReadingType expectedSourceReadingType =
                VirtualReadingType.from(
                        IntervalLength.HOUR1,
                        MetricMultiplier.KILO,
                        ReadingTypeUnit.WATTHOUR,
                        Accumulation.DELTADELTA,
                        Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        UnitConversionNode unitConversionNode = new UnitConversionNode(
                operationNode,
                Dimension.ENERGY,
                expectedTargetReadingType);

        // Asserts
        assertThat(unitConversionNode.getSourceReadingType()).isEqualTo(expectedSourceReadingType);
        assertThat(unitConversionNode.getTargetReadingType()).isEqualTo(expectedTargetReadingType);
    }

    @Test
    public void threeMinuresAnd15MinutesToHourlyShouldSetMonthlyAsSourceIntervalLength() {
        ReadingType threeMinutes = this.mockReadingType(TimeAttribute.MINUTE3);
        VirtualReadingType fifteenMin_kWhVirtualReadingType = VirtualReadingType.from(this.fifteenMins_kWh);
        VirtualReadingType threeMin_kWhVirtualReadingType = VirtualReadingType.from(threeMinutes);
        when(this.requirement1.getReadingType()).thenReturn(threeMinutes);
        when(this.channel1.getMainReadingType()).thenReturn(threeMinutes);
        when(this.requirement2.getReadingType()).thenReturn(this.fifteenMins_kWh);
        when(this.channel2.getMainReadingType()).thenReturn(this.fifteenMins_kWh);
        when(this.virtualReadingTypeRequirement1.getSourceReadingType()).thenReturn(threeMin_kWhVirtualReadingType);
        when(this.virtualReadingTypeRequirement2.getSourceReadingType()).thenReturn(fifteenMin_kWhVirtualReadingType);
        OperationNode operationNode =
                Operator.PLUS.node(
                        this.requirementNode(this.requirement1),
                        this.requirementNode(this.requirement2));
        VirtualReadingType expectedReadingType = VirtualReadingType.from(
                IntervalLength.HOUR1,
                MetricMultiplier.KILO,
                ReadingTypeUnit.WATTHOUR,
                Accumulation.DELTADELTA,
                Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        UnitConversionNode unitConversionNode = new UnitConversionNode(
                operationNode,
                Dimension.ENERGY,
                expectedReadingType);

        // Asserts
        assertThat(unitConversionNode.getSourceReadingType()).isEqualTo(expectedReadingType);
        assertThat(unitConversionNode.getTargetReadingType()).isEqualTo(expectedReadingType);
    }

    private VirtualRequirementNode requirementNode(FullySpecifiedReadingTypeRequirement requirement) {
        return new VirtualRequirementNode(Formula.Mode.AUTO, this.virtualFactory, requirement, this.deliverable, this.meterActivationSet);
    }

    private ReadingType mock15minReadingType() {
        return this.mockReadingType(TimeAttribute.MINUTE15);
    }

    private ReadingType mock30minReadingType() {
        return this.mockReadingType(TimeAttribute.MINUTE30);
    }

    private ReadingType mockMonthlyReadingType() {
        return this.mockReadingType(MacroPeriod.MONTHLY, TimeAttribute.NOTAPPLICABLE);
    }

    private ReadingType mockReadingType(TimeAttribute timeAttribute) {
        return this.mockReadingType(MacroPeriod.NOTAPPLICABLE, timeAttribute);
    }

    private ReadingType mockReadingType(MacroPeriod macroPeriod, TimeAttribute timeAttribute) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("UnitConversionNodeTest");
        when(readingType.getMacroPeriod()).thenReturn(macroPeriod);
        when(readingType.getMeasuringPeriod()).thenReturn(timeAttribute);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

}