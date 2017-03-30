/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.ComTaskExecutionComCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;

import java.time.Clock;
import java.util.HashSet;
import java.util.Set;

public class RescheduleBehaviorForInbound extends AbstractRescheduleBehavior implements RescheduleBehavior {

    protected RescheduleBehaviorForInbound(ComServerDAO comServerDAO, ConnectionTask connectionTask, Clock clock) {
        super(comServerDAO, connectionTask, clock);
    }

    @Override
    protected void rescheduleForGeneralSetupError(CommandRoot commandRoot) {
        retryConnectionTask();
        for (ComTaskExecution comTaskExecution : commandRoot.getScheduledButNotPreparedComTaskExecutions()) {
            getComServerDAO().executionRescheduled(comTaskExecution, clock.instant());
        }
    }

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
            for (ComTaskExecution notExecutedComTask : notExecutedComTasks) {
                getComServerDAO().executionRescheduled(notExecutedComTask, clock.instant());
            }
            for (ComTaskExecution failedComTask : failedComTasks) {
                getComServerDAO().executionFailed(failedComTask);
            }
        } else {
            rescheduleSuccessfulConnectionTask();
        }
    }

    protected void rescheduleForConnectionError(CommandRoot commandRoot) {
        retryConnectionTask();
        for (GroupedDeviceCommand groupedDeviceCommand : commandRoot) {
            for (ComTaskExecutionComCommandImpl comTaskExecutionComCommand : groupedDeviceCommand) {
                switch (comTaskExecutionComCommand.getExecutionState()) {
                    case SUCCESSFULLY_EXECUTED:
                        getComServerDAO().executionCompleted(comTaskExecutionComCommand.getComTaskExecution());
                        break;
                    case NOT_EXECUTED: // intentional fallthrough
                    case FAILED: {
                        if (comTaskExecutionComCommand.getCompletionCode().equals(CompletionCode.NotExecuted)) {
                            getComServerDAO().executionRescheduled(comTaskExecutionComCommand.getComTaskExecution(), clock.instant());
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