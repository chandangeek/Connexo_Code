/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.mocks;

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
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskProperty;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.LookupEntry;
import com.energyict.mdc.engine.impl.PropertyValueType;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.remote.DeviceProtocolCacheXmlWrapper;
import com.energyict.mdc.engine.users.OfflineUserInfo;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCertificateWrapper;
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

import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link ComServerDAO} interface
 * that mocks all calls and returns dummy data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (11:47)
 */
public class MockComServerDAO implements ComServerDAO {

    public static final int CHANGES_DELAY_SECONDS = 1;

    private static final int PORT_NUMBER_START = 9000;
    private static final String HOST_NAME = "localhost";

    private List<MockOnlineComServer> comServers = new ArrayList<>();
    private List<MockOnlineComServer> comServerClones = new ArrayList<>();
    private int comServerRefreshCount = 0;
    private Map<ConnectionTask, ComPort> connectionTaskLocking = new HashMap<>();
    private Map<ComTaskExecution, ComPort> comTaskExecutionLocking = new HashMap<>();

    public MockOnlineComServer addEmptyComServer() {
        MockOnlineComServer comServer = new MockOnlineComServer(HOST_NAME);
        this.setMockProperties(comServer);
        this.comServers.add(comServer);
        this.comServerClones.add(null);
        return comServer;
    }

    private void setMockProperties(MockOnlineComServer comServer) {
        comServer.setActive(true);
        comServer.setServerLogLevel(ComServer.LogLevel.INFO);
        comServer.setCommunicationLogLevel(ComServer.LogLevel.INFO);
        comServer.setChangesInterPollDelay(new TimeDuration(CHANGES_DELAY_SECONDS, TimeDuration.TimeUnit.SECONDS));
        comServer.setSchedulingInterPollDelay(new TimeDuration(100, TimeDuration.TimeUnit.MILLISECONDS));
    }

    public MockOnlineComServer addComServer(int activeOutboundComPorts, int activeInboundComPorts) throws SQLException {
        MockOnlineComServer comServer = this.addEmptyComServer();
        this.addMockComPorts(comServer, activeOutboundComPorts, activeInboundComPorts);
        this.doRefresh(comServer);
        return comServer;
    }

    private void addMockComPorts(MockOnlineComServer comServer, int activeOutboundComPorts, int activeInboundComPorts) throws SQLException {
        this.addMockInboundComPorts(comServer, activeInboundComPorts);
        this.addMockOutboundComPorts(comServer, activeOutboundComPorts);
    }

    private void addMockOutboundComPorts(MockOnlineComServer comServer, int activeOutboundComPorts) throws SQLException {
        for (int i = 0; i < activeOutboundComPorts; i++) {
            this.createOutbound(comServer, i, true, 1);
        }
    }

    private void addMockInboundComPorts(MockOnlineComServer comServer, int activeInboundComPorts) throws SQLException {
        for (int i = 0; i < activeInboundComPorts; i++) {
            this.createInbound(comServer, i, true, 1);
        }
    }

    public OutboundComPort createOutbound(int serverIndex, boolean active, int numberOfConnections) throws SQLException {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        int newPortIndex = comServer.getOutboundComPorts().size();
        return this.createOutbound(comServer, newPortIndex, active, numberOfConnections);
    }

    private OutboundComPort createOutbound(MockOnlineComServer comServer, int i, boolean active, int numberOfConnections) throws SQLException {
        return comServer.createOutbound("ComPort-" + (i + 1), active, numberOfConnections);
    }

    public InboundComPort createInbound(int serverIndex, boolean active, int numberOfConnections) throws SQLException {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        int newPortIndex = comServer.getInboundComPorts().size();
        return this.createInbound(comServer, newPortIndex, active, numberOfConnections);
    }

    private InboundComPort createInbound(MockOnlineComServer comServer, int i, boolean active, int numberOfConnections) throws SQLException {
        return comServer.createTCPBasedInbound("ComPort-" + (i + 1), active, PORT_NUMBER_START + i, numberOfConnections);
    }

    public MockTCPInboundComPort deactivateInbound(int serverIndex, int portNumber) {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        return comServer.deactivateInbound(portNumber);
    }

    public void deleteInbound(int serverIndex, int portNumber) {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        comServer.deleteInbound(portNumber);
    }

    public MockOutboundComPort deactivateOutbound(int serverIndex, int portNumber) {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        return comServer.deactivateOutbound(portNumber);
    }

    public void deleteOutbound(int serverIndex, int portNumber) {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        comServer.deleteOutbound(portNumber);
    }

    public MockTCPInboundComPort setNumberOfSimultaneousInboundConnections(int serverIndex, int portNumber, int numberOfSimultaneousInboundConnections) {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        return comServer.setNumberOfSimultaneousInboundConnections(portNumber, numberOfSimultaneousInboundConnections);
    }

    public MockOutboundComPort setNumberOfSimultaneousOutboundConnections(int serverIndex, int portNumber, int numberOfSimultaneousConnections) {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        return comServer.setNumberOfSimultaneousOutboundConnections(portNumber, numberOfSimultaneousConnections);
    }

