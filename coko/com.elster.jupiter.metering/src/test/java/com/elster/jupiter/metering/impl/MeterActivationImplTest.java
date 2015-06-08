package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterAlreadyLinkedToUsagePoint;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.TimeZone;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MeterActivationImplTest {

    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();

    private static final String MRID1 = "13.2.2.4.0.8.12.8.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID2 = "13.2.2.1.0.8.12.9.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID3 = "13.2.3.4.0.8.12.10.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID4 = "13.2.3.4.0.8.12.10.16.9.11.12.13.14.128.3.72.124";
    private static final ZonedDateTime BASE = ZonedDateTime.of(1984, 11, 5, 13, 37, 3, 14_000_000, TimeZoneNeutral.getMcMurdo());
    private static final Instant ACTIVATION_TIME = BASE.toInstant();
    private static final long USAGEPOINT_ID = 6546L;
    private static final long METER_ID = 46335L;
    private static final Instant END = ZonedDateTime.of(2166, 8, 6, 8, 35, 0, 0, TimeZoneNeutral.getMcMurdo()).toInstant();
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
    private MeteringService meteringService;
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
				return new ChannelImpl(dataModel, idsService, meteringService, clock, eventService);
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
        when(clock.getZone()).thenReturn(timeZone.toZoneId());

        meterActivation = new MeterActivationImpl(dataModel,eventService,clock,channelBuilder, thesaurus).init(meter, usagePoint, ACTIVATION_TIME);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreationRemembersUsagePoint() {
        assertThat(meterActivation.getUsagePoint().get()).isEqualTo(usagePoint);
    }

    @Test
    public void testCreationRemembersMeter() {
        assertThat(meterActivation.getMeter().get()).isEqualTo(meter);
    }

    @Test
    public void testCreationRemembersStartDate() {
        assertThat(meterActivation.getRange().lowerEndpoint()).isEqualTo(ACTIVATION_TIME);
    }

    @Test
    public void testGetEnd() {
        simulateSavedMeterActivation();

        meterActivation.endAt(END);

        verify(dataModel.mapper(MeterActivation.class)).update(meterActivation);

        assertThat(meterActivation.getRange().upperEndpoint()).isEqualTo(END);
    }


    @Test
    public void testCreateChannel() {
        when(vault.createRegularTimeSeries(eq(recordSpec), eq(timeZone), any(), anyInt())).thenReturn(timeSeries);

        Channel channel = meterActivation.createChannel(readingType1, readingType3);

        assertThat(channel.getMeterActivation()).isEqualTo(meterActivation);
        assertThat(channel.getReadingTypes()).isEqualTo(Arrays.asList(readingType1, readingType3));
    }

    @Test
    public void testSetUsagePoint() {
        meterActivation = new MeterActivationImpl(dataModel,eventService,clock,channelBuilder, thesaurus).init(meter, ACTIVATION_TIME);

        assertThat(meterActivation.getUsagePoint()).isAbsent();

        meterActivation.setUsagePoint(usagePoint);

        assertThat(meterActivation.getUsagePoint()).contains(usagePoint);
    }

    @Test(expected = MeterAlreadyLinkedToUsagePoint.class)
    public void testSetUsagePointWhenAlreadySet() {
        meterActivation = new MeterActivationImpl(dataModel,eventService,clock,channelBuilder, thesaurus).init(meter, usagePoint, ACTIVATION_TIME);

        meterActivation.setUsagePoint(usagePoint);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdvanceStartDateMustBeEarlier() {
        meterActivation = new MeterActivationImpl(dataModel,eventService,clock,channelBuilder, thesaurus).init(meter, usagePoint, ACTIVATION_TIME);

        meterActivation.advanceStartDate(BASE.plusSeconds(1).toInstant());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdvanceStartDateMustNotOverlapWithMeterActivationOfMeter() {
        meterActivation = new MeterActivationImpl(dataModel,eventService,clock,channelBuilder, thesaurus).init(meter, usagePoint, ACTIVATION_TIME);

        MeterActivation earlier = mock(MeterActivation.class);

        doReturn(Arrays.asList(earlier, meterActivation)).when(meter).getMeterActivations();
        when(earlier.getId()).thenReturn(516501L);
        when(earlier.getRange()).thenReturn(Range.closedOpen(BASE.minusYears(1).toInstant(), ACTIVATION_TIME));

        meterActivation.advanceStartDate(BASE.minusDays(5).toInstant());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdvanceStartDateMustNotOverlapWithMeterActivationOfUsagePoint() {
        meterActivation = new MeterActivationImpl(dataModel,eventService,clock,channelBuilder, thesaurus).init(meter, usagePoint, ACTIVATION_TIME);

        MeterActivation earlier = mock(MeterActivation.class);

        doReturn(Arrays.asList(earlier, meterActivation)).when(usagePoint).getMeterActivations();
        when(earlier.getId()).thenReturn(516501L);
        when(earlier.getRange()).thenReturn(Range.closedOpen(BASE.minusYears(1).toInstant(), ACTIVATION_TIME));

        meterActivation.advanceStartDate(BASE.minusDays(5).toInstant());
    }

    public void testAdvanceStartDateSuccess() {
        meterActivation = new MeterActivationImpl(dataModel,eventService,clock,channelBuilder, thesaurus).init(meter, usagePoint, ACTIVATION_TIME);
        field("id").ofType(Long.TYPE).in(meterActivation).set(987987L);

        MeterActivation earlier = mock(MeterActivation.class);

        doReturn(Arrays.asList(earlier, meterActivation)).when(usagePoint).getMeterActivations();
        when(earlier.getId()).thenReturn(516501L);
        when(earlier.getRange()).thenReturn(Range.closedOpen(BASE.minusYears(1).toInstant(), BASE.minusMonths(8).toInstant()));

        meterActivation.advanceStartDate(BASE.minusDays(5).toInstant());

        verify(dataModel.mapper(MeterActivation.class)).update(meterActivation);
    }

    private void simulateSavedMeterActivation() {
        field("id").ofType(Long.TYPE).in(meterActivation).set(ID);
    }


}
