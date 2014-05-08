package com.energyict.mdc.engine.impl.core.mocks;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.impl.OnlineComServerImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.shadow.servers.OnlineComServerShadow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* Provides a mock implementation for the {@link OnlineComServer} interface.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-04-03 (12:04)
*/
public class MockOnlineComServer extends OnlineComServerImpl implements Cloneable, OnlineComServer, ServerComServer {

    private static final int COMPORT_ID_START = 97;
    private static int NEXT_COMPORT_ID = COMPORT_ID_START;

    private boolean active;
    private LogLevel serverLogLevel;
    private LogLevel communicationLogLevel;
    private TimeDuration changesInterPollDelay;
    private TimeDuration schedulingInterPollDelay;
    private String queryAPIPostUrl;
    private List<MockTCPInboundComPort> inboundComPorts = new ArrayList<>();
    private List<MockOutboundComPort> outboundComPorts = new ArrayList<>();
    private boolean dirty;
    private String name;

    public MockOnlineComServer (String name) {
        super(null,null,null, null, null, null, null); // TODO use true Mocks
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private int nextComPortId () {
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
    public Date getModificationDate() {
        return new Date();
    }

    public boolean isActive () {
        return active;
    }

    public void setActive (boolean active) {
        this.active = active;
    }

    public LogLevel getServerLogLevel () {
        return serverLogLevel;
    }

    public void setServerLogLevel (LogLevel serverLogLevel) {
        this.serverLogLevel = serverLogLevel;
    }

    public LogLevel getCommunicationLogLevel () {
        return communicationLogLevel;
    }

    public void setCommunicationLogLevel (LogLevel communicationLogLevel) {
        this.communicationLogLevel = communicationLogLevel;
    }

    public TimeDuration getChangesInterPollDelay () {
        return changesInterPollDelay;
    }

    public void setChangesInterPollDelay (TimeDuration changesInterPollDelay) {
        this.changesInterPollDelay = changesInterPollDelay;
    }

    public TimeDuration getSchedulingInterPollDelay () {
        return schedulingInterPollDelay;
    }

    public void setSchedulingInterPollDelay (TimeDuration schedulingInterPollDelay) {
        this.schedulingInterPollDelay = schedulingInterPollDelay;
    }

    @Override
    public boolean usesDefaultQueryApiPostUri () {
        return false;
    }

    public String getQueryApiPostUri () {
        return queryAPIPostUrl;
    }

    public void setQueryAPIPostUrl (String queryAPIPostUrl) {
        this.queryAPIPostUrl = queryAPIPostUrl;
    }

    @Override
    public int getStoreTaskQueueSize () {
        return OnlineComServerShadow.DEFAULT_STORE_TASK_QUEUE_SIZE;
    }

    @Override
    public int getNumberOfStoreTaskThreads () {
        return OnlineComServerShadow.DEFAULT_NUMBER_OF_STORE_TASK_THREADS;
    }

    @Override
    public int getStoreTaskThreadPriority () {
        return OnlineComServerShadow.DEFAULT_STORE_TASK_THREAD_PRIORITY;
    }

    @Override
    public boolean usesDefaultEventRegistrationUri () {
        return false;
    }

    @Override
    public String getEventRegistrationUri () {
        return null;
    }

    @Override
    public List<ComPort> getComPorts () {
        List<ComPort> allComPorts = new ArrayList<>();
        allComPorts.addAll(this.getInboundComPorts());
        allComPorts.addAll(this.getOutboundComPorts());
        return allComPorts;
    }

    public MockTCPInboundComPort getInboundComPort (int portNumber) {
        return this.inboundComPorts.get(portNumber - 1);
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
        return comPort;
    }

    public MockOutboundComPort createOutbound (String name, boolean active, int numberOfSimultaneousConnections) throws BusinessException, SQLException {
        MockOutboundComPort comPort = new MockOutboundComPort(this, this.nextComPortId(), name);
        comPort.setActive(active);
        comPort.setComPortType(ComPortType.TCP);
        comPort.setNumberOfSimultaneousConnections(numberOfSimultaneousConnections);
        comPort.becomeClean();
        this.outboundComPorts.add(comPort);
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
    public Date getObsoleteDate () {
        return null;
    }

    public MockTCPInboundComPort activateInbound (int portNumber) {
        MockTCPInboundComPort comPort = this.inboundComPorts.get(portNumber - 1);
        comPort.setActive(true);
        return comPort;
    }

    public MockOutboundComPort activateOutbound (int portNumber) {
        MockOutboundComPort comPort = this.outboundComPorts.get(portNumber - 1);
        comPort.setActive(true);
        return comPort;
    }

    public MockTCPInboundComPort deactivateInbound (int portNumber) {
        MockTCPInboundComPort comPort = this.inboundComPorts.get(portNumber - 1);
        comPort.setActive(false);
        return comPort;
    }

    public MockOutboundComPort deactivateOutbound (int portNumber) {
        MockOutboundComPort comPort = this.outboundComPorts.get(portNumber - 1);
        comPort.setActive(false);
        return comPort;
    }

    public void deleteInbound (int portNumber) {
        this.inboundComPorts.remove(portNumber - 1);
    }

    public void deleteOutbound (int portNumber) {
        this.outboundComPorts.remove(portNumber - 1);
    }

    public MockTCPInboundComPort setNumberOfSimultaneousInboundConnections (int portNumber, int numberOfSimultaneousConnections) {
        MockTCPInboundComPort comPort = this.inboundComPorts.get(portNumber - 1);
        comPort.setNumberOfSimultaneousConnections(numberOfSimultaneousConnections);
        return comPort;
    }

    public MockOutboundComPort setNumberOfSimultaneousOutboundConnections (int portNumber, int numberOfSimultaneousConnections) {
        MockOutboundComPort comPort = this.outboundComPorts.get(portNumber - 1);
        comPort.setNumberOfSimultaneousConnections(numberOfSimultaneousConnections);
        return comPort;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
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

}