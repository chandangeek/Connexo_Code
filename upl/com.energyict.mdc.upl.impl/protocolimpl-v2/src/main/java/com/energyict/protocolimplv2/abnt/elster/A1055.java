package com.energyict.protocolimplv2.abnt.elster;

import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.io.ConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
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
import com.energyict.mdc.upl.meterdata.ResultType;
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

import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimplv2.abnt.common.AbntProperties;
import com.energyict.protocolimplv2.abnt.common.AbstractAbntProtocol;
import com.energyict.protocolimplv2.abnt.common.LoadProfileBuilder;
import com.energyict.protocolimplv2.abnt.common.LogBookFactory;
import com.energyict.protocolimplv2.abnt.common.MessageFactory;
import com.energyict.protocolimplv2.abnt.common.RegisterFactory;
import com.energyict.protocolimplv2.abnt.common.RequestFactory;
import com.energyict.protocolimplv2.abnt.common.dialects.AbntOpticalDeviceProtocolDialect;
import com.energyict.protocolimplv2.abnt.common.dialects.AbntSerialDeviceProtocolDialect;
import com.energyict.protocolimplv2.abnt.common.dialects.AbntTransparentTCPDeviceProtocolDialect;
import com.energyict.protocolimplv2.abnt.common.exception.AbntException;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.DateTimeField;
import com.energyict.protocolimplv2.abnt.common.structure.ReadParameterFields;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.security.NoSecuritySupport;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

/**
 * @author sva
 * @since 13/08/2014 - 11:28
 */
public class A1055 extends AbstractAbntProtocol implements SerialNumberSupport {

    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final TariffCalendarExtractor calendarExtractor;

    private OfflineDevice offlineDevice;
    private RequestFactory requestFactory;
    private RegisterFactory registerFactory;
    private LogBookFactory logBookFactory;
    private LoadProfileBuilder loadProfileBuilder;
    private MessageFactory messageFactory;
    private DeviceProtocolSecurityCapabilities securitySupport;

    public A1055(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.calendarExtractor = calendarExtractor;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster A1055 ABNT";
    }

    /**
     * The protocol version date
     */
    @Override
    public String getVersion() {
        return "$Date: 2016-12-06 13:29:39 +0100 (Tue, 06 Dec 2016)$";
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
        return Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.getProperties().getPropertySpecs();
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        this.getProperties().setProperties(properties);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.<ConnectionType>asList(
                new SioSerialConnectionType(),
                new RxTxSerialConnectionType(),
                new SioOpticalConnectionType(),
                new RxTxOpticalConnectionType(),
                new OutboundTcpIpConnectionType()
        );
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList(new AbntSerialDeviceProtocolDialect(), new AbntOpticalDeviceProtocolDialect(propertySpecService), new AbntTransparentTCPDeviceProtocolDialect(propertySpecService));
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
            throw DataParseException.ioException(e);
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getRequestFactory().setTime(timeToSet);
        } catch (ParsingException e) {
            throw DataParseException.ioException(e);
        } catch (AbntException e) {
            throw DeviceConfigurationException.notAllowedToExecuteCommand("date/time change", e);
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
    public List<DeviceMessageSpec> getSupportedMessages() {
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
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getMessageFactory().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return "";
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return this.collectedDataFactory.createCollectedTopology(new DeviceIdentifierById(getOfflineDevice().getId()));
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
            this.requestFactory = new RequestFactory();
        }
        return this.requestFactory;
    }

    public void setRequestFactory(RequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    public LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this, collectedDataFactory, issueFactory);
        }
        return this.loadProfileBuilder;
    }

    public RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new RegisterFactory(this, collectedDataFactory, issueFactory);
        }
        return this.registerFactory;
    }

    public LogBookFactory getLogBookFactory() {
        if (this.logBookFactory == null) {
            this.logBookFactory = new LogBookFactory(this, collectedDataFactory, issueFactory);
        }
        return this.logBookFactory;
    }

    public MessageFactory getMessageFactory() {
        if (this.messageFactory == null) {
            this.messageFactory = new MessageFactory(this, collectedDataFactory, issueFactory, this.propertySpecService, this.nlsService, this.converter, this.calendarExtractor);
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
    public CollectedFirmwareVersion getFirmwareVersions() {
        return this.collectedDataFactory.createFirmwareVersionsCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return this.collectedDataFactory.createBreakerStatusCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        Issue warning = this.issueFactory.createWarning(
                this.offlineDevice,
                "issue.protocol.readingOfCalendarNotSupported",
                DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE);

        CollectedCalendar calendarCollectedData = this.collectedDataFactory.createCalendarCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
        calendarCollectedData.setFailureInformation(ResultType.NotSupported, warning);

        return calendarCollectedData;
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