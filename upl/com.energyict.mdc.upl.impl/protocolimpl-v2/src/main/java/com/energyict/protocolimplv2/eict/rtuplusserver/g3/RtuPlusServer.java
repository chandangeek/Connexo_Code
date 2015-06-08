package com.energyict.protocolimplv2.eict.rtuplusserver.g3;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.SAPAssignmentItem;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.events.G3GatewayEvents;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.RtuPlusServerMessages;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayProperties;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.G3GatewayRegisters;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 9/04/13
 * Time: 16:00
 */
public class RtuPlusServer implements DeviceProtocol {

    private static final ObisCode SERIAL_NUMBER_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");
    private static final ObisCode FRAMECOUNTER_OBISCODE = ObisCode.fromString("0.0.43.1.1.255");
    protected G3GatewayProperties dlmsProperties;
    protected G3GatewayConfigurationSupport configurationSupport;
    protected OfflineDevice offlineDevice;
    protected DlmsSession dlmsSession;
    protected DeviceProtocolSecurityCapabilities dlmsSecuritySupport;
    protected RtuPlusServerMessages rtuPlusServerMessages;
    protected ComChannel comChannel = null;
    private G3GatewayRegisters g3GatewayRegisters;
    private G3GatewayEvents g3GatewayEvents;
    private DLMSCache dlmsCache = null;

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU+Server2 G3 DLMS";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    public DlmsSession getDlmsSession() {
        return dlmsSession;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.comChannel = comChannel;
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        initDlmsSession();
    }

    //Cryptoserver protocol overrides this
    protected void initDlmsSession() {
        readFrameCounter();
        dlmsSession = new DlmsSession(comChannel, getDlmsSessionProperties());
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.<ConnectionType>asList(new OutboundTcpIpConnectionType(), new InboundIpConnectionType());
    }

    @Override
    public void logOn() {
        getDlmsSession().connect();
        checkCacheObjects();
    }

    /**
     * First read out the frame counter for the management client, using the public client.
     * Note that this happens without setting up an association, since the it's pre-established for the public client.
     */
    protected void readFrameCounter() {
        TypedProperties clone = getDlmsSessionProperties().getProperties().clone();
        clone.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(16));
        G3GatewayProperties publicClientProperties = new G3GatewayProperties();
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(0, 0, clone));    //SecurityLevel 0:0

        DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties);
        publicDlmsSession.assumeConnected(publicClientProperties.getMaxRecPDUSize(), publicClientProperties.getConformanceBlock());
        long frameCounter;
        try {
            frameCounter = publicDlmsSession.getCosemObjectFactory().getData(FRAMECOUNTER_OBISCODE).getValueAttr().longValue();
        } catch (DataAccessResultException | ProtocolException e) {
            frameCounter = new Random().nextInt();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, publicDlmsSession);
        }

        //Read out the frame counter using the public client, it has a pre-established association
        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    @Override
    public void logOff() {
        if (getDlmsSession() != null) {
            getDlmsSession().disconnect();
        }
    }

    @Override
    public String getSerialNumber() {
        try {
            return getDlmsSession().getCosemObjectFactory().getData(SERIAL_NUMBER_OBISCODE).getString();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }

    @Override
    public Date getTime() {
        try {
            return getDlmsSession().getCosemObjectFactory().getClock().getDateTime();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            Calendar cal = Calendar.getInstance(getDlmsSession().getTimeZone());
            Date currentTime = new Date();
            cal.setTime(currentTime);
            getDlmsSession().getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(cal));
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> result = new ArrayList<>();
        for (OfflineRegister register : registers) {
            result.add(getG3GatewayRegisters().readRegister(register));
        }
        return result;
    }

    private G3GatewayRegisters getG3GatewayRegisters() {
        if (g3GatewayRegisters == null) {
            g3GatewayRegisters = new G3GatewayRegisters(getDlmsSession());
        }
        return g3GatewayRegisters;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierById(offlineDevice.getId()));

        List<SAPAssignmentItem> sapAssignmentList;      //List that contains the SAP id's and the MAC addresses of all logical devices (= gateway + slaves)
        try {
            sapAssignmentList = this.getDlmsSession().getCosemObjectFactory().getSAPAssignment().getSapAssignmentList();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
        for (SAPAssignmentItem sapAssignmentItem : sapAssignmentList) {     //Using callHomeId as a general property
            if (!isGatewayNode(sapAssignmentItem)) {      //Don't include the gateway itself
                DialHomeIdDeviceIdentifier slaveDeviceIdentifier = new DialHomeIdDeviceIdentifier(sapAssignmentItem.getLogicalDeviceName());
                deviceTopology.addSlaveDevice(slaveDeviceIdentifier);
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier,
                                MeterProtocol.NODEID,
                                sapAssignmentItem.getSap()
                        )
                );
            }
        }
        return deviceTopology;
    }

    private boolean isGatewayNode(SAPAssignmentItem sapAssignmentItem) {
        return sapAssignmentItem.getSap() == 1;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getG3GatewayEvents().readEvents(logBooks);
    }

    private G3GatewayEvents getG3GatewayEvents() {
        if (g3GatewayEvents == null) {
            g3GatewayEvents = new G3GatewayEvents(getDlmsSession());
        }
        return g3GatewayEvents;
    }

    protected RtuPlusServerMessages getRtuPlusServerMessages() {
        if (rtuPlusServerMessages == null) {
            rtuPlusServerMessages = new RtuPlusServerMessages(this.getDlmsSession(), offlineDevice);
        }
        return rtuPlusServerMessages;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getRtuPlusServerMessages().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getRtuPlusServerMessages().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getRtuPlusServerMessages().updateSentMessages(sentMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getRtuPlusServerMessages().format(propertySpec, messageAttribute);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new TcpDeviceProtocolDialect());
    }

    protected DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = new DsmrSecuritySupport();
        }
        return dlmsSecuritySupport;
    }

    /**
     * Holder for all properties: security, general and dialects.
     */
    protected G3GatewayProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new G3GatewayProperties();
        }
        return dlmsProperties;
    }

    @Override
    public void addProperties(TypedProperties properties) {
        getDlmsSessionProperties().addProperties(properties);
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getDlmsSessionProperties().addProperties(dialectProperties);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getDlmsSessionProperties().addProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
        getDlmsSessionProperties().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return getConfigurationSupport().getRequiredProperties();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return getConfigurationSupport().getOptionalProperties();
    }

    protected ConfigurationSupport getConfigurationSupport() {
        if (configurationSupport == null) {
            configurationSupport = new G3GatewayConfigurationSupport();
        }
        return configurationSupport;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return getSecuritySupport().getSecurityProperties();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return getSecuritySupport().getSecurityRelationTypeName();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return getSecuritySupport().getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return getSecuritySupport().getEncryptionAccessLevels();
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        return getSecuritySupport().getSecurityPropertySpec(name);
    }

    @Override
    public void daisyChainedLogOn() {
        logOn();
    }

    @Override
    public void daisyChainedLogOff() {
        logOff();
    }

    @Override
    public void terminate() {
        //Nothing to do here...
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return Collections.emptyList(); //Not supported
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return Collections.emptyList(); //Not supported
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return dlmsCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.dlmsCache = (DLMSCache) deviceProtocolCache;
    }

    private void checkCacheObjects() {
        if (getDeviceCache() == null) {
            setDeviceCache(new DLMSCache());
        }
        DLMSCache dlmsCache = (DLMSCache) getDeviceCache();
        if (dlmsCache.getObjectList() == null || getDlmsSessionProperties().isReadCache()) {
            readObjectList();
            dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
        } else {
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(dlmsCache.getObjectList());
        }
    }

    /**
     * Request Association buffer list out of the meter.
     */
    private void readObjectList() {
        try {
            if (getDlmsSession().getReference() == ProtocolLink.LN_REFERENCE) {
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDlmsSession().getCosemObjectFactory().getAssociationLN().getBuffer());
            } else if (getDlmsSession().getReference() == ProtocolLink.SN_REFERENCE) {
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDlmsSession().getCosemObjectFactory().getAssociationSN().getBuffer());
            } else {
                throw new IllegalArgumentException("Invalid reference method, only 0 and 1 are allowed.");
            }
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }
}