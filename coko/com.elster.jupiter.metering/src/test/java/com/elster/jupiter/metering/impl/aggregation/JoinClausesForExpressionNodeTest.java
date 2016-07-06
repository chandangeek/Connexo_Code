package com.elster.jupiter.metering.impl.aggregation;

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
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link JoinClausesForExpressionNode} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-11 (09:39)
 */
public class JoinClausesForExpressionNodeTest {

    private static final String SOURCE_TABLE_NAME = "TST_1";

    @Test
    public void numericalConstantNodesDoNotProvideJoinClauseInformation() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        ServerExpressionNode node = new NumericalConstantNode(BigDecimal.ONE);

        // Business method
        node.accept(testInstance);

        assertThat(testInstance.joinClauses()).isEmpty();
    }

    @Test
    public void stringConstantNodesDoNotProvideJoinClauseInformation() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        ServerExpressionNode node = new StringConstantNode("stringConstantNodesDoNotProvideJoinClauseInformation");

        // Business method
        node.accept(testInstance);

        assertThat(testInstance.joinClauses()).isEmpty();

        // Business method + asserts
        assertThat(node.accept(this.testInstance())).isNull();
    }

    @Test
    public void sqlFragementNodesDoNotProvideJoinClauseInformation() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        ServerExpressionNode node = new SqlFragmentNode("sequence.nextval");

        // Business method
        node.accept(testInstance);

        assertThat(testInstance.joinClauses()).isEmpty();
    }

    @Test
    public void deliverableWithSameTableNameIsIgnored() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        ReadingType readingType = this.mockedReadingType();
        ReadingTypeDeliverableForMeterActivationSet readingTypeDeliverableForMeterActivationSet = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        when(readingTypeDeliverableForMeterActivationSet.sqlName()).thenReturn(SOURCE_TABLE_NAME);
        when(readingTypeDeliverableForMeterActivationSet.getReadingType()).thenReturn(readingType);
        ServerExpressionNode node = new VirtualDeliverableNode(readingTypeDeliverableForMeterActivationSet);

        // Business method
        node.accept(testInstance);

        assertThat(testInstance.joinClauses()).isEmpty();
    }

    @Test
    public void deliverableWithDifferentTableNameIsNotIgnored() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        ReadingType readingType = this.mockedReadingType();
        String expectedJoinTableName = "deliverableWithDifferentTableNameIsNotIgnored";
        ReadingTypeDeliverableForMeterActivationSet readingTypeDeliverableForMeterActivationSet = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        when(readingTypeDeliverableForMeterActivationSet.sqlName()).thenReturn(expectedJoinTableName);
        when(readingTypeDeliverableForMeterActivationSet.getReadingType()).thenReturn(readingType);
        ServerExpressionNode node = new VirtualDeliverableNode(readingTypeDeliverableForMeterActivationSet);

        // Business method
        node.accept(testInstance);

        List<String> joinClauses = testInstance.joinClauses();
        assertThat(joinClauses).hasSize(1);
        assertThat(joinClauses.get(0)).isEqualTo(" JOIN " + expectedJoinTableName + " ON " + expectedJoinTableName + ".timestamp = " + SOURCE_TABLE_NAME + ".timestamp");
    }

    @Test
    public void additionOfSameDeliverable() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        String expectedJoinTableName = "DEL1";
        ReadingType readingType = this.mockedReadingType();
        ReadingTypeDeliverableForMeterActivationSet readingTypeDeliverableForMeterActivationSet = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        when(readingTypeDeliverableForMeterActivationSet.sqlName()).thenReturn(expectedJoinTableName);
        when(readingTypeDeliverableForMeterActivationSet.getReadingType()).thenReturn(readingType);
        ServerExpressionNode node =
                Operator.PLUS.node(
                        new VirtualDeliverableNode(readingTypeDeliverableForMeterActivationSet),
                        new VirtualDeliverableNode(readingTypeDeliverableForMeterActivationSet));

        // Business method
        node.accept(testInstance);

        List<String> joinClauses = testInstance.joinClauses();
        assertThat(joinClauses).hasSize(1);
        assertThat(joinClauses.get(0)).isEqualTo(" JOIN " + expectedJoinTableName + " ON " + expectedJoinTableName + ".timestamp = " + SOURCE_TABLE_NAME + ".timestamp");
    }

    @Test
    public void functionCallWithSameDeliverable() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        String expectedJoinTableName = "DEL1";
        ReadingType readingType = this.mockedReadingType();
        ReadingTypeDeliverableForMeterActivationSet readingTypeDeliverableForMeterActivationSet = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        when(readingTypeDeliverableForMeterActivationSet.sqlName()).thenReturn(expectedJoinTableName);
        when(readingTypeDeliverableForMeterActivationSet.getReadingType()).thenReturn(readingType);
        ServerExpressionNode node =
                new FunctionCallNode(
                        Function.SUM,
                        IntermediateDimension.of(Dimension.DIMENSIONLESS), new VirtualDeliverableNode(readingTypeDeliverableForMeterActivationSet),
                        new VirtualDeliverableNode(readingTypeDeliverableForMeterActivationSet));

        // Business method
        node.accept(testInstance);

        List<String> joinClauses = testInstance.joinClauses();
        assertThat(joinClauses).hasSize(1);
        assertThat(joinClauses.get(0)).isEqualTo(" JOIN " + expectedJoinTableName + " ON " + expectedJoinTableName + ".timestamp = " + SOURCE_TABLE_NAME + ".timestamp");
    }

    @Test
    public void requirementWithSameTableNameIsIgnored() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        ReadingType readingType = this.mockedReadingType();
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirement.getReadingType()).thenReturn(mock(ReadingType.class));
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(readingType);
        VirtualReadingTypeRequirement virtualReadingTypeRequirement = mock(VirtualReadingTypeRequirement.class);
        when(virtualReadingTypeRequirement.sqlName()).thenReturn(SOURCE_TABLE_NAME);
        when(virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(requirement), eq(deliverable), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeRequirement);
        MeterActivationSet meterActivationSet = mock(MeterActivationSet.class);
        when(meterActivationSet.getRange()).thenReturn(Range.all());

        ServerExpressionNode node = new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, meterActivationSet);

        // Business method
        node.accept(testInstance);

        assertThat(testInstance.joinClauses()).isEmpty();
    }

    @Test
    public void requirementWithDifferentTableNameIsNotIgnored() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        ReadingType readingType = this.mockedReadingType();
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirement.getReadingType()).thenReturn(mock(ReadingType.class));
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(readingType);
        VirtualReadingTypeRequirement virtualReadingTypeRequirement = mock(VirtualReadingTypeRequirement.class);
        String expectedJoinTableName = "requirementWithDifferentTableNameIsNotIgnored";
        when(virtualReadingTypeRequirement.sqlName()).thenReturn(expectedJoinTableName);
        when(virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(requirement), eq(deliverable), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeRequirement);
        MeterActivationSet meterActivationSet = mock(MeterActivationSet.class);
        when(meterActivationSet.getRange()).thenReturn(Range.all());

        ServerExpressionNode node = new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, meterActivationSet);

        // Business method
        node.accept(testInstance);

        List<String> joinClauses = testInstance.joinClauses();
        assertThat(joinClauses).hasSize(1);
        assertThat(joinClauses.get(0)).isEqualTo(" JOIN " + expectedJoinTableName + " ON " + expectedJoinTableName + ".timestamp = " + SOURCE_TABLE_NAME + ".timestamp");
    }

    @Test
    public void additionOfSameRequirement() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        ReadingType readingType = this.mockedReadingType();
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirement.getReadingType()).thenReturn(mock(ReadingType.class));
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(readingType);
        VirtualReadingTypeRequirement virtualReadingTypeRequirement = mock(VirtualReadingTypeRequirement.class);
        String expectedJoinTableName = "REQ1";
        when(virtualReadingTypeRequirement.sqlName()).thenReturn(expectedJoinTableName);
        when(virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(requirement), eq(deliverable), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeRequirement);
        MeterActivationSet meterActivationSet = mock(MeterActivationSet.class);
        when(meterActivationSet.getRange()).thenReturn(Range.all());

        ServerExpressionNode node =
                Operator.PLUS.node(
                        new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, meterActivationSet),
                        new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, meterActivationSet));

        // Business method
        node.accept(testInstance);

        List<String> joinClauses = testInstance.joinClauses();
        assertThat(joinClauses).hasSize(1);
        assertThat(joinClauses.get(0)).isEqualTo(" JOIN " + expectedJoinTableName + " ON " + expectedJoinTableName + ".timestamp = " + SOURCE_TABLE_NAME + ".timestamp");
    }

    @Test
    public void functionCallWithSameRequirement() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        ReadingType readingType = this.mockedReadingType();
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirement.getReadingType()).thenReturn(mock(ReadingType.class));
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(readingType);
        VirtualReadingTypeRequirement virtualReadingTypeRequirement = mock(VirtualReadingTypeRequirement.class);
        String expectedJoinTableName = "REQ1";
        when(virtualReadingTypeRequirement.sqlName()).thenReturn(expectedJoinTableName);
        when(virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(requirement), eq(deliverable), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeRequirement);
        MeterActivationSet meterActivationSet = mock(MeterActivationSet.class);
        when(meterActivationSet.getRange()).thenReturn(Range.all());

        ServerExpressionNode node =
                new FunctionCallNode(
                        Function.SUM,
                        IntermediateDimension.of(Dimension.DIMENSIONLESS), new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, meterActivationSet),
                        new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, meterActivationSet));

        // Business method
        node.accept(testInstance);

        // Asserts
        List<String> joinClauses = testInstance.joinClauses();
        assertThat(joinClauses).hasSize(1);
        assertThat(joinClauses.get(0)).isEqualTo(" JOIN " + expectedJoinTableName + " ON " + expectedJoinTableName + ".timestamp = " + SOURCE_TABLE_NAME + ".timestamp");
    }

    @Test
    public void property() {
        JoinClausesForExpressionNode testInstance = this.testInstance();

        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn("example");
        RegisteredCustomPropertySet customPropertySet = mock(RegisteredCustomPropertySet.class);
        when(customPropertySet.getId()).thenReturn(97L);
        MeterActivationSet meterActivation = mock(MeterActivationSet.class);
        when(meterActivation.sequenceNumber()).thenReturn(101);
        ServerExpressionNode node = new CustomPropertyNode(mock(CustomPropertySetService.class), propertySpec, customPropertySet, mock(UsagePoint.class), meterActivation);
        String expectedJoinTableNameMatchPattern = "cps97_.*_101";

        // Business method
        node.accept(testInstance);

        // Asserts
        List<String> joinClauses = testInstance.joinClauses();
        assertThat(joinClauses).hasSize(1);
        assertThat(joinClauses.get(0)).matches(" JOIN " + expectedJoinTableNameMatchPattern + " ON " + expectedJoinTableNameMatchPattern + "\\.starttime < " + SOURCE_TABLE_NAME + "\\.timestamp AND " + SOURCE_TABLE_NAME + "\\.timestamp <= " + expectedJoinTableNameMatchPattern + "\\.endtime");
    }

    private ReadingType mockedReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        return readingType;
    }

    private JoinClausesForExpressionNode testInstance() {
        return new JoinClausesForExpressionNode(SOURCE_TABLE_NAME);
    }

}