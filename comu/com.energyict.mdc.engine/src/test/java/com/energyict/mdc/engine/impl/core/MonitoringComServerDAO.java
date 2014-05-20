package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.impl.core.verification.CounterVerifier;
import com.energyict.mdc.engine.impl.tools.Counter;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.tasks.history.ComSession;
import com.energyict.mdc.tasks.history.ComSessionBuilder;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.transaction.Transaction;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides an implementation for the {@link ComServerDAO} interface
 * that merely monitors that calls, i.e. will count the number of
 * occurrences in much the same way as that a mock created
 * by the Mockito framework will do. It's verification behavior
 * is more flexible as it will verify and allow failures at first
 * and then wait a number of milli seconds and then verify again.
 * This is very useful in a multi threaded environment where
 * not all threads have equal priority or when the executing
 * build server has a lot of jobs to execute and some
 * of the threads have not actually executed the expected code.
 * <br>
 * This ComServerDAO is actually wrapping another ComServerDAO
 * that is most likely going to be a proper mock so that
 * you are able to determine the return results of this ComServerDAO.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-19 (13:03)
 */
public class MonitoringComServerDAO implements ComServerDAO {

    private ComServerDAO actual;
    private Counter getThisComServer = new Counter();
    private Counter getComServer = new Counter();
    private Counter refreshComServer = new Counter();
    private Counter refreshComPort = new Counter();
    private Counter findExecutableComTasks = new Counter();
    private Counter connectionTaskExecutionStarted = new Counter();
    private Counter connectionTaskExecutionCompleted = new Counter();
    private Counter connectionTaskExecutionFailed = new Counter();
    private Counter comTaskExecutionStarted = new Counter();
    private Counter comTaskExecutionCompleted = new Counter();
    private Counter executionFailed = new Counter();

    public MonitoringComServerDAO (ComServerDAO actual) {
        super();
        this.actual = actual;
    }

    public ComServerDAO verify (CounterVerifier verifier) {
        return new VerifingComServerDAO(verifier);
    }

    @Override
    public ComServer getThisComServer () {
        this.getThisComServer.increment();
        return this.actual.getThisComServer();
    }

    @Override
    public ComServer getComServer (String hostName) {
        this.getComServer.increment();
        return this.actual.getComServer(hostName);
    }

    @Override
    public ComServer refreshComServer (ComServer comServer) {
        this.refreshComServer.increment();
        return this.actual.refreshComServer(comServer);
    }

    @Override
    public ComPort refreshComPort (ComPort comPort) {
        this.refreshComPort.increment();
        return this.actual.refreshComPort(comPort);
    }

    @Override
    public List<ComJob> findExecutableOutboundComTasks (OutboundComPort comPort) {
        this.findExecutableComTasks.increment();
        return this.actual.findExecutableOutboundComTasks(comPort);
    }

    @Override
    public List<ComTaskExecution> findExecutableInboundComTasks (OfflineDevice device, InboundComPort comPort) {
        return null;
    }

    @Override
    public OfflineDevice findDevice (DeviceIdentifier identifier) {
        return null;
    }

    @Override
    public OfflineRegister findRegister(RegisterIdentifier identifier) {
        return null;
    }

//    @Override
//    public OfflineDeviceMessage findDeviceMessage(MessageIdentifier identifier) {
//        return null;
//    }

