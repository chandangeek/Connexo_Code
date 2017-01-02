package com.energyict.protocolimplv2.eict.rtuplusserver.idis;

import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.io.ConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
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
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.cosem.SAPAssignmentItem;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.events.IDISGatewayEvents;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.messages.IDISGatewayMessages;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.properties.IDISGatewayConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.properties.IDISGatewayProperties;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.IDISGatewayRegisters;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author sva
 * @since 15/10/2014 - 10:55
 */
public class RtuPlusServer implements DeviceProtocol, SerialNumberSupport {

    private ComChannel comChannel;
    private DlmsSession dlmsSession;
    private OfflineDevice offlineDevice;
    private DsmrSecuritySupport dlmsSecuritySupport;
    private IDISGatewayEvents idisGatewayEvents;
    private IDISGatewayMessages idisGatewayMessages;
    private IDISGatewayRegisters idisGatewayRegisters;
    private IDISGatewayProperties dlmsSessionProperties;
    private IDISGatewayConfigurationSupport configurationSupport;
    private IDISConfigurationSupport meterConfigurationSupport;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;

    public RtuPlusServer(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.comChannel = comChannel;
        this.offlineDevice = offlineDevice;
        this.dlmsSession = new DlmsSession(comChannel, getDlmsSessionProperties());
    }

    @Override
    public void terminate() {
        // Nothing to do
    }

    @Override
    public void logOn() {
        getDlmsSession().connect();
    }

    @Override
    public void daisyChainedLogOn() {
        logOn();
    }

    @Override
    public void logOff() {
        if (getDlmsSession() != null) {
            getDlmsSession().disconnect();
        }
    }

    @Override
    public void daisyChainedLogOff() {
        logOff();
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        // Not used in this protocol
    }

    @Override
    public String getSerialNumber() {
        try {
            return getDlmsSession().getCosemObjectFactory().getData(IDISGatewayRegisters.SERIAL_NUMBER_OBIS).getString();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries()+ 1);
        }
    }

    @Override
    public Date getTime() {
        try {
            return getDlmsSession().getCosemObjectFactory().getClock(IDISGatewayRegisters.CLOCK_OBIS_CODE).getDateTime();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries()+ 1);
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getDlmsSession().getCosemObjectFactory().getClock(IDISGatewayRegisters.CLOCK_OBIS_CODE).setDateTime(timeToSet);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries()+ 1);
        }
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
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> result = new ArrayList<>();
        for (OfflineRegister register : registers) {
            result.add(getIDISGatewayRegisters().readRegister(register));
        }
        return result;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getIDISGatewayEvents().readEvents(logBooks);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getIDISGatewayMessages().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getIDISGatewayMessages().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getIDISGatewayMessages().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getIDISGatewayMessages().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return "";
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = this.collectedDataFactory.createCollectedTopology(new DeviceIdentifierById(getOfflineDevice().getId()));

        List<SAPAssignmentItem> sapAssignmentList;      //List that contains the SAP id's and the MAC addresses of all logical devices (= gateway + slaves)
        try {
            sapAssignmentList = this.getDlmsSession().getCosemObjectFactory().getSAPAssignment().getSapAssignmentList();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries()+ 1);
        }
        for (SAPAssignmentItem sapAssignmentItem : sapAssignmentList) {     //Using callHomeId as a general property
            if (!isGatewayNode(sapAssignmentItem)) {
                DialHomeIdDeviceIdentifier slaveDeviceIdentifier = new DialHomeIdDeviceIdentifier(sapAssignmentItem.getLogicalDeviceName().trim().toUpperCase());
                deviceTopology.addSlaveDevice(slaveDeviceIdentifier);
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        this.collectedDataFactory.createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier,
                                getMeterConfigurationSupport().callingAPTitlePropertySpec().getName(),
                                getDlmsSessionProperties().getDeviceId()    // The DeviceID of the gateway
                        )
                );
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        this.collectedDataFactory.createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier,
                                DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS,
                                sapAssignmentItem.getSap()
                        )
                );
            }
        }
        return deviceTopology;
    }

    /**
     * Properties of the IDIS e-meter.
     * Some of them are used in this protocol, their values are updated when executing the topology task.
     */
    private IDISConfigurationSupport getMeterConfigurationSupport() {
        if (meterConfigurationSupport == null) {
            meterConfigurationSupport = new IDISConfigurationSupport();
        }
        return meterConfigurationSupport;
    }

    private boolean isGatewayNode(SAPAssignmentItem sapAssignmentItem) {
        return sapAssignmentItem.getSap() == 1;
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.getConfigurationSupport().getPropertySpecs();
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Collections.singletonList(new OutboundTcpIpConnectionType());
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList(new TcpDeviceProtocolDialect(this.propertySpecService));
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
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
    public List<PropertySpec> getSecurityProperties() {
        return getDlmsSecuritySupport().getSecurityProperties();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return getDlmsSecuritySupport().getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return getDlmsSecuritySupport().getEncryptionAccessLevels();
    }

    @Override
    public Optional<PropertySpec> getSecurityPropertySpec(String name) {
        return getDlmsSecuritySupport().getSecurityPropertySpec(name);
    }

    public DlmsSession getDlmsSession() {
        return dlmsSession;
    }

    public IDISGatewayRegisters getIDISGatewayRegisters() {
        if (this.idisGatewayRegisters == null) {
            this.idisGatewayRegisters = new IDISGatewayRegisters(getDlmsSession(), collectedDataFactory, issueFactory);
        }
        return this.idisGatewayRegisters;
    }

    public IDISGatewayEvents getIDISGatewayEvents() {
        if (this.idisGatewayEvents == null) {
            this.idisGatewayEvents = new IDISGatewayEvents(getDlmsSession(), collectedDataFactory, issueFactory);
        }
        return this.idisGatewayEvents;
    }

    public IDISGatewayMessages getIDISGatewayMessages() {
        if (this.idisGatewayMessages == null) {
            this.idisGatewayMessages = new IDISGatewayMessages(getDlmsSession(), collectedDataFactory, issueFactory, propertySpecService, nlsService, converter);
        }
        return this.idisGatewayMessages;
    }

    private IDISGatewayProperties getDlmsSessionProperties() {
        if (this.dlmsSessionProperties == null) {
            this.dlmsSessionProperties = new IDISGatewayProperties();
        }
        return this.dlmsSessionProperties;
    }

    public DsmrSecuritySupport getDlmsSecuritySupport() {
        if (this.dlmsSecuritySupport == null) {
            this.dlmsSecuritySupport = new DsmrSecuritySupport(this.propertySpecService);
        }
        return this.dlmsSecuritySupport;
    }

    public IDISGatewayConfigurationSupport getConfigurationSupport() {
        if (this.configurationSupport == null) {
            this.configurationSupport = new IDISGatewayConfigurationSupport();
        }
        return this.configurationSupport;
    }

    public ComChannel getComChannel() {
        return comChannel;
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU+Server2 IDIS DLMS";
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-12-06 13:29:40 +0100 (Tue, 06 Dec 2016)$";
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return this.collectedDataFactory.createFirmwareVersionsCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
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