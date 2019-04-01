/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationReport;
import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskLogHandler;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EstimationTaskExecutorTest {

    @Mock
    private IEstimationService estimationService;
    @Mock
    private ValidationService validationService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private TransactionContext transactionContext;
    @Mock
    private ThreadPrincipalService threadPrincipleService;
    @Mock
    private TimeService timeService;
    @Mock
    private User estimationUser;
    @Mock
    private TaskOccurrence occurrence;
    @Mock
    private EstimationTask estimationTask;
    @Mock
    private TaskLogHandler taskLogger;
    @Mock
    private RelativePeriod relativePeriod;
    @Mock
    private EstimationResolver estimationResolver;
    @Mock
    private EventService eventService;

    private MyHandler myHandler = new MyHandler();

    private ZonedDateTime triggerTime = ZonedDateTime.now();
    private Range<Instant> estimationPeriod = Range.openClosed(triggerTime.minusDays(10).toInstant(), triggerTime.toInstant());

    private EstimationTaskExecutor executor;

    @Before
    public void setUp() {
        mockTransaction();
        when(relativePeriod.getOpenClosedInterval(triggerTime)).thenReturn(estimationPeriod);
        when(estimationService.getEstimationResolvers()).thenReturn(Collections.singletonList(estimationResolver));

        executor = new EstimationTaskExecutor(estimationService, validationService, meteringService,
                timeService, transactionService, eventService, threadPrincipleService, estimationUser);
    }

    private void mockTransaction() {
        when(transactionService.getContext()).thenReturn(transactionContext);
        doAnswer(invocationOnMock -> {
            invocationOnMock.getArgumentAt(1, Runnable.class).run();
            return null;
        }).when(threadPrincipleService).runAs(eq(estimationUser), any(Runnable.class), eq(Locale.getDefault()));
        doAnswer(invocationOnMock -> {
            invocationOnMock.getArgumentAt(0, Runnable.class).run();
            return null;
        }).when(transactionService).run(any(Runnable.class));
    }

    private EstimationTask mockEstimationTask(EndDeviceGroup endDeviceGroup) {
        EstimationTask estimationTask = mockEstimationTask();
        when(estimationTask.getEndDeviceGroup()).thenReturn(Optional.of(endDeviceGroup));
        when(estimationTask.getUsagePointGroup()).thenReturn(Optional.empty());
        return estimationTask;
    }

    private EstimationTask mockEstimationTask(UsagePointGroup usagePointGroup, MetrologyPurpose metrologyPurpose) {
        EstimationTask estimationTask = mockEstimationTask();
        when(estimationTask.getUsagePointGroup()).thenReturn(Optional.of(usagePointGroup));
        when(estimationTask.getMetrologyPurpose()).thenReturn(Optional.ofNullable(metrologyPurpose));
        when(estimationTask.getEndDeviceGroup()).thenReturn(Optional.empty());
        return estimationTask;
    }

    private EstimationTask mockEstimationTask() {
        RecurrentTask task = mock(RecurrentTask.class);
        when(occurrence.getRecurrentTask()).thenReturn(task);
        when(occurrence.getTriggerTime()).thenReturn(triggerTime.toInstant());
        when(occurrence.createTaskLogHandler()).thenReturn(taskLogger);
        when(taskLogger.asHandler()).thenReturn(myHandler);
        doReturn(Optional.of(estimationTask)).when(estimationService).findEstimationTask(task);
        when(estimationTask.getPeriod()).thenReturn(Optional.of(relativePeriod));
        return estimationTask;
    }

    private Meter mockMeter(String name, Instant meterActivationTime) {
        Meter meter = mock(Meter.class);
        when(meter.getName()).thenReturn(name);
        MeterActivation meterActivation = mock(MeterActivation.class);
        doReturn(Optional.of(meterActivation)).when(meter).getMeterActivation(meterActivationTime);
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainer.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(channelsContainer.getMeter()).thenReturn(Optional.of(meter));
        when(meter.getChannelsContainers()).thenReturn(Collections.singletonList(channelsContainer));
        return meter;
    }

    private EndDeviceGroup mockEndDeviceGroupWithSuspects(Meter... meters) {
        Subquery endDeviceSubQuery = mock(Subquery.class);
        EndDeviceGroup endDeviceGroup = mock(EndDeviceGroup.class);
        when(endDeviceGroup.toSubQuery(eq("id"))).thenReturn(endDeviceSubQuery);
        Query<Meter> meterWithSuspectsQuery = mock(Query.class);
        when(meterWithSuspectsQuery.select(any(Condition.class))).thenReturn(Arrays.asList(meters));
        when(meteringService.getMeterWithReadingQualitiesQuery(any(Range.class), eq(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT))))
                .thenReturn(meterWithSuspectsQuery);
        return endDeviceGroup;
    }

    private ChannelsContainer mockUsagePointChannelsContainer(String name) {
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getName()).thenReturn(name);
        MetrologyContractChannelsContainer channelsContainer = mock(MetrologyContractChannelsContainer.class);
        when(channelsContainer.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(channelsContainer.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        MetrologyPurpose metrologyPurpose = mock(MetrologyPurpose.class);
        when(metrologyContract.getMetrologyPurpose()).thenReturn(metrologyPurpose);
        when(metrologyPurpose.getName()).thenReturn("Billing");
        when(channelsContainer.getMetrologyContract()).thenReturn(metrologyContract);
        return channelsContainer;
    }

    private UsagePointGroup mockUsagePointGroup(ChannelsContainer... usagePointChannelContainers) {
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        Query<ChannelsContainer> usagePointWithSuspectQuery = mock(Query.class);
        when(usagePointWithSuspectQuery.select(any())).thenReturn(Arrays.asList(usagePointChannelContainers));
        when(meteringService.getChannelsContainerWithReadingQualitiesQuery(any(Range.class), eq(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT))))
                .thenReturn(usagePointWithSuspectQuery);
        return usagePointGroup;
    }

    @Test
    public void testSuccessfulRunForEndDeviceGroup() {
        Meter meter1 = mockMeter("M1", triggerTime.toInstant());
        Meter meter2 = mockMeter("M2", triggerTime.toInstant());
        EndDeviceGroup endDeviceGroup = mockEndDeviceGroupWithSuspects(meter1, meter2);
        mockEstimationTask(endDeviceGroup);
        when(estimationResolver.isEstimationActive(meter1)).thenReturn(true);

        // Business method
        executor.postExecute(occurrence);

        // Asserts
        verify(transactionService).run(any(Runnable.class));
        verify(transactionService).getContext();
        verify(transactionContext).commit();
        verify(transactionContext).close();

        ChannelsContainer estimatedChannelContainer = meter1.getChannelsContainers().get(0);
        verify(estimationService).estimate(eq(QualityCodeSystem.MDC), eq(estimatedChannelContainer), eq(estimationPeriod), any(Logger.class));
        verify(estimationTask).updateLastRun(eq(triggerTime.toInstant()));
    }

    @Test
    public void testUnsuccessfulRunForEndDeviceGroup() {
        Meter meter1 = mockMeter("M1", triggerTime.toInstant());
        Meter meter2 = mockMeter("M2", triggerTime.toInstant());
        EndDeviceGroup endDeviceGroup = mockEndDeviceGroupWithSuspects(meter1, meter2);
        when(estimationTask.getEndDeviceGroup()).thenReturn(Optional.of(endDeviceGroup));
        mockEstimationTask(endDeviceGroup);
        when(estimationResolver.isEstimationActive(meter1)).thenReturn(true);
        ChannelsContainer estimatedChannelContainer = meter1.getChannelsContainers().get(0);
        when(estimationService.estimate(eq(QualityCodeSystem.MDC), eq(estimatedChannelContainer), eq(estimationPeriod), any(Logger.class)))
                .thenThrow(new NullPointerException());

        // Business method
        executor.postExecute(occurrence);

        // Asserts
        verify(transactionService, times(2)).run(any(Runnable.class));
        verify(transactionService).getContext();
        verify(transactionContext, never()).commit();
        verify(transactionContext).close();

        verify(estimationService).estimate(eq(QualityCodeSystem.MDC), eq(estimatedChannelContainer), eq(estimationPeriod), any(Logger.class));
        verify(estimationTask).updateLastRun(eq(triggerTime.toInstant()));
        assertThat(myHandler.records.stream().anyMatch(record -> Level.WARNING.equals(record.getLevel()) && record.getThrown() instanceof NullPointerException)).isTrue();
    }

    @Test
    public void testSuccessfulRunWithUsagePoint() {
        ChannelsContainer estimatedChannelContainer = mockUsagePointChannelsContainer("UP");
        UsagePointGroup usagePointGroup = mockUsagePointGroup(estimatedChannelContainer);
        mockEstimationTask(usagePointGroup, null);

        // Business method
        executor.postExecute(occurrence);

        // Asserts
        verify(transactionService).run(any(Runnable.class));
        verify(transactionService).getContext();
        verify(transactionContext).commit();
        verify(transactionContext).close();

        verify(estimationService).estimate(eq(QualityCodeSystem.MDM), eq(estimatedChannelContainer), eq(estimationPeriod), any(Logger.class));
        verify(estimationTask).updateLastRun(eq(triggerTime.toInstant()));
    }

    @Test
    public void testEstimationTaskWithPurpose() {
        ChannelsContainer estimatedChannelContainer = mockUsagePointChannelsContainer("UP");
        UsagePointGroup usagePointGroup = mockUsagePointGroup(estimatedChannelContainer);
        MetrologyPurpose metrologyPurpose = mock(MetrologyPurpose.class);
        mockEstimationTask(usagePointGroup, metrologyPurpose);

        // Business method
        executor.postExecute(occurrence);

        // Asserts
        // Asserts
        verify(transactionService).run(any(Runnable.class));
        verify(transactionService).getContext();
        verify(transactionContext).commit();
        verify(transactionContext).close();

        verify(estimationService).estimate(eq(QualityCodeSystem.MDM), eq(estimatedChannelContainer), eq(estimationPeriod), any(Logger.class));
        verify(estimationTask).updateLastRun(eq(triggerTime.toInstant()));
    }

    @Test
    public void testRevalidateAfterEstimation() {
        ChannelsContainer estimatedChannelContainer = mockUsagePointChannelsContainer("UP");
        UsagePointGroup usagePointGroup = mockUsagePointGroup(estimatedChannelContainer);
        EstimationTask estimationTask = mockEstimationTask(usagePointGroup, null);
        when(estimationTask.shouldRevalidate()).thenReturn(true);

        // mock estimation result
        ReadingType rt1 = mockReadingType("rt1");
        ReadingType rt2 = mockReadingType("rt2");
        ReadingType rt3 = mockReadingType("rt3");
        Channel channel1 = mockChannel(estimatedChannelContainer, rt1, rt2);
        Channel channel2 = mockChannel(estimatedChannelContainer, rt3);

        EstimationReport estimationReport = mockEstimationReport(ImmutableMap.of(
                rt1, mockEstimationResult(
                        mockEstimationBlock(channel1, mockEstimatable(triggerTime.toInstant())),
                        mockEstimationBlock(channel1, mockEstimatable(triggerTime.toInstant()), mockEstimatable(triggerTime.minusDays(1).toInstant())),
                        mockEstimationBlock(channel1, mockEstimatable(triggerTime.toInstant()))),
                rt2, mockEstimationResult(
                        mockEstimationBlock(channel1, mockEstimatable(triggerTime.minusDays(2).toInstant()))),
                rt3, mockEstimationResult(
                        mockEstimationBlock(channel2, mockEstimatable(triggerTime.toInstant())))
        ));
        when(estimationService.estimate(eq(QualityCodeSystem.MDM), eq(estimatedChannelContainer), eq(estimationPeriod), any(Logger.class))).thenReturn(estimationReport);
        Instant lastChecked = this.triggerTime.toInstant();
        when(validationService.getLastChecked(channel1)).thenReturn(Optional.of(lastChecked));
        when(validationService.getLastChecked(channel2)).thenReturn(Optional.of(lastChecked));

        // Business method
        executor.postExecute(occurrence);

        // Asserts
        ArgumentCaptor<ValidationContext> validationContextArgumentCaptor = ArgumentCaptor.forClass(ValidationContext.class);
        ArgumentCaptor<Range> rangeArgumentCaptor = ArgumentCaptor.forClass(Range.class);
        verify(validationService, times(2)).validate(validationContextArgumentCaptor.capture(), rangeArgumentCaptor.capture());
        List<ValidationContext> validationContexts = validationContextArgumentCaptor.getAllValues();
        List<Range> validationRanges = rangeArgumentCaptor.getAllValues();

        Map<ReadingType, Range<Instant>> arguments = ImmutableMap.of(
                validationContexts.get(0).getReadingType().get(), (Range<Instant>) validationRanges.get(0),
                validationContexts.get(1).getReadingType().get(), (Range<Instant>) validationRanges.get(1)
        );
        assertThat(arguments).contains(
                MapEntry.entry(rt1, Range.closed(triggerTime.minusDays(2).toInstant(), lastChecked)),
                MapEntry.entry(rt3, Range.singleton(lastChecked))
        );

        List<String> logs = myHandler.records.stream().filter(record -> Level.INFO.equals(record.getLevel())).map(LogRecord::getMessage).collect(Collectors.toList());
        assertThat(logs).contains(
                "Re-validation of estimated readings on UP/Billing/rt1",
                "Re-validation of estimated readings on UP/Billing/rt3");
    }

    private EstimationReport mockEstimationReport(Map<ReadingType, EstimationResult> estimationResult) {
        EstimationReport estimationReport = mock(EstimationReport.class);
        when(estimationReport.getResults()).thenReturn(estimationResult);
        return estimationReport;
    }

    private EstimationResult mockEstimationResult(EstimationBlock... estimatedBlocks) {
        EstimationResult estimationResult = mock(EstimationResult.class);
        when(estimationResult.estimated()).thenReturn(Arrays.asList(estimatedBlocks));
        return estimationResult;
    }

    private EstimationBlock mockEstimationBlock(Channel channel, Estimatable... estimatables) {
        EstimationBlock estimationBlock = mock(EstimationBlock.class);
        when(estimationBlock.getChannel()).thenReturn(channel);
        doReturn(Arrays.asList(estimatables)).when(estimationBlock).estimatables();
        return estimationBlock;
    }

    private Estimatable mockEstimatable(Instant timestamp) {
        Estimatable estimatable = mock(Estimatable.class);
        when(estimatable.getTimestamp()).thenReturn(timestamp);
        return estimatable;
    }

    private Channel mockChannel(ChannelsContainer channelsContainer, ReadingType... readingTypes) {
        Channel channel = mock(Channel.class);
        for (ReadingType readingType : readingTypes) {
            when(channelsContainer.getChannel(readingType)).thenReturn(Optional.of(channel));
        }
        when(channelsContainer.getChannels()).thenReturn(Collections.singletonList(channel));
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(channel.getMainReadingType()).thenReturn(readingTypes[0]);
        return channel;
    }

    private ReadingType mockReadingType(String mrid) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getFullAliasName()).thenReturn(mrid);
        return readingType;
    }

    private class MyHandler extends Handler {

        private List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }
    }
}
