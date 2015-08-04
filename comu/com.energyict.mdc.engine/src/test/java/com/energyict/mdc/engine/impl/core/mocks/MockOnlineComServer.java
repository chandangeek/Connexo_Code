package com.energyict.mdc.engine.impl.core.mocks;

import com.energyict.mdc.common.BusinessException;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.io.SerialPortConfiguration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import java.time.Instant;

/**
* Provides a mock implementation for the {@link OnlineComServer} interface.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-04-03 (12:04)
*/
public class MockOnlineComServer implements Cloneable, OnlineComServer {

    private static final long SEQUENCE_START = 97;
    private static long NEXT_ID = SEQUENCE_START;
    private static final long COMPORT_ID_START = SEQUENCE_START;
    private static long NEXT_COMPORT_ID = COMPORT_ID_START;

    private long id;
    private boolean active;
    private LogLevel serverLogLevel;
    private LogLevel communicationLogLevel;
    private TimeDuration changesInterPollDelay;
    private TimeDuration schedulingInterPollDelay;
    private List<MockTCPInboundComPort> inboundComPorts = new ArrayList<>();
    private List<MockOutboundComPort> outboundComPorts = new ArrayList<>();
    private boolean dirty;
    private String name;
    private int storeTaskQueueSize = ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE;
    private int numberOfStoreTaskThreads = ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS;
    private int storeTaskThreadPriority = ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY;
    private boolean usesDefaultEventRegistrationUri = false;
    private String eventRegistrationUri;
    private String queryAPIPostUri;
    private boolean usesDefaultQueryApiPostUri = false;
    private String statusUri;
    private boolean usesDefaultStatusUri = false;
    private long version;