    public void setSchedulingInterPollDelay(int serverIndex, TimeDuration interPollDelay) {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        comServer.setSchedulingInterPollDelay(interPollDelay);
    }

    private MockOnlineComServer getCloneForServer(int serverIndex) {
        MockOnlineComServer clone = this.comServerClones.get(serverIndex);
        if (clone == null) {
            try {
                clone = (MockOnlineComServer) this.comServers.get(serverIndex).clone();
                this.comServerClones.set(serverIndex, clone);
            } catch (CloneNotSupportedException e) {
                // Silly compiler, MockOnlineComServer implements Cloneable
            }
        }
        return clone;
    }

    @Override
    public ComServer getThisComServer() {
        return this.getComServer("localhost");
    }

    @Override
    public ComServer getComServer(String hostName) {
        for (ComServer comServer : this.comServers) {
            if (is(comServer.getName()).equalTo(hostName)) {
                return comServer;
            }
        }
        return null;
    }

    @Override
    public ComServer refreshComServer(ComServer comServer) {
        this.comServerRefreshCount++;
        return this.doRefresh(comServer);
    }


    public List<OfflineUserInfo> getUsersCredentialInformation() {
        return Collections.emptyList();
    }

    @Override
    public ComPort refreshComPort(ComPort comPort) {
        MockComPort mockComPort = (MockComPort) comPort;
        if (mockComPort.isDirty()) {
            mockComPort.setDirty(false);
            try {
                return (ComPort) mockComPort.clone();
            } catch (CloneNotSupportedException e) {
                // Silly bugger, the class implements Cloneable
                return comPort;
            }
        } else {
            return comPort;
        }
    }

    private ComServer doRefresh(ComServer comServer) {
        MockOnlineComServer mockComserver = (MockOnlineComServer) comServer;
        if (mockComserver.isDirty()) {
            int i = this.comServers.indexOf(comServer);
            MockOnlineComServer clone = this.comServerClones.get(i);
            this.comServerClones.set(i, null);  // Throw away the clone
            return clone;
        } else {
            return comServer;
        }
    }

    public int getAndResetComServerRefreshCount() {
        int value = comServerRefreshCount;
        this.comServerRefreshCount = 0;
        return value;
    }

    @Override
    public List<ComJob> findPendingOutboundComTasks(OutboundComPort comPort) {
        return Collections.emptyList();
    }

    @Override
    public List<ComJob> findExecutableOutboundComTasks(OutboundComPort comPort) {
        return Collections.emptyList();
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
        return Collections.emptyList();
    }

