package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    private Range<Instant> aggregationPeriod;

    @Before
    public void initializeMocks() {
        Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
        when(this.meterActivation.getRange()).thenReturn(Range.atLeast(jan1st2016));
        this.aggregationPeriod = Range.atLeast(jan1st2016);
    }

    @Test(expected = IllegalStateException.class)
    public void requirementsNotSupportedWithoutCurrentMeterActivation() {
        VirtualFactoryImpl factory = this.testInstance();

        // Business method
        factory.requirementFor(this.requirement, this.deliverable, this.dailyVirtualReadingType());

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalStateException.class)
    public void deliverablesNotSupportedWithoutCurrentMeterActivation() {
        VirtualFactoryImpl factory = this.testInstance();
        ReadingTypeDeliverableForMeterActivation deliverable =
                new ReadingTypeDeliverableForMeterActivation(
                        this.deliverable,
                        this.meterActivation,
                        Range.all(),
                        0,
                        mock(ServerExpressionNode.class),
                        this.dailyVirtualReadingType());

        // Business method
        factory.deliverableFor(deliverable, this.dailyVirtualReadingType());

        // Asserts: see expected exception rule
    }

    @Test
    public void noDeliverablesWithoutCurrentMeterActivation() {
        VirtualFactoryImpl factory = this.testInstance();

        // Business method and asserts
        assertThat(factory.allDeliverables()).isEmpty();
    }

    @Test
    public void noRequirementsWithoutCurrentMeterActivation() {
        VirtualFactoryImpl factory = this.testInstance();

        // Business method and asserts
        assertThat(factory.allRequirements()).isEmpty();
    }

    @Test
    public void nextMeterActivationIncreasesMeterActivationSequenceNumber() {
        VirtualFactoryImpl factory = this.testInstance();
        int before = factory.meterActivationSequenceNumber();

        // Business method
        factory.nextMeterActivation(this.meterActivation, this.aggregationPeriod);

        // Asserts
        int after = factory.meterActivationSequenceNumber();
        assertThat(after).isGreaterThan(before);
    }

    @Test
    public void sameRequirementIsCreatedOnlyOnce() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivation(this.meterActivation, this.aggregationPeriod);
        VirtualReadingTypeRequirement first = factory.requirementFor(this.requirement, this.deliverable, this.dailyVirtualReadingType());

        // Business method
        VirtualReadingTypeRequirement second = factory.requirementFor(this.requirement, this.deliverable, this.dailyVirtualReadingType());

        // Asserts
        assertThat(first).isSameAs(second);
    }

    @Test
    public void sameRequirementIsRecreatedForAnotherMeterActivation() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivation(this.meterActivation, this.aggregationPeriod);
        VirtualReadingTypeRequirement first = factory.requirementFor(this.requirement, this.deliverable, this.dailyVirtualReadingType());
        MeterActivation nextMeterActivation = mock(MeterActivation.class);
        when(nextMeterActivation.getRange()).thenReturn(Range.all());
        factory.nextMeterActivation(nextMeterActivation, this.aggregationPeriod);

        // Business method
        VirtualReadingTypeRequirement second = factory.requirementFor(this.requirement, this.deliverable, this.dailyVirtualReadingType());

        // Asserts
        assertThat(first).isNotSameAs(second);
    }

    @Test
    public void sameRequirementIsRecreatedForAnotherInterval() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivation(this.meterActivation, this.aggregationPeriod);
        VirtualReadingTypeRequirement first = factory.requirementFor(this.requirement, this.deliverable, this.dailyVirtualReadingType());

        // Business method
        VirtualReadingTypeRequirement second = factory.requirementFor(this.requirement, this.deliverable, this.hourlyVirtualReadingType());

        // Asserts
        assertThat(first).isNotSameAs(second);
    }

    @Test
    public void sameRequirementIsRecreatedForAnotherDeliverable() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivation(this.meterActivation, this.aggregationPeriod);
        VirtualReadingTypeRequirement first = factory.requirementFor(this.requirement, this.deliverable, this.dailyVirtualReadingType());
        ReadingTypeDeliverable otherDeliverable = mock(ReadingTypeDeliverable.class);

        // Business method
        VirtualReadingTypeRequirement second = factory.requirementFor(this.requirement, otherDeliverable, this.dailyVirtualReadingType());

        // Asserts
        assertThat(first).isNotSameAs(second);
    }

    @Test
    public void allRequirementsWhenNoneCreated() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivation(this.meterActivation, this.aggregationPeriod);

        // Business method + asserts
        assertThat(factory.allRequirements()).isEmpty();
    }

    @Test
    public void allRequirementsWhenOnlyOneCreated() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivation(this.meterActivation, this.aggregationPeriod);
        VirtualReadingTypeRequirement requirement = factory.requirementFor(this.requirement, this.deliverable, this.dailyVirtualReadingType());

        // Business method
        List<VirtualReadingTypeRequirement> requirements = factory.allRequirements();

        // Asserts
        assertThat(requirements).containsOnly(requirement);
    }

    @Test
    public void allRequirementsWhenMultipleCreated() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivation(this.meterActivation, this.aggregationPeriod);
        VirtualReadingTypeRequirement daily = factory.requirementFor(this.requirement, this.deliverable, this.dailyVirtualReadingType());
        VirtualReadingTypeRequirement hourly = factory.requirementFor(this.requirement, this.deliverable, this.hourlyVirtualReadingType());
        ReadingTypeDeliverable otherDeliverable = mock(ReadingTypeDeliverable.class);
        VirtualReadingTypeRequirement dailyForOtherDeliverable = factory.requirementFor(this.requirement, otherDeliverable, this.dailyVirtualReadingType());
        MeterActivation nextMeterActivation = mock(MeterActivation.class);
        when(nextMeterActivation.getRange()).thenReturn(Range.all());
        factory.nextMeterActivation(nextMeterActivation, this.aggregationPeriod);
        VirtualReadingTypeRequirement dailyForOtherMeterActivation = factory.requirementFor(this.requirement, this.deliverable, this.dailyVirtualReadingType());

        // Business method
        List<VirtualReadingTypeRequirement> requirements = factory.allRequirements();

        // Asserts
        assertThat(requirements).containsOnly(hourly, daily, dailyForOtherDeliverable, dailyForOtherMeterActivation);
    }

    @Test
    public void sameDeliverableIsCreatedOnlyOnce() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivation(this.meterActivation, this.aggregationPeriod);
        ReadingTypeDeliverableForMeterActivation deliverable =
                new ReadingTypeDeliverableForMeterActivation(
                        this.deliverable,
                        this.meterActivation,
                        Range.all(),
                        0,
                        mock(ServerExpressionNode.class),
                        this.dailyVirtualReadingType());
        VirtualReadingTypeDeliverable first = factory.deliverableFor(deliverable, this.dailyVirtualReadingType());

        // Business method
        VirtualReadingTypeDeliverable second = factory.deliverableFor(deliverable, this.dailyVirtualReadingType());

        // Asserts
        assertThat(first).isSameAs(second);
    }

    @Test
    public void sameDeliverableIsRecreatedForAnotherMeterActivation() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivation(this.meterActivation, this.aggregationPeriod);
        ReadingTypeDeliverableForMeterActivation deliverable =
                new ReadingTypeDeliverableForMeterActivation(
                        this.deliverable,
                        this.meterActivation,
                        Range.all(),
                        0,
                        mock(ServerExpressionNode.class),
                        this.dailyVirtualReadingType());
        VirtualReadingTypeDeliverable first = factory.deliverableFor(deliverable, this.dailyVirtualReadingType());
        factory.nextMeterActivation(mock(MeterActivation.class), this.aggregationPeriod);

        // Business method
        VirtualReadingTypeDeliverable second = factory.deliverableFor(deliverable, this.dailyVirtualReadingType());

        // Asserts
        assertThat(first).isNotSameAs(second);
    }

    @Test
    public void sameDeliverableIsRecreatedForAnotherInterval() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivation(this.meterActivation, this.aggregationPeriod);
        ReadingTypeDeliverableForMeterActivation deliverable =
                new ReadingTypeDeliverableForMeterActivation(
                        this.deliverable,
                        this.meterActivation,
                        Range.all(),
                        0,
                        mock(ServerExpressionNode.class),
                        this.dailyVirtualReadingType());
        VirtualReadingTypeDeliverable daily = factory.deliverableFor(deliverable, this.dailyVirtualReadingType());

        // Business method
        VirtualReadingTypeDeliverable hourly = factory.deliverableFor(deliverable, this.hourlyVirtualReadingType());

        // Asserts
        assertThat(daily).isNotSameAs(hourly);
    }

    @Test
    public void allDeliverablesWhenNoneCreated() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivation(this.meterActivation, this.aggregationPeriod);

        // Business method + asserts
        assertThat(factory.allDeliverables()).isEmpty();
    }

    @Test
    public void allDeliverablesWhenOnlyOneCreated() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivation(this.meterActivation, this.aggregationPeriod);
        ReadingTypeDeliverableForMeterActivation deliverable =
                new ReadingTypeDeliverableForMeterActivation(
                        this.deliverable,
                        this.meterActivation,
                        Range.all(),
                        0,
                        mock(ServerExpressionNode.class),
                        this.dailyVirtualReadingType());
        VirtualReadingTypeDeliverable daily = factory.deliverableFor(deliverable, this.dailyVirtualReadingType());

        // Business method + asserts
        assertThat(factory.allDeliverables()).containsOnly(daily);
    }

    @Test
    public void allDeliverablesWhenMultipleCreated() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivation(this.meterActivation, this.aggregationPeriod);
        ReadingTypeDeliverableForMeterActivation deliverable =
                new ReadingTypeDeliverableForMeterActivation(
                        this.deliverable,
                        this.meterActivation,
                        Range.all(),
                        0,
                        mock(ServerExpressionNode.class),
                        this.dailyVirtualReadingType());
        VirtualReadingTypeDeliverable daily = factory.deliverableFor(deliverable, this.dailyVirtualReadingType());
        VirtualReadingTypeDeliverable hourly = factory.deliverableFor(deliverable, this.hourlyVirtualReadingType());
        factory.nextMeterActivation(mock(MeterActivation.class), this.aggregationPeriod);
        VirtualReadingTypeDeliverable dailyForOtherMeterActivation = factory.deliverableFor(deliverable, this.dailyVirtualReadingType());

        // Business method + asserts
        assertThat(factory.allDeliverables()).containsOnly(daily, hourly, dailyForOtherMeterActivation);
    }

    private VirtualFactoryImpl testInstance() {
        return new VirtualFactoryImpl();
    }

    private VirtualReadingType hourlyVirtualReadingType() {
        return VirtualReadingType.from(this.mock60MinkWh());
    }

    private VirtualReadingType dailyVirtualReadingType() {
        return VirtualReadingType.from(this.mockDailyReadingType());
    }

    private ReadingType mock60MinkWh() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        return readingType;
    }

    private ReadingType mockDailyReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        return readingType;
    }

}