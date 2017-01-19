/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.Priority;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskLogHandler;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Subquery;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EstimationTaskExecutorTest {

    public static final QualityCodeSystem QUALITY_CODE_SYSTEM = QualityCodeSystem.MDC;
    @Mock
    private IEstimationService estimationService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private TimeService timeService;
    @Mock
    private ThreadPrincipalService threadPrincipleService;
    @Mock
    private User estimationUser;
    @Mock
    private TaskOccurrence occurrence;
    @Mock
    private EstimationTask estimationTask;
    @Mock
    private EndDeviceGroup endDeviceGroup;
    @Mock
    private UsagePointGroup usagePointGroup;
    @Mock
    private TaskLogHandler taskLogger;
    @Mock
    private Subquery endDeviceSubQuery;
    @Mock
    private Subquery usagePointSubQuery;
    @Mock
    private Query<Meter> meterWithSuspectsQuery;
    @Mock
    private Query<ChannelsContainer> channelsContainerWithSuspectsQuery;
    @Mock
    private Meter meter1, meter2;
    @Mock
    private com.elster.jupiter.metering.MeterActivation meter2Activation;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private RelativePeriod relativePeriod;
    @Mock
    private com.elster.jupiter.transaction.TransactionContext transactionContext;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint;
    @Mock
    private UsagePointMetrologyConfiguration usagePointMetrologyConfiguration;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private MetrologyPurpose metrologyPurpose;


    private MyHandler myHandler;
    private ZonedDateTime triggerTime = ZonedDateTime.now();
    private Range<Instant> periodRange = Range.openClosed(triggerTime.minusDays(10).toInstant(), triggerTime.toInstant());

    @Before
    public void setUp() throws Exception {
        RecurrentTask task = mock(RecurrentTask.class);
        when(occurrence.getRecurrentTask()).thenReturn(task);
        when(occurrence.getTriggerTime()).thenReturn(triggerTime.toInstant());
        when(occurrence.createTaskLogHandler()).thenReturn(taskLogger);
        myHandler = new MyHandler();
        when(taskLogger.asHandler()).thenReturn(myHandler);
        doReturn(Optional.of(estimationTask)).when(estimationService).findEstimationTask(eq(task));
        when(estimationTask.getEndDeviceGroup()).thenReturn(Optional.of(endDeviceGroup));
        when(meteringService.getMeterWithReadingQualitiesQuery(any(Range.class), eq(ReadingQualityType.of(QUALITY_CODE_SYSTEM, QualityCodeIndex.SUSPECT)))).thenReturn(meterWithSuspectsQuery);
        when(meteringService.getChannelsContainerWithReadingQualitiesQuery(any(Range.class), eq(ReadingQualityType.of(QUALITY_CODE_SYSTEM, QualityCodeIndex.SUSPECT))))
                .thenReturn(channelsContainerWithSuspectsQuery);
        when(endDeviceGroup.toSubQuery(eq("id"))).thenReturn(endDeviceSubQuery);
        when(meterWithSuspectsQuery.select(any(Condition.class))).thenReturn(Arrays.asList(meter1, meter2));
        when(channelsContainerWithSuspectsQuery.select(any(Condition.class))).thenReturn(Collections.singletonList(channelsContainer));
        when(estimationService.getEstimationResolvers()).thenReturn(Collections.singletonList(new EstimationResolver() {
            @Override
            public boolean isEstimationActive(Meter meter) {
                return meter2.equals(meter);
            }

            @Override
            public List<EstimationRuleSet> resolve(ChannelsContainer channelsContainer) {
                return Collections.emptyList();
            }

            @Override
            public boolean isInUse(EstimationRuleSet estimationRuleSet) {
                return false;
            }

            @Override
            public Priority getPriority() {
                return Priority.HIGHEST;
            }
        }));
        doReturn(Optional.of(meter2Activation)).when(meter2).getMeterActivation(eq(triggerTime.toInstant()));
        when(meter2Activation.getMeter()).thenReturn(Optional.of(meter2));
        when(meter2.getMRID()).thenReturn("meter2");
        when(meter2Activation.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainer.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(estimationTask.getPeriod()).thenReturn(Optional.of(relativePeriod));
        when(estimationTask.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        when(relativePeriod.getOpenClosedInterval(triggerTime)).thenReturn(periodRange);
        when(transactionService.getContext()).thenReturn(transactionContext);
        when(channelsContainer.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(usagePointMetrologyConfiguration);
        when(usagePointMetrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));

        doAnswer(invocationOnMock -> {
            invocationOnMock.getArgumentAt(1, Runnable.class).run();
            return null;
        }).when(threadPrincipleService).runAs(eq(estimationUser), any(Runnable.class), eq(Locale.getDefault()));
        doAnswer(invocationOnMock -> {
            invocationOnMock.getArgumentAt(0, Runnable.class).run();
            return null;
        }).when(transactionService).run(any(Runnable.class));

    }


    @Test
    public void testSuccessfullRunWithEndDevice() {
        EstimationTaskExecutor executor = new EstimationTaskExecutor(estimationService, transactionService, meteringService,
                timeService, threadPrincipleService, estimationUser);
        executor.postExecute(occurrence);
        verify(transactionService, times(1)).getContext();
        verify(transactionContext, times(1)).commit();
        verify(transactionContext, times(1)).close();
        verify(estimationService, times(1)).estimate(eq(QUALITY_CODE_SYSTEM), eq(channelsContainer), eq(periodRange), any(Logger.class));
        verify(transactionService, times(1)).run(any(Runnable.class));
        verify(estimationTask, times(1)).updateLastRun(eq(triggerTime.toInstant()));
    }

    @Test
    public void testSuccessfullRunWithUsagePoint() {
        when(estimationTask.getEndDeviceGroup()).thenReturn(Optional.empty());
        when(estimationTask.getUsagePointGroup()).thenReturn(Optional.of(usagePointGroup));
        when(estimationTask.getMetrologyPurpose()).thenReturn(Optional.empty());
        when(usagePointGroup.toSubQuery()).thenReturn(usagePointSubQuery);

        EstimationTaskExecutor executor = new EstimationTaskExecutor(estimationService, transactionService, meteringService,
                timeService, threadPrincipleService, estimationUser);
        executor.postExecute(occurrence);
        verify(transactionService, times(1)).getContext();
        verify(transactionContext, times(1)).close();
        verify(transactionContext, times(1)).commit();
        verify(estimationService, times(1)).estimate(eq(QUALITY_CODE_SYSTEM), eq(channelsContainer), eq(periodRange), any(Logger.class));
        verify(transactionService, times(1)).run(any(Runnable.class));
        verify(estimationTask, times(1)).updateLastRun(eq(triggerTime.toInstant()));
    }

    @Test
    public void testUnSuccessfullRunWithEndDevice() {
        when(estimationService.estimate(eq(QUALITY_CODE_SYSTEM), eq(channelsContainer), eq(periodRange), any(Logger.class)))
                .thenThrow(new NullPointerException());
        EstimationTaskExecutor executor = new EstimationTaskExecutor(estimationService, transactionService, meteringService,
                timeService, threadPrincipleService, estimationUser);
        executor.postExecute(occurrence);
        verify(transactionService, times(1)).getContext();
        verify(estimationService, times(1)).estimate(eq(QUALITY_CODE_SYSTEM), eq(channelsContainer), eq(periodRange), any(Logger.class));
        verify(transactionContext, times(0)).commit();
        verify(transactionContext, times(1)).close();
        verify(transactionService, times(2)).run(any(Runnable.class));
        verify(estimationTask, times(1)).updateLastRun(eq(triggerTime.toInstant()));
        assertTrue(myHandler.records.stream().anyMatch(record -> Level.WARNING.equals(record.getLevel()) && record.getThrown() instanceof NullPointerException));

    }

    @Test
    public void testEstimationTaskWithPurpose() {
        when(estimationTask.getEndDeviceGroup()).thenReturn(Optional.empty());
        when(estimationTask.getUsagePointGroup()).thenReturn(Optional.of(usagePointGroup));
        when(usagePointGroup.toSubQuery()).thenReturn(usagePointSubQuery);
        when(estimationTask.getMetrologyPurpose()).thenReturn(Optional.of(metrologyPurpose));
        when(metrologyContract.getMetrologyPurpose()).thenReturn(metrologyPurpose);
        EstimationTaskExecutor executor = new EstimationTaskExecutor(estimationService, transactionService, meteringService,
                timeService, threadPrincipleService, estimationUser);
        executor.postExecute(occurrence);

        verify(transactionService, times(1)).getContext();
        verify(estimationService, times(1)).estimate(eq(QUALITY_CODE_SYSTEM), eq(channelsContainer), eq(periodRange), any(Logger.class));
        verify(transactionContext, times(1)).commit();
        verify(transactionContext, times(1)).close();
        verify(transactionService, times(1)).run(any(Runnable.class));
        verify(estimationTask, times(1)).updateLastRun(eq(triggerTime.toInstant()));
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
