package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.math.BigDecimal;

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
    public void testVariableReference() {
        String name = "variableName";

        // Business method
        String expression = testInstance().visitSqlFragment(new SqlFragmentNode(name));

        // Asserts
        assertThat(expression).isEqualTo(name);
    }

    @Test
    public void testVariablePlusConstant() {
        String variableName = "var";
        SqlFragmentNode variable = new SqlFragmentNode(variableName);
        OperationNode operation = new OperationNode(Operator.PLUS, variable, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        String expression = testInstance().visitOperation(operation);

        // Asserts
        assertThat(expression).isEqualTo("(" + variableName + " + 10)");
    }

    @Test
    public void testVariableMinusConstant() {
        String variableName = "var";
        SqlFragmentNode variable = new SqlFragmentNode(variableName);
        OperationNode operation = new OperationNode(Operator.MINUS, variable, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        String expression = testInstance().visitOperation(operation);

        // Asserts
        assertThat(expression).isEqualTo("(" + variableName + " - 10)");
    }

    @Test
    public void testVariableTimesConstant() {
        String variableName = "var";
        SqlFragmentNode variable = new SqlFragmentNode(variableName);
        OperationNode operation = new OperationNode(Operator.MULTIPLY, variable, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        String expression = testInstance().visitOperation(operation);

        // Asserts
        assertThat(expression).isEqualTo("(" + variableName + " * 10)");
    }

    @Test
    public void testVariableDividedByConstant() {
        String variableName = "var";
        SqlFragmentNode variable = new SqlFragmentNode(variableName);
        OperationNode operation = new OperationNode(Operator.DIVIDE, variable, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        String expression = testInstance().visitOperation(operation);

        // Asserts
        assertThat(expression).isEqualTo("(" + variableName + " / 10)");
    }

    @Test
    public void testSafeVariableDivision() {
        SqlFragmentNode variable1 = new SqlFragmentNode("var1");
        SqlFragmentNode variable2 = new SqlFragmentNode("var2");
        OperationNode operation = new OperationNode(Operator.SAFE_DIVIDE, variable1, variable2, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        String expression = testInstance().visitOperation(operation);

        // Asserts
        assertThat(expression).isEqualTo("(var1 / decode(var2, 0, 10, var2))");
    }

    @Test
    public void testFunctionCall() {
        SqlFragmentNode variable1 = new SqlFragmentNode("var1");
        SqlFragmentNode variable2 = new SqlFragmentNode("var2");
        FunctionCallNode node = new FunctionCallNode(Function.SUM, variable1, variable2, new NumericalConstantNode(BigDecimal.TEN));

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
                                Commodity.WEATHER));

        // Business method
        String expression = testInstance().visitTimeBasedAggregation(node);

        // Asserts
        assertThat(expression).isEqualTo("AVG(10)");
    }

    @Test
    public void testRequirement() {
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        ReadingType hourlyWattHours = this.mockHourlyWattHoursReadingType();
        when(deliverable.getReadingType()).thenReturn(hourlyWattHours);
        VirtualReadingTypeRequirement virtualRequirement = mock(VirtualReadingTypeRequirement.class);
        when(virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(requirement), eq(deliverable), any(VirtualReadingType.class)))
            .thenReturn(virtualRequirement);
        VirtualRequirementNode node =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        virtualFactory,
                        requirement,
                        deliverable,
                        mock(MeterActivation.class));

        // Business method
        testInstance().visitVirtualRequirement(node);

        // Asserts
        verify(virtualRequirement).appendReferenceTo(any(SqlBuilder.class));
    }

    @Test
    public void testDeliverable() {
        ReadingType hourlyWattHours = this.mockHourlyWattHoursReadingType();
        ReadingTypeDeliverableForMeterActivation deliverable = mock(ReadingTypeDeliverableForMeterActivation.class);
        when(deliverable.getReadingType()).thenReturn(hourlyWattHours);
        VirtualDeliverableNode node = new VirtualDeliverableNode(deliverable);

        // Business method
        testInstance().visitVirtualDeliverable(node);

        // Asserts
        verify(deliverable).appendReferenceTo(any(SqlBuilder.class), eq(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Commodity.ELECTRICITY_PRIMARY_METERED)));
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

    private ExpressionNodeToString testInstance() {
        return new ExpressionNodeToString(Formula.Mode.AUTO);
    }

}