/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.ImporterExtension;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterMessageHandler;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Component(name = "com.energyict.mdc.device.data.importers." + SecureHSMDeviceShipmentImporterFactory.NAME,
        service = FileImporterFactory.class,
        immediate = true)
public class SecureHSMDeviceShipmentImporterFactory implements FileImporterFactory {
    public static final String NAME = "SecureHSMDeviceShipmentImporterFactory";
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile SecurityManagementService securityManagementService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;
    private volatile Optional<ImporterExtension> importExtension = Optional.empty();
    private volatile HsmEnergyService hsmEnergyService;


    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        TrustStore trustStore = (TrustStore) properties.get(SecureHSMImporterProperties.TRUSTSTORE.getPropertyKey());

        ImporterProperties.ImporterPropertiesBuilder importerPropertiesBuilder = new ImporterProperties.ImporterPropertiesBuilder();


        ImporterProperties importerProperties = importerPropertiesBuilder.withThesaurus(thesaurus)
                .withDeviceConfigurationService(deviceConfigurationService)
                .withDeviceService(deviceService)
                .withImporterExtension(importExtension)
                .withSecurityManagementService(securityManagementService)
                .withTrustStore(trustStore)
                .withHsmEnergyService(hsmEnergyService)
                .build();
        return new  SecureHSMDeviceShipmentImporter(importerProperties);
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

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setImporterExtension(ImporterExtension importExtension) {
        this.importExtension = Optional.of(importExtension);
    }

    public void unsetImporterExtension(ImporterExtension importerExtension) {
        this.importExtension = Optional.empty();
    }

    @Reference
    public void setHsmEnergyService(HsmEnergyService hsmEnergyService) {
        this.hsmEnergyService = hsmEnergyService;
    }

    @Override
    public String getDisplayName() {
        return thesaurus.getFormat(TranslationKeys.SECURE_HSM_DEVICE_SHIPMENT_IMPORTER).format();
    }

    @Override
    public String getDestinationName() {
        return SecureHSMDeviceShipmentImporterMessageHandler.DESTINATION_NAME;
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
        return Stream.of(SecureHSMImporterProperties.values()).map(e -> e.getPropertySpec(propertySpecService, thesaurus, securityManagementService))
                .collect(toList());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