    @Override
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier) {
        return Optional.empty();
    }

    @Override
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier, OfflineDeviceContext offlineDeviceContext) {
        return Optional.empty();
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
        return connectionTask.getProperties();
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
    public synchronized ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, ComPort comPort) {
        ComPort alreadyLockingComPort = connectionTaskLocking.get(connectionTask);
        if (alreadyLockingComPort == null || !comPort.equals(alreadyLockingComPort)) {
            connectionTaskLocking.put(connectionTask, comPort);
            return connectionTask;
        } else {
            return null;
        }
    }

    @Override
    public synchronized boolean attemptLock(OutboundConnectionTask connectionTask, ComPort comPort) {
        ComPort alreadyLockingComPort = connectionTaskLocking.get(connectionTask);
        if (alreadyLockingComPort == null || !comPort.equals(alreadyLockingComPort)) {
            connectionTaskLocking.put(connectionTask, comPort);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void unlock(OutboundConnectionTask connectionTask) {
        connectionTaskLocking.remove(connectionTask);
    }

    @Override
    public boolean attemptLock(ComTaskExecution comTaskExecution, ComPort comPort) {
        ComPort alreadyLockingComPort = this.comTaskExecutionLocking.get(comTaskExecution);
        if (alreadyLockingComPort == null || !comPort.equals(alreadyLockingComPort)) {
            comTaskExecutionLocking.put(comTaskExecution, comPort);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean attemptLock(PriorityComTaskExecutionLink priorityComTaskExecutionLink, ComPort comPort) {
        return attemptLock(priorityComTaskExecutionLink.getComTaskExecution(), comPort);
    }

    @Override
    public void unlock(ComTaskExecution comTaskExecution) {
        comTaskExecutionLocking.remove(comTaskExecution);
    }

    @Override
    public ConnectionTask<?, ?> executionStarted(ConnectionTask connectionTask, ComPort comPort) {
        connectionTaskLocking.put(connectionTask, comPort);
        return connectionTask;
    }

    @Override
    public ConnectionTask<?, ?> executionFailed(ConnectionTask connectionTask) {
        connectionTaskLocking.remove(connectionTask);
        return connectionTask;
    }

    @Override
    public ConnectionTask<?, ?> executionRescheduled(ConnectionTask connectionTask) {
        connectionTaskLocking.remove(connectionTask);
        return connectionTask;
    }

    @Override
    public ConnectionTask<?, ?> executionCompleted(ConnectionTask connectionTask) {
        connectionTaskLocking.remove(connectionTask);
        return connectionTask;
    }

    @Override
    public void executionStarted(ComTaskExecution comTaskExecution, ComPort comPort, boolean executeInTransaction) {
        comTaskExecutionLocking.put(comTaskExecution, comPort);
    }

    @Override
    public void executionCompleted(ComTaskExecution comTaskExecution) {
        comTaskExecutionLocking.remove(comTaskExecution);
    }

    @Override
    public void executionRescheduled(ComTaskExecution comTaskExecution, Instant rescheduleDate) {
        comTaskExecutionLocking.remove(comTaskExecution);
    }

    @Override
    public void executionRescheduledToComWindow(ComTaskExecution comTaskExecution, Instant comWindowStartDate) {
        comTaskExecutionLocking.remove(comTaskExecution);
    }

    @Override
    public void executionCompleted(List<? extends ComTaskExecution> comTaskExecutions) {
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            executionCompleted(comTaskExecution);
        }
    }

    @Override
    public void executionFailed(ComTaskExecution comTaskExecution) {
        this.comTaskExecutionLocking.remove(comTaskExecution);
    }

    @Override
    public void executionFailed(ComTaskExecution comTaskExecution, boolean noRetry) {
        executionFailed(comTaskExecution);
    }

    @Override
    public void executionFailed(List<? extends ComTaskExecution> comTaskExecutions) {
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            executionFailed(comTaskExecution);
        }
    }

    @Override
    public void releaseInterruptedTasks(ComPort comPort) {
        comTaskExecutionLocking.clear();
    }

    @Override
    public TimeDuration releaseTimedOutTasks(ComPort comPort) {
        comTaskExecutionLocking.clear();
        return new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
    }

    @Override
    public void releaseTasksFor(ComPort comPort) {
        comTaskExecutionLocking.clear();
    }

//    private EndDeviceCache createOrUpdateDeviceCache(int deviceId, DeviceCacheShadow shadow) {
//        // Not creating or updating device caches in mock mode
//        return null;
//    }

    @Override
    public ComSession createComSession(ComSessionBuilder builder, Instant stopDate, ComSession.SuccessIndicator successIndicator) {
//        // Not creating com sessions in mock mode
        return null;
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
        // Not updating logbook last reading in mock mode
    }

    @Override
    public void updateConnectionTaskProperty(Object propertyValue, ConnectionTask connectionTask, String connectionTaskPropertyName) {
    }

    @Override
    public void updateDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
    }

    @Override
    public void storeConfigurationFile(DeviceIdentifier deviceIdentifier, DateTimeFormatter timeStampFormat, String fileName, String fileExtension, byte[] contents) {
        // Not storing any configuration files in mock mode
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
        return TypedProperties.empty();
    }

    @Override
    public TypedProperties getOutboundConnectionTypeProperties(DeviceIdentifier deviceIdentifier) {
        return TypedProperties.empty();
    }

    @Override
    public TypedProperties getDeviceProtocolProperties(DeviceIdentifier deviceIdentifier) {
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
        // Not updating the gateway device in mock mode
    }

    @Override
    public void signalEvent(String topic, Object source) {
        // Not signaling events in mock mode
    }

    @Override
    public void updateDeviceMessageInformation(MessageIdentifier messageIdentifier, DeviceMessageStatus newDeviceMessageStatus, Instant sentDate, String protocolInformation) {
        // nothing to update
    }

    @Override
    public boolean isStillPending(long comTaskExecutionId) {
        return true;
    }

    @Override
    public boolean areStillPending(Collection<Long> comTaskExecutionIds) {
        return true;
    }

    @Override
    public boolean areStillPendingWithHighPriority(Collection<Long> priorityComTaskExecutionLinkIds) {
        return false;
    }

    @Override
    public <T> T executeTransaction(Transaction<T> transaction) {
        return null;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifierFor(LoadProfileIdentifier loadProfileIdentifier) {
        //com.energyict.mdc.device.data.LoadProfile loadProfile = (com.energyict.mdc.device.data.LoadProfile) loadProfileIdentifier.getLoadProfile();     //Downcast to Connexo LoadProfile
        //return new DeviceIdentifierForAlreadyKnownDeviceByMrID(loadProfile.getDevice());
        //TODO fix?
        return null;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifierFor(LogBookIdentifier logBookIdentifier) {
        //TODO fix?
        //return new DeviceIdentifierForAlreadyKnownDeviceByMrID(((LogBook) logBookIdentifier.getLogBook()).getDevice());
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
    public void updateLastReadingFor(LoadProfileIdentifier loadProfileIdentifier, Instant lastReading) {
        // I am her
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
    public void updateBreakerStatus(CollectedBreakerStatus collectedBreakerStatus) {

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
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public User getComServerUser() {
        return null;
    }

    @Override
    public List<Long> findContainingActiveComPortPoolsForComPort(OutboundComPort comPort) {
        return Collections.emptyList();
    }
}