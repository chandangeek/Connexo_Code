package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.protocol.exceptions.ConnectionException;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link InboundJobExecutionGroup} is responsible for execution
 * a group of inbound ComTasks.
 * A logical <i>connect</i> can be skipped as the
 * {@link ComChannel ComChannl}
 * will already be created by the ComPortListener
 * <p>
 * Copyrights EnergyICT
 * Date: 25/10/12
 * Time: 16:06
 */
public class InboundJobExecutionGroup extends JobExecution {

    private final InboundDiscoveryContext inboundDiscoveryContext;
    private InboundConnectionTask connectionTask;
    private List<ComTaskExecution> comTaskExecutions;

    public InboundJobExecutionGroup(ComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundDiscoveryContext inboundDiscoveryContext, ServiceProvider serviceProvider) {
        super(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
        this.inboundDiscoveryContext = inboundDiscoveryContext;
    }

    @Override
    protected ComPortRelatedComChannel findOrCreateComChannel(ConnectionTaskPropertyProvider connectionTaskPropertyProvider) throws ConnectionException {
        return (ComPortRelatedComChannel) this.inboundDiscoveryContext.getComChannel();
    }

    @Override
    public List<ComTaskExecution> getComTaskExecutions() {
        List<ComTaskExecution> inboundComTasks = new ArrayList<>();
        inboundComTasks.addAll(this.comTaskExecutions);
        return inboundComTasks;
    }

    @Override
    public boolean isConnected() {
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
        try {
            boolean connectionEstablished = false;
            this.setExecutionContext(this.newExecutionContext(this.connectionTask, this.getComPort()));
            commandRoot = this.prepareAll(this.comTaskExecutions);
            if (!commandRoot.hasGeneralSetupErrorOccurred()) {
                connectionEstablished = this.getExecutionContext().connect();
            }
            commandRoot.execute(connectionEstablished);
        } finally {
            this.closeConnection();
        }
    }

    @Override
    public boolean isWithinComWindow() {
        return true;    // ComWindow does not apply to inbound communication
    }

    @Override
    public void rescheduleToNextComWindow() {
        // rescheduling is done in the device for inbound communication
    }

    @Override
    public ConnectionTask getConnectionTask() {
        return this.connectionTask;
    }

    public void setConnectionTask(InboundConnectionTask connectionTask) {
        this.connectionTask = connectionTask;
    }

    protected InboundDiscoveryContextImpl getInboundDiscoveryContext() {
        return (InboundDiscoveryContextImpl) inboundDiscoveryContext;
    }
}