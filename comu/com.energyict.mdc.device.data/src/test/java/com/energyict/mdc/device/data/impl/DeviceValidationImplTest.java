package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.MeterActivationValidation;
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

import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceValidationImplTest {

    public static final Date NOW = new DateTime(2014, 7, 11, 9, 37, 53, 740).toDate();
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

    @Before
    public void setUp() {
        deviceValidation = new DeviceValidationImpl(amrSystem, validationService, device);

        when(device.findKoreMeter(amrSystem)).thenReturn(Optional.of(meter));
        when(device.findKoreChannel(channel, NOW)).thenReturn(Optional.of(koreChannel));
        when(koreChannel.getMeterActivation()).thenReturn(meterActivation);
        when(meterActivationValidation1.getChannelValidations()).thenReturn(ImmutableSet.of(channelValidation1, channelValidation2));
        when(meterActivationValidation2.getChannelValidations()).thenReturn(ImmutableSet.of(channelValidation3));
        doReturn(Arrays.asList(meterActivationValidation1, meterActivationValidation2)).when(validationService).getMeterActivationValidations(meterActivation);
        when(channelValidation1.hasActiveRules()).thenReturn(false);
        when(channelValidation2.hasActiveRules()).thenReturn(false);
        when(channelValidation3.hasActiveRules()).thenReturn(false);

        when(channelValidation2.getChannel()).thenReturn(koreChannel);
        when(channelValidation1.getChannel()).thenReturn(otherKoreChannel);
        when(channelValidation3.getChannel()).thenReturn(otherKoreChannel);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testIsValidationActive() {
        when(validationService.validationEnabled(meter)).thenReturn(true);

        boolean validationActive = deviceValidation.isValidationActive(NOW);
        assertThat(validationActive).isTrue();
    }

    @Test
    public void testIsValidationActiveInactiveWithoutKoreMeter() {
        when(validationService.validationEnabled(meter)).thenReturn(true);
        when(device.findKoreMeter(amrSystem)).thenReturn(Optional.absent());

        boolean validationActive = deviceValidation.isValidationActive(NOW);
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
        when(validationService.validationEnabled(meter)).thenReturn(true);
        when(channelValidation2.hasActiveRules()).thenReturn(true);

        boolean validationActive = deviceValidation.isValidationActive(channel, NOW);
        assertThat(validationActive).isTrue();
    }

}