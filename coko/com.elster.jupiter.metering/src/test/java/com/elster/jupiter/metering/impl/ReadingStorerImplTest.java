package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.google.common.collect.Range;
import java.time.Instant;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)

public class ReadingStorerImplTest {

    private ReadingStorerImpl readingStorer;

    @Mock
    private TimeSeriesDataStorer storer;
    @Mock
    private ChannelContract channel;
    @Mock
    private TimeSeries timeSeries;
    @Mock
    private IdsService idsService;
    @Mock
    private EventService eventService;

    @Before
    public void setUp() {
        when(channel.getTimeSeries()).thenReturn(timeSeries);
        when(channel.getBulkQuantityReadingType()).thenReturn(Optional.empty());
       when(idsService.createStorer(true)).thenReturn(storer);

        readingStorer = new ReadingStorerImpl(idsService, eventService, true);

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddReading() {
        Instant dateTime = Instant.ofEpochMilli(215215641L);
        BaseReading reading = ReadingImpl.of("", BigDecimal.valueOf(1), dateTime);
        when(channel.toArray(reading, ProcessStatus.of())).thenReturn(new Object[] { 0L, 0L, reading.getValue() } );
        readingStorer.addReading(channel, reading);
        verify(storer).add(timeSeries, dateTime, 0L , 0L, BigDecimal.valueOf(1));
    }


    @Test
    public void testScope() {
        Instant instant = Instant.ofEpochMilli(215215641L);
        for (int i = 0; i < 3; i++) {
            readingStorer.addReading(channel, ReadingImpl.of("", BigDecimal.valueOf(1), instant.plusSeconds(i * 3600L)));
        }
        Map<Channel, Range<Instant>> scope = readingStorer.getScope();
        assertThat(scope).contains(entry(channel, Range.closed(instant, instant.plusSeconds(2*3600L))));
    }


}
