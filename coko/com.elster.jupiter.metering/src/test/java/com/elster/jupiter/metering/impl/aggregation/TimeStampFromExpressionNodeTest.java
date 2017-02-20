/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.slp.SyntheticLoadProfile;
import com.elster.jupiter.util.units.Dimension;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link TimeStampFromExpressionNode} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class TimeStampFromExpressionNodeTest {

    public static final long REGISTERED_CUSTOM_PROPERTY_SET_ID = 97L;
    public static final String PROPERTY_SPEC_NAME = "dummy";
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private RegisteredCustomPropertySet registeredCPS;
    @Mock
    private CustomPropertySet<UsagePoint, ?> customPropertySet;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MeterActivationSet meterActivationSet;

    @Before
    public void initializeMocks() {
        when(this.customPropertySet.isVersioned()).thenReturn(true);
        when(this.registeredCPS.getCustomPropertySet()).thenReturn(this.customPropertySet);
        when(this.propertySpec.getName()).thenReturn(PROPERTY_SPEC_NAME);
        when(this.registeredCPS.getId()).thenReturn(REGISTERED_CUSTOM_PROPERTY_SET_ID);
        when(this.meterActivationSet.sequenceNumber()).thenReturn(1);
    }

    @Test
    public void unvisitedReturnsNull() {
        TimeStampFromExpressionNode visitor = getInstance();

        // Business method
        String sqlName = visitor.getSqlName();

        // Asserts
        assertThat(sqlName).isNull();
    }

    @Test
    public void numericalConstantNodeOnlyReturnsNull() {
        TimeStampFromExpressionNode visitor = getInstance();

        // Business method
        new NumericalConstantNode(BigDecimal.ONE).accept(visitor);

        // Asserts
        assertThat(visitor.getSqlName()).isNull();
    }

    @Test
    public void stringConstantNodeOnlyReturnsNull() {
        TimeStampFromExpressionNode visitor = getInstance();

        // Business method
        new StringConstantNode("BigDecimal.ONE").accept(visitor);

        // Asserts
        assertThat(visitor.getSqlName()).isNull();
    }

    @Test
    public void versionedCustomPropertyNodeOnlyReturnsPropertyRelatedSqlName() {
        TimeStampFromExpressionNode visitor = getInstance();
        when(this.customPropertySet.isVersioned()).thenReturn(true);

        // Business method
        new CustomPropertyNode(this.customPropertySetService, this.propertySpec, this.registeredCPS, this.usagePoint, this.meterActivationSet).accept(visitor);

        // Asserts
        verify(this.propertySpec).getName();
        verify(this.registeredCPS).getId();
        verify(this.meterActivationSet).sequenceNumber();
        assertThat(visitor.getSqlName()).contains(String.valueOf(PROPERTY_SPEC_NAME.hashCode()));
        assertThat(visitor.getSqlName()).contains(String.valueOf(REGISTERED_CUSTOM_PROPERTY_SET_ID));
    }

    @Test
    public void nonVersionedCustomPropertyNodeOnlyReturnsPropertyRelatedSqlName() {
        TimeStampFromExpressionNode visitor = getInstance();
        when(this.customPropertySet.isVersioned()).thenReturn(false);

        // Business method
        new CustomPropertyNode(this.customPropertySetService, this.propertySpec, this.registeredCPS, this.usagePoint, this.meterActivationSet).accept(visitor);

        // Asserts
        assertThat(visitor.getSqlName()).isNull();
    }

    @Test
    public void twoCustomPropertyNodesReturnsFirstPropertyRelatedSqlName() {
        TimeStampFromExpressionNode visitor = getInstance();
        RegisteredCustomPropertySet registeredCPS1 = mock(RegisteredCustomPropertySet.class);
        when(registeredCPS1.getId()).thenReturn(REGISTERED_CUSTOM_PROPERTY_SET_ID);
        CustomPropertySet cps1 = mock(CustomPropertySet.class);
        when(registeredCPS1.getCustomPropertySet()).thenReturn(cps1);
        when(cps1.isVersioned()).thenReturn(true);
        PropertySpec propertySpec1 = mock(PropertySpec.class);
        when(propertySpec1.getName()).thenReturn("prop1");
        CustomPropertyNode node1 = new CustomPropertyNode(this.customPropertySetService, propertySpec1, registeredCPS1, this.usagePoint, this.meterActivationSet);
        RegisteredCustomPropertySet registeredCPS2 = mock(RegisteredCustomPropertySet.class);
        when(registeredCPS2.getId()).thenReturn(REGISTERED_CUSTOM_PROPERTY_SET_ID + 1);
        CustomPropertySet cps2 = mock(CustomPropertySet.class);
        when(registeredCPS2.getCustomPropertySet()).thenReturn(cps2);
        when(cps2.isVersioned()).thenReturn(true);
        PropertySpec propertySpec2 = mock(PropertySpec.class);
        when(propertySpec2.getName()).thenReturn("prop2");
        CustomPropertyNode node2 = new CustomPropertyNode(this.customPropertySetService, propertySpec2, registeredCPS2, this.usagePoint, this.meterActivationSet);

        // Business method
        new OperationNode(Operator.PLUS, Dimension.DIMENSIONLESS, node1, node2).accept(visitor);

        // Asserts
        verify(propertySpec1).getName();
        verify(registeredCPS1).getId();
        verify(propertySpec2).getName();
        verify(registeredCPS2).getId();
        verify(this.meterActivationSet, times(2)).sequenceNumber();
        assertThat(visitor.getSqlName()).contains(String.valueOf("prop1".hashCode()));
        assertThat(visitor.getSqlName()).contains(String.valueOf(REGISTERED_CUSTOM_PROPERTY_SET_ID));
    }

    @Test
    public void customPropertyNodesHasPriorityOverConstantNode() {
        TimeStampFromExpressionNode visitor = getInstance();
        CustomPropertyNode cpsNode = new CustomPropertyNode(this.customPropertySetService, this.propertySpec, this.registeredCPS, this.usagePoint, this.meterActivationSet);
        NumericalConstantNode constantNode = new NumericalConstantNode(BigDecimal.ONE);

        // Business method
        new OperationNode(Operator.PLUS, Dimension.DIMENSIONLESS, cpsNode, constantNode).accept(visitor);

        // Asserts
        verify(this.propertySpec).getName();
        verify(this.registeredCPS).getId();
        verify(this.meterActivationSet).sequenceNumber();
        assertThat(visitor.getSqlName()).contains(String.valueOf(PROPERTY_SPEC_NAME.hashCode()));
        assertThat(visitor.getSqlName()).contains(String.valueOf(REGISTERED_CUSTOM_PROPERTY_SET_ID));
    }

    @Test
    public void versionedSlpNodeOnlyReturnsSlpName() {
        TimeStampFromExpressionNode visitor = getInstance();
        when(this.customPropertySet.isVersioned()).thenReturn(true);
        Range<Instant> range = Range.atLeast(Instant.EPOCH);
        when(meterActivationSet.getRange()).thenReturn(range);
        CustomPropertySetValues values = CustomPropertySetValues.emptyDuring(range);
        SyntheticLoadProfile slp = mock(SyntheticLoadProfile.class);
        values.setProperty(PROPERTY_SPEC_NAME, slp);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint, Instant.EPOCH)).thenReturn(values);

        // Business method
        new SyntheticLoadProfilePropertyNode(this.customPropertySetService, this.propertySpec, this.registeredCPS, this.usagePoint, this.meterActivationSet).accept(visitor);

        // Asserts
        verify(this.propertySpec, atLeastOnce()).getName();
        verify(this.registeredCPS).getId();
        verify(this.meterActivationSet).sequenceNumber();
        assertThat(visitor.getSqlName()).contains(String.valueOf(PROPERTY_SPEC_NAME.hashCode()));
        assertThat(visitor.getSqlName()).contains(String.valueOf(REGISTERED_CUSTOM_PROPERTY_SET_ID));
    }

    @Test
    public void nonVersionedSlpNodeOnlyReturnsSlpName() {
        TimeStampFromExpressionNode visitor = getInstance();
        when(this.customPropertySet.isVersioned()).thenReturn(false);
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        SyntheticLoadProfile slp = mock(SyntheticLoadProfile.class);
        values.setProperty(PROPERTY_SPEC_NAME, slp);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint)).thenReturn(values);

        // Business method
        new SyntheticLoadProfilePropertyNode(this.customPropertySetService, this.propertySpec, this.registeredCPS, this.usagePoint, this.meterActivationSet).accept(visitor);

        // Asserts
        verify(this.propertySpec, atLeastOnce()).getName();
        verify(this.registeredCPS).getId();
        verify(this.meterActivationSet).sequenceNumber();
        assertThat(visitor.getSqlName()).contains(String.valueOf(PROPERTY_SPEC_NAME.hashCode()));
        assertThat(visitor.getSqlName()).contains(String.valueOf(REGISTERED_CUSTOM_PROPERTY_SET_ID));
    }

    @Test
    public void namedSqlFragmentNodeOnlyReturnsNull() {
        TimeStampFromExpressionNode visitor = getInstance();

        // Business method
        new SqlFragmentNode("name").accept(visitor);

        // Asserts
        assertThat(visitor.getSqlName()).isNull();
    }

    @Test
    public void sqlFragmentNodeOnlyReturnsNull() {
        TimeStampFromExpressionNode visitor = getInstance();

        // Business method
        new SqlFragmentNode("select * from dual").accept(visitor);

        // Asserts
        assertThat(visitor.getSqlName()).isNull();
    }

    @Test
    public void nullNodeOnlyReturnsNull() {
        TimeStampFromExpressionNode visitor = getInstance();

        // Business method
        new NullNode().accept(visitor);

        // Asserts
        assertThat(visitor.getSqlName()).isNull();
    }

    @Test
    public void deliverableNodeOnlyReturnsDeliverableSqlName() {
        TimeStampFromExpressionNode visitor = getInstance();
        ReadingTypeDeliverableForMeterActivationSet mock = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        when(mock.sqlName()).thenReturn("deliverable");
        ReadingType readingType = this.mockReadingType();
        when(mock.getReadingType()).thenReturn(readingType);

        // Business method
        new VirtualDeliverableNode(mock).accept(visitor);

        // Asserts
        assertThat(visitor.getSqlName()).isEqualTo("deliverable." + SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName());
    }

    @Test
    public void twoDeliverableNodesReturnsFirstDeliverableSqlName() {
        TimeStampFromExpressionNode visitor = getInstance();
        ReadingType readingType = this.mockReadingType();
        ReadingTypeDeliverableForMeterActivationSet mock1 = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        when(mock1.sqlName()).thenReturn("deliverable1");
        when(mock1.getReadingType()).thenReturn(readingType);
        ReadingTypeDeliverableForMeterActivationSet mock2 = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        when(mock2.sqlName()).thenReturn("deliverable2");
        when(mock2.getReadingType()).thenReturn(readingType);

        VirtualDeliverableNode node1 = new VirtualDeliverableNode(mock1);
        VirtualDeliverableNode node2 = new VirtualDeliverableNode(mock2);

        // Business method
        new OperationNode(Operator.PLUS, Dimension.DIMENSIONLESS, node1, node2).accept(visitor);

        // Asserts
        assertThat(visitor.getSqlName()).isEqualTo("deliverable1." + SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName());
    }

    @Test
    public void requirementNodeOnlyReturnsDeliverableSqlName() {
        TimeStampFromExpressionNode visitor = getInstance();
        ReadingTypeDeliverableForMeterActivationSet mock = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        when(mock.sqlName()).thenReturn("deliverable");
        VirtualReadingTypeRequirement virtualRequirement = mock(VirtualReadingTypeRequirement.class);
        when(virtualRequirement.sqlName()).thenReturn("requirement");
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        ReadingType readingType = this.mockReadingType();
        when(deliverable.getReadingType()).thenReturn(readingType);
        Channel channel = mock(Channel.class);
        when(channel.getMainReadingType()).thenReturn(readingType);
        when(this.meterActivationSet.getMatchingChannelsFor(requirement)).thenReturn(Arrays.asList(channel));
        when(virtualFactory.requirementFor(Formula.Mode.AUTO, requirement, deliverable, VirtualReadingType.from(readingType))).thenReturn(virtualRequirement);

        // Business method
        new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, this.meterActivationSet).accept(visitor);

        // Asserts
        assertThat(visitor.getSqlName()).isEqualTo("requirement." + SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName());
    }

    @Test
    public void requirementNodeHasPriorityOverCustomPropertyNode() {
        TimeStampFromExpressionNode visitor = getInstance();
        when(this.customPropertySet.isVersioned()).thenReturn(true);
        CustomPropertyNode cpsNode = new CustomPropertyNode(this.customPropertySetService, this.propertySpec, this.registeredCPS, this.usagePoint, this.meterActivationSet);

        VirtualReadingTypeRequirement virtualRequirement = mock(VirtualReadingTypeRequirement.class);
        when(virtualRequirement.sqlName()).thenReturn("requirement");
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        ReadingType readingType = this.mockReadingType();
        when(deliverable.getReadingType()).thenReturn(readingType);
        Channel channel = mock(Channel.class);
        when(channel.getMainReadingType()).thenReturn(readingType);
        when(this.meterActivationSet.getMatchingChannelsFor(requirement)).thenReturn(Collections.singletonList(channel));
        when(virtualFactory.requirementFor(Formula.Mode.AUTO, requirement, deliverable, VirtualReadingType.from(readingType))).thenReturn(virtualRequirement);
        VirtualRequirementNode requirementNode = new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, this.meterActivationSet);

        // Business method
        new OperationNode(Operator.PLUS, Dimension.DIMENSIONLESS, cpsNode, requirementNode).accept(visitor);

        // Asserts
        verify(this.propertySpec).getName();
        verify(this.registeredCPS).getId();
        verify(this.meterActivationSet).sequenceNumber();
        assertThat(visitor.getSqlName()).isEqualTo("requirement." + SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName());
    }

    @Test
    public void requirementNodeHasPriorityOverConstantNode() {
        TimeStampFromExpressionNode visitor = getInstance();
        when(this.customPropertySet.isVersioned()).thenReturn(true);
        NumericalConstantNode constantNode = new NumericalConstantNode(BigDecimal.ONE);

        VirtualReadingTypeRequirement virtualRequirement = mock(VirtualReadingTypeRequirement.class);
        when(virtualRequirement.sqlName()).thenReturn("requirement");
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        ReadingType readingType = this.mockReadingType();
        when(deliverable.getReadingType()).thenReturn(readingType);
        Channel channel = mock(Channel.class);
        when(channel.getMainReadingType()).thenReturn(readingType);
        when(this.meterActivationSet.getMatchingChannelsFor(requirement)).thenReturn(Collections.singletonList(channel));
        when(virtualFactory.requirementFor(Formula.Mode.AUTO, requirement, deliverable, VirtualReadingType.from(readingType))).thenReturn(virtualRequirement);
        VirtualRequirementNode requirementNode = new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, this.meterActivationSet);

        // Business method
        new OperationNode(Operator.PLUS, Dimension.DIMENSIONLESS, requirementNode, constantNode).accept(visitor);

        // Asserts
        assertThat(visitor.getSqlName()).isEqualTo("requirement." + SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName());
    }

    @Test
    public void versionedSlpNodeHasPriorityOverCustomPropertyNodeInOperation() {
        TimeStampFromExpressionNode visitor = getInstance();
        when(this.customPropertySet.isVersioned()).thenReturn(true);
        Range<Instant> range = Range.atLeast(Instant.EPOCH);
        when(meterActivationSet.getRange()).thenReturn(range);
        CustomPropertySetValues values = CustomPropertySetValues.emptyDuring(range);
        SyntheticLoadProfile slp = mock(SyntheticLoadProfile.class);
        values.setProperty(PROPERTY_SPEC_NAME, slp);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint, Instant.EPOCH)).thenReturn(values);
        SyntheticLoadProfilePropertyNode slpNode = new SyntheticLoadProfilePropertyNode(this.customPropertySetService, this.propertySpec, this.registeredCPS, this.usagePoint, this.meterActivationSet);
        when(this.customPropertySet.isVersioned()).thenReturn(true);
        PropertySpec otherSpec = mock(PropertySpec.class);
        when(otherSpec.getName()).thenReturn("OTHER");
        CustomPropertyNode cpsNode = new CustomPropertyNode(this.customPropertySetService, otherSpec, this.registeredCPS, this.usagePoint, this.meterActivationSet);

        // Business method
        new OperationNode(Operator.PLUS, Dimension.DIMENSIONLESS, slpNode, cpsNode).accept(visitor);

        // Asserts
        verify(this.propertySpec, atLeastOnce()).getName();
        verify(this.registeredCPS, atLeastOnce()).getId();
        verify(this.meterActivationSet, atLeastOnce()).sequenceNumber();
        assertThat(visitor.getSqlName()).contains(String.valueOf(PROPERTY_SPEC_NAME.hashCode()));
        assertThat(visitor.getSqlName()).contains(String.valueOf(REGISTERED_CUSTOM_PROPERTY_SET_ID));
    }

    @Test
    public void versionedSlpNodeHasPriorityOverCustomPropertyNodeInFunction() {
        TimeStampFromExpressionNode visitor = getInstance();
        when(this.customPropertySet.isVersioned()).thenReturn(true);
        Range<Instant> range = Range.atLeast(Instant.EPOCH);
        when(meterActivationSet.getRange()).thenReturn(range);
        CustomPropertySetValues values = CustomPropertySetValues.emptyDuring(range);
        SyntheticLoadProfile slp = mock(SyntheticLoadProfile.class);
        values.setProperty(PROPERTY_SPEC_NAME, slp);
        when(this.customPropertySetService.getUniqueValuesFor(this.customPropertySet, this.usagePoint, Instant.EPOCH)).thenReturn(values);
        SyntheticLoadProfilePropertyNode slpNode = new SyntheticLoadProfilePropertyNode(this.customPropertySetService, this.propertySpec, this.registeredCPS, this.usagePoint, this.meterActivationSet);
        when(this.customPropertySet.isVersioned()).thenReturn(true);
        PropertySpec otherSpec = mock(PropertySpec.class);
        when(otherSpec.getName()).thenReturn("OTHER");
        CustomPropertyNode cpsNode = new CustomPropertyNode(this.customPropertySetService, otherSpec, this.registeredCPS, this.usagePoint, this.meterActivationSet);

        // Business method
        new FunctionCallNode(Function.MIN, IntermediateDimension.of(Dimension.DIMENSIONLESS), slpNode, cpsNode).accept(visitor);

        // Asserts
        verify(this.propertySpec, atLeastOnce()).getName();
        verify(this.registeredCPS, atLeastOnce()).getId();
        verify(this.meterActivationSet, atLeastOnce()).sequenceNumber();
        assertThat(visitor.getSqlName()).contains(String.valueOf(PROPERTY_SPEC_NAME.hashCode()));
        assertThat(visitor.getSqlName()).contains(String.valueOf(REGISTERED_CUSTOM_PROPERTY_SET_ID));
    }

    private ReadingType mockReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_SECONDARY_METERED);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        return readingType;
    }

    private TimeStampFromExpressionNode getInstance() {
        return new TimeStampFromExpressionNode();
    }
}