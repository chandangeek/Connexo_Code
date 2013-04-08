package com.energyict.protocolimplv2.nta.abstractnta;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;
import com.energyict.protocolimplv2.nta.elster.AM100;

/**
 * @author: sva
 * @since: 2/11/12 (9:11)
 */
public abstract class AbstractNtaMbusDevice implements DeviceProtocol, SimpleMeter, DeviceMessageSupport {

    private final AbstractNtaProtocol meterProtocol;

    private final String serialNumber;
    private final int physicalAddress;

    /**
     * Get the used MessageProtocol
     *
     * @return the DeviceMessageSupport message protocol
     */

    public abstract DeviceMessageSupport getMessageProtocol();


    // TODO Implement me
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
    public List<ConnectionType> getSupportedConnectionTypes() {
        return null;
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
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
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
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return null;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
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
    public String getSecurityRelationTypeName() {
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
    public PropertySpec getSecurityPropertySpec(String name) {
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
    public void addProperties(TypedProperties properties) {
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return new ArrayList<PropertySpec>();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<PropertySpec>();
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
