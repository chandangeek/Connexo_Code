/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Channel;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceValidationImplTest {
    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final Instant LAST_CHECKED = ZonedDateTime.of(2014, 7, 1, 14, 15, 0, 0, PARIS).toInstant();
    private static final Instant SWITCH = ZonedDateTime.of(2014, 7, 5, 0, 0, 0, 0, PARIS).toInstant();
    private static final Instant NOW = ZonedDateTime.of(2014, 7, 11, 9, 37, 53, 740, PARIS).toInstant();
    private DeviceValidationImpl deviceValidation;

    @Mock
    private DeviceImpl device;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ValidationService validationService;
    @Mock
    private Reference<Meter> meterReference;
    @Mock
    private Meter meter;
    @Mock
    private Channel channel;
    @Mock
    private com.elster.jupiter.metering.Channel koreChannel, koreChannel1, koreChannel2, koreChannel3, koreChannel4;
    @Mock
    private ChannelsContainer channelsContainer1, channelsContainer2, channelsContainer3;
    @Mock
    private ReadingType readingType1, readingType2;
    @Mock
    private MeterActivation meterActivation1, meterActivation2, meterActivation3;
    @Mock
    private ValidationRuleSet ruleSet1, ruleSet2;
    @Mock
    private ValidationRule rule1, rule2;
    @Mock
    private Clock clock;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ValidationEvaluator validationEvaluator;

    @Before
    public void setUp() {
        when(device.getId()).thenReturn(666L);
        when(device.getMeter()).thenReturn(meterReference);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getValidationRuleSets()).thenReturn(Arrays.asList(ruleSet1, ruleSet2));
        when(rule1.getReadingTypes()).thenReturn(Collections.singleton(readingType1));
        when(rule2.getReadingTypes()).thenReturn(Collections.singleton(readingType2));
        doReturn(Collections.singletonList(readingType1)).when(koreChannel1).getReadingTypes();
        doReturn(Collections.singletonList(readingType2)).when(koreChannel2).getReadingTypes();
        doReturn(Collections.singletonList(readingType1)).when(koreChannel3).getReadingTypes();
        doReturn(Collections.singletonList(readingType2)).when(koreChannel4).getReadingTypes();

        when(device.getMeter()).thenReturn(meterReference);
        when(meterReference.get()).thenReturn(meter);
        when(meterReference.isPresent()).thenReturn(true);
        when(device.findKoreChannel(channel, NOW)).thenReturn(Optional.of(koreChannel));
        when(koreChannel.getChannelsContainer()).thenReturn(channelsContainer1);
        doReturn(Collections.singletonList(readingType1)).when(koreChannel).getReadingTypes();
        doReturn(asList(channelsContainer1, channelsContainer2, channelsContainer3)).when(meter).getChannelsContainers();
        when(meterActivation1.getChannelsContainer()).thenReturn(channelsContainer1);
        when(meterActivation2.getChannelsContainer()).thenReturn(channelsContainer2);
        when(meterActivation3.getChannelsContainer()).thenReturn(channelsContainer3);
        when(channelsContainer1.getRange()).thenReturn(Range.atLeast(Instant.EPOCH));
        when(channelsContainer1.getChannels()).thenReturn(Collections.singletonList(koreChannel));
        when(clock.instant()).thenReturn(NOW);
        when(channel.getReadingType()).thenReturn(readingType1);

        when(validationService.getEvaluator(meter)).thenReturn(validationEvaluator);
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);

        deviceValidation = new DeviceValidationImpl(validationService, thesaurus, device, clock);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetLastChecked() {
        doReturn(asList(meterActivation1, meterActivation2, meterActivation3)).when(meter).getMeterActivations();
        when(meterActivation3.getRange()).thenReturn(Range.lessThan(LAST_CHECKED));
        when(meterActivation2.getRange()).thenReturn(Range.closedOpen(LAST_CHECKED, SWITCH));
        when(meterActivation1.getRange()).thenReturn(Range.atLeast(SWITCH));
        when(validationService.getLastChecked(channelsContainer3)).thenReturn(Optional.of(LAST_CHECKED));
        Instant lastChecked2 = LAST_CHECKED.plus(1, ChronoUnit.HOURS);
        when(validationService.getLastChecked(channelsContainer2)).thenReturn(Optional.of(lastChecked2));
        when(validationService.getLastChecked(channelsContainer1)).thenReturn(Optional.of(NOW));

        assertThat(deviceValidation.getLastChecked()).contains(NOW);
        verify(validationService).getLastChecked(channelsContainer1);
        verifyNoMoreInteractions(validationService);

        // let's go deeper in the algorithm
        reset(validationService);
        when(validationService.getLastChecked(channelsContainer3)).thenReturn(Optional.of(LAST_CHECKED));
        when(validationService.getLastChecked(channelsContainer2)).thenReturn(Optional.of(lastChecked2));
        when(validationService.getLastChecked(channelsContainer1)).thenReturn(Optional.empty());

        assertThat(deviceValidation.getLastChecked()).contains(lastChecked2);
        verify(validationService).getLastChecked(channelsContainer1);
        verify(validationService).getLastChecked(channelsContainer2);
        verifyNoMoreInteractions(validationService);

        // and now let's dig to the bottom
        reset(validationService);
        when(validationService.getLastChecked(channelsContainer3)).thenReturn(Optional.empty());
        when(validationService.getLastChecked(channelsContainer2)).thenReturn(Optional.empty());
        when(validationService.getLastChecked(channelsContainer1)).thenReturn(Optional.empty());

        assertThat(deviceValidation.getLastChecked()).isEmpty();
        verify(validationService).getLastChecked(channelsContainer1);
        verify(validationService).getLastChecked(channelsContainer2);
        verify(validationService).getLastChecked(channelsContainer3);
    }

    @Test
    public void testIsValidationActive() {
        doReturn(asList(meterActivation1, meterActivation2)).when(meter).getMeterActivations();
        when(channelsContainer1.getChannels()).thenReturn(asList(koreChannel1, koreChannel2));
        when(channelsContainer2.getChannels()).thenReturn(asList(koreChannel3, koreChannel4));

        when(validationEvaluator.isValidationEnabled(meter)).thenReturn(true);

        boolean validationActive = deviceValidation.isValidationActive();
        assertThat(validationActive).isTrue();
    }

    @Test
    public void testIsValidationActiveInactiveWithoutKoreMeter() {
        when(validationService.validationEnabled(meter)).thenReturn(true);

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
        doReturn(asList(meterActivation1, meterActivation2)).when(meter).getMeterActivations();
        when(channelsContainer1.getChannels()).thenReturn(asList(koreChannel1, koreChannel2));
        when(channelsContainer2.getChannels()).thenReturn(asList(koreChannel3, koreChannel4));
        when(meter.getChannelsContainers()).thenReturn(Arrays.asList(channelsContainer1, channelsContainer2));
        when(channelsContainer2.getRange()).thenReturn(Range.atLeast(Instant.EPOCH));
        when(channelsContainer2.getRange()).thenReturn(Range.atLeast(Instant.EPOCH));

        when(validationEvaluator.isValidationEnabled(eq(meter))).thenReturn(true);
        when(validationEvaluator.isValidationEnabled(koreChannel1)).thenReturn(true);

        boolean validationActive = deviceValidation.isValidationActive(channel, NOW);
        assertThat(validationActive).isTrue();
    }

    @Test
    public void testValidateChannel() {
        doReturn(asList(meterActivation1, meterActivation2)).when(meter).getMeterActivations();
        when(meterActivation1.getChannelsContainer()).thenReturn(channelsContainer1);
        when(meterActivation2.getChannelsContainer()).thenReturn(channelsContainer2);
        when(channelsContainer1.getChannels()).thenReturn(asList(koreChannel1, koreChannel2));
        when(channelsContainer2.getChannels()).thenReturn(asList(koreChannel3, koreChannel4));
        doReturn(asList(readingType1, readingType2)).when(koreChannel2).getReadingTypes();
        doReturn(asList(readingType1, readingType2)).when(koreChannel4).getReadingTypes();
        when(koreChannel2.getMainReadingType()).thenReturn(readingType1);
        when(koreChannel4.getMainReadingType()).thenReturn(readingType1);
        when(readingType1.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType1.getMRID()).thenReturn("MRID");
        when(validationService.getLastChecked(koreChannel2)).thenReturn(Optional.of(LAST_CHECKED));
        when(validationService.getLastChecked(koreChannel4)).thenReturn(Optional.empty());
        when(validationEvaluator.getLastChecked(meter, readingType1)).thenReturn(Optional.of(LAST_CHECKED), Optional.empty());
        when(koreChannel1.getChannelsContainer()).thenReturn(channelsContainer1);
        when(koreChannel2.getChannelsContainer()).thenReturn(channelsContainer1);
        when(koreChannel3.getChannelsContainer()).thenReturn(channelsContainer2);
        when(koreChannel4.getChannelsContainer()).thenReturn(channelsContainer2);
        when(meterActivation1.getRange()).thenReturn(Range.closedOpen(Instant.EPOCH, SWITCH));
        when(meterActivation2.getRange()).thenReturn(Range.atLeast(SWITCH));

        deviceValidation.validateChannel(channel);

        verify(validationService).validate(EnumSet.of(QualityCodeSystem.MDC), channelsContainer1, readingType1);
        verify(validationService).validate(EnumSet.of(QualityCodeSystem.MDC), channelsContainer2, readingType1);
    }

    @Test
    public void testValidateChannelWithDate() {
        doReturn(asList(meterActivation1, meterActivation2)).when(meter).getMeterActivations();
        when(meterActivation1.getChannelsContainer()).thenReturn(channelsContainer1);
        when(meterActivation2.getChannelsContainer()).thenReturn(channelsContainer2);
        when(channelsContainer1.getChannels()).thenReturn(asList(koreChannel1, koreChannel2));
        when(channelsContainer2.getChannels()).thenReturn(asList(koreChannel3, koreChannel4));
        doReturn(asList(readingType1, readingType2)).when(koreChannel2).getReadingTypes();
        doReturn(asList(readingType1, readingType2)).when(koreChannel4).getReadingTypes();
        when(koreChannel2.getMainReadingType()).thenReturn(readingType1);
        when(koreChannel4.getMainReadingType()).thenReturn(readingType1);
        when(readingType1.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType1.getMRID()).thenReturn("MRID");
        when(validationService.getLastChecked(koreChannel2)).thenReturn(Optional.of(LAST_CHECKED));
        when(validationService.getLastChecked(koreChannel4)).thenReturn(Optional.empty());
        when(koreChannel1.getChannelsContainer()).thenReturn(channelsContainer1);
        when(koreChannel2.getChannelsContainer()).thenReturn(channelsContainer1);
        when(koreChannel3.getChannelsContainer()).thenReturn(channelsContainer2);
        when(koreChannel4.getChannelsContainer()).thenReturn(channelsContainer2);
        when(meterActivation1.getRange()).thenReturn(Range.closed(Instant.EPOCH, SWITCH));
        when(meterActivation2.getRange()).thenReturn(Range.atLeast(SWITCH));

        deviceValidation.validateChannel(channel);

        verify(validationService).validate(EnumSet.of(QualityCodeSystem.MDC), channelsContainer1, readingType1);
        verify(validationService).validate(EnumSet.of(QualityCodeSystem.MDC), channelsContainer2, readingType1);
    }
}
