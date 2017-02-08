/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
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
        MeterActivationSet meterActivationSet = mock(MeterActivationSet.class);
        when(meterActivationSet.getMatchingChannelsFor(requirement)).thenReturn(Collections.emptyList());

        // Business method
        new MatchingChannelSelector(requirement, meterActivationSet, Formula.Mode.AUTO);

        // Asserts
        verify(meterActivationSet).getMatchingChannelsFor(requirement);
    }

    @Test
    public void noPreferredChannelWithZeroMatchingChannels() {
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Collections.emptyList(), Formula.Mode.AUTO);// As promised: no matching channels

        for (IntervalLength intervalLength : IntervalLength.values()) {
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);

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
                ), Formula.Mode.AUTO);

        for (IntervalLength intervalLength : EnumSet.of(IntervalLength.MINUTE3, IntervalLength.MINUTE5, IntervalLength.MINUTE15)) {
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);

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
                ), Formula.Mode.AUTO);

        for (IntervalLength intervalLength : EnumSet.of(IntervalLength.MINUTE1, IntervalLength.MINUTE2, IntervalLength.MINUTE3, IntervalLength.MINUTE4, IntervalLength.MINUTE6)) {
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);

            // Business method
            Optional<Channel> preferredChannel = testInstance.getPreferredChannel(readingType);

            // Asserts
            assertThat(preferredChannel).as("Was not expecting a preferred channel for " + intervalLength.name()).isEmpty();
        }
    }

    @Test
    public void wattRegisterChannel() {
        MatchingChannelSelector testInstance =
                new MatchingChannelSelector(Collections.singletonList(
                        this.mockChannelFor(this.mockWattRegisterReadingType())), Formula.Mode.AUTO);

        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.NOT_SUPPORTED, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        Optional<Channel> preferredChannel = testInstance.getPreferredChannel(readingType);

        // Asserts
        assertThat(preferredChannel).isPresent();
    }

    @Test
    public void wattRegisterChannelWithWattTimeSeries() {
        MatchingChannelSelector testInstance =
                new MatchingChannelSelector(Collections.singletonList(
                        this.mockChannelFor(this.mock15minReadingType(ReadingTypeUnit.WATT))), Formula.Mode.AUTO);

        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.NOT_SUPPORTED, MetricMultiplier.ZERO, ReadingTypeUnit.WATT, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        Optional<Channel> preferredChannel = testInstance.getPreferredChannel(readingType);

        // Asserts
        assertThat(preferredChannel).isEmpty();
    }

    @Test
    public void volumeRegisterChannelWithUnitConversion() {
        MatchingChannelSelector testInstance =
                new MatchingChannelSelector(Collections.singletonList(
                        this.mockChannelFor(this.mockLiterRegisterReadingType())), Formula.Mode.AUTO);

        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.NOT_SUPPORTED, MetricMultiplier.ZERO, ReadingTypeUnit.USGALLON, Accumulation.BULKQUANTITY, Commodity.POTABLEWATER);

        // Business method
        Optional<Channel> preferredChannel = testInstance.getPreferredChannel(readingType);

        // Asserts
        assertThat(preferredChannel).isPresent();
    }

    @Test
    public void hourlyIsClosestMatchForHourly() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        Channel hourly = this.mockChannelFor(this.mockHourlyReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes, hourly), Formula.Mode.AUTO);
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);

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
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes), Formula.Mode.AUTO);
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);

        // Business method
        Optional<Channel> preferredChannel = testInstance.getPreferredChannel(readingType);

        // Asserts
        assertThat(preferredChannel).contains(fifteenMinutes);
    }

    @Test
    public void noPreferredReadingTypeWithZeroMatchingChannels() {
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Collections.emptyList(), Formula.Mode.AUTO);// As promised: no matching channels

        for (IntervalLength intervalLength : IntervalLength.values()) {
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);

            // Business method
            Optional<VirtualReadingType> preferredReadingType = testInstance.getPreferredReadingType(readingType);

            // Asserts
            assertThat(preferredReadingType).as("Was not expecting a preferred reading type for " + intervalLength.name()).isEmpty();
        }
    }

    @Test
    public void noPreferredReadingTypeWhenMatchingChannelsAreSmallerOrIncompatible() {
        MatchingChannelSelector testInstance =
                new MatchingChannelSelector(Arrays.asList(
                        this.mockChannelFor(this.mock2minReadingType()),
                        this.mockChannelFor(this.mock10minReadingType()),
                        this.mockChannelFor(this.mock20minReadingType())
                ), Formula.Mode.AUTO);

        for (IntervalLength intervalLength : EnumSet.of(IntervalLength.MINUTE3, IntervalLength.MINUTE5, IntervalLength.MINUTE15)) {
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);

            // Business method
            Optional<VirtualReadingType> preferredReadingType = testInstance.getPreferredReadingType(readingType);

            // Asserts
            assertThat(preferredReadingType).as("Was not expecting a preferred reading type for " + intervalLength.name()).isEmpty();
        }
    }

    @Test
    public void noPreferredReadingTypeWhenMatchingChannelsAreAllBigger() {
        MatchingChannelSelector testInstance =
                new MatchingChannelSelector(Arrays.asList(
                        this.mockChannelFor(this.mock10minReadingType()),
                        this.mockChannelFor(this.mock15minReadingType()),
                        this.mockChannelFor(this.mock20minReadingType()),
                        this.mockChannelFor(this.mockHourlyReadingType())
                ), Formula.Mode.AUTO);

        for (IntervalLength intervalLength : EnumSet.of(IntervalLength.MINUTE1, IntervalLength.MINUTE2, IntervalLength.MINUTE3, IntervalLength.MINUTE4, IntervalLength.MINUTE6)) {
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);

            // Business method
            Optional<VirtualReadingType> preferredReadingType = testInstance.getPreferredReadingType(readingType);

            // Asserts
            assertThat(preferredReadingType).as("Was not expecting a preferred reading type for " + intervalLength.name()).isEmpty();
        }
    }

    @Test
    public void hourlyReadingTypeIsClosestMatchForHourly() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        Channel hourly = this.mockChannelFor(this.mockHourlyReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes, hourly), Formula.Mode.AUTO);
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        Optional<VirtualReadingType> preferredReadingType = testInstance.getPreferredReadingType(readingType);

        // Asserts
        assertThat(preferredReadingType).isPresent();
        assertThat(preferredReadingType).contains(readingType).as("Wrong match, found " + preferredReadingType.get());
    }

    @Test
    public void fifteenMinutesReadingTypeIsBestMatchForHourlyWhenOnly10And15MinAreAvailable() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes), Formula.Mode.AUTO);
        VirtualReadingType hourlyReadingType = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        VirtualReadingType min15ReadingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);

        // Business method
        Optional<VirtualReadingType> preferredReadingType = testInstance.getPreferredReadingType(hourlyReadingType);

        // Asserts
        assertThat(preferredReadingType).contains(min15ReadingType);
    }

    @Test
    public void readingTypeIsNeverSupportedWithZeroMatchingChannels() {
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Collections.emptyList(), Formula.Mode.AUTO);// As promised: no matching channels

        for (IntervalLength intervalLength : IntervalLength.values()) {
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);

            // Business method
            boolean isSupported = testInstance.isReadingTypeSupported(readingType);

            // Asserts
            assertThat(isSupported).isFalse().as("Was not expecting reading type for " + intervalLength.name() + " to be supported without any matching channel");
        }
    }

    @Test
    public void readingTypeIsNotSupportedWhenMatchingChannelsAreSmallerOrIncompatible() {
        MatchingChannelSelector testInstance =
                new MatchingChannelSelector(Arrays.asList(
                        this.mockChannelFor(this.mock2minReadingType()),
                        this.mockChannelFor(this.mock10minReadingType()),
                        this.mockChannelFor(this.mock20minReadingType())
                ), Formula.Mode.AUTO);

        for (IntervalLength intervalLength : EnumSet.of(IntervalLength.MINUTE3, IntervalLength.MINUTE5, IntervalLength.MINUTE15)) {
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);

            // Business method
            boolean isSupported = testInstance.isReadingTypeSupported(readingType);

            // Asserts
            assertThat(isSupported).isFalse().as("Was not expecting interval " + intervalLength.name() + " to be supported");
        }
    }

    @Test
    public void readingTypeIsNotSupportedWhenMatchingChannelsAreAllBigger() {
        MatchingChannelSelector testInstance =
                new MatchingChannelSelector(Arrays.asList(
                        this.mockChannelFor(this.mock10minReadingType()),
                        this.mockChannelFor(this.mock15minReadingType()),
                        this.mockChannelFor(this.mock20minReadingType()),
                        this.mockChannelFor(this.mockHourlyReadingType())
                ), Formula.Mode.AUTO);

        for (IntervalLength intervalLength : EnumSet.of(IntervalLength.MINUTE1, IntervalLength.MINUTE2, IntervalLength.MINUTE3, IntervalLength.MINUTE4, IntervalLength.MINUTE6)) {
            VirtualReadingType readingType = VirtualReadingType.from(intervalLength, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);

            // Business method
            boolean isSupported = testInstance.isReadingTypeSupported(readingType);

            // Asserts
            assertThat(isSupported).isFalse().as("Was not expecting interval " + intervalLength.name() + " to be supported");
        }
    }

    @Test
    public void hourlyReadingTypeIsSupportedWhenHourlyIsAvailable() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        Channel hourly = this.mockChannelFor(this.mockHourlyReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes, hourly), Formula.Mode.AUTO);
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);

        // Business method
        boolean isSupported = testInstance.isReadingTypeSupported(readingType);

        // Asserts
        assertThat(isSupported).isTrue();
    }

    @Test
    public void hourlyReadingTypeIsSupportedWhenOnly10And15MinAreAvailable() {
        Channel tenMinutes = this.mockChannelFor(this.mock10minReadingType());
        Channel fifteenMinutes = this.mockChannelFor(this.mock15minReadingType());
        MatchingChannelSelector testInstance = new MatchingChannelSelector(Arrays.asList(tenMinutes, fifteenMinutes), Formula.Mode.AUTO);
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.NOTAPPLICABLE);

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
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private ReadingType mock10minReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE10);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private ReadingType mock15minReadingType() {
        return this.mock15minReadingType(ReadingTypeUnit.WATTHOUR);
    }

    private ReadingType mock15minReadingType(ReadingTypeUnit unit) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getUnit()).thenReturn(unit);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private ReadingType mock20minReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE20);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private ReadingType mockHourlyReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private ReadingType mockWattRegisterReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATT);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private ReadingType mockLiterRegisterReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.LITRE);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(readingType.getCommodity()).thenReturn(Commodity.POTABLEWATER);
        return readingType;
    }

}