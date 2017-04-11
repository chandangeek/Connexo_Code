package com.energyict.protocolimplv2.eict.rtuplusserver.g3;

import com.energyict.cbo.ObservationTimestampProperties;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.SAPAssignmentItem;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceGroupExtractor;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.ProtocolLoggingSupport;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.G3Topology;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.events.G3GatewayEvents;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.RtuPlusServerMessages;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayProperties;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.G3GatewayRegisters;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 9/04/13
 * Time: 16:00
 */
public class RtuPlusServer implements DeviceProtocol, SerialNumberSupport, ProtocolLoggingSupport {

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
    private Logger logger;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final DeviceGroupExtractor deviceGroupExtractor;
    private final DeviceExtractor deviceExtractor;

    public RtuPlusServer(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor, DeviceGroupExtractor deviceGroupExtractor, DeviceExtractor deviceExtractor) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.messageFileExtractor = messageFileExtractor;
        this.deviceGroupExtractor = deviceGroupExtractor;
        this.deviceExtractor = deviceExtractor;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU+Server2 G3 DLMS";
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-12-06 13:29:40 +0100 (Tue, 06 Dec 2016)$";
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
        return Arrays.asList(new OutboundTcpIpConnectionType(this.propertySpecService), new InboundIpConnectionType());
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
        com.energyict.protocolimpl.properties.TypedProperties clone = com.energyict.protocolimpl.properties.TypedProperties.copyOf(getDlmsSessionProperties().getProperties());
        clone.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(16));
        G3GatewayProperties publicClientProperties = new G3GatewayProperties();
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(0, 0, 0, 0, 0, clone));    //SecurityLevel 0:0

        DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties);
        publicDlmsSession.assumeConnected(publicClientProperties.getMaxRecPDUSize(), publicClientProperties.getConformanceBlock());
        long frameCounter;
        try {
            frameCounter = publicDlmsSession.getCosemObjectFactory().getData(FRAMECOUNTER_OBISCODE).getValueAttr().longValue();
        } catch (DataAccessResultException | ProtocolException e) {
            frameCounter = new SecureRandom().nextInt();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
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
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    @Override
    public Date getTime() {
        try {
            return getDlmsSession().getCosemObjectFactory().getClock().getDateTime();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
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
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
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
            g3GatewayRegisters = new G3GatewayRegisters(getDlmsSession(), collectedDataFactory, issueFactory);
        }
        return g3GatewayRegisters;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = this.collectedDataFactory.createCollectedTopology(new DeviceIdentifierById(offlineDevice.getId()));

        List<SAPAssignmentItem> sapAssignmentList;      //List that contains the SAP id's and the MAC addresses of all logical devices (= gateway + slaves)
        final Array nodeList;
        try {
            sapAssignmentList = this.getDlmsSession().getCosemObjectFactory().getSAPAssignment().getSapAssignmentList();
            nodeList = this.getDlmsSession().getCosemObjectFactory().getG3NetworkManagement().getNodeList();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }

        final List<G3Topology.G3Node> g3Nodes = G3Topology.convertNodeList(nodeList, this.getDlmsSession().getTimeZone());

        for (SAPAssignmentItem sapAssignmentItem : sapAssignmentList) {     //Using callHomeId as a general property
            if (!isGatewayNode(sapAssignmentItem)) {      //Don't include the gateway itself

                final G3Topology.G3Node g3Node = findG3Node(sapAssignmentItem.getLogicalDeviceName(), g3Nodes);

                if (g3Node != null) {
                    //Always include the slave information if it is present in the SAP assignment list and the G3 node list.
                    //It is the ComServer framework that will then do a smart update in EIServer, taking the readout LastSeenDate into account.

                    DialHomeIdDeviceIdentifier slaveDeviceIdentifier = new DialHomeIdDeviceIdentifier(sapAssignmentItem.getLogicalDeviceName());
                    CollectedTopology.ObservationTimestampProperty lastSeenDateInfo = ObservationTimestampProperties.from(g3Node.getLastSeenDate(), G3Properties.PROP_LASTSEENDATE);
                    deviceTopology.addSlaveDevice(slaveDeviceIdentifier, lastSeenDateInfo);
                    deviceTopology.addAdditionalCollectedDeviceInfo(
                            this.collectedDataFactory.createCollectedDeviceProtocolProperty(
                                    slaveDeviceIdentifier,
                                    com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(),
                                    sapAssignmentItem.getSap()
                            )
                    );
                    deviceTopology.addAdditionalCollectedDeviceInfo(
                            this.collectedDataFactory.createCollectedDeviceProtocolProperty(
                                    slaveDeviceIdentifier,
                                    G3Properties.PROP_LASTSEENDATE,
                                    BigDecimal.valueOf(g3Node.getLastSeenDate().getTime())
                            )
                    );
                    //getLogger().log(Level.FINEST, "g3node with macAddress = "+g3Node.getMacAddressString() + " was added");
                }
            }
        }

        try {
            deviceTopology = cleanupDuplicatesLastSeenDate(deviceTopology);
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }

        return deviceTopology;
    }

    /**
     * Issue EISERVERSG-4655 - when a G3 Gateway is restarted, all slave devices will have the SAME lastSeenDate.
     * This method will clear the lastSeenDate in this case.
     *
     * @param deviceTopology
     */
    protected CollectedTopology cleanupDuplicatesLastSeenDate(CollectedTopology deviceTopology) {
        getLogger().finest("Cleaning up lastSeenDate with the same value, due to a gateway reset");
        Map<Long, Integer> counters = new HashMap<>();

        Iterator<DeviceIdentifier> iterator = deviceTopology.getSlaveDeviceIdentifiers().keySet().iterator();
        while (iterator.hasNext()) {
            DeviceIdentifier deviceIdentifier = iterator.next();
            Long currentValue = deviceTopology.getSlaveDeviceIdentifiers().get(deviceIdentifier).getValue().getTime();

            getLogger().finest(" - " + deviceIdentifier.toString() + " : " + getDateString(currentValue));

            if (counters.containsKey(currentValue)) {
                counters.put(currentValue, counters.get(currentValue) + 1);
            } else {
                counters.put(currentValue, 1);
            }
        }

        getLogger().finest("Checking lastSeenDate duplicates:");

        iterator = deviceTopology.getSlaveDeviceIdentifiers().keySet().iterator();
        while (iterator.hasNext()) {
            DeviceIdentifier deviceIdentifier = iterator.next();
            Long currentValue = deviceTopology.getSlaveDeviceIdentifiers().get(deviceIdentifier).getValue().getTime();

            Integer count = counters.get(currentValue);
            if (count > 1) {
                getLogger().finest(" - setting LSD from " + deviceIdentifier.toString() + ", to 01/01/2000 because the LSD appears " + count + " times. (" + getDateString(currentValue) + ")");
                //iterator.remove(); // -> this will remove this device from gateway, we don't want this
                // instead put an old date, to keep it attached to current gateway, or move it to a different gateway with a newer LSD
                CollectedTopology.ObservationTimestampProperty oldLastSeenDate = ObservationTimestampProperties.from(new Date(100, 0, 1), "LastSeenDate"); // 2000 Jan 01
                deviceTopology.getSlaveDeviceIdentifiers().put(deviceIdentifier, oldLastSeenDate);
            }
        }
        getLogger().finest("-done checking duplicates.");
        return deviceTopology;
    }

    private String getDateString(Long time) {
        if (time == null) {
            return "null";
        }

        Date date = new Date(time);
        return date.toString();
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
        for (OfflineDevice slaveDevice : offlineDevice.getAllSlaveDevices()) {
            String configuredCallHomeId = slaveDevice.getAllProperties().getTypedProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
            configuredCallHomeId = configuredCallHomeId == null ? "" : configuredCallHomeId;
            if (macAddress.equals(configuredCallHomeId)) {
                return slaveDevice.getAllProperties().getTypedProperty(G3Properties.PROP_LASTSEENDATE, BigDecimal.ZERO).longValue();
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

    private boolean isGatewayNode(SAPAssignmentItem sapAssignmentItem) {
        return sapAssignmentItem.getSap() == 1;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getG3GatewayEvents().readEvents(logBooks);
    }

    private G3GatewayEvents getG3GatewayEvents() {
        if (g3GatewayEvents == null) {
            g3GatewayEvents = new G3GatewayEvents(getDlmsSession(), collectedDataFactory, issueFactory);
        }
        return g3GatewayEvents;
    }

    protected RtuPlusServerMessages getRtuPlusServerMessages() {
        if (rtuPlusServerMessages == null) {
            rtuPlusServerMessages = new RtuPlusServerMessages(this.getDlmsSession(), offlineDevice, collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, messageFileExtractor, deviceGroupExtractor, deviceExtractor);
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
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getRtuPlusServerMessages().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList(new TcpDeviceProtocolDialect(this.propertySpecService));
    }

    protected DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = new DsmrSecuritySupport(this.propertySpecService);
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
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.getDlmsSessionProperties().addProperties(properties);
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
    public List<PropertySpec> getUPLPropertySpecs() {
        return getConfigurationSupport().getPropertySpecs();
    }

    protected G3GatewayConfigurationSupport getConfigurationSupport() {
        if (configurationSupport == null) {
            configurationSupport = new G3GatewayConfigurationSupport(this.propertySpecService);
        }
        return configurationSupport;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return getSecuritySupport().getSecurityProperties();
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
    public Optional<PropertySpec> getSecurityPropertySpec(String name) {
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
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration
            (List<LoadProfileReader> loadProfilesToRead) {
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
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
        return logger;
    }

    @Override
    public void setProtocolLogger(Logger protocolLogger) {
        this.logger = protocolLogger;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifierById(offlineDevice.getId());
        CollectedFirmwareVersion result = this.collectedDataFactory.createFirmwareVersionsCollectedData(deviceIdentifier);

        try {
            result.setActiveMeterFirmwareVersion(getG3GatewayRegisters().getFirmwareVersionString(G3GatewayRegisters.FW_APPLICATION));
        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSessionProperties().getRetries())) {
                Issue issue = this.issueFactory.createWarning(deviceIdentifier, "Could not read the active meter firmware version: " + e.getMessage());
                result.setFailureInformation(ResultType.DataIncomplete, issue);
            } //Else throws communication exception
        }

        try {
            result.setActiveCommunicationFirmwareVersion(getG3GatewayRegisters().getFirmwareVersionString(G3GatewayRegisters.FW_UPPER_MAC));
        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSessionProperties().getRetries())) {
                Issue issue = this.issueFactory.createWarning(deviceIdentifier, "Could not read the active upper mac communication firmware version: " + e.getMessage());
                result.setFailureInformation(ResultType.DataIncomplete, issue);
            } //Else throws communication exception
        }

        return result;
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return true;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return this.collectedDataFactory.createBreakerStatusCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return this.collectedDataFactory.createCalendarCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }
}