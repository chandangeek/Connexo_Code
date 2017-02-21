/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.ServerComTaskExecution;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.scheduling.NextExecutionSpecs;

import org.joda.time.DateTime;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Optional;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerComTaskStatusTest {

    public static final int DEFAULT_MAX_NUMBER_OF_TRIES = 3;

    private void markComTaskAsBusy(ServerComTaskExecution comTaskExecution) {
        when(comTaskExecution.isExecuting()).thenReturn(true);
    }

    private void markComTaskAsNotBusy(ServerComTaskExecution comTaskExecution) {
        when(comTaskExecution.isExecuting()).thenReturn(false);
    }

    @Test
    public void busyTest() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        markComTaskAsBusy(scheduledComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, Instant.now())).isEqualTo(TaskStatus.Busy);
    }

    @Test
    public void busyOverrulesPendingTest() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markComTaskAsPending(scheduledComTask);
        markComTaskAsBusy(scheduledComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, now.instant())).isEqualTo(TaskStatus.Busy);
        assertThat(ServerComTaskStatus.Busy.appliesTo(scheduledComTask, now.instant())).isEqualTo(Boolean.TRUE);
        assertThat(ServerComTaskStatus.Pending.appliesTo(scheduledComTask, now.instant())).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void busyOverrulesNeverCompletedTest() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markComTaskAsNeverCompleted(scheduledComTask);
        markComTaskAsBusy(scheduledComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, now.instant())).isEqualTo(TaskStatus.Busy);
    }

    private void markComTaskAsNeverCompleted(ServerComTaskExecution scheduledComTask) {
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());

        when(scheduledComTask.getLastSuccessfulCompletionTimestamp()).thenReturn(null);
        when(scheduledComTask.getExecutingComPort()).thenReturn(null);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(0);
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
    }

    @Test
    public void testBusyOverrulesRetrying() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        markComTaskAsRetrying(scheduledComTask);
        markComTaskAsBusy(scheduledComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, Instant.now())).isEqualTo(TaskStatus.Busy);
    }

    @Test
    public void busyOverrulesFailedTest() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markComTaskAsFailed(scheduledComTask);
        markComTaskAsBusy(scheduledComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, now.instant())).isEqualTo(TaskStatus.Busy);
    }

    private void markComTaskAsFailed(ServerComTaskExecution scheduledComTask) {
        Clock lastSuccessfulComplete = Clock.fixed(new DateTime(2011, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());

        when(scheduledComTask.getLastSuccessfulCompletionTimestamp()).thenReturn(lastSuccessfulComplete.instant());
        when(scheduledComTask.getExecutingComPort()).thenReturn(null);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(0);
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
    }

    @Test
    public void busyOverrulesWaitingTest() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markComTaskAsWaiting(scheduledComTask);
        markComTaskAsBusy(scheduledComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, now.instant())).isEqualTo(TaskStatus.Busy);
    }

    private void markComTaskAsWaiting(ServerComTaskExecution scheduledComTask) {
        Clock lastSuccessfulComplete = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        when(scheduledComTask.getLastSuccessfulCompletionTimestamp()).thenReturn(lastSuccessfulComplete.instant());
        when(scheduledComTask.getExecutingComPort()).thenReturn(null);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(0);
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
    }

    private void markComTaskAsRetrying(ServerComTaskExecution scheduledComTask) {
        when(scheduledComTask.getMaxNumberOfTries()).thenReturn(DEFAULT_MAX_NUMBER_OF_TRIES);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(DEFAULT_MAX_NUMBER_OF_TRIES - 1);
    }

    private void markComTaskAsOnHold(ServerComTaskExecution comTaskExecution) {
        when(comTaskExecution.isOnHold()).thenReturn(true);
        when(comTaskExecution.isExecuting()).thenReturn(false);
    }

    @Test
    public void testOnHoldForAdhocComTaskExecution() {
        ServerComTaskExecution adHocComTask = mock(ServerComTaskExecution.class);
        markComTaskAsOnHold(adHocComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(adHocComTask, Instant.now())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void onHoldOverrulesBusyTest() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        markComTaskAsBusy(scheduledComTask);
        markComTaskAsOnHold(scheduledComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, Instant.now())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void onHoldOverrulesPendingTest() {
        ServerComTaskExecution adHocComTask = mock(ServerComTaskExecution.class);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markComTaskAsPending(adHocComTask);
        markComTaskAsOnHold(adHocComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(adHocComTask, now.instant())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void onHoldOverrulesNeverCompletedTest() {
        ServerComTaskExecution adHocComTask = mock(ServerComTaskExecution.class);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markComTaskAsNeverCompleted(adHocComTask);
        markComTaskAsOnHold(adHocComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(adHocComTask, now.instant())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void onHoldOverrulesRetryingTest() {
        ServerComTaskExecution adHocComTask = mock(ServerComTaskExecution.class);
        markComTaskAsRetrying(adHocComTask);
        markComTaskAsOnHold(adHocComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(adHocComTask, Instant.now())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void onHoldOverrulesFailedTest() {
        ServerComTaskExecution adHocComTask = mock(ServerComTaskExecution.class);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markComTaskAsFailed(adHocComTask);
        markComTaskAsOnHold(adHocComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(adHocComTask, now.instant())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void onHoldOverrulesWaitingTest() {
        ServerComTaskExecution adHocComTask = mock(ServerComTaskExecution.class);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markComTaskAsWaiting(adHocComTask);
        markComTaskAsOnHold(adHocComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(adHocComTask, now.instant())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void testOnHoldForScheduledComTaskThatHasNeverCompleted() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getLastSuccessfulCompletionTimestamp()).thenReturn(null);
        when(scheduledComTask.isOnHold()).thenReturn(true);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, Instant.now())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void testOnHoldForScheduledComTaskWithNextExecutionTimeInFuture() {
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
        when(scheduledComTask.isOnHold()).thenReturn(true);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, now.instant())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void testOnHoldForScheduledComTaskWithNextExecutionTimeInPast() {
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());

        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
        when(scheduledComTask.isOnHold()).thenReturn(true);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, now.instant())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void testOnHoldForScheduledComTaskWithMaximumRetryCount() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getMaxNumberOfTries()).thenReturn(DEFAULT_MAX_NUMBER_OF_TRIES);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(DEFAULT_MAX_NUMBER_OF_TRIES);
        when(scheduledComTask.isOnHold()).thenReturn(true);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, Instant.now())).isEqualTo(TaskStatus.OnHold);
    }

    private void markComTaskAsPending(ServerComTaskExecution scheduledComTask) {
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());

        when(scheduledComTask.getCurrentTryCount()).thenReturn(1);
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
    }

    @Test
    public void testPending() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markComTaskAsPending(scheduledComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, now.instant())).isEqualTo(TaskStatus.Pending);
    }

    @Test
    public void pendingOverRulesNeverCompletedTest() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        markComTaskAsNeverCompleted(scheduledComTask);
        markComTaskAsPending(scheduledComTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, now.instant())).isEqualTo(TaskStatus.Pending);
    }

    @Test
    public void pendingOverrulesRetryingTest() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        markComTaskAsRetrying(scheduledComTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markComTaskAsPending(scheduledComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, now.instant())).isEqualTo(TaskStatus.Pending);
    }

    @Test
    public void pendingOverrulesFailedTest() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markComTaskAsFailed(scheduledComTask);
        markComTaskAsPending(scheduledComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, now.instant())).isEqualTo(TaskStatus.Pending);
    }

    @Test
    public void pendingOverrulesWaitingTest() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markComTaskAsWaiting(scheduledComTask);
        markComTaskAsPending(scheduledComTask);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, now.instant())).isEqualTo(TaskStatus.Pending);
    }

    @Test
    public void testNeverCompletedForScheduledComTaskThatHasNeverCompleted() {
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock lastExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());

        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getLastSuccessfulCompletionTimestamp()).thenReturn(null);
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
        when(scheduledComTask.getLastExecutionStartTimestamp()).thenReturn(lastExecutionTimestamp.instant());
        when(scheduledComTask.getPlannedNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
        when(scheduledComTask.getCurrentTryCount()).thenReturn(1);
        when(scheduledComTask.getNextExecutionSpecs()).thenReturn(Optional.of(mock(NextExecutionSpecs.class)));
        when(scheduledComTask.isAdHoc()).thenReturn(false);
        assertThat(ServerComTaskStatus.NeverCompleted.appliesTo(scheduledComTask, now.instant())).isTrue();
    }


    @Test
    public void testNeverCompletedForAdhocComTaskThatHasNeverCompleted() {
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock lastExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getLastSuccessfulCompletionTimestamp()).thenReturn(null);
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(null);
        when(scheduledComTask.getLastExecutionStartTimestamp()).thenReturn(lastExecutionTimestamp.instant());
        when(scheduledComTask.getPlannedNextExecutionTimestamp()).thenReturn(null);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(1);
        when(scheduledComTask.isAdHoc()).thenReturn(true);
        assertThat(ServerComTaskStatus.NeverCompleted.appliesTo(scheduledComTask, now.instant())).isTrue();
    }

    @Test
    public void testNeverCompletedForScheduledComTaskThatAlreadyCompletedSuccessfully() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getLastSuccessfulCompletionTimestamp()).thenReturn(Instant.now());
        assertThat(ServerComTaskStatus.NeverCompleted.appliesTo(scheduledComTask, Instant.now())).isFalse();
    }

    @Test
    public void testNeverCompletedOverrulesFailedForScheduledComTaskWithMaximumRetryCount() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getLastSuccessfulCompletionTimestamp()).thenReturn(null);
        when(scheduledComTask.getMaxNumberOfTries()).thenReturn(DEFAULT_MAX_NUMBER_OF_TRIES);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(DEFAULT_MAX_NUMBER_OF_TRIES);
        assertThat(ServerComTaskStatus.getApplicableStatusFor(scheduledComTask, Instant.now())).isEqualTo(TaskStatus.NeverCompleted);
    }

    @Test
    public void testWaitingForScheduledComTaskWithoutNextExecutionTimestamp() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(null);
        assertThat(ServerComTaskStatus.Waiting.appliesTo(scheduledComTask, Instant.now())).isFalse();
    }

    @Test
    public void testWaitingForScheduledComTaskWithNextExecutionTimeInFuture() {
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getLastSuccessfulCompletionTimestamp()).thenReturn(now.instant());
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
        when(scheduledComTask.getPlannedNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
        when(scheduledComTask.getCurrentTryCount()).thenReturn(1);
        when(scheduledComTask.getNextExecutionSpecs()).thenReturn(Optional.of(mock(NextExecutionSpecs.class)));
        when(scheduledComTask.isAdHoc()).thenReturn(false);
        assertThat(ServerComTaskStatus.Waiting.appliesTo(scheduledComTask, now.instant())).isTrue();
    }

    @Test
    public void testWaitingForAdhocComTaskWithNextExecutionTimeInFuture() {
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());

        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getLastSuccessfulCompletionTimestamp()).thenReturn(now.instant());
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(null);
        when(scheduledComTask.getPlannedNextExecutionTimestamp()).thenReturn(null);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(1);
        when(scheduledComTask.isAdHoc()).thenReturn(true);
        assertThat(ServerComTaskStatus.Waiting.appliesTo(scheduledComTask, now.instant())).isTrue();
    }

    @Test
    public void testWaitingForScheduledComTaskWithNextExecutionTimeInPast() {
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
        assertThat(ServerComTaskStatus.Waiting.appliesTo(scheduledComTask, now.instant())).isFalse();
    }

    @Test
    public void testRetryingOverrulesWaitingForScheduledComTaskWithNextExecutionTimeInFuture() {
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(2);
        when(scheduledComTask.getMaxNumberOfTries()).thenReturn(3);
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
        assertThat(ServerComTaskStatus.Retrying.appliesTo(scheduledComTask, now.instant())).isTrue();
        assertThat(ServerComTaskStatus.Failed.appliesTo(scheduledComTask, now.instant())).isFalse();
        assertThat(ServerComTaskStatus.Waiting.appliesTo(scheduledComTask, now.instant())).isFalse();
    }

    @Test
    public void testFailedOverrulesWaitingForScheduledComTaskWithNextExecutionTimeInFuture() {
        Clock now = Clock.fixed(new DateTime(2012, 1, 5, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 6, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock lastSuccessfulEnd = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock lastStart = Clock.fixed(new DateTime(2012, 1, 4, 0, 0).toDate().toInstant(), ZoneId.systemDefault());

        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(1);
        when(scheduledComTask.getMaxNumberOfTries()).thenReturn(3);
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
        when(scheduledComTask.getLastSuccessfulCompletionTimestamp()).thenReturn(lastSuccessfulEnd.instant());
        when(scheduledComTask.getLastExecutionStartTimestamp()).thenReturn(lastStart.instant());
        when(scheduledComTask.isLastExecutionFailed()).thenReturn(true);
        assertThat(ServerComTaskStatus.Retrying.appliesTo(scheduledComTask, now.instant())).isFalse();
        assertThat(ServerComTaskStatus.Failed.appliesTo(scheduledComTask, now.instant())).isTrue();
        assertThat(ServerComTaskStatus.Waiting.appliesTo(scheduledComTask, now.instant())).isFalse();
    }

    @Test
    public void testRetrying() {
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
        markComTaskAsRetrying(scheduledComTask);
        assertThat(ServerComTaskStatus.Retrying.appliesTo(scheduledComTask, Instant.now())).isTrue();
    }

    @Test
    public void testRetryingForFailedTask() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getMaxNumberOfTries()).thenReturn(DEFAULT_MAX_NUMBER_OF_TRIES);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(DEFAULT_MAX_NUMBER_OF_TRIES);
        assertThat(ServerComTaskStatus.Retrying.appliesTo(scheduledComTask, Instant.now())).isFalse();
    }

    @Test
    public void testRetryingForWaitingOrPendingTask() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getMaxNumberOfTries()).thenReturn(DEFAULT_MAX_NUMBER_OF_TRIES);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(0);
        assertThat(ServerComTaskStatus.Retrying.appliesTo(scheduledComTask, Instant.now())).isFalse();
    }

    @Test
    public void testFailed() {
        Clock lastSuccessfulCompletionTimestamp = Clock.fixed(new DateTime(2011, Calendar.DECEMBER, 30, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock lastExecutionStart = Clock.fixed(new DateTime(2011, Calendar.DECEMBER, 30, 10, 0, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(1);
        when(scheduledComTask.getNextExecutionSpecs()).thenReturn(Optional.empty());
        when(scheduledComTask.getLastSuccessfulCompletionTimestamp()).thenReturn(lastSuccessfulCompletionTimestamp.instant());
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
        when(scheduledComTask.isLastExecutionFailed()).thenReturn(true);
        when(scheduledComTask.getLastExecutionStartTimestamp()).thenReturn(lastExecutionStart.instant());
        assertThat(ServerComTaskStatus.Failed.appliesTo(scheduledComTask, now.instant())).isTrue();
    }

    @Test
    public void testFailedForScheduledComTaskThatIsRetrying() {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        markComTaskAsRetrying(scheduledComTask);
        when(scheduledComTask.isLastExecutionFailed()).thenReturn(true);
        assertThat(ServerComTaskStatus.Failed.appliesTo(scheduledComTask, Instant.now())).isFalse();
    }

    @Test
    public void testOnHoldMapsToOnHold() {
        assertThat(ServerComTaskStatus.OnHold.getPublicStatus()).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void testNeverCompletedMapsToNeverCompleted() {
        assertThat(ServerComTaskStatus.NeverCompleted.getPublicStatus()).isEqualTo(TaskStatus.NeverCompleted);
    }

    @Test
    public void testWaitingMapsToWaiting() {
        assertThat(ServerComTaskStatus.Waiting.getPublicStatus()).isEqualTo(TaskStatus.Waiting);
    }

    @Test
    public void testPendingMapsToPending() {
        assertThat(ServerComTaskStatus.Pending.getPublicStatus()).isEqualTo(TaskStatus.Pending);
    }

    @Test
    public void testBusyMapsToBusy() {
        assertThat(ServerComTaskStatus.Busy.getPublicStatus()).isEqualTo(TaskStatus.Busy);
    }

    @Test
    public void testRetryingMapsToRetrying() {
        assertThat(ServerComTaskStatus.Retrying.getPublicStatus()).isEqualTo(TaskStatus.Retrying);
    }

    @Test
    public void testFailedMapsToFailed() {
        assertThat(ServerComTaskStatus.Failed.getPublicStatus()).isEqualTo(TaskStatus.Failed);
    }

    @Test
    public void testNeverCompletedForAdhocComTask() {
        Clock lastExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());

        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.isExecuting()).thenReturn(false);
        when(scheduledComTask.getExecutingComPort()).thenReturn(null);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(1);
        when(scheduledComTask.getLastSuccessfulCompletionTimestamp()).thenReturn(null);
        when(scheduledComTask.getLastExecutionStartTimestamp()).thenReturn(lastExecutionTimestamp.instant());
        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(null);
        when(scheduledComTask.getPlannedNextExecutionTimestamp()).thenReturn(null);
        when(scheduledComTask.getNextExecutionSpecs()).thenReturn(Optional.empty());
        when(scheduledComTask.isAdHoc()).thenReturn(true);
        assertThat(ServerComTaskStatus.NeverCompleted.appliesTo(scheduledComTask, Instant.now())).isTrue();
    }

    @Test
    public void testNeverCompletedForScheduledComTask() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());

        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.isExecuting()).thenReturn(false);
        when(scheduledComTask.getExecutingComPort()).thenReturn(null);
        when(scheduledComTask.getCurrentTryCount()).thenReturn(1);
        when(scheduledComTask.getLastSuccessfulCompletionTimestamp()).thenReturn(null);
        when(scheduledComTask.getLastExecutionStartTimestamp()).thenReturn(frozenClock.instant());

        Clock nowDate = Clock.fixed(new DateTime(2013, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock futureDate = Clock.fixed(new DateTime(2014, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());

        when(scheduledComTask.getNextExecutionTimestamp()).thenReturn(futureDate.instant());
        when(scheduledComTask.getPlannedNextExecutionTimestamp()).thenReturn(null);
        when(scheduledComTask.getNextExecutionSpecs()).thenReturn(Optional.of(mock(NextExecutionSpecs.class)));
        when(scheduledComTask.isAdHoc()).thenReturn(false);
        assertThat(ServerComTaskStatus.NeverCompleted.appliesTo(scheduledComTask, nowDate.instant())).isTrue();
    }

}