package com.energyict.protocolimplv2.common;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
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
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.security.InheritedAuthenticationDeviceAccessLevel;
import com.energyict.protocolimplv2.security.InheritedEncryptionDeviceAccessLevel;
import com.energyict.protocols.exception.UnsupportedMethodException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * The Abstract Mbus device implements the {@link DeviceProtocol} interface so we can
 * define this as a pluggable class in EIS 9.1.
 * Most of the methods throw an unsupportedMethod CodingException, if your subclass wants
 * to use one of these, then simply override them.
 * <p/>
 *
 * @author sva
 * @since 19/06/2014 - 8:11
 */
public abstract class AbstractMbusDevice implements DeviceProtocol {

    private final String serialNumber;
    private final DeviceProtocol meterProtocol;
    private final PropertySpecService propertySpecService;

    public abstract DeviceMessageSupport getDeviceMessageSupport();

    protected AbstractMbusDevice(DeviceProtocol meterProtocol, PropertySpecService propertySpecService) {
        this(meterProtocol, "CurrentlyUnKnown", propertySpecService);
    }

    protected AbstractMbusDevice(DeviceProtocol meterProtocol, String serialNumber, PropertySpecService propertySpecService) {
        this.meterProtocol = meterProtocol;
        this.serialNumber = serialNumber;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SLAVE);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Collections.emptyList();
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return getDeviceMessageSupport().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getDeviceMessageSupport().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getDeviceMessageSupport().updateSentMessages(sentMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getDeviceMessageSupport().format(propertySpec, messageAttribute);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList((DeviceProtocolDialect) new NoParamsDeviceProtocolDialect(propertySpecService));
    }

    /**
     * The serialNumber of the meter
     *
     * @return the serialNumber of the meter
     */
    public String getSerialNumber() {
        return this.serialNumber;
    }

    /**
     * Getter for the master DeviceProtocol
     *
     * @return the protocol of the master
     */
    public DeviceProtocol getMeterProtocol() {
        return meterProtocol;
    }

    @Override
    public List<PropertySpec> getSecurityPropertySpecs() {
        return getMeterProtocol().getSecurityPropertySpecs();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return getMeterProtocol().getSecurityRelationTypeName();
    }

    /**
     * Return the access levels of the master AND a dummy level that indicates that this device can also
     * simply inherit the security properties of the master device, instead of specifying the security properties again
     */
    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        List<AuthenticationDeviceAccessLevel> authenticationAccessLevels = new ArrayList<>();
        authenticationAccessLevels.addAll(getMeterProtocol().getAuthenticationAccessLevels());
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
        encryptionAccessLevels.addAll(getMeterProtocol().getEncryptionAccessLevels());
        encryptionAccessLevels.add(new InheritedEncryptionDeviceAccessLevel());
        return encryptionAccessLevels;
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        return getMeterProtocol().getSecurityPropertySpec(name);
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return null;
    }

    //############## Unsupported methods ##############//

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
        throw new UnsupportedMethodException(this.getClass(), "longOn");
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
        throw new UnsupportedMethodException(this.getClass(), "addDeviceProtocolDialectProperties");
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        throw new UnsupportedMethodException(this.getClass(), "setSecurityPropertySet");
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
        throw new UnsupportedMethodException(this.getClass(), "getTime");
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        throw new UnsupportedMethodException(this.getClass(), "setDeviceCache");
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
        throw new UnsupportedMethodException(this.getClass(), "getFirmwareVersions");
    }
}