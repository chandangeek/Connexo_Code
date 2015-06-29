package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChannelImplTest extends EqualsContractTest {

    private static final String MRID1 = "11.2.2.4.0.8.12.8.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID2 = "11.2.2.1.0.8.12.8.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID3 = "13.2.3.4.0.8.12.10.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID4 = "11.2.3.4.0.8.12.10.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID1_IRR = "0.2.0.4.0.8.12.8.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID2_IRR = "0.2.0.1.0.8.12.9.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID4_IRR = "0.2.0.4.0.8.12.10.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID5_BIL = "8.26.0.4.20.1.12.0.0.0.0.0.0.0.0.3.73.0";
    private static final long METER_ACTIVATION_ID = 164;
    private static final long ID = 15L;
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Asia/Calcutta");
    private static final Instant TO = LocalDate.of(2013, 9, 20).atStartOfDay(TIME_ZONE.toZoneId()).toInstant();
    private static final Instant FROM = LocalDate.of(2013, 9, 19).atStartOfDay(TIME_ZONE.toZoneId()).toInstant();
    private static final Range<Instant> INTERVAL = Range.closed(FROM, TO);
    private static final long TIMESERIES_ID = 21316L;
    private static final BigDecimal VALUE = BigDecimal.valueOf(3156516, 2);

    private ChannelImpl channel;
    private ChannelImpl channelInstanceA;

    private ReadingTypeImpl readingType1, readingType2, readingType3, readingType4;

    @Mock
    private MeterActivation meterActivation;
    @Mock
    private IdsService idsService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private TimeSeries timeSeries, regularTimeSeries;
    @Mock
    private Vault vault;
    @Mock
    private RecordSpec recordSpec;
    @Mock
    private TimeSeriesEntry timeSeriesEntry;
    @Mock
    private DataModel dataModel;
    @Mock
    private Clock clock;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private EventService eventService;

    @Before
    public void setUp() {
        when(dataModel.getInstance(ChannelImpl.class)).thenReturn(createChannel());
        when(dataModel.getInstance(ReadingTypeImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ReadingTypeImpl(dataModel, thesaurus);
            }
        });
        when(meterActivation.getId()).thenReturn(METER_ACTIVATION_ID);        
        when(meterActivation.getZoneId()).thenReturn(TIME_ZONE.toZoneId());       
        when(idsService.getVault(MeteringService.COMPONENTNAME, 1)).thenReturn(Optional.of(vault));
        when(idsService.getVault(MeteringService.COMPONENTNAME, 2)).thenReturn(Optional.of(vault));
        when(idsService.getVault(MeteringService.COMPONENTNAME, 3)).thenReturn(Optional.of(vault));
        when(idsService.getVault(MeteringService.COMPONENTNAME, 4)).thenReturn(Optional.of(vault));
        when(idsService.getRecordSpec(MeteringService.COMPONENTNAME, 1)).thenReturn(Optional.of(recordSpec));
        when(idsService.getRecordSpec(MeteringService.COMPONENTNAME, 2)).thenReturn(Optional.of(recordSpec));
        when(idsService.getRecordSpec(MeteringService.COMPONENTNAME, 4)).thenReturn(Optional.of(recordSpec));
        when(idsService.getRecordSpec(MeteringService.COMPONENTNAME, 5)).thenReturn(Optional.of(recordSpec));
        when(vault.createIrregularTimeSeries(recordSpec, TIME_ZONE)).thenReturn(timeSeries);
        when(vault.createRegularTimeSeries(recordSpec, TIME_ZONE, Period.ofDays(1), 0)).thenReturn(regularTimeSeries);
        when(timeSeries.getId()).thenReturn(TIMESERIES_ID);
        when(regularTimeSeries.getId()).thenReturn(TIMESERIES_ID);

        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID1, "1");
        readingType2 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID2, "2");
        readingType3 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID3, "3");
        readingType4 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID4, "4");

        channel = createChannel().init(meterActivation, ImmutableList.of(readingType1, readingType2));
    }

    @After
    public void tearDown() {
    }

    private ChannelImpl createChannel() {
    	return new ChannelImpl(dataModel, idsService, meteringService, clock, eventService);
    }
    
    @Override
    protected Object getInstanceA() {
        if (channelInstanceA == null) {
            channelInstanceA = createChannel();
            field("id").ofType(Long.TYPE).in(channelInstanceA).set(ID);
        }
        return channelInstanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        ChannelImpl channel1 = createChannel();
        field("id").ofType(Long.TYPE).in(channel1).set(ID);

        return channel1;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        ChannelImpl channel1 = createChannel();
        field("id").ofType(Long.TYPE).in(channel1).set(ID + 1);
        return ImmutableList.of(channel1);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    @Test
    public void testGetMeterActivation() {

        assertThat(channel.getMeterActivation()).isEqualTo(meterActivation);
    }

    @Test
    public void testGetId() {
        simulateSavedChannel();

        assertThat(channel.getId()).isEqualTo(ID);
    }

    @Test
    public void testInitIrregularTimeSeries() {
    	readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID1_IRR, "1");
        readingType2 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID2_IRR, "2");

        channel = createChannel().init(meterActivation,ImmutableList.of(readingType1, readingType2));

        assertThat(channel.getMainReadingType()).isEqualTo(readingType1);
        assertThat(channel.getBulkQuantityReadingType().isPresent()).isFalse();
        assertThat(channel.getReadingTypes()).hasSize(2)
                .contains(readingType1)
                .contains(readingType2);
        assertThat(channel.getIntervalLength().isPresent()).isFalse();
        assertThat(channel.getTimeSeries()).isEqualTo(timeSeries);
    }

    @Test
    public void testInitWithIntervalLength() {
      assertThat(channel.getMainReadingType()).isEqualTo(readingType1);
        assertThat(channel.getBulkQuantityReadingType().get()).isEqualTo(readingType2);
        assertThat(channel.getReadingTypes()).hasSize(2)
                .contains(readingType1)
                .contains(readingType2);
        assertThat(channel.getIntervalLength()).isEqualTo(Optional.of(Period.ofDays(1)));
        assertThat(channel.getTimeSeries()).isEqualTo(regularTimeSeries);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitWithInconsistentIntervalLength() {
        createChannel().init(meterActivation, ImmutableList.of(readingType1, readingType3));
    }

    @Test
    public void testInitIrregularTimeSeriesWithAdditionalReadingTypes() {
        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID1_IRR, "1");
        readingType2 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID2_IRR, "2");
        readingType4 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID4_IRR, "4");

        channel = createChannel().init(meterActivation,ImmutableList.of(readingType1, readingType2, readingType4));

        assertThat(channel.getMainReadingType()).isEqualTo(readingType1);
        assertThat(channel.getBulkQuantityReadingType().isPresent()).isFalse();
        assertThat(channel.getReadingTypes()).hasSize(3)
                .contains(readingType1)
                .contains(readingType2)
                .contains(readingType4);
        assertThat(channel.getIntervalLength().isPresent()).isFalse();
        assertThat(channel.getTimeSeries()).isEqualTo(timeSeries);
    }

    @Test
    public void testGetIntervalReadings() {
        when(regularTimeSeries.getEntries(INTERVAL)).thenReturn(Arrays.asList(timeSeriesEntry));
        when(timeSeriesEntry.getBigDecimal(2)).thenReturn(VALUE);

        List<IntervalReadingRecord> intervalReadings = channel.getIntervalReadings(INTERVAL);

        assertThat(intervalReadings).hasSize(1);

        IntervalReadingRecord intervalReading = intervalReadings.get(0);
        List<? extends ReadingType> readingTypes = intervalReading.getReadingTypes();
        assertThat(intervalReading.getReadingTypes()).hasSize(2);
        assertThat(readingTypes.contains(readingType1)).isTrue();
        assertThat(readingTypes.contains(readingType2)).isTrue();
        assertThat(intervalReading.getValue()).isEqualTo(VALUE);
    }

    @Test
    public void testGetIntervalReadingsForReadingType() {
        //channel.init(Arrays.<ReadingType>asList(readingType1, readingType2));
        when(regularTimeSeries.getEntries(INTERVAL)).thenReturn(Arrays.asList(timeSeriesEntry));
        when(timeSeriesEntry.getBigDecimal(anyInt())).thenReturn(VALUE);

        List<IntervalReadingRecord> intervalReadings = channel.getIntervalReadings(readingType1, INTERVAL);

        assertThat(intervalReadings).hasSize(1);

        IntervalReadingRecord intervalReading = intervalReadings.get(0);
        List<? extends ReadingType> readingTypes = intervalReading.getReadingTypes(); 
        assertThat(readingTypes).hasSize(1);
        assertThat(readingTypes.contains(readingType1)).isTrue();
        assertThat(intervalReading.getValue()).isEqualTo(VALUE);
    }

    @Test
    public void testGetRegisterReadings() {

        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID1_IRR, "1");
        readingType2 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID2_IRR, "2");

        channel = createChannel().init(meterActivation,ImmutableList.of(readingType1,readingType2));

        when(timeSeries.getEntries(INTERVAL)).thenReturn(Arrays.asList(timeSeriesEntry));
        when(timeSeriesEntry.getBigDecimal(anyInt())).thenReturn(VALUE);

        List<ReadingRecord> registerReadings = channel.getRegisterReadings(INTERVAL);

        assertThat(registerReadings).hasSize(1);

        ReadingRecord registerReading = registerReadings.get(0);
        List<? extends ReadingType> readingTypes = registerReading.getReadingTypes(); 
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes.contains(readingType1)).isTrue();
        assertThat(readingTypes.contains(readingType2)).isTrue();
        assertThat(registerReading.getValue()).isEqualTo(VALUE);
    }

    @Test(expected = LocalizedFieldValidationException.class)
    public void testValidateBillingReadingWithTimestampLessThanStart() {
        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID5_BIL, "Billing");
        channel = createChannel().init(meterActivation, ImmutableList.of(readingType1));
        ReadingImpl reading = ReadingImpl.of(readingType1.getMRID(), BigDecimal.valueOf(50), LocalDateTime.of(2014, 6, 2, 0, 0).toInstant(ZoneOffset.UTC));
        reading.setTimePeriod(
                LocalDateTime.of(2014, 6, 3, 0, 0).toInstant(ZoneOffset.UTC),
                LocalDateTime.of(2014, 6, 5, 0, 0).toInstant(ZoneOffset.UTC));
        channel.validateValues(reading, null);
    }

    @Test(expected = LocalizedFieldValidationException.class)
    public void testValidateBillingReadingWithTimestampGreaterThanEnd() {
        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID5_BIL, "Billing");
        channel = createChannel().init(meterActivation, ImmutableList.of(readingType1));
        ReadingImpl reading = ReadingImpl.of(readingType1.getMRID(), BigDecimal.valueOf(50), LocalDateTime.of(2014, 6, 6, 0, 0).toInstant(ZoneOffset.UTC));
        reading.setTimePeriod(
                LocalDateTime.of(2014, 6, 3, 0, 0).toInstant(ZoneOffset.UTC),
                LocalDateTime.of(2014, 6, 5, 0, 0).toInstant(ZoneOffset.UTC));
        channel.validateValues(reading, null);
    }

    @Test
    public void testValidateBillingReadingAtTimePeriodStart() {
        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID5_BIL, "Billing");
        channel = createChannel().init(meterActivation, ImmutableList.of(readingType1));
        Instant startInstant = LocalDateTime.of(2014, 6, 3, 0, 0).toInstant(ZoneOffset.UTC);
        ReadingImpl reading = ReadingImpl.of(readingType1.getMRID(), BigDecimal.valueOf(50), startInstant);
        reading.setTimePeriod(
                startInstant,
                LocalDateTime.of(2014, 6, 5, 0, 0).toInstant(ZoneOffset.UTC));
        channel.validateValues(reading, null);
        // no exception
    }

    @Test
    public void testValidateBillingReadingAtTimePeriodEnd() {
        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID5_BIL, "Billing");
        channel = createChannel().init(meterActivation, ImmutableList.of(readingType1));
        Instant endInstant = LocalDateTime.of(2014, 6, 5, 0, 0).toInstant(ZoneOffset.UTC);
        ReadingImpl reading = ReadingImpl.of(readingType1.getMRID(), BigDecimal.valueOf(50), endInstant);
        reading.setTimePeriod(
                LocalDateTime.of(2014, 6, 3, 0, 0).toInstant(ZoneOffset.UTC),
                endInstant);
        channel.validateValues(reading, null);
        // no exception
    }

    @Test
    public void testValidateBillingReadingInTheMiddleOfTimePeriod() {
        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID5_BIL, "Billing");
        channel = createChannel().init(meterActivation, ImmutableList.of(readingType1));
        ReadingImpl reading = ReadingImpl.of(readingType1.getMRID(), BigDecimal.valueOf(50), LocalDateTime.of(2014, 6, 4, 12, 0).toInstant(ZoneOffset.UTC));
        reading.setTimePeriod(
                LocalDateTime.of(2014, 6, 3, 0, 0).toInstant(ZoneOffset.UTC),
                LocalDateTime.of(2014, 6, 5, 0, 0).toInstant(ZoneOffset.UTC));
        channel.validateValues(reading, null);
        // no exception
    }


    @Test
    public void testGetTimePeriodForNumericalRegister() {
        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID1, "Numerical");
        channel = createChannel().init(meterActivation, ImmutableList.of(readingType1));

        ReadingImpl reading = ReadingImpl.of(readingType1.getMRID(), BigDecimal.valueOf(50), LocalDateTime.of(2014, 6, 4, 12, 0).toInstant(ZoneOffset.UTC));
        Object[] values = channel.toArray(reading, readingType1, new ProcessStatus(0));

        Optional<Range<Instant>> period = ((ChannelImpl) channel).getTimePeriod(reading, values);
        assertThat(period.isPresent()).isFalse();
    }

    @Test
    public void testGetTimePeriodForBillingRegister() {
        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID5_BIL, "Billing");
        channel = createChannel().init(meterActivation, ImmutableList.of(readingType1));

        ReadingImpl reading = ReadingImpl.of(readingType1.getMRID(), BigDecimal.valueOf(50), LocalDateTime.of(2014, 6, 4, 12, 0).toInstant(ZoneOffset.UTC));
        Instant start = LocalDateTime.of(2014, 6, 3, 0, 0).toInstant(ZoneOffset.UTC);
        Instant end = LocalDateTime.of(2014, 6, 5, 0, 0).toInstant(ZoneOffset.UTC);
        reading.setTimePeriod(start, end);
        Object[] values = channel.toArray(reading, readingType1, new ProcessStatus(0));

        Optional<Range<Instant>> period = ((ChannelImpl) channel).getTimePeriod(reading, values);
        assertThat(period.isPresent()).isTrue();
        assertThat(period.get().lowerEndpoint()).isEqualTo(start);
        assertThat(period.get().upperEndpoint()).isEqualTo(end);
    }

    @Test
    public void testGetTimePeriodForCorruptedReading() {
        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID5_BIL, "Billing");
        channel = createChannel().init(meterActivation, ImmutableList.of(readingType1));
        ReadingImpl reading = ReadingImpl.of(readingType1.getMRID(), BigDecimal.valueOf(50), LocalDateTime.of(2014, 6, 4, 12, 0).toInstant(ZoneOffset.UTC));
        Object[] values = channel.toArray(reading, readingType1, new ProcessStatus(0));

        Optional<Range<Instant>> period = ((ChannelImpl) channel).getTimePeriod(null, values);
        assertThat(period.isPresent()).isFalse();
    }

    @Test
    public void testGetTimePeriodForCorruptedValues() {
        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID5_BIL, "Billing");
        channel = createChannel().init(meterActivation, ImmutableList.of(readingType1));
        Optional<Range<Instant>> period = ((ChannelImpl) channel).getTimePeriod(null, new Object[]{});
        assertThat(period.isPresent()).isFalse();
    }

    private void simulateSavedChannel() {
        field("id").ofType(Long.TYPE).in(channel).set(ID);
    }
}
