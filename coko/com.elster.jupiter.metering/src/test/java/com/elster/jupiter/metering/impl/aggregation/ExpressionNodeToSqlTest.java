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
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.units.Dimension;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
 * Tests the {@link ExpressionNodeToSql} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-14 (16:34)
 */
public class ExpressionNodeToSqlTest {

    @Test
    public void testNull() {
        // Business method
        String expression = testInstance().visitNull(new NullNode()).getText();

        // Asserts
        assertThat(expression).isEqualTo("null");
    }

    @Test
    public void testNumericalConstant() throws SQLException {
        // Business method
        SqlFragment sqlFragment = testInstance().visitConstant(new NumericalConstantNode(BigDecimal.TEN));

        // Asserts
        String expression = sqlFragment.getText();
        assertThat(expression).isEqualTo(" ? ");
        PreparedStatement statement = mock(PreparedStatement.class);
        sqlFragment.bind(statement, 1);
        verify(statement).setObject(1, BigDecimal.TEN);
    }

    @Test
    public void testStringConstant() throws SQLException {
        String expectedExpression = "Whatever";

        // Business method
        SqlFragment sqlFragment = testInstance().visitConstant(new StringConstantNode(expectedExpression));

        // Asserts
        String expression = sqlFragment.getText();
        assertThat(expression).isEqualTo(" ? ");
        PreparedStatement statement = mock(PreparedStatement.class);
        sqlFragment.bind(statement, 1);
        verify(statement).setObject(1, expectedExpression);
    }

    @Test
    public void testSqlFragment() {
        String sql = "select sysdate from dual";

        // Business method
        String expression = testInstance().visitSqlFragment(new SqlFragmentNode(sql)).getText();

        // Asserts
        assertThat(expression).isEqualTo(sql);
    }

