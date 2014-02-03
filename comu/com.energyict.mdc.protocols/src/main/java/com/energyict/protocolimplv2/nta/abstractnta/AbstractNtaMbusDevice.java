package com.energyict.protocolimplv2.nta.abstractnta;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
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
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.nta.elster.AM100;
import com.energyict.protocolimplv2.security.NoSecuritySupport;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author sva
 * @since 2/11/12 (9:11)
 */
public abstract class AbstractNtaMbusDevice implements DeviceProtocol, SimpleMeter, DeviceMessageSupport {

    private PropertySpecService propertySpecService;
    private final AbstractNtaProtocol meterProtocol;
    private final String serialNumber;
    private final int physicalAddress;
    private final DeviceProtocolSecurityCapabilities securityCapabilities = new NoSecuritySupport();

    /**
     * Get the used MessageProtocol
     *
     * @return the DeviceMessageSupport message protocol
     */

    public abstract DeviceMessageSupport getMessageProtocol();

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    // TODO Implement me
    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
    }

    @Override
    public void terminate() {
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Collections.emptyList();
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
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Collections.emptyList();
    }

    @Override
    public void logOn() {
    }

    @Override
    public void daisyChainedLogOn() {
    }

    @Override
    public void logOff() {
    }

    @Override
    public void daisyChainedLogOff() {
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public void setTime(Date timeToSet) {
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
        return null;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return Collections.emptyList();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return null;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.emptyList();
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
    }

    @Override
    public final List<PropertySpec> getSecurityProperties() {
        return securityCapabilities.getSecurityProperties();
    }

    @Override
    public final String getSecurityRelationTypeName() {
        return securityCapabilities.getSecurityRelationTypeName();
    }

    @Override
    public final List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return securityCapabilities.getAuthenticationAccessLevels();
    }

    @Override
    public final List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return securityCapabilities.getEncryptionAccessLevels();
    }

    @Override
    public final PropertySpec getSecurityPropertySpec(String name) {
        return securityCapabilities.getSecurityPropertySpec(name);
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
    public String getVersion() {
        return null;
    }

    public AbstractNtaMbusDevice() {
        this.meterProtocol = new AM100();
        this.serialNumber = "CurrentlyUnKnown";
        this.physicalAddress = -1;
    }

    public AbstractNtaMbusDevice(final AbstractNtaProtocol meterProtocol, final String serialNumber, final int physicalAddress) {
        this.meterProtocol = meterProtocol;
        this.serialNumber = serialNumber;
        this.physicalAddress = physicalAddress;
    }

    @Override
    public TimeZone getTimeZone() {
        return this.meterProtocol.getTimeZone();
    }

    @Override
    public Logger getLogger() {
        return this.meterProtocol.getLogger();
    }

    @Override
    public String getSerialNumber() {
        return this.serialNumber;
    }

    @Override
    public int getPhysicalAddress() {
        return this.physicalAddress;
    }

    public AbstractNtaProtocol getMeterProtocol() {
        return meterProtocol;
    }

    @Override
    public void copyProperties(TypedProperties properties) {
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
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getMessageProtocol().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageProtocol().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessageProtocol().updateSentMessages(sentMessages);
    }
}
