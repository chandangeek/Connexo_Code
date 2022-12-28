/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.OutboundCapableComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskProperty;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.LookupEntry;
import com.energyict.mdc.engine.impl.PropertyValueType;
import com.energyict.mdc.engine.impl.core.remote.DeviceProtocolCacheXmlWrapper;
import com.energyict.mdc.engine.impl.core.verification.CounterVerifier;
import com.energyict.mdc.engine.impl.tools.Counter;
import com.energyict.mdc.engine.users.OfflineUserInfo;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCertificateWrapper;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.G3TopologyDeviceAddressInformation;
import com.energyict.mdc.upl.meterdata.TopologyNeighbour;
import com.energyict.mdc.upl.meterdata.TopologyPathSegment;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineDeviceContext;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLogBook;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.security.CertificateWrapper;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private Counter connectionTaskExecutionRescheduled = new Counter();
    private Counter comTaskExecutionStarted = new Counter();
    private Counter comTaskExecutionCompleted = new Counter();
    private Counter executionFailed = new Counter();

    public MonitoringComServerDAO(ComServerDAO actual) {
        super();
        this.actual = actual;
    }

    public ComServerDAO verify(CounterVerifier verifier) {
        return new VerifyingComServerDAO(verifier);
    }

    @Override
    public ComServer getThisComServer() {
        this.getThisComServer.increment();
        return this.actual.getThisComServer();
    }

    @Override
    public ComServer getComServer(String hostName) {
        this.getComServer.increment();
        return this.actual.getComServer(hostName);
    }

    @Override
    public ComServer refreshComServer(ComServer comServer) {
        this.refreshComServer.increment();
        return this.actual.refreshComServer(comServer);
    }

    public List<OfflineUserInfo> getUsersCredentialInformation() {
        return Collections.emptyList();
    }

    @Override
    public ComPort refreshComPort(ComPort comPort) {
        this.refreshComPort.increment();
        return this.actual.refreshComPort(comPort);
    }

    @Override
    public List<ComJob> findPendingOutboundComTasks(OutboundComPort comPort) {
        this.findExecutableComTasks.increment();
        return this.actual.findExecutableOutboundComTasks(comPort);
    }

    @Override
    public List<ComJob> findExecutableOutboundComTasks(OutboundComPort comPort) {
        this.findExecutableComTasks.increment();
        return this.actual.findExecutableOutboundComTasks(comPort);
    }

    @Override
    public List<HighPriorityComJob> findExecutableHighPriorityOutboundComTasks(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool) {
        return null;
    }

    @Override
    public List<HighPriorityComJob> findExecutableHighPriorityOutboundComTasks(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool, Instant date) {
        return null;
    }

    @Override
    public List<ComTaskExecution> findExecutableInboundComTasks(OfflineDevice device, InboundComPort comPort) {
        return null;
    }

    @Override
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier) {
        return Optional.empty();
    }

    @Override
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier, OfflineDeviceContext offlineDeviceContext) {
        return actual.findOfflineDevice(identifier, offlineDeviceContext);
    }


    @Override
    public Optional<OfflineRegister> findOfflineRegister(RegisterIdentifier identifier, Instant when) {
        return Optional.empty();
    }

    @Override
    public Boolean getInboundComTaskOnHold(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        return null;
    }

    @Override
    public List<LookupEntry> getCompletionCodeLookupEntries() {
        return null;
    }

    @Override
    public Optional<OfflineLoadProfile> findOfflineLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        return Optional.empty();
    }

    @Override
    public Optional<OfflineLogBook> findOfflineLogBook(LogBookIdentifier logBookIdentifier) {
        return Optional.empty();
    }

    @Override
    public Optional<OfflineDeviceMessage> findOfflineDeviceMessage(MessageIdentifier identifier) {
        return Optional.empty();
    }

    @Override
    public List<ConnectionTaskProperty> findProperties(ConnectionTask connectionTask) {
        return actual.findProperties(connectionTask);
    }

    @Override
    public List<DeviceMasterDataExtractor.SecurityProperty> getPropertiesFromSecurityPropertySet(DeviceIdentifier deviceIdentifier, Long securityPropertySetId) {
        return Collections.emptyList();
    }

    @Override
    public ComTaskEnablement findComTaskEnablementByDeviceAndComTask(DeviceIdentifier deviceIdentifier, long comTaskId) {
        return null;
    }

    @Override
    public List<SecurityPropertySet> findAllSecurityPropertySetsForDevice(DeviceIdentifier deviceIdentifier) {
        return Collections.emptyList();
    }

    @Override
    public TypedProperties findProtocolDialectPropertiesFor(long comTaskExecutionId) {
        return null;
    }

    @Override
    public ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, ComPort comPort) {
        return actual.attemptLock(connectionTask, comPort);
    }

    @Override
    public boolean attemptLock(OutboundConnectionTask connectionTask, ComPort comPort) {
        return actual.attemptLock(connectionTask, comPort);
    }

    @Override
    public void unlock(OutboundConnectionTask connectionTask) {
        actual.unlock(connectionTask);
    }

    @Override
    public boolean attemptLock(ComTaskExecution comTaskExecution, ComPort comPort) {
        return actual.attemptLock(comTaskExecution, comPort);
    }

    @Override
    public boolean attemptLock(PriorityComTaskExecutionLink priorityComTaskExecutionLink, ComPort comPort) {
        return actual.attemptLock(priorityComTaskExecutionLink, comPort);
    }

    @Override
    public void unlock(ComTaskExecution comTaskExecution) {
        actual.unlock(comTaskExecution);
    }

    @Override
    public ConnectionTask<?, ?> executionStarted(ConnectionTask connectionTask, ComPort comPort) {
        connectionTaskExecutionStarted.increment();
        actual.executionStarted(connectionTask, comPort);
        return connectionTask;
    }

    @Override
    public ConnectionTask<?, ?> executionCompleted(ConnectionTask connectionTask) {
        connectionTaskExecutionCompleted.increment();
        actual.executionCompleted(connectionTask);
        return connectionTask;
    }

    @Override
    public ConnectionTask<?, ?> executionFailed(ConnectionTask connectionTask) {
        connectionTaskExecutionFailed.increment();
        actual.executionFailed(connectionTask);
        return connectionTask;
    }

    @Override
    public ConnectionTask<?, ?> executionRescheduled(ConnectionTask connectionTask) {
        this.connectionTaskExecutionRescheduled.increment();
        this.actual.executionRescheduled(connectionTask);
        return connectionTask;
    }

    @Override
    public void executionStarted(ComTaskExecution comTaskExecution, ComPort comPort, boolean executeInTransaction) {
        this.comTaskExecutionStarted.increment();
        this.actual.executionStarted(comTaskExecution, comPort, true);
    }

    @Override
    public void executionCompleted(ComTaskExecution comTaskExecution) {
        this.comTaskExecutionCompleted.increment();
        this.actual.executionCompleted(comTaskExecution);
    }

    @Override
    public void executionRescheduled(ComTaskExecution comTaskExecution, Instant rescheduleDate) {
        this.comTaskExecutionCompleted.increment();
        this.actual.executionCompleted(comTaskExecution);
    }

    @Override
    public void executionRescheduledToComWindow(ComTaskExecution comTaskExecution, Instant comWindowStartDate) {
        this.comTaskExecutionCompleted.increment();
        this.actual.executionCompleted(comTaskExecution);
    }

    @Override
    public void executionCompleted(List<? extends ComTaskExecution> comTaskExecutions) {
        comTaskExecutions.forEach(this::executionCompleted);
    }

    @Override
    public void executionFailed(ComTaskExecution comTaskExecution) {
        this.executionFailed.increment();
        this.actual.executionFailed(comTaskExecution);
    }

    @Override
    public void executionFailed(ComTaskExecution comTaskExecution, boolean noRetry) {
        executionFailed(comTaskExecution);
    }

    @Override
    public void executionFailed(List<? extends ComTaskExecution> comTaskExecutions) {
        comTaskExecutions.forEach(this::executionFailed);
    }

    @Override
    public boolean isStillPending(long comTaskExecutionId) {
        return this.actual.isStillPending(comTaskExecutionId);
    }

    @Override
    public boolean areStillPending(Collection<Long> comTaskExecutionIds) {
        return this.actual.areStillPending(comTaskExecutionIds);
    }

    @Override
    public boolean areStillPendingWithHighPriority(Collection<Long> priorityComTaskExecutionLinkIds) {
        return false;
    }

    @Override
    public <T> T executeTransaction(Transaction<T> transaction) {
        return this.actual.executeTransaction(transaction);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifierFor(LoadProfileIdentifier loadProfileIdentifier) {
        return this.actual.getDeviceIdentifierFor(loadProfileIdentifier);
    }

    @Override
    public PropertyValueType getDeviceProtocolPropertyValueType(DeviceIdentifier deviceIdentifier, String propertyName) {
        return actual.getDeviceProtocolPropertyValueType(deviceIdentifier, propertyName);
    }

    @Override
    public void updateDeviceDialectProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        actual.updateDeviceDialectProperty(deviceIdentifier, propertyName, propertyValue);
    }

    @Override
    public void addTrustedCertificates(List<CollectedCertificateWrapper> collectedCertificates) {
        actual.addTrustedCertificates(collectedCertificates);
    }

    @Override
    public void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        actual.updateDeviceSecurityProperty(deviceIdentifier, propertyName, propertyValue);
    }

    @Override
    public void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue, ComTaskExecution comTaskExecution) {
        actual.updateDeviceSecurityProperty(deviceIdentifier, propertyName, propertyValue, comTaskExecution);
    }

    @Override
    public void activateSecurityAccessorPassiveValue(DeviceIdentifier deviceIdentifier, String propertyName, ComTaskExecution comTaskExecution) {
        actual.activateSecurityAccessorPassiveValue(deviceIdentifier, propertyName, comTaskExecution);
    }

    @Override
    public void addCACertificate(CertificateWrapper certificateWrapper) {
        actual.addCACertificate(certificateWrapper);
    }

    @Override
    public long addEndDeviceCertificate(CollectedCertificateWrapper collectedCertificateWrapper) {
        return actual.addEndDeviceCertificate(collectedCertificateWrapper);
    }

    @Override
    public Optional<Device> getDeviceFor(DeviceIdentifier deviceIdentifier) {
        return actual.getDeviceFor(deviceIdentifier);
    }

    @Override
    public List<Device> getAllDevicesFor(DeviceIdentifier deviceIdentifier) {
        return actual.getAllDevicesFor(deviceIdentifier);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifierFor(LogBookIdentifier logBookIdentifier) {
        return this.actual.getDeviceIdentifierFor(logBookIdentifier);
    }

    @Override
    public void updateLastReadingFor(LoadProfileIdentifier loadProfileIdentifier, Instant lastReading) {
        this.actual.updateLastReadingFor(loadProfileIdentifier, lastReading);
    }

    @Override
    public void updateLastLogBook(LogBookIdentifier logBookIdentifier, Instant lastLogBook) {
        this.actual.updateLastLogBook(logBookIdentifier, lastLogBook);
    }

    @Override
    public void updateLastDataSourceReadingsFor(Map<LoadProfileIdentifier, Instant> lastReadings, Map<LogBookIdentifier, Instant> lastLogBooks) {
        this.actual.updateLastDataSourceReadingsFor(lastReadings, lastLogBooks);
    }

    @Override
    public void storePathSegments(List<TopologyPathSegment> topologyPathSegment) {
        this.actual.storePathSegments(topologyPathSegment);
    }

    @Override
    public void storeNeighbours(DeviceIdentifier sourceDeviceIdentifier, List<TopologyNeighbour> topologyNeighbours) {
        this.actual.storeNeighbours(sourceDeviceIdentifier, topologyNeighbours);
    }

    @Override
    public void storeG3IdentificationInformation(G3TopologyDeviceAddressInformation topologyDeviceAddressInformation) {
        this.actual.storeG3IdentificationInformation(topologyDeviceAddressInformation);
    }

    @Override
    public void updateFirmwareVersions(CollectedFirmwareVersion collectedFirmwareVersions) {

    }

    @Override
    public void updateBreakerStatus(CollectedBreakerStatus collectedBreakerStatus, boolean registerUpdateRequired, boolean tableUpdateRequired) {

    }

    public void updateCreditAmount(CollectedCreditAmount collectedBreakerStatus, boolean registerUpdateRequired, boolean tableUpdateRequired) {

    }

    @Override
    public void updateDeviceCSR(DeviceIdentifier deviceIdentifier, String certificateType, String csr) {

    }

    @Override
    public void updateCalendars(CollectedCalendar collectedCalendar) {
    }

    @Override
    public void cleanupOutdatedComTaskExecutionTriggers() {
    }

    @Override
    public ComSession createComSession(ComSessionBuilder builder, Instant stopDate, ComSession.SuccessIndicator successIndicator) {
        return this.actual.createComSession(builder, stopDate, successIndicator);
    }

    @Override
    public void releaseInterruptedTasks(ComPort comPort) {
        // No need to release when in monitoring mode
    }

    @Override
    public TimeDuration releaseTimedOutTasks(ComPort comPort) {
        // No need to release when in monitoring mode
        return new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
    }

    @Override
    public void releaseTasksFor(ComPort comPort) {
        // No implementation required
    }

    @Override
    public void createOrUpdateDeviceCache(DeviceIdentifier deviceIdentifier, DeviceProtocolCacheXmlWrapper cache) {
    }

    @Override
    public void storeMeterReadings(DeviceIdentifier deviceIdentifier, MeterReading meterReading) {
    }

    @Override
    public void storeLoadProfile(LoadProfileIdentifier loadProfileIdentifier, CollectedLoadProfile collectedLoadProfile, Instant currentDate) {
    }

    @Override
    public void storeLogBookData(LogBookIdentifier logBookIdentifier, CollectedLogBook collectedLogBook, Instant currentDate) {
    }

    @Override
    public void updateLogBookLastReading(LogBookIdentifier logBookIdentifier, Date lastExecutionStartTimestamp) {
    }

    @Override
    public void updateLogBookLastReadingFromTask(final LogBookIdentifier logBookIdentifier, final long comTaskExecutionId) {
        // Not updating logbook last reading in monitoring mode
    }

    @Override
    public void updateConnectionTaskProperty(Object propertyValue, ConnectionTask connectionTask, String connectionTaskPropertyName) {
        // Not updating device ip address in monitoring mode
    }

    @Override
    public void updateConnectionTaskProperties(ConnectionTask connectionTask, Map<String, Object> connectionPropertyNameAndValue) {
    }

    @Override
    public void updateDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {

    }

    @Override
    public void storeConfigurationFile(DeviceIdentifier deviceIdentifier, DateTimeFormatter timeStampFormat, String fileName, String fileExtension, byte[] contents) {
        // Not storing configuration files in monitoring mode
    }

    @Override
    public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(DeviceIdentifier deviceIdentifier, int confirmationCount) {
        return new ArrayList<>(0);
    }

    @Override
    public DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        return null;
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceProtocolSecurityProperties(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
        return null;
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceDialectProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        return null;
    }

    @Override
    public TypedProperties getDeviceConnectionTypeProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        // No support for inbound communication in monitoring mode
        return TypedProperties.empty();
    }

    @Override
    public TypedProperties getOutboundConnectionTypeProperties(DeviceIdentifier deviceIdentifier) {
        return TypedProperties.empty();
    }

    @Override
    public TypedProperties getDeviceProtocolProperties(DeviceIdentifier deviceIdentifier) {
        // No support for inbound communication in monitoring mode
        return TypedProperties.empty();
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceLocalProtocolProperties(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public com.energyict.mdc.upl.offline.OfflineDevice getOfflineDevice(DeviceIdentifier deviceIdentifier, OfflineDeviceContext context) {
        return null;
    }

    @Override
    public String getDeviceProtocolClassName(DeviceIdentifier identifier) {
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
    public void updateDeviceMessageInformation(DeviceMessage deviceMessage, DeviceMessageStatus newDeviceMessageStatus, Instant sentDate, String protocolInformation) {
        // nothing to update
    }

    @Override
    public void updateDeviceMessageInformation(MessageIdentifier messageIdentifier, DeviceMessageStatus newDeviceMessageStatus, Instant sentDate, String protocolInformation) {
        // nothing to update
    }

    @Override
    public ServerProcessStatus getStatus() {
        return null;
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void shutdownImmediate() {
    }

    @Override
    public List<Pair<OfflineLoadProfile, Range<Instant>>> getStorageLoadProfileIdentifiers(OfflineLoadProfile loadProfile, String readingTypeMRID, Range<Instant> dataPeriod) {
        return this.actual.getStorageLoadProfileIdentifiers(loadProfile, readingTypeMRID, dataPeriod);
    }

    @Override
    public User getComServerUser() {
        return this.actual.getComServerUser();
    }

    @Override
    public Map<OfflineDeviceMessage, DeviceMessage> lockDeviceMessages(Collection<OfflineDeviceMessage> offlineDeviceMessages) {
        return null;
    }

    @Override
    public List<Long> findContainingActiveComPortPoolsForComPort(OutboundComPort comPort) {
        return Collections.emptyList();
    }

    @Override
    public void updateUmiwanStructure(ComTaskExecution comTaskExecution, Map<String, Object> properties, String cas) {

    }

    private class VerifyingComServerDAO implements ComServerDAO {
        private CounterVerifier verifier;

        private VerifyingComServerDAO(CounterVerifier verifier) {
            super();
            this.verifier = verifier;
        }

        @Override
        public ComServer getThisComServer() {
            this.verifier.verify(getThisComServer);
            return null;
        }

        @Override
        public ComServer getComServer(String hostName) {
            this.verifier.verify(getComServer);
            return null;
        }

        @Override
        public ComServer refreshComServer(ComServer comServer) {
            this.verifier.verify(refreshComServer);
            return null;
        }

        public List<OfflineUserInfo> getUsersCredentialInformation() {
            return Collections.emptyList();
        }

        @Override
        public ComPort refreshComPort(ComPort comPort) {
            this.verifier.verify(refreshComPort);
            return null;
        }

        @Override
        public List<ComJob> findPendingOutboundComTasks(OutboundComPort comPort) {
            this.verifier.verify(findExecutableComTasks);
            return null;
        }

        @Override
        public List<ComJob> findExecutableOutboundComTasks(OutboundComPort comPort) {
            this.verifier.verify(findExecutableComTasks);
            return null;
        }

        @Override
        public List<HighPriorityComJob> findExecutableHighPriorityOutboundComTasks(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool) {
            return null;
        }

        @Override
        public List<HighPriorityComJob> findExecutableHighPriorityOutboundComTasks(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool, Instant date) {
            return null;
        }

        @Override
        public <T> T executeTransaction(Transaction<T> transaction) {
            return null;
        }

        @Override
        public DeviceIdentifier getDeviceIdentifierFor(LoadProfileIdentifier loadProfileIdentifier) {
            return null;
        }

        @Override
        public DeviceIdentifier getDeviceIdentifierFor(LogBookIdentifier logBookIdentifier) {
            return null;
        }

        @Override
        public void updateLastReadingFor(LoadProfileIdentifier loadProfileIdentifier, Instant lastReading) {
        }

        @Override
        public void updateLastLogBook(LogBookIdentifier logBookIdentifier, Instant lastLogBook) {
        }

        @Override
        public void updateLastDataSourceReadingsFor(Map<LoadProfileIdentifier, Instant> lastReadings, Map<LogBookIdentifier, Instant> lastLogBooks) {
        }

        @Override
        public void storePathSegments(List<TopologyPathSegment> topologyPathSegment) {
        }

        @Override
        public void storeNeighbours(DeviceIdentifier sourceDeviceIdentifier, List<TopologyNeighbour> topologyNeighbours) {
        }

        @Override
        public void storeG3IdentificationInformation(G3TopologyDeviceAddressInformation topologyDeviceAddressInformation) {
        }

        @Override
        public void updateFirmwareVersions(CollectedFirmwareVersion collectedFirmwareVersions) {
        }

        @Override
        public void updateBreakerStatus(CollectedBreakerStatus collectedBreakerStatus, boolean registerUpdateRequired, boolean tableUpdateRequired) {
        }

        public void updateCreditAmount(CollectedCreditAmount collectedBreakerStatus, boolean registerUpdateRequired, boolean tableUpdateRequired) {
        }

        @Override
        public void updateDeviceCSR(DeviceIdentifier deviceIdentifier, String certificateType, String csr) {
        }

        @Override
        public void updateCalendars(CollectedCalendar collectedCalendar) {
        }

        @Override
        public Boolean getInboundComTaskOnHold(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
            return null;
        }

        @Override
        public List<LookupEntry> getCompletionCodeLookupEntries() {
            return null;
        }

        @Override
        public void cleanupOutdatedComTaskExecutionTriggers() {

        }

        @Override
        public List<ComTaskExecution> findExecutableInboundComTasks(OfflineDevice device, InboundComPort comPort) {
            return null;
        }

        @Override
        public PropertyValueType getDeviceProtocolPropertyValueType(DeviceIdentifier deviceIdentifier, String propertyName) {
            return null;
        }

        @Override
        public void updateDeviceDialectProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        }

        @Override
        public void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue, ComTaskExecution comTaskExecution) {
        }

        @Override
        public void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        }

        @Override
        public void addTrustedCertificates(List<CollectedCertificateWrapper> collectedCertificates) {
        }

        @Override
        public void activateSecurityAccessorPassiveValue(DeviceIdentifier deviceIdentifier, String propertyName, ComTaskExecution comTaskExecution) {
        }

        @Override
        public void addCACertificate(CertificateWrapper certificateWrapper) {

        }

        @Override
        public long addEndDeviceCertificate(CollectedCertificateWrapper collectedCertificateWrapper) {
            return 0;
        }

        @Override
        public Optional<Device> getDeviceFor(DeviceIdentifier deviceIdentifier) {
            return null;
        }

        @Override
        public List<Device> getAllDevicesFor(DeviceIdentifier deviceIdentifier) {
            return null;
        }

        @Override
        public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier) {
            return Optional.empty();
        }

        @Override
        public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier, OfflineDeviceContext offlineDeviceContext) {
            return actual.findOfflineDevice(identifier, offlineDeviceContext);
        }

        @Override
        public Optional<OfflineRegister> findOfflineRegister(RegisterIdentifier identifier, Instant when) {
            return Optional.empty();
        }

        @Override
        public Optional<OfflineLoadProfile> findOfflineLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
            return Optional.empty();
        }

        @Override
        public Optional<OfflineLogBook> findOfflineLogBook(LogBookIdentifier logBookIdentifier) {
            return Optional.empty();
        }

        @Override
        public Optional<OfflineDeviceMessage> findOfflineDeviceMessage(MessageIdentifier identifier) {
            return Optional.empty();
        }

        @Override
        public List<ConnectionTaskProperty> findProperties(ConnectionTask connectionTask) {
            return null;
        }

        @Override
        public List<DeviceMasterDataExtractor.SecurityProperty> getPropertiesFromSecurityPropertySet(DeviceIdentifier deviceIdentifier, Long securityPropertySetId) {
            return Collections.emptyList();
        }

        @Override
        public ComTaskEnablement findComTaskEnablementByDeviceAndComTask(DeviceIdentifier deviceIdentifier, long comTaskId) {
            return null;
        }

        @Override
        public List<SecurityPropertySet> findAllSecurityPropertySetsForDevice(DeviceIdentifier deviceIdentifier) {
            return Collections.emptyList();
        }

        @Override
        public TypedProperties findProtocolDialectPropertiesFor(long comTaskExecutionId) {
            return null;
        }

        @Override
        public ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, ComPort comPort) {
            return connectionTask;
        }

        @Override
        public boolean attemptLock(OutboundConnectionTask connectionTask, ComPort comPort) {
            return true;
        }

        @Override
        public void unlock(OutboundConnectionTask connectionTask) {
            // No implementation required so far
        }

        @Override
        public boolean attemptLock(ComTaskExecution comTaskExecution, ComPort comPort) {
            return true;
        }

        @Override
        public boolean attemptLock(PriorityComTaskExecutionLink comTaskExecution, ComPort comPort) {
            return true;
        }

        @Override
        public void unlock(ComTaskExecution comTaskExecution) {
            // No implementation required so far
        }

        @Override
        public ConnectionTask<?, ?> executionStarted(ConnectionTask connectionTask, ComPort comPort) {
            verifier.verify(connectionTaskExecutionStarted);
            return connectionTask;
        }

        @Override
        public ConnectionTask<?, ?> executionCompleted(ConnectionTask connectionTask) {
            this.verifier.verify(connectionTaskExecutionCompleted);
            return connectionTask;
        }

        @Override
        public ConnectionTask<?, ?> executionFailed(ConnectionTask connectionTask) {
            this.verifier.verify(connectionTaskExecutionFailed);
            return connectionTask;
        }

        @Override
        public ConnectionTask<?, ?> executionRescheduled(ConnectionTask connectionTask) {
            this.verifier.verify(connectionTaskExecutionRescheduled);
            return connectionTask;
        }

        @Override
        public void executionStarted(ComTaskExecution comTaskExecution, ComPort comPort, boolean executeInTransaction) {
            this.verifier.verify(comTaskExecutionStarted);
        }

        @Override
        public void executionCompleted(ComTaskExecution comTaskExecution) {
            this.verifier.verify(comTaskExecutionCompleted);
        }

        @Override
        public void executionRescheduled(ComTaskExecution comTaskExecution, Instant rescheduleDate) {

        }

        @Override
        public void executionRescheduledToComWindow(ComTaskExecution comTaskExecution, Instant comWindowStartDate) {

        }

        @Override
        public void executionCompleted(List<? extends ComTaskExecution> comTaskExecutions) {
            comTaskExecutions.forEach(this::executionCompleted);
        }

        @Override
        public void executionFailed(ComTaskExecution comTaskExecution) {
            this.verifier.verify(executionFailed);
        }

        @Override
        public void executionFailed(ComTaskExecution comTaskExecution, boolean noRetry) {
            executionFailed(comTaskExecution);
        }

        @Override
        public void executionFailed(List<? extends ComTaskExecution> comTaskExecutions) {
            comTaskExecutions.forEach(this::executionFailed);
        }

        @Override
        public void releaseInterruptedTasks(ComPort comPort) {
            // No implementation required
        }

        @Override
        public TimeDuration releaseTimedOutTasks(ComPort comPort) {
            // No implementation required
            return new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
        }

        @Override
        public void releaseTasksFor(ComPort comPort) {
            // No implementation required
        }

        @Override
        public void createOrUpdateDeviceCache(DeviceIdentifier deviceIdentifier, DeviceProtocolCacheXmlWrapper cache) {
        }

        @Override
        public void storeMeterReadings(DeviceIdentifier deviceIdentifier, MeterReading meterReading) {
        }

        public void updateLogBookLastReadingFromTask(final LogBookIdentifier logBookIdentifier, final long comTaskExecutionId) {
            // No implementation required
        }

        @Override
        public void storeLoadProfile(LoadProfileIdentifier loadProfileIdentifier, CollectedLoadProfile collectedLoadProfile, Instant currentDate) {
        }

        @Override
        public void storeLogBookData(LogBookIdentifier logBookIdentifier, CollectedLogBook collectedLogBook, Instant currentDate) {
        }

        @Override
        public void updateLogBookLastReading(LogBookIdentifier logBookIdentifier, Date lastExecutionStartTimestamp) {
        }

        @Override
        public void updateConnectionTaskProperty(Object propertyValue, ConnectionTask connectionTask, String connectionTaskPropertyName) {
            // No implementation required
        }

        @Override
        public void updateConnectionTaskProperties(ConnectionTask connectionTask, Map<String, Object> connectionPropertyNameAndValue) {
        }

        @Override
        public void updateDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {

        }

        @Override
        public void storeConfigurationFile(DeviceIdentifier deviceIdentifier, DateTimeFormatter timeStampFormat, String fileName, String fileExtension, byte[] contents) {
            // No implementation required
        }

        @Override
        public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(DeviceIdentifier deviceIdentifier, int confirmationCount) {
            // No implementation required
            return new ArrayList<>(0);
        }

        @Override
        public com.energyict.mdc.upl.properties.TypedProperties getDeviceDialectProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
            return null;
        }

        @Override
        public DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
            return null;
        }

        @Override
        public com.energyict.mdc.upl.properties.TypedProperties getDeviceProtocolSecurityProperties(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
            return null;
        }

        @Override
        public TypedProperties getDeviceConnectionTypeProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
            // No implementation required
            return TypedProperties.empty();
        }

        @Override
        public TypedProperties getOutboundConnectionTypeProperties(DeviceIdentifier deviceIdentifier) {
            return TypedProperties.empty();

        }

        @Override
        public com.energyict.mdc.upl.properties.TypedProperties getDeviceLocalProtocolProperties(DeviceIdentifier deviceIdentifier) {
            return null;
        }

        @Override
        public com.energyict.mdc.upl.offline.OfflineDevice getOfflineDevice(DeviceIdentifier deviceIdentifier, OfflineDeviceContext context) {
            return null;
        }

        @Override
        public String getDeviceProtocolClassName(DeviceIdentifier identifier) {
            return null;
        }

        @Override
        public TypedProperties getDeviceProtocolProperties(DeviceIdentifier deviceIdentifier) {
            // No implementation required
            return TypedProperties.empty();
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
        public void updateDeviceMessageInformation(DeviceMessage deviceMessage, DeviceMessageStatus newDeviceMessageStatus, Instant sentDate, String protocolInformation) {
            // nothing to update
        }

        @Override
        public void updateDeviceMessageInformation(MessageIdentifier messageIdentifier, DeviceMessageStatus newDeviceMessageStatus, Instant sentDate, String protocolInformation) {
            // nothing to update
        }

        @Override
        public boolean isStillPending(long comTaskExecutionId) {
            return false;
        }

        @Override
        public boolean areStillPending(Collection<Long> comTaskExecutionIds) {
            return false;
        }

        @Override
        public boolean areStillPendingWithHighPriority(Collection<Long> priorityComTaskExecutionLinkIds) {
            return false;
        }

        @Override
        public ServerProcessStatus getStatus() {
            return null;
        }

        @Override
        public void start() {
        }

        @Override
        public void shutdown() {
        }

        @Override
        public void shutdownImmediate() {
        }

        @Override
        public ComSession createComSession(ComSessionBuilder builder, Instant stopDate, ComSession.SuccessIndicator successIndicator) {
            return null;
        }

        @Override
        public List<Pair<OfflineLoadProfile, Range<Instant>>> getStorageLoadProfileIdentifiers(OfflineLoadProfile loadProfile, String readingTypeMRID, Range<Instant> dataPeriod) {
            throw new UnsupportedOperationException("Method not implemented");
        }

        @Override
        public User getComServerUser() {
            return null;
        }

        @Override
        public Map<OfflineDeviceMessage, DeviceMessage> lockDeviceMessages(Collection<OfflineDeviceMessage> offlineDeviceMessages) {
            return null;
        }

        @Override
        public List<Long> findContainingActiveComPortPoolsForComPort(OutboundComPort comPort) {
            return Collections.emptyList();
        }

        @Override
        public void updateUmiwanStructure(ComTaskExecution comTaskExecution, Map<String, Object> properties, String cas) {

        }
    }
}
