package com.energyict.protocolimplv2.dlms;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.*;
import com.energyict.mdc.protocol.api.device.data.*;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.security.InheritedAuthenticationDeviceAccessLevel;
import com.energyict.protocolimplv2.security.InheritedEncryptionDeviceAccessLevel;
import com.energyict.protocols.exception.UnsupportedMethodException;

import java.util.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 15/01/2015 - 10:18
 */
public abstract class AbstractDlmsSlaveProtocol implements DeviceProtocol {

    private final PropertySpecService propertySpecService;

    protected AbstractDlmsSlaveProtocol(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    abstract protected DeviceProtocolSecurityCapabilities getSecurityCapabilities();

    abstract protected DeviceMessageSupport getDeviceMessageSupport();

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_SLAVE);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return new ArrayList<>(0);
    }


    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList((DeviceProtocolDialect) new NoParamsDeviceProtocolDialect(propertySpecService));
    }


    @Override
    public String getSecurityRelationTypeName() {
        return getSecurityCapabilities().getSecurityRelationTypeName();
    }

    /**
     * Return the access levels of the master AND a dummy level that indicates that this device can also
     * simply inherit the security properties of the master device, instead of specifying the security properties again
     */
    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        List<AuthenticationDeviceAccessLevel> authenticationAccessLevels = new ArrayList<>();
        authenticationAccessLevels.addAll(getSecurityCapabilities().getAuthenticationAccessLevels());
        authenticationAccessLevels.add(new InheritedAuthenticationDeviceAccessLevel());
        return authenticationAccessLevels;
    }

    /**
     * Return the access levels of the master AND a dummy level that indicates that this device can also
     * simply inherit the security properties of the master device, instead of specifying the security properties again
     */
    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        List<EncryptionDeviceAccessLevel> encryptionAccessLevels = new ArrayList<>();
        encryptionAccessLevels.addAll(getSecurityCapabilities().getEncryptionAccessLevels());
        encryptionAccessLevels.add(new InheritedEncryptionDeviceAccessLevel());
        return encryptionAccessLevels;
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        return getSecurityCapabilities().getSecurityPropertySpec(name);
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return getDeviceMessageSupport().getSupportedMessages();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getDeviceMessageSupport().format(propertySpec, messageAttribute);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw new UnsupportedMethodException(this.getClass(), "executePendingMessages");
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        throw new UnsupportedMethodException(this.getClass(), "updateSentMessages");
    }

    @Override
    public String getSerialNumber() {
        throw new UnsupportedMethodException(this.getClass(), "getSerialNumber");
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        throw new UnsupportedMethodException(this.getClass(), "init");
    }

    @Override
    public void terminate() {
        throw new UnsupportedMethodException(this.getClass(), "terminate");
    }

    @Override
    public void logOn() {
        throw new UnsupportedMethodException(this.getClass(), "logOn");
    }

    @Override
    public void daisyChainedLogOn() {
        throw new UnsupportedMethodException(this.getClass(), "daisyChainedLogOn");
    }

    @Override
    public void logOff() {
        throw new UnsupportedMethodException(this.getClass(), "logOff");
    }

    @Override
    public void daisyChainedLogOff() {
        throw new UnsupportedMethodException(this.getClass(), "daisyChainedLogOff");
    }

    @Override
    public void setTime(Date timeToSet) {
        throw new UnsupportedMethodException(this.getClass(), "setTime");
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        // nothing to do here, move along ...
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        // nothing to do here, move along ...
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        throw new UnsupportedMethodException(this.getClass(), "fetchLoadProfileConfiguration");
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        throw new UnsupportedMethodException(this.getClass(), "getLoadProfileData");
    }

    @Override
    public Date getTime() {
        throw new UnsupportedMethodException(this.getClass(), "createUnsupportedMethodException");
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        // nothing to do here, move along ...
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        throw new UnsupportedMethodException(this.getClass(), "getDeviceCache");
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        throw new UnsupportedMethodException(this.getClass(), "getLogBookData");
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        throw new UnsupportedMethodException(this.getClass(), "readRegisters");
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        throw new UnsupportedMethodException(this.getClass(), "getDeviceTopology");
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        throw new UnsupportedMethodException(this.getClass(), "getFirmwareVersion");
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public String getProtocolDescription() {
        throw new UnsupportedMethodException(this.getClass(), "getProtocolDescription");
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        throw new UnsupportedMethodException(this.getClass(), "getDeviceFunction");
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        throw new UnsupportedMethodException(this.getClass(), "getManufacturerInformation");
    }

    @Override
    public List<PropertySpec> getSecurityPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        // nothing to do here, move along ...
    }
}