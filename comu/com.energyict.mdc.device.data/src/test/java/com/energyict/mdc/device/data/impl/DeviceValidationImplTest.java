package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.MeterActivationValidation;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Channel;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeviceValidationImplTest {

    public static final Date NOW = new DateTime(2014, 7, 11, 9, 37, 53, 740).toDate();
    public static final Date LAST_CHECKED = new DateTime(2014, 7, 1, 14, 15, 0, 0).toDate();
    public static final Date MANUAL_LAST_CHECKED = new DateTime(2014, 6, 1, 14, 15, 0, 0).toDate();
    public static final Date SWITCH = new DateTime(2014, 7, 5, 0, 0, 0, 0).toDate();
    private DeviceValidationImpl deviceValidation;

    @Mock
    private DeviceImpl device;
    @Mock
    private ValidationService validationService;
    @Mock
    private AmrSystem amrSystem;
    @Mock
    private Meter meter;
    @Mock
    private Channel channel;
    @Mock
    private com.elster.jupiter.metering.Channel koreChannel, otherKoreChannel;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private MeterActivationValidation meterActivationValidation1, meterActivationValidation2;
    @Mock
    private ChannelValidation channelValidation1, channelValidation2, channelValidation3;
    @Mock
    private com.elster.jupiter.metering.ReadingType readingType;
    @Mock
    private MeterActivation meterActivation1, meterActivation2;
    @Mock
    private com.elster.jupiter.metering.Channel koreChannel1, koreChannel2, koreChannel3, koreChannel4;
    @Mock
    private Clock clock;
    @Mock
    private ValidationEvaluator validationEvaluator;

    @Before
    public void setUp() {
        deviceValidation = new DeviceValidationImpl(amrSystem, validationService, clock, device);

        when(device.findKoreMeter(amrSystem)).thenReturn(Optional.of(meter));
        when(device.findKoreChannel(channel, NOW)).thenReturn(Optional.of(koreChannel));
        when(koreChannel.getMeterActivation()).thenReturn(meterActivation);
        doReturn(asList(readingType)).when(koreChannel).getReadingTypes();
        doReturn(asList(meterActivation)).when(meter).getMeterActivations();
        when(meterActivation.getInterval()).thenReturn(Interval.sinceEpoch());
        when(meterActivation.getChannels()).thenReturn(asList(koreChannel));
        when(meterActivationValidation1.getChannelValidations()).thenReturn(ImmutableSet.of(channelValidation1, channelValidation2));
        when(meterActivationValidation2.getChannelValidations()).thenReturn(ImmutableSet.of(channelValidation3));
        doReturn(asList(meterActivationValidation1, meterActivationValidation2)).when(validationService).getMeterActivationValidations(meterActivation);
        when(channelValidation1.hasActiveRules()).thenReturn(false);
        when(channelValidation2.hasActiveRules()).thenReturn(false);
        when(channelValidation3.hasActiveRules()).thenReturn(false);
        when(clock.now()).thenReturn(NOW);

        when(meterActivationValidation1.getChannelValidation(koreChannel)).thenReturn(Optional.of(channelValidation2));

        when(channelValidation2.getChannel()).thenReturn(koreChannel);
        when(channelValidation1.getChannel()).thenReturn(otherKoreChannel);
        when(channelValidation3.getChannel()).thenReturn(otherKoreChannel);

        when(channel.getReadingType()).thenReturn(readingType);
        when(device.findOrCreateKoreMeter(amrSystem)).thenReturn(meter);
        when(validationService.getEvaluator(eq(meter), any(Interval.class))).thenReturn(validationEvaluator);
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testIsValidationActive() {
        when(validationEvaluator.isValidationEnabled(meter)).thenReturn(true);

        boolean validationActive = deviceValidation.isValidationActive();
        assertThat(validationActive).isTrue();
    }

    @Test
    public void testIsValidationActiveInactiveWithoutKoreMeter() {
        when(validationService.validationEnabled(meter)).thenReturn(true);
        when(device.findKoreMeter(amrSystem)).thenReturn(Optional.absent());

        boolean validationActive = deviceValidation.isValidationActive();
        assertThat(validationActive).isFalse();
    }

    @Test
    public void testValidationActiveForChannelInactiveIfDeviceInactive() {
        when(validationService.validationEnabled(meter)).thenReturn(false);

        boolean validationActive = deviceValidation.isValidationActive(channel, NOW);
        assertThat(validationActive).isFalse();
    }

    @Test
    public void testValidationActiveForChannel() {
        when(validationEvaluator.isValidationEnabled(meter)).thenReturn(true);
        when(validationEvaluator.isValidationEnabled(koreChannel)).thenReturn(true);
        when(channelValidation2.hasActiveRules()).thenReturn(true);

        boolean validationActive = deviceValidation.isValidationActive(channel, NOW);
        assertThat(validationActive).isTrue();
    }

    @Test
    public void testValidateChannel() {
        doReturn(asList(meterActivation1, meterActivation2)).when(meter).getMeterActivations();
        when(meterActivation1.getChannels()).thenReturn(asList(koreChannel1, koreChannel2));
        when(meterActivation2.getChannels()).thenReturn(asList(koreChannel3, koreChannel4));
        doReturn(asList(readingType)).when(koreChannel2).getReadingTypes();
        doReturn(asList(readingType)).when(koreChannel4).getReadingTypes();
        when(readingType.getMRID()).thenReturn("MRID");
        when(validationService.getLastChecked(koreChannel2)).thenReturn(Optional.of(LAST_CHECKED));
        when(validationService.getLastChecked(koreChannel4)).thenReturn(Optional.absent());
        when(validationEvaluator.getLastChecked(meter, readingType)).thenReturn(Optional.of(LAST_CHECKED), Optional.<Date>absent());
        when(koreChannel1.getMeterActivation()).thenReturn(meterActivation1);
        when(koreChannel2.getMeterActivation()).thenReturn(meterActivation1);
        when(koreChannel3.getMeterActivation()).thenReturn(meterActivation2);
        when(koreChannel4.getMeterActivation()).thenReturn(meterActivation2);
        when(meterActivation1.getInterval()).thenReturn(Interval.endAt(SWITCH));
        when(meterActivation2.getInterval()).thenReturn(Interval.startAt(SWITCH));

        deviceValidation.validateChannel(channel, null, NOW);

        verify(validationService).validate(meterActivation1, "MRID", Interval.startAt(LAST_CHECKED).withEnd(SWITCH));
        verify(validationService).validate(meterActivation2, "MRID", Interval.startAt(SWITCH).withEnd(NOW));
    }

    @Test
    public void testValidateChannelWithDate() {
        doReturn(asList(meterActivation1, meterActivation2)).when(meter).getMeterActivations();
        when(meterActivation1.getChannels()).thenReturn(asList(koreChannel1, koreChannel2));
        when(meterActivation2.getChannels()).thenReturn(asList(koreChannel3, koreChannel4));
        doReturn(asList(readingType)).when(koreChannel2).getReadingTypes();
        doReturn(asList(readingType)).when(koreChannel4).getReadingTypes();
        when(readingType.getMRID()).thenReturn("MRID");
        when(validationService.getLastChecked(koreChannel2)).thenReturn(Optional.of(LAST_CHECKED));
        when(validationService.getLastChecked(koreChannel4)).thenReturn(Optional.absent());
        when(koreChannel1.getMeterActivation()).thenReturn(meterActivation1);
        when(koreChannel2.getMeterActivation()).thenReturn(meterActivation1);
        when(koreChannel3.getMeterActivation()).thenReturn(meterActivation2);
        when(koreChannel4.getMeterActivation()).thenReturn(meterActivation2);
        when(meterActivation1.getInterval()).thenReturn(Interval.endAt(SWITCH));
        when(meterActivation2.getInterval()).thenReturn(Interval.startAt(SWITCH));

        deviceValidation.validateChannel(channel, MANUAL_LAST_CHECKED, NOW);

        verify(validationService).validate(meterActivation1, "MRID", Interval.startAt(MANUAL_LAST_CHECKED).withEnd(SWITCH));
        verify(validationService).validate(meterActivation2, "MRID", Interval.startAt(SWITCH).withEnd(NOW));
    }

}