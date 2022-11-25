/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;

import com.energyict.protocol.exceptions.ConnectionException;

import java.util.ArrayList;
import java.util.List;

public class InboundJobExecutionGroup extends JobExecution {

    private final InboundDiscoveryContext inboundDiscoveryContext;
    private final InboundCommunicationHandler inboundCommunicationHandler;
    private InboundConnectionTask connectionTask;
    private List<ComTaskExecution> comTaskExecutions;

    public InboundJobExecutionGroup(ComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundDiscoveryContext inboundDiscoveryContext, ServiceProvider serviceProvider, InboundCommunicationHandler InboundCommunicationHandler) {
        super(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
        this.inboundDiscoveryContext = inboundDiscoveryContext;
        inboundCommunicationHandler = InboundCommunicationHandler;
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
        return isBinaryConnected() || isCoapConnected() || isServletConnected();
    }

    private boolean isCoapConnected() {
        return this.inboundDiscoveryContext.getCoapBasedExchange() != null;
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
            if (!commandRoot.hasGeneralSetupErrorOccurred() && !commandRoot.getCommands().isEmpty()) {
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
    public boolean isHighPriorityJob() {
        return false;
    }

    @Override
    public void rescheduleToNextComWindow() {
        // rescheduling is done in the device for inbound communication
    }

    @Override
    public boolean isConnectedTo(OutboundConnectionTask connectionTask) {
        return false;
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

    public void appendStatisticalInformationToComSession() {
        inboundCommunicationHandler.appendStatisticalInformationToComSession();
    }
}