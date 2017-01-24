package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LastSeenDateInfo;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.legacy.dynamic.ConfigurationSupport;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.io.ConnectionCommunicationException;
import com.energyict.protocols.impl.channels.ip.InboundIpConnectionType;
import com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.protocols.mdc.protocoltasks.GatewayTcpDeviceProtocolDialect;
import com.energyict.protocols.mdc.protocoltasks.MirrorTcpDeviceProtocolDialect;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.dlms.cosem.SAPAssignmentItem;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.DlmsProperties;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks.Beacon3100LogBookFactory;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.registers.RegisterFactory;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.events.G3GatewayEvents;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 06/10/16
 * Time: 13:32
 */
public class Beacon3100 extends AbstractDlmsProtocol {

    // https://confluence.eict.vpdc/display/G3IntBeacon3100/DLMS+management
    // https://jira.eict.vpdc/browse/COMMUNICATION-1552
    public static final ObisCode FRAMECOUNTER_OBISCODE_1_MNG = ObisCode.fromString("0.0.43.1.1.255");
    public static final ObisCode FRAMECOUNTER_OBISCODE_32_RW = ObisCode.fromString("0.0.43.1.2.255");
    public static final ObisCode FRAMECOUNTER_OBISCODE_64_FW = ObisCode.fromString("0.0.43.1.3.255");
    public static final int CLIENT_1_MNG = 1;
    public static final int CLIENT_32_RW = 32;
    public static final int CLIENT_64_MNG = 64;
    private static final ObisCode SERIAL_NUMBER_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");
    private static final String MIRROR_LOGICAL_DEVICE_PREFIX = "ELS-MIR-";
    private static final String GATEWAY_LOGICAL_DEVICE_PREFIX = "ELS-UGW-";
    private static final String UTF_8 = "UTF-8";
    private static final int MAC_ADDRESS_LENGTH = 8;    //In bytes

    //    private Beacon3100Messaging beacon3100Messaging;
    private G3GatewayEvents g3GatewayEvents;
    private RegisterFactory registerFactory;
    private Beacon3100LogBookFactory logBookFactory;
    protected ConfigurationSupport dlmsConfigurationSupport;
    private AM540ConfigurationSupport am540ConfigurationSupport;

    @Inject
    public Beacon3100(Clock clock, Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService, IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, LoadProfileFactory loadProfileFactory, Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider) {
        super(clock, thesaurus, propertySpecService, socketService, serialComponentService, issueService, topologyService, readingTypeUtilService, identificationService, collectedDataFactory, meteringService, loadProfileFactory, dsmrSecuritySupportProvider);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsProperties().setSerialNumber(offlineDevice.getSerialNumber());
        getLogger().info("Start protocol for " + offlineDevice.getSerialNumber());
        getLogger().info("-version: " + getVersion());
        readFrameCounter(comChannel);
        setDlmsSession(new DlmsSession(comChannel, getDlmsProperties()));
    }


    /**
     * First read out the frame counter for the management client, using the public client. It has a pre-established association.
     * Note that this happens without setting up an association, since the it's pre-established for the public client.
     * <p>
     * For EVN we'll read the frame counter using the frame counter provider custom method in the beacon
     */
    protected void readFrameCounter(ComChannel comChannel) {
        if (this.usesSessionKey()) {
            //No need to read out the global FC if we're going to use a new session key in this AA.
            return;
        }
        // construct a temporary session with 0:0 security and clientId=16 (public)
        final TypedProperties publicProperties = getDlmsProperties().getProperties().clone();
        publicProperties.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(16));
        final Beacon3100Properties publicClientProperties = new Beacon3100Properties(propertySpecService, thesaurus);
        publicClientProperties.addProperties(publicProperties);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySet() {
            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return 0;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return 0;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return publicProperties;
            }
        });    //SecurityLevel 0:0

        final DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties, getDlmsProperties().getSerialNumber());
        final ObisCode frameCounterObisCode = this.getFrameCounterObisCode(getDlmsProperties().getClientMacAddress());
        final long frameCounter;

        if (getDlmsProperties().getRequestAuthenticatedFrameCounter()) {
            getLogger().finest("Requesting authenticated frame counter");
            try {
                publicDlmsSession.getDlmsV2Connection().connectMAC();
                publicDlmsSession.createAssociation();

                FrameCounterProvider frameCounterProvider = publicDlmsSession.getCosemObjectFactory().getFrameCounterProvider(frameCounterObisCode);
                frameCounter = frameCounterProvider.getFrameCounter(publicDlmsSession.getProperties().getSecurityProvider().getAuthenticationKey());
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, e.getCause() + e.getMessage(), e);
                throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, e.getCause() + e.getMessage(), e);
                final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the frame counter, cannot continue! " + e.getMessage());
                throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, protocolException);
            } finally {
                publicDlmsSession.disconnect();
            }
        } else {
            /* Pre-established */
            getLogger().finest("Reading frame counter with the public pre-established association");
            publicDlmsSession.assumeConnected(publicClientProperties.getMaxRecPDUSize(), publicClientProperties.getConformanceBlock());
            try {
                frameCounter = publicDlmsSession.getCosemObjectFactory().getData(frameCounterObisCode).getValueAttr().longValue();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, e.getCause() + e.getMessage(), e);
                throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
            }
            //frameCounter = new SecureRandom().nextInt();
        }
        this.getDlmsProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    public Beacon3100Properties getDlmsProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new Beacon3100Properties(propertySpecService, thesaurus);
        }
        return (Beacon3100Properties) dlmsProperties;
    }

    @Override
    protected ConfigurationSupport getDlmsConfigurationSupport() {
        if (this.dlmsConfigurationSupport == null) {
            this.dlmsConfigurationSupport = new Beacon3100ConfigurationSupport(propertySpecService);
        }
        return this.dlmsConfigurationSupport;
    }

    /**
     * General ciphering (wrapped-key and agreed-key) are sessions keys
     */
    private boolean usesSessionKey() {
        return getDlmsProperties().getCipheringType()
                .equals(CipheringType.GENERAL_CIPHERING) && getDlmsProperties().getGeneralCipheringKeyType() != GeneralCipheringKeyType.IDENTIFIED_KEY;
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
        return Arrays.<ConnectionType>asList(
                new OutboundTcpIpConnectionType(thesaurus, propertySpecService, getSocketService()),
                new InboundIpConnectionType());
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
            logBookFactory = new Beacon3100LogBookFactory(this, getCollectedDataFactory(), getIssueService(), getMeteringService());
        }
        return logBookFactory;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        //TODO if required, add some ...
        return Collections.emptySet();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.getCollectedDataFactory().createCollectedMessageList(sentMessages);
    }

