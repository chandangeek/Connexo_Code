package com.energyict.mdc.device.data.impl;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;

import com.energyict.mdc.device.data.Channel;
import com.google.common.collect.Range;

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
        deviceValidation = new DeviceValidationImpl(amrSystem, validationService, clock, thesaurus, device);

        when(device.findKoreMeter(amrSystem)).thenReturn(Optional.of(meter));
        when(device.findKoreChannel(channel, NOW.toInstant())).thenReturn(Optional.of(koreChannel));
        when(koreChannel.getMeterActivation()).thenReturn(meterActivation);
        doReturn(asList(readingType)).when(koreChannel).getReadingTypes();
        doReturn(asList(meterActivation)).when(meter).getMeterActivations();
        when(meterActivation.getRange()).thenReturn(Range.atLeast(Instant.EPOCH));
        when(meterActivation.getChannels()).thenReturn(asList(koreChannel));
        when(clock.instant()).thenReturn(NOW.toInstant());
        when(channel.getReadingType()).thenReturn(readingType);
        when(device.findOrCreateKoreMeter(amrSystem)).thenReturn(meter);
        when(validationService.getEvaluator(eq(meter), any(Range.class))).thenReturn(validationEvaluator);
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
        when(device.findKoreMeter(amrSystem)).thenReturn(Optional.empty());

        boolean validationActive = deviceValidation.isValidationActive();
        assertThat(validationActive).isFalse();
    }

    @Test
    public void testValidationActiveForChannelInactiveIfDeviceInactive() {
        when(validationService.validationEnabled(meter)).thenReturn(false);

        boolean validationActive = deviceValidation.isValidationActive(channel, NOW.toInstant());
        assertThat(validationActive).isFalse();
    }

    @Test
    public void testValidationActiveForChannel() {
        when(validationEvaluator.isValidationEnabled(meter)).thenReturn(true);
        when(validationEvaluator.isValidationEnabled(koreChannel)).thenReturn(true);

        boolean validationActive = deviceValidation.isValidationActive(channel, NOW.toInstant());
        assertThat(validationActive).isTrue();
    }

    @Test
    public void testValidateChannel() {
        doReturn(asList(meterActivation1, meterActivation2)).when(meter).getMeterActivations();
        when(meterActivation1.getChannels()).thenReturn(asList(koreChannel1, koreChannel2));
        when(meterActivation2.getChannels()).thenReturn(asList(koreChannel3, koreChannel4));
        doReturn(asList(readingType)).when(koreChannel2).getReadingTypes();
        doReturn(asList(readingType)).when(koreChannel4).getReadingTypes();
        when(koreChannel2.getMainReadingType()).thenReturn(readingType);
        when(koreChannel4.getMainReadingType()).thenReturn(readingType);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType.getMRID()).thenReturn("MRID");
        when(validationService.getLastChecked(koreChannel2)).thenReturn(Optional.of(LAST_CHECKED.toInstant()));
        when(validationService.getLastChecked(koreChannel4)).thenReturn(Optional.empty());
        when(validationEvaluator.getLastChecked(meter, readingType)).thenReturn(Optional.of(LAST_CHECKED.toInstant()), Optional.<Instant>empty());
        when(koreChannel1.getMeterActivation()).thenReturn(meterActivation1);
        when(koreChannel2.getMeterActivation()).thenReturn(meterActivation1);
        when(koreChannel3.getMeterActivation()).thenReturn(meterActivation2);
        when(koreChannel4.getMeterActivation()).thenReturn(meterActivation2);
        when(meterActivation1.getRange()).thenReturn(Range.closedOpen(Instant.EPOCH, SWITCH.toInstant()));
        when(meterActivation2.getRange()).thenReturn(Range.atLeast(SWITCH.toInstant()));

        deviceValidation.validateChannel(channel);

        verify(validationService).validate(meterActivation1, readingType);
        verify(validationService).validate(meterActivation2, readingType);
    }

    @Test
    public void testValidateChannelWithDate() {
        doReturn(asList(meterActivation1, meterActivation2)).when(meter).getMeterActivations();
        when(meterActivation1.getChannels()).thenReturn(asList(koreChannel1, koreChannel2));
        when(meterActivation2.getChannels()).thenReturn(asList(koreChannel3, koreChannel4));
        doReturn(asList(readingType)).when(koreChannel2).getReadingTypes();
        doReturn(asList(readingType)).when(koreChannel4).getReadingTypes();
        when(koreChannel2.getMainReadingType()).thenReturn(readingType);
        when(koreChannel4.getMainReadingType()).thenReturn(readingType);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType.getMRID()).thenReturn("MRID");
        when(validationService.getLastChecked(koreChannel2)).thenReturn(Optional.of(LAST_CHECKED.toInstant()));
        when(validationService.getLastChecked(koreChannel4)).thenReturn(Optional.empty());
        when(koreChannel1.getMeterActivation()).thenReturn(meterActivation1);
        when(koreChannel2.getMeterActivation()).thenReturn(meterActivation1);
        when(koreChannel3.getMeterActivation()).thenReturn(meterActivation2);
        when(koreChannel4.getMeterActivation()).thenReturn(meterActivation2);
        when(meterActivation1.getRange()).thenReturn(Range.closed(Instant.EPOCH, SWITCH.toInstant()));
        when(meterActivation2.getRange()).thenReturn(Range.atLeast(SWITCH.toInstant()));

        deviceValidation.validateChannel(channel);

        verify(validationService).validate(meterActivation1, readingType);
        verify(validationService).validate(meterActivation2, readingType);
    }

}
