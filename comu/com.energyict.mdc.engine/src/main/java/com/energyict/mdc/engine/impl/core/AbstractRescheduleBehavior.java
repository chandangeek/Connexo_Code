package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import java.util.List;

/**
 * Provides code reuse in RescheduleBehaviors
 *
 * Copyrights EnergyICT
 * Date: 4/06/13
 * Time: 16:44
 */
public abstract class AbstractRescheduleBehavior {

    private final ComServerDAO comServerDAO;
    private final List<ComTaskExecution> successfulComTaskExecutions;
    private final List<ComTaskExecution> failedComTaskExecutions;
    private final List<ComTaskExecution> notExecutedComTaskExecutions;
    private final ConnectionTask connectionTask;

    protected AbstractRescheduleBehavior(ComServerDAO comServerDAO,
                                         List<ComTaskExecution> successfulComTaskExecutions,
                                         List<ComTaskExecution> failedComTaskExecutions,
                                         List<ComTaskExecution> notExecutedComTaskExecutions, ConnectionTask connectionTask) {
        this.comServerDAO = comServerDAO;
        this.successfulComTaskExecutions = successfulComTaskExecutions;
        this.failedComTaskExecutions = failedComTaskExecutions;
        this.notExecutedComTaskExecutions = notExecutedComTaskExecutions;
        this.connectionTask = connectionTask;
    }

    protected void retryFailedComTasks() {
        this.comServerDAO.executionFailed(this.failedComTaskExecutions);
    }

    protected void rescheduleSuccessfulComTasks() {
        this.comServerDAO.executionCompleted(this.successfulComTaskExecutions);
    }

    protected void rescheduleNotExecutedComTasks() {
        this.comServerDAO.executionCompleted(this.notExecutedComTaskExecutions);
    }

    protected void retryConnectionTask() {
        this.comServerDAO.executionFailed(this.connectionTask);
    }

    protected void rescheduleSuccessfulConnectionTask(){
        this.comServerDAO.executionCompleted(this.connectionTask);
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
}
