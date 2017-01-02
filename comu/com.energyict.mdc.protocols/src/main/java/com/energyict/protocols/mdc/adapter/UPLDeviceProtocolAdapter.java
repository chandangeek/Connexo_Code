package com.energyict.protocols.mdc.adapter;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
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
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.ProvisionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Adapter between a {@link com.energyict.mdc.upl.DeviceProtocol} and a {@link DeviceProtocol}.
 * <p>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 23/11/2016 - 16:56
 */
public class UPLDeviceProtocolAdapter extends AbstractUPLProtocolAdapter implements DeviceProtocol {

    private static final Logger LOGGER = Logger.getLogger(UPLDeviceProtocolAdapter.class.getName());
    private static final String MAPPING_PROPERTIES_FILE_NAME = "custom-property-set-mapping.properties";

    private static CustomPropertySetNameDetective customPropertySetNameDetective;

    /**
     * The UPL deviceProtocol instance {@link com.energyict.mdc.upl.DeviceProtocol} that needs to be wrapped (adapted)
     * so it is compliant with the Connexo DeviceProtocol interface: {@link DeviceProtocol}
     */
    private final com.energyict.mdc.upl.DeviceProtocol deviceProtocol;
    private final Thesaurus thesaurus;
    private final PropertySpecService mdcPropertySpecService;
    private final com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService;
    private final Injector injector;

    private UPLDeviceProtocolAdapter(com.energyict.mdc.upl.DeviceProtocol deviceProtocol, Thesaurus thesaurus, PropertySpecService mdcPropertySpecService, com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService) {
        this.deviceProtocol = deviceProtocol;
        this.thesaurus = thesaurus;
        this.mdcPropertySpecService = mdcPropertySpecService;
        this.jupiterPropertySpecService = jupiterPropertySpecService;
        this.injector = Guice.createInjector(this.getModule());
    }

