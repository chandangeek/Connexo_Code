package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 4/06/13
 * Time: 15:29
 */
public class RescheduleBehaviorForInbound extends AbstractRescheduleBehavior implements RescheduleBehavior {

    protected RescheduleBehaviorForInbound(ComServerDAO comServerDAO, List<ComTaskExecution> successfulComTaskExecutions, List<ComTaskExecution> failedComTaskExecutions, List<ComTaskExecution> notExecutedComTaskExecutions, ConnectionTask connectionTask) {
        super(comServerDAO, successfulComTaskExecutions, failedComTaskExecutions, notExecutedComTaskExecutions, connectionTask);
    }

    protected void performRetryForConnectionSetupError() {
        retryConnectionTask();
    }

    protected void performRetryForConnectionException() {
        rescheduleSuccessfulComTasks();
        retryFailedComTasks();
        retryConnectionTask();
    }

    protected void performRetryForCommunicationTasks() {
        rescheduleSuccessfulComTasks();
        retryFailedComTasks();
        if (getNumberOfFailedComTasks() == 0) {
            rescheduleSuccessfulConnectionTask();
        } else {
            retryConnectionTask();
        }
    }

    protected void performRetryForNotExecutedCommunicationTasks() {
        this.rescheduleNotExecutedComTasks();
    }

}
