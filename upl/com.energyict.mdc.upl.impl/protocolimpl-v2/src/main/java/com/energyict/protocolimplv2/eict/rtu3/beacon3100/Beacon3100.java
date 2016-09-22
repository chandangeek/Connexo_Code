package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cbo.LastSeenDateInfo;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.dlms.cosem.SAPAssignmentItem;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.ip.socket.TLSConnectionType;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.*;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.GatewayTcpDeviceProtocolDialect;
import com.energyict.mdc.tasks.MirrorTcpDeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.DataEncryptionException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks.Beacon3100LogBookFactory;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.Beacon3100Messaging;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.registers.RegisterFactory;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.events.G3GatewayEvents;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;
import com.energyict.protocolimplv2.security.DlmsSecuritySuite1And2Support;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 18/06/2015 - 15:07
 */
public class Beacon3100 extends AbstractDlmsProtocol implements MigratePropertiesFromPreviousSecuritySet, AdvancedDeviceProtocolSecurityCapabilities {

    private static final ObisCode SERIAL_NUMBER_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");

    // https://confluence.eict.vpdc/display/G3IntBeacon3100/DLMS+management
    // https://jira.eict.vpdc/browse/COMMUNICATION-1552
    public static final ObisCode FRAMECOUNTER_OBISCODE_1_MNG = ObisCode.fromString("0.0.43.1.1.255");
    public static final ObisCode FRAMECOUNTER_OBISCODE_32_RW = ObisCode.fromString("0.0.43.1.2.255");
    public static final ObisCode FRAMECOUNTER_OBISCODE_64_FW = ObisCode.fromString("0.0.43.1.3.255");

    public static final int CLIENT_1_MNG = 1;
    public static final int CLIENT_32_RW = 32;
    public static final int CLIENT_64_MNG = 64;

    private static final String MIRROR_LOGICAL_DEVICE_PREFIX = "ELS-MIR-";
    private static final String GATEWAY_LOGICAL_DEVICE_PREFIX = "ELS-UGW-";
    private static final String UTF_8 = "UTF-8";
    private static final int MAC_ADDRESS_LENGTH = 8;    //In bytes

