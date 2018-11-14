package com.energyict.protocolimplv2.nta.esmr50.sagemcom;

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
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50ConfigurationSupport;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Properties;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Cache;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class T210 extends AbstractDlmsProtocol implements SerialNumberSupport {


    private final NlsService nlsService;
    private long cachedFrameCounter = -1;
    private final int PUBLIC_CLIENT_MAC_ADDRESS = 16;
    private static final ObisCode FRAME_COUNTER_OBISCODE = ObisCode.fromString("0.0.43.1.0.255");
    ESMR50Cache esmr50Cache;


    public T210(PropertySpecService propertySpecService, NlsService nlsService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.nlsService = nlsService;
    }

    @Override
    public String getVersion() {
        return "Enexis first protocol integration version 10.10.2018";
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        getLogger().info("Sagemcom T210 protocol init V2");
        this.offlineDevice = offlineDevice;
        String deviceSerialNumber = offlineDevice.getSerialNumber();
        getDlmsSessionProperties().setSerialNumber(deviceSerialNumber);
        getLogger().info("Initialize communication with device identified by serial number: " + deviceSerialNumber);
        readFrameCounter(comChannel);
        DlmsSession dlmsSession = new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger());
        setDlmsSession(dlmsSession);
    }

    private boolean testCachedFrameCounter(DlmsSession dlmsSession, int cachedFrameCounter){
        getLogger().info("Testing frame counter: " + cachedFrameCounter );
        dlmsSession.getDlmsV2Connection().connectMAC();
        dlmsSession.createAssociation();
        if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {
            dlmsSession.disconnect();
//            getLogger().info("This FrameCounter was validated: " + testDlmsSession.getAso().getSecurityContext().getFrameCounter());
//            setTXFrameCounter(dlmsSession.getAso().getSecurityContext().getFrameCounter());
            return true;
        }
        return true;
    }

    protected void readFrameCounter(ComChannel comChannel) {
        if (getDlmsSessionProperties().usesPublicClient()) {
            return;
        }

        TypedProperties clone = getDlmsSessionProperties().getProperties().clone();
        clone.setProperty(com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(PUBLIC_CLIENT_MAC_ADDRESS));
        ESMR50Properties publicClientProperties = new ESMR50Properties(this.getPropertySpecService());
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(PUBLIC_CLIENT_MAC_ADDRESS), 0, 0, 0, 0, 0, clone));    //SecurityLevel 0:0

        long frameCounter;
        DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties);
        getLogger().info("Connecting to public client:" + PUBLIC_CLIENT_MAC_ADDRESS);
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

        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    private ObisCode getFrameCounterForClient(int clientMacAddress) {
        return FRAME_COUNTER_OBISCODE;
    }

    /**
     * Actually create an association to the public client, it is not pre-established
     */
    protected void connectToPublicClient(DlmsSession publicDlmsSession) {
        connectWithRetries(publicDlmsSession);
    }

    private ESMR50Properties getNewInstanceOfProperties() {
        return new ESMR50Properties(this.getPropertySpecService());
    }

    private void sleep() {
        try {
            getLogger().finest("... sleeping 5 seconds to allow the meter to settle ...");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DLMSCache getDeviceCache() {
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
        return null;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return null;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return null;
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

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return null;
    }

}
