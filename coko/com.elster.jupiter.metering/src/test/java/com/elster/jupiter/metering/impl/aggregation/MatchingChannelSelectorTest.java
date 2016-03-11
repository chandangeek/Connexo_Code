package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.Test;

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
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

            // Business method
            Optional<Channel> preferredChannel = testInstance.getPreferredChannel(readingType);

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
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

            // Business method
            Optional<Channel> preferredChannel = testInstance.getPreferredChannel(readingType);

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
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

            // Business method
            Optional<Channel> preferredChannel = testInstance.getPreferredChannel(readingType);

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
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

        // Business method
        Optional<Channel> preferredChannel = testInstance.getPreferredChannel(readingType);

        // Asserts
        assertThat(preferredChannel).isPresent();
        assertThat(preferredChannel)
                .as("Wrong match, found " + IntervalLength.from(preferredChannel.get().getMainReadingType()) + " but got " + IntervalLength.from(hourly.getMainReadingType()))
                .contains(hourly);
    }

    @Test
    public void fifteenMinutesIsBestMatchForHourlyWhenOnly10And15MinAreAvailable() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes));
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

        // Business method
        Optional<Channel> preferredChannel = testInstance.getPreferredChannel(readingType);

        // Asserts
        assertThat(preferredChannel).contains(fifteenMinutes);
    }

    @Test
    public void noPreferredIntervalWithZeroMatchingChannels() {
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Collections.emptyList());// As promised: no matching channels

        for (IntervalLength intervalLength : IntervalLength.values()) {
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

            // Business method
            Optional<VirtualReadingType> preferredReadingType = testInstance.getPreferredReadingType(readingType);

            // Asserts
            assertThat(preferredReadingType).as("Was not expecting a preferred reading type for " + intervalLength.name()).isEmpty();
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
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

            // Business method
            Optional<VirtualReadingType> preferredReadingType = testInstance.getPreferredReadingType(readingType);

            // Asserts
            assertThat(preferredReadingType).as("Was not expecting a preferred reading type for " + intervalLength.name()).isEmpty();
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
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

            // Business method
            Optional<VirtualReadingType> preferredReadingType = testInstance.getPreferredReadingType(readingType);

            // Asserts
            assertThat(preferredReadingType).as("Was not expecting a preferred reading type for " + intervalLength.name()).isEmpty();
        }
    }

    @Test
    public void hourlyIntervalIsClosestMatchForHourly() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        Channel hourly = this.mockChannelFor(this.mockHourlyReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes, hourly));
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

        // Business method
        Optional<VirtualReadingType> preferredReadingType = testInstance.getPreferredReadingType(readingType);

        // Asserts
        assertThat(preferredReadingType).isPresent();
        assertThat(preferredReadingType).contains(readingType).as("Wrong match, found " + preferredReadingType.get());
    }

    @Test
    public void fifteenMinutesIntervalIsBestMatchForHourlyWhenOnly10And15MinAreAvailable() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes));
        VirtualReadingType hourlyReadingType = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);
        VirtualReadingType min15ReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

        // Business method
        Optional<VirtualReadingType> preferredReadingType = testInstance.getPreferredReadingType(hourlyReadingType);

        // Asserts
        assertThat(preferredReadingType).contains(min15ReadingType);
    }

    @Test
    public void intervalIsNeverSupportedWithZeroMatchingChannels() {
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Collections.emptyList());// As promised: no matching channels

        for (IntervalLength intervalLength : IntervalLength.values()) {
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

            // Business method
            boolean isSupported = testInstance.isReadingTypeSupported(readingType);

            // Asserts
            assertThat(isSupported).isFalse().as("Was not expecting reading type for " + intervalLength.name() + " to be supported without any matching channel");
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
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

            // Business method
            boolean isSupported = testInstance.isReadingTypeSupported(readingType);

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
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

            // Business method
            boolean isSupported = testInstance.isReadingTypeSupported(readingType);

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
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

        // Business method
        boolean isSupported = testInstance.isReadingTypeSupported(readingType);

        // Asserts
        assertThat(isSupported).isTrue();
    }

    @Test
    public void hourlyIntervalIsSupportedWhenOnly10And15MinAreAvailable() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes));
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

        // Business method
        boolean isSupported = testInstance.isReadingTypeSupported(readingType);

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
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE5);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        return readingType;
    }

    private ReadingType mock10minReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE10);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        return readingType;
    }

    private ReadingType mock15minReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        return readingType;
    }

    private ReadingType mock20minReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE20);
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

}