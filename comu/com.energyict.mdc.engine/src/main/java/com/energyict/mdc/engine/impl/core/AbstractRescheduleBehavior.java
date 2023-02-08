/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.scheduling.NextExecutionSpecs;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.ComTaskExecutionComCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

abstract class AbstractRescheduleBehavior {
    protected final Clock clock;
    private final ComServerDAO comServerDAO;
    private ConnectionTask connectionTask;

    AbstractRescheduleBehavior(ComServerDAO comServerDAO, ConnectionTask connectionTask, Clock clock) {
        this.comServerDAO = comServerDAO;
        this.connectionTask = connectionTask;
        this.clock = clock;
    }

    public void rescheduleOutsideComWindow(List<ComTaskExecution> comTaskExecutions, Instant startingPoint) {
        comTaskExecutions.forEach(comTaskExecution -> this.comServerDAO.executionRescheduledToComWindow(comTaskExecution, startingPoint));
    }

    void rescheduleNotExecutedComTasks(List<ComTaskExecution> comTaskExecutions) {
        getComServerDAO().executionCompleted(comTaskExecutions);
    }

    void retryConnectionTask() {
        this.connectionTask = this.comServerDAO.executionFailed(this.connectionTask);
    }

    void rescheduleSuccessfulConnectionTask() {
        this.connectionTask = this.comServerDAO.executionCompleted(this.connectionTask);
    }

    void rescheduleInterruptedConnectionTask() {
        getComServerDAO().executionRescheduled(connectionTask);
    }

    ConnectionTask getConnectionTask() {
        return this.connectionTask;
    }

    ComServerDAO getComServerDAO() {
        return this.comServerDAO;
    }

    public void reschedule(CommandRoot commandRoot) {
        if (commandRoot.hasConnectionErrorOccurred() || commandRoot.hasConnectionSetupError()) {
            rescheduleForConnectionError(commandRoot);
        } else if (commandRoot.hasGeneralSetupErrorOccurred()) {
            rescheduleForGeneralSetupError(commandRoot);
        } else if (commandRoot.hasConnectionBeenInterrupted()) {
            rescheduleForConnectionInterrupted(commandRoot);
        } else {
            rescheduleForConnectionSuccess(commandRoot);
        }
    }

    protected abstract void rescheduleForGeneralSetupError(CommandRoot commandRoot);

    protected abstract void rescheduleForConnectionSuccess(CommandRoot commandRoot);

    protected abstract void rescheduleForConnectionError(CommandRoot commandRoot);

    protected void rescheduleForConnectionInterrupted(CommandRoot commandRoot) {
        Logger.getAnonymousLogger().warning("[" + Thread.currentThread().getName() + "] rescheduleForConnectionInterrupted");
        rescheduleInterruptedConnectionTask();
        Instant nextConnectionRescheduleDate = calculateNextRescheduleExecutionTimestamp();
        for (GroupedDeviceCommand groupedDeviceCommand : commandRoot) {
            for (ComTaskExecutionComCommandImpl comTaskExecutionComCommand : groupedDeviceCommand) {
                switch (comTaskExecutionComCommand.getExecutionState()) {
                    case SUCCESSFULLY_EXECUTED:
                        getComServerDAO().executionCompleted(comTaskExecutionComCommand.getComTaskExecution());
                        break;
                    case NOT_EXECUTED: // intentional fallthrough
                    case FAILED: {
                        Logger.getAnonymousLogger().warning(comTaskExecutionComCommand.getComTaskExecution().getComTask().getName() + " rescheduled to " + nextConnectionRescheduleDate);
                        getComServerDAO().executionRescheduled(comTaskExecutionComCommand.getComTaskExecution(), nextConnectionRescheduleDate);
                    }
                    break;
                }
            }
        }
    }

    protected abstract Instant calculateNextRescheduleExecutionTimestamp();

    protected Instant calculateNextExecutionTimestampFromNow(ComTaskExecution comTaskExecution) {
        return calculateNextExecutionTimestampFromBaseline(clock.instant(), comTaskExecution);
    }

    protected Instant calculateNextExecutionTimestampFromBaseline(Instant baseLine, ComTaskExecution comTaskExecution) {
        Optional<NextExecutionSpecs> nextExecutionSpecs = comTaskExecution.getNextExecutionSpecs();
        if (nextExecutionSpecs.isPresent()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(baseLine));
            return nextExecutionSpecs.get().getNextTimestamp(calendar).toInstant();
        } else {
            return null;
        }
    }

    protected Instant calculateNextRetryExecutionTimestamp(OutboundConnectionTask connectionTask) {
        Instant nextExecution = clock.instant();
        TimeDuration baseRetryDelay = getRescheduleRetryDelay(connectionTask);
        TimeDuration failureRetryDelay = new TimeDuration(baseRetryDelay.getCount() * connectionTask.getCurrentRetryCount(), baseRetryDelay.getTimeUnitCode());
        nextExecution = nextExecution.plusSeconds(failureRetryDelay.getSeconds());

        return connectionTask.applyComWindowIfAny(nextExecution);
    }

    /**
     * The rescheduleRetryDelay is fetched as follow:
     * <ul>
     * <li>First we check if this {@link ScheduledConnectionTask} has a proper {@link ScheduledConnectionTask#getRescheduleDelay()}</li>
     * <li>Finally, when none of the above are provided, we return the default {@link ComTaskExecution#DEFAULT_COMTASK_FAILURE_RESCHEDULE_DELAY_SECONDS}</li>
     * </ul>
     *
     * @return the configured rescheduleRetryDelay
     */
    public TimeDuration getRescheduleRetryDelay(OutboundConnectionTask connectionTask) {
        if (connectionTask.getRescheduleDelay() != null) {
            return connectionTask.getRescheduleDelay();
        }
        return defaultRescheduleDelay();
    }

    private TimeDuration defaultRescheduleDelay() {
        return new TimeDuration(ComTaskExecution.DEFAULT_COMTASK_FAILURE_RESCHEDULE_DELAY_SECONDS, TimeDuration.TimeUnit.SECONDS);
    }
}
