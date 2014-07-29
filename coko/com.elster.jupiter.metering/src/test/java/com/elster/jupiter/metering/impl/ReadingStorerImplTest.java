package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.util.time.Interval;

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
    private Channel channel;
    @Mock
    private TimeSeries timeSeries;
    @Mock
    private IdsService idsService;
    @Mock
    private EventService eventService;

    @Before
    public void setUp() {
        when(channel.getTimeSeries()).thenReturn(timeSeries);
        when(idsService.createStorer(true)).thenReturn(storer);

        readingStorer = new ReadingStorerImpl(idsService, eventService, true);

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddIntervalReading() {
        ProfileStatus profileStatus = ProfileStatus.of(ProfileStatus.Flag.POWERDOWN,ProfileStatus.Flag.POWERUP);
        Date dateTime = new Date(215215641L);
        readingStorer.addIntervalReading(channel, dateTime, profileStatus, BigDecimal.valueOf(1), BigDecimal.valueOf(2));

        verify(storer).add(timeSeries, dateTime, new Object[] { 0L , profileStatus.getBits(), BigDecimal.valueOf(1), BigDecimal.valueOf(2) });
    }

    @Test
    public void testAddReading() {
        Date dateTime = new Date(215215641L);
        readingStorer.addReading(channel, dateTime, BigDecimal.valueOf(1));

        verify(storer).add(timeSeries, dateTime, 0L , 0L, BigDecimal.valueOf(1),null);
    }

    @Test
    public void testAddReadingWithFrom() {
        Date dateTime = new Date(215215641L);
        Date from = new Date(215215151L);
        readingStorer.addReading(channel, dateTime, BigDecimal.valueOf(1), from);

        verify(storer).add(timeSeries, dateTime, 0L , 0L, BigDecimal.valueOf(1), null, from);
    }

    @Test
    public void testAddReadingWithFromAndWhen() {
        Date dateTime = new Date(215215641L);
        Date from = new Date(215215151L);
        Date when = new Date(215215199L);
        readingStorer.addReading(channel, dateTime, BigDecimal.valueOf(1), from, when);

        verify(storer).add(timeSeries, dateTime, 0L , 0L, BigDecimal.valueOf(1), null, from, when);
    }

    @Test
    public void testScope() {
        DateTime dateTime = new DateTime(215215641L);
        Date from = new Date(215215151L);
        Date when = new Date(215215199L);
        for (int i = 0; i < 3; i++) {
            readingStorer.addReading(channel, dateTime.plusHours(i).toDate(), BigDecimal.valueOf(1), from, when);
        }
        Map<Channel,Interval> scope = readingStorer.getScope();
        assertThat(scope).contains(entry(channel, new Interval(dateTime.toDate(), dateTime.plusHours(2).toDate())));
    }


}
