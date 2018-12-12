/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ReadingTypeDeliverableForMeterActivationSet#appendReferenceTo(SqlBuilder, VirtualReadingType)} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-25 (09:17)
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeDeliverableForMeterActivationSetReferenceTest {

    private static final long DELIVERABLE_ID = 97L;

    @Mock
    private ReadingTypeDeliverable deliverable;
    @Mock
    private ReadingType readingType;
    @Mock
    private MeterActivationSet meterActivationSet;
    @Mock
    private ServerMeteringService meteringService;

    private ServerExpressionNode expressionNode = new NumericalConstantNode(BigDecimal.TEN);

    @Before
    public void initializeMocks() {
        when(this.deliverable.getId()).thenReturn(DELIVERABLE_ID);
        when(this.deliverable.getReadingType()).thenReturn(this.readingType);
        when(this.readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(this.readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(this.readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(this.readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
    }

    @Test
    public void appendReferenceToWithSameSourceAndTargetReadingType() {
        VirtualReadingType targetReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(targetReadingType);
        SqlBuilder sqlBuilder = new SqlBuilder();

        // Business method
        testInstance.appendReferenceTo(sqlBuilder, targetReadingType);

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualTo("rod97_1.value");
    }

    @Test
    public void appendReferenceToWithDownScaling() {
        VirtualReadingType sourceReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType targetReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(sourceReadingType);
        SqlBuilder sqlBuilder = new SqlBuilder();

        // Business method
        testInstance.appendReferenceTo(sqlBuilder, targetReadingType);

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualTo("(rod97_1.value * 1E+3)");
    }

    @Test
    public void appendReferenceToWithUpScaling() {
        VirtualReadingType sourceReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType targetReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(sourceReadingType);
        SqlBuilder sqlBuilder = new SqlBuilder();

        // Business method
        testInstance.appendReferenceTo(sqlBuilder, targetReadingType);

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualTo("(rod97_1.value * 0.001)");
    }

    @Test
    public void appendReferenceToWithFlowToVolumeConversion() {
        VirtualReadingType sourceReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(this.readingType.getUnit()).thenReturn(ReadingTypeUnit.WATT);
        VirtualReadingType targetReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(sourceReadingType);
        SqlBuilder sqlBuilder = new SqlBuilder();

        // Business method
        testInstance.appendReferenceTo(sqlBuilder, targetReadingType);

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualTo("(rod97_1.value / 4)");
    }

    @Test
    public void appendReferenceToWithFlowToVolumeConversionWithDownScaling() {
        VirtualReadingType sourceReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(this.readingType.getUnit()).thenReturn(ReadingTypeUnit.WATT);
        VirtualReadingType targetReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(sourceReadingType);
        SqlBuilder sqlBuilder = new SqlBuilder();

        // Business method
        testInstance.appendReferenceTo(sqlBuilder, targetReadingType);

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualTo("((rod97_1.value / 4) * 1E+3)");
    }

    @Test
    public void appendReferenceToWithFlowToVolumeConversionWithUpScaling() {
        VirtualReadingType sourceReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(this.readingType.getUnit()).thenReturn(ReadingTypeUnit.WATT);
        VirtualReadingType targetReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(sourceReadingType);
        SqlBuilder sqlBuilder = new SqlBuilder();

        // Business method
        testInstance.appendReferenceTo(sqlBuilder, targetReadingType);

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualTo("((rod97_1.value / 4) * 0.001)");
    }

    @Test
    public void appendReferenceToWithVolumeToFlowConversion() {
        VirtualReadingType sourceReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(this.readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        VirtualReadingType targetReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(sourceReadingType);
        SqlBuilder sqlBuilder = new SqlBuilder();

        // Business method
        testInstance.appendReferenceTo(sqlBuilder, targetReadingType);

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualTo("(rod97_1.value * 4)");
    }

    @Test
    public void appendReferenceToWithVolumeToFlowConversionWithDownScaling() {
        VirtualReadingType sourceReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(this.readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        VirtualReadingType targetReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(sourceReadingType);
        SqlBuilder sqlBuilder = new SqlBuilder();

        // Business method
        testInstance.appendReferenceTo(sqlBuilder, targetReadingType);

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualTo("(rod97_1.value * 4E+3)");
    }

    @Test
    public void appendReferenceToWithVolumeToFlowConversionWithUpScaling() {
        VirtualReadingType sourceReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(this.readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        VirtualReadingType targetReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATT, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(sourceReadingType);
        SqlBuilder sqlBuilder = new SqlBuilder();

        // Business method
        testInstance.appendReferenceTo(sqlBuilder, targetReadingType);

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualTo("(rod97_1.value * 0.004)");
    }

    @Test
    public void appendReferenceToWithLiterToCubicMeterConversion() {
        VirtualReadingType sourceReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.LITRE, Accumulation.DELTADELTA, Commodity.POTABLEWATER);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(this.readingType.getUnit()).thenReturn(ReadingTypeUnit.LITRE);
        VirtualReadingType targetReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.CUBICMETER, Accumulation.DELTADELTA, Commodity.POTABLEWATER);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(sourceReadingType);
        SqlBuilder sqlBuilder = new SqlBuilder();

        // Business method
        testInstance.appendReferenceTo(sqlBuilder, targetReadingType);

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualTo("(0.001 * rod97_1.value)");
    }

    @Test
    public void appendReferenceToWithLiterToCubicMeterConversionAndDownScaling() {
        VirtualReadingType sourceReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.MEGA, ReadingTypeUnit.LITRE, Accumulation.DELTADELTA, Commodity.POTABLEWATER);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.MEGA);
        when(this.readingType.getUnit()).thenReturn(ReadingTypeUnit.LITRE);
        VirtualReadingType targetReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.CUBICMETER, Accumulation.DELTADELTA, Commodity.POTABLEWATER);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(sourceReadingType);
        SqlBuilder sqlBuilder = new SqlBuilder();

        // Business method
        testInstance.appendReferenceTo(sqlBuilder, targetReadingType);

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualTo("(0.001 * (1E+6 * rod97_1.value))");
    }

    @Test
    public void appendReferenceToWithLiterToCubicMeterConversionAndUpScaling() {
        VirtualReadingType sourceReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.LITRE, Accumulation.DELTADELTA, Commodity.POTABLEWATER);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(this.readingType.getUnit()).thenReturn(ReadingTypeUnit.LITRE);
        VirtualReadingType targetReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.CUBICMETER, Accumulation.DELTADELTA, Commodity.POTABLEWATER);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(sourceReadingType);
        SqlBuilder sqlBuilder = new SqlBuilder();

        // Business method
        testInstance.appendReferenceTo(sqlBuilder, targetReadingType);

        // Asserts
        assertThat(sqlBuilder.getText()).isEqualTo("((0.001 * rod97_1.value) / 1E+3)");
    }

    @Test
    public void appendReferenceToInExpertMode() {
        VirtualReadingType sourceReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.LITRE, Accumulation.DELTADELTA, Commodity.POTABLEWATER);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(this.readingType.getUnit()).thenReturn(ReadingTypeUnit.LITRE);
        VirtualReadingType targetReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.DEGREESCELSIUS, Accumulation.BULKQUANTITY, Commodity.WEATHER);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(Formula.Mode.EXPERT, sourceReadingType);
        SqlBuilder sqlBuilder = new SqlBuilder();

        // Business method
        testInstance.appendReferenceTo(sqlBuilder, targetReadingType);

        // Asserts: even the silliest of request from expert generates a simple reference to the value
        assertThat(sqlBuilder.getText()).isEqualTo("rod97_1.value");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void appendReferenceToForBogusRequest() {
        VirtualReadingType sourceReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.LITRE, Accumulation.DELTADELTA, Commodity.POTABLEWATER);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(this.readingType.getUnit()).thenReturn(ReadingTypeUnit.LITRE);
        VirtualReadingType targetReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.DEGREESCELSIUS, Accumulation.BULKQUANTITY, Commodity.WEATHER);
        ReadingTypeDeliverableForMeterActivationSet testInstance = this.testInstance(sourceReadingType);
        SqlBuilder sqlBuilder = new SqlBuilder();

        // Business method
        testInstance.appendReferenceTo(sqlBuilder, targetReadingType);

        // Asserts: silly requests in auto mode should always produce exception
    }

    private ReadingTypeDeliverableForMeterActivationSet testInstance(VirtualReadingType virtualReadingType) {
        return this.testInstance(Formula.Mode.AUTO, virtualReadingType);
    }

    private ReadingTypeDeliverableForMeterActivationSet testInstance(Formula.Mode mode, VirtualReadingType virtualReadingType) {
        return new ReadingTypeDeliverableForMeterActivationSet(
                this.meteringService,
                mode,
                this.deliverable,
                this.meterActivationSet,
                1,
                this.expressionNode,
                virtualReadingType);
    }

}