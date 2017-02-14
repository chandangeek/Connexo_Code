/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.ServerUsagePoint;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link MeterActivationSetBuilder} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterActivationSetBuilderTest {

    private static final Instant MAY_1ST_2016 = Instant.ofEpochMilli(1462053600000L);
    private static final Instant JUNE_1ST_2016 = Instant.ofEpochMilli(1464732000000L);
    private static final Instant JUNE_2ND_2016 = Instant.ofEpochMilli(1464818400000L);
    private static final Instant JULY_1ST_2016 = Instant.ofEpochMilli(1467324000000L);
    private static final Instant AUG_1ST_2016 = Instant.ofEpochMilli(1470002400000L);

    @Mock
    private ServerUsagePoint usagePoint;
    @Mock
    private MeterRole main;
    @Mock
    private MeterActivation mainMeterActivation;
    @Mock
    private MeterRole check;
    @Mock
    private MeterActivation checkMeterActivation;
    @Mock
    private UsagePointMetrologyConfiguration configuration;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint;

    private Range<Instant> period;

    @Before
    public void initializeMocks() {
        when(this.main.getKey()).thenReturn("meterole.main");
        when(this.main.getDisplayName()).thenReturn("Main");
        when(this.mainMeterActivation.getMeterRole()).thenReturn(Optional.of(this.main));
        when(this.check.getKey()).thenReturn("meterole.check");
        when(this.check.getDisplayName()).thenReturn("Check");
        when(this.checkMeterActivation.getMeterRole()).thenReturn(Optional.of(this.check));
        when(this.usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.of(this.effectiveMetrologyConfigurationOnUsagePoint));
        when(this.usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(this.effectiveMetrologyConfigurationOnUsagePoint));
        when(this.effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(this.configuration);
    }

    @Test
    public void noMeterActivations() {
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        this.period = Range.atLeast(MAY_1ST_2016);
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> stream = builder.build();

        // Asserts
        assertThat(stream).isEmpty();
    }

    @Test
    public void noOverlappingMeterActivations() {
        this.period = Range.closedOpen(JUNE_1ST_2016, JUNE_2ND_2016);
        MeterActivation meterActivation = mock(MeterActivation.class);
        this.mockRange(meterActivation, Range.closedOpen(MAY_1ST_2016, Instant.ofEpochMilli(1462140000000L)));  // (May 1st 2016, May 2nd 2016]
        when(meterActivation.overlaps(this.period)).thenReturn(false);
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.singletonList(meterActivation));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).isEmpty();
    }

    @Test
    public void twoMeterActivationsWithTheSamePeriod() {
        this.period = Range.closedOpen(JUNE_1ST_2016, JULY_1ST_2016);
        this.mockRange(this.mainMeterActivation, Range.atLeast(JUNE_1ST_2016));
        when(this.mainMeterActivation.overlaps(this.period)).thenReturn(true);
        this.mockRange(this.checkMeterActivation, Range.atLeast(JUNE_1ST_2016));
        when(this.checkMeterActivation.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(this.mainMeterActivation, this.checkMeterActivation));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(1);
        MeterActivationSet set = meterActivationSets.get(0);
        assertThat(set.getRange()).isEqualTo(Range.closedOpen(JUNE_1ST_2016, JULY_1ST_2016));
        assertThat(set.sequenceNumber()).isEqualTo(1);
    }

    @Test
    public void twoMainMeterActivations() {
        this.period = Range.closedOpen(MAY_1ST_2016, JULY_1ST_2016);
        MeterActivation mainActivation1 = mock(MeterActivation.class);
        this.mockRange(mainActivation1, Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        when(mainActivation1.overlaps(this.period)).thenReturn(true);
        MeterActivation mainActivation2 = mock(MeterActivation.class);
        this.mockRange(mainActivation2, Range.atLeast(JUNE_1ST_2016));
        when(mainActivation2.overlaps(this.period)).thenReturn(true);
        this.mockRange(this.checkMeterActivation, Range.atLeast(JUNE_1ST_2016));
        when(this.checkMeterActivation.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(mainActivation1, mainActivation2, this.checkMeterActivation));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(2);
        MeterActivationSet set1 = meterActivationSets.get(0);
        assertThat(set1.getRange()).isEqualTo(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        assertThat(set1.sequenceNumber()).isEqualTo(1);
        MeterActivationSet set2 = meterActivationSets.get(1);
        assertThat(set2.getRange()).isEqualTo(Range.closedOpen(JUNE_1ST_2016, JULY_1ST_2016));
        assertThat(set2.sequenceNumber()).isEqualTo(2);
    }

    @Test
    public void twoMainMeterActivationsAndCheckThatClosesHalfwayThrough2ndMainActivation() {
        this.period = Range.closedOpen(MAY_1ST_2016, JULY_1ST_2016);
        MeterActivation mainActivation1 = mock(MeterActivation.class);
        this.mockRange(mainActivation1, Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        when(mainActivation1.overlaps(this.period)).thenReturn(true);
        MeterActivation mainActivation2 = mock(MeterActivation.class);
        this.mockRange(mainActivation2, Range.atLeast(JUNE_1ST_2016));
        when(mainActivation2.overlaps(this.period)).thenReturn(true);
        this.mockRange(this.checkMeterActivation, Range.closedOpen(JUNE_1ST_2016, JUNE_2ND_2016));
        when(this.checkMeterActivation.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(mainActivation1, mainActivation2, this.checkMeterActivation));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(3);
        MeterActivationSet set1 = meterActivationSets.get(0);
        assertThat(set1.getRange()).isEqualTo(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        assertThat(set1.sequenceNumber()).isEqualTo(1);
        MeterActivationSet set2 = meterActivationSets.get(1);
        assertThat(set2.getRange()).isEqualTo(Range.closedOpen(JUNE_1ST_2016, JUNE_2ND_2016));
        assertThat(set2.sequenceNumber()).isEqualTo(2);
        MeterActivationSet set3 = meterActivationSets.get(2);
        assertThat(set3.getRange()).isEqualTo(Range.closedOpen(JUNE_2ND_2016, JULY_1ST_2016));
        assertThat(set3.sequenceNumber()).isEqualTo(3);
    }

    @Test
    public void twoMeterActivationsWithGapInBetween() {
        this.period = Range.closedOpen(MAY_1ST_2016, AUG_1ST_2016);
        MeterActivation ma1 = mock(MeterActivation.class);
        this.mockRange(ma1, Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        when(ma1.overlaps(this.period)).thenReturn(true);
        MeterActivation ma2 = mock(MeterActivation.class);
        this.mockRange(ma2, Range.atLeast(JULY_1ST_2016));
        when(ma2.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(ma1, ma2));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(2);
        MeterActivationSet set1 = meterActivationSets.get(0);
        assertThat(set1.getRange()).isEqualTo(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        assertThat(set1.sequenceNumber()).isEqualTo(1);
        MeterActivationSet set2 = meterActivationSets.get(1);
        assertThat(set2.getRange()).isEqualTo(Range.closedOpen(JULY_1ST_2016, AUG_1ST_2016));
        assertThat(set2.sequenceNumber()).isEqualTo(2);
    }

    @Test
    public void twoMeterActivationsWithGapInBetweenAndRequestedPeriodThatStartsAtEndOfFirstMeterActivation() {
        this.period = Range.closedOpen(JUNE_1ST_2016, AUG_1ST_2016);
        MeterActivation ma1 = mock(MeterActivation.class);
        this.mockRange(ma1, Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        when(ma1.overlaps(this.period)).thenReturn(true);
        MeterActivation ma2 = mock(MeterActivation.class);
        this.mockRange(ma2, Range.atLeast(JULY_1ST_2016));
        when(ma2.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(ma1, ma2));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(1);
        MeterActivationSet set1 = meterActivationSets.get(0);
        assertThat(set1.getRange()).isEqualTo(Range.closedOpen(JULY_1ST_2016, AUG_1ST_2016));
        assertThat(set1.sequenceNumber()).isEqualTo(1);
    }

    @Test
    public void twoClosedMeterActivationsWithGapInBetween() {
        this.period = Range.closedOpen(MAY_1ST_2016, AUG_1ST_2016);
        MeterActivation ma1 = mock(MeterActivation.class);
        this.mockRange(ma1, Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        when(ma1.overlaps(this.period)).thenReturn(true);
        MeterActivation ma2 = mock(MeterActivation.class);
        this.mockRange(ma2, Range.closedOpen(JULY_1ST_2016, AUG_1ST_2016));
        when(ma2.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(ma1, ma2));
        MeterActivationSetBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build();

        // Asserts
        assertThat(meterActivationSets).hasSize(2);
        MeterActivationSet set1 = meterActivationSets.get(0);
        assertThat(set1.getRange()).isEqualTo(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        assertThat(set1.sequenceNumber()).isEqualTo(1);
        MeterActivationSet set2 = meterActivationSets.get(1);
        assertThat(set2.getRange()).isEqualTo(Range.closedOpen(JULY_1ST_2016, AUG_1ST_2016));
        assertThat(set2.sequenceNumber()).isEqualTo(2);
    }

    private MeterActivationSetBuilder getTestInstance() {
        return new MeterActivationSetBuilder(this.usagePoint, this.period);
    }

    private void mockRange(MeterActivation meterActivation, Range<Instant> range) {
        when(meterActivation.getRange()).thenReturn(range);
        when(meterActivation.getStart()).thenReturn(range.lowerEndpoint());
        if (range.hasUpperBound()) {
            when(meterActivation.getEnd()).thenReturn(range.upperEndpoint());
        } else {
            when(meterActivation.getEnd()).thenReturn(null);
        }
    }

}