    @Override
    public ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, ComServer comServer) {
        return this.actual.attemptLock(connectionTask, comServer);
    }

    @Override
    public void unlock (ScheduledConnectionTask connectionTask) {
        this.actual.unlock(connectionTask);
    }

    @Override
    public boolean attemptLock (ComTaskExecution comTaskExecution, ComPort comPort) {
        return this.actual.attemptLock(comTaskExecution, comPort);
    }

    @Override
    public void unlock (ComTaskExecution comTaskExecution) {
        this.actual.unlock(comTaskExecution);
    }

    @Override
    public void executionStarted (ConnectionTask connectionTask, ComServer comServer) {
        this.connectionTaskExecutionStarted.increment();
        this.actual.executionStarted(connectionTask, comServer);
    }

    @Override
    public void executionCompleted (ConnectionTask connectionTask) {
        this.connectionTaskExecutionCompleted.increment();
        this.actual.executionCompleted(connectionTask);
    }

    @Override
    public void executionFailed (ConnectionTask connectionTask) {
        this.connectionTaskExecutionFailed.increment();
        this.actual.executionFailed(connectionTask);
    }

    @Override
    public void executionStarted (ComTaskExecution comTaskExecution, ComPort comPort) {
        this.comTaskExecutionStarted.increment();
        this.actual.executionStarted(comTaskExecution, comPort);
    }

    @Override
    public void executionCompleted (ComTaskExecution comTaskExecution) {
        this.comTaskExecutionCompleted.increment();
        this.actual.executionCompleted(comTaskExecution);
    }

    @Override
    public void executionCompleted (List<? extends ComTaskExecution> comTaskExecutions) {
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            this.executionCompleted(comTaskExecution);
        }
    }

    @Override
    public void executionFailed (ComTaskExecution comTaskExecution) {
        this.executionFailed.increment();
        this.actual.executionFailed(comTaskExecution);
    }

    @Override
    public void executionFailed (List<? extends ComTaskExecution> comTaskExecutions) {
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            this.executionFailed(comTaskExecution);
        }
    }

    @Override
    public boolean isStillPending (long comTaskExecutionId) {
        return this.actual.isStillPending(comTaskExecutionId);
    }

    @Override
    public boolean areStillPending (Collection<Long> comTaskExecutionIds) {
        return this.actual.areStillPending(comTaskExecutionIds);
    }

    @Override
    public void setMaxNumberOfTries(ScheduledConnectionTask connectionTask, int maxNumberOfTries) {
        this.actual.setMaxNumberOfTries(connectionTask, maxNumberOfTries);
    }

    @Override
    public <T> T executeTransaction(Transaction<T> transaction) {
        return this.actual.executeTransaction(transaction);
    }

    @Override
    public ComSession createComSession(ComSessionBuilder builder, ComSession.SuccessIndicator successIndicator) {
        return this.actual.createComSession(builder, successIndicator);
    }

    private class VerifingComServerDAO implements ComServerDAO {
        private CounterVerifier verifier;

        private VerifingComServerDAO (CounterVerifier verifier) {
            super();
            this.verifier = verifier;
        }

        @Override
        public ComServer getThisComServer () {
            this.verifier.verify(getThisComServer);
            return null;
        }

        @Override
        public ComServer getComServer (String hostName) {
            this.verifier.verify(getComServer);
            return null;
        }

        @Override
        public ComServer refreshComServer (ComServer comServer) {
            this.verifier.verify(refreshComServer);
            return null;
        }

        @Override
        public ComPort refreshComPort (ComPort comPort) {
            this.verifier.verify(refreshComPort);
            return null;
        }

        @Override
        public List<ComJob> findExecutableOutboundComTasks (OutboundComPort comPort) {
            this.verifier.verify(findExecutableComTasks);
            return null;
        }

        @Override
        public void setMaxNumberOfTries(ScheduledConnectionTask connectionTask, int maxNumberOfTries) {
        }

        @Override
        public <T> T executeTransaction(Transaction<T> transaction) {
            return null;
        }

        @Override
        public List<ComTaskExecution> findExecutableInboundComTasks (OfflineDevice device, InboundComPort comPort) {
            return null;
        }

        @Override
        public OfflineDevice findDevice (DeviceIdentifier identifier) {
            return null;
        }

        @Override
        public OfflineRegister findRegister(RegisterIdentifier identifier) {
            return null;
        }

//        @Override
//        public OfflineDeviceMessage findDeviceMessage(MessageIdentifier identifier) {
//            return null;
//        }

        @Override
        public ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, ComServer comServer) {
            return connectionTask;
        }

        @Override
        public void unlock (ScheduledConnectionTask connectionTask) {
            // No implementation required so far
        }

        @Override
        public boolean attemptLock (ComTaskExecution comTaskExecution, ComPort comPort) {
            return true;
        }

        @Override
        public void unlock (ComTaskExecution comTaskExecution) {
            // No implementation required so far
        }

        @Override
        public void executionStarted (ConnectionTask connectionTask, ComServer comServer) {
            this.verifier.verify(connectionTaskExecutionStarted);
        }

        @Override
        public void executionCompleted (ConnectionTask connectionTask) {
            this.verifier.verify(connectionTaskExecutionCompleted);
        }

        @Override
        public void executionFailed (ConnectionTask connectionTask) {
            this.verifier.verify(connectionTaskExecutionFailed);
        }

        @Override
        public void executionStarted (ComTaskExecution comTaskExecution, ComPort comPort) {
            this.verifier.verify(comTaskExecutionStarted);
        }

        @Override
        public void executionCompleted (ComTaskExecution comTaskExecution) {
            this.verifier.verify(comTaskExecutionCompleted);
        }

        @Override
        public void executionCompleted (List<? extends ComTaskExecution> comTaskExecutions) {
            for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                this.executionCompleted(comTaskExecution);
            }
        }

        @Override
        public void executionFailed (ComTaskExecution comTaskExecution) {
            this.verifier.verify(executionFailed);
        }

        @Override
        public void executionFailed (List<? extends ComTaskExecution> comTaskExecutions) {
            for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                this.executionFailed(comTaskExecution);
            }
        }

        @Override
        public void releaseInterruptedTasks (ComServer comServer) {
            // No implementation required
        }

        @Override
        public TimeDuration releaseTimedOutTasks (ComServer comServer) {
            // No implementation required
            return new TimeDuration(1, TimeDuration.DAYS);
        }

        @Override
        public void storeMeterReadings(DeviceIdentifier deviceIdentifier, MeterReading meterReading) {
            // Not storing readings in mock mode
        }


        @Override
        public void updateIpAddress (String ipAddress, ConnectionTask connectionTask, String connectionTaskPropertyName) {
            // No implementation required
        }

        @Override
        public void storeConfigurationFile (DeviceIdentifier deviceIdentifier, DateFormat timeStampFormat, String fileExtension, byte[] contents) {
            // No implementation required
        }

        @Override
        public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(DeviceIdentifier deviceIdentifier, int confirmationCount) {
            // No implementation required
            return new ArrayList<>(0);
        }

        @Override
        public List<SecurityProperty> getDeviceProtocolSecurityProperties (DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
            // No implementation required
            return null;
        }

        @Override
        public TypedProperties getDeviceConnectionTypeProperties (DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
            // No implementation required
            return null;
        }

        @Override
        public TypedProperties getDeviceProtocolProperties (DeviceIdentifier deviceIdentifier) {
            // No implementation required
            return null;
        }

        @Override
        public void updateGateway(DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier) {
            // No implementation required
        }

        @Override
        public void signalEvent(String topic, Object source) {
            // No implementation required
        }

        @Override
        public void updateDeviceMessageInformation(MessageIdentifier messageIdentifier, DeviceMessageStatus newDeviceMessageStatus, String protocolInformation) {
            // nothing to update
        }

        @Override
        public boolean isStillPending (long comTaskExecutionId) {
            return false;
        }

        @Override
        public boolean areStillPending (Collection<Long> comTaskExecutionIds) {
            return false;
        }

        @Override
        public ServerProcessStatus getStatus () {
            return null;
        }

        @Override
        public void start () {
        }

        @Override
        public void shutdown () {
        }

        @Override
        public void shutdownImmediate () {
        }

        @Override
        public ComSession createComSession(ComSessionBuilder builder, ComSession.SuccessIndicator successIndicator) {
            return null;
        }
    }

    @Override
    public void releaseInterruptedTasks (ComServer comServer) {
        // No need to release when in monitoring mode
    }

    @Override
    public TimeDuration releaseTimedOutTasks (ComServer comServer) {
        // No need to release when in monitoring mode
        return new TimeDuration(1, TimeDuration.DAYS);
    }

    @Override
    public void storeMeterReadings(DeviceIdentifier deviceIdentifier, MeterReading meterReading) {
        // Not storing readings in mock mode
    }

    @Override
    public void updateIpAddress (String ipAddress, ConnectionTask connectionTask, String connectionTaskPropertyName) {
        // Not updating device ip address in monitoring mode
    }

    @Override
    public void storeConfigurationFile (DeviceIdentifier deviceIdentifier, DateFormat timeStampFormat, String fileExtension, byte[] contents) {
        // Not storing configuration files in monitoring mode
    }

    @Override
    public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(DeviceIdentifier deviceIdentifier, int confirmationCount) {
        return new ArrayList<>(0);
    }

    @Override
    public List<SecurityProperty> getDeviceProtocolSecurityProperties (DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        // No support for inbound communication in monitoring mode
        return null;
    }

    @Override
    public TypedProperties getDeviceConnectionTypeProperties (DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        // No support for inbound communication in monitoring mode
        return null;
    }

    @Override
    public TypedProperties getDeviceProtocolProperties (DeviceIdentifier deviceIdentifier) {
        // No support for inbound communication in monitoring mode
        return null;
    }

    @Override
    public void updateGateway(DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier) {
        // Not updating the gateway device in monitor mode
    }

    @Override
    public void signalEvent(String topic, Object source) {
        // Not signaling events in monitor mode
    }

    @Override
    public void updateDeviceMessageInformation(MessageIdentifier messageIdentifier, DeviceMessageStatus newDeviceMessageStatus, String protocolInformation) {
        // nothing to update
    }

    @Override
    public ServerProcessStatus getStatus () {
        return null;
    }

    @Override
    public void start () {
    }

    @Override
    public void shutdown () {
    }

    @Override
    public void shutdownImmediate () {
    }

}