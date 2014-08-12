package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.protocol.api.crypto.Cryptographer;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.security.SecurityProperty;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Holds contextual information for an {@link InboundDeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-15 (09:15)
 */
public class InboundDiscoveryContextImpl implements InboundDiscoveryContext {

    private final DeviceDataService deviceDataService;

    private Logger logger;
    private Cryptographer cryptographer;
    private InboundDAO inboundDAO;
    private ComSessionBuilder sessionBuilder;
    private JournalEntryBacklog journalEntryBacklog = new JournalEntryBacklog();
    private final InboundComPort comPort;
    private ComPortRelatedComChannel comChannel;
    private HttpServletRequest servletRequest;
    private HttpServletResponse servletResponse;

    public InboundDiscoveryContextImpl(InboundComPort comPort, ComPortRelatedComChannel comChannel, DeviceDataService deviceDataService) {
        super();
        this.comPort = comPort;
        this.comChannel = comChannel;
        this.deviceDataService = deviceDataService;
    }

    public InboundDiscoveryContextImpl(InboundComPort comPort, HttpServletRequest servletRequest, HttpServletResponse servletResponse, DeviceDataService deviceDataService) {
        super();
        this.comPort = comPort;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.deviceDataService = deviceDataService;
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

    public InboundDAO getInboundDAO () {
        return inboundDAO;
    }

    public void setInboundDAO (InboundDAO inboundDAO) {
        this.inboundDAO = inboundDAO;
    }

    public ComSessionBuilder getComSessionBuilder() {
        return sessionBuilder;
    }

    public ComSessionBuilder buildComSession(ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Date startTime) {
        sessionBuilder = deviceDataService.buildComSession(connectionTask, comPortPool, comPort, startTime);
        this.journalEntryBacklog.createWith(sessionBuilder);
        return sessionBuilder;
    }

    public InboundComPort getComPort () {
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

    public void addJournalEntry(Date timestamp, String description, Throwable t) {
        if (this.sessionBuilder != null) {
            this.sessionBuilder.addJournalEntry(timestamp, description, t);
        }
        else {
            this.journalEntryBacklog.addJournalEntry(timestamp, description, t);
        }
    }

    private class JournalEntryBacklog {
        private List<JournalEntryBacklogEntry> entries = new ArrayList<>();

        private void addJournalEntry(Date timestamp, String description, Throwable t) {
            this.entries.add(new JournalEntryBacklogEntry(timestamp, description, t));
        }

        private void createWith (ComSessionBuilder builder) {
            for (JournalEntryBacklogEntry entry : this.entries) {
                entry.createWith(builder);
            }
            this.entries = new ArrayList<>();
        }
    }

    private class JournalEntryBacklogEntry {
        private Date timestamp;
        private String description;
        private Throwable thrown;

        private JournalEntryBacklogEntry(Date timestamp, String description, Throwable thrown) {
            super();
            this.timestamp = timestamp;
            this.description = description;
            this.thrown = thrown;
        }

        private void createWith (ComSessionBuilder builder) {
            builder.addJournalEntry(this.timestamp, this.description, this.thrown);
        }

    }

}