    @Test
    public void testSqlPlusConstant() throws SQLException {
        String sqlText = "sequence.nextval";
        SqlFragmentNode sql = new SqlFragmentNode(sqlText);
        OperationNode operation = Operator.PLUS.node(sql, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        SqlFragment sqlFragment = testInstance().visitOperation(operation);

        // Asserts
        String expression = sqlFragment.getText().replace(" ", "");
        assertThat(expression).isEqualTo("(" + sqlText + "+?)");
        PreparedStatement statement = mock(PreparedStatement.class);
        sqlFragment.bind(statement, 1);
        verify(statement).setObject(1, BigDecimal.TEN);
    }

    @Test
    public void testSqlMinusConstant() throws SQLException {
        String sqlText = "sequence.nextval";
        SqlFragmentNode sql = new SqlFragmentNode(sqlText);
        OperationNode operation = Operator.MINUS.node(sql, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        SqlFragment sqlFragment = testInstance().visitOperation(operation);

        // Asserts
        String expression = sqlFragment.getText().replace(" ", "");
        assertThat(expression).isEqualTo("(" + sqlText + "-?)");
        PreparedStatement statement = mock(PreparedStatement.class);
        sqlFragment.bind(statement, 1);
        verify(statement).setObject(1, BigDecimal.TEN);
    }

    @Test
    public void testSqlTimesConstant() throws SQLException {
        String sqlText = "sequence.nextval";
        SqlFragmentNode sql = new SqlFragmentNode(sqlText);
        OperationNode operation = Operator.MULTIPLY.node(sql, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        SqlFragment sqlFragment = testInstance().visitOperation(operation);

        // Asserts
        String expression = sqlFragment.getText().replace(" ", "");
        assertThat(expression).isEqualTo("(" + sqlText + "*?)");
        PreparedStatement statement = mock(PreparedStatement.class);
        sqlFragment.bind(statement, 1);
        verify(statement).setObject(1, BigDecimal.TEN);
    }

    @Test
    public void testSqlDividedByConstant() throws SQLException {
        String sqlText = "var";
        SqlFragmentNode sql = new SqlFragmentNode(sqlText);
        OperationNode operation = Operator.DIVIDE.node(sql, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        SqlFragment sqlFragment = testInstance().visitOperation(operation);

        // Asserts
        String expression = sqlFragment.getText().replace(" ", "");
        assertThat(expression).isEqualTo("(" + sqlText + "/?)");
        PreparedStatement statement = mock(PreparedStatement.class);
        sqlFragment.bind(statement, 1);
        verify(statement).setObject(1, BigDecimal.TEN);
    }

    @Test
    public void testSafeDivision() throws SQLException {
        SqlFragmentNode sql1 = new SqlFragmentNode("var1");
        SqlFragmentNode sql2 = new SqlFragmentNode("var2");
        OperationNode operation = Operator.SAFE_DIVIDE.safeNode(sql1, sql2, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        SqlFragment sqlFragment = testInstance().visitOperation(operation);

        // Asserts
        String expression = sqlFragment.getText().replace(" ", "");
        assertThat(expression).isEqualTo("(var1/decode(var2,0,?,var2))");
        PreparedStatement statement = mock(PreparedStatement.class);
        sqlFragment.bind(statement, 1);
        verify(statement).setObject(1, BigDecimal.TEN);
    }

    @Test
    public void testFunctionCall() throws SQLException {
        SqlFragmentNode sql1 = new SqlFragmentNode("var1");
        SqlFragmentNode sql2 = new SqlFragmentNode("var2");
        FunctionCallNode node = new FunctionCallNode(Function.SUM, IntermediateDimension.of(Dimension.DIMENSIONLESS), sql1, sql2, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        SqlFragment sqlFragment = testInstance().visitFunctionCall(node);

        // Asserts
        String expression = sqlFragment.getText().replace(" ", "");
        assertThat(expression).isEqualTo("SUM(var1,var2,?)");
        PreparedStatement statement = mock(PreparedStatement.class);
        sqlFragment.bind(statement, 1);
        verify(statement).setObject(1, BigDecimal.TEN);
    }

    @Test
    public void testTimeBasedAggregationForFlow() throws SQLException {
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
        SqlFragment sqlFragment = testInstance().visitTimeBasedAggregation(node);

        // Asserts
        String expression = sqlFragment.getText().replace(" ", "");
        assertThat(expression).isEqualTo("AVG(?)");
        PreparedStatement statement = mock(PreparedStatement.class);
        sqlFragment.bind(statement, 1);
        verify(statement).setObject(1, BigDecimal.TEN);
    }

    @Test
    public void testTimeBasedAggregationForVolume() throws SQLException {
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
        SqlFragment sqlFragment = testInstance().visitTimeBasedAggregation(node);

        // Asserts
        String expression = sqlFragment.getText().replace(" ", "");
        assertThat(expression).isEqualTo("SUM(?)");
        PreparedStatement statement = mock(PreparedStatement.class);
        sqlFragment.bind(statement, 1);
        verify(statement).setObject(1, BigDecimal.TEN);
    }

    @Test
    public void testTimeBasedAggregationForTemperature() throws SQLException {
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
        SqlFragment sqlFragment = testInstance().visitTimeBasedAggregation(node);

        // Asserts
        String expression = sqlFragment.getText().replace(" ", "");
        assertThat(expression).isEqualTo("AVG(?)");
        PreparedStatement statement = mock(PreparedStatement.class);
        sqlFragment.bind(statement, 1);
        verify(statement).setObject(1, BigDecimal.TEN);
    }

    @Test
    public void testTimeBasedAggregationForPressure() throws SQLException {
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
        SqlFragment sqlFragment = testInstance().visitTimeBasedAggregation(node);

        // Asserts
        String expression = sqlFragment.getText().replace(" ", "");
        assertThat(expression).isEqualTo("AVG(?)");
        PreparedStatement statement = mock(PreparedStatement.class);
        sqlFragment.bind(statement, 1);
        verify(statement).setObject(1, BigDecimal.TEN);
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
        when(hourlyWattHours.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(hourlyWattHours.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        when(hourlyWattHours.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(hourlyWattHours.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(hourlyWattHours.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        ReadingTypeDeliverableForMeterActivationSet deliverable = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        when(deliverable.getReadingType()).thenReturn(hourlyWattHours);
        VirtualDeliverableNode node = new VirtualDeliverableNode(deliverable);

        // Business method
        testInstance().visitVirtualDeliverable(node);

        // Asserts
        verify(deliverable).appendReferenceTo(any(SqlBuilder.class), eq(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED)));
    }

    @Test
    public void testWattToKiloWattHourUnitConversion() {
        MeterActivationSet meterActivationSet = mock(MeterActivationSet.class);
        when(meterActivationSet.getRange()).thenReturn(Range.atLeast(Instant.EPOCH));
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingType hourlyWattHours = this.mockHourlyWattHoursReadingType();
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(hourlyWattHours);

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
        SqlFragment sqlFragment = testInstance().visitUnitConversion(node);

        // Asserts
        verify(virtualCurrentRequirement).appendSimpleReferenceTo(any(SqlBuilder.class));
        verify(virtualVoltageRequirement).appendSimpleReferenceTo(any(SqlBuilder.class));
        String expression = sqlFragment.getText().replace(" ", "");
        assertThat(expression).isEqualTo("(((*)/4)*0.001)"); // because the requirements are mocked, their appendSimpleReferenceTo method does not actually append anything to the SqlBuilder
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

    private ExpressionNodeToSql testInstance() {
        return new ExpressionNodeToSql(Formula.Mode.AUTO);
    }

}