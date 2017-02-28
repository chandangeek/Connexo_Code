/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationScope;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationEventHandlerTest {

    private static final Instant date1 = ZonedDateTime.of(1983, 5, 31, 14, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant date2 = ZonedDateTime.of(1983, 5, 31, 15, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant date3 = ZonedDateTime.of(1983, 5, 31, 16, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant date4 = ZonedDateTime.of(1983, 5, 31, 17, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant date5 = ZonedDateTime.of(1983, 5, 31, 18, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant date6 = ZonedDateTime.of(1983, 5, 31, 19, 0, 0, 0, ZoneId.systemDefault()).toInstant();

    private ValidationEventHandler handler;

    @Mock
    private ReadingStorer readingStorer;
    @Mock
    private EventType eventType;
    @Mock
    private Channel channel1, channel2, channel3, channelMdm1, channelMdm2;
    @Mock
    private ReadingType readingTypeMdm1, readingTypeMdm2;
    @Mock
    private CimChannel cimChannel1, cimChannel2, cimChannel3;
    @Mock
    private ChannelsContainer channelsContainer1, channelsContainer2, channelsContainerMdm;
    @Mock
    private ValidationService validationService;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private ValidationRuleSet ruleSet;
    @Mock
    private ValidationRule rule;
    private Map<Channel, Range<Instant>> scope1, scope2, dependentScope1, dependentScope2, allDependentScope;

    @Before
    public void setUp() {
        handler = new ValidationEventHandler();
        handler.setValidationService(validationService);

        when(channel1.getChannelsContainer()).thenReturn(channelsContainer1);
        when(channel2.getChannelsContainer()).thenReturn(channelsContainer1);
        when(channel3.getChannelsContainer()).thenReturn(channelsContainer2);
        when(channelMdm1.getChannelsContainer()).thenReturn(channelsContainerMdm);
        when(channelMdm2.getChannelsContainer()).thenReturn(channelsContainerMdm);
        when(cimChannel1.getChannelContainer()).thenReturn(channelsContainer1);
        when(cimChannel2.getChannelContainer()).thenReturn(channelsContainer1);
        when(cimChannel3.getChannelContainer()).thenReturn(channelsContainer2);
        doReturn(channel1).when(cimChannel1).getChannel();
        doReturn(channel2).when(cimChannel2).getChannel();
        doReturn(channel3).when(cimChannel3).getChannel();

        when(channelMdm1.getMainReadingType()).thenReturn(readingTypeMdm1);
        when(channelMdm2.getMainReadingType()).thenReturn(readingTypeMdm2);

        when(readingStorer.getStorerProcess()).thenReturn(StorerProcess.CONFIRM);
        when(readingStorer.getScope()).thenReturn(ImmutableMap.of(
                cimChannel1, Range.openClosed(date1, date3),
                cimChannel2, Range.openClosed(date3, date5),
                cimChannel3, Range.openClosed(date4, date6)
        ));

        scope1 = ImmutableMap.of(channel1, Range.openClosed(date1, date3),
                channel2, Range.openClosed(date3, date5));
        scope2 = ImmutableMap.of(channel3, Range.openClosed(date4, date6));
        dependentScope1 = ImmutableMap.of(channelMdm1, Range.openClosed(date2, date3),
                channelMdm2, Range.openClosed(date4, date5));
        dependentScope2 = ImmutableMap.of(channelMdm2, Range.openClosed(date3, date4));
        allDependentScope = ImmutableMap.of(channelMdm1, Range.openClosed(date2, date3),
                channelMdm2, Range.openClosed(date3, date5));

        when(localEvent.getSource()).thenReturn(readingStorer);
        when(localEvent.getType()).thenReturn(eventType);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.READINGS_CREATED.topic());

        when(channelsContainer1.findDependentChannelScope(scope1)).thenReturn(dependentScope1);
        when(channelsContainer2.findDependentChannelScope(scope2)).thenReturn(dependentScope2);

        when(validationService.activeRuleSets(channelsContainerMdm)).thenReturn(Collections.singletonList(ruleSet));
        doReturn(Collections.singletonList(rule)).when(ruleSet).getRules();
        when(rule.isActive()).thenReturn(true);
        when(rule.getImplementation()).thenReturn(DefaultValidatorFactory.READING_QUALITIES_VALIDATOR);
        when(rule.getProps()).thenReturn(ImmutableMap.of(ReadingQualitiesValidator.READING_QUALITIES,
                ImmutableSet.of("2.5.257", "2.6.*")));
        when(rule.appliesTo(readingTypeMdm1)).thenReturn(true);
        when(rule.appliesTo(readingTypeMdm2)).thenReturn(true);
    }

    @Test
    public void testOnEventDoesValidateOnConfirmation() {
        handler.handle(localEvent);

        verify(validationService).activeRuleSets(channelsContainerMdm);
        verify(validationService).validate(channelsContainerMdm, allDependentScope);
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testOnEventDoesValidateOnValidation() {
        when(localEvent.getSource()).thenReturn(new ValidationScope() {
            @Override
            public ChannelsContainer getChannelsContainer() {
                return channelsContainer1;
            }
            @Override
            public Map<Channel, Range<Instant>> getValidationScope() {
                return scope1;
            }
        });
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.validation.EventType.VALIDATION_PERFORMED.topic());
        handler.handle(localEvent);

        verify(validationService).activeRuleSets(channelsContainerMdm);
        verify(validationService).validate(channelsContainerMdm, dependentScope1);
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testOnEventDoesValidateOnValidationReset() {
        when(localEvent.getSource()).thenReturn(new ValidationScope() {
            @Override
            public ChannelsContainer getChannelsContainer() {
                return channelsContainer2;
            }
            @Override
            public Map<Channel, Range<Instant>> getValidationScope() {
                return scope2;
            }
        });
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.validation.EventType.VALIDATION_RESET.topic());
        handler.handle(localEvent);

        verify(validationService).activeRuleSets(channelsContainerMdm);
        verify(validationService).validate(channelsContainerMdm, dependentScope2);
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testRuleIsNotApplicableForOneOfChannels() {
        when(rule.appliesTo(readingTypeMdm1)).thenReturn(false);
        Map<Channel, Range<Instant>> scope = allDependentScope.entrySet().stream()
                .filter(entry -> !entry.getKey().getMainReadingType().equals(readingTypeMdm1))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        handler.handle(localEvent);

        verify(validationService).activeRuleSets(channelsContainerMdm);
        verify(validationService).validate(channelsContainerMdm, scope);
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testRuleDoesNotCheckValidationRelatedReadingQualities() {
        when(rule.getProps()).thenReturn(ImmutableMap.of(ReadingQualitiesValidator.READING_QUALITIES,
                ImmutableSet.of("2.5.257")));

        handler.handle(localEvent);

        verify(validationService).activeRuleSets(channelsContainerMdm);
        verify(validationService).validate(channelsContainerMdm, Collections.emptyMap());
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testRuleChecksSuspect() {
        when(rule.getProps()).thenReturn(ImmutableMap.of(ReadingQualitiesValidator.READING_QUALITIES,
                ImmutableSet.of("2.5.258")));

        handler.handle(localEvent);

        verify(validationService).activeRuleSets(channelsContainerMdm);
        verify(validationService).validate(channelsContainerMdm, allDependentScope);
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testRuleChecksMissing() {
        when(rule.getProps()).thenReturn(ImmutableMap.of(ReadingQualitiesValidator.READING_QUALITIES,
                ImmutableSet.of("2.5.259")));

        handler.handle(localEvent);

        verify(validationService).activeRuleSets(channelsContainerMdm);
        verify(validationService).validate(channelsContainerMdm, allDependentScope);
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testRuleIsNotBasedOnReadingQualitiesValidator() {
        when(rule.getImplementation()).thenReturn(DefaultValidatorFactory.MISSING_VALUES_VALIDATOR);

        handler.handle(localEvent);

        verify(validationService).activeRuleSets(channelsContainerMdm);
        verify(validationService).validate(channelsContainerMdm, Collections.emptyMap());
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testRuleIsNotActive() {
        when(rule.isActive()).thenReturn(false);

        handler.handle(localEvent);

        verify(validationService).activeRuleSets(channelsContainerMdm);
        verify(validationService).validate(channelsContainerMdm, Collections.emptyMap());
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testNoActiveRuleSetsForDependentContainer() {
        when(validationService.activeRuleSets(channelsContainerMdm)).thenReturn(Collections.emptyList());

        handler.handle(localEvent);

        verify(validationService).activeRuleSets(channelsContainerMdm);
        verify(validationService).validate(channelsContainerMdm, Collections.emptyMap());
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testNoChannelsDependentOnSecondContainer() {
        when(channelsContainer2.findDependentChannelScope(anyMap())).thenReturn(Collections.emptyMap());

        handler.handle(localEvent);

        verify(validationService).activeRuleSets(channelsContainerMdm);
        verify(validationService).validate(channelsContainerMdm, dependentScope1);
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testNoDependentChannels() {
        when(channelsContainer1.findDependentChannelScope(anyMap())).thenReturn(Collections.emptyMap());
        when(channelsContainer2.findDependentChannelScope(anyMap())).thenReturn(Collections.emptyMap());

        handler.handle(localEvent);

        verifyZeroInteractions(validationService);
    }
}
