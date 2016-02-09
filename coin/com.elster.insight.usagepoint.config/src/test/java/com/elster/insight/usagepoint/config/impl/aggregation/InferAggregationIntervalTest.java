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
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(this.deliverable.getReadingType()).thenReturn(readingType);
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
        when(this.meterActivation.getReadingTypes()).thenReturn(Collections.singletonList(readingType));

        // Business method
        IntervalLength preferredInterval = node.accept(infer);

        // Asserts
        assertThat(preferredInterval).isEqualTo(IntervalLength.HOUR1);
    }

    private InferAggregationInterval testInstance() {
        return new InferAggregationInterval(IntervalLength.MINUTE15);
    }

}