/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.ComTaskExecutionComCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

class RescheduleBehaviorForAsap extends AbstractRescheduleBehavior implements RescheduleBehavior {

    RescheduleBehaviorForAsap(ComServerDAO comServerDAO,
                              ConnectionTask connectionTask, Clock clock) {
        super(comServerDAO, connectionTask, clock);
    }

    protected void rescheduleForConnectionSuccess(CommandRoot commandRoot) {
        rescheduleSuccessfulConnectionTask();
        for (GroupedDeviceCommand groupedDeviceCommand : commandRoot) {
            Instant nextConnectionExecutionDate = null;
            Set<ComTaskExecution> notExecutedComTasks = new HashSet<>();
            for (ComTaskExecutionComCommandImpl comTaskExecutionComCommand : groupedDeviceCommand) {
                switch (comTaskExecutionComCommand.getExecutionState()) {
                    case SUCCESSFULLY_EXECUTED:
                        getComServerDAO().executionCompleted(comTaskExecutionComCommand.getComTaskExecution());
                        break;
                    case NOT_EXECUTED: // intentional fallthrough
                    case FAILED:
                        if (comTaskExecutionComCommand.getCompletionCode().equals(CompletionCode.NotExecuted)) {
                            if (comTaskExecutionComCommand.getCommands().size() == 0)  //there were no commands to execute
                            {
                                getComServerDAO().executionCompleted(comTaskExecutionComCommand.getComTaskExecution());
                            } else {
                                notExecutedComTasks.add(comTaskExecutionComCommand.getComTaskExecution());
                            }
                        } else if (comTaskExecutionComCommand.getCompletionCode().equals(CompletionCode.ConfigurationWarning)
                                || comTaskExecutionComCommand.getCompletionCode().equals(CompletionCode.ConfigurationError)) {
                            getComServerDAO().executionFailed(comTaskExecutionComCommand.getComTaskExecution(), true);
                        } else {
                            getComServerDAO().executionFailed(comTaskExecutionComCommand.getComTaskExecution());
                            nextConnectionExecutionDate = comTaskExecutionComCommand.getComTaskExecution().getNextExecutionTimestamp();
                        }
                        break;
                }
            }
            for (ComTaskExecution notExecutedComTask : notExecutedComTasks) {
                if (nextConnectionExecutionDate == null) {
                    nextConnectionExecutionDate = calculateNextExecutionTimestampFromNow(notExecutedComTask);
                }
                getComServerDAO().executionRescheduled(notExecutedComTask, nextConnectionExecutionDate);
            }
        }
        // reschedule all not executed tasks to the next date of the failed comtasks
    }

    @Override
    protected void rescheduleForGeneralSetupError(CommandRoot commandRoot) {
        retryConnectionTask();
        Instant connectionTaskRetryNextExecution = calculateNextRetryExecutionTimestamp((OutboundConnectionTask) getConnectionTask());
        for (ComTaskExecution comTaskExecution : commandRoot.getScheduledButNotPreparedComTaskExecutions()) {
            rescheduleComTaskExecutionAccordingToConnectionRetry(connectionTaskRetryNextExecution, comTaskExecution);
        }
    }

    protected void rescheduleForConnectionError(CommandRoot commandRoot) {
        if (commandRoot.hasConnectionNotExecuted() && commandRoot.getCommands().size() == 0) //there were no commands to execute
        {
            rescheduleSuccessfulConnectionTask();
        } else {
            retryConnectionTask();
        }
        for (GroupedDeviceCommand groupedDeviceCommand : commandRoot) {
            Instant connectionTaskRetryNextExecution = null;
            for (ComTaskExecutionComCommandImpl comTaskExecutionComCommand : groupedDeviceCommand) {
                switch (comTaskExecutionComCommand.getExecutionState()) {
                    case SUCCESSFULLY_EXECUTED:
                        getComServerDAO().executionCompleted(comTaskExecutionComCommand.getComTaskExecution());
                        break;
                    case NOT_EXECUTED: // intentional fallthrough
                    case FAILED:
                        if (commandRoot.hasConnectionNotExecuted() && commandRoot.getCommands().size() == 0) { //there were no commands to execute
                            getComServerDAO().executionCompleted(comTaskExecutionComCommand.getComTaskExecution());
                        } else {
                            connectionTaskRetryNextExecution = calculateNextRetryExecutionTimestamp((OutboundConnectionTask) getConnectionTask());
                            rescheduleComTaskExecutionAccordingToConnectionRetry(connectionTaskRetryNextExecution, comTaskExecutionComCommand.getComTaskExecution());
                        }
                        break;
                }
            }
        }
    }

    @Override
    protected Instant calculateNextRescheduleExecutionTimestamp() {
        OutboundConnectionTask connectionTask = (OutboundConnectionTask) getConnectionTask();
        Instant nextExecution = clock.instant();
        TimeDuration baseRetryDelay = getRescheduleRetryDelay(connectionTask);
        nextExecution = nextExecution.plusSeconds(baseRetryDelay.getSeconds());

        return connectionTask.applyComWindowIfAny(nextExecution);
    }

    private void rescheduleComTaskExecutionAccordingToConnectionRetry(Instant connectionTaskRetryNextExecution, ComTaskExecution comTaskExecution) {
        OutboundConnectionTask connectionTask = (OutboundConnectionTask) getConnectionTask();
        if (connectionTask.lastExecutionFailed() && connectionTask.getCurrentRetryCount() == 0) {
            Instant nextExecutionTimeStamp = calculateNextExecutionTimestampFromNow(comTaskExecution);
            getComServerDAO().executionRescheduled(comTaskExecution, nextExecutionTimeStamp);
        } else {
            getComServerDAO().executionRescheduled(comTaskExecution, connectionTaskRetryNextExecution);
        }
    }
}
