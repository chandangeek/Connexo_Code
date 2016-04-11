package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;

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

    public static final String SOURCE_TABLE_NAME = "TST_1";

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
    public void variableReferenceNodesDoNotProvideJoinClauseInformation() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        ServerExpressionNode node = new VariableReferenceNode("varName");

        // Business method
        node.accept(testInstance);

        assertThat(testInstance.joinClauses()).isEmpty();
    }

    @Test
    public void deliverableWithSameTableNameIsIgnored() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingType readingType = this.mockedReadingType();
        ReadingTypeDeliverableForMeterActivation readingTypeDeliverableForMeterActivation = mock(ReadingTypeDeliverableForMeterActivation.class);
        when(readingTypeDeliverableForMeterActivation.getReadingType()).thenReturn(readingType);
        VirtualReadingTypeDeliverable virtualReadingTypeDeliverable = mock(VirtualReadingTypeDeliverable.class);
        when(virtualReadingTypeDeliverable.sqlName()).thenReturn(SOURCE_TABLE_NAME);
        when(virtualFactory.deliverableFor(eq(readingTypeDeliverableForMeterActivation), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeDeliverable);
        ServerExpressionNode node = new VirtualDeliverableNode(virtualFactory, readingTypeDeliverableForMeterActivation);

        // Business method
        node.accept(testInstance);

        assertThat(testInstance.joinClauses()).isEmpty();
    }

    @Test
    public void deliverableWithDifferentTableNameIsNotIgnored() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingType readingType = this.mockedReadingType();
        ReadingTypeDeliverableForMeterActivation readingTypeDeliverableForMeterActivation = mock(ReadingTypeDeliverableForMeterActivation.class);
        when(readingTypeDeliverableForMeterActivation.getReadingType()).thenReturn(readingType);
        VirtualReadingTypeDeliverable virtualReadingTypeDeliverable = mock(VirtualReadingTypeDeliverable.class);
        String expectedJoinTableName = "deliverableWithDifferentTableNameIsNotIgnored";
        when(virtualReadingTypeDeliverable.sqlName()).thenReturn(expectedJoinTableName);
        when(virtualFactory.deliverableFor(eq(readingTypeDeliverableForMeterActivation), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeDeliverable);
        ServerExpressionNode node = new VirtualDeliverableNode(virtualFactory, readingTypeDeliverableForMeterActivation);

        // Business method
        node.accept(testInstance);

        List<String> joinClauses = testInstance.joinClauses();
        assertThat(joinClauses).hasSize(1);
        assertThat(joinClauses.get(0)).isEqualTo(" JOIN " + expectedJoinTableName + " ON " + expectedJoinTableName + ".timestamp = " + SOURCE_TABLE_NAME + ".timestamp");
    }

    @Test
    public void additionOfSameDeliverable() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingType readingType = this.mockedReadingType();
        ReadingTypeDeliverableForMeterActivation readingTypeDeliverableForMeterActivation = mock(ReadingTypeDeliverableForMeterActivation.class);
        when(readingTypeDeliverableForMeterActivation.getReadingType()).thenReturn(readingType);
        VirtualReadingTypeDeliverable virtualReadingTypeDeliverable = mock(VirtualReadingTypeDeliverable.class);
        String expectedJoinTableName = "DEL1";
        when(virtualReadingTypeDeliverable.sqlName()).thenReturn(expectedJoinTableName);
        when(virtualFactory.deliverableFor(eq(readingTypeDeliverableForMeterActivation), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeDeliverable);
        ServerExpressionNode node =
                new OperationNode(
                        Operator.PLUS,
                        new VirtualDeliverableNode(virtualFactory, readingTypeDeliverableForMeterActivation),
                        new VirtualDeliverableNode(virtualFactory, readingTypeDeliverableForMeterActivation));

        // Business method
        node.accept(testInstance);

        List<String> joinClauses = testInstance.joinClauses();
        assertThat(joinClauses).hasSize(1);
        assertThat(joinClauses.get(0)).isEqualTo(" JOIN " + expectedJoinTableName + " ON " + expectedJoinTableName + ".timestamp = " + SOURCE_TABLE_NAME + ".timestamp");
    }

    @Test
    public void functionCallWithSameDeliverable() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingType readingType = this.mockedReadingType();
        ReadingTypeDeliverableForMeterActivation readingTypeDeliverableForMeterActivation = mock(ReadingTypeDeliverableForMeterActivation.class);
        when(readingTypeDeliverableForMeterActivation.getReadingType()).thenReturn(readingType);
        VirtualReadingTypeDeliverable virtualReadingTypeDeliverable = mock(VirtualReadingTypeDeliverable.class);
        String expectedJoinTableName = "DEL1";
        when(virtualReadingTypeDeliverable.sqlName()).thenReturn(expectedJoinTableName);
        when(virtualFactory.deliverableFor(eq(readingTypeDeliverableForMeterActivation), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeDeliverable);
        ServerExpressionNode node =
                new FunctionCallNode(
                        Function.SUM,
                        new VirtualDeliverableNode(virtualFactory, readingTypeDeliverableForMeterActivation),
                        new VirtualDeliverableNode(virtualFactory, readingTypeDeliverableForMeterActivation));

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
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(readingType);
        VirtualReadingTypeRequirement virtualReadingTypeRequirement = mock(VirtualReadingTypeRequirement.class);
        when(virtualReadingTypeRequirement.sqlName()).thenReturn(SOURCE_TABLE_NAME);
        when(virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(requirement), eq(deliverable), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeRequirement);

        ServerExpressionNode node = new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, mock(MeterActivation.class));

        // Business method
        node.accept(testInstance);

        assertThat(testInstance.joinClauses()).isEmpty();
    }

    @Test
    public void requirementWithDifferentTableNameIsNotIgnored() {
        JoinClausesForExpressionNode testInstance = this.testInstance();
        ReadingType readingType = this.mockedReadingType();
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(readingType);
        VirtualReadingTypeRequirement virtualReadingTypeRequirement = mock(VirtualReadingTypeRequirement.class);
        String expectedJoinTableName = "requirementWithDifferentTableNameIsNotIgnored";
        when(virtualReadingTypeRequirement.sqlName()).thenReturn(expectedJoinTableName);
        when(virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(requirement), eq(deliverable), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeRequirement);

        ServerExpressionNode node = new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, mock(MeterActivation.class));

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
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(readingType);
        VirtualReadingTypeRequirement virtualReadingTypeRequirement = mock(VirtualReadingTypeRequirement.class);
        String expectedJoinTableName = "REQ1";
        when(virtualReadingTypeRequirement.sqlName()).thenReturn(expectedJoinTableName);
        when(virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(requirement), eq(deliverable), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeRequirement);

        ServerExpressionNode node =
                new OperationNode(
                        Operator.PLUS,
                        new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, mock(MeterActivation.class)),
                        new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, mock(MeterActivation.class)));

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
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
        VirtualFactory virtualFactory = mock(VirtualFactory.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(readingType);
        VirtualReadingTypeRequirement virtualReadingTypeRequirement = mock(VirtualReadingTypeRequirement.class);
        String expectedJoinTableName = "REQ1";
        when(virtualReadingTypeRequirement.sqlName()).thenReturn(expectedJoinTableName);
        when(virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(requirement), eq(deliverable), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeRequirement);

        ServerExpressionNode node =
                new FunctionCallNode(
                        Function.SUM,
                        new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, mock(MeterActivation.class)),
                        new VirtualRequirementNode(Formula.Mode.AUTO, virtualFactory, requirement, deliverable, mock(MeterActivation.class)));

        // Business method
        node.accept(testInstance);

        List<String> joinClauses = testInstance.joinClauses();
        assertThat(joinClauses).hasSize(1);
        assertThat(joinClauses.get(0)).isEqualTo(" JOIN " + expectedJoinTableName + " ON " + expectedJoinTableName + ".timestamp = " + SOURCE_TABLE_NAME + ".timestamp");
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
        return new JoinClausesForExpressionNode(" JOIN ", SOURCE_TABLE_NAME);
    }

}