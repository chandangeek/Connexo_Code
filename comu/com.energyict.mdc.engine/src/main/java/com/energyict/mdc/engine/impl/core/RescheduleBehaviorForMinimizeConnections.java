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
import java.util.List;
import java.util.Set;

/**
 * <ul>
 * <li>
 * When a connectionTasks is executed and fails before executing any scheduledComTasks,
 * the connnectionTask needs to be rescheduled and the scheduledComTasks will remain pending.
 * This means you will see the following in the LastCommunicationTaskResult column;</br>
 * <i>Success: 0 – Failed: 0 – Not Executed: 3 (if you had 3 ComTasks pending)</i>
 * </li>
 * <li>When a connection task is executed and succeeds together with all communication tasks (even over multiple devices), the connection task will be rescheduled as success/waiting for the next planned date. Together all the communication tasks will get a "next execution" date set to the next planned date of the connection task.</li>
 * <li>When a connectionTask is executed and succeeds, but one or more of the scheduledComTasks fail, then the connectionTask is rescheduled according to his retryDelay.
 * <ul>
 * <li>The scheduledComTasks which were successful are rescheduled according to their own scheduling.</li>
 * <li>The scheduledComTasks that failed and that can be retried are rescheduled according to the retryDelay defined on the connectionTask (in theory this is the same date as the nextExecution of the connectionTask).</li>
 * <li>The scheduledComTasks that failed and that can NOT be retried, are rescheduled according to their own scheduling (their status will be failed as the lastSuccessfulEnd will be before the lastCommunicationStart date)</li>
 * <li>The scheduledComTasks that weren’t executed are untouched. They will be picked up the next time the connectionTask is picked up.</li>
 * </ul></li>
 * <li>
 * When the max. number of connection retries has been reached, the connection task will be rescheduled to the next planned scheduled date (with a status failed, first call)
 * <ul>
 * <li> If one or more communication tasks would still be retrying they will be rescheduled according to their own schedule frequency. Their status will result in failed.</li>
 * <li>If one or more communication tasks have not been executed at all, they will keep their old "next execution" date</li>
 * </ul>
 * </li>
 * <li>The number of retries on the connectionTask is reset when one of the following applies:
 * <ul>
 * <li>A user triggers the connectionTask</li>
 * <li>All scheduledComTasks during a connectionTask execution were successful</li>
 * </ul></li>
 * <li>
 * The number of retries on a scheduledComTasks is reset when one of the following applies:
 * <ul>
 * <li>The scheduledComTask is executed successfully</li>
 * <li>The maxNumberOfTries has exceeded and the scheduledComTask is rescheduled according to this schedule frequency instead of the retry frequency</li>
 * <li>A user triggers the scheduledComTask</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * Copyrights EnergyICT
 * Date: 3/06/13
 * Time: 11:09
 */
public class RescheduleBehaviorForMinimizeConnections extends AbstractRescheduleBehavior implements RescheduleBehavior {

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