package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.messages.convertor.EIWebMessageConverter;
import com.energyict.protocolimplv2.security.SimplePasswordSecuritySupport;
import com.energyict.protocols.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.protocols.mdc.services.impl.Bus;
import com.energyict.protocols.messaging.LegacyMessageConverter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Basic implementation of the EIWeb DeviceProtocol.
 * The basic implementation will not do much, mainly serve as a placeholder so the DeviceType can be created
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/10/13
 * Time: 12:02 PM
 */
public class EIWeb implements DeviceProtocol {

    private OfflineDevice offlineDevice;
    private SimplePasswordSecuritySupport securitySupport;
    private DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet;
    private LegacyMessageConverter messageConverter;

    @Override
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.securitySupport = new SimplePasswordSecuritySupport(propertySpecService);
    }

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
    public List<PropertySpec> getPropertySpecs () {
        return Collections.emptyList();
    }

    @Override
    public PropertySpec getPropertySpec (String name) {
        return null;
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
        if (this.offlineDevice != null) {
            return this.offlineDevice.getSerialNumber();
        }
        else {
            return "";
        }
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        // nothing much to do
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public void setTime(Date timeToSet) {
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
        return Bus.getClock().now();
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
        return this.getCollectedDataFactory().createEmptyCollectedMessageList();     //Messages are executed in ProtocolHandler, not here
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.getCollectedDataFactory().createEmptyCollectedMessageList();     //Messages are executed in ProtocolHandler, not here
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
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        // nothing much to do
    }

    private CollectedDataFactory getCollectedDataFactory() {
        List<CollectedDataFactory> factories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(CollectedDataFactory.class);
        if (factories.isEmpty()) {
            throw CommunicationException.missingModuleException(CollectedDataFactory.class);
        }
        else {
            return factories.get(0);
        }
    }

}
