/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.Range;

import java.time.Instant;
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
    private MeterActivationSet meterActivationSet;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private MeteringDataModelService dataModelService;

    private Range<Instant> aggregationPeriod;

    @Before
    public void initializeMocks() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
        when(this.meterActivationSet.getRange()).thenReturn(Range.atLeast(jan1st2016));
        this.aggregationPeriod = Range.atLeast(jan1st2016);
        ReadingType daily_kWh = this.mockDailyReadingType();
        when(this.deliverable.getReadingType()).thenReturn(daily_kWh);
        when(this.dataModelService.getThesaurus()).thenReturn(this.thesaurus);
    }

    @Test(expected = IllegalStateException.class)
    public void requirementsNotSupportedWithoutCurrentMeterActivation() {
        VirtualFactoryImpl factory = this.testInstance();

        // Business method
        factory.requirementFor(Formula.Mode.AUTO, this.requirement, this.deliverable, this.dailyVirtualReadingType());

        // Asserts: see expected exception rule
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
        int before = factory.sequenceNumber();

        // Business method
        factory.nextMeterActivationSet(this.meterActivationSet, this.aggregationPeriod);

        // Asserts
        int after = factory.sequenceNumber();
        assertThat(after).isGreaterThan(before);
    }

    @Test
    public void sameRequirementIsCreatedOnlyOnce() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivationSet(this.meterActivationSet, this.aggregationPeriod);
        VirtualReadingTypeRequirement first = factory.requirementFor(Formula.Mode.AUTO, this.requirement, this.deliverable, this.dailyVirtualReadingType());

        // Business method
        VirtualReadingTypeRequirement second = factory.requirementFor(Formula.Mode.AUTO, this.requirement, this.deliverable, this.dailyVirtualReadingType());

        // Asserts
        assertThat(first).isSameAs(second);
    }

    @Test
    public void sameRequirementIsRecreatedForAnotherMeterActivation() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivationSet(this.meterActivationSet, this.aggregationPeriod);
        VirtualReadingTypeRequirement first = factory.requirementFor(Formula.Mode.AUTO, this.requirement, this.deliverable, this.dailyVirtualReadingType());
        MeterActivationSet nextMeterActivationSet = mock(MeterActivationSet.class);
        when(nextMeterActivationSet.getRange()).thenReturn(Range.all());
        factory.nextMeterActivationSet(nextMeterActivationSet, this.aggregationPeriod);

        // Business method
        VirtualReadingTypeRequirement second = factory.requirementFor(Formula.Mode.AUTO, this.requirement, this.deliverable, this.dailyVirtualReadingType());

        // Asserts
        assertThat(first).isNotSameAs(second);
    }

    @Test
    public void sameRequirementIsRecreatedForAnotherInterval() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivationSet(this.meterActivationSet, this.aggregationPeriod);
        VirtualReadingTypeRequirement first = factory.requirementFor(Formula.Mode.AUTO, this.requirement, this.deliverable, this.dailyVirtualReadingType());

        // Business method
        VirtualReadingTypeRequirement second = factory.requirementFor(Formula.Mode.AUTO, this.requirement, this.deliverable, this.hourlyVirtualReadingType());

        // Asserts
        assertThat(first).isNotSameAs(second);
    }

    @Test
    public void sameRequirementIsRecreatedForAnotherDeliverable() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivationSet(this.meterActivationSet, this.aggregationPeriod);
        VirtualReadingTypeRequirement first = factory.requirementFor(Formula.Mode.AUTO, this.requirement, this.deliverable, this.dailyVirtualReadingType());
        ReadingTypeDeliverable otherDeliverable = mock(ReadingTypeDeliverable.class);

        // Business method
        VirtualReadingTypeRequirement second = factory.requirementFor(Formula.Mode.AUTO, this.requirement, otherDeliverable, this.dailyVirtualReadingType());

        // Asserts
        assertThat(first).isNotSameAs(second);
    }

    @Test
    public void allRequirementsWhenNoneCreated() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivationSet(this.meterActivationSet, this.aggregationPeriod);

        // Business method + asserts
        assertThat(factory.allRequirements()).isEmpty();
    }

    @Test
    public void allRequirementsWhenOnlyOneCreated() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivationSet(this.meterActivationSet, this.aggregationPeriod);
        VirtualReadingTypeRequirement requirement = factory.requirementFor(Formula.Mode.AUTO, this.requirement, this.deliverable, this.dailyVirtualReadingType());

        // Business method
        List<VirtualReadingTypeRequirement> requirements = factory.allRequirements();

        // Asserts
        assertThat(requirements).containsOnly(requirement);
    }

    @Test
    public void allRequirementsWhenMultipleCreated() {
        VirtualFactoryImpl factory = this.testInstance();
        factory.nextMeterActivationSet(this.meterActivationSet, this.aggregationPeriod);
        VirtualReadingTypeRequirement daily = factory.requirementFor(Formula.Mode.AUTO, this.requirement, this.deliverable, this.dailyVirtualReadingType());
        VirtualReadingTypeRequirement hourly = factory.requirementFor(Formula.Mode.AUTO, this.requirement, this.deliverable, this.hourlyVirtualReadingType());
        ReadingTypeDeliverable otherDeliverable = mock(ReadingTypeDeliverable.class);
        VirtualReadingTypeRequirement dailyForOtherDeliverable = factory.requirementFor(Formula.Mode.AUTO, this.requirement, otherDeliverable, this.dailyVirtualReadingType());
        MeterActivationSet nextMeterActivationSet = mock(MeterActivationSet.class);
        when(nextMeterActivationSet.getRange()).thenReturn(Range.all());
        factory.nextMeterActivationSet(nextMeterActivationSet, this.aggregationPeriod);
        VirtualReadingTypeRequirement dailyForOtherMeterActivation = factory.requirementFor(Formula.Mode.AUTO, this.requirement, this.deliverable, this.dailyVirtualReadingType());

        // Business method
        List<VirtualReadingTypeRequirement> requirements = factory.allRequirements();

        // Asserts
        assertThat(requirements).containsOnly(hourly, daily, dailyForOtherDeliverable, dailyForOtherMeterActivation);
    }

    private VirtualFactoryImpl testInstance() {
        return new VirtualFactoryImpl(this.dataModelService);
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