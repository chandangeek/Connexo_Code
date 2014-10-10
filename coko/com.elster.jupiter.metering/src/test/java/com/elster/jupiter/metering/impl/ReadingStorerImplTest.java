package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.util.time.Interval;
import java.util.Optional;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Date;
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
        Date dateTime = new Date(215215641L);
        BaseReading reading = new ReadingImpl("", BigDecimal.valueOf(1), dateTime);
        when(channel.toArray(reading, ProcessStatus.of())).thenReturn(new Object[] { 0L, 0L, reading.getValue() } );
        readingStorer.addReading(channel, reading);
        verify(storer).add(timeSeries, dateTime.toInstant(), 0L , 0L, BigDecimal.valueOf(1));
    }


    @Test
    public void testScope() {
        DateTime dateTime = new DateTime(215215641L);
        for (int i = 0; i < 3; i++) {
            readingStorer.addReading(channel, new ReadingImpl("", BigDecimal.valueOf(1), dateTime.plusHours(i).toDate()));
        }
        Map<Channel,Interval> scope = readingStorer.getScope();
        assertThat(scope).contains(entry(channel, new Interval(dateTime.toDate(), dateTime.plusHours(2).toDate())));
    }


}
