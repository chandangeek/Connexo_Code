package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.upl.DeviceGroupExtractor;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.ObjectMapperService;
import com.energyict.mdc.upl.crypto.KeyStoreService;
import com.energyict.mdc.upl.crypto.X509Service;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.CertificateAliasFinder;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Holds contextual information for an {@link InboundDeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-15 (09:15)
 */
public class InboundDiscoveryContextImpl implements InboundDiscoveryContext {

    private final ConnectionTaskService connectionTaskService;
    private final InboundComPort comPort;
    private Logger logger;
    private ComServerDAO comServerDAO;
    private ComSessionBuilder sessionBuilder;
    private JournalEntryBacklog journalEntryBacklog = new JournalEntryBacklog();
    private ComPortRelatedComChannel comChannel;
    private HttpServletRequest servletRequest;
    private HttpServletResponse servletResponse;
    private boolean allCollectedDataWasProcessed = true;
    private boolean encryptionRequired;

    private ObjectMapperService objectMapperService;
    private CollectedDataFactory collectedDataFactory;
    private IssueFactory issueFactory;
    private PropertySpecService propertySpecService;
    private NlsService nlsService;
    private Converter converter;
    private DeviceGroupExtractor deviceGroupExtractor;
    private DeviceMasterDataExtractor deviceMasterDataExtractor;
    private DeviceExtractor deviceExtractor;
    private DeviceMessageFileExtractor messageFileExtractor;
    private KeyStoreService keyStoreService;
    private X509Service x509Service;
    private CertificateWrapperExtractor certificateWrapperExtractor;
    private CertificateAliasFinder certificateAliasFinder;


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
    public boolean encryptionRequired() {
        return encryptionRequired;
    }

    @Override
    public void markEncryptionRequired() {
        encryptionRequired = true;
    }

    @Override
    public CollectedDataFactory getCollectedDataFactory() {
        return collectedDataFactory;
    }

    public void setCollectedDataFactory(CollectedDataFactory collectedDataFactory) {
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public ObjectMapperService getObjectMapperService() {
        return objectMapperService;
    }

    public void setObjectMapperService(ObjectMapperService objectMapperService) {
        this.objectMapperService = objectMapperService;
    }

    @Override
    public IssueFactory getIssueFactory() {
        return issueFactory;
    }

    public void setIssueFactory(IssueFactory issueFactory) {
        this.issueFactory = issueFactory;
    }

    @Override
    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public NlsService getNlsService() {
        return nlsService;
    }

    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Override
    public Converter getConverter() {
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public com.energyict.mdc.upl.InboundDAO getInboundDAO() {
        return comServerDAO;
    }

    public void setComServerDAO(ComServerDAO comServerDAO) {
        this.comServerDAO = comServerDAO;
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
    public DeviceGroupExtractor getDeviceGroupExtractor() {
        return deviceGroupExtractor;
    }

    public void setDeviceGroupExtractor(DeviceGroupExtractor deviceGroupExtractor) {
        this.deviceGroupExtractor = deviceGroupExtractor;
    }

    @Override
    public DeviceMasterDataExtractor getDeviceMasterDataExtractor() {
        return deviceMasterDataExtractor;
    }

    public void setDeviceMasterDataExtractor(DeviceMasterDataExtractor deviceMasterDataExtractor) {
        this.deviceMasterDataExtractor = deviceMasterDataExtractor;
    }

    @Override
    public DeviceExtractor getDeviceExtractor() {
        return deviceExtractor;
    }

    public void setDeviceExtractor(DeviceExtractor deviceExtractor) {
        this.deviceExtractor = deviceExtractor;
    }

    @Override
    public DeviceMessageFileExtractor getMessageFileExtractor() {
        return messageFileExtractor;
    }

    public void setMessageFileExtractor(DeviceMessageFileExtractor messageFileExtractor) {
        this.messageFileExtractor = messageFileExtractor;
    }

    @Override
    public KeyStoreService getKeyStoreService() {
        return keyStoreService;
    }

    public void setKeyStoreService(KeyStoreService keyStoreService) {
        this.keyStoreService = keyStoreService;
    }

    @Override
    public CertificateWrapperExtractor getCertificateWrapperExtractor() {
        return certificateWrapperExtractor;
    }

    public void setCertificateWrapperExtractor(CertificateWrapperExtractor certificateWrapperExtractor) {
        this.certificateWrapperExtractor = certificateWrapperExtractor;
    }

    @Override
    public CertificateAliasFinder getCertificateAliasFinder() {
        return certificateAliasFinder;
    }

    public void setCertificateAliasFinder(CertificateAliasFinder certificateAliasFinder) {
        this.certificateAliasFinder = certificateAliasFinder;
    }

    @Override
    public X509Service getX509Service() {
        return x509Service;
    }

    public void setX509Service(X509Service x509Service) {
        this.x509Service = x509Service;
    }

    @Override
    public Optional<List<? extends com.energyict.mdc.upl.security.SecurityProperty>> getProtocolSecurityProperties(DeviceIdentifier deviceIdentifier) {
        return Optional.ofNullable(comServerDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, getComPort()));
    }

    @Override
    public Optional<TypedProperties> getDeviceDialectProperties(DeviceIdentifier deviceIdentifier) {
        return Optional.ofNullable(comServerDAO.getDeviceDialectProperties(deviceIdentifier, getComPort()));
    }

    @Override
    public Optional<Boolean> isInboundOnHold(DeviceIdentifier deviceIdentifier) {
        return Optional.ofNullable(comServerDAO.getInboundComTaskOnHold(deviceIdentifier, this.comPort));
    }

    @Override
    public Optional<com.energyict.mdc.upl.properties.TypedProperties> getConnectionTypeProperties(DeviceIdentifier deviceIdentifier) {
        return Optional.ofNullable(comServerDAO.getDeviceConnectionTypeProperties(deviceIdentifier, this.comPort));
    }

    @Override
    public void markNotAllCollectedDataWasProcessed() {
        this.allCollectedDataWasProcessed = false;
    }

    public boolean isAllCollectedDataWasProcessed() {
        return allCollectedDataWasProcessed;
    }

    void addJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String description, Throwable t) {
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