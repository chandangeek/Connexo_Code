package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.IntervalLength;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
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
    private ChannelImpl channel1, channel2;
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
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID1, "readingType1");
        readingType2 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID2, "readingType2");
        readingType3 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID3, "readingType3");
        
        final Provider<ChannelImpl> channelFactory = new Provider<ChannelImpl>() {
			@Override
			public ChannelImpl get() {
				return new ChannelImpl(dataModel, idsService, clock);
			}
		};
		
        channelBuilder = new Provider<ChannelBuilder>() {
			@Override
			public ChannelBuilder get() {
				return new ChannelBuilderImpl(dataModel,channelFactory);
			}
        };
        when(usagePoint.getId()).thenReturn(USAGEPOINT_ID);
        when(meter.getId()).thenReturn(METER_ID);
        when(idsService.getVault(anyString(), anyInt())).thenReturn(Optional.of(vault));
        when(idsService.getRecordSpec(anyString(), anyInt())).thenReturn(Optional.of(recordSpec));
        when(clock.getTimeZone()).thenReturn(timeZone);

        meterActivation = new MeterActivationImpl(dataModel,eventService,clock,channelBuilder).init(meter, usagePoint, ACTIVATION_TIME);
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
        when(vault.createRegularTimeSeries(eq(recordSpec), eq(timeZone), any(IntervalLength.class), anyInt())).thenReturn(timeSeries);

        Channel channel = meterActivation.createChannel(readingType1, readingType3);

        assertThat(channel.getMeterActivation()).isEqualTo(meterActivation);
        assertThat(channel.getReadingTypes()).isEqualTo(Arrays.asList(readingType1, readingType3));
    }


    private void simulateSavedMeterActivation() {
        field("id").ofType(Long.TYPE).in(meterActivation).set(ID);
    }


}
