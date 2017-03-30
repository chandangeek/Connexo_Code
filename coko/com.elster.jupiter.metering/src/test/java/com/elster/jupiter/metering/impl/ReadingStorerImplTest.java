/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.entry;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)

public class ReadingStorerImplTest {

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
    private IReadingType readingType;
    @Mock
    private RecordSpec recordSpec;
    @Mock
    private FieldSpec fieldSpec;
    @Mock
    private ChannelsContainer channelsContainer;

    @Before
    public void setUp() {
        when(channel.getTimeSeries()).thenReturn(timeSeries);
        doReturn(recordSpec).when(timeSeries).getRecordSpec();
        doReturn(Arrays.asList(fieldSpec, fieldSpec, fieldSpec)).when(recordSpec).getFieldSpecs();
        when(channel.getBulkQuantityReadingType()).thenReturn(Optional.empty());
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainer.getMeter()).thenReturn(Optional.empty());
        when(idsService.createOverrulingStorer()).thenReturn(storer);
        when(idsService.createUpdatingStorer()).thenReturn(updatingStorer);
        doReturn(channel).when(cimChannel).getChannel();
        doReturn(readingType).when(cimChannel).getReadingType();
        doReturn(Collections.singletonList(readingType)).when(channel).getReadingTypes();
        doReturn(DerivationRule.MEASURED).when(channel).getDerivationRule(any());

        readingStorer = ReadingStorerImpl.createOverrulingStorer(idsService, eventService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddReading() {
        Instant dateTime = Instant.ofEpochMilli(215215641L);
        BaseReading reading = ReadingImpl.of("", BigDecimal.valueOf(1), dateTime);
        when(channel.toArray(reading, readingType, ProcessStatus.of())).thenReturn(new Object[]{0L, 0L, reading.getValue()});

        readingStorer.addReading(cimChannel, reading);

        assertThat(readingStorer.getScope().get(cimChannel)).isEqualTo(Range.singleton(dateTime));

        readingStorer.execute(QualityCodeSystem.MDC);

        verify(storer).add(timeSeries, dateTime, 0L, 0L, BigDecimal.valueOf(1));
        verify(storer).execute();
    }

    @Test
    public void testScope() {
        when(channel.toArray(any(), eq(readingType), any())).thenAnswer(invocation ->
                new Object[]{0L, 0L, ((BaseReading) invocation.getArguments()[0]).getValue()});

        Instant instant = Instant.ofEpochMilli(215215641L);
        for (int i = 0; i < 3; i++) {
            readingStorer.addReading(cimChannel, ReadingImpl.of("", BigDecimal.valueOf(1), instant.plusSeconds(i * 3600L)));
        }
        Map<CimChannel, Range<Instant>> scope = readingStorer.getScope();
        assertThat(scope).contains(entry(cimChannel, Range.closed(instant, instant.plusSeconds(2 * 3600L))));
    }
}