    public MockOnlineComServer (String name) {
        super();
        this.id = NEXT_ID++;
        this.name = name;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    private long nextComPortId () {
        return NEXT_COMPORT_ID++;
    }

    @Override
    public boolean isOffline () {
        return false;
    }

    @Override
    public boolean isRemote () {
        return false;
    }

    @Override
    public boolean isOnline () {
        return true;
    }

    @Override
    public Instant getModificationDate() {
        return Instant.now();
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public boolean isActive () {
        return active;
    }

    @Override
    public void setActive (boolean active) {
        this.active = active;
    }

    @Override
    public LogLevel getServerLogLevel () {
        return serverLogLevel;
    }

    @Override
    public void setServerLogLevel (LogLevel serverLogLevel) {
        this.serverLogLevel = serverLogLevel;
    }

    @Override
    public LogLevel getCommunicationLogLevel () {
        return communicationLogLevel;
    }

    @Override
    public void setCommunicationLogLevel (LogLevel communicationLogLevel) {
        this.communicationLogLevel = communicationLogLevel;
    }

    @Override
    public TimeDuration getChangesInterPollDelay () {
        return changesInterPollDelay;
    }

    @Override
    public void setChangesInterPollDelay (TimeDuration changesInterPollDelay) {
        this.changesInterPollDelay = changesInterPollDelay;
        this.becomeDirty();
    }

    @Override
    public TimeDuration getSchedulingInterPollDelay () {
        return schedulingInterPollDelay;
    }

    @Override
    public void setSchedulingInterPollDelay (TimeDuration schedulingInterPollDelay) {
        this.schedulingInterPollDelay = schedulingInterPollDelay;
        this.becomeDirty();
    }

    @Override
    public boolean usesDefaultQueryApiPostUri() {
        return this.usesDefaultQueryApiPostUri;
    }

    @Override
    public void setUsesDefaultQueryAPIPostUri(boolean usesDefaultQueryAPIPostUri) {
        this.usesDefaultQueryApiPostUri = usesDefaultQueryAPIPostUri;
    }

    @Override
    public String getQueryApiPostUri () {
        return queryAPIPostUri;
    }

    @Override
    public void setQueryAPIPostUri (String queryAPIPostUri) {
        this.queryAPIPostUri = queryAPIPostUri;
    }

    @Override
    public String getStatusUri() {
        return this.statusUri;
    }

    @Override
    public void setStatusUri(String statusUri) {
        this.statusUri = statusUri;
    }

    @Override
    public int getStoreTaskQueueSize () {
        return this.storeTaskQueueSize;
    }

    @Override
    public boolean usesDefaultStatusUri() {
        return this.usesDefaultStatusUri;
    }

    @Override
    public void setUsesDefaultStatusUri(boolean usesDefaultStatusUri) {
        this.usesDefaultStatusUri = usesDefaultStatusUri;
    }

    @Override
    public void setStoreTaskQueueSize(int storeTaskQueueSize) {
        this.storeTaskQueueSize = storeTaskQueueSize;
        this.becomeDirty();
    }

    @Override
    public int getNumberOfStoreTaskThreads () {
        return this.numberOfStoreTaskThreads;
    }

    @Override
    public void setNumberOfStoreTaskThreads(int numberOfStoreTaskThreads) {
        this.numberOfStoreTaskThreads = numberOfStoreTaskThreads;
        this.becomeDirty();
    }

    @Override
    public int getStoreTaskThreadPriority () {
        return this.storeTaskThreadPriority;
    }

    @Override
    public void setStoreTaskThreadPriority(int storeTaskThreadPriority) {
        this.storeTaskThreadPriority = storeTaskThreadPriority;
    }

    @Override
    public boolean usesDefaultEventRegistrationUri () {
        return this.usesDefaultEventRegistrationUri;
    }

    @Override
    public void setUsesDefaultEventRegistrationUri(boolean usesDefaultEventRegistrationUri) {
        this.usesDefaultEventRegistrationUri = usesDefaultEventRegistrationUri;
    }

    @Override
    public String getEventRegistrationUri () {
        return eventRegistrationUri;
    }

    @Override
    public void setEventRegistrationUri(String eventRegistrationUri) {
        this.eventRegistrationUri = eventRegistrationUri;
    }

    @Override
    public List<ComPort> getComPorts () {
        List<ComPort> allComPorts = new ArrayList<>();
        allComPorts.addAll(this.getInboundComPorts());
        allComPorts.addAll(this.getOutboundComPorts());
        return allComPorts;
    }

    @Override
    public List<InboundComPort> getInboundComPorts () {
        return new ArrayList<InboundComPort>(this.inboundComPorts);
    }

    public MockTCPInboundComPort getInboundComPort (int portNumber) {
        return this.inboundComPorts.get(portNumber - 1);
    }

    @Override
    public List<OutboundComPort> getOutboundComPorts () {
        return new ArrayList<OutboundComPort>(this.outboundComPorts);
    }

    public MockOutboundComPort getOutboundComPort (int portNumber) {
        return this.outboundComPorts.get(portNumber - 1);
    }


    public TCPBasedInboundComPort createTCPBasedInbound (String name, boolean active, int portNumber, int numberOfSimultaneousConnections)
        throws
            BusinessException,
            SQLException {
        MockTCPInboundComPort comPort = new MockTCPInboundComPort(this, this.nextComPortId(), name);
        comPort.setActive(active);
        comPort.setPortNumber(portNumber);
        comPort.setNumberOfSimultaneousConnections(numberOfSimultaneousConnections);
        comPort.becomeClean();
        this.inboundComPorts.add(comPort);
        this.becomeDirty();
        return comPort;
    }

    public MockOutboundComPort createOutbound (String name, boolean active, int numberOfSimultaneousConnections) throws BusinessException, SQLException {
        MockOutboundComPort comPort = new MockOutboundComPort(this, this.nextComPortId(), name);
        comPort.setActive(active);
        comPort.setComPortType(ComPortType.TCP);
        comPort.setNumberOfSimultaneousConnections(numberOfSimultaneousConnections);
        comPort.becomeClean();
        this.outboundComPorts.add(comPort);
        this.becomeDirty();
        return comPort;
    }


    @Override
    public boolean isObsolete () {
        return false;
    }

    @Override
    public void makeObsolete () {

    }

    @Override
    public Instant getObsoleteDate () {
        return null;
    }

    public MockTCPInboundComPort activateInbound (int portNumber) {
        MockTCPInboundComPort comPort = this.inboundComPorts.get(portNumber - 1);
        comPort.setActive(true);
        this.becomeDirty();
        return comPort;
    }

    public MockOutboundComPort activateOutbound (int portNumber) {
        MockOutboundComPort comPort = this.outboundComPorts.get(portNumber - 1);
        comPort.setActive(true);
        this.becomeDirty();
        return comPort;
    }

    public MockTCPInboundComPort deactivateInbound (int portNumber) {
        MockTCPInboundComPort comPort = this.inboundComPorts.get(portNumber - 1);
        comPort.setActive(false);
        this.becomeDirty();
        return comPort;
    }

    public MockOutboundComPort deactivateOutbound (int portNumber) {
        MockOutboundComPort comPort = this.outboundComPorts.get(portNumber - 1);
        comPort.setActive(false);
        this.becomeDirty();
        return comPort;
    }

    public void deleteInbound (int portNumber) {
        this.inboundComPorts.remove(portNumber - 1);
        this.becomeDirty();
    }

    public void deleteOutbound (int portNumber) {
        this.outboundComPorts.remove(portNumber - 1);
        this.becomeDirty();
    }

    public MockTCPInboundComPort setNumberOfSimultaneousInboundConnections (int portNumber, int numberOfSimultaneousConnections) {
        MockTCPInboundComPort comPort = this.inboundComPorts.get(portNumber - 1);
        comPort.setNumberOfSimultaneousConnections(numberOfSimultaneousConnections);
        this.becomeDirty();
        return comPort;
    }

    public MockOutboundComPort setNumberOfSimultaneousOutboundConnections (int portNumber, int numberOfSimultaneousConnections) {
        MockOutboundComPort comPort = this.outboundComPorts.get(portNumber - 1);
        comPort.setNumberOfSimultaneousConnections(numberOfSimultaneousConnections);
        this.becomeDirty();
        return comPort;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    protected void becomeDirty () {
        this.setDirty(true);
    }

    protected void becomeClean () {
        this.setDirty(false);
    }

    @Override
    public Object clone () throws CloneNotSupportedException {
        MockOnlineComServer clone = (MockOnlineComServer) super.clone();
        clone.inboundComPorts = new ArrayList<>(this.inboundComPorts.size());
        for (MockTCPInboundComPort comPort : this.inboundComPorts) {
            clone.inboundComPorts.add((MockTCPInboundComPort) comPort.clone());
        }
        clone.outboundComPorts = new ArrayList<>(this.outboundComPorts.size());
        for (MockOutboundComPort comPort : this.outboundComPorts) {
            clone.outboundComPorts.add((MockOutboundComPort) comPort.clone());
        }
        return clone;
    }

    @Override
    public String getEventRegistrationUriIfSupported () throws BusinessException {
        throw new BusinessException("eventRegistrationNotSupportedByMockOnlineComServers", "Event registration is not supported by mock online comservers");
    }

    @Override
    public String getQueryApiPostUriIfSupported () throws BusinessException {
        throw new BusinessException("queryAPINotSupportedByMockOnlineComServers", "Query API is not supported by mock online comservers");
    }

    @Override
    public OutboundComPort.OutboundComPortBuilder newOutboundComPort(String name, int numberOfSimultaneousConnections) {
        return null;
    }

    @Override
    public ServletBasedInboundComPort.ServletBasedInboundComPortBuilder newServletBasedInboundComPort(String name, String contextPath, int numberOfSimultaneousConnections, int portNumber) {
        return null;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder newModemBasedInboundComport(String name, int ringCount, int maximumDialErrors, TimeDuration connectTimeout, TimeDuration atCommandTimeout, SerialPortConfiguration serialPortConfiguration) {
        return null;
    }

    @Override
    public TCPBasedInboundComPort.TCPBasedInboundComPortBuilder newTCPBasedInboundComPort(String name, int numberOfSimultaneousConnections, int portNumber) {
        return null;
    }

    @Override
    public UDPBasedInboundComPort.UDPBasedInboundComPortBuilder newUDPBasedInboundComPort(String name, int numberOfSimultaneousConnections, int portNumber) {
        return null;
    }

    @Override
    public void removeComPort(long id) {

    }

    @Override
    public void delete() {

    }

    @Override
    public void save() {
        this.version++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MockOnlineComServer that = (MockOnlineComServer) o;

        if (id != that.id) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

}