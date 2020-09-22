/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.metering.UsagePointReadingTypeConfiguration;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadingStorerImplDerivationTest {

    @Rule
    public TestRule zones = Using.timeZoneOfMcMurdo();

    private static final ZonedDateTime BASE_TIME = ZonedDateTime.of(1975, 7, 6, 15, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

    private ReadingStorerImpl readingStorer;

    @Mock
    private TimeSeriesDataStorer storer, updatingStorer;
    @Mock
    private ChannelContract channel;
    @Mock
    private CimChannel cimChannel;
    @Mock
    private TimeSeries timeSeries;
    @Mock
    private IdsService idsService;
    @Mock
    private EventService eventService;
    @Mock
    private IReadingType secondaryDeltaReadingType, secondaryBulkReadingType, primaryDeltaReadingType, primaryBulkReadingType, pulseReadingType, calculatedOnPulseReadingType;
    @Mock
    private RecordSpec recordSpec;
    @Mock
    private FieldSpec fieldSpec;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private UsagePointConfiguration usagePointConfiguration;
    @Mock
    private UsagePointReadingTypeConfiguration readingTypeConfiguration;
    @Mock
    private MultiplierType multiplierType;

    @Before
    public void setUp() {
        when(cimChannel.getChannel()).thenReturn(channel);
        when(cimChannel.getReadingType()).thenReturn(secondaryBulkReadingType);
        when(channel.getRecordSpecDefinition()).thenReturn(RecordSpecs.BULKQUANTITYINTERVAL);
        when(channel.getTimeSeries()).thenReturn(timeSeries);
        when(channel.getReadingTypes()).thenReturn(asList(secondaryDeltaReadingType, secondaryBulkReadingType));
        when(channel.getDerivationRule(secondaryDeltaReadingType)).thenReturn(DerivationRule.DELTA);
        when(channel.getDerivationRule(secondaryBulkReadingType)).thenReturn(DerivationRule.MEASURED);
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainer.getMeter()).thenReturn(Optional.empty());
        when(channelsContainer.getInterval()).thenReturn(Interval.forever());
        when(timeSeries.getRecordSpec()).thenReturn(recordSpec);
        doReturn(asList(fieldSpec, fieldSpec, fieldSpec, fieldSpec)).when(recordSpec).getFieldSpecs();
        when(idsService.createOverrulingStorer()).thenReturn(storer);
        when(idsService.createUpdatingStorer()).thenReturn(updatingStorer);
        when(channel.toArray(any(), any(), any())).thenAnswer(invocation -> {
            BaseReading reading = (BaseReading) invocation.getArguments()[0];
            ReadingType readingType = (ReadingType) invocation.getArguments()[1];
            boolean bulk = readingType == secondaryBulkReadingType;
            return new Object[]{0L, 0L, bulk ? null : reading.getValue(), bulk ? reading.getValue() : null};
        });
        when(channel.toArray(any())).thenAnswer(invocation -> {
            BaseReadingRecord reading = (BaseReadingRecord) invocation.getArguments()[0];
            BigDecimal delta = Optional.ofNullable(reading.getQuantity(secondaryDeltaReadingType)).map(Quantity::getValue).orElse(null);
            BigDecimal bulk = Optional.ofNullable(reading.getQuantity(secondaryBulkReadingType)).map(Quantity::getValue).orElse(null);
            return new Object[]{0L, 0L, delta, bulk};
        });
        when(channel.getReading(any())).thenReturn(Optional.empty());

        when(channel.getPreviousDateTime(any())).thenAnswer(invocation -> invocation.getArgumentAt(0, Instant.class).minusSeconds(15 * 60));
        when(channel.getNextDateTime(any())).thenAnswer(invocation -> invocation.getArgumentAt(0, Instant.class).plusSeconds(15 * 60));
        Answer<Quantity> toQuantity = invocation -> Optional.ofNullable(invocation.getArgumentAt(0, BigDecimal.class)).map(value -> Quantity.create(value, "Wh")).orElse(null);
        when(secondaryDeltaReadingType.toQuantity(any())).thenAnswer(toQuantity);
        when(secondaryBulkReadingType.toQuantity(any())).thenAnswer(toQuantity);
        when(primaryDeltaReadingType.getBulkReadingType()).thenReturn(Optional.of(primaryBulkReadingType));
        when(secondaryBulkReadingType.isRegular()).thenReturn(true);
        when(primaryBulkReadingType.isRegular()).thenReturn(true);
        when(primaryDeltaReadingType.isRegular()).thenReturn(true);
        when(channel.isRegular()).thenReturn(true);

        readingStorer = ReadingStorerImpl.createOverrulingStorer(idsService, eventService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddReadingNoneInDb() {
        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.toInstant(), BigDecimal.valueOf(314, 2)));
        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.plusMinutes(15).toInstant(), BigDecimal.valueOf(628, 2)));
        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.plusMinutes(30).toInstant(), BigDecimal.valueOf(1000, 2)));

        readingStorer.execute(QualityCodeSystem.MDC);

        verify(storer).add(timeSeries, BASE_TIME.toInstant(), 0L, 0L, null, BigDecimal.valueOf(314, 2));
        verify(storer).add(timeSeries, BASE_TIME.plusMinutes(15).toInstant(), 0L, 0L, BigDecimal.valueOf(314, 2), BigDecimal.valueOf(628, 2));
        verify(storer).add(timeSeries, BASE_TIME.plusMinutes(30).toInstant(), 0L, 0L, BigDecimal.valueOf(372, 2), BigDecimal.valueOf(1000, 2));
    }

    @Test
    public void testAddReadingOneInDb() {
        TimeSeriesEntry timeSeriesEntry = mock(TimeSeriesEntry.class);
        when(timeSeriesEntry.getTimeSeries()).thenReturn(timeSeries);
        when(timeSeriesEntry.getBigDecimal(2)).thenReturn(null);
        when(timeSeriesEntry.getBigDecimal(3)).thenReturn(BigDecimal.valueOf(100, 2));
        when(timeSeriesEntry.getTimeStamp()).thenReturn(BASE_TIME.minusMinutes(15).toInstant());
        when(idsService.getEntries(Collections.singletonList(Pair.of(timeSeries, BASE_TIME.minusMinutes(15).toInstant())))).thenReturn(Collections.singletonList(timeSeriesEntry));

        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.toInstant(), BigDecimal.valueOf(314, 2)));
        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.plusMinutes(15).toInstant(), BigDecimal.valueOf(628, 2)));
        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.plusMinutes(30).toInstant(), BigDecimal.valueOf(1000, 2)));

        readingStorer.execute(QualityCodeSystem.MDC);

        verify(storer).add(timeSeries, BASE_TIME.toInstant(), 0L, 0L, BigDecimal.valueOf(214, 2), BigDecimal.valueOf(314, 2));
        verify(storer).add(timeSeries, BASE_TIME.plusMinutes(15).toInstant(), 0L, 0L, BigDecimal.valueOf(314, 2), BigDecimal.valueOf(628, 2));
        verify(storer).add(timeSeries, BASE_TIME.plusMinutes(30).toInstant(), 0L, 0L, BigDecimal.valueOf(372, 2), BigDecimal.valueOf(1000, 2));
    }

    @Test
    public void testUsingMultiplier() {
        when(channel.getReadingTypes()).thenReturn(asList(primaryDeltaReadingType, secondaryBulkReadingType));
        when(channel.getDerivationRule(primaryDeltaReadingType)).thenReturn(DerivationRule.MULTIPLIED_DELTA);
        when(channel.getDerivationRule(secondaryBulkReadingType)).thenReturn(DerivationRule.MEASURED);
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainer.getMeter()).thenReturn(Optional.empty());
        when(channelsContainer.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getConfiguration(any())).thenReturn(Optional.of(usagePointConfiguration));
        when(usagePointConfiguration.getReadingTypeConfigs()).thenReturn(Collections.singletonList(readingTypeConfiguration));
        when(readingTypeConfiguration.getMeasured()).thenReturn(secondaryBulkReadingType);
        when(readingTypeConfiguration.getCalculated()).thenReturn(Optional.of(primaryBulkReadingType));
        when(readingTypeConfiguration.getMultiplierType()).thenReturn(multiplierType);
        when(channelsContainer.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.valueOf(5, 0)));
