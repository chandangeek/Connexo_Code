package com.energyict.protocolimplv2.eict.rtuplusserver.idis;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.dlms.cosem.SAPAssignmentItem;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.data.*;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.LoadProfileReader;

import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.events.IDISGatewayEvents;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.messages.IDISGatewayMessages;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.properties.IDISGatewayDynamicPropertySupportSupport;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.IDISGatewayRegisters;
import com.energyict.protocolimplv2.elster.garnet.TcpDeviceProtocolDialect;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;
import com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.protocols.mdc.services.impl.Bus;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

/**
 * @author sva
 * @since 15/10/2014 - 10:55
 */
public class RtuPlusServer implements DeviceProtocol {

    private ComChannel comChannel;
    private DlmsSession dlmsSession;
    private OfflineDevice offlineDevice;
    private DsmrSecuritySupport dlmsSecuritySupport;
    private IDISGatewayEvents idisGatewayEvents;
    private IDISGatewayMessages idisGatewayMessages;
    private IDISGatewayRegisters idisGatewayRegisters;
    private IDISGatewayDynamicPropertySupportSupport dynamicPropertySupport;

    private PropertySpecService propertySpecService;
    private SocketService socketService;

    public RtuPlusServer() {
    }

    @Inject
    public RtuPlusServer(PropertySpecService propertySpecService, SocketService socketService) {
        this.propertySpecService = propertySpecService;
        this.socketService = socketService;
    }

    @Override
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
        getDlmsSecuritySupport().setPropertySpecService(propertySpecService);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.comChannel = comChannel;
        this.offlineDevice = offlineDevice;
        this.dlmsSession = new DlmsSession(comChannel, getDynamicPropertySupport());
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
        getDlmsSession().disconnect();
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
    public String getSerialNumber() {
        try {
            return getDlmsSession().getCosemObjectFactory().getData(IDISGatewayRegisters.SERIAL_NUMBER_OBIS).getString();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }

    @Override
    public Date getTime() {
        try {
            return getDlmsSession().getCosemObjectFactory().getClock(IDISGatewayRegisters.CLOCK_OBIS_CODE).getDateTime();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getDlmsSession().getCosemObjectFactory().getClock(IDISGatewayRegisters.CLOCK_OBIS_CODE).setDateTime(timeToSet);
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
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
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        // Not used in this protocol
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
    public Set<DeviceMessageId> getSupportedMessages() {
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
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getIDISGatewayMessages().format(propertySpec, messageAttribute);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = com.energyict.mdc.protocol.api.CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierById(getOfflineDevice().getId()));

        List<SAPAssignmentItem> sapAssignmentList;      //List that contains the SAP id's and the MAC addresses of all logical devices (= gateway + slaves)
        try {
            sapAssignmentList = this.getDlmsSession().getCosemObjectFactory().getSAPAssignment().getSapAssignmentList();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
        for (SAPAssignmentItem sapAssignmentItem : sapAssignmentList) {     //Using callHomeId as a general property
            if (!isGatewayNode(sapAssignmentItem)) {
                DialHomeIdDeviceIdentifier slaveDeviceIdentifier = new DialHomeIdDeviceIdentifier(sapAssignmentItem.getLogicalDeviceName().trim().toUpperCase());
                deviceTopology.addSlaveDevice(slaveDeviceIdentifier);
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        com.energyict.mdc.protocol.api.CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier,
                                getDynamicPropertySupport().callingAPTitlePropertySpec(),
                                getDynamicPropertySupport().getDeviceId()    // The DeviceID of the gateway
                        )
                );
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        com.energyict.mdc.protocol.api.CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier,
                                getDynamicPropertySupport().nodeAddressPropertySpec(),
                                Integer.toString(sapAssignmentItem.getSap())
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
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.<ConnectionType>asList(new OutboundTcpIpConnectionType(getPropertySpecService(), getSocketService()));
    }

    private SocketService getSocketService() {
        if(this.socketService == null){
            return Bus.getSocketService();
        }
        return this.socketService;
    }

    private PropertySpecService getPropertySpecService() {
        if(this.propertySpecService == null){
            return Bus.getPropertySpecService();
        }
        return this.propertySpecService;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new TcpDeviceProtocolDialect(propertySpecService));
    }

        @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getDynamicPropertySupport().addProperties(dialectProperties);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getDynamicPropertySupport().addProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
        getDynamicPropertySupport().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return getDlmsSecuritySupport().getSecurityProperties();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return getDlmsSecuritySupport().getSecurityRelationTypeName();
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
    public PropertySpec getSecurityPropertySpec(String name) {
        return getDlmsSecuritySupport().getSecurityPropertySpec(name);
    }

    public DlmsSession getDlmsSession() {
        return dlmsSession;
    }

    public IDISGatewayRegisters getIDISGatewayRegisters() {
        if (this.idisGatewayRegisters == null) {
            this.idisGatewayRegisters = new IDISGatewayRegisters(getDlmsSession());
        }
        return this.idisGatewayRegisters;
    }

    public IDISGatewayEvents getIDISGatewayEvents() {
        if (this.idisGatewayEvents == null) {
            this.idisGatewayEvents = new IDISGatewayEvents(getDlmsSession());
        }
        return this.idisGatewayEvents;
    }

    public IDISGatewayMessages getIDISGatewayMessages() {
        if (this.idisGatewayMessages == null) {
            this.idisGatewayMessages = new IDISGatewayMessages(getDlmsSession());
        }
        return this.idisGatewayMessages;
    }

    public DsmrSecuritySupport getDlmsSecuritySupport() {
        if (this.dlmsSecuritySupport == null) {
            this.dlmsSecuritySupport = new DsmrSecuritySupport();
        }
        return this.dlmsSecuritySupport;
    }

    public IDISGatewayDynamicPropertySupportSupport getDynamicPropertySupport() {
        if (this.dynamicPropertySupport == null) {
            this.dynamicPropertySupport = new IDISGatewayDynamicPropertySupportSupport(propertySpecService);
        }
        return this.dynamicPropertySupport;
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
        getDynamicPropertySupport().addProperties(properties);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return getDynamicPropertySupport().getPropertySpecs();
    }

    @Override
    public PropertySpec getPropertySpec(String s) {
        return getPropertySpecs().stream().filter(propertySpec -> propertySpec.getName().equals(s)).findFirst().orElse(null);
    }
}