    private Beacon3100Messaging beacon3100Messaging;
    private G3GatewayEvents g3GatewayEvents;
    private RegisterFactory registerFactory;
    private Beacon3100LogBookFactory logBookFactory;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        getLogger().info("Start protocol for " + offlineDevice.getSerialNumber());
        getLogger().info("-version: "+getVersion());
        readFrameCounter(comChannel);
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));
    }
    /**
     * Will return the correct frame counter obis code, for each client ID.
     * Management Client (1): 0 0 43 1 1 255 -> With a pre-established framecounter association.
     * R/W Client (32): 0 0 43 1 2 255 -> With a pre-established framecounter association.
     * Firmware Client (64): 0 0 43 1 3 255 255 -> With a pre-established framecounter association.
     * https://jira.eict.vpdc/browse/COMMUNICATION-1552
     *
     * @param clientId - DLMS Client ID used in association
     * @return - the correct obis code for this client
     */
    protected ObisCode getFrameCounterObisCode(int clientId) {
        switch (clientId) {
            case CLIENT_32_RW:
                return FRAMECOUNTER_OBISCODE_32_RW;

            case CLIENT_64_MNG:
                return FRAMECOUNTER_OBISCODE_64_FW;
        }

        return FRAMECOUNTER_OBISCODE_1_MNG;
    }

    protected AdvancedDeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = new DlmsSecuritySuite1And2Support();
        }
        return (AdvancedDeviceProtocolSecurityCapabilities) dlmsSecuritySupport;
    }

    @Override
    public List<SecuritySuite> getSecuritySuites() {
        return getSecuritySupport().getSecuritySuites();
    }

    @Override
    public List<RequestSecurityLevel> getRequestSecurityLevels() {
        return getSecuritySupport().getRequestSecurityLevels();
    }

    @Override
    public List<ResponseSecurityLevel> getResponseSecurityLevels() {
        return getSecuritySupport().getResponseSecurityLevels();
    }

    /**
     * First read out the frame counter for the management client, using the public client. It has a pre-established association.
     * Note that this happens without setting up an association, since the it's pre-established for the public client.
     *
     * For EVN we'll read the frame counter using the frame counter provider custom method in the beacon
     */
    protected void readFrameCounter(ComChannel comChannel) {
        if (this.usesSessionKey()) {
        	//No need to read out the global FC if we're going to use a new session key in this AA.
        	return;
        }
        // construct a temporary session with 0:0 security and clientId=16 (public)
        final TypedProperties publicProperties = getDlmsSessionProperties().getProperties().clone();
        publicProperties.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(16));
        final Beacon3100Properties publicClientProperties = new Beacon3100Properties();
        publicClientProperties.addProperties(publicProperties);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(0, 0, 0, 0, 0, clone));    //SecurityLevel 0:0

        final DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties, getDlmsSessionProperties().getSerialNumber());
        final ObisCode frameCounterObisCode = this.getFrameCounterObisCode(getDlmsSessionProperties().getClientMacAddress());
        final long frameCounter;

        if (getDlmsSessionProperties().getRequestAuthenticatedFrameCounter()){
        	publicDlmsSession.getDlmsV2Connection().connectMAC();
        	publicDlmsSession.createAssociation();
            try {

                FrameCounterProvider frameCounterProvider = publicDlmsSession.getCosemObjectFactory().getFrameCounterProvider(frameCounterObisCode);
                frameCounter = frameCounterProvider.getFrameCounter(publicDlmsSession.getProperties().getSecurityProvider().getAuthenticationKey());

            } catch (IOException e) {
                    throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
            } catch (Exception e) {
                final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the framecounter, cannot continue! " + e.getMessage());
                throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
            } finally {
            	publicDlmsSession.disconnect();
            }
        } else {
        	/* Pre-established */
            publicDlmsSession.assumeConnected(publicClientProperties.getMaxRecPDUSize(), publicClientProperties.getConformanceBlock());
            try {
                frameCounter = publicDlmsSession.getCosemObjectFactory().getData(frameCounterObisCode).getValueAttr().longValue();
            } catch (IOException e) {
                throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
            }
            //frameCounter = new SecureRandom().nextInt();
        }
        this.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    /**
     * General ciphering (wrapped-key and agreed-key) are sessions keys
     */
    private boolean usesSessionKey() {
        return getDlmsSessionProperties().getCipheringType().equals(CipheringType.GENERAL_CIPHERING) && getDlmsSessionProperties().getGeneralCipheringKeyType() != GeneralCipheringKeyType.IDENTIFIED_KEY;
    }

    @Override
    public String getSerialNumber() {
        try {
            return getDlmsSession().getCosemObjectFactory().getData(SERIAL_NUMBER_OBISCODE).getString();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.<ConnectionType>asList(new OutboundTcpIpConnectionType(), new InboundIpConnectionType(), new TLSConnectionType());
    }

    @Override
    public String getProtocolDescription() {
        return "Elster EnergyICT Beacon3100 G3 DLMS";
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
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getBeacon3100LogBookFactory().getLogBookData(logBooks);
    }

    private Beacon3100LogBookFactory getBeacon3100LogBookFactory() {
        if (logBookFactory == null) {
            logBookFactory = new Beacon3100LogBookFactory(this);
        }
        return logBookFactory;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getBeacon3100Messaging().getSupportedMessages();
    }

    private Beacon3100Messaging getBeacon3100Messaging() {
        if (beacon3100Messaging == null) {
            beacon3100Messaging = new Beacon3100Messaging(this);
        }
        return beacon3100Messaging;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getBeacon3100Messaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getBeacon3100Messaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getBeacon3100Messaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return getBeacon3100Messaging().prepareMessageContext(offlineDevice, deviceMessage);
    }

    /**
     * There's 2 dialects:
     * - 1 used to communicate to the mirrored device (= cached meter data) in the Beacon DC
     * - 1 used to communicate straight to the actual meter, using the Beacon as a gateway.
     */
    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new MirrorTcpDeviceProtocolDialect(), new GatewayTcpDeviceProtocolDialect());
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    private RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new RegisterFactory(getDlmsSession());
        }
        return registerFactory;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierById(offlineDevice.getId()));

        List<SAPAssignmentItem> sapAssignmentList;      //List that contains the SAP id's and the MAC addresses of all logical devices (= gateway + slaves)
        final Array nodeList;
        try {
            sapAssignmentList = this.getDlmsSession().getCosemObjectFactory().getSAPAssignment().getSapAssignmentList();
            nodeList = this.getDlmsSession().getCosemObjectFactory().getG3NetworkManagement().getNodeList();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
        final List<G3Topology.G3Node> g3Nodes = G3Topology.convertNodeList(nodeList, this.getDlmsSession().getTimeZone());

        for (SAPAssignmentItem sapAssignmentItem : sapAssignmentList) {

            byte[] logicalDeviceNameBytes = sapAssignmentItem.getLogicalDeviceNameBytes();
            if (hasLogicalDevicePrefix(logicalDeviceNameBytes, GATEWAY_LOGICAL_DEVICE_PREFIX)) {
                byte[] logicalNameMacBytes = ProtocolTools.getSubArray(logicalDeviceNameBytes, GATEWAY_LOGICAL_DEVICE_PREFIX.length(), GATEWAY_LOGICAL_DEVICE_PREFIX.length() + MAC_ADDRESS_LENGTH);
                final String macAddress = ProtocolTools.getHexStringFromBytes(logicalNameMacBytes, "");

                final G3Topology.G3Node g3Node = findG3Node(macAddress, g3Nodes);
                if (g3Node != null) {
                    //Always include the slave information if it is present in the SAP assignment list and the G3 node list.
                    //It is the ComServer framework that will then do a smart update in EIServer, taking the readout LastSeenDate into account.

                    BigDecimal gatewayLogicalDeviceId = BigDecimal.valueOf(sapAssignmentItem.getSap());
                    BigDecimal mirrorLogicalDeviceId = BigDecimal.valueOf(findMatchingMirrorLogicalDevice(macAddress, sapAssignmentList));
                    BigDecimal lastSeenDate = BigDecimal.valueOf(g3Node.getLastSeenDate().getTime());
                    BigDecimal persistedGatewayLogicalDeviceId = null;
                    BigDecimal persistedMirrorLogicalDeviceId = null;
                    BigDecimal persistedLastSeenDate = null;
                    try{
                        getGeneralProperty(macAddress, AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID);
                        getGeneralProperty(macAddress, AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID);
                        getGeneralProperty(macAddress, G3Properties.PROP_LASTSEENDATE);

                    }catch (Exception ex){
                    }

                    DialHomeIdDeviceIdentifier slaveDeviceIdentifier = new DialHomeIdDeviceIdentifier(macAddress);  //Using callHomeId as a general property
                    LastSeenDateInfo lastSeenDateInfo = new LastSeenDateInfo(G3Properties.PROP_LASTSEENDATE, lastSeenDate);
                    deviceTopology.addSlaveDevice(slaveDeviceIdentifier, lastSeenDateInfo);

                    if (persistedGatewayLogicalDeviceId == null || !gatewayLogicalDeviceId.equals(persistedGatewayLogicalDeviceId)) {
                        deviceTopology.addAdditionalCollectedDeviceInfo(
                                MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                        slaveDeviceIdentifier,
                                        AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID,
                                        gatewayLogicalDeviceId
                                )
                        );
                    }
                    if (persistedMirrorLogicalDeviceId == null || !mirrorLogicalDeviceId.equals(persistedMirrorLogicalDeviceId)) {
                        deviceTopology.addAdditionalCollectedDeviceInfo(
                                MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                        slaveDeviceIdentifier,
                                        AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID,
                                        mirrorLogicalDeviceId
                                )
                        );
                    }
                    if (persistedLastSeenDate == null || !lastSeenDate.equals(persistedLastSeenDate)) {
                        deviceTopology.addAdditionalCollectedDeviceInfo(
                                MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                        slaveDeviceIdentifier,
                                        G3Properties.PROP_LASTSEENDATE,
                                        lastSeenDate
                                )
                        );
                    }
                }
            }
        }
        return deviceTopology;
    }

    /**
     * Return the general property with the given name, for the device with the given macAddress.
     * Return null if the device does not exist, or if the property does not exist.
     */
    private BigDecimal getGeneralProperty(String macAddress, String propertyName) {
        for (OfflineDevice offlineSlaveDevice : offlineDevice.getAllSlaveDevices()) {
            String callHomeId = offlineSlaveDevice.getAllProperties().getStringProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
            if (callHomeId != null && callHomeId.equals(macAddress)) {
                return offlineSlaveDevice.getAllProperties().getTypedProperty(propertyName);
            }
        }
        return null;
    }

    private G3Topology.G3Node findG3Node(final String macAddress, final List<G3Topology.G3Node> g3Nodes) {
        if (macAddress != null && g3Nodes != null && !g3Nodes.isEmpty()) {
            for (final G3Topology.G3Node g3Node : g3Nodes) {
                if (g3Node != null) {
                    final String nodeMac = ProtocolTools.getHexStringFromBytes(g3Node.getMacAddress(), "");
                    if (nodeMac.equalsIgnoreCase(macAddress)) {
                        return g3Node;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasLogicalDevicePrefix(byte[] logicalDeviceNameBytes, String expectedPrefix) {
        byte[] actualPrefixBytes = ProtocolTools.getSubArray(logicalDeviceNameBytes, 0, expectedPrefix.length());
        String actualPrefix = new String(actualPrefixBytes, Charset.forName(UTF_8));
        return actualPrefix.equals(expectedPrefix);
    }

    private long findMatchingMirrorLogicalDevice(String gatewayMacAddress, List<SAPAssignmentItem> sapAssignmentList) {
        for (SAPAssignmentItem sapAssignmentItem : sapAssignmentList) {
            byte[] logicalDeviceNameBytes = sapAssignmentItem.getLogicalDeviceNameBytes();
            if (hasLogicalDevicePrefix(logicalDeviceNameBytes, MIRROR_LOGICAL_DEVICE_PREFIX)) {
                byte[] logicalNameMacBytes = ProtocolTools.getSubArray(logicalDeviceNameBytes, MIRROR_LOGICAL_DEVICE_PREFIX.length(), MIRROR_LOGICAL_DEVICE_PREFIX.length() + MAC_ADDRESS_LENGTH);
                final String mirrorMacAddress = ProtocolTools.getHexStringFromBytes(logicalNameMacBytes, "");

                if (gatewayMacAddress.equals(mirrorMacAddress)) {
                    return sapAssignmentItem.getSap();
                }
            }
        }
        return -1;
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-09-02 14:48:00 +0200 (Mon, 11 Jul 2016)$";
    }

    @Override
    public void logOn() {
        getDlmsSession().connect();
        checkCacheObjects();
    }

    protected void checkCacheObjects() {
        if (getDeviceCache() == null) {
            setDeviceCache(new DLMSCache());
        }
        DLMSCache dlmsCache = getDeviceCache();
        if (dlmsCache.getObjectList() == null || getDlmsSessionProperties().isReadCache()) {
            readObjectList();
            dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
        } else {
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(dlmsCache.getObjectList());
        }
    }

    public Beacon3100Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new Beacon3100Properties();
        }
        return (Beacon3100Properties) dlmsProperties;
    }

    /**
     * A collection of general DLMS properties.
     * These properties are not related to the security or the protocol dialects.
     */
    protected ConfigurationSupport getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new Beacon3100ConfigurationSupport();
        }
        return dlmsConfigurationSupport;
    }

    /**
     * The used security support has changed from DlmsSecuritySupport to DlmsSecuritySuite1And2Support, so we indicate here
     * that indeed the DlmsSecuritySupport was the previous type of security support.
     * This is used by the ProtocolSecurityRelationTypeUpgrader to migrate old, existing security properties that were created for
     * the previous security relation to the new security relation.
     */
    @Override
    public DeviceProtocolSecurityCapabilities getPreviousSecuritySupport() {
        return new DsmrSecuritySupport();
    }
}