//        when(channelsContainer.getMultiplierUsages(any())).thenReturn(Collections.singletonList(readingTypeConfiguration));

        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.toInstant(), BigDecimal.valueOf(314, 2)));
        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.plusMinutes(15).toInstant(), BigDecimal.valueOf(628, 2)));
        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.plusMinutes(30).toInstant(), BigDecimal.valueOf(1000, 2)));

        readingStorer.execute(QualityCodeSystem.MDC);

        verify(storer).add(timeSeries, BASE_TIME.toInstant(), 0L, 0L, null, BigDecimal.valueOf(314, 2));
        verify(storer).add(timeSeries, BASE_TIME.plusMinutes(15).toInstant(), 0L, 0L, BigDecimal.valueOf(1570, 2), BigDecimal.valueOf(628, 2));
        verify(storer).add(timeSeries, BASE_TIME.plusMinutes(30).toInstant(), 0L, 0L, BigDecimal.valueOf(1860, 2), BigDecimal.valueOf(1000, 2));
    }

    @Test
    public void testUsingMultiplierForPulseStyleMultiplication() {
        when(channel.getReadingTypes()).thenReturn(asList(calculatedOnPulseReadingType, pulseReadingType));
        when(channel.getDerivationRule(calculatedOnPulseReadingType)).thenReturn(DerivationRule.MULTIPLIED);
        when(channel.getDerivationRule(pulseReadingType)).thenReturn(DerivationRule.MEASURED);
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainer.getMeter()).thenReturn(Optional.empty());
        when(channelsContainer.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getConfiguration(any())).thenReturn(Optional.of(usagePointConfiguration));
        when(usagePointConfiguration.getReadingTypeConfigs()).thenReturn(Collections.singletonList(readingTypeConfiguration));
        when(readingTypeConfiguration.getMeasured()).thenReturn(pulseReadingType);
        when(readingTypeConfiguration.getCalculated()).thenReturn(Optional.of(calculatedOnPulseReadingType));
        when(readingTypeConfiguration.getMultiplierType()).thenReturn(multiplierType);
        when(channelsContainer.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.valueOf(5, 0)));
