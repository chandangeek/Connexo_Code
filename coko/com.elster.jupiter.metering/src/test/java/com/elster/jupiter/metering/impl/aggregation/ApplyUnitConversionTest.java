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
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.units.Dimension;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

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
 * Tests the {@link ApplyUnitConversion} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplyUnitConversionTest {

    @Mock
    private VirtualFactory virtualFactory;
    @Mock
    private TemporalAmountFactory temporalAmountFactory;
    @Mock
    private ReadingTypeDeliverable deliverable;
    @Mock
    private ReadingType deliverableReadingType;
    @Mock
    private MeterActivationSet meterActivationSet;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private ReadingTypeDeliverableForMeterActivationSetProvider readingTypeDeliverableForMeterActivationSetProvider;
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

    @Before
    public void initializeMocks() {
        when(this.deliverableReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(this.deliverableReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(this.deliverableReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(this.deliverableReadingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(this.deliverable.getReadingType()).thenReturn(this.deliverableReadingType);
        when(this.meteringService.getThesaurus()).thenReturn(this.thesaurus);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        when(this.meterActivationSet.getRange()).thenReturn(Range.atLeast(Instant.EPOCH));
    }

    @Test
    public void copyCurrentTimesVoltToDaily_kWh() {
        when(this.deliverableReadingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(this.deliverableReadingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(this.deliverableReadingType.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);
        when(this.deliverableReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);

        ApplyUnitConversion visitor = getDaily_kWhTestInstance();
        ReadingType ampereReadingType = mock(ReadingType.class);
        when(ampereReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(ampereReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(ampereReadingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(ampereReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        FullySpecifiedReadingTypeRequirement ampereRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(ampereRequirement.getDimension()).thenReturn(ReadingTypeUnit.AMPERE.getUnit().getDimension());
        when(ampereRequirement.getReadingType()).thenReturn(ampereReadingType);
        Channel chn1 = mock(Channel.class);
        when(chn1.getMainReadingType()).thenReturn(ampereReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(ampereRequirement)).thenReturn(Collections.singletonList(chn1));
        VirtualReadingTypeRequirement ampereVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType ampereVirtualReadingType = VirtualReadingType.from(ampereReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        ampereRequirement,
                        this.deliverable,
                        ampereVirtualReadingType))
                .thenReturn(ampereVirtualRequirement);
        when(ampereVirtualRequirement.getSourceReadingType()).thenReturn(ampereVirtualReadingType);

        ReadingType voltReadingType = mock(ReadingType.class);
        when(voltReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(voltReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(voltReadingType.getUnit()).thenReturn(ReadingTypeUnit.VOLT);
        when(voltReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        FullySpecifiedReadingTypeRequirement voltRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(voltRequirement.getDimension()).thenReturn(ReadingTypeUnit.VOLT.getUnit().getDimension());
        when(voltRequirement.getReadingType()).thenReturn(voltReadingType);
        Channel chn2 = mock(Channel.class);
        when(chn2.getMainReadingType()).thenReturn(voltReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(voltRequirement)).thenReturn(Collections.singletonList(chn2));
        VirtualReadingTypeRequirement voltVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType voltVirtualReadingType = VirtualReadingType.from(voltReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        voltRequirement,
                        this.deliverable,
                        voltVirtualReadingType))
                .thenReturn(voltVirtualRequirement);
        when(voltVirtualRequirement.getSourceReadingType()).thenReturn(voltVirtualReadingType);

        OperationNode node =
                Operator.MULTIPLY.node(
                        this.toRequirementNode(ampereRequirement),
                        this.toRequirementNode(voltRequirement));

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(UnitConversionNode.class);
        UnitConversionNode unitConversionNode = (UnitConversionNode) copied;
        OperationNode copiedNode = (OperationNode) unitConversionNode.getExpressionNode();
        assertThat(copiedNode.getOperator()).isEqualTo(Operator.MULTIPLY);
    }

    @Test
    public void copyCurrentTimesVoltTo15min_kWh() {
        ApplyUnitConversion visitor = get15minWhTestInstance();
        ReadingType ampereReadingType = mock(ReadingType.class);
        when(ampereReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(ampereReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(ampereReadingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(ampereReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        FullySpecifiedReadingTypeRequirement ampereRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(ampereRequirement.getDimension()).thenReturn(ReadingTypeUnit.AMPERE.getUnit().getDimension());
        when(ampereRequirement.getReadingType()).thenReturn(ampereReadingType);
        Channel chn1 = mock(Channel.class);
        when(chn1.getMainReadingType()).thenReturn(ampereReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(ampereRequirement)).thenReturn(Collections.singletonList(chn1));
        VirtualReadingTypeRequirement ampereVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType ampereVirtualReadingType = VirtualReadingType.from(ampereReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        ampereRequirement,
                        this.deliverable,
                        ampereVirtualReadingType))
                .thenReturn(ampereVirtualRequirement);
        when(ampereVirtualRequirement.getSourceReadingType()).thenReturn(ampereVirtualReadingType);

        ReadingType voltReadingType = mock(ReadingType.class);
        when(voltReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(voltReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(voltReadingType.getUnit()).thenReturn(ReadingTypeUnit.VOLT);
        when(voltReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        FullySpecifiedReadingTypeRequirement voltRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(voltRequirement.getDimension()).thenReturn(ReadingTypeUnit.VOLT.getUnit().getDimension());
        when(voltRequirement.getReadingType()).thenReturn(voltReadingType);
        Channel chn2 = mock(Channel.class);
        when(chn2.getMainReadingType()).thenReturn(voltReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(voltRequirement)).thenReturn(Collections.singletonList(chn2));
        VirtualReadingTypeRequirement voltVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType voltVirtualReadingType = VirtualReadingType.from(voltReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        voltRequirement,
                        this.deliverable,
                        voltVirtualReadingType))
                .thenReturn(voltVirtualRequirement);
        when(voltVirtualRequirement.getSourceReadingType()).thenReturn(voltVirtualReadingType);

        OperationNode node =
                Operator.MULTIPLY.node(
                        this.toRequirementNode(ampereRequirement),
                        this.toRequirementNode(voltRequirement));

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode copiedNode = (OperationNode) copied;
        assertThat(copiedNode.getOperator()).isEqualTo(Operator.MULTIPLY);
    }

    @Test
    public void currentTimesVoltPlusCurrentTimesVoltTo15min_kWh() {
        ApplyUnitConversion visitor = get15minWhTestInstance();
        ReadingType ampereReadingType = mock(ReadingType.class);
        when(ampereReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(ampereReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(ampereReadingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(ampereReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        FullySpecifiedReadingTypeRequirement ampereRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(ampereRequirement.getDimension()).thenReturn(ReadingTypeUnit.AMPERE.getUnit().getDimension());
        when(ampereRequirement.getReadingType()).thenReturn(ampereReadingType);
        Channel chn1 = mock(Channel.class);
        when(chn1.getMainReadingType()).thenReturn(ampereReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(ampereRequirement)).thenReturn(Collections.singletonList(chn1));
        VirtualReadingTypeRequirement ampereVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType ampereVirtualReadingType = VirtualReadingType.from(ampereReadingType);
        when(ampereVirtualRequirement.getSourceReadingType()).thenReturn(ampereVirtualReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        ampereRequirement,
                        this.deliverable,
                        ampereVirtualReadingType))
                .thenReturn(ampereVirtualRequirement);
        when(ampereVirtualRequirement.getSourceReadingType()).thenReturn(ampereVirtualReadingType);

        ReadingType voltReadingType = mock(ReadingType.class);
        when(voltReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(voltReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(voltReadingType.getUnit()).thenReturn(ReadingTypeUnit.VOLT);
        when(voltReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        Channel chn2 = mock(Channel.class);
        when(chn2.getMainReadingType()).thenReturn(voltReadingType);
        FullySpecifiedReadingTypeRequirement voltRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(voltRequirement.getDimension()).thenReturn(ReadingTypeUnit.VOLT.getUnit().getDimension());
        when(voltRequirement.getReadingType()).thenReturn(voltReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(voltRequirement)).thenReturn(Collections.singletonList(chn2));
        VirtualReadingTypeRequirement voltVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType voltVirtualReadingType = VirtualReadingType.from(voltReadingType);
        when(voltVirtualRequirement.getSourceReadingType()).thenReturn(voltVirtualReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        voltRequirement,
                        this.deliverable,
                        voltVirtualReadingType))
                .thenReturn(voltVirtualRequirement);
        when(voltVirtualRequirement.getSourceReadingType()).thenReturn(voltVirtualReadingType);

        ServerExpressionNode node =
                Operator.PLUS.node(
                        Operator.MULTIPLY.node(
                                this.toRequirementNode(ampereRequirement),
                                this.toRequirementNode(voltRequirement)),
                        Operator.MULTIPLY.node(
                                this.toRequirementNode(ampereRequirement),
                                this.toRequirementNode(voltRequirement)));

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode plus = (OperationNode) copied;
        assertThat(plus.getOperator()).isEqualTo(Operator.PLUS);
        assertThat(plus.getLeftOperand()).isInstanceOf(OperationNode.class);
        assertThat(plus.getRightOperand()).isInstanceOf(OperationNode.class);
    }

    @Test
    public void currentTimesVoltPlusCurrentTimesVoltToDaily_kWh() {
        when(this.deliverableReadingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(this.deliverableReadingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(this.deliverableReadingType.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);
        when(this.deliverableReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);

        ApplyUnitConversion visitor = getDaily_kWhTestInstance();
        ReadingType ampereReadingType = mock(ReadingType.class);
        when(ampereReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(ampereReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(ampereReadingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(ampereReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        FullySpecifiedReadingTypeRequirement ampereRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(ampereRequirement.getDimension()).thenReturn(ReadingTypeUnit.AMPERE.getUnit().getDimension());
        when(ampereRequirement.getReadingType()).thenReturn(ampereReadingType);
        Channel chn1 = mock(Channel.class);
        when(chn1.getMainReadingType()).thenReturn(ampereReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(ampereRequirement)).thenReturn(Collections.singletonList(chn1));
        VirtualReadingTypeRequirement ampereVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType ampereVirtualReadingType = VirtualReadingType.from(ampereReadingType);
        when(ampereVirtualRequirement.getSourceReadingType()).thenReturn(ampereVirtualReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        ampereRequirement,
                        this.deliverable,
                        ampereVirtualReadingType))
                .thenReturn(ampereVirtualRequirement);
        when(ampereVirtualRequirement.getSourceReadingType()).thenReturn(ampereVirtualReadingType);

        ReadingType voltReadingType = mock(ReadingType.class);
        when(voltReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(voltReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(voltReadingType.getUnit()).thenReturn(ReadingTypeUnit.VOLT);
        when(voltReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        Channel chn2 = mock(Channel.class);
        when(chn2.getMainReadingType()).thenReturn(voltReadingType);
        FullySpecifiedReadingTypeRequirement voltRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(voltRequirement.getDimension()).thenReturn(ReadingTypeUnit.VOLT.getUnit().getDimension());
        when(voltRequirement.getReadingType()).thenReturn(voltReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(voltRequirement)).thenReturn(Collections.singletonList(chn2));
        VirtualReadingTypeRequirement voltVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType voltVirtualReadingType = VirtualReadingType.from(voltReadingType);
        when(voltVirtualRequirement.getSourceReadingType()).thenReturn(voltVirtualReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        voltRequirement,
                        this.deliverable,
                        voltVirtualReadingType))
                .thenReturn(voltVirtualRequirement);
        when(voltVirtualRequirement.getSourceReadingType()).thenReturn(voltVirtualReadingType);

        ServerExpressionNode node =
                Operator.PLUS.node(
                        Operator.MULTIPLY.node(
                                this.toRequirementNode(ampereRequirement),
                                this.toRequirementNode(voltRequirement)),
                        Operator.MULTIPLY.node(
                                this.toRequirementNode(ampereRequirement),
                                this.toRequirementNode(voltRequirement)));

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode plus = (OperationNode) copied;
        assertThat(plus.getOperator()).isEqualTo(Operator.PLUS);
        assertThat(plus.getLeftOperand()).isInstanceOf(UnitConversionNode.class);
        UnitConversionNode leftUnitConversionNode = (UnitConversionNode) plus.getLeftOperand();
        assertThat(leftUnitConversionNode.getExpressionNode()).isInstanceOf(OperationNode.class);
        OperationNode leftOperationNode = (OperationNode) leftUnitConversionNode.getExpressionNode();
        assertThat(leftOperationNode.getOperator()).isEqualTo(Operator.MULTIPLY);
        assertThat(plus.getRightOperand()).isInstanceOf(UnitConversionNode.class);
        UnitConversionNode rightUnitConversionNode = (UnitConversionNode) plus.getRightOperand();
        assertThat(rightUnitConversionNode.getExpressionNode()).isInstanceOf(OperationNode.class);
        OperationNode rightOperationNode = (OperationNode) rightUnitConversionNode.getExpressionNode();
        assertThat(rightOperationNode.getOperator()).isEqualTo(Operator.MULTIPLY);
    }

    @Test
    public void copyCurrentTimesVoltPlusAConstantToDaily_kWh() {
        when(this.deliverableReadingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(this.deliverableReadingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(this.deliverableReadingType.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);
        when(this.deliverableReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);

        ApplyUnitConversion visitor = getDaily_kWhTestInstance();
        ReadingType ampereReadingType = mock(ReadingType.class);
        when(ampereReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(ampereReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(ampereReadingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(ampereReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        FullySpecifiedReadingTypeRequirement ampereRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(ampereRequirement.getDimension()).thenReturn(ReadingTypeUnit.AMPERE.getUnit().getDimension());
        Channel chn1 = mock(Channel.class);
        when(chn1.getMainReadingType()).thenReturn(ampereReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(ampereRequirement)).thenReturn(Collections.singletonList(chn1));
        VirtualReadingTypeRequirement ampereVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType ampereVirtualReadingType = VirtualReadingType.from(ampereReadingType);
        when(ampereVirtualRequirement.getSourceReadingType()).thenReturn(ampereVirtualReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        ampereRequirement,
                        this.deliverable,
                        ampereVirtualReadingType))
                .thenReturn(ampereVirtualRequirement);
        when(ampereVirtualRequirement.getSourceReadingType()).thenReturn(ampereVirtualReadingType);

        ReadingType voltReadingType = mock(ReadingType.class);
        when(voltReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(voltReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(voltReadingType.getUnit()).thenReturn(ReadingTypeUnit.VOLT);
        when(voltReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        FullySpecifiedReadingTypeRequirement voltRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(voltRequirement.getDimension()).thenReturn(ReadingTypeUnit.VOLT.getUnit().getDimension());
        Channel chn2 = mock(Channel.class);
        when(chn2.getMainReadingType()).thenReturn(voltReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(voltRequirement)).thenReturn(Collections.singletonList(chn2));
        VirtualReadingTypeRequirement voltVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType voltVirtualReadingType = VirtualReadingType.from(voltReadingType);
        when(voltVirtualRequirement.getSourceReadingType()).thenReturn(voltVirtualReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        voltRequirement,
                        this.deliverable,
                        voltVirtualReadingType))
                .thenReturn(voltVirtualRequirement);
        when(voltVirtualRequirement.getSourceReadingType()).thenReturn(voltVirtualReadingType);

        ServerExpressionNode node =
                Operator.PLUS.node(
                        BigDecimal.TEN,
                        Operator.MULTIPLY.node(
                                this.toRequirementNode(ampereRequirement),
                                this.toRequirementNode(voltRequirement)));

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode plus = (OperationNode) copied;
        assertThat(plus.getOperator()).isEqualTo(Operator.PLUS);
        assertThat(plus.getLeftOperand()).isInstanceOf(NumericalConstantNode.class);
        NumericalConstantNode constantNode = (NumericalConstantNode) plus.getLeftOperand();
        assertThat(constantNode.getValue()).isEqualTo(BigDecimal.TEN);

        assertThat(plus.getRightOperand()).isInstanceOf(UnitConversionNode.class);
        UnitConversionNode rightUnitConversionNode = (UnitConversionNode) plus.getRightOperand();
        assertThat(rightUnitConversionNode.getExpressionNode()).isInstanceOf(OperationNode.class);
        OperationNode rightExpression = (OperationNode) rightUnitConversionNode.getExpressionNode();
        assertThat(rightExpression.getOperator()).isEqualTo(Operator.MULTIPLY);
        assertThat(rightExpression.getLeftOperand()).isInstanceOf(VirtualRequirementNode.class);
        assertThat(rightExpression.getRightOperand()).isInstanceOf(VirtualRequirementNode.class);
    }

    @Test
    public void copyCurrentTimesVoltPlusAConstantTo15min_kWh() {
        ApplyUnitConversion visitor = get15minWhTestInstance();
        ReadingType ampereReadingType = mock(ReadingType.class);
        when(ampereReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(ampereReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(ampereReadingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(ampereReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        FullySpecifiedReadingTypeRequirement ampereRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(ampereRequirement.getDimension()).thenReturn(ReadingTypeUnit.AMPERE.getUnit().getDimension());
        Channel chn1 = mock(Channel.class);
        when(chn1.getMainReadingType()).thenReturn(ampereReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(ampereRequirement)).thenReturn(Collections.singletonList(chn1));
        VirtualReadingTypeRequirement ampereVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType ampereVirtualReadingType = VirtualReadingType.from(ampereReadingType);
        when(ampereVirtualRequirement.getSourceReadingType()).thenReturn(ampereVirtualReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        ampereRequirement,
                        this.deliverable,
                        ampereVirtualReadingType))
                .thenReturn(ampereVirtualRequirement);
        when(ampereVirtualRequirement.getSourceReadingType()).thenReturn(ampereVirtualReadingType);

        ReadingType voltReadingType = mock(ReadingType.class);
        when(voltReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(voltReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(voltReadingType.getUnit()).thenReturn(ReadingTypeUnit.VOLT);
        when(voltReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        FullySpecifiedReadingTypeRequirement voltRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(voltRequirement.getDimension()).thenReturn(ReadingTypeUnit.VOLT.getUnit().getDimension());
        Channel chn2 = mock(Channel.class);
        when(chn2.getMainReadingType()).thenReturn(voltReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(voltRequirement)).thenReturn(Collections.singletonList(chn2));
        VirtualReadingTypeRequirement voltVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType voltVirtualReadingType = VirtualReadingType.from(voltReadingType);
        when(voltVirtualRequirement.getSourceReadingType()).thenReturn(voltVirtualReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        voltRequirement,
                        this.deliverable,
                        voltVirtualReadingType))
                .thenReturn(voltVirtualRequirement);
        when(voltVirtualRequirement.getSourceReadingType()).thenReturn(voltVirtualReadingType);

        ServerExpressionNode node =
                Operator.PLUS.node(
                        BigDecimal.TEN,
                        Operator.MULTIPLY.node(
                                this.toRequirementNode(ampereRequirement),
                                this.toRequirementNode(voltRequirement)));

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode plus = (OperationNode) copied;
        assertThat(plus.getOperator()).isEqualTo(Operator.PLUS);
        assertThat(plus.getLeftOperand()).isInstanceOf(NumericalConstantNode.class);
        assertThat(plus.getRightOperand()).isInstanceOf(OperationNode.class);
        OperationNode multiply = (OperationNode) plus.getRightOperand();
        assertThat(multiply.getOperator()).isEqualTo(Operator.MULTIPLY);
        assertThat(multiply.getLeftOperand()).isInstanceOf(VirtualRequirementNode.class);
        assertThat(multiply.getRightOperand()).isInstanceOf(VirtualRequirementNode.class);
    }

    @Test
    public void copyPressureTimesVolumeDividedByTemperature() {
        ApplyUnitConversion visitor = get15minWhTestInstance();
        ReadingType pressureReadingType = mock(ReadingType.class);
        when(pressureReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(pressureReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(pressureReadingType.getUnit()).thenReturn(ReadingTypeUnit.PASCAL);
        when(pressureReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        Channel pressureChannel = mock(Channel.class);
        when(pressureChannel.getMainReadingType()).thenReturn(pressureReadingType);
        FullySpecifiedReadingTypeRequirement pressure = mock(FullySpecifiedReadingTypeRequirement.class);
        when(pressure.getDimension()).thenReturn(Dimension.PRESSURE);
        when(pressure.getReadingType()).thenReturn(pressureReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(pressure)).thenReturn(Collections.singletonList(pressureChannel));
        VirtualReadingTypeRequirement pressureVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType pressureVirtualReadingType = VirtualReadingType.from(pressureReadingType);
        when(pressureVirtualRequirement.getSourceReadingType()).thenReturn(pressureVirtualReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        pressure,
                        this.deliverable,
                        VirtualReadingType.from(this.deliverableReadingType)))
                .thenReturn(pressureVirtualRequirement);
        when(pressureVirtualRequirement.getSourceReadingType()).thenReturn(pressureVirtualReadingType);

        ReadingType volumeReadingType = mock(ReadingType.class);
        when(volumeReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(volumeReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(volumeReadingType.getUnit()).thenReturn(ReadingTypeUnit.CUBICMETER);
        when(volumeReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        Channel volumeChannel = mock(Channel.class);
        when(volumeChannel.getMainReadingType()).thenReturn(volumeReadingType);
        FullySpecifiedReadingTypeRequirement volume = mock(FullySpecifiedReadingTypeRequirement.class);
        when(volume.getDimension()).thenReturn(Dimension.VOLUME);
        when(volume.getReadingType()).thenReturn(volumeReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(volume)).thenReturn(Collections.singletonList(volumeChannel));
        VirtualReadingTypeRequirement volumeVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType volumeVirtualReadingType = VirtualReadingType.from(volumeReadingType);
        when(volumeVirtualRequirement.getSourceReadingType()).thenReturn(volumeVirtualReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        volume,
                        this.deliverable,
                        VirtualReadingType.from(this.deliverableReadingType)))
                .thenReturn(volumeVirtualRequirement);
        when(volumeVirtualRequirement.getSourceReadingType()).thenReturn(volumeVirtualReadingType);

        ReadingType temperatureReadingType = mock(ReadingType.class);
        when(temperatureReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(temperatureReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(temperatureReadingType.getUnit()).thenReturn(ReadingTypeUnit.DEGREESCELSIUS);
        when(temperatureReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        Channel temperatureChannel = mock(Channel.class);
        when(temperatureChannel.getMainReadingType()).thenReturn(temperatureReadingType);
        FullySpecifiedReadingTypeRequirement temperature = mock(FullySpecifiedReadingTypeRequirement.class);
        when(temperature.getDimension()).thenReturn(Dimension.TEMPERATURE);
        when(temperature.getReadingType()).thenReturn(temperatureReadingType);
        when(this.meterActivationSet.getMatchingChannelsFor(temperature)).thenReturn(Collections.singletonList(temperatureChannel));
        VirtualReadingTypeRequirement temperatureVirtualRequirement = mock(VirtualReadingTypeRequirement.class);
        VirtualReadingType temperatureVirtualReadingType = VirtualReadingType.from(temperatureReadingType);
        when(temperatureVirtualRequirement.getSourceReadingType()).thenReturn(temperatureVirtualReadingType);
        when(this.virtualFactory
                .requirementFor(
                        Formula.Mode.AUTO,
                        temperature,
                        this.deliverable,
                        VirtualReadingType.from(this.deliverableReadingType)))
                .thenReturn(temperatureVirtualRequirement);
        when(temperatureVirtualRequirement.getSourceReadingType()).thenReturn(temperatureVirtualReadingType);

        ServerExpressionNode node =
                Operator.DIVIDE.node(
                        Operator.MULTIPLY.node(
                                this.toRequirementNode(pressure),
                                this.toRequirementNode(volume)),
                        this.toRequirementNode(temperature));

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode operationNode = (OperationNode) copied;
        assertThat(operationNode.getOperator()).isEqualTo(Operator.DIVIDE);
    }

    private VirtualRequirementNode toRequirementNode(ReadingTypeRequirement requirement1) {
        VirtualRequirementNode node = new VirtualRequirementNode(Formula.Mode.AUTO, this.virtualFactory, requirement1, this.deliverable, this.meterActivationSet);
        node.finish();  // Simulate InferReadingType
        return node;
    }

    private ApplyUnitConversion get15minWhTestInstance() {
        return this.getTestInstance(
                VirtualReadingType.from(
                        IntervalLength.MINUTE15,
                        MetricMultiplier.ZERO,
                        ReadingTypeUnit.WATTHOUR,
                        Accumulation.DELTADELTA,
                        Commodity.ELECTRICITY_PRIMARY_METERED));
    }

    private ApplyUnitConversion getDaily_kWhTestInstance() {
        return this.getTestInstance(
                VirtualReadingType.from(
                        IntervalLength.DAY1,
                        MetricMultiplier.KILO,
                        ReadingTypeUnit.WATTHOUR,
                        Accumulation.DELTADELTA,
                        Commodity.ELECTRICITY_PRIMARY_METERED));
    }

    private ApplyUnitConversion getTestInstance(VirtualReadingType virtualReadingType) {
        return new ApplyUnitConversion(Formula.Mode.AUTO, virtualReadingType);
    }

}