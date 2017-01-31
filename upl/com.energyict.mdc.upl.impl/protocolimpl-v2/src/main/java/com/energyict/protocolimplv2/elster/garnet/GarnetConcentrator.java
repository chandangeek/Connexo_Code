package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
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
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exceptions.CodingException;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.TopologyMaintainer;
import com.energyict.protocolimplv2.elster.garnet.exception.GarnetException;
import com.energyict.protocolimplv2.elster.garnet.structure.ConcentratorVersionResponseStructure;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author sva
 * @since 23/05/2014 - 9:12
 */
public class GarnetConcentrator implements DeviceProtocol, SerialNumberSupport {

    private OfflineDevice offlineDevice;
    private TopologyMaintainer topologyMaintainer;
    private RequestFactory requestFactory;

    private GarnetSecuritySupport securitySupport;
    private RegisterFactory registerFactory;
    private LogBookFactory logBookFactory;
    private ConcentratorMessaging messaging;
    private final PropertySpecService propertySpecService;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public GarnetConcentrator(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.propertySpecService = propertySpecService;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        setDevice(offlineDevice);
        getRequestFactory().setComChannel(comChannel);
    }

    @Override
    public void terminate() {
        //Nothing to do here...
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return this.getProperties().getUPLPropertySpecs();
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType(this.propertySpecService));
        result.add(new SioSerialConnectionType(this.propertySpecService));
        result.add(new RxTxSerialConnectionType(this.propertySpecService));
        return result;
    }

    @Override
    public void logOn() {
        try {
            getRequestFactory().openSession();
        } catch (GarnetException e) {
            throw CommunicationException.protocolConnectFailed(e);
        }
    }

    @Override
    public void daisyChainedLogOn() {
        logOn();
    }

    @Override
    public void logOff() {
        // No logoff required
    }

    @Override
    public void daisyChainedLogOff() {
        logOff();
    }

    @Override
    public String getSerialNumber() {
        try {
            ConcentratorVersionResponseStructure concentratorVersion = getRequestFactory().readConcentratorVersion();
            return ProtocolTools.removeLeadingZerosFromString(concentratorVersion.getSerialNumber().getSerialNumber());
        } catch (GarnetException e) {
            throw CommunicationException.unexpectedResponse(e);
        }
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = new ArrayList<>(loadProfilesToRead.size());
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {

            CollectedLoadProfileConfiguration configuration = this.collectedDataFactory.createCollectedLoadProfileConfiguration(
                    loadProfileReader.getProfileObisCode(),
                    loadProfileReader.getMeterSerialNumber()
            );
            configuration.setSupportedByMeter(false);
            configuration.setFailureInformation(ResultType.NotSupported, this.issueFactory.createProblem(loadProfileReader.getProfileObisCode(), "loadProfileXnotsupported", loadProfileReader.getProfileObisCode()));
            collectedLoadProfileConfigurations.add(configuration);
        }
        return collectedLoadProfileConfigurations;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        throw CodingException.unsupportedMethod(this.getClass(), "getLoadProfileData");
    }

    @Override
    public Date getTime() {
        return new Date();
    }

    @Override
    public void setTime(Date timeToSet) {
        // Garnet protocol has no formal time set method - time is automatically synced each communication session
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        // DeviceProtocolCache not used for Garnet protocol
    }

    @Override
    public String getProtocolDescription() {
        return "Elster Garnet Concentrator";
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getLogBookFactory().getLogBookData(logBooks);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getMessaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList(new TcpDeviceProtocolDialect(this.propertySpecService), new SerialDeviceProtocolDialect(this.propertySpecService));
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getRequestFactory().getProperties().addProperties(dialectProperties);

    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        getRequestFactory().getProperties().setUPLProperties(properties);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getRequestFactory().getProperties().addProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
        getRequestFactory().getProperties().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
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
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return getTopologyMaintainer().getDeviceTopology();
    }

    /**
     * The protocol version date
     */
    @Override
    public String getVersion() {
        return "$Date: Fri Dec 30 14:41:40 2016 +0100 $";
    }

    public GarnetProperties getProperties() {
        return getRequestFactory().getProperties();
    }

    public RequestFactory getRequestFactory() {
        if (requestFactory == null) {
            this.requestFactory = new RequestFactory(this.propertySpecService);
        }
        return requestFactory;
    }

    public RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            this.registerFactory = new RegisterFactory(this, collectedDataFactory, issueFactory);
        }
        return registerFactory;
    }

    public LogBookFactory getLogBookFactory() {
        if (this.logBookFactory == null) {
            this.logBookFactory = new LogBookFactory(this, collectedDataFactory, issueFactory);
        }
        return logBookFactory;
    }

    public ConcentratorMessaging getMessaging() {
        if (this.messaging == null) {
            this.messaging = new ConcentratorMessaging(this, collectedDataFactory, issueFactory);
        }
        return messaging;
    }

    public GarnetSecuritySupport getSecuritySupport() {
        if (securitySupport == null) {
            this.securitySupport = new GarnetSecuritySupport(propertySpecService);
        }
        return securitySupport;
    }

    public TopologyMaintainer getTopologyMaintainer() {
        if (topologyMaintainer == null) {
            this.topologyMaintainer = new TopologyMaintainer(this, collectedDataFactory, issueFactory);
        }
        return topologyMaintainer;
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    private void setDevice(OfflineDevice offlineDevice) {
        this.offlineDevice = offlineDevice;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(offlineDevice.getId());
        CollectedFirmwareVersion result = this.collectedDataFactory.createFirmwareVersionsCollectedData(deviceIdentifier);
        try {
            result.setActiveMeterFirmwareVersion(getRegisterFactory().readFirmwareVersion());
        } catch (GarnetException e) {
            result.setFailureInformation(
                    ResultType.InCompatible,
                    this.issueFactory.createProblem(
                            deviceIdentifier,
                            "issue.protocol.readingOfFirmwareFailed",
                            e.toString()));
        }
        return result;
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