package com.elster.jupiter.metering.impl;

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
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.util.units.Quantity;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReadingStorerImplDerivationTest {

    @Rule
    public TestRule zones = Using.timeZoneOfMcMurdo();

    private static final ZonedDateTime BASE_TIME = ZonedDateTime.of(1975, 7, 6, 15, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

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
    private IReadingType deltaReadingType, bulkReadingType;
    @Mock
    private RecordSpec recordSpec;
    @Mock
    private FieldSpec fieldSpec;

    @Before
    public void setUp() {

        when(cimChannel.getChannel()).thenReturn(channel);
        when(cimChannel.getReadingType()).thenReturn(bulkReadingType);
        when(channel.getRecordSpecDefinition()).thenReturn(RecordSpecs.BULKQUANTITYINTERVAL);
        when(channel.getTimeSeries()).thenReturn(timeSeries);
        when(channel.getReadingTypes()).thenReturn(asList(deltaReadingType, bulkReadingType));
        when(channel.getDerivationRule(deltaReadingType)).thenReturn(DerivationRule.DELTA);
        when(channel.getDerivationRule(bulkReadingType)).thenReturn(DerivationRule.MEASURED);
        when(timeSeries.getRecordSpec()).thenReturn(recordSpec);
        doReturn(asList(fieldSpec, fieldSpec, fieldSpec, fieldSpec)).when(recordSpec).getFieldSpecs();
        when(idsService.createOverrulingStorer()).thenReturn(storer);
        when(channel.toArray(any(), any(), any())).thenAnswer(invocation -> {
            BaseReading reading = (BaseReading) invocation.getArguments()[0];
            ReadingType readingType = (ReadingType) invocation.getArguments()[1];
            boolean bulk = readingType == bulkReadingType;
            return new Object[] { 0L, 0L, bulk ? null : reading.getValue(), bulk ? reading.getValue() : null };
        });
        when(channel.toArray(any())).thenAnswer(invocation -> {
            BaseReadingRecord reading = (BaseReadingRecord) invocation.getArguments()[0];
            BigDecimal delta = Optional.ofNullable(reading.getQuantity(deltaReadingType)).map(Quantity::getValue).orElse(null);
            BigDecimal bulk = Optional.ofNullable(reading.getQuantity(bulkReadingType)).map(Quantity::getValue).orElse(null);
            return new Object[] { 0L, 0L, delta, bulk };
        });

        when(channel.getPreviousDateTime(any())).thenAnswer(invocation -> invocation.getArgumentAt(0, Instant.class).minusSeconds(15 * 60));
        Answer<Quantity> toQuantity = invocation -> Optional.ofNullable(invocation.getArgumentAt(0, BigDecimal.class)).map(value -> Quantity.create(value, "Wh")).orElse(null);
        when(deltaReadingType.toQuantity(any())).thenAnswer(toQuantity);
        when(bulkReadingType.toQuantity(any())).thenAnswer(toQuantity);

        readingStorer = (ReadingStorerImpl) ReadingStorerImpl.createOverrulingStorer(idsService, eventService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddReadingNoneInDb() {
        when(channel.getReading(BASE_TIME.minusMinutes(15).toInstant())).thenReturn(Optional.empty());

        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.toInstant(), BigDecimal.valueOf(314, 2)));
        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.plusMinutes(15).toInstant(), BigDecimal.valueOf(628, 2)));
        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.plusMinutes(30).toInstant(), BigDecimal.valueOf(1000, 2)));

        readingStorer.execute();

        verify(storer).add(timeSeries, BASE_TIME.toInstant(), 0L, 0L, null, BigDecimal.valueOf(314, 2));
        verify(storer).add(timeSeries, BASE_TIME.plusMinutes(15).toInstant(), 0L, 0L, BigDecimal.valueOf(314, 2), BigDecimal.valueOf(628, 2));
        verify(storer).add(timeSeries, BASE_TIME.plusMinutes(30).toInstant(), 0L, 0L, BigDecimal.valueOf(372, 2), BigDecimal.valueOf(1000, 2));
    }

    @Test
    public void testAddReadingOneInDb() {
        TimeSeriesEntry timeSeriesEntry = mock(TimeSeriesEntry.class);
        when(timeSeriesEntry.getBigDecimal(2)).thenReturn(null);
        when(timeSeriesEntry.getBigDecimal(3)).thenReturn(BigDecimal.valueOf(100, 2));
        IntervalReadingRecordImpl readingRecord = new IntervalReadingRecordImpl(channel, timeSeriesEntry);
        when(channel.getReading(BASE_TIME.minusMinutes(15).toInstant())).thenReturn(Optional.of(readingRecord));

        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.toInstant(), BigDecimal.valueOf(314, 2)));
        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.plusMinutes(15).toInstant(), BigDecimal.valueOf(628, 2)));
        readingStorer.addReading(cimChannel, IntervalReadingImpl.of(BASE_TIME.plusMinutes(30).toInstant(), BigDecimal.valueOf(1000, 2)));

        readingStorer.execute();

        verify(storer).add(timeSeries, BASE_TIME.toInstant(), 0L, 0L, BigDecimal.valueOf(214, 2), BigDecimal.valueOf(314, 2));
        verify(storer).add(timeSeries, BASE_TIME.plusMinutes(15).toInstant(), 0L, 0L, BigDecimal.valueOf(314, 2), BigDecimal.valueOf(628, 2));
        verify(storer).add(timeSeries, BASE_TIME.plusMinutes(30).toInstant(), 0L, 0L, BigDecimal.valueOf(372, 2), BigDecimal.valueOf(1000, 2));
    }


}
