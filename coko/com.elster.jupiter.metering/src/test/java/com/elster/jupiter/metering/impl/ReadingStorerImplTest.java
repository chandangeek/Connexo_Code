package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.google.common.collect.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.entry;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)

public class ReadingStorerImplTest {

    private ReadingStorerImpl readingStorer;

    @Mock
    private TimeSeriesDataStorer storer;
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
    private ReadingType readingType1, readingType2;
    @Mock
    private RecordSpec recordSpec;
    @Mock
    private FieldSpec fieldSpec;

    @Before
    public void setUp() {
        when(channel.getTimeSeries()).thenReturn(timeSeries);
        doReturn(recordSpec).when(timeSeries).getRecordSpec();
        doReturn(Arrays.asList(fieldSpec, fieldSpec, fieldSpec)).when(recordSpec).getFieldSpecs();
        when(channel.getBulkQuantityReadingType()).thenReturn(Optional.empty());
        when(idsService.createOverrulingStorer()).thenReturn(storer);
        doReturn(channel).when(cimChannel).getChannel();
        doReturn(readingType2).when(cimChannel).getReadingType();
        doReturn(Arrays.asList(readingType1, readingType2)).when(channel).getReadingTypes();

        readingStorer = (ReadingStorerImpl) ReadingStorerImpl.createOverrulingStorer(idsService, eventService);

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddReading() {
        Instant dateTime = Instant.ofEpochMilli(215215641L);
        BaseReading reading = ReadingImpl.of("", BigDecimal.valueOf(1), dateTime);
        when(channel.toArray(reading, ProcessStatus.of())).thenReturn(new Object[]{0L, 0L, reading.getValue()});

        readingStorer.addReading(cimChannel, reading);

        assertThat(readingStorer.getScope().get(cimChannel)).isEqualTo(Range.singleton(dateTime));

        readingStorer.execute();

        verify(storer).add(timeSeries, dateTime, 0L, null, BigDecimal.valueOf(1));
        verify(storer).execute();
    }


    @Test
    public void testScope() {
        Instant instant = Instant.ofEpochMilli(215215641L);
        for (int i = 0; i < 3; i++) {
            readingStorer.addReading(cimChannel, ReadingImpl.of("", BigDecimal.valueOf(1), instant.plusSeconds(i * 3600L)));
        }
        Map<CimChannel, Range<Instant>> scope = readingStorer.getScope();
        assertThat(scope).contains(entry(cimChannel, Range.closed(instant, instant.plusSeconds(2 * 3600L))));
    }


}