//    @Override
//    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
//        return getBeacon3100Messaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
//    }
//
//    @Override
//    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
//        return getBeacon3100Messaging().prepareMessageContext(offlineDevice, deviceMessage);
//    }

    /**
     * There's 2 dialects:
     * - 1 used to communicate to the mirrored device (= cached meter data) in the Beacon DC
     * - 1 used to communicate straight to the actual meter, using the Beacon as a gateway.
     */
    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(
                new MirrorTcpDeviceProtocolDialect(thesaurus, propertySpecService),
                new GatewayTcpDeviceProtocolDialect(thesaurus, propertySpecService));
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    protected RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new RegisterFactory(getDlmsSession(), getIssueService(), getCollectedDataFactory());
        }
        return registerFactory;
    }


    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = getCollectedDataFactory().createCollectedTopology(offlineDevice.getDeviceIdentifier());

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
                    try {
                        getGeneralProperty(macAddress, AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID);
                        getGeneralProperty(macAddress, AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID);
                        getGeneralProperty(macAddress, G3Properties.PROP_LASTSEENDATE);

                    } catch (Exception ex) {
                    }

                    DeviceIdentifier slaveDeviceIdentifier = getIdentificationService().createDeviceIdentifierByProperty(DlmsProperties.CALL_HOME_ID_PROPERTY_NAME, macAddress);
                    LastSeenDateInfo lastSeenDateInfo = new LastSeenDateInfo(G3Properties.PROP_LASTSEENDATE, lastSeenDate);
                    deviceTopology.addSlaveDevice(slaveDeviceIdentifier);

                    if (persistedGatewayLogicalDeviceId == null || !gatewayLogicalDeviceId.equals(persistedGatewayLogicalDeviceId)) {
                        deviceTopology.addAdditionalCollectedDeviceInfo(
                                getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                        slaveDeviceIdentifier,
                                        getSlaveConfigurationSupport().actualLogicalDeviceIdPropertySpec(),
                                        gatewayLogicalDeviceId
                                )
                        );
                    }
                    if (persistedMirrorLogicalDeviceId == null || !mirrorLogicalDeviceId.equals(persistedMirrorLogicalDeviceId)) {
                        deviceTopology.addAdditionalCollectedDeviceInfo(
                                getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                        slaveDeviceIdentifier,
                                        getSlaveConfigurationSupport().mirrorLogicalDeviceIdPropertySpec(),
                                        mirrorLogicalDeviceId
                                )
                        );
                    }
                    if (persistedLastSeenDate == null || !lastSeenDate.equals(persistedLastSeenDate)) {
                        deviceTopology.addAdditionalCollectedDeviceInfo(
                                getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                        slaveDeviceIdentifier,
                                        getSlaveConfigurationSupport().lastSeenDatePropertySpec(),
                                        lastSeenDate
                                )
                        );
                    }
                }
            }
        }
        return deviceTopology;
    }

    private AM540ConfigurationSupport getSlaveConfigurationSupport() {
        if (am540ConfigurationSupport == null) {
            am540ConfigurationSupport = new AM540ConfigurationSupport(propertySpecService);
        }
        return am540ConfigurationSupport;
    }

    private boolean hasLogicalDevicePrefix(byte[] logicalDeviceNameBytes, String expectedPrefix) {
        byte[] actualPrefixBytes = ProtocolTools.getSubArray(logicalDeviceNameBytes, 0, expectedPrefix.length());
        String actualPrefix = new String(actualPrefixBytes, Charset.forName(UTF_8));
        return actualPrefix.equals(expectedPrefix);
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

    /**
     * Return the general property with the given name, for the device with the given macAddress.
     * Return null if the device does not exist, or if the property does not exist.
     */
    private BigDecimal getGeneralProperty(String macAddress, String propertyName) {
        for (OfflineDevice offlineSlaveDevice : offlineDevice.getAllSlaveDevices()) {
            String callHomeId = offlineSlaveDevice.getAllProperties().getStringProperty(DlmsProperties.CALL_HOME_ID_PROPERTY_NAME);
            if (callHomeId != null && callHomeId.equals(macAddress)) {
                return offlineSlaveDevice.getAllProperties().getTypedProperty(propertyName);
            }
        }
        return null;
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
        DLMSCache dlmsCache = (DLMSCache) getDeviceCache();
        if (dlmsCache.getObjectList() == null || getDlmsProperties().isReadCache()) {
            readObjectList();
            dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
        } else {
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(dlmsCache.getObjectList());
        }
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        //TODO no messages for now
        return "";
    }
}
