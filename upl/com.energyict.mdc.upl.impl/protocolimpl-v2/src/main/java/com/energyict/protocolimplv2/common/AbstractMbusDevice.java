package com.energyict.protocolimplv2.common;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.security.InheritedAuthenticationDeviceAccessLevel;
import com.energyict.protocolimplv2.security.InheritedEncryptionDeviceAccessLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

    public abstract DeviceMessageSupport getDeviceMessageSupport();

    protected AbstractMbusDevice(DeviceProtocol meterProtocol) {
        this.meterProtocol = meterProtocol;
        this.serialNumber = "CurrentlyUnKnown";
    }

    protected AbstractMbusDevice(DeviceProtocol meterProtocol, String serialNumber) {
        this.meterProtocol = meterProtocol;
        this.serialNumber = serialNumber;
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
    public List<DeviceMessageSpec> getSupportedMessages() {
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
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getDeviceMessageSupport().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return "";
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList((DeviceProtocolDialect) new NoParamsDeviceProtocolDialect());
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
    public List<PropertySpec> getSecurityProperties() {
        return getMeterProtocol().getSecurityProperties();
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

    //############## Unsupported methods ##############//

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "init");
    }

    @Override
    public void terminate() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "terminate");
    }

    @Override
    public void logOn() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "logOn");
    }

    @Override
    public void daisyChainedLogOn() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "daisyChainedLogOn");
    }

    @Override
    public void logOff() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "logOff");
    }

    @Override
    public void daisyChainedLogOff() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "daisyChainedLogOff");
    }

    @Override
    public void setTime(Date timeToSet) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "setTime");
    }

    @Override
    public void addProperties(TypedProperties properties) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "addProperties");
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "addDeviceProtocolDialectProperties");
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "setSecurityPropertySet");
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "fetchLoadProfileConfiguration");
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getLoadProfileData");
    }

    @Override
    public Date getTime() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "createUnsupportedMethodException");
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "setDeviceCache");
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getDeviceCache");
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getLogBookData");
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "readRegisters");
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getDeviceTopology");
    }
}