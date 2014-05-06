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

    @Override
    public void performRescheduling(RescheduleReason reason) {
        switch (reason) {
            case CONNECTION_SETUP: {
                performRetryForConnectionSetupError();
                break;
            }
            case CONNECTION_BROKEN: {
                performRetryForConnectionException();
                break;
            }
            case COMTASKS: {
                performRetryForCommunicationTasks();
                break;
            }
            case OUTSIDE_COM_WINDOW: {
                this.performRetryForNotExecutedCommunicationTasks();
                break;
            }
        }
    }

    private void performRetryForConnectionSetupError() {
        retryConnectionTask();
    }

    private void performRetryForConnectionException() {
        rescheduleSuccessfulComTasks();
        retryFailedComTasks();
        retryConnectionTask();
    }

    private void performRetryForCommunicationTasks() {
        rescheduleSuccessfulComTasks();
        retryFailedComTasks();
        if (getNumberOfFailedComTasks() == 0) {
            rescheduleSuccessfulConnectionTask();
        } else {
            retryConnectionTask();
        }
    }

    private void performRetryForNotExecutedCommunicationTasks() {
        this.rescheduleNotExecutedComTasks();
    }

}
