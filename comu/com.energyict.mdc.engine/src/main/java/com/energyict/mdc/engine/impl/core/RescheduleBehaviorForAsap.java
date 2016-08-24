package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;

import java.util.List;

/**
 * <ul>
 * <li>If the connectionTasks would fail before executing the communication task(s), automatically all (planned) communication tasks will also fail (even if they have not been executed). This means you will see the following in the LastCommunicationTaskResult column;
 * <i>Success: 0 – Failed: 3 – Not Executed: NA (because we can’t say we didn’t try any ComTask)</i></li>
 * <li>When a connection task is executed and succeeds together with all communication tasks (even over multiple devices). The scheduledComTasks will be rescheduled according to their own schedule frequency. The nextExecution of the ConnectionTask will be the minimum of his scheduledComTask (therefore it is called the ASAP strategy)</li>
 * <li>
 * When a connectionTask is executed and succeeds, but one or more of the scheduledComTasks fail then:
 *    <ul>
 *    <li>The scheduledComTasks which were successful are rescheduled according to their own scheduling.</li>
 *    <li>The scheduledComTasks that failed and that can be retried are rescheduled according to the retryDelay defined on the connectionTask</li>
 *    <li>The scheduledComTasks that failed and that can NOT be retried, are rescheduled according to their own scheduling (their status will be failed as the lastSuccessfulEnd will be before the lastCommunicationStart date)</li>
 *    <li>The scheduledComTasks that weren’t executed are also rescheduled according to their won retry delay. The same logic applies that if their maxRetry count is exceeded, they will be rescheduled according to their own frequency.</li>
 *    </ul>
 * </li>
 * <li>
 * The number of retries on the connectionTask is reset when one of the following applies:
 *    <ul>
 *    <li>A user triggers the connectionTask</li>
 *    <li>All scheduledComTasks during a connectionTask execution were successful</li>
 *    </ul>
 * </li>
 * <li>
 * The number of retries on a scheduledComTask is reset when one of the following applies:
 *    <ul>
 *    <li>The scheduledComTask is executed successfully</li>
 *    <li>The maxNumberOfTries has exceeded and the scheduledComTask is rescheduled according to this schedule frequency instead of the retry frequency</li>
 *    <li>A user triggers the scheduledComTask</li>
 *    </ul>
 * </li>
 * </ul>
 *
 * Copyrights EnergyICT
 * Date: 3/06/13
 * Time: 13:12
 */
class RescheduleBehaviorForAsap extends AbstractRescheduleBehavior implements RescheduleBehavior {

    private final ExecutionContext executionContext;

    RescheduleBehaviorForAsap(
            ComServerDAO comServerDAO,
            List<ComTaskExecution> successfulComTaskExecutions,
            List<ComTaskExecution> failedComTaskExecutions,
            List<ComTaskExecution> notExecutedComTaskExecutions,
            ConnectionTask connectionTask, ExecutionContext executionContext) {
        super(comServerDAO, successfulComTaskExecutions, failedComTaskExecutions, notExecutedComTaskExecutions, connectionTask);
        this.executionContext = executionContext;
    }

    @Override
    public void performRescheduling(RescheduleReason reason) {
        ScheduledConnectionTask connectionTask = getScheduledConnectionTask();
        connectionTask.setMaxNumberOfTries(Integer.MAX_VALUE);
        super.performRescheduling(reason);
    }

    /**
     * All ComTasks should be marked as failed, otherwise we can't reschedule with the asap strategy
     */
    @Override
    protected void performRetryForConnectionSetupError() {
        retryNotExecutedComTasks();
        retryConnectionTask();
    }

    @Override
    protected void performRetryForConnectionException() {
        rescheduleSuccessfulComTasks();
        retryFailedComTasks();
        retryNotExecutedComTasks();
        retryConnectionTask();
    }

    @Override
    protected void performRetryForCommunicationTasks() {
        rescheduleSuccessfulComTasks();
        retryFailedComTasks();
        if (getNumberOfFailedComTasks() == 0) {
            rescheduleSuccessfulConnectionTask();
        } else {
            retryConnectionTask();
        }
        performRetryForNotExecutedCommunicationTasks();
    }

    @Override
    protected void performRetryForNotExecutedCommunicationTasks() {
        this.rescheduleNotExecutedComTasks();
    }

    private void retryNotExecutedComTasks() {
        String reason = "Comtask failed due to connection setup error in ASAP strategy";
        Throwable t = new Throwable(reason);
        for (ComTaskExecution notExecutedComTaskExecution : getNotExecutedComTaskExecutions()) {
            if (this.executionContext != null) {
                notExecutedComTaskExecution
                        .getComTasks()
                        .forEach(ct -> {
                            this.executionContext.prepareStart(this.executionContext.getJob(), notExecutedComTaskExecution);
                            this.getComServerDAO().executionStarted(notExecutedComTaskExecution, this.executionContext.getComPort(), false);
                            this.executionContext.executionStarted(notExecutedComTaskExecution);
                            this.executionContext.start(notExecutedComTaskExecution, ct);
                            this.executionContext.markComTaskExecutionForConnectionSetupError(reason);
                            this.executionContext.failForRetryAsapComTaskExec(notExecutedComTaskExecution, t); // I know we just started it, but the start creates the proper shadow for the ComTaskExecution

                        });
            }
            getComServerDAO().executionFailed(notExecutedComTaskExecution);
        }
    }

}