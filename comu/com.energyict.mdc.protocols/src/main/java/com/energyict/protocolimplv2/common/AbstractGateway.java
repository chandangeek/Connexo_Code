package com.energyict.protocolimplv2.common;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
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
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;

import java.util.Date;
import java.util.List;

/**
 * Super class for every gateway protocol that implements the DeviceProtocol interface
 * <p/>
 * Copyrights EnergyICT
 * Date: 29/05/13
 * Time: 17:03
 * Author: khe
 */
public abstract class AbstractGateway implements DeviceProtocol {

    protected DeviceProtocolCache deviceCache;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        throw CommunicationException.unsupportedMethod(this.getClass(), "init");
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        throw CommunicationException.unsupportedMethod(this.getClass(), "getDeviceProtocolCapabilities");
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        throw CommunicationException.unsupportedMethod(this.getClass(), "getPropertySpecs");
    }

    @Override
    public PropertySpec getPropertySpec (String name) {
        throw CommunicationException.unsupportedMethod(this.getClass(), "getPropertySpec");
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        throw CommunicationException.unsupportedMethod(this.getClass(), "getSupportedConnectionTypes");
    }

    @Override
    public void logOn() {
        //Do nothing
    }

    @Override
    public void daisyChainedLogOn() {
        //Do nothing
    }

    @Override
    public void logOff() {
        //Do nothing
    }

    @Override
    public void daisyChainedLogOff() {
        //Do nothing
    }

    @Override
    public void terminate() {
        //Do nothing
    }

    @Override
    public String getSerialNumber() {
        throw CommunicationException.unsupportedMethod(this.getClass(), "getSerialNumber");
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        throw CommunicationException.unsupportedMethod(this.getClass(), "setDeviceCache");
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        throw CommunicationException.unsupportedMethod(this.getClass(), "getDeviceCache");
    }

    @Override
    public void setTime(Date timeToSet) {
        throw CommunicationException.unsupportedMethod(this.getClass(), "setTime");
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        throw CommunicationException.unsupportedMethod(this.getClass(), "fetchLoadProfileConfiguration");
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        throw CommunicationException.unsupportedMethod(this.getClass(), "getLoadProfileData");
    }

    @Override
    public Date getTime() {
        throw CommunicationException.unsupportedMethod(this.getClass(), "getTime");
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        throw CommunicationException.unsupportedMethod(this.getClass(), "getLogBookData");
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        throw CommunicationException.unsupportedMethod(this.getClass(), "getSupportedMessages");
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw CommunicationException.unsupportedMethod(this.getClass(), "executePendingMessages");
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        throw CommunicationException.unsupportedMethod(this.getClass(), "updateSentMessages");
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        throw CommunicationException.unsupportedMethod(this.getClass(), "format");
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        throw CommunicationException.unsupportedMethod(this.getClass(), "getDeviceProtocolDialects");
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        throw CommunicationException.unsupportedMethod(this.getClass(), "addDeviceProtocolDialectProperties");
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        throw CommunicationException.unsupportedMethod(this.getClass(), "setSecurityPropertySet");
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        throw CommunicationException.unsupportedMethod(this.getClass(), "readRegisters");
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        throw CommunicationException.unsupportedMethod(this.getClass(), "getDeviceTopology");
    }

    @Override
    public String getVersion() {
        throw CommunicationException.unsupportedMethod(this.getClass(), "getVersion");
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        throw CommunicationException.unsupportedMethod(this.getClass(), "addProperties");
    }

}