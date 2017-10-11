package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.KeypairWrapper;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterMessageHandler;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This factory creates importers for the Secure
 */
@Component(name = "com.energyict.mdc.device.data.importers." + SecureDeviceShipmentImporterFactory.NAME,
        service = FileImporterFactory.class,
        immediate = true)
public class SecureDeviceShipmentImporterFactory implements FileImporterFactory {
    public static final String NAME = "SecureDeviceShipmentImporterFactory";
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile PkiService pkiService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        TrustStore trustStore = (TrustStore) properties.get(SecureDeviceShipmentImporterProperty.TRUSTSTORE.getPropertyKey());
//        PublicKey publicKey = (PublicKey) properties.get(SecureDeviceShipmentImporterProperty.PUBLICKEY.getPropertyKey());
        return new SecureDeviceShipmentImporter(thesaurus, trustStore, deviceConfigurationService, deviceService, pkiService);
    }

    @Override
    public String getDisplayName() {
        return thesaurus.getFormat(TranslationKeys.SECURE_DEVICE_SHIPMENT_IMPORTER).format();
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
                .map(e -> e.getPropertySpec(propertySpecService, thesaurus, pkiService))
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
    public void setPkiService(PkiService pkiService) {
        this.pkiService = pkiService;
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
            public PropertySpec getPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus, PkiService pkiService) {
                return propertySpecService
                        .referenceSpec(TrustStore.class)
                        .named(this.getPropertyKey(), this.getNameTranslationKey())
                        .describedAs(this.getDescriptionTranslationKey())
                        .fromThesaurus(thesaurus)
                        .markExhaustive()
                        .addValues(pkiService.getAllTrustStores())
                        .finish();
            }
        },
        PUBLICKEY(TranslationKeys.DEVICE_DATA_IMPORTER_PUBLICKEY, TranslationKeys.DEVICE_DATA_IMPORTER_PUBLICKEY_DESCRIPTION) {
            @Override
            public PropertySpec getPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus, PkiService pkiService) {
                return propertySpecService
                        .referenceSpec(KeypairWrapper.class)
                        .named(this.getPropertyKey(), this.getNameTranslationKey())
                        .describedAs(this.getDescriptionTranslationKey())
                        .fromThesaurus(thesaurus)
                        .markExhaustive()
                        .addValues(pkiService.getAllKeyPairs())
                        .finish();
            }
        },
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

        public abstract PropertySpec getPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus, PkiService pkiService);

    }
}
