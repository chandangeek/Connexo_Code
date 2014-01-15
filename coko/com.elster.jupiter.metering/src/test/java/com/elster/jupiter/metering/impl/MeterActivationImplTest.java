package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.IntervalLengthUnit;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import com.google.inject.Provider;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MeterActivationImplTest {

    private static final String MRID1 = "13.2.2.4.0.8.12.8.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID2 = "13.2.2.1.0.8.12.9.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID3 = "13.2.3.4.0.8.12.10.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID4 = "13.2.3.4.0.8.12.10.16.9.11.12.13.14.128.3.72.124";
    private static final Date ACTIVATION_TIME = new DateTime(1984, 11, 5, 13, 37, 3, 14).toDate();
    private static final long USAGEPOINT_ID = 6546L;
    private static final long METER_ID = 46335L;
    private static final Date END = new DateTime(2166, 8, 6, 8, 35, 0, 0).toDate();
    private static final long ID = 154177L;

    private MeterActivationImpl meterActivation;

    @Mock
    private UsagePointImpl usagePoint;
    @Mock
    private Meter meter;
    @Mock
    private Channel channel1, channel2;
    private ReadingTypeImpl readingType1, readingType2, readingType3;
    @Mock
    private IntervalReadingRecord reading1, reading2;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private Clock clock;
    private Provider<ChannelBuilder> channelBuilder;
    @Mock
    private IdsService idsService;
    @Mock
    private Vault vault;
    @Mock
    private RecordSpec recordSpec;
    private TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
    @Mock
    private TimeSeries timeSeries;

    @Before
    public void setUp() {
        when((Object) dataModel.getInstance(ReadingTypeImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ReadingTypeImpl(dataModel);
            }
        });
        when((Object) dataModel.getInstance(ReadingTypeInChannel.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ReadingTypeInChannel();
            }
        });
        readingType1 = ReadingTypeImpl.from(dataModel, MRID1, "readingType1");
        readingType2 = ReadingTypeImpl.from(dataModel, MRID2, "readingType2");
        readingType3 = ReadingTypeImpl.from(dataModel, MRID3, "readingType3");
        channelBuilder = new Provider<ChannelBuilder>() {
			@Override
			public ChannelBuilder get() {
				return new ChannelBuilderImpl(dataModel);
			}
        };
        when((Object) dataModel.getInstance(MeterActivationImpl.class)).thenReturn(new MeterActivationImpl(dataModel, eventService, clock, channelBuilder));
        when(usagePoint.getId()).thenReturn(USAGEPOINT_ID);
        when(meter.getId()).thenReturn(METER_ID);
        when(channel1.getReadingTypes()).thenReturn(Arrays.<ReadingType>asList(readingType1, readingType2));
        when(channel2.getReadingTypes()).thenReturn(Arrays.<ReadingType>asList(readingType3));
        when((Object) dataModel.getInstance(ChannelImpl.class)).thenReturn(new ChannelImpl(dataModel, idsService, clock));
        when(idsService.getVault(anyString(), anyInt())).thenReturn(Optional.of(vault));
        when(idsService.getRecordSpec(anyString(), anyInt())).thenReturn(Optional.of(recordSpec));
        when(clock.getTimeZone()).thenReturn(timeZone);

        meterActivation = MeterActivationImpl.from(dataModel, meter, usagePoint, ACTIVATION_TIME);

        when(dataModel.mapper(Channel.class).find("meterActivation", meterActivation)).thenReturn(Arrays.asList(channel1, channel2));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreationRemembersUsagePoint() {
        assertThat(meterActivation.getUsagePoint()).contains(usagePoint);
    }

    @Test
    public void testCreationRemembersMeter() {
        assertThat(meterActivation.getMeter()).contains(meter);
    }

    @Test
    public void testCreationRemembersStartDate() {
        assertThat(meterActivation.getStart()).isEqualTo(ACTIVATION_TIME);
    }

    @Test
    public void testGetEnd() {
        simulateSavedMeterActivation();

        meterActivation.endAt(END);

        verify(dataModel.mapper(MeterActivation.class)).update(meterActivation);

        assertThat(meterActivation.getEnd()).isEqualTo(END);
    }


    @Test
    public void testCreateChannel() {
        when(vault.createRegularTimeSeries(eq(recordSpec), eq(timeZone), anyInt(), any(IntervalLengthUnit.class), anyInt())).thenReturn(timeSeries);

        Channel channel = meterActivation.createChannel(readingType1, readingType3);

        assertThat(channel.getMeterActivation()).isEqualTo(meterActivation);
        assertThat(channel.getReadingTypes()).isEqualTo(Arrays.asList(readingType1, readingType3));
    }


    private void simulateSavedMeterActivation() {
        field("id").ofType(Long.TYPE).in(meterActivation).set(ID);
    }


}
