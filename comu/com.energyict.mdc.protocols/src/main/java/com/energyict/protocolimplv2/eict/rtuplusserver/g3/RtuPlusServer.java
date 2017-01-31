/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtuplusserver.g3;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.protocols.mdc.protocoltasks.TcpDeviceProtocolDialect;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.SAPAssignmentItem;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractMeterTopology;
import com.energyict.protocolimplv2.dlms.DlmsProperties;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.events.G3GatewayEvents;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.RtuPlusServerMessages;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayProperties;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.G3GatewayRegisters;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class RtuPlusServer implements DeviceProtocol {

    private static final ObisCode SERIAL_NUMBER_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");
    private static final ObisCode FRAMECOUNTER_OBISCODE = ObisCode.fromString("0.0.43.1.1.255");

    private G3GatewayProperties dynamicProperties;
    private OfflineDevice offlineDevice;
    private DlmsSession dlmsSession;
    private DsmrSecuritySupport dlmsSecuritySupport;
    private G3GatewayRegisters g3GatewayRegisters;
    private G3GatewayEvents g3GatewayEvents;
    private RtuPlusServerMessages rtuPlusServerMessages;
    private DLMSCache dlmsCache = null;
    private ComChannel comChannel = null;

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;
    private final SocketService socketService;
    private final IssueService issueService;
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;
    private AbstractMeterTopology g3Topology;
    private final Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider;

    @Inject
    public RtuPlusServer(
            Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService socketService, IssueService issueService,
            IdentificationService identificationService, CollectedDataFactory collectedDataFactory,
            MeteringService meteringService, Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.socketService = socketService;
        this.issueService = issueService;
        this.identificationService = identificationService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
        this.dsmrSecuritySupportProvider = dsmrSecuritySupportProvider;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU+Server2 G3 DLMS";
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return null;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-11-05 11:51:51 +0100 (Wed, 05 Nov 2014) $";
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        getDynamicProperties().addProperties(properties);
    }

    public DlmsSession getDlmsSession() {
        return dlmsSession;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.comChannel = comChannel;
        this.offlineDevice = offlineDevice;
        getDynamicProperties().setSerialNumber(offlineDevice.getSerialNumber());
        dlmsSession = new DlmsSession(comChannel, getDynamicProperties());
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Collections.<ConnectionType>singletonList(new OutboundTcpIpConnectionType(this.thesaurus, getPropertySpecService(), getSocketService()));
    }

    private SocketService getSocketService() {
        return this.socketService;
    }

    private PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public void logOn() {
        readFrameCounter();
        getDlmsSession().connect();
        checkCacheObjects();
    }

    /**
     * First read out the frame counter for the management client, using the public client.
     * Note that this happens without setting up an association, since the it's pre-established for the public client.
     */
    private void readFrameCounter() {
        TypedProperties clone = getDynamicProperties().getProperties().clone();
        clone.setProperty(com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(16));
        clone.setProperty(com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.SECURITY_LEVEL, "0:0");
        G3GatewayProperties publicClientProperties = new G3GatewayProperties(propertySpecService, thesaurus);
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(getDynamicProperties().getSecurityPropertySet());

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
        getDynamicProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
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
            g3GatewayRegisters = new G3GatewayRegisters(getDlmsSession(), this.issueService, collectedDataFactory);
        }
        return g3GatewayRegisters;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = collectedDataFactory.createCollectedTopology(offlineDevice.getDeviceIdentifier());

        List<SAPAssignmentItem> sapAssignmentList;      //List that contains the SAP id's and the MAC addresses of all logical devices (= gateway + slaves)
        final Array nodeList;
        try {
            sapAssignmentList = this.getDlmsSession().getCosemObjectFactory().getSAPAssignment().getSapAssignmentList();
            nodeList = this.getDlmsSession().getCosemObjectFactory().getG3NetworkManagement().getNodeList();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }

        final List<com.energyict.protocolimplv2.eict.rtu3.beacon3100.G3Topology.G3Node> g3Nodes = com.energyict.protocolimplv2.eict.rtu3.beacon3100.G3Topology.convertNodeList(nodeList, this.getDlmsSession()
                .getTimeZone());

        for (SAPAssignmentItem sapAssignmentItem : sapAssignmentList) {     //Using callHomeId as a general property
            if (!isGatewayNode(sapAssignmentItem)) {      //Don't include the gateway itself

                final com.energyict.protocolimplv2.eict.rtu3.beacon3100.G3Topology.G3Node g3Node = findG3Node(sapAssignmentItem.getLogicalDeviceName(), g3Nodes);

                if (g3Node != null) {
                    //Always include the slave information if it is present in the SAP assignment list and the G3 node list.
                    //It is the ComServer framework that will then do a smart update in EIServer, taking the readout LastSeenDate into account.

                    //getLogger().log(Level.FINEST, "hasNewerLastSeenDate returns true");
                    //getLogger().log(Level.FINEST, "g3node macAddress = "+g3Node.getMacAddressString() +" g3node lastSeenDate = "+ g3Node.getLastSeenDate().toString() +" configuredLastSeenDate = "+configuredLastSeenDate);
                    DeviceIdentifier slaveDeviceIdentifier = identificationService.createDeviceIdentifierByProperty(DlmsProperties.CALL_HOME_ID_PROPERTY_NAME, sapAssignmentItem.getLogicalDeviceName());
                    deviceTopology.addSlaveDevice(slaveDeviceIdentifier);
                    deviceTopology.addAdditionalCollectedDeviceInfo(
                            collectedDataFactory.createCollectedDeviceProtocolProperty(
                                    slaveDeviceIdentifier,
                                    getPropertySpecForName(MeterProtocol.NODEID),
                                    sapAssignmentItem.getSap()
                            )
                    );
                    deviceTopology.addAdditionalCollectedDeviceInfo(
                            collectedDataFactory.createCollectedDeviceProtocolProperty(
                                    slaveDeviceIdentifier,
                                    getPropertySpecForName(G3Properties.PROP_LASTSEENDATE),
                                    BigDecimal.valueOf(g3Node.getLastSeenDate().getTime())
                            )
                    );
                    //getLogger().log(Level.FINEST, "g3node with macAddress = "+g3Node.getMacAddressString() + " was added");
                }
            }
        }
        return deviceTopology;
    }


    private PropertySpec getPropertySpecForName(String propertySpecName) {
        Optional<PropertySpec> propertySpec = getPropertySpec(propertySpecName);
        if (propertySpec.isPresent()) {
            return propertySpec.get();
        } else {
            return null;
        }
    }

    private com.energyict.protocolimplv2.eict.rtu3.beacon3100.G3Topology.G3Node findG3Node(final String macAddress, final List<com.energyict.protocolimplv2.eict.rtu3.beacon3100.G3Topology.G3Node> g3Nodes) {
        if (macAddress != null && g3Nodes != null && !g3Nodes.isEmpty()) {
            for (final com.energyict.protocolimplv2.eict.rtu3.beacon3100.G3Topology.G3Node g3Node : g3Nodes) {
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
            g3GatewayEvents = new G3GatewayEvents(getDlmsSession(), issueService, collectedDataFactory, meteringService);
        }
        return g3GatewayEvents;
    }

    private RtuPlusServerMessages getRtuPlusServerMessages() {
        if (rtuPlusServerMessages == null) {
            rtuPlusServerMessages = new RtuPlusServerMessages(this.getDlmsSession(), issueService, collectedDataFactory, this);
        }
        return rtuPlusServerMessages;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
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
        return Collections.<DeviceProtocolDialect>singletonList(new TcpDeviceProtocolDialect(this.thesaurus, this.propertySpecService));
    }

    private DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = dsmrSecuritySupportProvider.get();
        }
        return dlmsSecuritySupport;
    }

    /**
     * Holder for all properties: security, general and dialects.
     */
    G3GatewayProperties getDynamicProperties() {
        if (dynamicProperties == null) {
            dynamicProperties = new G3GatewayProperties(propertySpecService, thesaurus);
        }
        return dynamicProperties;
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getDynamicProperties().addProperties(dialectProperties);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getDynamicProperties().addProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
        getDynamicProperties().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return this.getSecuritySupport().getCustomPropertySet();
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
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.dlmsCache = (DLMSCache) deviceProtocolCache;
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return dlmsCache;
    }

    private void checkCacheObjects() {
        if (getDeviceCache() == null) {
            setDeviceCache(new DLMSCache());
        }
        DLMSCache dlmsCache = (DLMSCache) getDeviceCache();
        if (dlmsCache.getObjectList() == null || getDynamicProperties().isReadCache()) {
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

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return getDynamicProperties().getPropertySpecs();
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        DeviceIdentifier<?> deviceIdentifier = offlineDevice.getDeviceIdentifier();
        CollectedFirmwareVersion firmwareVersionsCollectedData = collectedDataFactory.createFirmwareVersionsCollectedData(deviceIdentifier);

        try {
            firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(getG3GatewayRegisters().getFirmwareVersionString(G3GatewayRegisters.FW_APPLICATION));
        } catch (IOException e) {
            Issue issue = issueService.newIssueCollector().addWarning(deviceIdentifier, "Could not read the active meter firmware version", e.getMessage());
            firmwareVersionsCollectedData.setFailureInformation(ResultType.DataIncomplete, issue);
        }

        try {
            firmwareVersionsCollectedData.setActiveCommunicationFirmwareVersion(getG3GatewayRegisters().getFirmwareVersionString(G3GatewayRegisters.FW_UPPER_MAC));
        } catch (IOException e) {
            Issue issue = issueService.newIssueCollector().addWarning(deviceIdentifier, "Could not read the active upper mac communication firmware version", e.getMessage());
            firmwareVersionsCollectedData.setFailureInformation(ResultType.DataIncomplete, issue);
        }

        return firmwareVersionsCollectedData;
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return true;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return collectedDataFactory.createBreakerStatusCollectedData(offlineDevice.getDeviceIdentifier());
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return this.collectedDataFactory.createCalendarCollectedData(this.offlineDevice.getDeviceIdentifier());
    }

}