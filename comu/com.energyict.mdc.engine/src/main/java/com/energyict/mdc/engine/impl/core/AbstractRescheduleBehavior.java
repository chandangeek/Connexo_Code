package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;

import java.time.Instant;
import java.util.List;

/**
 * Provides code reuse in RescheduleBehaviors
 *
 * Copyrights EnergyICT
 * Date: 4/06/13
 * Time: 16:44
 */
abstract class AbstractRescheduleBehavior {

    private final ComServerDAO comServerDAO;
    private final List<ComTaskExecution> successfulComTaskExecutions;
    private final List<ComTaskExecution> failedComTaskExecutions;
    private final List<ComTaskExecution> notExecutedComTaskExecutions;
    private final ConnectionTask connectionTask;

    AbstractRescheduleBehavior(
            ComServerDAO comServerDAO,
            List<ComTaskExecution> successfulComTaskExecutions,
            List<ComTaskExecution> failedComTaskExecutions,
            List<ComTaskExecution> notExecutedComTaskExecutions, ConnectionTask connectionTask) {
        this.comServerDAO = comServerDAO;
        this.successfulComTaskExecutions = successfulComTaskExecutions;
        this.failedComTaskExecutions = failedComTaskExecutions;
        this.notExecutedComTaskExecutions = notExecutedComTaskExecutions;
        this.connectionTask = connectionTask;
    }

    void retryFailedComTasks() {
        this.comServerDAO.executionFailed(this.failedComTaskExecutions);
    }

    void rescheduleSuccessfulComTasks() {
        this.comServerDAO.executionCompleted(this.successfulComTaskExecutions);
    }

    void rescheduleNotExecutedComTasks() {
        this.comServerDAO.executionCompleted(this.notExecutedComTaskExecutions);
    }

    void retryConnectionTask() {
        this.comServerDAO.executionFailed(this.connectionTask);
    }

    void rescheduleSuccessfulConnectionTask(){
        this.comServerDAO.executionCompleted(this.connectionTask);
    }

    private void performRescheduleNotExecutedComTasks(Instant startingPoint) {
        notExecutedComTaskExecutions.forEach(comTaskExecution -> this.comServerDAO.executionRescheduled(comTaskExecution, startingPoint));
    }

    ConnectionTask getConnectionTask(){
        return this.connectionTask;
    }

    ComServerDAO getComServerDAO(){
        return this.comServerDAO;
    }

    List<? extends ComTaskExecution> getNotExecutedComTaskExecutions() {
        return notExecutedComTaskExecutions;
    }

    int getNumberOfFailedComTasks(){
        return (this.failedComTaskExecutions != null?this.failedComTaskExecutions.size():0)
                + (this.getNotExecutedComTaskExecutions() != null?this.getNotExecutedComTaskExecutions().size():0);
    }

    public void performRescheduling(RescheduleBehavior.RescheduleReason reason) {
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
                //TODO verify if this is still valid
                this.performRetryForNotExecutedCommunicationTasks();
                break;
            }
        }
    }

    public void rescheduleOutsideWindow(Instant startingPoint) {
        performRescheduleNotExecutedComTasks(startingPoint);
    }

    protected abstract void performRetryForConnectionSetupError();

    protected abstract void performRetryForConnectionException();

    protected abstract void performRetryForCommunicationTasks();

    protected abstract void performRetryForNotExecutedCommunicationTasks();

    ScheduledConnectionTask getScheduledConnectionTask() {
        return (ScheduledConnectionTask) getConnectionTask();
    }
}