    public static Services adapt(com.energyict.mdc.upl.DeviceProtocol deviceProtocol) {
        return new Services(deviceProtocol);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                this.bind(PropertySpecService.class).toInstance(mdcPropertySpecService);
                this.bind(com.elster.jupiter.properties.PropertySpecService.class).toInstance(jupiterPropertySpecService);
                this.bind(Thesaurus.class).toInstance(thesaurus);
            }
        };
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
    public List<DeviceMessageSpec> getSupportedMessages() {
        return deviceProtocol.getSupportedMessages();
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
    public String format(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return deviceProtocol.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public String prepareMessageContext(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return deviceProtocol.prepareMessageContext(offlineDevice, deviceMessage);
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
    public Optional<CustomPropertySet<Device, ? extends PersistentDomainExtension<Device>>> getCustomPropertySet() {
        this.ensureCustomPropertySetNameMappingLoaded();
        return Optional
                .ofNullable(customPropertySetNameDetective.customPropertySetClassNameFor(this.deviceProtocol.getClass()))
                .flatMap(this::loadClass)
                .map(this::toCustomPropertySet);
    }

    private CustomPropertySet toCustomPropertySet(Class cpsClass) {
        try {
            return (CustomPropertySet) this.injector.getInstance(cpsClass);
        } catch (ConfigurationException | ProvisionException e) {
            throw new UnableToCreateCustomPropertySet(e, cpsClass);
        }
    }

    private Optional<Class> loadClass(String className) {
        if (Checks.is(className).emptyOrOnlyWhiteSpace()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(this.getClass().getClassLoader().loadClass(className));
            } catch (ClassNotFoundException e) {
                throw new UnableToLoadCustomPropertySetClass(e, className);
            }
        }
    }

    private void ensureCustomPropertySetNameMappingLoaded() {
        if (customPropertySetNameDetective == null) {
            customPropertySetNameDetective = new CustomPropertySetNameDetective();
        }
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
        //TODO how to solve this clashing? Same for supported messages, it's on the CXO interface and on the UPL one ?????
        return null;
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

        UPLDeviceProtocolAdapter with(
                Thesaurus thesaurus,
                com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService,
                com.energyict.mdc.dynamic.PropertySpecService mdcPropertySpecService) {
            return new UPLDeviceProtocolAdapter(this.deviceProtocol, thesaurus, mdcPropertySpecService, jupiterPropertySpecService);
        }
    }

    private static class CustomPropertySetNameDetective {
        private final Map<String, String> customPropertySetClassNameMap = new ConcurrentHashMap<>();

        private CustomPropertySetNameDetective() {
            super();
            this.loadCustomPropertySetNameMapping();
        }

        private void loadCustomPropertySetNameMapping() {
            Properties mappings = new Properties();
            try (InputStream inputStream = this.getClass().getResourceAsStream(MAPPING_PROPERTIES_FILE_NAME)) {
                if (inputStream == null) {
                    LOGGER.severe("CustomPropertySetNameMapping properties file location is probably not correct " + MAPPING_PROPERTIES_FILE_NAME);
                    throw new IllegalArgumentException("CustomPropertySetNameMapping - Could not load the properties from " + MAPPING_PROPERTIES_FILE_NAME);
                }
                mappings.load(inputStream);
                mappings.entrySet().forEach(entry -> this.customPropertySetClassNameMap.put((String) entry.getKey(), (String) entry.getValue()));
            } catch (IOException e) {
                LOGGER.severe("Could not load the properties from " + MAPPING_PROPERTIES_FILE_NAME);
                throw new IllegalArgumentException("CustomPropertySetNameMapping - Could not load the properties from " + MAPPING_PROPERTIES_FILE_NAME);
            }
        }

        String customPropertySetClassNameFor(Class deviceProtocolClass) {
            /* Would be nice to use computeIfAbsent (especially because this is a ConcurrentHashMap.
             * However: the function that calculates the value if it is absent is a recursive call.
             * A ConcurrentHashMap deadlocks itself in that case. */
            String customPropertySetClassName = this.customPropertySetClassNameMap.get(deviceProtocolClass.getName());
            if (customPropertySetClassName == null) {
                return this.customPropertySetClassNameForSuperclass(deviceProtocolClass);
            } else if (customPropertySetClassName.startsWith("@")) {
                return this.customPropertySetClassNameForReferencedClass(deviceProtocolClass, customPropertySetClassName.substring(1));
            } else {
                return customPropertySetClassName;
            }
        }

        private String customPropertySetClassNameForSuperclass(Class deviceProtocolClass) {
            Class superclass = deviceProtocolClass.getSuperclass();
            if (superclass != null) {
                String customPropertyClassName = this.customPropertySetClassNameFor(superclass);
                // Cache the class name at this level of the class hierarchy
                this.customPropertySetClassNameMap.put(deviceProtocolClass.getName(), customPropertyClassName);
                return customPropertyClassName;
            } else {
                throw new IllegalArgumentException("Unable to determine custom property set class name for protocol class " + deviceProtocolClass.getName());
            }
        }

        private String customPropertySetClassNameForReferencedClass(Class deviceProtocolClass, String referencedClassName) {
            try {
                Class<?> referencedClass = Class.forName(referencedClassName);
                String customPropertyClassName = this.customPropertySetClassNameFor(referencedClass);
                // Cache the class name for the class that references another
                this.customPropertySetClassNameMap.put(deviceProtocolClass.getName(), customPropertyClassName);
                return customPropertyClassName;
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Unable to determine custom property set class name for protocol class " + deviceProtocolClass.getName() + " because referenced class " + referencedClassName + " could not be found", e);
            }
        }
    }

    private static class UnableToLoadCustomPropertySetClass extends RuntimeException {
        private UnableToLoadCustomPropertySetClass(ClassNotFoundException cause, String className) {
            super("Unable to load class " + className + " that was configured in mapping file " + MAPPING_PROPERTIES_FILE_NAME, cause);
        }
    }

    private static class UnableToCreateCustomPropertySet extends RuntimeException {
        private UnableToCreateCustomPropertySet(Throwable cause, Class cpsClass) {
            super("Unable to create instance of class " + cpsClass.getName() + " that was configured in mapping file " + MAPPING_PROPERTIES_FILE_NAME, cause);
        }
    }

}