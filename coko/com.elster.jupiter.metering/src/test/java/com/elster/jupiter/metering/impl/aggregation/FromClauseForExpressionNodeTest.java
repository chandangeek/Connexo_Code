/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.units.Dimension;

import com.google.common.collect.Range;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link FromClauseForExpressionNode} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-01 (08:51)
 */
@RunWith(MockitoJUnitRunner.class)
public class FromClauseForExpressionNodeTest {

    @Mock
    private DataSourceTable defaultSource;
    @Mock
    private CustomPropertySetService customPropertySpecService;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private RegisteredCustomPropertySet registerCustomPropertySet;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MeterActivationSet meterActivationSet;
    @Mock
    private VirtualFactory virtualFactory;
    @Mock
    private FullySpecifiedReadingTypeRequirement requirement;
    @Mock
    private ReadingTypeDeliverable deliverable;
    @Mock
    private ReadingType readingType;
    @Mock
    private ReadingTypeDeliverableForMeterActivationSet deliverableForMeterActivationSet;

    @Before
    public void initializeMocks() {
        when(this.readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(this.readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(this.readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(this.readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(this.readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);

        when(this.deliverable.getReadingType()).thenReturn(this.readingType);
        when(this.deliverableForMeterActivationSet.getReadingType()).thenReturn(this.readingType);
        when(this.requirement.getReadingType()).thenReturn(this.readingType);

        when(this.meterActivationSet.getRange()).thenReturn(Range.all());
    }

    @Test
    public void numericalConstantNodeUsesDefault() {
        FromClauseForExpressionNode testInstance = this.getTestInstance();
        ServerExpressionNode node = new NumericalConstantNode(BigDecimal.ONE);

        // Business method
        node.accept(testInstance);

        // Asserts
        assertThat(testInstance.getSource()).isEqualTo(this.defaultSource);
    }

    @Test
    public void stringConstantNodeUsesDefault() {
        FromClauseForExpressionNode testInstance = this.getTestInstance();
        ServerExpressionNode node = new StringConstantNode("stringConstantNodeUsesDefault");

        // Business method
        node.accept(testInstance);

        // Asserts
        assertThat(testInstance.getSource()).isEqualTo(this.defaultSource);
    }

    @Test
    public void nullNodeUsesDefault() {
        FromClauseForExpressionNode testInstance = this.getTestInstance();
        ServerExpressionNode node = new NullNode();

        // Business method
        node.accept(testInstance);

        // Asserts
        assertThat(testInstance.getSource()).isEqualTo(this.defaultSource);
    }

    @Test
    public void sqlFragmentNodeUsesDefault() {
        FromClauseForExpressionNode testInstance = this.getTestInstance();
        ServerExpressionNode node = new SqlFragmentNode("sequence.nextval");

        // Business method
        node.accept(testInstance);

        // Asserts
        assertThat(testInstance.getSource()).isEqualTo(this.defaultSource);
    }

    @Test
    public void customPropertyNodeOnly() {
        FromClauseForExpressionNode testInstance = this.getTestInstance();
        when(this.meterActivationSet.sequenceNumber()).thenReturn(1);
        when(this.registerCustomPropertySet.getId()).thenReturn(97L);
        when(this.propertySpec.getName()).thenReturn("custom");
        when(this.propertySpec.getDisplayName()).thenReturn("Custom");
        ServerExpressionNode node = new CustomPropertyNode(this.customPropertySpecService, this.propertySpec, this.registerCustomPropertySet, this.usagePoint, this.meterActivationSet);

        // Business method
        node.accept(testInstance);

        // Asserts
        verify(this.propertySpec).getName();
        verify(this.registerCustomPropertySet).getId();
        verify(this.meterActivationSet).sequenceNumber();
        DataSourceTable source = testInstance.getSource();
        assertThat(source).isNotEqualTo(this.defaultSource);
        assertThat(source.getName()).startsWith("cps");
    }

    @Test
    public void requirementNodeOnly() {
        FromClauseForExpressionNode testInstance = this.getTestInstance();
        VirtualReadingTypeRequirement virtualReadingTypeRequirement = mock(VirtualReadingTypeRequirement.class);
        String expectedSqlName = "requirementNodeOnly";
        when(virtualReadingTypeRequirement.sqlName()).thenReturn(expectedSqlName);
        when(this.virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(this.requirement), eq(this.deliverable), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeRequirement);
        ServerExpressionNode node = new VirtualRequirementNode(Formula.Mode.AUTO, this.virtualFactory, this.requirement, this.deliverable, this.meterActivationSet);

        // Business method
        node.accept(testInstance);

        // Asserts
        verify(virtualReadingTypeRequirement).sqlName();
        DataSourceTable source = testInstance.getSource();
        assertThat(source).isNotEqualTo(this.defaultSource);
        assertThat(source.getName()).isEqualTo(expectedSqlName);
    }

    @Test
    public void deliverableNodeOnly() {
        FromClauseForExpressionNode testInstance = this.getTestInstance();
        String expectedSqlName = "deliverableNodeOnly";
        when(this.deliverableForMeterActivationSet.sqlName()).thenReturn(expectedSqlName);
        ServerExpressionNode node = new VirtualDeliverableNode(this.deliverableForMeterActivationSet);

        // Business method
        node.accept(testInstance);

        // Asserts
        verify(this.deliverableForMeterActivationSet).sqlName();
        DataSourceTable source = testInstance.getSource();
        assertThat(source).isNotEqualTo(this.defaultSource);
        assertThat(source.getName()).isEqualTo(expectedSqlName);
    }

    @Test
    public void firstTimeSeriesWinsInOperation() {
        FromClauseForExpressionNode testInstance = this.getTestInstance();

        when(this.deliverableForMeterActivationSet.sqlName()).thenReturn("deliverable");
        ServerExpressionNode deliverableNode = new VirtualDeliverableNode(this.deliverableForMeterActivationSet);
        VirtualReadingTypeRequirement virtualReadingTypeRequirement = mock(VirtualReadingTypeRequirement.class);
        when(virtualReadingTypeRequirement.sqlName()).thenReturn("requirement");
        when(this.virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(this.requirement), eq(this.deliverable), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeRequirement);
        ServerExpressionNode requirementNode = new VirtualRequirementNode(Formula.Mode.AUTO, this.virtualFactory, this.requirement, this.deliverable, this.meterActivationSet);
        ServerExpressionNode node = new OperationNode(Operator.PLUS, Dimension.DIMENSIONLESS, requirementNode, deliverableNode);

        // Business method
        node.accept(testInstance);

        // Asserts
        DataSourceTable source = testInstance.getSource();
        assertThat(source).isNotEqualTo(this.defaultSource);
        assertThat(source.getName()).isEqualTo(virtualReadingTypeRequirement.sqlName());
    }

    @Test
    public void firstTimeSeriesWinsInFunctionCall() {
        FromClauseForExpressionNode testInstance = this.getTestInstance();

        when(this.deliverableForMeterActivationSet.sqlName()).thenReturn("deliverable");
        ServerExpressionNode deliverableNode = new VirtualDeliverableNode(this.deliverableForMeterActivationSet);
        VirtualReadingTypeRequirement virtualReadingTypeRequirement = mock(VirtualReadingTypeRequirement.class);
        when(virtualReadingTypeRequirement.sqlName()).thenReturn("requirement");
        when(this.virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(this.requirement), eq(this.deliverable), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeRequirement);
        ServerExpressionNode requirementNode = new VirtualRequirementNode(Formula.Mode.AUTO, this.virtualFactory, this.requirement, this.deliverable, this.meterActivationSet);
        ServerExpressionNode node = new FunctionCallNode(Function.GREATEST, IntermediateDimension.of(Dimension.DIMENSIONLESS), requirementNode, deliverableNode);

        // Business method
        node.accept(testInstance);

        // Asserts
        DataSourceTable source = testInstance.getSource();
        assertThat(source).isNotEqualTo(this.defaultSource);
        assertThat(source.getName()).isEqualTo(virtualReadingTypeRequirement.sqlName());
    }

    @Test
    public void timeSeriesWinsFromCustomPropertyInOperation() {
        FromClauseForExpressionNode testInstance = this.getTestInstance();
        String expectedSqlName = "timeSeriesWinsFromCustomProperty";
        when(this.deliverableForMeterActivationSet.sqlName()).thenReturn(expectedSqlName);
        ServerExpressionNode deliverableNode = new VirtualDeliverableNode(this.deliverableForMeterActivationSet);
        when(this.meterActivationSet.sequenceNumber()).thenReturn(1);
        when(this.registerCustomPropertySet.getId()).thenReturn(97L);
        when(this.propertySpec.getName()).thenReturn("custom");
        when(this.propertySpec.getDisplayName()).thenReturn("Custom");
        ServerExpressionNode customPropertyNode = new CustomPropertyNode(this.customPropertySpecService, this.propertySpec, this.registerCustomPropertySet, this.usagePoint, this.meterActivationSet);
        ServerExpressionNode node = new OperationNode(Operator.PLUS, Dimension.DIMENSIONLESS, customPropertyNode, deliverableNode);

        // Business method
        node.accept(testInstance);

        // Asserts
        DataSourceTable source = testInstance.getSource();
        assertThat(source).isNotEqualTo(this.defaultSource);
        assertThat(source.getName()).isEqualTo(expectedSqlName);
    }

    @Test
    public void timeSeriesWinsFromCustomPropertyInFunctionCall() {
        FromClauseForExpressionNode testInstance = this.getTestInstance();
        String expectedSqlName = "timeSeriesWinsFromCustomProperty";
        when(this.deliverableForMeterActivationSet.sqlName()).thenReturn(expectedSqlName);
        ServerExpressionNode deliverableNode = new VirtualDeliverableNode(this.deliverableForMeterActivationSet);
        when(this.meterActivationSet.sequenceNumber()).thenReturn(1);
        when(this.registerCustomPropertySet.getId()).thenReturn(97L);
        when(this.propertySpec.getName()).thenReturn("custom");
        when(this.propertySpec.getDisplayName()).thenReturn("Custom");
        ServerExpressionNode customPropertyNode = new CustomPropertyNode(this.customPropertySpecService, this.propertySpec, this.registerCustomPropertySet, this.usagePoint, this.meterActivationSet);
        ServerExpressionNode node = new FunctionCallNode(Function.GREATEST, IntermediateDimension.of(Dimension.DIMENSIONLESS), customPropertyNode, deliverableNode);

        // Business method
        node.accept(testInstance);

        // Asserts
        DataSourceTable source = testInstance.getSource();
        assertThat(source).isNotEqualTo(this.defaultSource);
        assertThat(source.getName()).isEqualTo(expectedSqlName);
    }

    @Test
    public void timeBasedAggregationNodeDelegatesToActualExpression() {
        FromClauseForExpressionNode testInstance = this.getTestInstance();
        ServerExpressionNode expression = mock(ServerExpressionNode.class);
        ServerExpressionNode node = new TimeBasedAggregationNode(expression, AggregationFunction.MAX, IntervalLength.HOUR1);

        // Business method
        node.accept(testInstance);

        // Asserts
        verify(expression).accept(testInstance);
    }

    @Test
    public void unitConversionNodeDelegatesToActualExpression() {
        FromClauseForExpressionNode testInstance = this.getTestInstance();
        ServerExpressionNode expression = mock(ServerExpressionNode.class);
        ServerExpressionNode node = new UnitConversionNode(expression, VirtualReadingType.dontCare(), VirtualReadingType.dontCare());

        // Business method
        node.accept(testInstance);

        // Asserts
        verify(expression).accept(testInstance);
    }

    private FromClauseForExpressionNode getTestInstance() {
        return new FromClauseForExpressionNode(this.defaultSource);
    }

}