package com.elster.us.protocolimplv2;

import com.energyict.mdc.io.ConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author sva
 * @since 20/01/2016 - 17:18
 */
public class SampleProtocol implements DeviceProtocol {

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {

    }

    @Override
    public void terminate() {

    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return null;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
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
    public String getSerialNumber() {
        return null;
    }

    @Override
    public void setTime(Date timeToSet) {

    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return null;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return null;
    }

    @Override
    public Date getTime() {
        return null;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {

    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public String getProtocolDescription() {
        return null;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return null;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return null;
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
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return null;
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return null;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return null;
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {

    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {

    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return null;
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return null;
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return null;
    }

    @Override
    public Optional<PropertySpec> getSecurityPropertySpec(String name) {
        return null;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return null;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {

    }

}