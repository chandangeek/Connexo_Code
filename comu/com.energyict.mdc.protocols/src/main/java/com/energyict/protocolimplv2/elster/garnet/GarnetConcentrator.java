/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
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
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocols.exception.UnsupportedMethodException;
import com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.protocols.impl.channels.serial.direct.rxtx.RxTxPlainSerialConnectionType;
import com.energyict.protocols.impl.channels.serial.direct.serialio.SioPlainSerialConnectionType;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.TopologyMaintainer;
import com.energyict.protocolimplv2.elster.garnet.exception.GarnetException;
import com.energyict.protocolimplv2.elster.garnet.structure.ConcentratorVersionResponseStructure;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.energyict.mdc.protocol.api.MessageSeeds.COULD_NOT_PARSE_REGISTER_DATA;
import static com.energyict.mdc.protocol.api.MessageSeeds.LOADPROFILE_NOT_SUPPORTED;

/**
 * @author sva
 * @since 23/05/2014 - 9:12
 */
public class GarnetConcentrator implements DeviceProtocol {

    private OfflineDevice offlineDevice;
    private TopologyMaintainer topologyMaintainer;
    private RequestFactory requestFactory;

    private SecuritySupport securitySupport;
    private RegisterFactory registerFactory;
    private LogBookFactory logBookFactory;
    private ConcentratorMessaging messaging;

    private final PropertySpecService propertySpecService;
    private final SocketService socketService;
    private final SerialComponentService serialComponentService;
    private final IssueService issueService;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;
    private final Provider<SecuritySupport> securitySupportProvider;

    @Inject
    public GarnetConcentrator(PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService, IssueService issueService, Clock clock, Thesaurus thesaurus, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, Provider<SecuritySupport> securitySupportProvider) {
        this.propertySpecService = propertySpecService;
        this.socketService = socketService;
        this.serialComponentService = serialComponentService;
        this.issueService = issueService;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.identificationService = identificationService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
        this.securitySupportProvider = securitySupportProvider;
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
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(
                new OutboundTcpIpConnectionType(this.thesaurus, getPropertySpecService(), getSocketService()),
                new SioPlainSerialConnectionType(getSerialComponentService(), this.thesaurus),
                new RxTxPlainSerialConnectionType(getSerialComponentService(), this.thesaurus));
    }

    private SerialComponentService getSerialComponentService() {
        return this.serialComponentService;
    }

    private SocketService getSocketService() {
        return this.socketService;
    }

    private PropertySpecService getPropertySpecService() {
        return this.propertySpecService;
    }

    @Override
    public void logOn() {
        try {
            getRequestFactory().openSession();
        } catch (GarnetException e) {
            throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, e);
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
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        // Garnet protocol has no formal time set method - time is automatically synced each communication session
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = new ArrayList<>(loadProfilesToRead.size());
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            CollectedLoadProfileConfiguration configuration = this.collectedDataFactory.createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getDeviceIdentifier(), false);
            configuration.setFailureInformation(
                    ResultType.NotSupported,
                    this.issueService.newProblem(
                            loadProfileReader.getProfileObisCode(),
                            LOADPROFILE_NOT_SUPPORTED,
                            loadProfileReader.getProfileObisCode()));
            collectedLoadProfileConfigurations.add(configuration);
        }
        return collectedLoadProfileConfigurations;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        throw new UnsupportedMethodException(this.getClass(), "getLoadProfileData");
    }

    @Override
    public Date getTime() {
        return new Date();
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        // DeviceProtocolCache not used for Garnet protocol
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster Garnet Concentrator";
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
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getLogBookFactory().getLogBookData(logBooks);
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
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
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(propertySpec, messageAttribute);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(
                new GarnetTcpDeviceProtocolDialect(this.thesaurus, this.propertySpecService),
                new GarnetSerialDeviceProtocolDialect(this.thesaurus, this.propertySpecService));
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getRequestFactory().getProperties().addProperties(dialectProperties);

    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getRequestFactory().getProperties().addProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
        getRequestFactory().getProperties().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
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
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return getTopologyMaintainer().getDeviceTopology();
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-10-03 14:19:42 +0200 (Fri, 03 Oct 2014) $";
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        getGarnetProperties().addProperties(properties);
    }

    public GarnetProperties getGarnetProperties() {
        return getRequestFactory().getProperties();
    }

    public RequestFactory getRequestFactory() {
        if (requestFactory == null) {
            this.requestFactory = new RequestFactory(this.clock, propertySpecService);
        }
        return requestFactory;
    }

    public RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            this.registerFactory = new RegisterFactory(this, clock, issueService, collectedDataFactory);
        }
        return registerFactory;
    }

    public LogBookFactory getLogBookFactory() {
        if (this.logBookFactory == null) {
            this.logBookFactory = new LogBookFactory(this, issueService, collectedDataFactory, meteringService);
        }
        return logBookFactory;
    }

    public ConcentratorMessaging getMessaging() {
        if (this.messaging == null) {
            this.messaging = new ConcentratorMessaging(this, issueService, collectedDataFactory);
        }
        return messaging;
    }

    public SecuritySupport getSecuritySupport() {
        if (securitySupport == null) {
            this.securitySupport = securitySupportProvider.get();
        }
        return securitySupport;
    }

    public TopologyMaintainer getTopologyMaintainer() {
        if (topologyMaintainer == null) {
            this.topologyMaintainer = new TopologyMaintainer(this, issueService, identificationService, collectedDataFactory);
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
    public List<PropertySpec> getPropertySpecs() {
        return getGarnetProperties().getPropertySpecs();
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        CollectedFirmwareVersion firmwareVersionsCollectedData = collectedDataFactory.createFirmwareVersionsCollectedData(getOfflineDevice().getDeviceIdentifier());
        try {
            firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(getRegisterFactory().readFirmwareVersion());
        } catch (GarnetException e) {
            firmwareVersionsCollectedData.setFailureInformation(
                    ResultType.InCompatible,
                    this.issueService.newProblem(
                            "FirmwareVersion",
                            COULD_NOT_PARSE_REGISTER_DATA));
        }
        return firmwareVersionsCollectedData;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return collectedDataFactory.createBreakerStatusCollectedData(offlineDevice.getDeviceIdentifier());
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return this.collectedDataFactory.createCalendarCollectedData(this.getOfflineDevice().getDeviceIdentifier());
    }

}