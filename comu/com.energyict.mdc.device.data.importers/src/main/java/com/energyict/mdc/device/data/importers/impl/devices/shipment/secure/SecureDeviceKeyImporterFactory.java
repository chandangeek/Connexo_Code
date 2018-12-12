package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterMessageHandler;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This factory creates importers for the Secure
 */
@Component(name = "com.energyict.mdc.device.data.importers." + SecureDeviceKeyImporterFactory.NAME,
        service = FileImporterFactory.class,
        immediate = true)
public class SecureDeviceKeyImporterFactory implements FileImporterFactory {
    public static final String NAME = "SecureDeviceKeyImporterFactory";
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile SecurityManagementService securityManagementService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        TrustStore trustStore = (TrustStore) properties.get(SecureDeviceShipmentImporterProperty.TRUSTSTORE.getPropertyKey());
        return new SecureDeviceKeyImporter(thesaurus, trustStore, deviceConfigurationService, deviceService, securityManagementService);
    }

    @Override
    public String getDisplayName() {
        return thesaurus.getFormat(TranslationKeys.SECURE_DEVICE_KEY_IMPORTER).format();
    }

    @Override
    public String getDestinationName() {
        return SecureDeviceShipmentImporterMessageHandler.DESTINATION_NAME;
    }

    @Override
    public String getApplicationName() {
        return "MDC";
    }

    @Override
    public void validateProperties(List<FileImporterProperty> properties) {

    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Stream.of(SecureDeviceShipmentImporterProperty.values())
                .map(e -> e.getPropertySpec(propertySpecService, thesaurus, securityManagementService))
                .collect(toList());
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataImporterMessageHandler.COMPONENT, Layer.DOMAIN);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    static enum SecureDeviceShipmentImporterProperty {
        TRUSTSTORE(TranslationKeys.DEVICE_DATA_IMPORTER_TRUSTSTORE, TranslationKeys.DEVICE_DATA_IMPORTER_TRUSTSTORE_DESCRIPTION) {
            @Override
            public PropertySpec getPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus, SecurityManagementService securityManagementService) {
                return propertySpecService
                        .referenceSpec(TrustStore.class)
                        .named(this.getPropertyKey(), this.getNameTranslationKey())
                        .describedAs(this.getDescriptionTranslationKey())
                        .fromThesaurus(thesaurus)
                        .markExhaustive()
                        .addValues(securityManagementService.getAllTrustStores())
                        .markRequired()
                        .finish();
            }
        }
        ;
        private final TranslationKeys nameTranslationKey;
        private final TranslationKeys descriptionTranslationKey;

        SecureDeviceShipmentImporterProperty(TranslationKeys nameTranslationKey, TranslationKeys descriptionTranslationKey) {
            this.nameTranslationKey = nameTranslationKey;
            this.descriptionTranslationKey = descriptionTranslationKey;
        }

        public TranslationKeys getNameTranslationKey() {
            return nameTranslationKey;
        }

        public TranslationKeys getDescriptionTranslationKey() {
            return descriptionTranslationKey;
        }

        public String getPropertyKey() {
            return NAME + "." + this.nameTranslationKey.getKey();
        }

        public abstract PropertySpec getPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus, SecurityManagementService securityManagementService);

    }
}
