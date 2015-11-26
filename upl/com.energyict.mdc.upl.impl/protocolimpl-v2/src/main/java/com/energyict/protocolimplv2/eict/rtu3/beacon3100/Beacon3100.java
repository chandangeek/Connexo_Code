package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.SAPAssignmentItem;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
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
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.Beacon3100Messaging;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.registers.RegisterFactory;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.events.G3GatewayEvents;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 18/06/2015 - 15:07
 */
public class Beacon3100 extends AbstractDlmsProtocol {

    private static final ObisCode SERIAL_NUMBER_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");
    private static final ObisCode FRAMECOUNTER_OBISCODE = ObisCode.fromString("0.0.43.1.1.255");
    private static final String MIRROR_LOGICAL_DEVICE_PREFIX = "ELS-MIR-";
    private static final String GATEWAY_LOGICAL_DEVICE_PREFIX = "ELS-UGW-";
    private static final String UTF_8 = "UTF-8";
    private static final int MAC_ADDRESS_LENGTH = 8;    //In bytes

    private Beacon3100Messaging beacon3100Messaging;
    private G3GatewayEvents g3GatewayEvents;
    private RegisterFactory registerFactory;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        readFrameCounter(comChannel);
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));
    }

    /**
     * First read out the frame counter for the management client, using the public client. It has a pre-established association.
     * Note that this happens without setting up an association, since the it's pre-established for the public client.
     */
    protected void readFrameCounter(ComChannel comChannel) {
        TypedProperties clone = getDlmsSessionProperties().getProperties().clone();
        clone.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(16));
        Beacon3100Properties publicClientProperties = new Beacon3100Properties();
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

        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
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
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.<ConnectionType>asList(new OutboundTcpIpConnectionType(), new InboundIpConnectionType());
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT Beacon3100 G3 DLMS";
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return Collections.emptyList(); //Not supported
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return Collections.emptyList(); //Not supported
    }

    /**
     * Note that the logbook (and it's possible entries) are exactly the same as for the G3 gateway.
     */
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
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
        final List<G3Topology.G3Node> g3Nodes = G3Topology.convertNodeList(nodeList, this.getDlmsSession().getTimeZone());

        for (SAPAssignmentItem sapAssignmentItem : sapAssignmentList) {

            byte[] logicalDeviceNameBytes = sapAssignmentItem.getLogicalDeviceNameBytes();
            if (hasLogicalDevicePrefix(logicalDeviceNameBytes, GATEWAY_LOGICAL_DEVICE_PREFIX)) {
                byte[] logicalNameMacBytes = ProtocolTools.getSubArray(logicalDeviceNameBytes, GATEWAY_LOGICAL_DEVICE_PREFIX.length(), GATEWAY_LOGICAL_DEVICE_PREFIX.length() + MAC_ADDRESS_LENGTH);
                final String macAddress = ProtocolTools.getHexStringFromBytes(logicalNameMacBytes, "");

                final G3Topology.G3Node g3Node = findG3Node(macAddress, g3Nodes);
                if (g3Node != null) {

                    long configuredLastSeenDate = getConfiguredLastSeenDate(macAddress);

                    //G3 node that was read out has a newer link to a DC, update allowed.
                    //Else: this device is no longer considered a slave device of the gateway.
                    if (hasNewerLastSeenDate(g3Node, configuredLastSeenDate)) {
                        DialHomeIdDeviceIdentifier slaveDeviceIdentifier = new DialHomeIdDeviceIdentifier(macAddress);
                        deviceTopology.addSlaveDevice(slaveDeviceIdentifier);   //Using callHomeId as a general property
                        deviceTopology.addAdditionalCollectedDeviceInfo(
                                MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                        slaveDeviceIdentifier,
                                        AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID,
                                        BigDecimal.valueOf(sapAssignmentItem.getSap())
                                )
                        );
                        deviceTopology.addAdditionalCollectedDeviceInfo(
                                MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                        slaveDeviceIdentifier,
                                        AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID,
                                        BigDecimal.valueOf(findMatchingMirrorLogicalDevice(macAddress, sapAssignmentList))
                                )
                        );
                        deviceTopology.addAdditionalCollectedDeviceInfo(
                                MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                        slaveDeviceIdentifier,
                                        G3Properties.PROP_LASTSEENDATE,
                                        BigDecimal.valueOf(g3Node.getLastSeenDate().getTime())
                                )
                        );
                    }
                }
            }
        }
        return deviceTopology;
    }

    /**
     * This node is only considered an actual slave device if:
     * - the configuredLastSeenDate in EIServer is still empty
     * - the read out last seen date is empty (==> always update EIServer, by design)
     * - the read out last seen date is the same, or newer, compared to the configuredLastSeenDate in EIServer
     * <p/>
     * If true, the gateway link in EIServer will be created and the properties will be set.
     * If false, the gateway link (if it exists at all) will be removed.
     */
    private boolean hasNewerLastSeenDate(G3Topology.G3Node g3Node, long configuredLastSeenDate) {
        return (configuredLastSeenDate == 0) || (g3Node.getLastSeenDate() == null) || (g3Node.getLastSeenDate().getTime() >= configuredLastSeenDate);
    }

    /**
     * Return property "LastSeenDate" on slave device with callHomeId == macAddress
     * Return 0 if not found.
     */
    private long getConfiguredLastSeenDate(String macAddress) {
        for (OfflineDevice slaveDevice : getOfflineDevice().getAllSlaveDevices()) {
            String configuredCallHomeId = slaveDevice.getAllProperties().getStringProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
            configuredCallHomeId = configuredCallHomeId == null ? "" : configuredCallHomeId;
            if (macAddress.equals(configuredCallHomeId)) {
                return slaveDevice.getAllProperties().getIntegerProperty(G3Properties.PROP_LASTSEENDATE, BigDecimal.ZERO).longValue();
            }
        }
        return 0L;
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
        return "$Date$";
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
}