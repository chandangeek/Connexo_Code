package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.exceptions.ConnectionSetupException;

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

    public ScheduledComTaskExecutionJob(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
        this.comTaskExecution = comTaskExecution;
        this.notExecutedComTaskExecutions.add(this.comTaskExecution);
    }

    @Override
    public ScheduledConnectionTask getConnectionTask() {
        // ComTaskExecution was returned by task query that joins it with the ConnectionTask so it cannot be <code>null</code>
        return (ScheduledConnectionTask) this.comTaskExecution.getConnectionTask().get();
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
            PreparedComTaskExecution preparedComTaskExecution = prepare();
            if (this.establishConnection()) {
                connectionOk = true;
                this.performPreparedComTaskExecution(preparedComTaskExecution);
            }
        } catch (ConnectionSetupException e){
            int totalNumberOfComTasks = this.comTaskExecution.getComTasks().size();
            this.getExecutionContext().getComSessionBuilder().incrementNotExecutedTasks(totalNumberOfComTasks);
            connectionOk = false;
            throw e;
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

    private PreparedComTaskExecution prepare() {
        this.createExecutionContext();
        PreparedComTaskExecution preparedComTaskExecution = this.prepareOne(this.comTaskExecution);
        getExecutionContext().setCommandRoot(preparedComTaskExecution.getCommandRoot());
        return preparedComTaskExecution;
    }

}