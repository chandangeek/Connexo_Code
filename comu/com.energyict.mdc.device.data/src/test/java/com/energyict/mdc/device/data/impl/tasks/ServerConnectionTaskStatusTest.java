/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.config.ComPort;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServerConnectionTaskStatusTest {

    @Mock
    private ComPort comPort;
    @Mock
    private ComTaskExecution comTaskExecution;

    @Test
    public void testBusyForConnectionTaskThatIsNotExecutingYet() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(new ArrayList<>(0));
        when(outboundConnectionTask.getDevice()).thenReturn(device);
        assertThat(ServerConnectionTaskStatus.Busy.appliesTo(outboundConnectionTask, Instant.now())).isFalse();
    }

    @Test
    public void testBusyForConnectionTaskThatIsCurrentlyExecuting() throws SQLException {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsNotPaused(outboundConnectionTask);
        markConnectionTaskAsBusyPart1(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, Instant.now())).isEqualTo(TaskStatus.Busy);
    }

    @Test
    public void testBusyForConnectionTaskThatHasExecutionComTasksTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsNotPaused(outboundConnectionTask);
        markConnectionTaskAsBusyPart2(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, Instant.now())).isEqualTo(TaskStatus.Busy);
    }

    @Test
    public void testBusyOverrulesPendingTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        when(outboundConnectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(new ArrayList<>(0));
        when(outboundConnectionTask.getDevice()).thenReturn(device);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsPending(outboundConnectionTask);
        markConnectionTaskAsBusy(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.Busy);
    }

    @Test
    public void testBusyOverrulesNeverCompletedTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsNotPaused(outboundConnectionTask);
        markConnectionTaskAsNeverCompleted(outboundConnectionTask);
        markConnectionTaskAsBusy(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, Instant.now())).isEqualTo(TaskStatus.Busy);
    }

    @Test
    public void testBusyOverrulesRetryingTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsNotPaused(outboundConnectionTask);
        markConnectionTaskAsRetrying(outboundConnectionTask);
        markConnectionTaskAsBusy(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, Instant.now())).isEqualTo(TaskStatus.Busy);
    }

    @Test
    public void testBusyOverrulesFailedTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsNotPaused(outboundConnectionTask);
        markConnectionTaskAsFailed(outboundConnectionTask);
        markConnectionTaskAsBusy(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, Instant.now())).isEqualTo(TaskStatus.Busy);
    }

    @Test
    public void testBusyOverrulesWaitingTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        when(outboundConnectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(new ArrayList<>(0));
        when(outboundConnectionTask.getDevice()).thenReturn(device);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsWaiting(outboundConnectionTask);
        markConnectionTaskAsBusy(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.Busy);
    }

    private void markConnectionTaskAsBusy(ScheduledConnectionTask outboundConnectionTask) {
        markConnectionTaskAsBusyPart1(outboundConnectionTask);
        markConnectionTaskAsBusyPart2(outboundConnectionTask);
    }

    private void markConnectionTaskAsBusyPart1(ScheduledConnectionTask outboundConnectionTask) {
        when(outboundConnectionTask.isExecuting()).thenReturn(true);
    }


    private void markConnectionTaskAsBusyPart2(ScheduledConnectionTask outboundConnectionTask) {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        List<ComTaskExecution> comTaskExecutions = Arrays.<ComTaskExecution>asList(comTaskExecution);

        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(comTaskExecutions);
        when(comTaskExecution.isExecuting()).thenReturn(true);
        when(outboundConnectionTask.getDevice()).thenReturn(device);

        when(outboundConnectionTask.getScheduledComTasks()).thenReturn(comTaskExecutions);
    }

    private void markConnectionTaskAsNotBusy(ScheduledConnectionTask outboundConnectionTask) {
        when(outboundConnectionTask.isExecuting()).thenReturn(false);

        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(Collections.<ComTaskExecution>emptyList());
        when(outboundConnectionTask.getDevice()).thenReturn(device);
    }


    @Test
    public void testPausedForConnectionTaskThatIsNotPaused() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsNotBusy(outboundConnectionTask);
        markConnectionTaskAsNotPaused(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.OnHold.appliesTo(outboundConnectionTask, Instant.now())).isFalse();
    }

    private void markConnectionTaskAsNotPaused(ScheduledConnectionTask outboundConnectionTask) {
        when(outboundConnectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        when(outboundConnectionTask.getNextExecutionTimestamp()).thenReturn(Instant.now());
    }

    private void markConnectionTaskAsPaused(ScheduledConnectionTask outboundConnectionTask) {
        markConnectionTaskAsPausedPart1(outboundConnectionTask);
        markConnectionTaskAsPausedPart2(outboundConnectionTask);
    }

    private void markConnectionTaskAsPausedPart1(ScheduledConnectionTask outboundConnectionTask) {
        when(outboundConnectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
    }

    private void markConnectionTaskAsPausedPart2(ScheduledConnectionTask outboundConnectionTask) {
        when(outboundConnectionTask.getNextExecutionTimestamp()).thenReturn(null);
    }

    @Test
    public void testPausedForConnectionTaskThatIsPaused() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsNotBusy(outboundConnectionTask);
        markConnectionTaskAsPausedPart1(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, Instant.now())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void testPausedForConnectionTaskWithEmptyNextExecutionTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        when(outboundConnectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(new ArrayList<>(0));
        when(outboundConnectionTask.getDevice()).thenReturn(device);
        markConnectionTaskAsPausedPart2(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, Instant.now())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void testPausedOverrulesBusy() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsBusy(outboundConnectionTask);
        markConnectionTaskAsPaused(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, Instant.now())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void testNeverCompletedIsOverruledByPaused() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsNeverCompleted(outboundConnectionTask);
        markConnectionTaskAsPausedPart1(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, Instant.now())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void testWaitingIsOverruledByPaused() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsWaiting(outboundConnectionTask);
        markConnectionTaskAsPaused(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void testPendingIsOverruledByPaused() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsPending(outboundConnectionTask);
        markConnectionTaskAsPausedPart1(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void testRetryingIsOverruledByPausedTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsRetrying(outboundConnectionTask);
        markConnectionTaskAsPausedPart1(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, Instant.now())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void testFailedIsOverruledByPausedTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsFailed(outboundConnectionTask);
        markConnectionTaskAsPausedPart1(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, Instant.now())).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void testConnectionTaskAsPendingTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        when(outboundConnectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(new ArrayList<>(0));
        when(outboundConnectionTask.getDevice()).thenReturn(device);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsPending(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.Pending);
    }

    @Test
    public void testNeverCompletedIsOverruledByPending() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        when(outboundConnectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(new ArrayList<>(0));
        when(outboundConnectionTask.getDevice()).thenReturn(device);
        markConnectionTaskAsNeverCompleted(outboundConnectionTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsPending(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.Pending);
    }

    @Test
    public void testRetryingIsOverruledByPendingTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        when(outboundConnectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(new ArrayList<>(0));
        when(outboundConnectionTask.getDevice()).thenReturn(device);
        markConnectionTaskAsRetrying(outboundConnectionTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsPending(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.Pending);
    }

    @Test
    public void testFailedIsOverruledByPendingTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        when(outboundConnectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(new ArrayList<>(0));
        when(outboundConnectionTask.getDevice()).thenReturn(device);
        markConnectionTaskAsFailed(outboundConnectionTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsPending(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.Pending);
    }

    @Test
    public void testWaitingIsOverruledByPendingTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        when(outboundConnectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(new ArrayList<>(0));
        when(outboundConnectionTask.getDevice()).thenReturn(device);
        markConnectionTaskAsWaiting(outboundConnectionTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsPending(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.Pending);
    }

    private void markConnectionTaskAsPending(ScheduledConnectionTask outboundConnectionTask) {
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        when(outboundConnectionTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
    }

    private void markConnectionTaskAsNotPending(ScheduledConnectionTask outboundConnectionTask) {
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        when(outboundConnectionTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
    }

    private void markConnectionTaskAsNeverCompleted(ScheduledConnectionTask outboundConnectionTask) {
        when(outboundConnectionTask.getLastSuccessfulCommunicationEnd()).thenReturn(null);
        when(outboundConnectionTask.getCurrentRetryCount()).thenReturn(0);
    }

    @Test
    public void testNeverCompletedForConnectionTaskThatHasNeverCompleted() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsNotBusy(outboundConnectionTask);
        markConnectionTaskAsNotPaused(outboundConnectionTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsNotPending(outboundConnectionTask);
        markConnectionTaskAsNeverCompleted(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.NeverCompleted);
    }

    @Test
    public void testNeverCompletedForConnectionTaskThatAlreadyCompletedSuccessfully() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        when(outboundConnectionTask.getLastSuccessfulCommunicationEnd()).thenReturn(Instant.now());
        assertThat(ServerConnectionTaskStatus.NeverCompleted.appliesTo(outboundConnectionTask, Instant.now())).isFalse();
    }

    @Test
    public void testRetryingIsOverruledByNeverCompletedTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsNotBusy(outboundConnectionTask);
        markConnectionTaskAsNotPaused(outboundConnectionTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsNotPending(outboundConnectionTask);
        markConnectionTaskAsRetrying(outboundConnectionTask);
        markConnectionTaskAsNeverCompleted(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.NeverCompleted);
    }

    @Test
    public void testFailedIsOverruledByNeverCompleted() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsNotBusy(outboundConnectionTask);
        markConnectionTaskAsNotPaused(outboundConnectionTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsNotPending(outboundConnectionTask);
        markConnectionTaskAsFailed(outboundConnectionTask);
        markConnectionTaskAsNeverCompleted(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.NeverCompleted);
    }

    @Test
    public void testWaitingIsOverruledByNeverCompleted() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        when(outboundConnectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(new ArrayList<>(0));
        when(outboundConnectionTask.getDevice()).thenReturn(device);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsWaiting(outboundConnectionTask);
        markConnectionTaskAsNeverCompleted(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.NeverCompleted);
    }

    @Test
    public void testWaitingForConnectionTaskWithoutNextExecutionTimestamp() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        when(outboundConnectionTask.getNextExecutionTimestamp()).thenReturn(null);
        assertThat(ServerConnectionTaskStatus.Waiting.appliesTo(outboundConnectionTask, Instant.now())).isFalse();
    }

    @Test
    public void testWaitingForConnectionTaskWithNextExecutionTimeInFuture() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsNotPending(outboundConnectionTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsWaiting(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.Waiting.appliesTo(outboundConnectionTask, now.instant())).isTrue();
    }

    private void markConnectionTaskAsWaiting(ScheduledConnectionTask outboundConnectionTask) {
        Clock nextExecutionTimestamp = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        when(outboundConnectionTask.getNextExecutionTimestamp()).thenReturn(nextExecutionTimestamp.instant());
    }

    @Test
    public void testWaitingForConnectionTaskWithNextExecutionTimeInPast() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        Clock now = Clock.fixed(new DateTime(2012, 1, 2, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsPending(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.Waiting.appliesTo(outboundConnectionTask, now.instant())).isFalse();
    }

    private void markConnectionTaskAsNotNeverCompleted(ScheduledConnectionTask outboundConnectionTask) {
        when(outboundConnectionTask.getLastSuccessfulCommunicationEnd()).thenReturn(Instant.now());
        when(outboundConnectionTask.getCurrentRetryCount()).thenReturn(0);
    }

    @Test
    public void testRetrying() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsNotBusy(outboundConnectionTask);
        markConnectionTaskAsNotPaused(outboundConnectionTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsNotPending(outboundConnectionTask);
        markConnectionTaskAsNotNeverCompleted(outboundConnectionTask);
        markConnectionTaskAsRetrying(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.Retrying);
    }

    @Test
    public void testFailedIsOverruledByRetryingTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        markConnectionTaskAsNotBusy(outboundConnectionTask);
        markConnectionTaskAsNotPaused(outboundConnectionTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsNotPending(outboundConnectionTask);
        markConnectionTaskAsNotNeverCompleted(outboundConnectionTask);
        markConnectionTaskAsFailed(outboundConnectionTask);
        markConnectionTaskAsRetrying(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.Retrying);
    }

    @Test
    public void testWaitingIsOverruledByRetrying() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(new ArrayList<>(0));
        when(outboundConnectionTask.getDevice()).thenReturn(device);
        markConnectionTaskAsNotBusy(outboundConnectionTask);
        markConnectionTaskAsNotPaused(outboundConnectionTask);
        markConnectionTaskAsNotPending(outboundConnectionTask);
        markConnectionTaskAsNotNeverCompleted(outboundConnectionTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsWaiting(outboundConnectionTask);
        markConnectionTaskAsRetrying(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.Retrying);
    }

    private void markConnectionTaskAsRetrying(ScheduledConnectionTask outboundConnectionTask) {
        when(outboundConnectionTask.getCurrentRetryCount()).thenReturn(1);
        when(outboundConnectionTask.getMaxNumberOfTries()).thenReturn(3);
    }

    @Test
    public void testFailed() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(new ArrayList<>(0));
        when(outboundConnectionTask.getDevice()).thenReturn(device);
        markConnectionTaskAsNotPaused(outboundConnectionTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsNotPending(outboundConnectionTask);
        markConnectionTaskAsNotNeverCompleted(outboundConnectionTask);
        markConnectionTaskAsFailed(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.Failed);
    }

    @Test
    public void testWaitingIsOverruledByFailed() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(new ArrayList<>(0));
        when(outboundConnectionTask.getDevice()).thenReturn(device);
        when(outboundConnectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        markConnectionTaskAsNotNeverCompleted(outboundConnectionTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsWaiting(outboundConnectionTask);
        markConnectionTaskAsFailed(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.Failed);
    }

    private void markConnectionTaskAsFailed(ScheduledConnectionTask outboundConnectionTask) {
        when(outboundConnectionTask.lastExecutionFailed()).thenReturn(true);
        when(outboundConnectionTask.getCurrentRetryCount()).thenReturn(0);
    }

    private void markConnectionTaskAsNotFailed(ScheduledConnectionTask outboundConnectionTask) {
        when(outboundConnectionTask.lastExecutionFailed()).thenReturn(false);
        when(outboundConnectionTask.getCurrentRetryCount()).thenReturn(1);
    }

    @Test
    public void testWaitingTest() {
        ScheduledConnectionTask outboundConnectionTask = mock(ScheduledConnectionTask.class);
        when(outboundConnectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        Device device = mock(Device.class);
        when(device.getComTaskExecutions()).thenReturn(new ArrayList<>(0));
        when(outboundConnectionTask.getDevice()).thenReturn(device);
        markConnectionTaskAsNotNeverCompleted(outboundConnectionTask);
        markConnectionTaskAsNotFailed(outboundConnectionTask);
        Clock now = Clock.fixed(new DateTime(2012, 1, 1, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        markConnectionTaskAsWaiting(outboundConnectionTask);
        assertThat(ServerConnectionTaskStatus.getApplicableStatusFor(outboundConnectionTask, now.instant())).isEqualTo(TaskStatus.Waiting);
    }

    @Test
    public void testPausedMapsToOnHold() {
        assertThat(ServerConnectionTaskStatus.OnHold.getPublicStatus()).isEqualTo(TaskStatus.OnHold);
    }

    @Test
    public void testNeverCompletedMapsToNeverCompleted() {
        assertThat(ServerConnectionTaskStatus.NeverCompleted.getPublicStatus()).isEqualTo(TaskStatus.NeverCompleted);
    }

    @Test
    public void testWaitingMapsToWaiting() {
        assertThat(ServerConnectionTaskStatus.Waiting.getPublicStatus()).isEqualTo(TaskStatus.Waiting);
    }

    @Test
    public void testPendingMapsToPending() {
        assertThat(ServerConnectionTaskStatus.Pending.getPublicStatus()).isEqualTo(TaskStatus.Pending);
    }

    @Test
    public void testBusyMapsToBusy() {
        assertThat(ServerConnectionTaskStatus.Busy.getPublicStatus()).isEqualTo(TaskStatus.Busy);
    }

    @Test
    public void testRetryingMapsToRetrying() {
        assertThat(ServerConnectionTaskStatus.Retrying.getPublicStatus()).isEqualTo(TaskStatus.Retrying);
    }

    @Test
    public void testFailedMapsToFailed() {
        assertThat(ServerConnectionTaskStatus.Failed.getPublicStatus()).isEqualTo(TaskStatus.Failed);
    }

}