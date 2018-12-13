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
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.units.Dimension;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ExpressionNodeToString} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-14 (16:34)
 */
public class ExpressionNodeToStringTest {

    @Test
    public void testNull() {
        // Business method
        String expression = testInstance().visitNull(new NullNode());

        // Asserts
        assertThat(expression).isEqualTo("null");
    }

    @Test
    public void testNumericalConstant() {
        // Business method
        String expression = testInstance().visitConstant(new NumericalConstantNode(BigDecimal.TEN));

        // Asserts
        assertThat(expression).isEqualTo("10");
    }

    @Test
    public void testStringConstant() {
        String expectedExpression = "Whatever";

        // Business method
        String expression = testInstance().visitConstant(new StringConstantNode(expectedExpression));

        // Asserts
        assertThat(expression).isEqualTo(expectedExpression);
    }

    @Test
    public void testSqlFragment() {
        String sql = "select sysdate from dual";

        // Business method
        String expression = testInstance().visitSqlFragment(new SqlFragmentNode(sql));

        // Asserts
        assertThat(expression).isEqualTo(sql);
    }

    @Test
    public void testSqlPlusConstant() {
        String sqlText = "sequence.nextval";
        SqlFragmentNode sql = new SqlFragmentNode(sqlText);
        OperationNode operation = Operator.PLUS.node(sql, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        String expression = testInstance().visitOperation(operation);

        // Asserts
        assertThat(expression).isEqualTo("(" + sqlText + " + 10)");
    }

    @Test
    public void testSqlMinusConstant() {
        String sqlText = "sequence.nextval";
        SqlFragmentNode sql = new SqlFragmentNode(sqlText);
        OperationNode operation = Operator.MINUS.node(sql, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        String expression = testInstance().visitOperation(operation);

        // Asserts
        assertThat(expression).isEqualTo("(" + sqlText + " - 10)");
    }

    @Test
    public void testSqlTimesConstant() {
        String sqlText = "sequence.nextval";
        SqlFragmentNode sql = new SqlFragmentNode(sqlText);
        OperationNode operation = Operator.MULTIPLY.node(sql, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        String expression = testInstance().visitOperation(operation);

        // Asserts
        assertThat(expression).isEqualTo("(" + sqlText + " * 10)");
    }

    @Test
    public void testSqlDividedByConstant() {
        String sqlText = "sequence.nextval";
        SqlFragmentNode sql = new SqlFragmentNode(sqlText);
        OperationNode operation = Operator.DIVIDE.node(sql, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        String expression = testInstance().visitOperation(operation);

        // Asserts
        assertThat(expression).isEqualTo("(" + sqlText + " / 10)");
    }

    @Test
    public void testSafeDivision() {
        SqlFragmentNode sql1 = new SqlFragmentNode("var1");
        SqlFragmentNode sql2 = new SqlFragmentNode("var2");
        OperationNode operation = Operator.SAFE_DIVIDE.safeNode(sql1, sql2, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        String expression = testInstance().visitOperation(operation);

        // Asserts
        assertThat(expression).isEqualTo("(var1 / decode(var2, 0, 10, var2))");
    }

    @Test
    public void testFunctionCall() {
        SqlFragmentNode sql1 = new SqlFragmentNode("var1");
        SqlFragmentNode sql2 = new SqlFragmentNode("var2");
        FunctionCallNode node = new FunctionCallNode(Function.SUM, IntermediateDimension.of(Dimension.DIMENSIONLESS), sql1, sql2, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        String expression = testInstance().visitFunctionCall(node);

        // Asserts
        assertThat(expression).isEqualTo("SUM(var1, var2, 10)");
    }

    @Test
    public void testTimeBasedAggregationForFlow() {
        TimeBasedAggregationNode node =
                new TimeBasedAggregationNode(
                        new NumericalConstantNode(BigDecimal.TEN),
                        VirtualReadingType.from(
                                IntervalLength.DAY1,
                                MetricMultiplier.ZERO,
                                ReadingTypeUnit.WATT,
                                Accumulation.BULKQUANTITY,
                                Commodity.ELECTRICITY_PRIMARY_METERED));

        // Business method
        String expression = testInstance().visitTimeBasedAggregation(node);

        // Asserts
        assertThat(expression).isEqualTo("AVG(10)");
    }

    @Test
    public void testTimeBasedAggregationForVolume() {
        TimeBasedAggregationNode node =
                new TimeBasedAggregationNode(
                        new NumericalConstantNode(BigDecimal.TEN),
                        VirtualReadingType.from(
                                IntervalLength.DAY1,
                                MetricMultiplier.ZERO,
                                ReadingTypeUnit.WATTHOUR,
                                Accumulation.DELTADELTA,
                                Commodity.ELECTRICITY_PRIMARY_METERED));

        // Business method
        String expression = testInstance().visitTimeBasedAggregation(node);

        // Asserts
        assertThat(expression).isEqualTo("SUM(10)");
    }

    @Test
    public void testTimeBasedAggregationForTemperature() {
        TimeBasedAggregationNode node =
                new TimeBasedAggregationNode(
                        new NumericalConstantNode(BigDecimal.TEN),
                        VirtualReadingType.from(
                                IntervalLength.DAY1,
                                MetricMultiplier.ZERO,
                                ReadingTypeUnit.DEGREESCELSIUS,
                                Accumulation.BULKQUANTITY,
                                Commodity.WEATHER));

        // Business method
        String expression = testInstance().visitTimeBasedAggregation(node);

        // Asserts
        assertThat(expression).isEqualTo("AVG(10)");
    }

    @Test
    public void testTimeBasedAggregationForPressure() {
        TimeBasedAggregationNode node =
                new TimeBasedAggregationNode(
                        new NumericalConstantNode(BigDecimal.TEN),
                        VirtualReadingType.from(
                                IntervalLength.DAY1,
                                MetricMultiplier.ZERO,
                                ReadingTypeUnit.PASCAL,
                                Accumulation.BULKQUANTITY,
                                Commodity.WEATHER));

        // Business method
        String expression = testInstance().visitTimeBasedAggregation(node);

        // Asserts
        assertThat(expression).isEqualTo("AVG(10)");
    }

    @Test
    public void testRequirement() {
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("ExpressionNodeToSqlTest");
        when(requirement.getReadingType()).thenReturn(readingType);
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        ReadingType hourlyWattHours = this.mockHourlyWattHoursReadingType();
        when(deliverable.getReadingType()).thenReturn(hourlyWattHours);
        VirtualReadingTypeRequirement virtualRequirement = mock(VirtualReadingTypeRequirement.class);
        when(virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(requirement), eq(deliverable), any(VirtualReadingType.class)))
                .thenReturn(virtualRequirement);
        MeterActivationSet meterActivationSet = mock(MeterActivationSet.class);
        when(meterActivationSet.getRange()).thenReturn(Range.atLeast(Instant.EPOCH));
        VirtualRequirementNode node =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        virtualFactory,
                        requirement,
                        deliverable,
                        meterActivationSet);

        // Business method
        testInstance().visitVirtualRequirement(node);

        // Asserts
        verify(virtualRequirement).appendReferenceTo(any(SqlBuilder.class));
    }

    @Test
    public void testDeliverable() {
        ReadingType hourlyWattHours = this.mockHourlyWattHoursReadingType();
        ReadingTypeDeliverableForMeterActivationSet deliverable = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        when(deliverable.getReadingType()).thenReturn(hourlyWattHours);
        VirtualDeliverableNode node = new VirtualDeliverableNode(deliverable);

        // Business method
        testInstance().visitVirtualDeliverable(node);

        // Asserts
        verify(deliverable).appendReferenceTo(any(SqlBuilder.class), eq(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED)));
    }

    @Test
    public void testUnitConversion() {
        MeterActivationSet meterActivationSet = mock(MeterActivationSet.class);
        when(meterActivationSet.getRange()).thenReturn(Range.atLeast(Instant.EPOCH));
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingType hourWattHours = this.mockHourlyWattHoursReadingType();
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(hourWattHours);

        ReadingType ampereReadingType = this.mock15MinutesAmpereReadingType();
        FullySpecifiedReadingTypeRequirement currentRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(currentRequirement.getDimension()).thenReturn(Dimension.ELECTRIC_CURRENT);
        when(currentRequirement.getReadingType()).thenReturn(ampereReadingType);
        Channel ampereChannel = mock(Channel.class);
        when(ampereChannel.getMainReadingType()).thenReturn(ampereReadingType);
        when(meterActivationSet.getMatchingChannelsFor(currentRequirement)).thenReturn(Collections.singletonList(ampereChannel));
        VirtualReadingTypeRequirement virtualCurrentRequirement = mock(VirtualReadingTypeRequirement.class);
        when(virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(currentRequirement), eq(deliverable), any(VirtualReadingType.class)))
                .thenReturn(virtualCurrentRequirement);
        VirtualRequirementNode current =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        virtualFactory,
                        currentRequirement,
                        deliverable,
                        meterActivationSet);
        // Similate effect of InferReadingType
        current.setTargetReadingType(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.AMPERE, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_PRIMARY_METERED));

        ReadingType voltReadingType = this.mock15MinutesVoltReadingType();
        FullySpecifiedReadingTypeRequirement voltageRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(voltageRequirement.getDimension()).thenReturn(Dimension.ELECTRIC_POTENTIAL);
        when(voltageRequirement.getReadingType()).thenReturn(voltReadingType);
        Channel voltChannel = mock(Channel.class);
        when(voltChannel.getMainReadingType()).thenReturn(voltReadingType);
        when(meterActivationSet.getMatchingChannelsFor(voltageRequirement)).thenReturn(Collections.singletonList(voltChannel));
        VirtualReadingTypeRequirement virtualVoltageRequirement = mock(VirtualReadingTypeRequirement.class);
        when(virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(voltageRequirement), eq(deliverable), any(VirtualReadingType.class)))
                .thenReturn(virtualVoltageRequirement);
        VirtualRequirementNode voltage =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        virtualFactory,
                        voltageRequirement,
                        deliverable,
                        meterActivationSet);
        // Similate effect of InferReadingType
        voltage.setTargetReadingType(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.VOLT, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_PRIMARY_METERED));

        UnitConversionNode node = new UnitConversionNode(
                Operator.MULTIPLY.node(current, voltage),
                Dimension.POWER,
                VirtualReadingType.from(
                        IntervalLength.HOUR1,
                        MetricMultiplier.KILO,
                        ReadingTypeUnit.WATTHOUR,
                        Accumulation.DELTADELTA,
                        Commodity.ELECTRICITY_PRIMARY_METERED));

        // Business method
        String expression = testInstance().visitUnitConversion(node).replace(" ", "");

        // Asserts
        verify(virtualCurrentRequirement).appendSimpleReferenceTo(any(SqlBuilder.class));
        verify(virtualVoltageRequirement).appendSimpleReferenceTo(any(SqlBuilder.class));
        assertThat(expression).isEqualTo("(((*)/4)*0.001)"); // because the requirements are mocked, their appendSimpleReferenceTo method does not actually append anything to the StringBuilder
    }

    private ReadingType mockHourlyWattHoursReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private ReadingType mock15MinutesAmpereReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private ReadingType mock15MinutesVoltReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.VOLT);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private ExpressionNodeToString testInstance() {
        return new ExpressionNodeToString(Formula.Mode.AUTO);
    }

}