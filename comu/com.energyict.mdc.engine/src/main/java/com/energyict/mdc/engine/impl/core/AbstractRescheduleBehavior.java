package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.scheduling.NextExecutionSpecs;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Provides code reuse in RescheduleBehaviors
 * <p>
 * Copyrights EnergyICT
 * Date: 4/06/13
 * Time: 16:44
 */
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
        comTaskExecutions.forEach(comTaskExecution -> this.comServerDAO.executionRescheduled(comTaskExecution, startingPoint));
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
        } else {
            rescheduleForConnectionSuccess(commandRoot);
        }
    }

    protected abstract void rescheduleForGeneralSetupError(CommandRoot commandRoot);

    protected abstract void rescheduleForConnectionSuccess(CommandRoot commandRoot);

    protected abstract void rescheduleForConnectionError(CommandRoot commandRoot);

    protected Instant calculateNextExecutionTimestampFromNow(ComTaskExecution comTaskExecution) {
        return calculateNextExecutionTimestampFromBaseline(clock.instant(), comTaskExecution);
    }

    protected Instant calculateNextExecutionTimestampFromBaseline(Instant baseLine, ComTaskExecution comTaskExecution) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Date.from(baseLine));
        Optional<NextExecutionSpecs> nextExecutionSpecs = comTaskExecution.getNextExecutionSpecs();
        if (nextExecutionSpecs.isPresent()) {
            return nextExecutionSpecs.get().getNextTimestamp(calendar).toInstant();
        } else {
            return null;
        }
    }

    protected Instant calculateNextRetryExecutionTimestamp(OutboundConnectionTask connectionTask) {
        Instant failureDate = clock.instant();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Date.from(failureDate));
        TimeDuration baseRetryDelay = getRescheduleRetryDelay(connectionTask);
        TimeDuration failureRetryDelay = new TimeDuration(baseRetryDelay.getCount() * connectionTask.getCurrentRetryCount(), baseRetryDelay.getTimeUnitCode());
        failureRetryDelay.addTo(calendar);
        return connectionTask.applyComWindowIfAny(calendar.getTime().toInstant());
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