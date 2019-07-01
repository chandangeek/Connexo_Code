package com.energyict.protocolimplv2.nta.esmr50.common;


import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.*;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.*;
import com.energyict.mdc.upl.meterdata.*;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.esmr50.common.events.Esmr50LogBookFactory;
import com.energyict.protocolimplv2.nta.esmr50.common.loadprofiles.ESMR50LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.ESMR50MessageExecutor;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.ESMR50Messaging;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.ESMR50RegisterFactory;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public abstract class ESMR50Protocol extends AbstractSmartNtaProtocol {

    public static final ObisCode EMETER_LP1_OBISCODE = ObisCode.fromString("1.0.99.1.0.255");
    public static final ObisCode EMETER_LP2_OBISCODE_SAME_AS_MBUS_LP2 = ObisCode.fromString("1.0.99.2.0.255");
    public static final ObisCode EMETER_LP3_OBISCODE_SAME_AS_MBUS_LP3 = ObisCode.fromString("1.0.98.1.0.255");

    public static final ObisCode MBUS_LP1_OBISCODE = ObisCode.fromString("0.x.24.3.0.255");
    public static final ObisCode MBUS_LP2_OBISCODE_SAME_AS_EMETER_LP2 = ObisCode.fromString("0.x.99.2.0.255");
    public static final ObisCode MBUS_LP3_OBISCODE_SAME_AS_EMETER_LP3 = ObisCode.fromString("0.x.98.1.0.255");

    private final int PUBLIC_CLIENT_MAC_ADDRESS = 16;

    private final long UNSIGNED32_MAX = 0xFFFFFFFFL; // 4294967295;

    private static final ObisCode FRAME_COUNTER_OBISCODE = ObisCode.fromString("0.0.43.1.0.255");

    protected final NlsService nlsService;
    ESMR50Cache esmr50Cache;
    private Esmr50LogBookFactory esmr50LogBookFactory;
    ESMR50RegisterFactory registerFactory;
    private ESMR50Messaging esmr50Messaging;
    private ESMR50MessageExecutor esmr50MessageExecutor;
    protected final KeyAccessorTypeExtractor keyAccessorTypeExtractor;
    protected final Converter converter;
    protected final TariffCalendarExtractor calendarExtractor;
    protected final DeviceMessageFileExtractor messageFileExtractor;
    protected final NumberLookupExtractor numberLookupExtractor;
    protected final LoadProfileExtractor loadProfileExtractor;


    public ESMR50Protocol(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.calendarExtractor = calendarExtractor;
        this.nlsService = nlsService;
        this.converter = converter;
        this.messageFileExtractor = messageFileExtractor;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
        this.numberLookupExtractor = numberLookupExtractor;
        this.loadProfileExtractor = loadProfileExtractor;
    }

    @Override
    public String getVersion() {
        return "ESMR 5.0 - 2019-07-01";
    }

    @Override
    public void journal(String message) {
        super.journal("[ESMR50] " + message);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        journal("Sagemcom T210 protocol init");
        this.offlineDevice = offlineDevice;
        String serialNumber = getDlmsSessionProperties().getSerialNumber();
        getDlmsSessionProperties().setSerialNumber(serialNumber);
        journal("Initialize communication with device identified by serial number: " + serialNumber);
        if(!testCachedFrameCounterAndEstablishAssociation(comChannel)){
            readFrameCounter(comChannel);
            reEstablishAssociation(comChannel);
        } else {
            //Framecounter was validated and DLMSSession set so go on
            journal("Secure association established");
        }
        journal("Initialization phase has ended.");
    }

    private void reEstablishAssociation(ComChannel comChannel) {
        DlmsSession dlmsSession = newDlmsSession(comChannel);
        long initialFrameCounter = getDlmsSessionProperties().getSecurityProvider().getInitialFrameCounter();
        journal("Re-establishing secure association with frame counter "+initialFrameCounter);
        dlmsSession.getAso().getSecurityContext().setFrameCounter(initialFrameCounter);
        setDlmsSession(dlmsSession);
        if (dlmsSession.getAso().getAssociationStatus() != ApplicationServiceObject.ASSOCIATION_CONNECTED){
            try {
                dlmsSession.getDlmsV2Connection().connectMAC();
                dlmsSession.createAssociation();
                if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {
                    journal("Reconnection successful");
                }
            } catch (CommunicationException ex) {
                journal("Association using the new frame counter has failed ("+ex.getLocalizedMessage()+")");
            } catch (Exception ex){
                journal(Level.SEVERE, ex.getLocalizedMessage() + " caused by " + ex.getCause().getLocalizedMessage());
                throw ex;
            }
        } else {
            journal("Secure connection already established");
        }
    }

    protected DlmsSession newDlmsSession(ComChannel comChannel) {
        return new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger()); }

    protected boolean testCachedFrameCounterAndEstablishAssociation(ComChannel comChannel){
        boolean validCachedFrameCounter = false;
        DlmsSession dlmsSession = newDlmsSession(comChannel);
        long cachedFramecounter = getDeviceCache().getFrameCounter();
        journal("Testing cached frame counter: " + cachedFramecounter );
        checkFrameCounterLimits(cachedFramecounter);
        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(cachedFramecounter);
        dlmsSession.getAso().getSecurityContext().setFrameCounter(cachedFramecounter);
        try {
            dlmsSession.getDlmsV2Connection().connectMAC();
            dlmsSession.createAssociation();
            if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {
                long frameCounter = dlmsSession.getAso().getSecurityContext().getFrameCounter();
                journal("This FrameCounter was validated: " + frameCounter);
                getDeviceCache().setFrameCounter(frameCounter);
                validCachedFrameCounter = true;
                setDlmsSession(dlmsSession);
            }
        } catch (CommunicationException ex) {
            journal("Association using cached frame counter has failed.");
        } catch (Exception ex){
            journal(Level.SEVERE, ex.getLocalizedMessage() + " caused by " + ex.getCause().getLocalizedMessage());
            throw ex;
        }

        return validCachedFrameCounter;
    }

    protected void readFrameCounter(ComChannel comChannel) {

        if (getDlmsSessionProperties().usesPublicClient()) {
            return;
        }

        journal("Starting public DLMS session to read the frame counter.");

        TypedProperties clone = getDlmsSessionProperties().getProperties().clone();
        clone.setProperty(com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(PUBLIC_CLIENT_MAC_ADDRESS));
        ESMR50Properties publicClientProperties = new ESMR50Properties(this.getPropertySpecService());
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(PUBLIC_CLIENT_MAC_ADDRESS), 0, 0, 0, 0, 0, clone));    //SecurityLevel 0:0

        long frameCounter = 0;
        DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties);
        journal("Connecting to public client: " + PUBLIC_CLIENT_MAC_ADDRESS);
        connectWithRetries(publicDlmsSession);
        try {
            ObisCode frameCounterObisCode = getFrameCounterForClient(getDlmsSessionProperties().getClientMacAddress());
            journal("Public client connected, reading framecounter " + frameCounterObisCode.toString() + ", corresponding to client "+getDlmsSessionProperties().getClientMacAddress());
            frameCounter = publicDlmsSession.getCosemObjectFactory().getData(frameCounterObisCode).getValueAttr().longValue();
            journal("Frame counter received: " + frameCounter);
            checkFrameCounterLimits(frameCounter);
        } catch (DataAccessResultException | ProtocolException e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the framecounter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
        }
        journal("Disconnecting public client");
        publicDlmsSession.disconnect();
        long incrementedFramecounter = frameCounter + 1;
        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(incrementedFramecounter);
    }

    private boolean checkFrameCounterLimits(long frameCounter) {

        long frameCounterLimit = getDlmsSessionProperties().getFrameCounterLimit();

        if (frameCounterLimit>0 && frameCounter>frameCounterLimit) {
            journal(Level.WARNING, "Frame-counter is above the threshold (" + frameCounterLimit + "), consider key-roll to reset it!");
        }

        if (frameCounter<=0 || frameCounter >= UNSIGNED32_MAX){
            journal(Level.SEVERE, "Frame counter "+frameCounter+" is not within Unsigned32 acceptable limits, consider key-roll to reset it");
            return false;
        }
        return true;
    }

    private ObisCode getFrameCounterForClient(int clientMacAddress) {
        return FRAME_COUNTER_OBISCODE;
    }

    @Override
    public ESMR50Cache getDeviceCache() {
        if(esmr50Cache == null){
            esmr50Cache = new ESMR50Cache();
        }
        return esmr50Cache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if (deviceProtocolCache instanceof ESMR50Cache) {
            esmr50Cache = (ESMR50Cache) deviceProtocolCache;
        }
    }

    @Override
    protected void checkCacheObjects(){
        if (getDeviceCache() == null) {
            setDeviceCache(new ESMR50Cache());
        }
        DLMSCache dlmsCache = getDeviceCache();
        if (dlmsCache.getObjectList() == null || getDlmsSessionProperties().isReadCache()) {
            journal("Cache is empty or Read Cache property is set. Reading device object list.");
            readObjectList();
            dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
        } else {
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(dlmsCache.getObjectList());
            journal("Cache exist, will not be read.");
        }
    }

    @Override
    public void terminate(){
        if(getDlmsSession() != null && getDlmsSession().getAso().getSecurityContext() != null){
            long frameCounter = getDlmsSession().getAso().getSecurityContext().getFrameCounter();
            getDeviceCache().setFrameCounter(frameCounter);
            journal("Caching frameCounter=" + frameCounter);

        }
    }

    public ESMR50Properties getDlmsSessionProperties(){
        if(dlmsProperties == null){
            dlmsProperties = new ESMR50Properties(this.getPropertySpecService());
        }
        return (ESMR50Properties) dlmsProperties;
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if(dlmsConfigurationSupport == null){
            dlmsConfigurationSupport = new ESMR50ConfigurationSupport(this.getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return super.getUPLPropertySpecs();
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public List<? extends ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType(this.getPropertySpecService()));
        return result;
    }

    @Override
    public String getProtocolDescription() {
        return "Enexis Sagemcom T210 protocol";
    }

    @Override
    public List<? extends DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new TcpDeviceProtocolDialect(this.getPropertySpecService(), this.nlsService));
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }


    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBookReaders) {
        return getEsmr50LogBookFactory().getLogBookData(logBookReaders);
    }

    private Esmr50LogBookFactory getEsmr50LogBookFactory() {
        if (esmr50LogBookFactory == null) {
            esmr50LogBookFactory = new Esmr50LogBookFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return esmr50LogBookFactory;
    }

    @Override
    protected LoadProfileBuilder getLoadProfileBuilder(){
        if(this.loadProfileBuilder == null){
            loadProfileBuilder = new ESMR50LoadProfileBuilder(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return this.loadProfileBuilder;
    }

    @Override
    public DeviceRegisterSupport getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new ESMR50RegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    protected ESMR50Messaging getMessaging() {
        if (this.esmr50Messaging == null) {
            this.esmr50Messaging = new ESMR50Messaging(getMessageExecutor(), this.getPropertySpecService(), this.nlsService, this.converter, this.messageFileExtractor, this.calendarExtractor, this.numberLookupExtractor, this.loadProfileExtractor, this.keyAccessorTypeExtractor);
        }
        return this.esmr50Messaging;
    }

    protected ESMR50MessageExecutor getMessageExecutor() {
        if (this.esmr50MessageExecutor == null) {
            this.esmr50MessageExecutor = new ESMR50MessageExecutor(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.keyAccessorTypeExtractor);
        }
        return this.esmr50MessageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getMessaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessaging().executePendingMessages(pendingMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessaging().updateSentMessages(sentMessages);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public ObisCode getFirmwareVersionCommsModuleObisCode(){
        return ESMR50RegisterFactory.ACTIVE_MODEM_FIRMWARE_VERSION_OBISCODE;
    }

   //TODO This method must be overriden in all nta protocols. It had a different implementation in 8.11 AbstractSmartDlmsProtocol than in AbstractDlmsProtcol from connexo
    /**
     * E-meter has address 0. Subclasses can override to add MBus address functionality.
     */
    @Override
    public int getPhysicalAddressFromSerialNumber(final String serialNumber) {
        return getMeterTopology().getPhysicalAddress(serialNumber);
    }

    public void resetFrameCounter(long newFrameCounter) {
        getProperties().getSecurityProvider().setInitialFrameCounter(newFrameCounter);
        if (getDlmsSession().getDLMSConnection()!=null) {
            getDlmsSession().getAso().getSecurityContext().setFrameCounter(newFrameCounter);
        }
        ((ESMR50Cache)getDeviceCache()).setFrameCounter(newFrameCounter);
    }

}


