package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.exceptions.NestedPropertyValidationException;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.journal.ProtocolJournal;
import com.energyict.mdc.protocol.pluggable.adapters.upl.TypedPropertiesValueAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLConnectionTypeAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.*;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.*;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;

import javax.xml.bind.annotation.*;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter between a {@link com.energyict.mdc.upl.DeviceProtocol} and a {@link DeviceProtocol}.
 * <p>
 *
 * @author khe
 * @since 23/11/2016 - 16:56
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UPLDeviceProtocolAdapter implements DeviceProtocol, UPLProtocolAdapter<com.energyict.mdc.upl.DeviceProtocol> {

    /**
     * The UPL actual instance {@link com.energyict.mdc.upl.DeviceProtocol} that needs to be wrapped (adapted)
     * so it is compliant with the Connexo DeviceProtocol interface: {@link DeviceProtocol}
     */
    private com.energyict.mdc.upl.DeviceProtocol actual;
    private CustomPropertySetInstantiatorService customPropertySetInstantiatorService;

    public UPLDeviceProtocolAdapter() {
        super();
    }

    private UPLDeviceProtocolAdapter(com.energyict.mdc.upl.DeviceProtocol actual, CustomPropertySetInstantiatorService customPropertySetInstantiatorService) {
        this.actual = actual;
        this.customPropertySetInstantiatorService = customPropertySetInstantiatorService;
    }

    public static Services adapt(com.energyict.mdc.upl.DeviceProtocol actual) {
        return new Services(actual);
    }

    @Override
    public Class getActualClass() {
        return actual.getClass();
    }

    @Override
    public com.energyict.mdc.upl.DeviceProtocol getActual() {
        return actual;
    }

    @Override
    public void init(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, com.energyict.mdc.protocol.ComChannel comChannel) {
        this.actual.init(offlineDevice, comChannel);
    }

    @Override
    public void terminate() {
        actual.terminate();
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return actual.getDeviceProtocolCapabilities();
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return actual.getDeviceFunction();
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return actual.getManufacturerInformation();
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return actual.getCollectedCalendar();
    }

    @Override
    public List<? extends ConnectionType> getSupportedConnectionTypes() {
        return actual.getSupportedConnectionTypes()
                .stream()
                .map(connectionType -> new UPLConnectionTypeAdapter(connectionType, customPropertySetInstantiatorService))
                .collect(Collectors.toList());
    }

    @Override
    public List<UPLConnectionFunction> getProvidedConnectionFunctions() {
        return actual.getProvidedConnectionFunctions();
    }

    @Override
    public List<UPLConnectionFunction> getConsumableConnectionFunctions() {
        return actual.getConsumableConnectionFunctions();
    }

    @Override
    public void logOn() {
        actual.logOn();
    }

    @Override
    public void daisyChainedLogOn() {
        actual.daisyChainedLogOn();
    }

    @Override
    public void logOff() {
        actual.logOff();
    }

    @Override
    public void daisyChainedLogOff() {
        actual.daisyChainedLogOff();
    }

    @Override
    public String getSerialNumber() {
        return actual.getSerialNumber();
    }

    @Override
    public Date getTime() {
        return actual.getTime();
    }

    @Override
    public void setTime(Date timeToSet) {
        actual.setTime(timeToSet);
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return actual.getBreakerStatus();
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return actual.getDeviceCache();
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache actualCache) {
        actual.setDeviceCache(actualCache);
    }

    @Override
    public String getProtocolDescription() {
        return actual.getProtocolDescription();
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return actual.getFirmwareVersions();
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions(String serialNumber) {
        return actual.getFirmwareVersions(serialNumber);
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return actual.fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return actual.getLoadProfileData(loadProfiles);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return actual.getLogBookData(logBooks);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return actual.getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return actual.executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return actual.updateSentMessages(sentMessages);
    }

    @Override
    public String format(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return actual.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return actual.prepareMessageContext(device, offlineDevice, deviceMessage);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return actual.getDeviceProtocolDialects().stream().map(dialect -> new UPLDeviceProtocolDialectAdapter(dialect, customPropertySetInstantiatorService)).collect(Collectors.toList());
    }

    @Override
    public void addDeviceProtocolDialectProperties(com.energyict.mdc.upl.properties.TypedProperties dialectProperties) {
        actual.addDeviceProtocolDialectProperties(dialectProperties);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet actualSecurityPropertySet) {
        actual.setSecurityPropertySet(actualSecurityPropertySet);
    }

    @Override
    public Optional<com.energyict.mdc.upl.properties.PropertySpec> getClientSecurityPropertySpec() {
        return actual.getClientSecurityPropertySpec();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return actual.getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return actual.getEncryptionAccessLevels();
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return actual.readRegisters(registers);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return actual.getDeviceTopology();
    }

    @Override
    public String getVersion() {
        return actual.getVersion();
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        com.energyict.mdc.upl.properties.TypedProperties adaptedProperties = TypedPropertiesValueAdapter.adaptToUPLValues(properties);
        try {
            actual.setUPLProperties(adaptedProperties);
        } catch (PropertyValidationException e) {
            throw new NestedPropertyValidationException(e);
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return new ArrayList<>(this.actual.getUPLPropertySpecs().stream().map(UPLToConnexoPropertySpecAdapter::adaptTo).collect(Collectors.toList()));
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return new ArrayList<>(this.actual.getUPLPropertySpecs());
    }

    @Override
    public void setProtocolJournaling(ProtocolJournal protocolJournal) {
        this.actual.setProtocolJournaling(protocolJournal);
    }

    @Override
    public void journal(String message) {
        this.actual.journal(message);
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        com.energyict.mdc.upl.properties.TypedProperties adaptedProperties = TypedPropertiesValueAdapter.adaptToUPLValues(properties);
        actual.setUPLProperties(adaptedProperties);
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return actual.supportsCommunicationFirmwareVersion();
    }

    @Override
    public boolean supportsAuxiliaryFirmwareVersion() {
        return actual.supportsAuxiliaryFirmwareVersion();
    }

    @Override
    public boolean supportsCaConfigImageVersion() {
        return actual.supportsCaConfigImageVersion();
    }
        
    @Override
    public boolean firmwareSignatureCheckSupported() {
        return actual.firmwareSignatureCheckSupported();
    }

    @Override
    public boolean verifyFirmwareSignature(File firmwareFile, PublicKey pubKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException {
        return actual.verifyFirmwareSignature(firmwareFile, pubKey);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UPLDeviceProtocolAdapter) {
            return actual.equals(((UPLDeviceProtocolAdapter) obj).actual);
        } else {
            return actual.equals(obj);
        }
    }

    @Override
    public int hashCode() {
       return actual != null ? actual.hashCode() : 0;
    }

    public static final class Services {
        private final com.energyict.mdc.upl.DeviceProtocol actual;

        public Services(com.energyict.mdc.upl.DeviceProtocol actual) {
            this.actual = actual;
        }

        public UPLDeviceProtocolAdapter with(
                CustomPropertySetInstantiatorService customPropertySetInstantiatorService) {
            return new UPLDeviceProtocolAdapter(this.actual, customPropertySetInstantiatorService);
        }
    }
}