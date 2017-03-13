/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.metering.impl.ChannelImpl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    private CimChannel cimChannel1, cimChannel2, cimChannel3;
    @Mock
    private ChannelsContainer channelsContainer1, channelsContainer2;
    @Mock
    private ValidationServiceImpl validationService;
    @Mock
    private LocalEvent localEvent;
    private Map<Channel, Range<Instant>> scope1, scope2, dependentScope1, dependentScope2, allDependentScope;

    @Before
    public void setUp() {
        handler = new ValidationEventHandler();
        handler.setValidationService(validationService);

        when(channel1.getChannelsContainer()).thenReturn(channelsContainer1);
        when(channel2.getChannelsContainer()).thenReturn(channelsContainer1);
        when(channel3.getChannelsContainer()).thenReturn(channelsContainer2);
        when(cimChannel1.getChannelContainer()).thenReturn(channelsContainer1);
        when(cimChannel2.getChannelContainer()).thenReturn(channelsContainer1);
        when(cimChannel3.getChannelContainer()).thenReturn(channelsContainer2);
        doReturn(channel1).when(cimChannel1).getChannel();
        doReturn(channel2).when(cimChannel2).getChannel();
        doReturn(channel3).when(cimChannel3).getChannel();

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
    }

    @Test
    public void testValidateOnReadingsCreatedNoDependentScope() {
        when(readingStorer.getStorerProcess()).thenReturn(StorerProcess.DEFAULT);
        when(channelsContainer1.findDependentChannelScope(scope1)).thenReturn(Collections.emptyMap());
        when(channelsContainer2.findDependentChannelScope(scope2)).thenReturn(Collections.emptyMap());

        handler.handle(localEvent);

        verify(validationService).validate(channelsContainer1, scope1);
        verify(validationService).validate(channelsContainer2, scope2);
        verify(validationService).validate(Collections.emptyMap());
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testValidateOnReadingsCreatedWithDependentScope() {
        when(readingStorer.getStorerProcess()).thenReturn(StorerProcess.DEFAULT);

        handler.handle(localEvent);

        verify(validationService).validate(channelsContainer1, scope1);
        verify(validationService).validate(channelsContainer2, scope2);
        verify(validationService).validate(allDependentScope);
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testOnEventDoesValidateOnEdit() {
        when(readingStorer.getStorerProcess()).thenReturn(StorerProcess.EDIT);

        handler.handle(localEvent);

        verify(validationService).validate(channelsContainer1, scope1);
        verify(validationService).validate(channelsContainer2, scope2);
        verify(validationService).validate(allDependentScope);
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testOnEventDoesValidateOnRemove() {
        when(localEvent.getSource()).thenReturn(new ChannelImpl.ReadingsDeletedEventImpl(channel3,
                ImmutableSet.of(date4, date5, date6)));
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.READINGS_DELETED.topic());

        Map<Channel, Range<Instant>> scope = ImmutableMap.of(channel3, Range.closed(date4, date6));
        when(channelsContainer2.findDependentChannelScope(scope)).thenReturn(dependentScope2);

        handler.handle(localEvent);

        verify(validationService).validate(channelsContainer2, scope);
        verify(validationService).validate(dependentScope2);
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testOnEventDoesValidateOnlyDependentScopeOnEstimate() {
        when(readingStorer.getStorerProcess()).thenReturn(StorerProcess.ESTIMATION);

        handler.handle(localEvent);

        verify(validationService).validate(allDependentScope);
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testOnEventDoesNotValidateOnConfirm() {
        when(readingStorer.getStorerProcess()).thenReturn(StorerProcess.CONFIRM);

        handler.handle(localEvent);

        verifyZeroInteractions(validationService);
    }
}
