/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.AggregationLevel;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.FunctionCallNodeImpl;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationServiceImpl;
import com.elster.jupiter.metering.impl.config.ServerFormulaBuilder;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.units.Dimension;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link Copy} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (16:32)
 */
@RunWith(MockitoJUnitRunner.class)
public class CopyTest {

    @Mock
    private VirtualFactory virtualFactory;
    @Mock
    private TemporalAmountFactory temporalAmountFactory;
    @Mock
    private ReadingTypeDeliverable deliverable;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MeterActivationSet meterActivationSet;
    @Mock
    private ReadingTypeDeliverableForMeterActivationSetProvider readingTypeDeliverableForMeterActivationSetProvider;
    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private UserService userService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private MetrologyConfiguration metrologyConfiguration;
    @Mock
    private ReadingType readingType;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private MeteringDataModelService meteringDataModelService;
    @Mock
    private ServerMeteringService meteringService;
    @Mock
    private Clock clock;

    private ServerMetrologyConfigurationService metrologyConfigurationService;

    @Before
    public void initializeMocks() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(this.deliverable.getReadingType()).thenReturn(readingType);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        this.metrologyConfigurationService = new MetrologyConfigurationServiceImpl(this.meteringDataModelService, this.dataModel, this.thesaurus);
        when(this.meterActivationSet.getRange()).thenReturn(Range.atLeast(Instant.EPOCH));
        when(this.readingType.getMRID()).thenReturn("CopyTest");
    }

    @Test
    public void copyNullNode() {
        Copy visitor = getTestInstance();

        com.elster.jupiter.metering.config.NullNode node = (com.elster.jupiter.metering.config.NullNode) this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.AUTO).nullValue().create();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(NullNode.class);
    }

    @Test
    public void copyConstantNode() {
        Copy visitor = getTestInstance();

        ConstantNode node = (ConstantNode) this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.AUTO).constant(BigDecimal.TEN).create();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) copied).getValue()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void copyMinimumFunctionCallNodeWithoutArguments() {
        Copy visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        FunctionCallNodeImpl node =
                (FunctionCallNodeImpl) formulaBuilder.minimum(Collections.emptyList()).create();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getFunction()).isEqualTo(Function.LEAST);
        assertThat(copiedFunctionCallNode.getArguments()).isEmpty();
    }

    @Test
    public void copyMaximumFunctionCallNodeWithoutArguments() {
        Copy visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        FunctionCallNodeImpl node =
                (FunctionCallNodeImpl) formulaBuilder.maximum(Collections.emptyList()).create();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getFunction()).isEqualTo(Function.GREATEST);
        assertThat(copiedFunctionCallNode.getArguments()).isEmpty();
    }

    @Test
    public void copyMinimumFunctionCallNodeWithOneArgument() {
        ServerFormulaBuilder formulaBuilder = metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        FunctionCallNodeImpl node =
                (FunctionCallNodeImpl) formulaBuilder.minimum(Collections.singletonList(formulaBuilder.constant(BigDecimal.TEN))).create();
        Copy visitor = getTestInstance();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getFunction()).isEqualTo(Function.LEAST);
        assertThat(copiedFunctionCallNode.getArguments()).hasSize(1);
        ServerExpressionNode onlyChild = copiedFunctionCallNode.getArguments().get(0);
        assertThat(onlyChild).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) onlyChild).getValue()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void copyMaximumFunctionCallNodeWithTwoArguments() {
        Copy visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        FunctionCallNodeImpl node =
                (FunctionCallNodeImpl) formulaBuilder
                        .maximum(Arrays.asList(
                                formulaBuilder.constant(BigDecimal.TEN),
                                formulaBuilder.constant(BigDecimal.ZERO))).create();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getFunction()).isEqualTo(Function.GREATEST);
        assertThat(copiedFunctionCallNode.getArguments()).hasSize(2);
        ServerExpressionNode firstArgument = copiedFunctionCallNode.getArguments().get(0);
        assertThat(firstArgument).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) firstArgument).getValue()).isEqualTo(BigDecimal.TEN);
        ServerExpressionNode secondArgument = copiedFunctionCallNode.getArguments().get(1);
        assertThat(secondArgument).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) secondArgument).getValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void copyFunctionCallNodeWithThreeArguments() {
        Copy visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        FunctionCallNodeImpl node =
                (FunctionCallNodeImpl) formulaBuilder
                        .maximum(Arrays.asList(
                                formulaBuilder.constant(BigDecimal.TEN),
                                formulaBuilder.constant(BigDecimal.valueOf(1000L)),
                                formulaBuilder.constant(BigDecimal.ONE))).create();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        com.elster.jupiter.metering.impl.aggregation.FunctionCallNode copiedFunctionCallNode = (com.elster.jupiter.metering.impl.aggregation.FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getFunction()).isEqualTo(Function.GREATEST);
        assertThat(copiedFunctionCallNode.getArguments()).hasSize(3);
        ServerExpressionNode firstArgument = copiedFunctionCallNode.getArguments().get(0);
        assertThat(firstArgument).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) firstArgument).getValue()).isEqualTo(BigDecimal.TEN);
        ServerExpressionNode secondArgument = copiedFunctionCallNode.getArguments().get(1);
        assertThat(secondArgument).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) secondArgument).getValue()).isEqualTo(BigDecimal.valueOf(1000L));
        ServerExpressionNode thirdArgument = copiedFunctionCallNode.getArguments().get(2);
        assertThat(thirdArgument).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) thirdArgument).getValue()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    public void copyPlusOperation() {
        Copy visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.AUTO);
        ExpressionNode formulaPart = formulaBuilder.plus(
                formulaBuilder.constant(BigDecimal.TEN),
                formulaBuilder.constant(BigDecimal.ZERO)).create();
        com.elster.jupiter.metering.config.OperationNode node = (com.elster.jupiter.metering.config.OperationNode) formulaPart;

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode copiedOperationNode = (OperationNode) copied;
        assertThat(copiedOperationNode.getOperator()).isEqualTo(Operator.PLUS);
        ServerExpressionNode leftOperand = copiedOperationNode.getLeftOperand();
        assertThat(leftOperand).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) leftOperand).getValue()).isEqualTo(BigDecimal.TEN);
        ServerExpressionNode rightOperand = copiedOperationNode.getRightOperand();
        assertThat(rightOperand).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) rightOperand).getValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void copyAggregationNode() {
        Copy visitor = getTestInstance(Formula.Mode.EXPERT);
        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        FunctionCallNodeImpl node =
                (FunctionCallNodeImpl) formulaBuilder.aggregate(
                        formulaBuilder.plus(
                                formulaBuilder.constant(BigDecimal.TEN),
                                formulaBuilder.constant(BigDecimal.ZERO))).create();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(TimeBasedAggregationNode.class);
        TimeBasedAggregationNode copiedTimeBasedAggregationNode = (TimeBasedAggregationNode) copied;
        assertThat(copiedTimeBasedAggregationNode.getFunction()).isEqualTo(AggregationFunction.SUM);
        ServerExpressionNode aggregatedExpression = copiedTimeBasedAggregationNode.getAggregatedExpression();
        assertThat(aggregatedExpression).isNotNull();
    }

    @Test
    public void copyComplexTree() {
        Copy visitor = getTestInstance();
        ReadingTypeDeliverable readingTypeDeliverable = mock(ReadingTypeDeliverable.class);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingTypeDeliverable.getReadingType()).thenReturn(readingType);

        ServerExpressionNode serverExpressionNode = mock(ServerExpressionNode.class);
        when(serverExpressionNode.accept(any(RequirementsFromExpressionNode.class))).thenReturn(new ArrayList<>());

        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);
        Dimension dimension = readingType.getUnit().getUnit().getDimension();
        when(requirement.getDimension()).thenReturn(dimension);
        when(requirement.getReadingType()).thenReturn(readingType);
        Channel channel = mock(Channel.class);
        when(channel.getMainReadingType()).thenReturn(readingType);
        when(this.meterActivationSet.getMatchingChannelsFor(requirement)).thenReturn(Collections.singletonList(channel));
        ExpressionNode formulaPart =
                formulaBuilder.plus(
                        formulaBuilder.requirement(requirement),
                        formulaBuilder.maximum(Arrays.asList(
                                formulaBuilder.deliverable(readingTypeDeliverable),
                                formulaBuilder.constant(BigDecimal.TEN)))).create();
        com.elster.jupiter.metering.config.OperationNode node = (com.elster.jupiter.metering.config.OperationNode) formulaPart;
        ReadingTypeDeliverableForMeterActivationSet readingTypeDeliverableForMeterActivationSet =
                new ReadingTypeDeliverableForMeterActivationSet(
                        this.meteringService,
                        Formula.Mode.AUTO,
                        readingTypeDeliverable,
                        this.meterActivationSet,
                        1,
                        serverExpressionNode,
                        VirtualReadingType.from(readingType));
        when(this.readingTypeDeliverableForMeterActivationSetProvider.from(readingTypeDeliverable, this.meterActivationSet)).thenReturn(readingTypeDeliverableForMeterActivationSet);

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode operationNode = (OperationNode) copied;
        assertThat(operationNode.getOperator()).isEqualTo(Operator.PLUS);
        ServerExpressionNode leftOperand = operationNode.getLeftOperand();
        assertThat(leftOperand).isInstanceOf(VirtualRequirementNode.class);
        ServerExpressionNode rightOperand = operationNode.getRightOperand();
        assertThat(rightOperand).isInstanceOf(com.elster.jupiter.metering.impl.aggregation.FunctionCallNode.class);
        com.elster.jupiter.metering.impl.aggregation.FunctionCallNode functionCallNode = (com.elster.jupiter.metering.impl.aggregation.FunctionCallNode) rightOperand;
        List<ServerExpressionNode> maxFunctionArguments = functionCallNode.getArguments();
        assertThat(maxFunctionArguments).hasSize(2);
        assertThat(maxFunctionArguments.get(0)).isInstanceOf(VirtualDeliverableNode.class);
        assertThat(maxFunctionArguments.get(1)).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) maxFunctionArguments.get(1)).getValue()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void copyMinusOperation() {
        Copy visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        ExpressionNode node =
                formulaBuilder.minus(
                        formulaBuilder.constant(BigDecimal.TEN),
                        formulaBuilder.constant(BigDecimal.ZERO)).create();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode copiedOperationNode = (OperationNode) copied;
        assertThat(copiedOperationNode.getOperator()).isEqualTo(Operator.MINUS);
        ServerExpressionNode leftOperand = copiedOperationNode.getLeftOperand();
        assertThat(leftOperand).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) leftOperand).getValue()).isEqualTo(BigDecimal.TEN);
        ServerExpressionNode rightOperand = copiedOperationNode.getRightOperand();
        assertThat(rightOperand).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) rightOperand).getValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void copyMinimumAggregation() {
        Copy visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirement.getReadingType()).thenReturn(this.readingType);
        ExpressionNode node =
                formulaBuilder.minimum(
                        AggregationLevel.DAY,
                        Collections.singletonList(formulaBuilder.requirement(requirement))).create();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(TimeBasedAggregationNode.class);
        TimeBasedAggregationNode copiedAggregationNode = (TimeBasedAggregationNode) copied;
        assertThat(copiedAggregationNode.getFunction()).isEqualTo(AggregationFunction.MIN);
        assertThat(copiedAggregationNode.getIntervalLength()).isEqualTo(IntervalLength.DAY1);
        ServerExpressionNode operand = copiedAggregationNode.getAggregatedExpression();
        assertThat(operand).isInstanceOf(VirtualRequirementNode.class);
        assertThat(((VirtualRequirementNode) operand).getRequirement()).isEqualTo(requirement);
    }

    @Test
    public void copyMaximumAggregation() {
        Copy visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirement.getReadingType()).thenReturn(this.readingType);
        ExpressionNode node =
                formulaBuilder.maximum(
                        AggregationLevel.WEEK,
                        Collections.singletonList(formulaBuilder.requirement(requirement))).create();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(TimeBasedAggregationNode.class);
        TimeBasedAggregationNode copiedAggregationNode = (TimeBasedAggregationNode) copied;
        assertThat(copiedAggregationNode.getFunction()).isEqualTo(AggregationFunction.MAX);
        assertThat(copiedAggregationNode.getIntervalLength()).isEqualTo(IntervalLength.WEEK1);
        ServerExpressionNode operand = copiedAggregationNode.getAggregatedExpression();
        assertThat(operand).isInstanceOf(VirtualRequirementNode.class);
        assertThat(((VirtualRequirementNode) operand).getRequirement()).isEqualTo(requirement);
    }

    @Test
    public void copyAverageAggregation() {
        Copy visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirement.getReadingType()).thenReturn(this.readingType);
        ExpressionNode node =
                formulaBuilder.average(
                        AggregationLevel.MONTH,
                        Collections.singletonList(formulaBuilder.requirement(requirement))).create();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(TimeBasedAggregationNode.class);
        TimeBasedAggregationNode copiedAggregationNode = (TimeBasedAggregationNode) copied;
        assertThat(copiedAggregationNode.getFunction()).isEqualTo(AggregationFunction.AVG);
        assertThat(copiedAggregationNode.getIntervalLength()).isEqualTo(IntervalLength.MONTH1);
        ServerExpressionNode operand = copiedAggregationNode.getAggregatedExpression();
        assertThat(operand).isInstanceOf(VirtualRequirementNode.class);
        assertThat(((VirtualRequirementNode) operand).getRequirement()).isEqualTo(requirement);
    }

    @Test
    public void copySumAggregation() {
        Copy visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirement.getReadingType()).thenReturn(this.readingType);
        ExpressionNode node =
                formulaBuilder.sum(
                        AggregationLevel.YEAR,
                        Collections.singletonList(formulaBuilder.requirement(requirement))).create();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(TimeBasedAggregationNode.class);
        TimeBasedAggregationNode copiedAggregationNode = (TimeBasedAggregationNode) copied;
        assertThat(copiedAggregationNode.getFunction()).isEqualTo(AggregationFunction.SUM);
        assertThat(copiedAggregationNode.getIntervalLength()).isEqualTo(IntervalLength.YEAR1);
        ServerExpressionNode operand = copiedAggregationNode.getAggregatedExpression();
        assertThat(operand).isInstanceOf(VirtualRequirementNode.class);
        assertThat(((VirtualRequirementNode) operand).getRequirement()).isEqualTo(requirement);
    }

    @Test
    public void copySafeDivide() {
        Copy visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        FullySpecifiedReadingTypeRequirement requirement1 = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirement1.getDimension()).thenReturn(Dimension.ENERGY);
        when(requirement1.getReadingType()).thenReturn(this.readingType);
        FullySpecifiedReadingTypeRequirement requirement2 = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirement2.getDimension()).thenReturn(Dimension.ENERGY);
        when(requirement2.getReadingType()).thenReturn(this.readingType);
        ExpressionNode node =
                formulaBuilder.safeDivide(
                        formulaBuilder.requirement(requirement1),
                        formulaBuilder.requirement(requirement2),
                        formulaBuilder.constant(BigDecimal.ONE)).create();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode copiedNode = (OperationNode) copied;
        assertThat(copiedNode.getOperator()).isEqualTo(Operator.SAFE_DIVIDE);
        ServerExpressionNode safeDivisorNode = copiedNode.getSafeDivisor();
        assertThat(safeDivisorNode).isInstanceOf(NumericalConstantNode.class);
    }

    @Test
    public void copyCustomProperty() {
        Copy visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.AUTO);
        PropertySpec propertySpec = mock(PropertySpec.class);
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        ExpressionNode node = formulaBuilder.property(registeredCustomPropertySet, propertySpec).create();

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(CustomPropertyNode.class);
        CustomPropertyNode customPropertyNode = (CustomPropertyNode) copied;
        assertThat(customPropertyNode.getCustomPropertySet()).isEqualTo(customPropertySet);
    }

    private Copy getTestInstance() {
        return this.getTestInstance(Formula.Mode.AUTO);
    }

    private Copy getTestInstance(Formula.Mode mode) {
        return new Copy(mode, this.virtualFactory, this.customPropertySetService, this.readingTypeDeliverableForMeterActivationSetProvider, this.deliverable, this.usagePoint, this.meterActivationSet);
    }

}