package com.energyict.protocols.mdc.adapter;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Adapter between a {@link com.energyict.mdc.upl.DeviceProtocol} and a {@link DeviceProtocol}.
 * <p>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 23/11/2016 - 16:56
 */
public class UPLDeviceProtocolAdapter extends AbstractUPLProtocolAdapter implements DeviceProtocol {

    /**
     * The UPL deviceProtocol instance {@link com.energyict.mdc.upl.DeviceProtocol} that needs to be wrapped (adapted)
     * so it is compliant with the Connexo DeviceProtocol interface: {@link DeviceProtocol}
     */
    private final com.energyict.mdc.upl.DeviceProtocol deviceProtocol;

    private UPLDeviceProtocolAdapter(com.energyict.mdc.upl.DeviceProtocol deviceProtocol) {
        this.deviceProtocol = deviceProtocol;
    }

    public static UPLDeviceProtocolAdapter adapt(com.energyict.mdc.upl.DeviceProtocol deviceProtocol) {
        return new UPLDeviceProtocolAdapter(deviceProtocol);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.deviceProtocol.init(offlineDevice, comChannel);
    }

    @Override
    public void terminate() {
        deviceProtocol.terminate();
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return deviceProtocol.getDeviceProtocolCapabilities();
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return null; //TODO?
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;   //TODO?
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return deviceProtocol.getCollectedCalendar();
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return null;    //TODO Govanni?
    }

    @Override
    public void logOn() {
        deviceProtocol.logOn();
    }

    @Override
    public void daisyChainedLogOn() {
        deviceProtocol.daisyChainedLogOn();
    }

    @Override
    public void logOff() {
        deviceProtocol.logOff();
    }

    @Override
    public void daisyChainedLogOff() {
        deviceProtocol.daisyChainedLogOff();
    }

    @Override
    public String getSerialNumber() {
        return deviceProtocol.getSerialNumber();
    }

    @Override
    public Date getTime() {
        return deviceProtocol.getTime();
    }

    @Override
    public void setTime(Date timeToSet) {
        deviceProtocol.setTime(timeToSet);
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return deviceProtocol.getBreakerStatus();
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return deviceProtocol.getDeviceCache();
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        deviceProtocol.setDeviceCache(deviceProtocolCache);
    }

    @Override
    public String getProtocolDescription() {
        return deviceProtocol.getProtocolDescription();
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return deviceProtocol.getFirmwareVersions();
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
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return null;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
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
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
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
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return deviceProtocol.readRegisters(registers);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return null;
    }

    @Override
    public String getVersion() {
        return deviceProtocol.getVersion();
    }

    @Override
    public void copyProperties(TypedProperties properties) {

    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return null;
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return deviceProtocol.supportsCommunicationFirmwareVersion();
    }
}