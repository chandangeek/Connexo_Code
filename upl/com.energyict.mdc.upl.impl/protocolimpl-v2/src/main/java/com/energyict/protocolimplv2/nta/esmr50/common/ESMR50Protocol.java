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
import com.energyict.mdc.upl.meterdata.*;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
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
import com.energyict.protocolimplv2.nta.esmr50.common.registers.ESMR50RegisterFactory;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class ESMR50Protocol extends AbstractSmartNtaProtocol {

    public static final ObisCode EMETER_LP1_OBISCODE = ObisCode.fromString("1.0.99.1.0.255");
    public static final ObisCode EMETER_LP2_OBISCODE_SAME_AS_MBUS_LP2 = ObisCode.fromString("1.0.99.2.0.255");
    public static final ObisCode EMETER_LP3_OBISCODE_SAME_AS_MBUS_LP3 = ObisCode.fromString("1.0.98.1.0.255");

    public static final ObisCode MBUS_LP1_OBISCODE = ObisCode.fromString("0.x.24.3.0.255");
    public static final ObisCode MBUS_LP2_OBISCODE_SAME_AS_EMETER_LP2 = ObisCode.fromString("0.x.99.2.0.255");
    public static final ObisCode MBUS_LP3_OBISCODE_SAME_AS_EMETER_LP3 = ObisCode.fromString("0.x.98.1.0.255");

    private final int PUBLIC_CLIENT_MAC_ADDRESS = 16;

    private static final ObisCode FRAME_COUNTER_OBISCODE = ObisCode.fromString("0.0.43.1.0.255");

    private final NlsService nlsService;
    ESMR50Cache esmr50Cache;
    private Esmr50LogBookFactory esmr50LogBookFactory;
    ESMR50RegisterFactory registerFactory;

    public ESMR50Protocol(PropertySpecService propertySpecService, NlsService nlsService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.nlsService = nlsService;
        this.registerFactory = new ESMR50RegisterFactory(this, collectedDataFactory, issueFactory);
    }

    @Override
    public String getVersion() {
        return "Enexis first protocol integration version 10.12.2018";
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        getLogger().info("Sagemcom T210 protocol init V2");
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(getDlmsSessionProperties().getDeviceId());
        getLogger().info("Initialize communication with device identified by device ID: " + getDlmsSessionProperties().getDeviceId());
        if(testCachedFrameCounter(comChannel)){
            DlmsSession dlmsSession = new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger());
            dlmsSession.getAso().getSecurityContext().setFrameCounter(getDeviceCache().getFrameCounter());
            setDlmsSession(dlmsSession);
        } else {
            readFrameCounter(comChannel);
            DlmsSession dlmsSession = new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger());
            dlmsSession.getAso().getSecurityContext().setFrameCounter(getDlmsSessionProperties().getSecurityProvider().getInitialFrameCounter());
            setDlmsSession(dlmsSession);
        }
        getLogger().info("Initialization phase has ended.");

    }

    private boolean testCachedFrameCounter(ComChannel comChannel){
        boolean validCachedFrameCounter = false;
        DlmsSession dlmsSession = new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger());
        long cachedFramecounter = getDeviceCache().getFrameCounter();
        getLogger().info("Testing cached frame counter: " + cachedFramecounter );
        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(cachedFramecounter);
        dlmsSession.getAso().getSecurityContext().setFrameCounter(cachedFramecounter);
        try {
            dlmsSession.getDlmsV2Connection().connectMAC();
            dlmsSession.createAssociation();
            if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {
                dlmsSession.disconnect();
                long frameCounter = dlmsSession.getAso().getSecurityContext().getFrameCounter();
                getLogger().info("This FrameCounter was validated: " + frameCounter);
                getDeviceCache().setFrameCounter(frameCounter);
                validCachedFrameCounter = true;
            }
        } catch (CommunicationException ex) {
            getLogger().info("Association using cached frame counter has failed.");
        }
        return validCachedFrameCounter;
    }

    protected void readFrameCounter(ComChannel comChannel) {

        if (getDlmsSessionProperties().usesPublicClient()) {
            return;
        }

        getLogger().info("Starting public DLMS session to read the frame counter.");

        TypedProperties clone = getDlmsSessionProperties().getProperties().clone();
        clone.setProperty(com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(PUBLIC_CLIENT_MAC_ADDRESS));
        ESMR50Properties publicClientProperties = new ESMR50Properties(this.getPropertySpecService());
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(PUBLIC_CLIENT_MAC_ADDRESS), 0, 0, 0, 0, 0, clone));    //SecurityLevel 0:0

        long frameCounter;
        DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties);
        getLogger().info("Connecting to public client: " + PUBLIC_CLIENT_MAC_ADDRESS);
        connectWithRetries(publicDlmsSession);
        try {
            ObisCode frameCounterObisCode = getFrameCounterForClient(getDlmsSessionProperties().getClientMacAddress());
            getLogger().info("Public client connected, reading framecounter " + frameCounterObisCode.toString() + ", corresponding to client "+getDlmsSessionProperties().getClientMacAddress());
            frameCounter = publicDlmsSession.getCosemObjectFactory().getData(frameCounterObisCode).getValueAttr().longValue();
            getLogger().info("Frame counter received: " + frameCounter);
        } catch (DataAccessResultException | ProtocolException e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the framecounter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
        }
        getLogger().info("Disconnecting public client");
        publicDlmsSession.disconnect();
        long incrementedFramecounter = frameCounter + 1;
        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(incrementedFramecounter);
//        getDlmsSession().getAso().getSecurityContext().setFrameCounter(incrementedFramecounter);
//        getDeviceCache().setFrameCounter(incrementedFramecounter);
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
        if(deviceProtocolCache != null && deviceProtocolCache instanceof ESMR50Cache){
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
            getLogger().info("Cache is empty or Read Cache property is set. Reading device object list.");
            readObjectList();
            dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
        } else {
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(dlmsCache.getObjectList());
            getLogger().info("Cache exist, will not be read.");
        }
    }

    @Override
    public void terminate(){
        if(getDlmsSession() != null && getDlmsSession().getAso().getSecurityContext() != null){
            long frameCounter = getDlmsSession().getAso().getSecurityContext().getFrameCounter();
            getDeviceCache().setFrameCounter(frameCounter);
            getLogger().info("Caching frameCounter=" + frameCounter);

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

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return new ArrayList<>();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return null;
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return null;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return null;
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }
}


