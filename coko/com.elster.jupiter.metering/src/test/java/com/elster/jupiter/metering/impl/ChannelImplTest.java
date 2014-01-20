package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.IntervalLengthUnit;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
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
    private static final long METER_ACTIVATION_ID = 164;
    private static final long ID = 15L;
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Asia/Calcutta");
    private static final Date TO = new DateMidnight(2013, 9, 20, DateTimeZone.forTimeZone(TIME_ZONE)).toDate();
    private static final Date FROM = new DateMidnight(2013, 9, 19, DateTimeZone.forTimeZone(TIME_ZONE)).toDate();
    private static final Interval INTERVAL = new Interval(FROM, TO);
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
   

    @Before
    public void setUp() {
        when(dataModel.getInstance(ChannelImpl.class)).thenReturn(new ChannelImpl(dataModel, idsService, clock));
        when(dataModel.getInstance(ReadingTypeImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ReadingTypeImpl(dataModel);
            }
        });
        when(dataModel.getInstance(ReadingTypeInChannel.class)).thenReturn(new ReadingTypeInChannel());
        when(meterActivation.getId()).thenReturn(METER_ACTIVATION_ID);
        when(clock.getTimeZone()).thenReturn(TIME_ZONE);
        when(idsService.getVault(MeteringService.COMPONENTNAME, 1)).thenReturn(Optional.of(vault));
        when(idsService.getVault(MeteringService.COMPONENTNAME, 2)).thenReturn(Optional.of(vault));
        when(idsService.getVault(MeteringService.COMPONENTNAME, 3)).thenReturn(Optional.of(vault));
        when(idsService.getVault(MeteringService.COMPONENTNAME, 4)).thenReturn(Optional.of(vault));
        when(idsService.getRecordSpec(MeteringService.COMPONENTNAME, 1)).thenReturn(Optional.of(recordSpec));
        when(idsService.getRecordSpec(MeteringService.COMPONENTNAME, 2)).thenReturn(Optional.of(recordSpec));
        when(idsService.getRecordSpec(MeteringService.COMPONENTNAME, 4)).thenReturn(Optional.of(recordSpec));
        when(vault.createIrregularTimeSeries(recordSpec, TIME_ZONE)).thenReturn(timeSeries);
        when(vault.createRegularTimeSeries(recordSpec, TIME_ZONE, 1, IntervalLengthUnit.DAY, 0)).thenReturn(regularTimeSeries);
        when(timeSeries.getId()).thenReturn(TIMESERIES_ID);
        when(regularTimeSeries.getId()).thenReturn(TIMESERIES_ID);

        readingType1 = ReadingTypeImpl.from(dataModel, MRID1, "1");
        readingType2 = ReadingTypeImpl.from(dataModel, MRID2, "2");
        readingType3 = ReadingTypeImpl.from(dataModel, MRID3, "3");
        readingType4 = ReadingTypeImpl.from(dataModel, MRID4, "4");

        channel = ChannelImpl.from(dataModel, meterActivation);
    }

    @After
    public void tearDown() {
    }

    @Override
    protected Object getInstanceA() {
        if (channelInstanceA == null) {
            channelInstanceA = new ChannelImpl(dataModel, idsService, clock).init(meterActivation);
            field("id").ofType(Long.TYPE).in(channelInstanceA).set(ID);
        }
        return channelInstanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        ChannelImpl channel1 = new ChannelImpl(dataModel, idsService, clock).init(meterActivation);
        field("id").ofType(Long.TYPE).in(channel1).set(ID);

        return channel1;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        ChannelImpl channel1 = ChannelImpl.from(dataModel, meterActivation);
        field("id").ofType(Long.TYPE).in(channel1).set(ID + 1);
        return ImmutableList.of(channel1, ChannelImpl.from(dataModel, meterActivation));
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
        readingType1 = ReadingTypeImpl.from(dataModel, MRID1_IRR, "1");
        readingType2 = ReadingTypeImpl.from(dataModel, MRID2_IRR, "2");

        channel.init(Arrays.<ReadingType>asList(readingType1, readingType2));

        assertThat(channel.getMainReadingType()).isEqualTo(readingType1);
        assertThat(channel.getBulkQuantityReadingType()).isAbsent();
        assertThat(channel.getReadingTypes()).hasSize(2)
                .contains(readingType1)
                .contains(readingType2);
        assertThat(channel.getIntervalLength()).isAbsent();
        assertThat(channel.getTimeSeries()).isEqualTo(timeSeries);
    }

    @Test
    public void testInitWithIntervalLength() {
        channel.init(Arrays.<ReadingType>asList(readingType1, readingType2));

        assertThat(channel.getMainReadingType()).isEqualTo(readingType1);
        assertThat(channel.getBulkQuantityReadingType().get()).isEqualTo(readingType2);
        assertThat(channel.getReadingTypes()).hasSize(2)
                .contains(readingType1)
                .contains(readingType2);
        assertThat(channel.getIntervalLength()).isEqualTo(Optional.of(IntervalLength.ofDay()));
        assertThat(channel.getTimeSeries()).isEqualTo(regularTimeSeries);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitWithInconsistentIntervalLength() {
        channel.init(Arrays.<ReadingType>asList(readingType1, readingType3));
    }

    @Test
    public void testInitIrregularTimeSeriesWithAdditionalReadingTypes() {
        readingType1 = ReadingTypeImpl.from(dataModel, MRID1_IRR, "1");
        readingType2 = ReadingTypeImpl.from(dataModel, MRID2_IRR, "2");
        readingType4 = ReadingTypeImpl.from(dataModel, MRID4_IRR, "4");

        channel.init(Arrays.<ReadingType>asList(readingType1, readingType2, readingType4));

        assertThat(channel.getMainReadingType()).isEqualTo(readingType1);
        assertThat(channel.getBulkQuantityReadingType()).isAbsent();
        assertThat(channel.getReadingTypes()).hasSize(3)
                .contains(readingType1)
                .contains(readingType2)
                .contains(readingType4);
        assertThat(channel.getIntervalLength()).isAbsent();
        assertThat(channel.getTimeSeries()).isEqualTo(timeSeries);
    }

    @Test
    public void testGetIntervalReadings() {
        channel.init(Arrays.<ReadingType>asList(readingType1, readingType2));
        when(regularTimeSeries.getEntries(INTERVAL)).thenReturn(Arrays.asList(timeSeriesEntry));
        when(timeSeriesEntry.getBigDecimal(2)).thenReturn(VALUE);

        List<IntervalReadingRecord> intervalReadings = channel.getIntervalReadings(INTERVAL);

        assertThat(intervalReadings).hasSize(1);

        IntervalReadingRecord intervalReading = intervalReadings.get(0);

        assertThat(intervalReading.getReadingTypes()).hasSize(2)
                .contains(readingType1)
                .contains(readingType2);
        assertThat(intervalReading.getValue()).isEqualTo(VALUE);
    }

    @Test
    public void testGetIntervalReadingsForReadingType() {
        channel.init(Arrays.<ReadingType>asList(readingType1, readingType2));
        when(regularTimeSeries.getEntries(INTERVAL)).thenReturn(Arrays.asList(timeSeriesEntry));
        when(timeSeriesEntry.getBigDecimal(anyInt())).thenReturn(VALUE);

        List<IntervalReadingRecord> intervalReadings = channel.getIntervalReadings(readingType1, INTERVAL);

        assertThat(intervalReadings).hasSize(1);

        IntervalReadingRecord intervalReading = intervalReadings.get(0);

        assertThat(intervalReading.getReadingTypes()).hasSize(1)
                .contains(readingType1);
        assertThat(intervalReading.getValue()).isEqualTo(VALUE);
    }

    @Test
    public void testGetRegisterReadings() {
        channel.init(Arrays.<ReadingType>asList(readingType1, readingType2));
        when(regularTimeSeries.getEntries(INTERVAL)).thenReturn(Arrays.asList(timeSeriesEntry));
        when(timeSeriesEntry.getBigDecimal(anyInt())).thenReturn(VALUE);

        List<ReadingRecord> registerReadings = channel.getRegisterReadings(INTERVAL);

        assertThat(registerReadings).hasSize(1);

        ReadingRecord registerReading = registerReadings.get(0);

        assertThat(registerReading.getReadingTypes()).hasSize(2)
                .contains(readingType1)
                .contains(readingType2);
        assertThat(registerReading.getValue()).isEqualTo(VALUE);
    }


    private void simulateSavedChannel() {
        field("id").ofType(Long.TYPE).in(channel).set(ID);
    }
}
