package com.energyict.protocolimplv2.common;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.*;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.*;
import com.energyict.protocol.*;
import com.energyict.protocolimplv2.MdcManager;

import java.util.Date;
import java.util.List;

/**
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
    public List<PropertySpec> getRequiredProperties() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getRequiredProperties");
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getOptionalProperties");
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
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
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
    public void addProperties(TypedProperties properties) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "addProperties");
    }
}