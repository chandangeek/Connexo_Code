package com.energyict.protocolimplv2.abnt.elster;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.data.*;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.abnt.common.AbntProperties;
import com.energyict.protocolimplv2.abnt.common.AbstractAbntProtocol;
import com.energyict.protocolimplv2.abnt.common.LoadProfileBuilder;
import com.energyict.protocolimplv2.abnt.common.LogBookFactory;
import com.energyict.protocolimplv2.abnt.common.MessageFactory;
import com.energyict.protocolimplv2.abnt.common.RegisterFactory;
import com.energyict.protocolimplv2.abnt.common.RequestFactory;
import com.energyict.protocolimplv2.abnt.common.dialects.AbntOpticalDeviceProtocolDialect;
import com.energyict.protocolimplv2.abnt.common.dialects.AbntSerialDeviceProtocolDialect;
import com.energyict.protocolimplv2.abnt.common.exception.AbntException;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.DateTimeField;
import com.energyict.protocolimplv2.abnt.common.structure.ReadParameterFields;
import com.energyict.protocolimplv2.security.NoSecuritySupport;
import com.energyict.protocols.impl.channels.serial.direct.rxtx.RxTxPlainSerialConnectionType;
import com.energyict.protocols.impl.channels.serial.direct.serialio.SioPlainSerialConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

/**
 * @author sva
 * @since 13/08/2014 - 11:28
 */
public class A1055 extends AbstractAbntProtocol {

    private OfflineDevice offlineDevice;
    private RequestFactory requestFactory;
    private RegisterFactory registerFactory;
    private LogBookFactory logBookFactory;
    private LoadProfileBuilder loadProfileBuilder;
    private MessageFactory messageFactory;
    private DeviceProtocolSecurityCapabilities securitySupport;
    private final PropertySpecService propertySpecService;
    private final SerialComponentService serialComponentService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final Clock clock;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;

    @Inject
    public A1055(PropertySpecService propertySpecService, SerialComponentService serialComponentService, MdcReadingTypeUtilService readingTypeUtilService, Clock clock, IssueService issueService, CollectedDataFactory collectedDataFactory, MeteringService meteringService) {
        this.propertySpecService = propertySpecService;
        this.serialComponentService = serialComponentService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.clock = clock;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster A1055 ABNT";
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return null;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    /**
     * The protocol version date
     */
    @Override
    public String getVersion() {
        return "$Date: 2014-11-17 10:33:16 +0100 (Mon, 17 Nov 2014) $";
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        getProperties().addProperties(properties);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        setOfflineDevice(offlineDevice);
        getRequestFactory().setComChannel(comChannel);
    }

    @Override
    public void terminate() {
        //Nothing to do here...
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.<ConnectionType>asList(
                new SioPlainSerialConnectionType(getSerialComponentService()),
                new RxTxPlainSerialConnectionType(getSerialComponentService()),
                new SioOpticalConnectionType(getSerialComponentService()),
                new RxTxOpticalConnectionType(getSerialComponentService())
        );
    }

    private SerialComponentService getSerialComponentService() {
        return this.serialComponentService;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new AbntSerialDeviceProtocolDialect(propertySpecService), new AbntOpticalDeviceProtocolDialect(propertySpecService));
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getProperties().addProperties(dialectProperties);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getProperties().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
    }

    @Override
    public List<PropertySpec> getSecurityPropertySpecs() {
        return getSecuritySupport().getSecurityPropertySpecs();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return getSecuritySupport().getSecurityRelationTypeName();
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
    public PropertySpec getSecurityPropertySpec(String name) {
        return getSecuritySupport().getSecurityPropertySpec(name);
    }

    @Override
    public void logOn() {
        getRequestFactory().logOn();
    }

    @Override
    public void daisyChainedLogOn() {
        logOn();
    }

    @Override
    public void logOff() {
        // No logOff required
    }

    @Override
    public void daisyChainedLogOff() {
        // Not the case
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        // DeviceProtocolCache not used for the ABNT protocol
    }

    @Override
    public String getSerialNumber() {
        return getRequestFactory().getMeterSerialNumber();
    }

    @Override
    public Date getTime() {
        try {
            DateTimeField dateTimeField = (DateTimeField) getRequestFactory().readDefaultParameters().getField(ReadParameterFields.currentDateTime);
            return dateTimeField.getDate(getProperties().getTimeZone());
        } catch (ParsingException e) {
            throw new CommunicationException(MessageSeeds.GENERAL_PARSE_ERROR, e);
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getRequestFactory().setTime(timeToSet);
        } catch (ParsingException e) {
            throw new CommunicationException(MessageSeeds.GENERAL_PARSE_ERROR, e);
        } catch (AbntException e) {
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e, "date/time change");
        }
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getLogBookFactory().getLogBookData(logBooks);
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return getMessageFactory().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageFactory().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessageFactory().updateSentMessages(sentMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getMessageFactory().format(propertySpec, messageAttribute);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return this.collectedDataFactory.createCollectedTopology(getOfflineDevice().getDeviceIdentifier());
    }

    public AbntProperties getProperties() {
        return getRequestFactory().getProperties();
    }

    @Override
    public TimeZone getTimeZone() {
        return getProperties().getTimeZone();
    }

    public OfflineDevice getOfflineDevice() {
        return this.offlineDevice;
    }

    public void setOfflineDevice(OfflineDevice offlineDevice) {
        this.offlineDevice = offlineDevice;
    }

    public RequestFactory getRequestFactory() {
        if (this.requestFactory == null) {
            this.requestFactory = new RequestFactory(propertySpecService);
        }
        return this.requestFactory;
    }

    public void setRequestFactory(RequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    public LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this, this.readingTypeUtilService, this.issueService, collectedDataFactory);
        }
        return this.loadProfileBuilder;
    }

    public RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new RegisterFactory(this, clock, issueService, collectedDataFactory);
        }
        return this.registerFactory;
    }

    public LogBookFactory getLogBookFactory() {
        if (this.logBookFactory == null) {
            this.logBookFactory = new LogBookFactory(this, issueService, collectedDataFactory, meteringService);
        }
        return this.logBookFactory;
    }

    public MessageFactory getMessageFactory() {
        if (this.messageFactory == null) {
            this.messageFactory = new MessageFactory(this, issueService, collectedDataFactory);
        }
        return this.messageFactory;
    }

    public DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (this.securitySupport == null) {
            this.securitySupport = new NoSecuritySupport();
        }
        return this.securitySupport;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return getProperties().getPropertySpecs();
    }

    @Override
    public PropertySpec getPropertySpec(String s) {
        return getProperties().getPropertySpec(s);
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return collectedDataFactory.createFirmwareVersionsCollectedData(offlineDevice.getDeviceIdentifier());
    }
}