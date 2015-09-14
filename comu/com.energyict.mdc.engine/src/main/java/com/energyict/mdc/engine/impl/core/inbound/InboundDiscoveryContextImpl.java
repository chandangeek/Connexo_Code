package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.protocol.api.crypto.Cryptographer;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.security.SecurityProperty;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Holds contextual information for an {@link InboundDeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-15 (09:15)
 */
public class InboundDiscoveryContextImpl implements InboundDiscoveryContext {

    private final ConnectionTaskService connectionTaskService;

    private Logger logger;
    private Cryptographer cryptographer;
    private InboundDAO inboundDAO;
    private ComSessionBuilder sessionBuilder;
    private JournalEntryBacklog journalEntryBacklog = new JournalEntryBacklog();
    private final InboundComPort comPort;
    private ComPortRelatedComChannel comChannel;
    private HttpServletRequest servletRequest;
    private HttpServletResponse servletResponse;
    private boolean allCollectedDataWasProcessed = true;

    public InboundDiscoveryContextImpl(InboundComPort comPort, ComPortRelatedComChannel comChannel, ConnectionTaskService connectionTaskService) {
        super();
        this.comPort = comPort;
        this.comChannel = comChannel;
        this.connectionTaskService = connectionTaskService;
    }

    public InboundDiscoveryContextImpl(InboundComPort comPort, HttpServletRequest servletRequest, HttpServletResponse servletResponse, ConnectionTaskService connectionTaskService) {
        super();
        this.comPort = comPort;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.connectionTaskService = connectionTaskService;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Cryptographer getCryptographer() {
        return cryptographer;
    }

    @Override
    public void setCryptographer(Cryptographer cryptographer) {
        this.cryptographer = cryptographer;
    }

    public InboundDAO getInboundDAO() {
        return inboundDAO;
    }

    public void setInboundDAO(InboundDAO inboundDAO) {
        this.inboundDAO = inboundDAO;
    }

    public ComSessionBuilder getComSessionBuilder() {
        return sessionBuilder;
    }

    public ComSessionBuilder buildComSession(ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Instant startTime) {
        sessionBuilder = this.connectionTaskService.buildComSession(connectionTask, comPortPool, comPort, startTime);
        this.journalEntryBacklog.createWith(sessionBuilder);
        return sessionBuilder;
    }

    public InboundComPort getComPort() {
        return comPort;
    }

    @Override
    public ComPortRelatedComChannel getComChannel() {
        return comChannel;
    }

    @Override
    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    @Override
    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    @Override
    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    @Override
    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    @Override
    public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(DeviceIdentifier deviceIdentifier, int confirmationCount) {
        return this.getInboundDAO().confirmSentMessagesAndGetPending(deviceIdentifier, confirmationCount);
    }

    @Override
    public List<SecurityProperty> getDeviceProtocolSecurityProperties(DeviceIdentifier deviceIdentifier) {
        return this.getInboundDAO().getDeviceProtocolSecurityProperties(deviceIdentifier, this.getComPort());
    }

    @Override
    public TypedProperties getDeviceConnectionTypeProperties(DeviceIdentifier deviceIdentifier) {
        return this.getInboundDAO().getDeviceConnectionTypeProperties(deviceIdentifier, this.getComPort());
    }

    @Override
    public TypedProperties getDeviceProtocolProperties(DeviceIdentifier deviceIdentifier) {
        return this.getInboundDAO().getDeviceProtocolProperties(deviceIdentifier);
    }

    @Override
    public void markNotAllCollectedDataWasProcessed() {
        this.allCollectedDataWasProcessed = false;
    }

    public boolean isAllCollectedDataWasProcessed() {
        return allCollectedDataWasProcessed;
    }

    public void addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String description, Throwable t) {
        if (this.sessionBuilder != null) {
            this.sessionBuilder.addJournalEntry(timestamp, logLevel, description, t);
        } else {
            this.journalEntryBacklog.addJournalEntry(timestamp, logLevel, description, t);
        }
    }

    private class JournalEntryBacklog {
        private List<JournalEntryBacklogEntry> entries = new ArrayList<>();

        private void addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String description, Throwable t) {
            this.entries.add(new JournalEntryBacklogEntry(timestamp, logLevel, description, t));
        }

        private void createWith(ComSessionBuilder builder) {
            for (JournalEntryBacklogEntry entry : this.entries) {
                entry.createWith(builder);
            }
            this.entries = new ArrayList<>();
        }
    }

    private class JournalEntryBacklogEntry {
        private Instant timestamp;
        private ComServer.LogLevel logLevel;
        private String description;
        private Throwable thrown;

        private JournalEntryBacklogEntry(Instant timestamp, ComServer.LogLevel logLevel, String description, Throwable thrown) {
            super();
            this.timestamp = timestamp;
            this.logLevel = logLevel;
            this.description = description;
            this.thrown = thrown;
        }

        private void createWith(ComSessionBuilder builder) {
            builder.addJournalEntry(this.timestamp, this.logLevel, this.description, this.thrown);
        }

    }

}