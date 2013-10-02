package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReading;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MeterActivationImplTest {

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
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;
    @Mock
    private Channel channel1, channel2;
    @Mock
    private ReadingType readingType1, readingType2, readingType3;
    @Mock
    private IntervalReading reading1, reading2;

    @Before
    public void setUp() {

        when(usagePoint.getId()).thenReturn(USAGEPOINT_ID);
        when(meter.getId()).thenReturn(METER_ID);
        when(channel1.getReadingTypes()).thenReturn(Arrays.asList(readingType1, readingType2));
        when(channel2.getReadingTypes()).thenReturn(Arrays.asList(readingType3));

        Bus.setServiceLocator(serviceLocator);

        meterActivation = new MeterActivationImpl(usagePoint, ACTIVATION_TIME, meter);

        when(serviceLocator.getOrmClient().getChannelFactory().find("meterActivation", meterActivation)).thenReturn(Arrays.asList(channel1, channel2));
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testCreationRemembersUsagePoint() {
        assertThat(meterActivation.getUsagePoint().isPresent()).isTrue();
        assertThat(meterActivation.getUsagePoint().get()).isEqualTo(usagePoint);
    }

    @Test
    public void testCreationRemembersMeter() {
        assertThat(meterActivation.getMeter().isPresent()).isTrue();
        assertThat(meterActivation.getMeter().get()).isEqualTo(meter);
    }

    @Test
    public void testCreationRemembersStartDate() {
        assertThat(meterActivation.getStart()).isEqualTo(ACTIVATION_TIME);
    }

    @Test
    public void testGetChannels() {
        assertThat(meterActivation.getChannels()).isEqualTo(Arrays.asList(channel1, channel2));
    }

    @Test
    public void testGetEnd() {
        simulateSavedMeterActivation();

        meterActivation.endAt(END);

        verify(serviceLocator.getOrmClient().getMeterActivationFactory()).update(meterActivation);

        assertThat(meterActivation.getEnd()).isEqualTo(END);
    }

    @Test
    public void testGetReadingTypes() {
        assertThat(meterActivation.getReadingTypes())
                .hasSize(3)
                .contains(readingType1, readingType2, readingType3);
    }

    @Test
    public void testCreateChannel() {
        ChannelBuilder channelBuilder = mock(ChannelBuilder.class);
        when(channelBuilder.meterActivation(meterActivation)).thenReturn(channelBuilder);
        when(channelBuilder.readingTypes(readingType1, readingType3)).thenReturn(channelBuilder);
        when(channelBuilder.build()).thenReturn(channel1);
        when(Bus.getChannelBuilder()).thenReturn(channelBuilder);

        Channel channel = meterActivation.createChannel(readingType1, readingType3);

        assertThat(channel).isEqualTo(channel1);
        verify(channelBuilder).meterActivation(meterActivation);
        verify(channelBuilder).readingTypes(readingType1, readingType3);
    }


    private void simulateSavedMeterActivation() {
        field("id").ofType(Long.TYPE).in(meterActivation).set(ID);
    }

    @Test
    public void testGetReadings() {
        Date to = new DateTime(2300, 2, 28, 14, 15, 9, 2).toDate();
        when(channel1.getIntervalReadings(readingType1, ACTIVATION_TIME, END)).thenReturn(Arrays.asList(reading1, reading2));

        meterActivation.endAt(END);

        assertThat(meterActivation.getReadings(ACTIVATION_TIME, to, readingType1)).hasSize(2).contains(reading1, reading2);
    }

}
