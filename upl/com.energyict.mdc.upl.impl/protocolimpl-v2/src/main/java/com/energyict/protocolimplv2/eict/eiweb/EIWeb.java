package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.messages.convertor.EIWebMessageConverter;
import com.energyict.protocolimplv2.security.SimplePasswordSecuritySupport;

import java.util.*;

/**
 * Basic implementation of the EIWeb DeviceProtocol.
 * The basic implementation will not do much, mainly serve as a placeholder so the DeviceType can be created
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/10/13
 * Time: 12:02 PM
 */
public class EIWeb implements DeviceProtocol {

    private static final String PHONE_NUMBER = "PhoneNumber";
    private OfflineDevice offlineDevice;
    private SimplePasswordSecuritySupport securitySupport = new SimplePasswordSecuritySupport();
    private DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet;
    private LegacyMessageConverter messageConverter;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        // nothing much to do
        this.offlineDevice = offlineDevice;
    }


    @Override
    public void terminate() {
        // nothing much to do
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SESSION, DeviceProtocolCapabilities.PROTOCOL_MASTER);
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(getPhoneNumberPropertySpec());
        return optionalProperties;
    }

    private PropertySpec getPhoneNumberPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(PHONE_NUMBER);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.<ConnectionType>asList(new EIWebConnectionType());
    }

    @Override
    public void logOn() {
        // nothing much to do
    }

    @Override
    public void daisyChainedLogOn() {
        // nothing much to do
    }

    @Override
    public void logOff() {
        // nothing much to do
    }

    @Override
    public void daisyChainedLogOff() {
        // nothing much to do
    }

    @Override
    public String getSerialNumber() {
        return this.offlineDevice != null ? this.offlineDevice.getSerialNumber() : "";
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        // nothing much to do
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return Collections.emptyList();
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return Collections.emptyList();
    }

    @Override
    public Date getTime() {
        return new Date();
    }

    @Override
    public void setTime(Date timeToSet) {
        // nothing much to do
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return Collections.emptyList();
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getMessageConverter().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();     //Messages are executed in ProtocolHandler, not here
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();     //Messages are executed in ProtocolHandler, not here
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    private LegacyMessageConverter getMessageConverter() {
        if (messageConverter == null) {
            messageConverter = new EIWebMessageConverter();
        }
        return messageConverter;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new NoParamsDeviceProtocolDialect());
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        // nothing much to do
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        this.deviceProtocolSecurityPropertySet = deviceProtocolSecurityPropertySet;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return this.securitySupport.getSecurityProperties();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return this.securitySupport.getSecurityRelationTypeName();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return this.securitySupport.getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return this.securitySupport.getEncryptionAccessLevels();
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        return this.securitySupport.getSecurityPropertySpec(name);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return Collections.emptyList();
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return null;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU EIWeb";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
        // nothing much to do
    }
}
