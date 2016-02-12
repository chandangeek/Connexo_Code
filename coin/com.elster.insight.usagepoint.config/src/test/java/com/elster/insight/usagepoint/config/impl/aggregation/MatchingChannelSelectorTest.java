package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;

import com.elster.insight.usagepoint.config.ReadingTypeRequirement;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link MatchingChannelSelector} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-11 (15:19)
 */
public class MatchingChannelSelectorTest {

    @Test
    public void meterActivationMatchingChannelsAreUsed() {
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(requirement.getMatchingChannelsFor(meterActivation)).thenReturn(Collections.emptyList());

        // Business method
        new MatchingChannelSelector(requirement, meterActivation);

        // Asserts
        verify(requirement).getMatchingChannelsFor(meterActivation);
    }

    @Test
    public void noPreferredChannelWithZeroMatchingChannels() {
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Collections.emptyList());// As promised: no matching channels

        for (IntervalLength intervalLength : IntervalLength.values()) {
            // Business method
            Optional<Channel> preferredChannel = testInstance.getPreferredChannel(intervalLength);

            // Asserts
            assertThat(preferredChannel).as("Was not expecting a preferred channel for " + intervalLength.name()).isEmpty();
        }
    }

    @Test
    public void noPreferredChannelWhenMatchingChannelsAreSmallerOrIncompatible() {
        MatchingChannelSelector testInstance =
                new MatchingChannelSelector(Arrays.asList(
                        this.mockChannelFor(this.mock2minReadingType()),
                        this.mockChannelFor(this.mock10minReadingType()),
                        this.mockChannelFor(this.mock20minReadingType())
                ));

        for (IntervalLength intervalLength : EnumSet.of(IntervalLength.MINUTE3, IntervalLength.MINUTE5, IntervalLength.MINUTE15)) {
            // Business method
            Optional<Channel> preferredChannel = testInstance.getPreferredChannel(intervalLength);

            // Asserts
            assertThat(preferredChannel).as("Was not expecting a preferred channel for " + intervalLength.name()).isEmpty();
        }
    }

    @Test
    public void noPreferredChannelWhenMatchingChannelsAreAllBigger() {
        MatchingChannelSelector testInstance =
                new MatchingChannelSelector(Arrays.asList(
                        this.mockChannelFor(this.mock10minReadingType()),
                        this.mockChannelFor(this.mock15minReadingType()),
                        this.mockChannelFor(this.mock20minReadingType()),
                        this.mockChannelFor(this.mockHourlyReadingType())
                ));

        for (IntervalLength intervalLength : EnumSet.of(IntervalLength.MINUTE1, IntervalLength.MINUTE2, IntervalLength.MINUTE3, IntervalLength.MINUTE4, IntervalLength.MINUTE6)) {
            // Business method
            Optional<Channel> preferredChannel = testInstance.getPreferredChannel(intervalLength);

            // Asserts
            assertThat(preferredChannel).as("Was not expecting a preferred channel for " + intervalLength.name()).isEmpty();
        }
    }

    @Test
    public void hourlyIsClosestMatchForHourly() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        Channel hourly = this.mockChannelFor(this.mockHourlyReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes, hourly));

        // Business method
        Optional<Channel> preferredChannel = testInstance.getPreferredChannel(IntervalLength.HOUR1);

        // Asserts
        assertThat(preferredChannel).isPresent();
        assertThat(preferredChannel).contains(hourly).as("Wrong match, found " + IntervalLength.from(preferredChannel.get().getMainReadingType()));
    }

    @Test
    public void tenMinutesIsBestMatchForHourlyWhenOnly10And15MinAreAvailable() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes));

        // Business method
        Optional<Channel> preferredChannel = testInstance.getPreferredChannel(IntervalLength.HOUR1);

        // Asserts
        assertThat(preferredChannel).contains(tenMinutes);
    }

    @Test
    public void noPreferredIntervalWithZeroMatchingChannels() {
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Collections.emptyList());// As promised: no matching channels

        for (IntervalLength intervalLength : IntervalLength.values()) {
            // Business method
            Optional<IntervalLength> preferredInterval = testInstance.getPreferredInterval(intervalLength);

            // Asserts
            assertThat(preferredInterval).as("Was not expecting a preferred interval for " + intervalLength.name()).isEmpty();
        }
    }

    @Test
    public void noPreferredIntervalWhenMatchingChannelsAreSmallerOrIncompatible() {
        MatchingChannelSelector testInstance =
                new MatchingChannelSelector(Arrays.asList(
                        this.mockChannelFor(this.mock2minReadingType()),
                        this.mockChannelFor(this.mock10minReadingType()),
                        this.mockChannelFor(this.mock20minReadingType())
                ));

        for (IntervalLength intervalLength : EnumSet.of(IntervalLength.MINUTE3, IntervalLength.MINUTE5, IntervalLength.MINUTE15)) {
            // Business method
            Optional<IntervalLength> preferredInterval = testInstance.getPreferredInterval(intervalLength);

            // Asserts
            assertThat(preferredInterval).as("Was not expecting a preferred interval for " + intervalLength.name()).isEmpty();
        }
    }

    @Test
    public void noPreferredIntervalWhenMatchingChannelsAreAllBigger() {
        MatchingChannelSelector testInstance =
                new MatchingChannelSelector(Arrays.asList(
                        this.mockChannelFor(this.mock10minReadingType()),
                        this.mockChannelFor(this.mock15minReadingType()),
                        this.mockChannelFor(this.mock20minReadingType()),
                        this.mockChannelFor(this.mockHourlyReadingType())
                ));

        for (IntervalLength intervalLength : EnumSet.of(IntervalLength.MINUTE1, IntervalLength.MINUTE2, IntervalLength.MINUTE3, IntervalLength.MINUTE4, IntervalLength.MINUTE6)) {
            // Business method
            Optional<IntervalLength> preferredInterval = testInstance.getPreferredInterval(intervalLength);

            // Asserts
            assertThat(preferredInterval).as("Was not expecting a preferred interval for " + intervalLength.name()).isEmpty();
        }
    }

    @Test
    public void hourlyIntervalIsClosestMatchForHourly() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        Channel hourly = this.mockChannelFor(this.mockHourlyReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes, hourly));

        // Business method
        Optional<IntervalLength> preferredInterval = testInstance.getPreferredInterval(IntervalLength.HOUR1);

        // Asserts
        assertThat(preferredInterval).isPresent();
        assertThat(preferredInterval).contains(IntervalLength.HOUR1).as("Wrong match, found " + preferredInterval.get());
    }

    @Test
    public void tenMinutesIntervalIsBestMatchForHourlyWhenOnly10And15MinAreAvailable() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes));

        // Business method
        Optional<IntervalLength> preferredInterval = testInstance.getPreferredInterval(IntervalLength.HOUR1);

        // Asserts
        assertThat(preferredInterval).contains(IntervalLength.MINUTE10);
    }

    @Test
    public void intervalIsNeverSupportedWithZeroMatchingChannels() {
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Collections.emptyList());// As promised: no matching channels

        for (IntervalLength intervalLength : IntervalLength.values()) {
            // Business method
            boolean isSupported = testInstance.isIntervalSupported(intervalLength);

            // Asserts
            assertThat(isSupported).isFalse().as("Was not expecting interval " + intervalLength.name() + " to be supported without any matching channel");
        }
    }

    @Test
    public void intervalIsNotSupportedWhenMatchingChannelsAreSmallerOrIncompatible() {
        MatchingChannelSelector testInstance =
                new MatchingChannelSelector(Arrays.asList(
                        this.mockChannelFor(this.mock2minReadingType()),
                        this.mockChannelFor(this.mock10minReadingType()),
                        this.mockChannelFor(this.mock20minReadingType())
                ));

        for (IntervalLength intervalLength : EnumSet.of(IntervalLength.MINUTE3, IntervalLength.MINUTE5, IntervalLength.MINUTE15)) {
            // Business method
            boolean isSupported = testInstance.isIntervalSupported(intervalLength);

            // Asserts
            assertThat(isSupported).isFalse().as("Was not expecting interval " + intervalLength.name() + " to be supported");
        }
    }

    @Test
    public void intervalIsNotSupportedWhenMatchingChannelsAreAllBigger() {
        MatchingChannelSelector testInstance =
                new MatchingChannelSelector(Arrays.asList(
                        this.mockChannelFor(this.mock10minReadingType()),
                        this.mockChannelFor(this.mock15minReadingType()),
                        this.mockChannelFor(this.mock20minReadingType()),
                        this.mockChannelFor(this.mockHourlyReadingType())
                ));

        for (IntervalLength intervalLength : EnumSet.of(IntervalLength.MINUTE1, IntervalLength.MINUTE2, IntervalLength.MINUTE3, IntervalLength.MINUTE4, IntervalLength.MINUTE6)) {
            // Business method
            boolean isSupported = testInstance.isIntervalSupported(intervalLength);

            // Asserts
            assertThat(isSupported).isFalse().as("Was not expecting interval " + intervalLength.name() + " to be supported");
        }
    }

    @Test
    public void hourlyIntervalIsSupportedWhenHourlyIsAvailable() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        Channel hourly = this.mockChannelFor(this.mockHourlyReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes, hourly));

        // Business method
        boolean isSupported = testInstance.isIntervalSupported(IntervalLength.HOUR1);

        // Asserts
        assertThat(isSupported).isTrue();
    }

    @Test
    public void hourlyIntervalIsSupportedWhenOnly10And15MinAreAvailable() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes));

        // Business method
        boolean isSupported = testInstance.isIntervalSupported(IntervalLength.HOUR1);

        // Asserts
        assertThat(isSupported).isTrue();
    }

    private Channel mockChannelFor(ReadingType readingType) {
        Channel channel = mock(Channel.class);
        when(channel.getMainReadingType()).thenReturn(readingType);
        return channel;
    }

    private ReadingType mock2minReadingType() {
        ReadingType meterActivationReadingType = mock(ReadingType.class);
        when(meterActivationReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(meterActivationReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE2);
        return meterActivationReadingType;
    }

    private ReadingType mock5minReadingType() {
        ReadingType meterActivationReadingType = mock(ReadingType.class);
        when(meterActivationReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(meterActivationReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE5);
        return meterActivationReadingType;
    }

    private ReadingType mock10minReadingType() {
        ReadingType meterActivationReadingType = mock(ReadingType.class);
        when(meterActivationReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(meterActivationReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE10);
        return meterActivationReadingType;
    }

    private ReadingType mock15minReadingType() {
        ReadingType meterActivationReadingType = mock(ReadingType.class);
        when(meterActivationReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(meterActivationReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        return meterActivationReadingType;
    }

    private ReadingType mock20minReadingType() {
        ReadingType meterActivationReadingType = mock(ReadingType.class);
        when(meterActivationReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(meterActivationReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE20);
        return meterActivationReadingType;
    }

    private ReadingType mockHourlyReadingType() {
        ReadingType meterActivationReadingType = mock(ReadingType.class);
        when(meterActivationReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(meterActivationReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        return meterActivationReadingType;
    }

}