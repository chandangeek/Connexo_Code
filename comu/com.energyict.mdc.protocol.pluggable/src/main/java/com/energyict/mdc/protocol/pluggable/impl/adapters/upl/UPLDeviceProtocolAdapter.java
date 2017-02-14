package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.NestedPropertyValidationException;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLConnectionTypeAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.cps.SecurityCustomPropertySetNameDetective;
import com.energyict.mdc.protocol.pluggable.adapters.upl.cps.UnableToLoadCustomPropertySetClass;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter between a {@link com.energyict.mdc.upl.DeviceProtocol} and a {@link DeviceProtocol}.
 * <p>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 23/11/2016 - 16:56
 */
public class UPLDeviceProtocolAdapter implements DeviceProtocol, UPLProtocolAdapter {

    private static SecurityCustomPropertySetNameDetective securityCustomPropertySetNameDetective;

    /**
     * The UPL deviceProtocol instance {@link com.energyict.mdc.upl.DeviceProtocol} that needs to be wrapped (adapted)
     * so it is compliant with the Connexo DeviceProtocol interface: {@link DeviceProtocol}
     */
    private final com.energyict.mdc.upl.DeviceProtocol deviceProtocol;
    private final CustomPropertySetInstantiatorService customPropertySetInstantiatorService;

    private UPLDeviceProtocolAdapter(com.energyict.mdc.upl.DeviceProtocol deviceProtocol, CustomPropertySetInstantiatorService customPropertySetInstantiatorService) {
        this.deviceProtocol = deviceProtocol;
        this.customPropertySetInstantiatorService = customPropertySetInstantiatorService;
    }

    public static Services adapt(com.energyict.mdc.upl.DeviceProtocol deviceProtocol) {
        return new Services(deviceProtocol);
    }

    @Override
    public Class getActualClass() {
        return deviceProtocol.getClass();
    }

    @Override
    public void init(OfflineDevice offlineDevice, com.energyict.mdc.protocol.ComChannel comChannel) {
        this.deviceProtocol.init(offlineDevice, comChannel);
    }

    @Override
    public void init(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, com.energyict.mdc.protocol.ComChannel comChannel) {
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
        return deviceProtocol.getDeviceFunction(); //TODO?
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return deviceProtocol.getManufacturerInformation(); //TODO?
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return deviceProtocol.getCollectedCalendar();
    }

    @Override
    public List<? extends ConnectionType> getSupportedConnectionTypes() {
        return deviceProtocol.getSupportedConnectionTypes()
                .stream()
                .map(connectionType -> new UPLConnectionTypeAdapter(connectionType, customPropertySetInstantiatorService))
                .collect(Collectors.toList());
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
        return deviceProtocol.fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return deviceProtocol.getLoadProfileData(loadProfiles);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return deviceProtocol.getLogBookData(logBooks);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return deviceProtocol.getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return deviceProtocol.executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return deviceProtocol.updateSentMessages(sentMessages);
    }

    @Override
    public String format(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return deviceProtocol.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return deviceProtocol.prepareMessageContext(device, offlineDevice, deviceMessage);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return deviceProtocol.getDeviceProtocolDialects().stream().map(dialect -> new UPLDeviceProtocolDialectAdapter(dialect, customPropertySetInstantiatorService)).collect(Collectors.toList());
    }

    @Override
    public void addDeviceProtocolDialectProperties(com.energyict.mdc.upl.properties.TypedProperties dialectProperties) {
        deviceProtocol.addDeviceProtocolDialectProperties(dialectProperties);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        deviceProtocol.setSecurityPropertySet(deviceProtocolSecurityPropertySet);
    }

    @Override
    public Optional<CustomPropertySet<Device, ? extends PersistentDomainExtension<Device>>> getCustomPropertySet() {
        this.ensureSecurityCustomPropertySetNameMappingLoaded();
        String cpsJavaClassName = securityCustomPropertySetNameDetective.securityCustomPropertySetClassNameFor(this.deviceProtocol.getClass());

        if (Checks.is(cpsJavaClassName).emptyOrOnlyWhiteSpace()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(customPropertySetInstantiatorService.createCustomPropertySet(cpsJavaClassName));
            } catch (ClassNotFoundException e) {
                throw new UnableToLoadCustomPropertySetClass(e, cpsJavaClassName, SecurityCustomPropertySetNameDetective.MAPPING_PROPERTIES_FILE_NAME);
            }
        }
    }

    private void ensureSecurityCustomPropertySetNameMappingLoaded() {
        if (securityCustomPropertySetNameDetective == null) {
            securityCustomPropertySetNameDetective = new SecurityCustomPropertySetNameDetective(customPropertySetInstantiatorService);
        }
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
        return deviceProtocol.getSecurityProperties();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return deviceProtocol.getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return deviceProtocol.getEncryptionAccessLevels();
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return deviceProtocol.readRegisters(registers);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return deviceProtocol.getDeviceTopology();
    }

    @Override
    public String getVersion() {
        return deviceProtocol.getVersion();
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        try {
            deviceProtocol.setUPLProperties(properties);
        } catch (PropertyValidationException e) {
            throw new NestedPropertyValidationException(e);
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return new ArrayList<>(this.deviceProtocol.getUPLPropertySpecs().stream().map(UPLToConnexoPropertySpecAdapter::new).collect(Collectors.toList()));
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return new ArrayList<>(this.deviceProtocol.getUPLPropertySpecs());
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        deviceProtocol.setUPLProperties(properties);
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return deviceProtocol.supportsCommunicationFirmwareVersion();
    }

    public static final class Services {
        private final com.energyict.mdc.upl.DeviceProtocol deviceProtocol;

        public Services(com.energyict.mdc.upl.DeviceProtocol deviceProtocol) {
            this.deviceProtocol = deviceProtocol;
        }

        public UPLDeviceProtocolAdapter with(
                CustomPropertySetInstantiatorService customPropertySetInstantiatorService) {
            return new UPLDeviceProtocolAdapter(this.deviceProtocol, customPropertySetInstantiatorService);
        }
    }
}