//        when(channelsContainer.getMultiplierUsages(any())).thenReturn(Collections.singletonList(readingTypeConfiguration));

        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.toInstant(), BigDecimal.valueOf(314, 0)));
        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.plusMinutes(15).toInstant(), BigDecimal.valueOf(628, 0)));
        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.plusMinutes(30).toInstant(), BigDecimal.valueOf(1000, 0)));

        readingStorer.execute(QualityCodeSystem.MDC);

        verify(storer).add(timeSeries, BASE_TIME.toInstant(), 0L, 0L, BigDecimal.valueOf(1570, 0), BigDecimal.valueOf(314, 0));
        verify(storer).add(timeSeries, BASE_TIME.plusMinutes(15).toInstant(), 0L, 0L, BigDecimal.valueOf(3140, 0), BigDecimal.valueOf(628, 0));
        verify(storer).add(timeSeries, BASE_TIME.plusMinutes(30).toInstant(), 0L, 0L, BigDecimal.valueOf(5000, 0), BigDecimal.valueOf(1000, 0));
    }

    @Test
    public void testValuesOnAEdgeOfChannelContainer() {

        ChannelsContainer channelsContainerBefore = mock(ChannelsContainer.class);
        ChannelsContainer channelsContainerAfter = mock(ChannelsContainer.class);

        when(channelsContainerBefore.getInterval()).thenReturn(Interval.of(Range.range(Instant.EPOCH, BoundType.OPEN, BASE_TIME.toInstant(), BoundType.CLOSED)));
        when(channelsContainer.getInterval()).thenReturn(Interval.of(Range.range(BASE_TIME.toInstant(), BoundType.OPEN, BASE_TIME.plusMinutes(15).toInstant(), BoundType.CLOSED)));
        when(channelsContainerAfter.getInterval()).thenReturn(Interval.of(Range.range(BASE_TIME.plusMinutes(15).toInstant(), BoundType.OPEN, BASE_TIME.plusMinutes(120).toInstant(), BoundType.CLOSED)));

        ChannelContract channelBefore = mock(ChannelContract.class);
        ChannelContract channelAfter = mock(ChannelContract.class);

        TimeSeries timeSeriesBefore = mock(TimeSeries.class);
        TimeSeries timeSeriesAfter = mock(TimeSeries.class);

        when(timeSeriesBefore.getRecordSpec()).thenReturn(recordSpec);
        when(timeSeriesAfter.getRecordSpec()).thenReturn(recordSpec);

        when(channelBefore.getTimeSeries()).thenReturn(timeSeriesBefore);
        when(channelAfter.getTimeSeries()).thenReturn(timeSeriesAfter);

        CimChannel cimChannelBefore = mock(CimChannel.class);
        CimChannel cimChannelAfter = mock(CimChannel.class);

        when(cimChannelBefore.getChannel()).thenReturn(channelBefore);
        when(cimChannelAfter.getChannel()).thenReturn(channelAfter);

        when(cimChannelBefore.getReadingType()).thenReturn(secondaryBulkReadingType);
        when(cimChannelAfter.getReadingType()).thenReturn(secondaryBulkReadingType);

        ChannelContract[] channels = {channelBefore, channelAfter};
        Meter meter = mock(Meter.class);

        when(meter.getConfiguration(any())).thenReturn(Optional.empty());

        for (ChannelContract channelContract : channels) {
            when(channelContract.getRecordSpecDefinition()).thenReturn(RecordSpecs.BULKQUANTITYINTERVAL);
            when(channelContract.getReadingTypes()).thenReturn(asList(secondaryDeltaReadingType, secondaryBulkReadingType));
            when(channelContract.getDerivationRule(secondaryDeltaReadingType)).thenReturn(DerivationRule.DELTA);
            when(channelContract.getDerivationRule(secondaryBulkReadingType)).thenReturn(DerivationRule.MEASURED);
            when(channelContract.isRegular()).thenReturn(true);

            when(channelContract.toArray(any(), any(), any())).thenAnswer(invocation -> {
                BaseReading reading = (BaseReading) invocation.getArguments()[0];
                ReadingType readingType = (ReadingType) invocation.getArguments()[1];
                boolean bulk = readingType == secondaryBulkReadingType;
                return new Object[]{0L, 0L, bulk ? null : reading.getValue(), bulk ? reading.getValue() : null};
            });
            when(channelContract.toArray(any())).thenAnswer(invocation -> {
                BaseReadingRecord reading = (BaseReadingRecord) invocation.getArguments()[0];
                BigDecimal delta = Optional.ofNullable(reading.getQuantity(secondaryDeltaReadingType)).map(Quantity::getValue).orElse(null);
                BigDecimal bulk = Optional.ofNullable(reading.getQuantity(secondaryBulkReadingType)).map(Quantity::getValue).orElse(null);
                return new Object[]{0L, 0L, delta, bulk};
            });
            when(channelContract.getReading(any())).thenReturn(Optional.empty());
            when(channelContract.getPreviousDateTime(any())).thenAnswer(invocation -> invocation.getArgumentAt(0, Instant.class).minusSeconds(15 * 60));
            when(channelContract.getNextDateTime(any())).thenAnswer(invocation -> invocation.getArgumentAt(0, Instant.class).plusSeconds(15 * 60));
        }

        when(channelBefore.getChannelsContainer()).thenReturn(channelsContainerBefore);
        when(channelAfter.getChannelsContainer()).thenReturn(channelsContainerAfter);

        when(channelsContainerBefore.getMeter()).thenReturn(Optional.of(meter));
        when(channelsContainer.getMeter()).thenReturn(Optional.of(meter));
        when(channelsContainerAfter.getMeter()).thenReturn(Optional.of(meter));

        IMeterActivation beforeActivation  = mock(IMeterActivation.class);
        IMeterActivation activation  = mock(IMeterActivation.class);
        IMeterActivation afterActivation  = mock(IMeterActivation.class);

        when(beforeActivation.getChannelsContainer()).thenReturn(channelsContainerBefore);
        when(activation.getChannelsContainer()).thenReturn(channelsContainer);
        when(afterActivation.getChannelsContainer()).thenReturn(channelsContainerAfter);

        when(meter.getMeterActivation(eq(BASE_TIME.minusMinutes(15).toInstant()))).thenAnswer(invocationOnMock -> Optional.of(beforeActivation));
        when(meter.getMeterActivation(BASE_TIME.toInstant())).thenAnswer(invocationOnMock -> Optional.of(activation));
        when(meter.getMeterActivation(BASE_TIME.plusMinutes(15).toInstant())).thenAnswer(invocationOnMock -> Optional.of(afterActivation));

        readingStorer.addReading(cimChannelBefore, IntervalReadingImpl.of(BASE_TIME.minusMinutes(15).toInstant(), BigDecimal.valueOf(100, 1)));
        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.toInstant(), BigDecimal.valueOf(200, 1)));
        readingStorer.addReading(cimChannelAfter, IntervalReadingImpl.of(BASE_TIME.plusMinutes(15).toInstant(), BigDecimal.valueOf(300, 1)));

        readingStorer.execute(QualityCodeSystem.MDC);

        verify(storer).add(timeSeriesBefore, BASE_TIME.minusMinutes(15).toInstant(), 0L, 0L, null, BigDecimal.valueOf(100, 1));
        verify(storer).add(timeSeries, BASE_TIME.toInstant(), 0L, 0L, null, BigDecimal.valueOf(200, 1));
        verify(storer).add(timeSeriesAfter, BASE_TIME.plusMinutes(15).toInstant(), 0L, 0L, null, BigDecimal.valueOf(300, 1));
    }

}
