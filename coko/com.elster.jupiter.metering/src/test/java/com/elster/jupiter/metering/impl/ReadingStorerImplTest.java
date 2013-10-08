package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.Channel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Date;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadingStorerImplTest {

    private ReadingStorerImpl readingStorer;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;
    @Mock
    private TimeSeriesDataStorer storer;
    @Mock
    private Channel channel;
    @Mock
    private TimeSeries timeSeries;

    @Before
    public void setUp() {
        when(serviceLocator.getIdsService().createStorer(true)).thenReturn(storer);
        Bus.setServiceLocator(serviceLocator);
        when(channel.getTimeSeries()).thenReturn(timeSeries);

        readingStorer = new ReadingStorerImpl(true);

    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testAddIntervalReading() {
        long profileStatus = 465;
        Date dateTime = new Date(215215641L);
        readingStorer.addIntervalReading(channel, dateTime, profileStatus, BigDecimal.valueOf(1), BigDecimal.valueOf(2));

        verify(storer).add(timeSeries, dateTime, new Object[] { 0L , profileStatus, BigDecimal.valueOf(1), BigDecimal.valueOf(2) });
    }

    @Test
    public void testAddReading() {
        Date dateTime = new Date(215215641L);
        readingStorer.addReading(channel, dateTime, BigDecimal.valueOf(1));

        verify(storer).add(timeSeries, dateTime, 0L , 0L, BigDecimal.valueOf(1));
    }

    @Test
    public void testAddReadingWithFrom() {
        Date dateTime = new Date(215215641L);
        Date from = new Date(215215151L);
        readingStorer.addReading(channel, dateTime, BigDecimal.valueOf(1), from);

        verify(storer).add(timeSeries, dateTime, 0L , 0L, BigDecimal.valueOf(1), from);
    }

    @Test
    public void testAddReadingWithFromAndWhen() {
        Date dateTime = new Date(215215641L);
        Date from = new Date(215215151L);
        Date when = new Date(215215199L);
        readingStorer.addReading(channel, dateTime, BigDecimal.valueOf(1), from, when);

        verify(storer).add(timeSeries, dateTime, 0L , 0L, BigDecimal.valueOf(1), from, when);
    }

}
