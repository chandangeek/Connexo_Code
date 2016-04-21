package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.AggregationLevel;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.FunctionCallNodeImpl;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationServiceImpl;
import com.elster.jupiter.metering.impl.config.ServerFormulaBuilder;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.Range;

import java.math.BigDecimal;
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
 * Tests the {@link CopyAndVirtualizeReferences} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (16:32)
 */
@RunWith(MockitoJUnitRunner.class)
public class CopyAndVirtualizeReferencesTest {

    @Mock
    private VirtualFactory virtualFactory;
    @Mock
    private TemporalAmountFactory temporalAmountFactory;
    @Mock
    private ReadingTypeDeliverable deliverable;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ReadingTypeDeliverableForMeterActivationProvider readingTypeDeliverableForMeterActivationProvider;
    @Mock
    private ServerMeteringService meteringService;
    @Mock
    private EventService eventService;
    @Mock
    private UserService userService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private MetrologyConfiguration metrologyConfiguration;

    private ServerMetrologyConfigurationService metrologyConfigurationService;

    @Before
    public void initializeMocks() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(this.deliverable.getReadingType()).thenReturn(readingType);
        when(this.meteringService.getThesaurus()).thenReturn(this.thesaurus);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        this.metrologyConfigurationService = new MetrologyConfigurationServiceImpl(this.meteringService, this.userService);
    }

    @Test
    public void copyConstantNode() {
        CopyAndVirtualizeReferences visitor = getTestInstance();

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
        CopyAndVirtualizeReferences visitor = getTestInstance();
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
        CopyAndVirtualizeReferences visitor = getTestInstance();
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
        CopyAndVirtualizeReferences visitor = getTestInstance();

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
        CopyAndVirtualizeReferences visitor = getTestInstance();
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
        CopyAndVirtualizeReferences visitor = getTestInstance();
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
        CopyAndVirtualizeReferences visitor = getTestInstance();
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
        CopyAndVirtualizeReferences visitor = getTestInstance(Formula.Mode.EXPERT);
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
        CopyAndVirtualizeReferences visitor = getTestInstance();
        ReadingTypeDeliverable readingTypeDeliverable = mock(ReadingTypeDeliverable.class);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        ExpressionNode formulaPart =
                formulaBuilder.plus(
                    formulaBuilder.requirement(mock(ReadingTypeRequirement.class)),
                    formulaBuilder.maximum(Arrays.asList(
                            formulaBuilder.deliverable(readingTypeDeliverable),
                            formulaBuilder.constant(BigDecimal.TEN)))).create();
        com.elster.jupiter.metering.config.OperationNode node = (com.elster.jupiter.metering.config.OperationNode) formulaPart;
        ReadingTypeDeliverableForMeterActivation readingTypeDeliverableForMeterActivation =
                new ReadingTypeDeliverableForMeterActivation(
                        Formula.Mode.AUTO, readingTypeDeliverable,
                        this.meterActivation,
                        Range.all(),
                        1,
                        mock(ServerExpressionNode.class),
                        VirtualReadingType.from(readingType));
        when(this.readingTypeDeliverableForMeterActivationProvider.from(readingTypeDeliverable, this.meterActivation)).thenReturn(readingTypeDeliverableForMeterActivation);

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode copiedOperationNode = (OperationNode) copied;
        assertThat(copiedOperationNode.getOperator()).isEqualTo(Operator.PLUS);
        ServerExpressionNode leftOperand = copiedOperationNode.getLeftOperand();
        assertThat(leftOperand).isInstanceOf(VirtualRequirementNode.class);
        ServerExpressionNode rightOperand = copiedOperationNode.getRightOperand();
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
        CopyAndVirtualizeReferences visitor = getTestInstance();
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
        CopyAndVirtualizeReferences visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
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
        CopyAndVirtualizeReferences visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
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
        CopyAndVirtualizeReferences visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
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
        CopyAndVirtualizeReferences visitor = getTestInstance();
        ServerFormulaBuilder formulaBuilder = this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT);
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
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

    private CopyAndVirtualizeReferences getTestInstance() {
        return this.getTestInstance(Formula.Mode.AUTO);
    }

    private CopyAndVirtualizeReferences getTestInstance(Formula.Mode mode) {
        return new CopyAndVirtualizeReferences(mode, this.virtualFactory, this.readingTypeDeliverableForMeterActivationProvider, this.deliverable, this.meterActivation);
    }

}