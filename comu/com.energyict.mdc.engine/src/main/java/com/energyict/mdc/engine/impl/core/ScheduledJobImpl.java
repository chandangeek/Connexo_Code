/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.RescheduleToNextComWindow;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.connection.EstablishConnectionEvent;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;

import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocol.exceptions.ConnectionSetupException;

import java.time.Clock;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Provides code reuse opportunities for component that
 * want to implement the {@link ScheduledJob} interface.
 * The support is primarily towards creating the physical
 * connection with the device, i.e. the {@link ComChannel},
 * keeping track of the ComSession,
 * the ComTaskExecutionSession
 * and the related statistics.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-14 (16:44)
 */
public abstract class ScheduledJobImpl extends JobExecution {

    protected ScheduledJobImpl(ComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        super(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    /**
     * Provide the {@link OutboundConnectionTask}
     *
     * @return the used OutboundConnectionTask
     */
    public abstract ScheduledConnectionTask getConnectionTask();

    @Override
    public boolean isConnectedTo(OutboundConnectionTask connectionTask) {
        return getConnectionTask().equals(connectionTask);
    }

    @Override
    public boolean isConnected() {
        return getExecutionContext().getComPortRelatedComChannel() != null;
    }

    @Override
    protected ComPortRelatedComChannel findOrCreateComChannel(ConnectionTaskPropertyProvider propertyProvider) throws ConnectionException {
        return new ComPortRelatedComChannelImpl(
                getConnectionTask().connect(getComPort(), propertyProvider.getProperties()),
                getComPort(),
                getServiceProvider().clock(),
                getServiceProvider().deviceMessageService(),
                getServiceProvider().hexService(),
                getServiceProvider().eventPublisher());
    }

    @Override
    public boolean isWithinComWindow() {
        return this.isWithinComWindow(getComWindow());
    }

    private ComWindow getComWindow() {
        ComWindow comWindowToUse = this.getConnectionTask().getCommunicationWindow();

        Optional<ComTaskExecution> touComTaskExecution = getComTaskExecutions().stream()
                .filter(comTaskExecution -> getServiceProvider().touService().getCampaignOn(comTaskExecution).isPresent())
                .filter(comTaskExecution -> comTaskExecution.getComTask().getId() == getServiceProvider().touService().getCampaignOn(comTaskExecution).get().getCalendarUploadComTaskId())
                .findFirst();
        if (touComTaskExecution.isPresent()) {
            comWindowToUse = getServiceProvider().touService().getCampaignOn(touComTaskExecution.get()).get().getComWindow();
        }
        Optional<ComTaskExecution> firmwareComTaskExecution = getComTaskExecutions().stream().filter(ComTaskExecution::isFirmware)
                .filter(comTaskExecution -> getServiceProvider().firmwareService().getFirmwareCampaignService().getCampaignOn(comTaskExecution).isPresent())
                .filter(comTaskExecution -> comTaskExecution.getComTask().getId() == getServiceProvider().firmwareService().getFirmwareCampaignService().getCampaignOn(comTaskExecution).get().getFirmwareUploadComTaskId())
                .findFirst();
        if (firmwareComTaskExecution.isPresent()) {
            comWindowToUse =  getServiceProvider().firmwareService().getFirmwareCampaignService().getCampaignOn(firmwareComTaskExecution.get()).get().getComWindow();
        }
        return comWindowToUse;
    }

    private boolean isWithinComWindow(ComWindow comWindow) {
        if (comWindow == null) {
            return true;
        } else {
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            now.setTimeInMillis(getServiceProvider().clock().millis());
            return comWindow.includes(now);
        }
    }

    @Override
    public void rescheduleToNextComWindow() {
        this.createExecutionContext(false);

        if (getExecutionContext() != null) {
            this.getExecutionContext().getComSessionBuilder().incrementNotExecutedTasks(this.getComTaskExecutions().size());
            this.getExecutionContext().createJournalEntry(ComServer.LogLevel.INFO, "Rescheduling to next ComWindow because current timestamp is not " + getComWindow());
            this.getExecutionContext().getStoreCommand().add(new RescheduleToNextComWindow(this, getServiceProvider().firmwareService(), getServiceProvider().touService()));
            this.completeOutsideComWindow();
        } else {
            this.releaseToken();
        }
    }

    @Override
    public boolean isHighPriorityJob() {
        return false;
    }

    /**
     * Attempts to lock the specified {@link ComTaskExecution}
     * and returns <code>true</code> if the lock succeeded.
     * If the lock did not succeed, this is an indication
     * that another component has already locked it,
     * most likely for executing it.
     *
     * @param comTaskExecution The ComTaskExecution
     * @return A flag that indicates a successful locking of the ComTaskExecution
     */
    boolean attemptLock(ComTaskExecution comTaskExecution) {
        return this.getComServerDAO().attemptLock(comTaskExecution, this.getComPort());
    }

    /**
     * Attempts to lock the specified {@link PriorityComTaskExecutionLink}
     * and returns <code>true</code> if the lock succeeded.
     * If the lock did not succeed, this is an indication
     * that another component has already locked it,
     * most likely for executing it.
     *
     * @param comTaskExecution The HighPriorityComTaskExecution
     * @return A flag that indicates a successful locking of the HighPriorityComTaskExecution
     */
    protected boolean attemptLock(PriorityComTaskExecutionLink comTaskExecution) {
        return this.getComServerDAO().attemptLock(comTaskExecution, this.getComPort());
    }

    /**
     * Attempts to lock the specified {@link OutboundConnectionTask}
     * and returns <code>true</code> if the lock succeeded.
     * If the lock did not succeed, this is an indication
     * that another component has already locked it,
     * most likely for executing {@link ComTaskExecution}s against it.
     *
     * @param connectionTask The ConnectionTask
     * @return A flag that indicates a successful locking of the ConnectionTask
     */
    protected boolean attemptLock(ScheduledConnectionTask connectionTask) {
        return this.getComServerDAO().attemptLock(connectionTask, this.getComPort().getComServer()) != null;
    }

    protected void unlock(ScheduledConnectionTask connectionTask) {
        this.getComServerDAO().unlock(connectionTask);
    }

    public boolean establishConnectionFor(ComPort comPort) {
        /* Regular code */
        ExecutionContext executionContext = this.getExecutionContext();
        if (executionContext == null) {
            this.createExecutionContext(comPort);
            executionContext = this.getExecutionContext();
        }
        boolean success = executionContext.connect();

        if (success) {
            this.publish(new EstablishConnectionEvent(new ComServerEventServiceProvider(), this.getComPort(), this.getConnectionTask()));
            /* AspectJ - (Abstract)ComPortLogging */
            getExecutionContext().connectionLogger.connectionEstablished(getThreadName(), comPort.getName());
            /* AspectJ - OutboundConnectionEventPublisher */
        }

        /* Regular code */
        return success;
    }

    public void createExecutionContext() {
        this.createExecutionContext(true);
    }

    protected void createExecutionContext(boolean logConnectionProperties) {
        this.createExecutionContext(this.getComPort(), logConnectionProperties);
    }

    protected void createExecutionContext(ComPort comPort) {
        this.createExecutionContext(comPort, true);
    }

    private void createExecutionContext(ComPort comPort, boolean logConnectionProperties) {
        this.setExecutionContext(this.newExecutionContext(this.getConnectionTask(), comPort, logConnectionProperties));
    }

    protected void completeConnection() {
        if (getExecutionContext() != null && isConnected()) {
            try {
                this.getConnectionTask().disconnect(getExecutionContext().getComPortRelatedComChannel());
            } catch (ConnectionException e) {
                throw new ConnectionSetupException(e, MessageSeeds.DISCONNECT_FAILED);
            }
        }
    }

    private void publish(ComServerEvent event) {
        this.getServiceProvider().eventPublisher().publish(event);
    }

    public void appendStatisticalInformationToComSession() {
        ComSessionBuilder comSessionBuilder = getExecutionContext().getComSessionBuilder();
        comSessionBuilder.connectDuration(getExecutionContext().getElapsedTimeInMillis());
        comSessionBuilder.talkDuration(getExecutionContext().getComPortRelatedComChannel().talkTime());
        Counters sessionCounters = getExecutionContext().getComPortRelatedComChannel().getSessionCounters();
        comSessionBuilder.addSentBytes(sessionCounters.getBytesSent());
        comSessionBuilder.addReceivedBytes(sessionCounters.getBytesRead());
        comSessionBuilder.addSentPackets(sessionCounters.getPacketsSent());
        comSessionBuilder.addReceivedPackets(sessionCounters.getPacketsRead());
    }

    private class ComServerEventServiceProvider implements AbstractComServerEventImpl.ServiceProvider {
        @Override
        public Clock clock() {
            return getServiceProvider().clock();
        }

        @Override
        public DeviceMessageService deviceMessageService() {
            return getServiceProvider().deviceMessageService();
        }
    }


}