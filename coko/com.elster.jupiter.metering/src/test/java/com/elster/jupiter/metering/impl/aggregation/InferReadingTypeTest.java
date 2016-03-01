package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link InferReadingType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-08 (12:13)
 */
@RunWith(MockitoJUnitRunner.class)
public class InferReadingTypeTest {

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

    @Test
    public void inferNumericalConstantOnly() {
        InferReadingType infer = this.testInstance();
        NumericalConstantNode node = new NumericalConstantNode(BigDecimal.TEN);

        // Business method
        VirtualReadingType preferredReadingType = node.accept(infer);

        // Asserts
        assertThat(preferredReadingType.getIntervalLength()).isEqualTo(IntervalLength.HOUR1);
    }

    @Test
    public void inferStringConstantOnly() {
        InferReadingType infer = this.testInstance();
        StringConstantNode node = new StringConstantNode("BigDecimal.TEN");

        // Business method
        VirtualReadingType preferredReadingType = node.accept(infer);

        // Asserts
        assertThat(preferredReadingType.getIntervalLength()).isEqualTo(IntervalLength.HOUR1);
    }

    @Test
    public void inferConstantsOnly() {
        InferReadingType infer = this.testInstance();
        OperationNode node =
                new OperationNode(
                        Operator.PLUS,
                        new NumericalConstantNode(BigDecimal.valueOf(123L)),
                        new FunctionCallNode(
                                Function.MAX,
                                new NumericalConstantNode(BigDecimal.ZERO),
                                new NumericalConstantNode(BigDecimal.TEN)));

        // Business method
        VirtualReadingType preferredReadingType = node.accept(infer);

        // Asserts
        assertThat(preferredReadingType.getIntervalLength()).isEqualTo(IntervalLength.HOUR1);
    }

    @Test
    public void inferRequirementOnly() {
        InferReadingType infer = this.testInstance();
        VirtualRequirementNode node =
                new VirtualRequirementNode(
                        this.virtualFactory,
                        this.requirement,
                        this.deliverable,
                        this.meterActivation);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);    // Different from the target set in test instance
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        Channel channel = mock(Channel.class);
        when(channel.getMainReadingType()).thenReturn(readingType);
        when(this.requirement.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(channel));
        when(this.requirement.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(readingType));

        // Business method
        VirtualReadingType preferredReadingType = node.accept(infer);

        // Asserts
        assertThat(preferredReadingType.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void inferIncompatibleRequirementsInOperation() {
        InferReadingType infer = this.testInstance();

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
        OperationNode multiply = new OperationNode(Operator.MULTIPLY, sum, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        multiply.accept(infer);

        // Asserts: see expected exception rule
    }

    @Test(expected = UnsupportedOperationException.class)
    public void inferIncompatibleRequirementsInFuntionCall() {
        InferReadingType infer = this.testInstance();

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
        FunctionCallNode maximum =
                new FunctionCallNode(
                        Function.MAX,
                        requirementNode1,
                        requirementNode2,
                        new NumericalConstantNode(BigDecimal.TEN));

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
        InferReadingType infer = this.testInstance();

        ReadingType hourlyReadingType = this.mockHourlyReadingType();
        when(this.deliverable.getReadingType()).thenReturn(hourlyReadingType);
        Channel hourlyChannel = mock(Channel.class);
        when(hourlyChannel.getMainReadingType()).thenReturn(hourlyReadingType);
        ReadingType fifteenMinReadingType = this.mock15minReadingType();
        Channel fifteenMinChannel = mock(Channel.class);
        when(fifteenMinChannel.getMainReadingType()).thenReturn(fifteenMinReadingType);

        ReadingTypeRequirement requirement1 = mock(ReadingTypeRequirement.class);
        when(requirement1.getMatchingChannelsFor(this.meterActivation)).thenReturn(Arrays.asList(fifteenMinChannel, hourlyChannel));
        VirtualRequirementNode requirementNode1 =
                new VirtualRequirementNode(
                        this.virtualFactory,
                        requirement1,
                        this.deliverable,
                        this.meterActivation);
        ReadingTypeRequirement requirement2 = mock(ReadingTypeRequirement.class);
        when(requirement2.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(hourlyChannel));
        VirtualRequirementNode requirementNode2 =
                new VirtualRequirementNode(
                        this.virtualFactory,
                        requirement2,
                        this.deliverable,
                        this.meterActivation);
        OperationNode sum = new OperationNode(Operator.PLUS, requirementNode1, requirementNode2);
        OperationNode multiply = new OperationNode(Operator.MULTIPLY, sum, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        VirtualReadingType preferredReadingType = multiply.accept(infer);

        // Asserts
        assertThat(preferredReadingType.getIntervalLength()).isEqualTo(IntervalLength.HOUR1);
        assertThat(requirementNode1.getTargetReadingType().getIntervalLength()).isEqualByComparingTo(IntervalLength.HOUR1);
        assertThat(requirementNode2.getTargetReadingType().getIntervalLength()).isEqualByComparingTo(IntervalLength.HOUR1);
    }

    /**
     * Same setup and purpose as {@link #inferCompatibleRequirementsInOperation()}.
     */
    @Test
    public void inferCompatibleRequirementsInFunctionCall() {
        InferReadingType infer = this.testInstance();

        ReadingType hourlyReadingType = this.mockHourlyReadingType();
        Channel hourlyChannel = mock(Channel.class);
        when(hourlyChannel.getMainReadingType()).thenReturn(hourlyReadingType);
        when(this.deliverable.getReadingType()).thenReturn(hourlyReadingType);
        ReadingType fifteenMinReadingType = this.mock15minReadingType();
        Channel fifteenMinChannel = mock(Channel.class);
        when(fifteenMinChannel.getMainReadingType()).thenReturn(fifteenMinReadingType);

        ReadingTypeRequirement requirement1 = mock(ReadingTypeRequirement.class);

        when(requirement1.getMatchingChannelsFor(this.meterActivation)).thenReturn(Arrays.asList(fifteenMinChannel, hourlyChannel));
        VirtualRequirementNode requirementNode1 =
                new VirtualRequirementNode(
                        this.virtualFactory,
                        requirement1,
                        this.deliverable,
                        this.meterActivation);
        ReadingTypeRequirement requirement2 = mock(ReadingTypeRequirement.class);
        when(requirement2.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(hourlyChannel));
        VirtualRequirementNode requirementNode2 =
                new VirtualRequirementNode(
                        this.virtualFactory,
                        requirement2,
                        this.deliverable,
                        this.meterActivation);
        FunctionCallNode maximum =
                new FunctionCallNode(
                        Function.MAX,
                        requirementNode1,
                        requirementNode2,
                        new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        VirtualReadingType preferredReadingType = maximum.accept(infer);

        // Asserts
        assertThat(preferredReadingType.getIntervalLength()).isEqualTo(IntervalLength.HOUR1);
        assertThat(requirementNode1.getTargetReadingType().getIntervalLength()).isEqualByComparingTo(IntervalLength.HOUR1);
        assertThat(requirementNode2.getTargetReadingType().getIntervalLength()).isEqualByComparingTo(IntervalLength.HOUR1);
    }

    private ReadingType mock15minReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        return readingType;
    }

    private ReadingType mockHourlyReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        return readingType;
    }

    private InferReadingType testInstance() {
        return new InferReadingType(this.hourlyVirtualReadingType());
    }

    private VirtualReadingType hourlyVirtualReadingType() {
        return VirtualReadingType.from(this.mock60MinkWh());
    }

    private ReadingType mock60MinkWh() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        return readingType;
    }

}