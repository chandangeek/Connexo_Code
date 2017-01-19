package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.ComTaskExecutionComCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * <ul>
 * <li>If the connectionTasks would fail before executing the communication task(s), automatically all (planned) communication tasks will also fail (even if they have not been executed). This means you will see the following in the LastCommunicationTaskResult column;
 * <i>Success: 0 – Failed: 3 – Not Executed: NA (because we can’t say we didn’t try any ComTask)</i></li>
 * <li>When a connection task is executed and succeeds together with all communication tasks (even over multiple devices). The scheduledComTasks will be rescheduled according to their own schedule frequency. The nextExecution of the ConnectionTask will be the minimum of his scheduledComTask (therefore it is called the ASAP strategy)</li>
 * <li>
 * When a connectionTask is executed and succeeds, but one or more of the scheduledComTasks fail then:
 * <ul>
 * <li>The scheduledComTasks which were successful are rescheduled according to their own scheduling.</li>
 * <li>The scheduledComTasks that failed and that can be retried are rescheduled according to the retryDelay defined on the connectionTask</li>
 * <li>The scheduledComTasks that failed and that can NOT be retried, are rescheduled according to their own scheduling (their status will be failed as the lastSuccessfulEnd will be before the lastCommunicationStart date)</li>
 * <li>The scheduledComTasks that weren’t executed are also rescheduled according to their won retry delay. The same logic applies that if their maxRetry count is exceeded, they will be rescheduled according to their own frequency.</li>
 * </ul>
 * </li>
 * <li>
 * The number of retries on the connectionTask is reset when one of the following applies:
 * <ul>
 * <li>A user triggers the connectionTask</li>
 * <li>All scheduledComTasks during a connectionTask execution were successful</li>
 * </ul>
 * </li>
 * <li>
 * The number of retries on a scheduledComTask is reset when one of the following applies:
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
 * Time: 13:12
 */
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
                    case FAILED: {
                        if (comTaskExecutionComCommand.getCompletionCode().equals(CompletionCode.NotExecuted)) {
                            notExecutedComTasks.add(comTaskExecutionComCommand.getComTaskExecution());
                        } else {
                            getComServerDAO().executionFailed(comTaskExecutionComCommand.getComTaskExecution());
                            nextConnectionExecutionDate = comTaskExecutionComCommand.getComTaskExecution().getNextExecutionTimestamp();
                        }
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
        retryConnectionTask();
        for (GroupedDeviceCommand groupedDeviceCommand : commandRoot) {
            Instant connectionTaskRetryNextExecution = null;
            for (ComTaskExecutionComCommandImpl comTaskExecutionComCommand : groupedDeviceCommand) {
                switch (comTaskExecutionComCommand.getExecutionState()) {
                    case SUCCESSFULLY_EXECUTED:
                        getComServerDAO().executionCompleted(comTaskExecutionComCommand.getComTaskExecution());
                        break;
                    case NOT_EXECUTED: // intentional fallthrough
                    case FAILED: {
                        connectionTaskRetryNextExecution = calculateNextRetryExecutionTimestamp((OutboundConnectionTask) getConnectionTask());
                        rescheduleComTaskExecutionAccordingToConnectionRetry(connectionTaskRetryNextExecution, comTaskExecutionComCommand.getComTaskExecution());
                    }
                    break;
                }
            }
        }
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