package com.energyict.protocolimplv2.common;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionType;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.DeviceProtocolDialect;
import com.energyict.mdc.protocol.LoadProfileReader;
import com.energyict.mdc.protocol.LogBookReader;
import com.energyict.mdc.protocol.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.device.data.CollectedRegister;
import com.energyict.mdc.protocol.device.data.CollectedTopology;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocolimplv2.MdcManager;

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
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "init");
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getDeviceProtocolCapabilities");
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getPropertySpecs");
    }

    @Override
    public PropertySpec getPropertySpec (String name) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getPropertySpec");
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getSupportedConnectionTypes");
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
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getSerialNumber");
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
    public void setTime(Date timeToSet) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "setTime");
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
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getTime");
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getLogBookData");
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getSupportedMessages");
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "executePendingMessages");
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "updateSentMessages");
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "format");
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getDeviceProtocolDialects");
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
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "readRegisters");
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getDeviceTopology");
    }

    @Override
    public String getVersion() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getVersion");
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "addProperties");
    }

}