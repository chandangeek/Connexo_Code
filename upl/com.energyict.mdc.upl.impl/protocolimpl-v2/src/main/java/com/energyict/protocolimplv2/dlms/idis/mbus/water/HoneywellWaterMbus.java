package com.energyict.protocolimplv2.dlms.idis.mbus.water;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
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

import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolcommon.exceptions.CodingException;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.security.InheritedAuthenticationDeviceAccessLevel;
import com.energyict.protocolimplv2.security.InheritedEncryptionDeviceAccessLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public abstract class HoneywellWaterMbus implements DeviceProtocol {

    private final NlsService nlsService;
    private final PropertySpecService propertySpecService;
    private final Converter converter;

    public HoneywellWaterMbus(NlsService nlsService, PropertySpecService propertySpecService, Converter converter) {
        this.nlsService = nlsService;
        this.propertySpecService = propertySpecService;
        this.converter = converter;
    }

    public NlsService getNlsService() {
        return nlsService;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public Converter getConverter(){
        return converter;
    }

    @Override
    public List<? extends ConnectionType> getSupportedConnectionTypes() {
        return Collections.emptyList();
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_SLAVE);
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList((DeviceProtocolDialect) new NoParamsDeviceProtocolDialect(getNlsService()));
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        List<AuthenticationDeviceAccessLevel> authenticationAccessLevels = new ArrayList<>();
        authenticationAccessLevels.add(new InheritedAuthenticationDeviceAccessLevel());
        return authenticationAccessLevels;
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        List<EncryptionDeviceAccessLevel> encryptionAccessLevels = new ArrayList<>();
        encryptionAccessLevels.add(new InheritedEncryptionDeviceAccessLevel());
        return encryptionAccessLevels;
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return Optional.empty();
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    @Override
    public String getSerialNumber() {
        return "Unknown";
    }

    //############## Unsupported methods ##############//

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        throw CodingException.unsupportedMethod(this.getClass(), "getSupportedMessages");
    }

    @Override
    public void logOn() {
        throw CodingException.unsupportedMethod(this.getClass(), "logOn");
    }

    @Override
    public void daisyChainedLogOn() {
        throw CodingException.unsupportedMethod(this.getClass(), "daisyChainedLogOn");
    }

    @Override
    public void logOff() {
        throw CodingException.unsupportedMethod(this.getClass(), "logOff");
    }

    @Override
    public void daisyChainedLogOff() {
        throw CodingException.unsupportedMethod(this.getClass(), "daisyChainedLogOff");
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        throw CodingException.unsupportedMethod(this.getClass(), "setDeviceCache");
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        throw CodingException.unsupportedMethod(this.getClass(), "getDeviceCache");
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        throw CodingException.unsupportedMethod(this.getClass(), "init");
    }

    @Override
    public void terminate() {
        throw CodingException.unsupportedMethod(this.getClass(), "terminate");
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        throw CodingException.unsupportedMethod(this.getClass(), "getCollectedCalendar");
    }

    @Override
    public Date getTime() {
        throw CodingException.unsupportedMethod(this.getClass(), "getTime");
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        throw CodingException.unsupportedMethod(this.getClass(), "getBreakerStatus");
    }

    @Override
    public void setTime(Date timeToSet) {
        throw CodingException.unsupportedMethod(this.getClass(), "setTime");
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        throw CodingException.unsupportedMethod(this.getClass(), "fetchLoadProfileConfiguration");
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        throw CodingException.unsupportedMethod(this.getClass(), "getLoadProfileData");
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        throw CodingException.unsupportedMethod(this.getClass(), "getLogBookData");
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return null;
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return null;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        throw CodingException.unsupportedMethod(this.getClass(), "readRegisters");
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        throw CodingException.unsupportedMethod(this.getClass(), "getDeviceTopology");
    }

    @Override
    public CollectedCreditAmount getCreditAmount() {
        throw CodingException.unsupportedMethod(this.getClass(), "getCreditAmount");
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        throw CodingException.unsupportedMethod(this.getClass(), "getFirmwareVersions");
    }
}