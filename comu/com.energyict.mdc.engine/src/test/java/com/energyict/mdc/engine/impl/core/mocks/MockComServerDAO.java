package com.energyict.mdc.engine.impl.core.mocks;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierForAlreadyKnownDeviceByMrID;
import com.energyict.mdc.device.data.tasks.*;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.G3TopologyDeviceAddressInformation;
import com.energyict.mdc.protocol.api.device.data.TopologyNeighbour;
import com.energyict.mdc.protocol.api.device.data.TopologyPathSegment;
import com.energyict.mdc.protocol.api.device.data.identifiers.*;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.*;
import com.energyict.mdc.protocol.api.security.SecurityProperty;

import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private Map<ConnectionTask, ComServer> connectionTaskLocking = new HashMap<>();
    private Map<ComTaskExecution, ComPort> comTaskExecutionLocking = new HashMap<>();

    public MockOnlineComServer addEmptyComServer () {
        MockOnlineComServer comServer = new MockOnlineComServer(HOST_NAME);
        this.setMockProprerties(comServer);
        this.comServers.add(comServer);
        this.comServerClones.add(null);
        return comServer;
    }

    private void setMockProprerties (MockOnlineComServer comServer) {
        comServer.setActive(true);
        comServer.setServerLogLevel(ComServer.LogLevel.INFO);
        comServer.setCommunicationLogLevel(ComServer.LogLevel.INFO);
        comServer.setChangesInterPollDelay(new TimeDuration(CHANGES_DELAY_SECONDS, TimeDuration.TimeUnit.SECONDS));
        comServer.setSchedulingInterPollDelay(new TimeDuration(2, TimeDuration.TimeUnit.HOURS));
    }

    public MockOnlineComServer addComServer (int activeOutboundComPorts, int activeInboundComPorts) throws BusinessException, SQLException {
        MockOnlineComServer comServer = this.addEmptyComServer();
        this.addMockComPorts(comServer, activeOutboundComPorts, activeInboundComPorts);
        this.doRefresh(comServer);
        return comServer;
    }

    private void addMockComPorts (MockOnlineComServer comServer, int activeOutboundComPorts, int activeInboundComPorts) throws BusinessException, SQLException {
        this.addMockInboundComPorts(comServer, activeInboundComPorts);
        this.addMockOutboundComPorts(comServer, activeOutboundComPorts);
    }

    private void addMockOutboundComPorts (MockOnlineComServer comServer, int activeOutboundComPorts) throws BusinessException, SQLException {
        for (int i = 0; i < activeOutboundComPorts; i++) {
            this.createOutbound(comServer, i, true, 1);
        }
    }

    private void addMockInboundComPorts (MockOnlineComServer comServer, int activeInboundComPorts) throws BusinessException, SQLException {
        for (int i = 0; i < activeInboundComPorts; i++) {
            this.createInbound(comServer, i, true, 1);
        }
    }

    public OutboundComPort createOutbound (int serverIndex, boolean active, int numberOfConnections) throws BusinessException, SQLException {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        int newPortIndex = comServer.getOutboundComPorts().size();
        return this.createOutbound(comServer, newPortIndex, active, numberOfConnections);
    }

    private OutboundComPort createOutbound (MockOnlineComServer comServer, int i, boolean active, int numberOfConnections) throws BusinessException, SQLException {
        return comServer.createOutbound("ComPort-" + (i + 1), active, numberOfConnections);
    }

    public InboundComPort createInbound (int serverIndex, boolean active, int numberOfConnections) throws BusinessException, SQLException {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        int newPortIndex = comServer.getInboundComPorts().size();
        return this.createInbound(comServer, newPortIndex, active, numberOfConnections);
    }

    private InboundComPort createInbound (MockOnlineComServer comServer, int i, boolean active, int numberOfConnections) throws BusinessException, SQLException {
        return comServer.createTCPBasedInbound("ComPort-" + (i + 1), active, PORT_NUMBER_START + i, numberOfConnections);
    }

    public MockTCPInboundComPort deactivateInbound (int serverIndex, int portNumber) {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        return comServer.deactivateInbound(portNumber);
    }

    public void deleteInbound (int serverIndex, int portNumber) {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        comServer.deleteInbound(portNumber);
    }

    public MockOutboundComPort deactivateOutbound (int serverIndex, int portNumber) {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        return comServer.deactivateOutbound(portNumber);
    }

    public void deleteOutbound (int serverIndex, int portNumber) {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        comServer.deleteOutbound(portNumber);
    }

    public MockTCPInboundComPort setNumberOfSimultaneousInboundConnections (int serverIndex, int portNumber, int numberOfSimultaneousInboundConnections) {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        return comServer.setNumberOfSimultaneousInboundConnections(portNumber, numberOfSimultaneousInboundConnections);
    }

    public MockOutboundComPort setNumberOfSimultaneousOutboundConnections (int serverIndex, int portNumber, int numberOfSimultaneousConnections) {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        return comServer.setNumberOfSimultaneousOutboundConnections(portNumber, numberOfSimultaneousConnections);
    }

    public void setSchedulingInterPollDelay (int serverIndex, TimeDuration interPollDelay) {
        MockOnlineComServer comServer = this.getCloneForServer(serverIndex);
        comServer.setSchedulingInterPollDelay(interPollDelay);
    }

    private MockOnlineComServer getCloneForServer (int serverIndex) {
        MockOnlineComServer clone = this.comServerClones.get(serverIndex);
        if (clone == null) {
            try {
                clone = (MockOnlineComServer) this.comServers.get(serverIndex).clone();
                this.comServerClones.set(serverIndex, clone);
            }
            catch (CloneNotSupportedException e) {
                // Silly compiler, MockOnlineComServer implements Cloneable
            }
        }
        return clone;
    }

    @Override
    public ComServer getThisComServer () {
        return this.getComServer("localhost");
    }

    @Override
    public ComServer getComServer (String hostName) {
        for (ComServer comServer : this.comServers) {
            if (is(comServer.getName()).equalTo(hostName)) {
                return comServer;
            }
        }
        return null;
    }

    @Override
    public ComServer refreshComServer (ComServer comServer) {
        this.comServerRefreshCount++;
        return this.doRefresh(comServer);
    }

    private ComServer doRefresh (ComServer comServer) {
        MockOnlineComServer mockComserver = (MockOnlineComServer) comServer;
        if (mockComserver.isDirty()) {
            int i = this.comServers.indexOf(comServer);
            MockOnlineComServer clone = this.comServerClones.get(i);
            this.comServerClones.set(i, null);  // Throw away the clone
            return clone;
        }
        else {
            return comServer;
        }
    }

    public int getAndResetComServerRefreshCount () {
        int value = comServerRefreshCount;
        this.comServerRefreshCount = 0;
        return value;
    }

    @Override
    public List<ComJob> findExecutableOutboundComTasks(com.energyict.mdc.engine.config.OutboundComPort comPort) {
        return Collections.emptyList();
    }

    @Override
    public List<ComTaskExecution> findExecutableInboundComTasks (OfflineDevice device, InboundComPort comPort) {
        return Collections.emptyList();
    }

    @Override
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier) {
        return Optional.empty();
    }

    @Override
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier<?> identifier, OfflineDeviceContext offlineDeviceContext) {
        return Optional.empty();
    }

    @Override
    public Optional<OfflineRegister> findOfflineRegister(RegisterIdentifier identifier) {
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
    public synchronized ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, ComServer comServer) {
        ComServer alreadyLockingComServer = this.connectionTaskLocking.get(connectionTask);
        if (alreadyLockingComServer == null || !comServer.equals(alreadyLockingComServer)) {
            this.connectionTaskLocking.put(connectionTask, comServer);
            return connectionTask;
        }
        else {
            return null;
        }
    }

    @Override
    public synchronized boolean attemptLock(OutboundConnectionTask connectionTask, ComServer comServer) {
        ComServer alreadyLockingComServer = this.connectionTaskLocking.get(connectionTask);
        if (alreadyLockingComServer == null || !comServer.equals(alreadyLockingComServer)) {
            this.connectionTaskLocking.put(connectionTask, comServer);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void unlock (ScheduledConnectionTask connectionTask) {
        this.connectionTaskLocking.remove(connectionTask);
    }

    @Override
    public void unlock (OutboundConnectionTask connectionTask) {
        this.connectionTaskLocking.remove(connectionTask);
    }

    @Override
    public boolean attemptLock (ComTaskExecution comTaskExecution, ComPort comPort) {
        ComPort alreadyLockingComPort = this.comTaskExecutionLocking.get(comTaskExecution);
        if (alreadyLockingComPort == null || !comPort.equals(alreadyLockingComPort)) {
            this.comTaskExecutionLocking.put(comTaskExecution, comPort);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void unlock (ComTaskExecution comTaskExecution) {
        this.comTaskExecutionLocking.remove(comTaskExecution);
    }

    @Override
    public void executionStarted (ConnectionTask connectionTask, ComServer comServer) {
        this.connectionTaskLocking.put(connectionTask, comServer);
    }

    @Override
    public void executionFailed (ConnectionTask connectionTask) {
        this.connectionTaskLocking.remove(connectionTask);
    }

    @Override
    public void executionCompleted (ConnectionTask connectionTask) {
        this.connectionTaskLocking.remove(connectionTask);
    }

    @Override
    public void executionStarted(ComTaskExecution comTaskExecution, ComPort comPort, boolean executeInTransaction) {
        this.comTaskExecutionLocking.put(comTaskExecution, comPort);
    }

    @Override
    public void executionCompleted (ComTaskExecution comTaskExecution) {
        this.comTaskExecutionLocking.remove(comTaskExecution);
    }

    @Override
    public void executionCompleted (List<? extends ComTaskExecution> comTaskExecutions) {
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            this.executionCompleted(comTaskExecution);
        }
    }

    @Override
    public void executionFailed (ComTaskExecution comTaskExecution) {
        this.comTaskExecutionLocking.remove(comTaskExecution);
    }

    @Override
    public void executionFailed (List<? extends ComTaskExecution> comTaskExecutions) {
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            this.executionFailed(comTaskExecution);
        }
    }

    @Override
    public void releaseInterruptedTasks (ComServer comServer) {
        this.comTaskExecutionLocking.clear();
    }

    @Override
    public TimeDuration releaseTimedOutTasks (ComServer comServer) {
        this.comTaskExecutionLocking.clear();
        return new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
    }

//    private EndDeviceCache createOrUpdateDeviceCache(int deviceId, DeviceCacheShadow shadow) {
//        // Not creating or updating device caches in mock mode
//        return null;
//    }

    @Override
    public ComSession createComSession(ComSessionBuilder builder, ComSession.SuccessIndicator successIndicator) {
//        // Not creating com sessions in mock mode
        return null;
    }

    @Override
    public void storeMeterReadings(DeviceIdentifier deviceIdentifier, MeterReading meterReading) {
        // Not storing readings in mock mode
    }

    @Override
    public void updateIpAddress (String ipAddress, ConnectionTask connectionTask, String connectionTaskPropertyName) {
    }

    @Override
    public void updateDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {

    }

    @Override
    public void storeConfigurationFile (DeviceIdentifier deviceIdentifier, DateTimeFormatter timeStampFormat, String fileExtension, byte[] contents) {
        // Not storing any configuration files in mock mode
    }

    @Override
    public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(DeviceIdentifier deviceIdentifier, int confirmationCount) {
        return new ArrayList<>(0);
    }

    @Override
    public List<SecurityProperty> getDeviceProtocolSecurityProperties (DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        return null;
    }

    @Override
    public TypedProperties getDeviceConnectionTypeProperties (DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        return null;
    }

    @Override
    public TypedProperties getDeviceProtocolProperties (DeviceIdentifier deviceIdentifier) {
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
    public void updateDeviceMessageInformation(MessageIdentifier messageIdentifier, DeviceMessageStatus newDeviceMessageStatus, String protocolInformation) {
        // nothing to update
    }

    @Override
    public boolean isStillPending (long comTaskExecutionId) {
        return true;
    }

    @Override
    public boolean areStillPending (Collection<Long> comTaskExecutionIds) {
        return true;
    }

    @Override
    public <T> T executeTransaction(Transaction<T> transaction) {
        return null;
    }

    @Override
    public DeviceIdentifier<Device> getDeviceIdentifierFor(LoadProfileIdentifier loadProfileIdentifier) {
        return new DeviceIdentifierForAlreadyKnownDeviceByMrID((Device) loadProfileIdentifier.findLoadProfile().getDevice());
    }

    @Override
    public DeviceIdentifier<Device> getDeviceIdentifierFor(LogBookIdentifier logBookIdentifier) {
        return new DeviceIdentifierForAlreadyKnownDeviceByMrID(((LogBook) logBookIdentifier.getLogBook()).getDevice());
    }

    @Override
    public void updateLastReadingFor(LoadProfileIdentifier loadProfileIdentifier, Instant lastReading) {

    }

    @Override
    public void updateLastLogBook(LogBookIdentifier logBookIdentifier, Instant lastLogBook) {

    }

    @Override
    public void storePathSegments(DeviceIdentifier sourceDeviceIdentifier, List<TopologyPathSegment> topologyPathSegment) {

    }

    @Override
    public void storeNeighbours(DeviceIdentifier sourceDeviceIdentifier, List<TopologyNeighbour> topologyNeighbours) {

    }

    @Override
    public void storeG3IdentificationInformation(G3TopologyDeviceAddressInformation topologyDeviceAddressInformation) {

    }

    @Override
    public void updateFirmwareVersions(CollectedFirmwareVersion collectedFirmwareVersions) {
        //
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