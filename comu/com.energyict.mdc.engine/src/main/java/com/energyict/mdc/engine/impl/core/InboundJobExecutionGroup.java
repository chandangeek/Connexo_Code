package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link InboundJobExecutionGroup} is responsible for execution
 * a group of inbound ComTasks.
 * A logical <i>connect</i> can be skipped as the
 * {@link ComChannel ComChannl}
 * will already be created by the ComPortListener
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/10/12
 * Time: 16:06
 */
public class InboundJobExecutionGroup extends JobExecution {

    protected final InboundCommunicationHandler inboundCommunicationHandler;
    private final InboundDiscoveryContextImpl inboundDiscoveryContext;
    private InboundConnectionTask connectionTask;
    private List<ComTaskExecution> comTaskExecutions;
    private List<ComTaskExecution> notExecutedComTaskExecutions = new ArrayList<>();
    private List<ComTaskExecution> failedComTaskExecutions = new ArrayList<>();
    private List<ComTaskExecution> successfulComTaskExecutions = new ArrayList<>();

    public InboundJobExecutionGroup(ComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundDiscoveryContextImpl inboundDiscoveryContext, ServiceProvider serviceProvider, InboundCommunicationHandler inboundCommunicationHandler) {
        super(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
        this.inboundDiscoveryContext = inboundDiscoveryContext;
        this.inboundCommunicationHandler = inboundCommunicationHandler;
    }

    @Override
    protected ComPortRelatedComChannel findOrCreateComChannel(ConnectionTaskPropertyProvider connectionTaskPropertyProvider) throws ConnectionException {
        return this.inboundDiscoveryContext.getComChannel();
    }

    @Override
    public List<ComTaskExecution> getComTaskExecutions() {
        List<ComTaskExecution> inboundComTasks = new ArrayList<>();
        inboundComTasks.addAll(this.comTaskExecutions);
        return inboundComTasks;
    }

    @Override
    protected boolean isConnected() {
        return isBinaryConnected() || isServletConnected();
    }

    private boolean isServletConnected() {
        return this.inboundDiscoveryContext.getServletRequest() != null && this.inboundDiscoveryContext.getServletResponse() != null;
    }

    private boolean isBinaryConnected() {
        return getExecutionContext().getComPortRelatedComChannel() != null;
    }

    public void executeDeviceProtocol(List<ComTaskExecution> inboundComTaskExecutions) {
        this.comTaskExecutions = inboundComTaskExecutions;
        this.notExecutedComTaskExecutions.addAll(inboundComTaskExecutions);
        /*
        The InboundJobExecutor already has a token from the InboundCommunicationHandler. We are sure nobody else
        will pick up this task, as it is the ComPort which received the trigger to start.
         */
        InboundScheduledJobExecutor jobExecutor =
            new InboundScheduledJobExecutor(
                getServiceProvider().transactionService(),
                this.getComPort().getComServer().getCommunicationLogLevel(),
                getDeviceCommandExecutor());
        jobExecutor.execute(this);
    }

    @Override
    public void completed() {
        this.inboundCommunicationHandler.complete();
        super.completed();
    }

    @Override
    public void failed(Throwable t, ExecutionFailureReason reason) {
        this.inboundCommunicationHandler.complete();
        super.failed(t, reason);
    }

    @Override
    protected ComSession.SuccessIndicator getFailureIndicatorFor(RescheduleBehavior.RescheduleReason rescheduleReason) {
        return this.inboundCommunicationHandler.getFailureIndicator();
    }

    @Override
    public boolean attemptLock() {
        /* Devices that do inbound communication will never connect in parallel sessions
         * so we do not really need locking. */
        return true;
    }

    @Override
    public void unlock() {
        // No effects from attemptLock to undo
    }

    @Override
    public boolean isStillPending() {
        /* Inbound jobs cannot be stolen by other threads
         * so must always still be pending. */
        return true;
    }

    @Override
    public void execute() {
        this.executeAllTasks();
    }

    void executeAllTasks() {
        try {
            this.setExecutionContext(this.newExecutionContext(this.connectionTask, this.getComPort()));
            List<PreparedComTaskExecution> preparedComTaskExecutions = this.prepareAll(this.comTaskExecutions);
            if (this.getExecutionContext().connect()) {
                preparedComTaskExecutions.forEach(this::performPreparedComTaskExecution);
            }
        } finally {
            this.closeConnection();
        }
    }

    @Override
    public boolean isWithinComWindow () {
        return true;    // ComWindow does not apply to inbound communication
    }

    @Override
    public void outsideComWindow() {
        // scheduling is done in the device for inbound communication so I can't fix this here
    }

    @Override
    public void rescheduleToNextComWindow (ComServerDAO comServerDAO) {
        // rescheduling is done in the device for inbound communication
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
    public ConnectionTask getConnectionTask() {
        return this.connectionTask;
    }

    public void setConnectionTask(InboundConnectionTask connectionTask) {
        this.connectionTask = connectionTask;
    }

    protected InboundDiscoveryContextImpl getInboundDiscoveryContext() {
        return this.inboundDiscoveryContext;
    }
}
