package com.energyict.protocolimplv2.common;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.util.Date;
import java.util.List;
import java.util.Set;

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
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "init", this.getClass());
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "getDeviceProtocolCapabilities", this.getClass());
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "getSupportedConnectionTypes", this.getClass());
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
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "getSerialNumber", this.getClass());
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "setDeviceCache", this.getClass());
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "getDeviceCache", this.getClass());
    }

    @Override
    public void setTime(Date timeToSet) {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "setTime", this.getClass());
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "fetchLoadProfileConfiguration", this.getClass());
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "getLoadProfileData", this.getClass());
    }

    @Override
    public Date getTime() {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "getTime", this.getClass());
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "getLogBookData", this.getClass());
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "getSupportedMessages", this.getClass());
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "executePendingMessages", this.getClass());
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "updateSentMessages", this.getClass());
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "format", this.getClass());
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "getDeviceProtocolDialects", this.getClass());
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "addDeviceProtocolDialectProperties", this.getClass());
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "setSecurityPropertySet", this.getClass());
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "readRegisters", this.getClass());
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "getDeviceTopology", this.getClass());
    }

    @Override
    public String getVersion() {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "getVersion", this.getClass());
    }


}