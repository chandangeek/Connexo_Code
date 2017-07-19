package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;


import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterMessageHandler;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This factory creates importers for the Secure
 */
@Component(name = "com.energyict.mdc.device.data.importers." + SecureDeviceShipmentImporterFactory.NAME,
        service = FileImporterFactory.class,
        immediate = true)
public class SecureDeviceShipmentImporterFactory implements FileImporterFactory {
    public static final String NAME = "SecureDeviceShipmentImporterFactory";
    private Thesaurus thesaurus;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        return new SecureDeviceShipmentImporter();
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
        return Collections.emptyList();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataImporterMessageHandler.COMPONENT, Layer.DOMAIN);

    }
}
