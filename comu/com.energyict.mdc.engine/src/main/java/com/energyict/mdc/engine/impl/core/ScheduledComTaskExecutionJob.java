package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a single {@link ComTaskExecution}s
 * that can be executed as a single job because the related
 * ConnectionTask
 * allows simultaneous connections.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-14 (17:06)
 */
public class ScheduledComTaskExecutionJob extends ScheduledJobImpl {

    private ComTaskExecution comTaskExecution;
    private List<ComTaskExecution> notExecutedComTaskExecutions = new ArrayList<>();
    private List<ComTaskExecution> failedComTaskExecutions = new ArrayList<>();
    private List<ComTaskExecution> successfulComTaskExecutions = new ArrayList<>();

    public ScheduledComTaskExecutionJob(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ComTaskExecution comTaskExecution, IssueService issueService) {
        super(comPort, comServerDAO, deviceCommandExecutor, issueService);
        this.comTaskExecution = comTaskExecution;
        this.notExecutedComTaskExecutions.add(this.comTaskExecution);
    }

    @Override
    public ScheduledConnectionTask getConnectionTask() {
        return (ScheduledConnectionTask) this.comTaskExecution.getConnectionTask();
    }

    @Override
    public List<ComTaskExecution> getNotExecutedComTaskExecutions() {
        return this.notExecutedComTaskExecutions;
    }

    @Override
    public List<ComTaskExecution> getFailedComTaskExecutions() {
        return this.failedComTaskExecutions;
    }

    @Override
    public List<ComTaskExecution> getSuccessfulComTaskExecutions() {
        return this.successfulComTaskExecutions;
    }

    @Override
    public List<ComTaskExecution> getComTaskExecutions() {
        List<ComTaskExecution> scheduledComTasks = new ArrayList<>(1);
        scheduledComTasks.add(this.comTaskExecution);
        return scheduledComTasks;
    }

    @Override
    public boolean attemptLock() {
        return this.attemptLock(this.comTaskExecution);
    }

    @Override
    public void unlock() {
        this.unlock(this.comTaskExecution);
    }

    @Override
    public boolean isStillPending() {
        return this.getComServerDAO().isStillPending(this.comTaskExecution.getId());
    }

    @Override
    public void execute() {
        boolean connectionOk = false;
        try {
            this.createExecutionContext();
            JobExecution.PreparedComTaskExecution preparedComTaskExecution = this.prepareOne(this.comTaskExecution);
            Environment.DEFAULT.get().closeConnection();
            if (this.establishConnectionFor()) {
                connectionOk = true;
                performPreparedComTaskExecution(preparedComTaskExecution);
            }
        } catch (CommunicationException e) {
            connectionOk = false;
            throw e;
        } finally {
            if (connectionOk) {
                this.completeConnection();
            }
            this.closeConnection();
        }
    }
}