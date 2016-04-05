package com.energyict.protocolimplv2.abnt.elster;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.abnt.common.*;
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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author sva
 * @since 13/08/2014 - 11:28
 */
public class A1055 extends AbstractAbntProtocol implements SerialNumberSupport {

    private OfflineDevice offlineDevice;
    private RequestFactory requestFactory;
    private RegisterFactory registerFactory;
    private LogBookFactory logBookFactory;
    private LoadProfileBuilder loadProfileBuilder;
    private MessageFactory messageFactory;
    private DeviceProtocolSecurityCapabilities securitySupport;

    @Override
    public String getProtocolDescription() {
        return "Elster A1055 ABNT";
    }

    /**
     * The protocol version date
     */
    @Override
    public String getVersion() {
        return "$Date: 2015-11-26 15:25:57 +0200 (Thu, 26 Nov 2015)$";
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
    public List<PropertySpec> getRequiredProperties() {
        return getProperties().getRequiredProperties();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return getProperties().getOptionalProperties();
    }

    @Override
    public void addProperties(TypedProperties properties) {
        getProperties().addProperties(properties);
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
        return Arrays.<DeviceProtocolDialect>asList(new AbntSerialDeviceProtocolDialect(), new AbntOpticalDeviceProtocolDialect(), new AbntTransparentTCPDeviceProtocolDialect());
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
        return MdcManager.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierById(getOfflineDevice().getId()));
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
            this.loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return this.loadProfileBuilder;
    }

    public RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new RegisterFactory(this);
        }
        return this.registerFactory;
    }

    public LogBookFactory getLogBookFactory() {
        if (this.logBookFactory == null) {
            this.logBookFactory = new LogBookFactory(this);
        }
        return this.logBookFactory;
    }

    public MessageFactory getMessageFactory() {
        if (this.messageFactory == null) {
            this.messageFactory = new MessageFactory(this);
        }
        return this.messageFactory;
    }

    public DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (this.securitySupport == null) {
            this.securitySupport = new NoSecuritySupport();
        }
        return this.securitySupport;
    }
}