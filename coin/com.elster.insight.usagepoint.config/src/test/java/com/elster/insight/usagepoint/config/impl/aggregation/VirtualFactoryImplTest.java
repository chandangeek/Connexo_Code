package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.insight.usagepoint.config.ReadingTypeRequirement;
import com.google.common.collect.Range;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;

/**
 * Tests the {@link VirtualFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-10 (11:58)
 */
@RunWith(MockitoJUnitRunner.class)
public class VirtualFactoryImplTest {

    @Mock
    private ReadingTypeRequirement requirement;
    @Mock
    private ReadingTypeDeliverable deliverable;
    @Mock
    private MeterActivation meterActivation;

    @Test(expected = IllegalStateException.class)
    public void noRequirementsWithoutCurrentMeterActivation() {
        VirtualFactoryImpl factory = this.testInstance();

        // Business method
        factory.requirementFor(this.requirement, this.deliverable, IntervalLength.DAY1);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalStateException.class)
    public void noDeliverablesWithoutCurrentMeterActivation() {
        VirtualFactoryImpl factory = this.testInstance();
        ReadingTypeDeliverableForMeterActivation deliverable =
                new ReadingTypeDeliverableForMeterActivation(
                        this.deliverable,
                        this.meterActivation,
                        Range.all(),
                        0,
                        mock(ServerExpressionNode.class),
                        IntervalLength.DAY1);

        // Business method
        factory.deliverableFor(deliverable, IntervalLength.DAY1);

        // Asserts: see expected exception rule
    }

    private VirtualFactoryImpl testInstance() {
        return new VirtualFactoryImpl();
    }

}