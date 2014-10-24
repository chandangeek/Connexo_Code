package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Models a group of ComTaskExecutions that need to
 * executed as a single job because the related
 * ConnectionTask
 * does not allow simultaneous connections.
 * Executing these ScheduledComTasks in parallel would cause
 * problems because each isolated execution would actually
 * require the ScheduledComTask to create a connection for each.
 */
public class ScheduledComTaskExecutionGroup extends ScheduledJobImpl {

    private ScheduledConnectionTask connectionTask;
    private List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
    private List<ComTaskExecution> notExecutedComTaskExecutions = new ArrayList<>();
    private List<ComTaskExecution> failedComTaskExecutions = new ArrayList<>();
    private List<ComTaskExecution> successfulComTaskExecutions = new ArrayList<>();

    public ScheduledComTaskExecutionGroup(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ScheduledConnectionTask connectionTask, ServiceProvider serviceProvider) {
        super(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
        this.connectionTask = connectionTask;
    }

    @Override
    public ScheduledConnectionTask getConnectionTask() {
        return this.connectionTask;
    }

    @Override
    public List<ComTaskExecution> getNotExecutedComTaskExecutions() {
        return notExecutedComTaskExecutions;
    }

    @Override
    public List<ComTaskExecution> getFailedComTaskExecutions() {
        return failedComTaskExecutions;
    }

    @Override
    public List<ComTaskExecution> getSuccessfulComTaskExecutions() {
        return successfulComTaskExecutions;
    }

    public void add(ComTaskExecution comTask) {
        this.comTaskExecutions.add(comTask);
        this.notExecutedComTaskExecutions.add(comTask);
    }

    @Override
    public List<ComTaskExecution> getComTaskExecutions() {
        List<ComTaskExecution> scheduledComTasks = new ArrayList<>();
        scheduledComTasks.addAll(this.comTaskExecutions);
        return scheduledComTasks;
    }

    @Override
    public boolean attemptLock() {
        return this.attemptLock(this.connectionTask);
    }

    @Override
    public void unlock() {
        this.unlock(this.connectionTask);
    }

    @Override
    public boolean isStillPending() {
        return this.getComServerDAO().areStillPending(this.collectIds(this.comTaskExecutions));
    }

    private Collection<Long> collectIds(List<? extends HasId> hasIds) {
        Collection<Long> ids = new ArrayList<>(hasIds.size());
        for (HasId hasId : hasIds) {
            ids.add(hasId.getId());
        }
        return ids;
    }

    @Override
    public void execute() {
        boolean connectionOk = false;
        try {
            List<PreparedComTaskExecution> preparedComTaskExecutions = prepare();
            if (this.establishConnectionFor()) {
                connectionOk = true;
                preparedComTaskExecutions.forEach(this::performPreparedComTaskExecution);
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

    private List<PreparedComTaskExecution> prepare() {
        this.createExecutionContext();
        List<PreparedComTaskExecution> preparedComTaskExecutions = this.prepareAll(this.comTaskExecutions);
        getExecutionContext().setCommandRoot(preparedComTaskExecutions.get(0).getCommandRoot());
        return preparedComTaskExecutions;
    }

}