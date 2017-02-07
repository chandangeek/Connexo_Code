package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cbo.LastSeenDateInfo;
import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.cosem.SAPAssignmentItem;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.ip.socket.TLSConnectionType;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocolCache;
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
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
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
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;
import com.energyict.protocolimplv2.security.DlmsSecuritySuite1And2Support;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 18/06/2015 - 15:07
 */
public class Beacon3100 extends AbstractDlmsProtocol implements MigratePropertiesFromPreviousSecuritySet, AdvancedDeviceProtocolSecurityCapabilities {

    // https://confluence.eict.vpdc/display/G3IntBeacon3100/DLMS+management
    // https://jira.eict.vpdc/browse/COMMUNICATION-1552
    private static final ObisCode SERIAL_NUMBER_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");
    private static final String MIRROR_LOGICAL_DEVICE_PREFIX = "ELS-MIR-";
    private static final String GATEWAY_LOGICAL_DEVICE_PREFIX = "ELS-UGW-";
    private static final String UTF_8 = "UTF-8";
    private static final int MAC_ADDRESS_LENGTH = 8;    //In bytes
    protected Beacon3100Messaging beacon3100Messaging;
    private BeaconCache beaconCache = null;
    private RegisterFactory registerFactory;
    private Beacon3100LogBookFactory logBookFactory;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        getLogger().info("Start protocol for " + offlineDevice.getSerialNumber());
        getLogger().info("-version: " + getVersion());
        handleFrameCounter(comChannel);
        initDlmsSession(comChannel);
    }

    /**
     * Can be overridden by the crypto-protocols
     */
    protected void initDlmsSession(ComChannel comChannel) {
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));
    }

    /**
     * Either get the FC from the device cache, or read it out using the public client. This can be configured with general properties.
     * Unless of course the whole session is done with the public client, then there's no need to read out the FC.
     */
    protected void handleFrameCounter(ComChannel comChannel) {
        int clientMacAddress = getDlmsSessionProperties().getClientMacAddress();
        if (clientMacAddress != ClientConfiguration.PUBLIC.clientId) {

            boolean weHaveValidCachedFrameCounter = false;
            if (getDlmsSessionProperties().useCachedFrameCounter()) {
                weHaveValidCachedFrameCounter = getCachedFrameCounter(comChannel, clientMacAddress);
            }

            if (!weHaveValidCachedFrameCounter) {
                //No cached FC available. Read it out using the public client.
                readFrameCounter(comChannel);
            }
        }
    }

    @Override
    public BeaconCache getDeviceCache() {
        if (beaconCache == null) {
            beaconCache = new BeaconCache();
        }
        return beaconCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if ((deviceProtocolCache != null) && (deviceProtocolCache instanceof BeaconCache)) {
            beaconCache = (BeaconCache) deviceProtocolCache;
        }
    }

    @Override
    public void terminate() {
        //As a last step, update the cache with the last FC
        if (getDlmsSession() != null && getDlmsSession().getAso() != null && getDlmsSession().getAso().getSecurityContext() != null) {
            getDeviceCache().setTXFrameCounter(getDlmsSessionProperties().getClientMacAddress(), getDlmsSession().getAso().getSecurityContext().getFrameCounter());
        }
    }

    /**
     * Get the frame counter from the cache, for the given clientId.
     * If no frame counter is available in the cache (value -1), use the configured InitialFC property.
     * <p/>
     * Additionally, the FC value can be validated with ValidateCachedFrameCounterAndFallback
     */
    private boolean getCachedFrameCounter(ComChannel comChannel, int clientId) {
        getLogger().info("Will try to use a cached frame counter");
        boolean weHaveAFrameCounter = false;
        long cachedFrameCounter = getDeviceCache().getTXFrameCounter(clientId);
        long initialFrameCounter = getDlmsSessionProperties().getInitialFrameCounter();

        if (initialFrameCounter > cachedFrameCounter) { //Note that this is also the case when the cachedFrameCounter is unavailable (value -1).
            getLogger().info("Using initial frame counter: " + initialFrameCounter + " because it has a higher value than the cached frame counter: " + cachedFrameCounter);
            setTXFrameCounter(initialFrameCounter);
            weHaveAFrameCounter = true;
        } else if (cachedFrameCounter > 0) {
            getLogger().info("Using cached frame counter: " + cachedFrameCounter);
            setTXFrameCounter(cachedFrameCounter + 1);
            weHaveAFrameCounter = true;
        }

        if (weHaveAFrameCounter) {
            if (getDlmsSessionProperties().validateCachedFrameCounter()) {
                return testConnectionAndRetryWithFrameCounterIncrements(comChannel);
            } else {
                getLogger().warning(" - cached frame counter will not be validated - if the communication fails please set the cache property back to {No}, so a fresh one will be read-out");
                // do not validate, just use it and hope for the best
                return true;
            }
        }

        return false;
    }

    private boolean testConnectionAndRetryWithFrameCounterIncrements(ComChannel comChannel) {
        DlmsSession testDlmsSession = getDlmsSessionForFCTesting(comChannel);
        int retries = getDlmsSessionProperties().getFrameCounterRecoveryRetries();
        int step = getDlmsSessionProperties().getFrameCounterRecoveryStep();
        boolean releaseOnce = true;

        getLogger().info("Will test the frameCounter. Recovery mechanism: retries=" + retries + ", step=" + step);
        if (retries <= 0) {
            retries = 0;
            step = 0;
        }

        do {
            try {
                testDlmsSession.getDlmsV2Connection().connectMAC();
                testDlmsSession.createAssociation();
                if (testDlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {
                    testDlmsSession.disconnect();
                    getLogger().info("Cached FrameCounter is valid!");
                    setTXFrameCounter(testDlmsSession.getAso().getSecurityContext().getFrameCounter());
                    return true;
                }
            } catch (CommunicationException ex) {
                long frameCounter = testDlmsSession.getAso().getSecurityContext().getFrameCounter();
                getLogger().warning("Current frame counter [" + frameCounter + "] is not valid, received exception " + ex.getMessage() + ", increasing frame counter by " + step);
                frameCounter += step;
                setTXFrameCounter(frameCounter);
                testDlmsSession.getAso().getSecurityContext().setFrameCounter(frameCounter);

                if (releaseOnce) {
                    releaseOnce = false;
                    //Try to release that association once, it may be that it was still open from a previous session, causing troubles to create the new association.
                    try {
                        testDlmsSession.getAso().releaseAssociation();
                    } catch (ProtocolRuntimeException e) {
                        testDlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                        // Absorb exception: in 99% of the cases we expect an exception here ...
                    }
                }
            }
            retries--;
        } while (retries > 0);

        testDlmsSession.disconnect();
        getLogger().warning("Could not validate the frame counter, seems that it's out-of synch whith the device. You'll have to read a fresh one.");
        return false;
    }

    /**
     * Sub classes (for example the crypto-protocol) can override
     */
    protected DlmsSession getDlmsSessionForFCTesting(ComChannel comChannel) {
        return new DlmsSession(comChannel, getDlmsSessionProperties());
    }

    private void setTXFrameCounter(long frameCounter) {
        this.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    /**
     * Will return the correct frame counter obis code, for each client ID.
     *
     * @param clientId - DLMS Client ID used in association
     * @return - the correct obis code for this client
     */
    private ObisCode getFrameCounterObisCode(final int clientId) {
        final ClientConfiguration client = ClientConfiguration.getByID(clientId);

        if (client == null) {
            throw new IllegalArgumentException("No client with ID [" + clientId + "] defined for this device !");
        }

        return client.getFrameCounterOBIS();
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
     * <p/>
     * For EVN we'll read the frame counter using the frame counter provider custom method in the beacon
     */
    protected void readFrameCounter(ComChannel comChannel) {
        if (this.usesSessionKey()) {
            //No need to read out the global FC if we're going to use a new session key in this AA.
            return;
        }

        final Beacon3100Properties publicClientProperties = new Beacon3100Properties();

        publicClientProperties.addProperties(getDlmsSessionProperties().getProperties().clone());
        publicClientProperties.getProperties().setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS,
                BigDecimal.valueOf(ClientConfiguration.PUBLIC.clientId));

        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(0, 0, 0, 0, 0, getDlmsSessionProperties().getProperties()));    //SecurityLevel 0:0

        final DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties, getDlmsSessionProperties().getSerialNumber());

        final long frameCounter;

        final com.energyict.dlms.protocolimplv2.DlmsSessionProperties sessionProperties = this.getDlmsSessionProperties();

        if (this.getDlmsSessionProperties().isPublicClientPreEstablished() && this.getDlmsSessionProperties().getRequestAuthenticatedFrameCounter()) {
            if (this.getLogger().isLoggable(Level.WARNING)) {
                this.getLogger().log(Level.WARNING, "Invalid configuration detected : cannot use a pre-established public client association in combination with and authenticated frame counter, overriding to non-pre-established.");
            }
        }
        final boolean preEstablished = sessionProperties.isPublicClientPreEstablished() && !this.getDlmsSessionProperties().getRequestAuthenticatedFrameCounter();

        try {
            // Associate if necessary.
            if (preEstablished) {
                if (this.getLogger().isLoggable(Level.FINE)) {
                    this.getLogger().log(Level.FINE, "Public client association is pre-established.");
                }

                publicDlmsSession.assumeConnected(publicClientProperties.getMaxRecPDUSize(), publicClientProperties.getConformanceBlock());
            } else {
                if (this.getLogger().isLoggable(Level.FINE)) {
                    this.getLogger().log(Level.FINE, "Public client association is not pre-established.");
                }

                publicDlmsSession.getDlmsV2Connection().connectMAC();
                publicDlmsSession.createAssociation();
            }

            // Then read out the frame counter.
            final ObisCode frameCounterObisCode = this.getFrameCounterObisCode(getDlmsSessionProperties().getClientMacAddress());

            if (getDlmsSessionProperties().getRequestAuthenticatedFrameCounter()) {
                getLogger().finest("Requesting authenticated frame counter");
                try {
                    FrameCounterProvider frameCounterProvider = publicDlmsSession.getCosemObjectFactory().getFrameCounterProvider(frameCounterObisCode);
                    frameCounter = frameCounterProvider.getFrameCounter(publicDlmsSession.getProperties().getSecurityProvider().getAuthenticationKey());
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, e.getCause() + e.getMessage(), e);
                    throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
                } catch (Exception e) {
                    getLogger().log(Level.SEVERE, e.getCause() + e.getMessage(), e);
                    final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the frame counter, cannot continue! " + e.getMessage());
                    throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
                }
            } else {
                try {
                    frameCounter = publicDlmsSession.getCosemObjectFactory().getData(frameCounterObisCode).getValueAttr().longValue();
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, e.getCause() + e.getMessage(), e);
                    throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
                }
            }
        } finally {
            // Only disconnect if the association is not pre-established.
            if (!preEstablished) {
                publicDlmsSession.disconnect();
            }
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

    protected Beacon3100Messaging getBeacon3100Messaging() {
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
            nodeList = getG3NetworkManagement().getNodeList();
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

    private G3NetworkManagement getG3NetworkManagement() throws NotInObjectListException {
        if(getDlmsSessionProperties().getReadOldObisCodes()) {
            return this.getDlmsSession().getCosemObjectFactory().getG3NetworkManagement();
        }else{
            return this.getDlmsSession().getCosemObjectFactory().getG3NetworkManagement(Beacon3100Messaging.G3_NETWORK_MANAGEMENT_NEW_OBISCODE);
        }
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
        return "$Date: 2016-11-15 11:14:48 +0100 (Tue, 15 Nov 2016)$";
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

    /**
     * Enumerates the different clients for the Beacon.
     *
     * @author alex
     */
    public enum ClientConfiguration {

        /**
         * Management client.
         */
        MANAGEMENT(1, ObisCode.fromString("0.0.43.1.1.255"), ObisCode.fromString("0.0.43.0.2.255"), ObisCode.fromString("0.0.40.0.2.255")),

        /**
         * Public client.
         */
        PUBLIC(16, null, ObisCode.fromString("0.0.40.0.1.255"), ObisCode.fromString("0.0.40.0.1.255")),

        /**
         * Read-write client.
         */
        READ_WRITE(32, ObisCode.fromString("0.0.43.1.2.255"), ObisCode.fromString("0.0.43.0.3.255"), ObisCode.fromString("0.0.40.0.3.255")),

        /**
         * Firmware upgrade client.
         */
        FIRMWARE(64, ObisCode.fromString("0.0.43.1.3.255"), ObisCode.fromString("0.0.43.0.4.255"), ObisCode.fromString("0.0.40.0.4.255")),

        /**
         * Read only client.
         */
        READ_ONLY(127, ObisCode.fromString("0.0.43.1.4.255"), ObisCode.fromString("0.0.43.0.5.255"), ObisCode.fromString("0.0.40.0.5.255"));

        /**
         * Client ID to be used.
         */
        private final int clientId;

        /**
         * OBIS code of the frame counter.
         */
        private final ObisCode frameCounterOBIS;

        /**
         * OBIS code of the Security Setup
         */
        private final ObisCode securitySetupOBIS;

        /**
         * OBIS code of the Association LN setup
         */
        private final ObisCode associationLnOBIS;

        /**
         * Create a new instance.
         *
         * @param id               Client ID.
         * @param frameCounterOBIS Frame counter OBIS code.
         */
        ClientConfiguration(final int id, final ObisCode frameCounterOBIS, final ObisCode securitySetupOBIS, final ObisCode associationLNOBIS) {
            this.clientId = id;
            this.frameCounterOBIS = frameCounterOBIS;
            this.securitySetupOBIS = securitySetupOBIS;
            this.associationLnOBIS = associationLNOBIS;
        }

        /**
         * Returns the client with the given ID.
         *
         * @param clientId ID of the requested client.
         * @return The matching client, <code>null</code> if not known.
         */
        public static ClientConfiguration getByID(final int clientId) {
            for (final ClientConfiguration client : ClientConfiguration.values()) {
                if (client.clientId == clientId) {
                    return client;
                }
            }

            return null;
        }

        /**
         * Returns the OBIS of the FC.
         *
         * @return The OBIS of the FC.
         */
        public final ObisCode getFrameCounterOBIS() {
            return frameCounterOBIS;
        }

        /**
         * Returns the OBIS of the SecuritySetup.
         *
         * @return The OBIS of the SecuritySetup.
         */
        public final ObisCode getSecuritySetupOBIS() {
            return securitySetupOBIS;
        }

        /**
         * Return the Association LN ObisCode
         */
        public ObisCode getAssociationLN() {
            return associationLnOBIS;
        }
    }
}