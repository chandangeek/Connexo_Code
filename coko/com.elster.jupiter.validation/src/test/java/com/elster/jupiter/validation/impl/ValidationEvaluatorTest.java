/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.validation.ValidationContext;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationEvaluatorTest {
    @Mock
    ValidationServiceImpl validationService;
    @Mock
    ChannelsContainer channelsContainer;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    Channel channel1, channel2, channel3;

    private final static Set<QualityCodeSystem> SYSTEMS = Collections.singleton(QualityCodeSystem.OTHER);


    @Before
    public void setUp() {
        when(channelsContainer.getChannels()).thenReturn(Arrays.asList(channel1, channel2, channel3));
        when(channelsContainer.getRange()).thenReturn(Range.all());
    }

    @Test
    public void isAllDataValidTestWithNoSuspects() {
        //No suspects
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, channelsContainer)).isFalse();
    }

    @Test
    public void isAllDataValidTestWithSuspectsForEachChannel() {
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(true);
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(true);
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(true);

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, channelsContainer)).isTrue();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel1() {
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(true);
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, channelsContainer)).isTrue();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel2() {
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(true);
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, channelsContainer)).isTrue();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel3() {
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(true);

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, channelsContainer)).isTrue();
    }

    @Test
    public void testIsAllDataValidatedFalse() {
        Instant now = Instant.now();
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getId()).thenReturn(1L);
        Channel channel = mock(Channel.class);
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(channel.getLastDateTime()).thenReturn(now);
        ChannelValidation channelValidation = mock(ChannelValidation.class);
        when(channelValidation.hasActiveRules()).thenReturn(true);
        when(channelValidation.getLastChecked()).thenReturn(now.minus(1, ChronoUnit.DAYS));
        when(channelValidation.getChannel()).thenReturn(channel);
        ChannelsContainerValidation channelsContainerValidation = mock(ChannelsContainerValidation.class);
        when(channelsContainerValidation.getChannelValidation(channel)).thenReturn(Optional.of(channelValidation));
        when(channelsContainerValidation.getLastRun()).thenReturn(now);
        when(validationService.getPersistedChannelsContainerValidations(channelsContainer)).thenReturn(Collections.singletonList(channelsContainerValidation));
        assertThat(new ValidationEvaluatorImpl(validationService).isAllDataValidated(Collections.singletonList(channel))).isFalse();
    }

    @Test
    public void testIsAllDataValidatedTrue() {
        Instant now = Instant.now();
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getId()).thenReturn(1L);
        Channel channel = mock(Channel.class);
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(channel.getLastDateTime()).thenReturn(now);
        ChannelValidation channelValidation = mock(ChannelValidation.class);
        when(channelValidation.hasActiveRules()).thenReturn(true);
        when(channelValidation.getLastChecked()).thenReturn(now);
        when(channelValidation.getChannel()).thenReturn(channel);
        ChannelsContainerValidation channelsContainerValidation = mock(ChannelsContainerValidation.class);
        when(channelsContainerValidation.getChannelValidation(channel)).thenReturn(Optional.of(channelValidation));
        when(channelsContainerValidation.getLastRun()).thenReturn(now);
        when(validationService.getPersistedChannelsContainerValidations(channelsContainer)).thenReturn(Collections.singletonList(channelsContainerValidation));
        assertThat(new ValidationEvaluatorImpl(validationService).isAllDataValidated(Collections.singletonList(channel))).isTrue();
    }

    @Test
    public void testIsAllDataValidatedNoValidation() {
        Instant now = Instant.now();
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getId()).thenReturn(1L);
        Channel channel = mock(Channel.class);
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(channel.getLastDateTime()).thenReturn(now);
        when(validationService.getPersistedChannelsContainerValidations(channelsContainer)).thenReturn(Collections.emptyList());
        when(validationService.getUpdatedChannelsContainerValidations(any(ValidationContext.class))).thenReturn(Collections.emptyList());
        assertThat(new ValidationEvaluatorImpl(validationService).isAllDataValidated(Collections.singletonList(channel))).isFalse();
    }
}
