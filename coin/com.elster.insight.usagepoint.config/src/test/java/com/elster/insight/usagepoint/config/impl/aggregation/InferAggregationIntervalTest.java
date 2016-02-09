package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.insight.usagepoint.config.ReadingTypeRequirement;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link InferAggregationInterval} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-08 (12:13)
 */
@RunWith(MockitoJUnitRunner.class)
public class InferAggregationIntervalTest {

    @Mock
    private ReadingTypeRequirement requirement;
    @Mock
    private ReadingTypeDeliverable deliverable;
    @Mock
    private VirtualFactory virtualFactory;
    @Mock
    private MeterActivation meterActivation;

    @Before
    public void initializeMocks() {
        ReadingType readingType = this.mock15minReadingType();
        when(this.deliverable.getReadingType()).thenReturn(readingType);
        when(this.requirement.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(readingType));
    }

    @Test(expected = IllegalArgumentException.class)
    public void requirementNotSupported() {
        InferAggregationInterval infer = this.testInstance();
        ReadingTypeRequirementNode node = new ReadingTypeRequirementNode(this.requirement);

        // Business method
        node.accept(infer);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void deliverableNotSupported() {
        InferAggregationInterval infer = this.testInstance();
        ReadingTypeDeliverableNode node = new ReadingTypeDeliverableNode(this.deliverable);

        // Business method
        node.accept(infer);

        // Asserts: see expected exception rule
    }

    @Test
    public void inferConstantOnly() {
        InferAggregationInterval infer = this.testInstance();
        ConstantNode node = new ConstantNode(BigDecimal.TEN);

        // Business method
        IntervalLength preferredInterval = node.accept(infer);

        // Asserts
        assertThat(preferredInterval).isEqualTo(IntervalLength.MINUTE15);
    }

    @Test
    public void inferConstantsOnly() {
        InferAggregationInterval infer = this.testInstance();
        OperationNode node =
                new OperationNode(
                        Operator.PLUS,
                        new ConstantNode(BigDecimal.valueOf(123L)),
                        new FunctionCallNode(Arrays.asList(
                                new ConstantNode(BigDecimal.ZERO),
                                new ConstantNode(BigDecimal.TEN)),
                                Function.MAX));

        // Business method
        IntervalLength preferredInterval = node.accept(infer);

        // Asserts
        assertThat(preferredInterval).isEqualTo(IntervalLength.MINUTE15);
    }

    @Test
    public void inferRequirementOnly() {
        InferAggregationInterval infer = this.testInstance();
        VirtualRequirementNode node =
                new VirtualRequirementNode(
                        this.virtualFactory,
                        this.requirement,
                        this.deliverable,
                        this.meterActivation);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);    // Different from the target set in test instance
        when(this.requirement.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(readingType));

        // Business method
        IntervalLength preferredInterval = node.accept(infer);

        // Asserts
        assertThat(preferredInterval).isEqualTo(IntervalLength.HOUR1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void inferIncompatibleRequirementsInOperation() {
        InferAggregationInterval infer = this.testInstance();

        ReadingTypeRequirement requirement1 = mock(ReadingTypeRequirement.class);
        ReadingType requirement1ReadingType = mock(ReadingType.class);
        when(requirement1ReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(requirement1ReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE2);   // Make sure this is not compatible with requirement 2
        when(requirement1.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(requirement1ReadingType));
        VirtualRequirementNode requirementNode1 =
                new VirtualRequirementNode(
                        this.virtualFactory,
                        requirement1,
                        this.deliverable,
                        this.meterActivation);
        ReadingTypeRequirement requirement2 = mock(ReadingTypeRequirement.class);
        ReadingType requirement2ReadingType = mock(ReadingType.class);
        when(requirement2ReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(requirement2ReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);  // Incompatible with requirement 1
        when(requirement2.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(requirement2ReadingType));
        VirtualRequirementNode requirementNode2 =
                new VirtualRequirementNode(
                        this.virtualFactory,
                        requirement2,
                        this.deliverable,
                        this.meterActivation);
        OperationNode sum = new OperationNode(Operator.PLUS, requirementNode1, requirementNode2);
        OperationNode multiply = new OperationNode(Operator.MULTIPLY, sum, new ConstantNode(BigDecimal.TEN));

        // Business method
        multiply.accept(infer);

        // Asserts: see expected exception rule
    }

    @Test(expected = UnsupportedOperationException.class)
    public void inferIncompatibleRequirementsInFuntionCall() {
        InferAggregationInterval infer = this.testInstance();

        ReadingTypeRequirement requirement1 = mock(ReadingTypeRequirement.class);
        ReadingType requirement1ReadingType = mock(ReadingType.class);
        when(requirement1ReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(requirement1ReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE2);   // Make sure this is not compatible with requirement 2
        when(requirement1.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(requirement1ReadingType));
        VirtualRequirementNode requirementNode1 =
                new VirtualRequirementNode(
                        this.virtualFactory,
                        requirement1,
                        this.deliverable,
                        this.meterActivation);
        ReadingTypeRequirement requirement2 = mock(ReadingTypeRequirement.class);
        ReadingType requirement2ReadingType = mock(ReadingType.class);
        when(requirement2ReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(requirement2ReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);  // Incompatible with requirement 1
        when(requirement2.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(requirement2ReadingType));
        VirtualRequirementNode requirementNode2 =
                new VirtualRequirementNode(
                        this.virtualFactory,
                        requirement2,
                        this.deliverable,
                        this.meterActivation);
        FunctionCallNode maximum = new FunctionCallNode(Arrays.asList(requirementNode1, requirementNode2, new ConstantNode(BigDecimal.TEN)), Function.MAX);

        // Business method
        maximum.accept(infer);

        // Asserts: see expected exception rule
    }

    /**
     * Setup: X = Y + Z
     *        Y = requirement that is backed by 15 min and 60 min values
     *        Z = requirement that is only backed by 15 min values
     *        X explicitly needs 60 min values
     * Initially, Y will be requested to produce 60 min values, which it can.
     * Then the algorithm should discover that Z can only produce 15 min values but those are more precise
     * than the hourly values and will try to enforce 15 min onto Y. Since this is also possible,
     * the inference algorithm should return 15 min as a result.
     */
    @Test
    public void inferCompatibleRequirementsInOperation() {
        InferAggregationInterval infer = this.testInstance();

        ReadingType hourlyReadingType = this.mockHourlyReadingType();
        when(this.deliverable.getReadingType()).thenReturn(hourlyReadingType);
        ReadingType fifteenMinReadingType = this.mock15minReadingType();

        ReadingTypeRequirement requirement1 = mock(ReadingTypeRequirement.class);
        when(requirement1.getMatchesFor(this.meterActivation)).thenReturn(Arrays.asList(fifteenMinReadingType, hourlyReadingType));
        VirtualRequirementNode requirementNode1 =
                new VirtualRequirementNode(
                        this.virtualFactory,
                        requirement1,
                        this.deliverable,
                        this.meterActivation);
        ReadingTypeRequirement requirement2 = mock(ReadingTypeRequirement.class);
        when(requirement2.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(hourlyReadingType));
        VirtualRequirementNode requirementNode2 =
                new VirtualRequirementNode(
                        this.virtualFactory,
                        requirement2,
                        this.deliverable,
                        this.meterActivation);
        OperationNode sum = new OperationNode(Operator.PLUS, requirementNode1, requirementNode2);
        OperationNode multiply = new OperationNode(Operator.MULTIPLY, sum, new ConstantNode(BigDecimal.TEN));

        // Business method
        IntervalLength intervalLength = multiply.accept(infer);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE15);
        assertThat(requirementNode1.getTargetInterval()).isEqualByComparingTo(IntervalLength.MINUTE15);
        assertThat(requirementNode2.getTargetInterval()).isEqualByComparingTo(IntervalLength.MINUTE15);
    }

    /**
     * Same setup and purpose as {@link #inferCompatibleRequirementsInOperation()}.
     */
    @Test
    public void inferCompatibleRequirementsInFunctionCall() {
        InferAggregationInterval infer = this.testInstance();

        ReadingType hourlyReadingType = this.mockHourlyReadingType();
        when(this.deliverable.getReadingType()).thenReturn(hourlyReadingType);
        ReadingType fifteenMinReadingType = this.mock15minReadingType();

        ReadingTypeRequirement requirement1 = mock(ReadingTypeRequirement.class);
        when(requirement1.getMatchesFor(this.meterActivation)).thenReturn(Arrays.asList(fifteenMinReadingType, hourlyReadingType));
        VirtualRequirementNode requirementNode1 =
                new VirtualRequirementNode(
                        this.virtualFactory,
                        requirement1,
                        this.deliverable,
                        this.meterActivation);
        ReadingTypeRequirement requirement2 = mock(ReadingTypeRequirement.class);
        when(requirement2.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(hourlyReadingType));
        VirtualRequirementNode requirementNode2 =
                new VirtualRequirementNode(
                        this.virtualFactory,
                        requirement2,
                        this.deliverable,
                        this.meterActivation);
        FunctionCallNode maximum = new FunctionCallNode(Arrays.asList(requirementNode1, requirementNode2, new ConstantNode(BigDecimal.TEN)), Function.MAX);

        // Business method
        IntervalLength intervalLength = maximum.accept(infer);

        // Asserts
        assertThat(intervalLength).isEqualTo(IntervalLength.MINUTE15);
        assertThat(requirementNode1.getTargetInterval()).isEqualByComparingTo(IntervalLength.MINUTE15);
        assertThat(requirementNode2.getTargetInterval()).isEqualByComparingTo(IntervalLength.MINUTE15);
    }

    private ReadingType mock15minReadingType() {
        ReadingType meterActivationReadingType = mock(ReadingType.class);
        when(meterActivationReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(meterActivationReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        return meterActivationReadingType;
    }

    private ReadingType mockHourlyReadingType() {
        ReadingType meterActivationReadingType = mock(ReadingType.class);
        when(meterActivationReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(meterActivationReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        return meterActivationReadingType;
    }

    private InferAggregationInterval testInstance() {
        return new InferAggregationInterval(IntervalLength.MINUTE15);
    }

}