/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityWithTypeFetcher;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.impl.ChannelImpl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.MockitoExtension.neverWithOtherArguments;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationEventHandlerTest {

    private static final Instant DATE_1 = ZonedDateTime.of(1983, 5, 31, 14, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant DATE_2 = ZonedDateTime.of(1983, 5, 31, 15, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant DATE_3 = ZonedDateTime.of(1983, 5, 31, 16, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant DATE_4 = ZonedDateTime.of(1983, 5, 31, 17, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant DATE_5 = ZonedDateTime.of(1983, 5, 31, 18, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant DATE_6 = ZonedDateTime.of(1983, 5, 31, 19, 0, 0, 0, ZoneId.systemDefault()).toInstant();

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
    private ChannelsContainer channelsContainer1, channelsContainer2;
    @Mock
    private MetrologyContractChannelsContainer channelsContainerMdm;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private ValidationServiceImpl validationService;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private MeterActivation meterActivation1, meterActivation2;
    @Mock
    private ChannelsContainerValidation validation1, validation2;
    @Captor
    private ArgumentCaptor<Range<Instant>> intervalCaptor;
    private Map<Channel, Range<Instant>> scope1, scope2, dependentScope1, dependentScope2, allDependentScope;
    private List<BaseReadingRecord> channelMdm1EstimatedReadings, channelMdm2EstimatedReadings;

    @Before
    public void setUp() {
        handler = new ValidationEventHandler();
        handler.setValidationService(validationService);

        when(channelMdm1.getChannelsContainer()).thenReturn(channelsContainerMdm);
        when(channelMdm2.getChannelsContainer()).thenReturn(channelsContainerMdm);
        when(channelMdm1.getMainReadingType()).thenReturn(readingTypeMdm1);
        when(channelMdm2.getMainReadingType()).thenReturn(readingTypeMdm2);
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
                cimChannel1, Range.openClosed(DATE_1, DATE_3),
                cimChannel2, Range.openClosed(DATE_3, DATE_5),
                cimChannel3, Range.openClosed(DATE_4, DATE_6)
        ));
        scope1 = ImmutableMap.of(channel1, Range.openClosed(DATE_1, DATE_3), channel2, Range.openClosed(DATE_3, DATE_5));
        scope2 = ImmutableMap.of(channel3, Range.openClosed(DATE_4, DATE_6));
        dependentScope1 = ImmutableMap.of(channelMdm1, Range.openClosed(DATE_2, DATE_3), channelMdm2, Range.openClosed(DATE_4, DATE_5));
        dependentScope2 = ImmutableMap.of(channelMdm2, Range.openClosed(DATE_3, DATE_4));
        allDependentScope = ImmutableMap.of(channelMdm1, Range.openClosed(DATE_2, DATE_3), channelMdm2, Range.openClosed(DATE_3, DATE_5));

        when(localEvent.getSource()).thenReturn(readingStorer);
        when(localEvent.getType()).thenReturn(eventType);
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.READINGS_CREATED.topic());

        when(channelsContainer1.findDependentChannelScope(scope1)).thenReturn(dependentScope1);
        when(channelsContainer2.findDependentChannelScope(scope2)).thenReturn(dependentScope2);
        when(channelsContainerMdm.getMetrologyContract()).thenReturn(metrologyContract);
        when(metrologyContract.sortReadingTypesByDependency()).thenReturn(Arrays.asList(readingTypeMdm1, readingTypeMdm2));

        channelMdm1EstimatedReadings = mockEstimatedReadings(channelMdm1, DATE_1, DATE_2, DATE_3, DATE_4, DATE_5, DATE_6);
        channelMdm2EstimatedReadings = mockEstimatedReadings(channelMdm2, DATE_1, DATE_2, DATE_3, DATE_4, DATE_5, DATE_6);
    }

    private List<BaseReadingRecord> mockEstimatedReadings(Channel channel, Instant... timestamps) {
        List<BaseReadingRecord> readings = new ArrayList<>(timestamps.length);
        List<ReadingQualityRecord> readingQualities = Arrays.stream(timestamps)
                .peek(timestamp -> readings.add(mockReading(channel, timestamp)))
                .map(ValidationEventHandlerTest::mockEstimatedReadingQuality)
                .collect(Collectors.toList());
        ReadingQualityWithTypeFetcher readingQualityFetcher = Mockito.mock(ReadingQualityWithTypeFetcher.class);
        when(channel.findReadingQualities()).thenReturn(readingQualityFetcher);
        when(readingQualityFetcher.actual()).thenReturn(readingQualityFetcher);
        when(readingQualityFetcher.ofQualitySystem(QualityCodeSystem.MDM)).thenReturn(readingQualityFetcher);
        when(readingQualityFetcher.ofAnyQualityIndexInCategory(QualityCodeCategory.ESTIMATED)).thenReturn(readingQualityFetcher);
        when(readingQualityFetcher.inTimeInterval(intervalCaptor.capture())).thenReturn(readingQualityFetcher);
        when(readingQualityFetcher.stream()).thenAnswer(invocation -> {
            Range<Instant> requestedInterval = intervalCaptor.getValue();
            return readingQualities.stream()
                    .filter(quality -> requestedInterval.contains(quality.getReadingTimestamp()));
        });
        return readings;
    }

    private static BaseReadingRecord mockReading(Channel channel, Instant timestamp) {
        BaseReadingRecord reading = Mockito.mock(BaseReadingRecord.class);
        when(reading.getTimeStamp()).thenReturn(timestamp);
        when(channel.getReading(timestamp)).thenReturn(Optional.of(reading));
        return reading;
    }

    private static ReadingQualityRecord mockEstimatedReadingQuality(Instant timestamp) {
        ReadingQualityRecord readingQuality = Mockito.mock(ReadingQualityRecord.class);
        when(readingQuality.getReadingTimestamp()).thenReturn(timestamp);
        return readingQuality;
    }

    @Test
    public void testOnReadingsCreatedWithNoDependentScope() {
        when(readingStorer.getStorerProcess()).thenReturn(StorerProcess.DEFAULT);
        when(channelsContainer1.findDependentChannelScope(scope1)).thenReturn(Collections.emptyMap());
        when(channelsContainer2.findDependentChannelScope(scope2)).thenReturn(Collections.emptyMap());

        handler.handle(localEvent);

        verify(validationService).validate(channelsContainer1, scope1);
        verify(validationService).validate(channelsContainer2, scope2);
        verify(validationService).validate(Collections.emptyMap());
        verifyNoMoreInteractions(validationService);
        verify(channelMdm1, never()).removeReadings(any(QualityCodeSystem.class), any());
        verify(channelMdm2, never()).removeReadings(any(QualityCodeSystem.class), any());
    }

    @Test
    public void testOnReadingsCreatedWithDependentScope() {
        when(readingStorer.getStorerProcess()).thenReturn(StorerProcess.DEFAULT);

        handler.handle(localEvent);

        verify(validationService).validate(channelsContainer1, scope1);
        verify(validationService).validate(channelsContainer2, scope2);
        verify(validationService).validate(allDependentScope);
        verifyNoMoreInteractions(validationService);
        InOrder inOrder = Mockito.inOrder(channelMdm1, channelMdm2);
        inOrder.verify(channelMdm2).removeReadings(QualityCodeSystem.MDM,
                filterReadingsByTimestamps(channelMdm2EstimatedReadings, DATE_4, DATE_5, DATE_6));
        inOrder.verify(channelMdm1).removeReadings(QualityCodeSystem.MDM,
                filterReadingsByTimestamps(channelMdm1EstimatedReadings, DATE_3, DATE_4, DATE_5, DATE_6));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testOnReadingsCreatedWithNoMatchingEstimatedReadings() {
        when(readingStorer.getStorerProcess()).thenReturn(StorerProcess.DEFAULT);
        mockEstimatedReadings(channelMdm1, DATE_1, DATE_2);
        mockEstimatedReadings(channelMdm2, DATE_1, DATE_2, DATE_3);

        handler.handle(localEvent);

        verify(channelMdm1, neverWithOtherArguments()).removeReadings(QualityCodeSystem.MDM, Collections.emptyList());
        verify(channelMdm2, neverWithOtherArguments()).removeReadings(QualityCodeSystem.MDM, Collections.emptyList());
    }

    @Test
    public void testOnReadingsCreatedWithOneMatchingEstimatedReading() {
        when(readingStorer.getStorerProcess()).thenReturn(StorerProcess.DEFAULT);
        List<BaseReadingRecord> channelMdm1EstimatedReading = mockEstimatedReadings(channelMdm1, DATE_2.plusMillis(1));
        List<BaseReadingRecord> channelMdm2EstimatedReading = mockEstimatedReadings(channelMdm2, DATE_3.plusMillis(1));

        handler.handle(localEvent);

        verify(channelMdm1, neverWithOtherArguments()).removeReadings(QualityCodeSystem.MDM, channelMdm1EstimatedReading);
        verify(channelMdm2, neverWithOtherArguments()).removeReadings(QualityCodeSystem.MDM, channelMdm2EstimatedReading);
    }

    @Test
    public void testOnEdit() {
        when(readingStorer.getStorerProcess()).thenReturn(StorerProcess.EDIT);

        handler.handle(localEvent);

        verify(validationService).validate(allDependentScope);
        verifyNoMoreInteractions(validationService);
        InOrder inOrder = Mockito.inOrder(channelMdm1, channelMdm2);
        inOrder.verify(channelMdm2).removeReadings(QualityCodeSystem.MDM,
                filterReadingsByTimestamps(channelMdm2EstimatedReadings, DATE_4, DATE_5, DATE_6));
        inOrder.verify(channelMdm1).removeReadings(QualityCodeSystem.MDM,
                filterReadingsByTimestamps(channelMdm1EstimatedReadings, DATE_3, DATE_4, DATE_5, DATE_6));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testOnEditWithDependentScope1() {
        when(readingStorer.getStorerProcess()).thenReturn(StorerProcess.EDIT);
        when(channelsContainer2.findDependentChannelScope(scope2)).thenReturn(Collections.emptyMap());

        handler.handle(localEvent);

        verify(validationService).validate(dependentScope1);
        verifyNoMoreInteractions(validationService);
        InOrder inOrder = Mockito.inOrder(channelMdm1, channelMdm2);
        inOrder.verify(channelMdm2).removeReadings(QualityCodeSystem.MDM,
                filterReadingsByTimestamps(channelMdm2EstimatedReadings, DATE_5, DATE_6));
        inOrder.verify(channelMdm1).removeReadings(QualityCodeSystem.MDM,
                filterReadingsByTimestamps(channelMdm1EstimatedReadings, DATE_3, DATE_4, DATE_5, DATE_6));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testOnRemove() {
        when(localEvent.getSource()).thenReturn(new ChannelImpl.ReadingsDeletedEventImpl(channel3,
                ImmutableSet.of(DATE_4, DATE_5, DATE_6)));
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.READINGS_DELETED.topic());

        Map<Channel, Range<Instant>> scope = ImmutableMap.of(channel3, Range.closed(DATE_4, DATE_6));
        when(channelsContainer2.findDependentChannelScope(scope)).thenReturn(dependentScope2);

        handler.handle(localEvent);

        verify(validationService).validate(dependentScope2);
        verifyNoMoreInteractions(validationService);
        verify(channelMdm1, never()).removeReadings(any(QualityCodeSystem.class), any());
        verify(channelMdm2, neverWithOtherArguments()).removeReadings(QualityCodeSystem.MDM,
                filterReadingsByTimestamps(channelMdm2EstimatedReadings, DATE_4, DATE_5, DATE_6));
    }

    @Test
    public void testOnEstimate() {
        when(readingStorer.getStorerProcess()).thenReturn(StorerProcess.ESTIMATION);

        handler.handle(localEvent);

        verify(validationService).validate(allDependentScope);
        verifyNoMoreInteractions(validationService);
        InOrder inOrder = Mockito.inOrder(channelMdm1, channelMdm2);
        inOrder.verify(channelMdm2).removeReadings(QualityCodeSystem.MDM,
                filterReadingsByTimestamps(channelMdm2EstimatedReadings, DATE_4, DATE_5, DATE_6));
        inOrder.verify(channelMdm1).removeReadings(QualityCodeSystem.MDM,
                filterReadingsByTimestamps(channelMdm1EstimatedReadings, DATE_3, DATE_4, DATE_5, DATE_6));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testOnConfirm() {
        when(readingStorer.getStorerProcess()).thenReturn(StorerProcess.CONFIRM);

        handler.handle(localEvent);

        verifyZeroInteractions(validationService);
        verify(channelMdm1, never()).removeReadings(any(QualityCodeSystem.class), any());
        verify(channelMdm2, never()).removeReadings(any(QualityCodeSystem.class), any());
    }

    @Test
    public void testOnAdvancedMeterActivationWithShrunkOne() {
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METER_ACTIVATION_ADVANCED.topic());
        when(localEvent.getSource()).thenReturn(new com.elster.jupiter.metering.EventType.MeterActivationAdvancedEvent(meterActivation1, meterActivation2));
        Instant now = Instant.now();
        when(meterActivation2.getEnd()).thenReturn(now);
        when(meterActivation1.getStart()).thenReturn(now);
        when(meterActivation1.getChannelsContainer()).thenReturn(channelsContainer1);
        when(meterActivation2.getChannelsContainer()).thenReturn(channelsContainer2);
        when(validationService.getPersistedChannelsContainerValidations(channelsContainer1)).thenReturn(Collections.singletonList(validation1));
        when(validationService.getPersistedChannelsContainerValidations(channelsContainer2)).thenReturn(Collections.singletonList(validation2));

        handler.handle(localEvent);

        verify(validation1).updateLastChecked(now);
        verify(validation2).moveLastCheckedBefore(now.plusMillis(1));
    }

    @Test
    public void testOnAdvancedMeterActivationWithoutShrunkOne() {
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.METER_ACTIVATION_ADVANCED.topic());
        when(localEvent.getSource()).thenReturn(new com.elster.jupiter.metering.EventType.MeterActivationAdvancedEvent(meterActivation1, null));
        Instant now = Instant.now();
        when(meterActivation1.getStart()).thenReturn(now);
        when(meterActivation1.getChannelsContainer()).thenReturn(channelsContainer1);
        when(validationService.getPersistedChannelsContainerValidations(channelsContainer1)).thenReturn(Collections.singletonList(validation1));

        handler.handle(localEvent);

        verify(validation1).updateLastChecked(now);
        verifyZeroInteractions(validation2);
    }

    @Test
    public void testOnClippedChannelsContainer() {
        when(eventType.getTopic()).thenReturn(com.elster.jupiter.metering.EventType.CHANNELS_CONTAINERS_CLIPPED.topic());
        when(localEvent.getSource()).thenReturn(new com.elster.jupiter.metering.EventType.ChannelsContainersClippedEvent(Arrays.asList(channelsContainer1, channelsContainer2)));
        Instant now = Instant.now();
        when(channelsContainer1.getRange()).thenReturn(Range.closedOpen(Instant.EPOCH, now));
        when(channelsContainer2.getRange()).thenReturn(Range.closedOpen(Instant.EPOCH, Instant.EPOCH));
        when(validationService.getPersistedChannelsContainerValidations(channelsContainer1)).thenReturn(Collections.singletonList(validation1));
        when(validationService.getPersistedChannelsContainerValidations(channelsContainer2)).thenReturn(Collections.singletonList(validation2));

        handler.handle(localEvent);

        verify(validation1).moveLastCheckedBefore(now.plusMillis(1));
        verify(validation2).makeObsolete();
    }

    private static List<BaseReadingRecord> filterReadingsByTimestamps(List<BaseReadingRecord> readings, Instant... timestamps) {
        Set<Instant> set = Arrays.stream(timestamps).collect(Collectors.toSet());
        return readings.stream()
                .filter(reading -> set.contains(reading.getTimeStamp()))
                .collect(Collectors.toList());
    }
}
