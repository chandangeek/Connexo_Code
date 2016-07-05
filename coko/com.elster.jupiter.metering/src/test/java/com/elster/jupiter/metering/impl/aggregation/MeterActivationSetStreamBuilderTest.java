package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
 * Tests the {@link MeterActivationSetStreamBuilder} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterActivationSetStreamBuilderTest {

    private static final Instant MAY_1ST_2016 = Instant.ofEpochMilli(1462053600000L);
    private static final Instant JUNE_1ST_2016 = Instant.ofEpochMilli(1464732000000L);
    private static final Instant JUNE_2ND_2016 = Instant.ofEpochMilli(1464818400000L);
    private static final Instant JULY_1ST_2016 = Instant.ofEpochMilli(1467324000000L);

    @Mock
    private UsagePoint usagePoint;
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

    private Range<Instant> period;

    @Before
    public void initializeMocks() {
        when(this.main.getKey()).thenReturn("meterole.main");
        when(this.main.getDisplayName()).thenReturn("Main");
        when(this.mainMeterActivation.getMeterRole()).thenReturn(Optional.of(this.main));
        when(this.check.getKey()).thenReturn("meterole.check");
        when(this.check.getDisplayName()).thenReturn("Check");
        when(this.checkMeterActivation.getMeterRole()).thenReturn(Optional.of(this.check));
    }

    @Test
    public void noMeterActivations() {
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        MeterActivationSetStreamBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> stream = builder.build().collect(Collectors.toList());

        // Asserts
        assertThat(stream).isEmpty();
    }

    @Test
    public void noOverlappingMeterActivations() {
        this.period = Range.openClosed(JUNE_1ST_2016, JUNE_2ND_2016);
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getRange()).thenReturn(Range.closedOpen(MAY_1ST_2016, Instant.ofEpochMilli(1462140000000L))); // (May 1st 2016, May 2nd 2016]
        when(meterActivation.overlaps(this.period)).thenReturn(false);
        when(this.usagePoint.getMeterActivations()).thenReturn(Collections.singletonList(meterActivation));
        MeterActivationSetStreamBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build().collect(Collectors.toList());

        // Asserts
        assertThat(meterActivationSets).isEmpty();
    }

    @Test
    public void twoMeterActivationsWithTheSamePeriod() {
        this.period = Range.openClosed(JUNE_1ST_2016, JULY_1ST_2016);
        when(this.mainMeterActivation.getRange()).thenReturn(Range.atLeast(JUNE_1ST_2016));
        when(this.mainMeterActivation.getStart()).thenReturn(JUNE_1ST_2016);
        when(this.mainMeterActivation.overlaps(this.period)).thenReturn(true);
        when(this.checkMeterActivation.getRange()).thenReturn(Range.atLeast(JUNE_1ST_2016));
        when(this.checkMeterActivation.getStart()).thenReturn(JUNE_1ST_2016);
        when(this.checkMeterActivation.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(this.mainMeterActivation, this.checkMeterActivation));
        when(this.usagePoint.getMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.of(this.configuration));
        MeterActivationSetStreamBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build().collect(Collectors.toList());

        // Asserts
        assertThat(meterActivationSets).hasSize(1);
        MeterActivationSet set = meterActivationSets.get(0);
        assertThat(set.getRange()).isEqualTo(Range.atLeast(JUNE_1ST_2016));
    }

    @Test
    public void twoMainMeterActivations() {
        this.period = Range.openClosed(MAY_1ST_2016, JULY_1ST_2016);
        MeterActivation mainActivation1 = mock(MeterActivation.class);
        when(mainActivation1.getRange()).thenReturn(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        when(mainActivation1.getStart()).thenReturn(MAY_1ST_2016);
        when(mainActivation1.overlaps(this.period)).thenReturn(true);
        MeterActivation mainActivation2 = mock(MeterActivation.class);
        when(mainActivation2.getRange()).thenReturn(Range.atLeast(JUNE_1ST_2016));
        when(mainActivation2.getStart()).thenReturn(JUNE_1ST_2016);
        when(mainActivation2.overlaps(this.period)).thenReturn(true);
        when(this.checkMeterActivation.getRange()).thenReturn(Range.atLeast(JUNE_1ST_2016));
        when(this.checkMeterActivation.getStart()).thenReturn(JUNE_1ST_2016);
        when(this.checkMeterActivation.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(mainActivation1, mainActivation2, this.checkMeterActivation));
        when(this.usagePoint.getMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.of(this.configuration));
        MeterActivationSetStreamBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build().collect(Collectors.toList());

        // Asserts
        assertThat(meterActivationSets).hasSize(2);
        MeterActivationSet set1 = meterActivationSets.get(0);
        assertThat(set1.getRange()).isEqualTo(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        MeterActivationSet set2 = meterActivationSets.get(1);
        assertThat(set2.getRange()).isEqualTo(Range.atLeast(JUNE_1ST_2016));
    }

    @Test
    public void twoMainMeterActivationsAndCheckThatClosesHalfwayThrough2ndMainActivation() {
        this.period = Range.openClosed(MAY_1ST_2016, JULY_1ST_2016);
        MeterActivation mainActivation1 = mock(MeterActivation.class);
        when(mainActivation1.getRange()).thenReturn(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        when(mainActivation1.getStart()).thenReturn(MAY_1ST_2016);
        when(mainActivation1.overlaps(this.period)).thenReturn(true);
        MeterActivation mainActivation2 = mock(MeterActivation.class);
        when(mainActivation2.getRange()).thenReturn(Range.atLeast(JUNE_1ST_2016));
        when(mainActivation2.getStart()).thenReturn(JUNE_1ST_2016);
        when(mainActivation2.overlaps(this.period)).thenReturn(true);
        when(this.checkMeterActivation.getRange()).thenReturn(Range.closedOpen(JUNE_1ST_2016, JUNE_2ND_2016));
        when(this.checkMeterActivation.getStart()).thenReturn(JUNE_1ST_2016);
        when(this.checkMeterActivation.overlaps(this.period)).thenReturn(true);
        when(this.usagePoint.getMeterActivations()).thenReturn(Arrays.asList(mainActivation1, mainActivation2, this.checkMeterActivation));
        when(this.usagePoint.getMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.of(this.configuration));
        MeterActivationSetStreamBuilder builder = this.getTestInstance();

        // Business method
        List<MeterActivationSet> meterActivationSets = builder.build().collect(Collectors.toList());

        // Asserts
        assertThat(meterActivationSets).hasSize(3);
        MeterActivationSet set1 = meterActivationSets.get(0);
        assertThat(set1.getRange()).isEqualTo(Range.closedOpen(MAY_1ST_2016, JUNE_1ST_2016));
        MeterActivationSet set2 = meterActivationSets.get(1);
        assertThat(set2.getRange()).isEqualTo(Range.closedOpen(JUNE_1ST_2016, JUNE_2ND_2016));
        MeterActivationSet set3 = meterActivationSets.get(2);
        assertThat(set3.getRange()).isEqualTo(Range.atLeast(JUNE_2ND_2016));
    }

    private MeterActivationSetStreamBuilder getTestInstance() {
        return new MeterActivationSetStreamBuilder(this.usagePoint, this.period);
    }

}