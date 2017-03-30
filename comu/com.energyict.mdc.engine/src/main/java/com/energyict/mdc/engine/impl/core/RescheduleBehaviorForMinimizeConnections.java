/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.ComTaskExecutionComCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

class RescheduleBehaviorForMinimizeConnections extends AbstractRescheduleBehavior implements RescheduleBehavior {

    RescheduleBehaviorForMinimizeConnections(ComServerDAO comServerDAO, ConnectionTask connectionTask, Clock clock) {
        super(comServerDAO, connectionTask, clock);
    }

    @Override
    protected void rescheduleForGeneralSetupError(CommandRoot commandRoot) {
        retryConnectionTask();
        Instant connectionTaskRetryNextExecution = calculateNextRetryExecutionTimestamp((OutboundConnectionTask) getConnectionTask());
        for (ComTaskExecution comTaskExecution : commandRoot.getScheduledButNotPreparedComTaskExecutions()) {
            rescheduleComTaskExecutionAccordingToConnectionRetry(connectionTaskRetryNextExecution, comTaskExecution);
        }
    }

    private void rescheduleComTaskExecutionAccordingToConnectionRetry(Instant connectionTaskRetryNextExecution, ComTaskExecution comTaskExecution) {
        if (((OutboundConnectionTask) getConnectionTask()).getCurrentRetryCount() == 0) {
            Instant nextExecutionTimeStamp = calculateNextExecutionTimestampFromBaseline(clock.instant(), comTaskExecution);
            getComServerDAO().executionRescheduled(comTaskExecution, nextExecutionTimeStamp);
        } else {
            getComServerDAO().executionRescheduled(comTaskExecution, connectionTaskRetryNextExecution);
        }
    }

    @Override
    protected void rescheduleForConnectionSuccess(CommandRoot commandRoot) {
        Set<ComTaskExecution> notExecutedComTasks = new HashSet<>();
        Set<ComTaskExecution> failedComTasks = new HashSet<>();
        for (GroupedDeviceCommand groupedDeviceCommand : commandRoot) {
            for (ComTaskExecutionComCommandImpl comTaskExecutionComCommand : groupedDeviceCommand) {
                switch (comTaskExecutionComCommand.getExecutionState()) {
                    case SUCCESSFULLY_EXECUTED:
                        getComServerDAO().executionCompleted(comTaskExecutionComCommand.getComTaskExecution());
                        break;
                    case NOT_EXECUTED: // intentional fallthrough
                    case FAILED: {
                        if (comTaskExecutionComCommand.getCompletionCode().equals(CompletionCode.NotExecuted)) {
                            notExecutedComTasks.add(comTaskExecutionComCommand.getComTaskExecution());
                        } else {
                            failedComTasks.add(comTaskExecutionComCommand.getComTaskExecution());
                        }
                    }
                    break;
                }
            }
        }
        if (notExecutedComTasks.size() > 0 || failedComTasks.size() > 0) {
            retryConnectionTask();
            // reschedule all not executed tasks to the next date of the connection
            Instant nextExecutionTimestamp = ((ScheduledConnectionTask) getConnectionTask()).getNextExecutionTimestamp();
            for (ComTaskExecution notExecutedComTask : notExecutedComTasks) {
                getComServerDAO().executionRescheduled(notExecutedComTask, nextExecutionTimestamp);
            }
            for (ComTaskExecution failedComTask : failedComTasks) {
                getComServerDAO().executionFailed(failedComTask);
            }
        } else {
            rescheduleSuccessfulConnectionTask();
        }
    }

    @Override
    protected void rescheduleForConnectionError(CommandRoot commandRoot) {
        Instant nextConnectionRetryDate = null;
        retryConnectionTask();
        if (getConnectionTask() instanceof ScheduledConnectionTask) {
            nextConnectionRetryDate = ((ScheduledConnectionTask) getConnectionTask()).getNextExecutionTimestamp();
        }
        for (GroupedDeviceCommand groupedDeviceCommand : commandRoot) {
            for (ComTaskExecutionComCommandImpl comTaskExecutionComCommand : groupedDeviceCommand) {
                switch (comTaskExecutionComCommand.getExecutionState()) {
                    case SUCCESSFULLY_EXECUTED:
                        getComServerDAO().executionCompleted(comTaskExecutionComCommand.getComTaskExecution());
                        break;
                    case NOT_EXECUTED: // intentional fallthrough
                    case FAILED: {
                        if (comTaskExecutionComCommand.getCompletionCode().equals(CompletionCode.NotExecuted)) {
                            getComServerDAO().executionRescheduled(comTaskExecutionComCommand.getComTaskExecution(), nextConnectionRetryDate);
                        } else {
                            getComServerDAO().executionFailed(comTaskExecutionComCommand.getComTaskExecution());
                        }
                    }
                    break;
                }
            }
        }
    }
}