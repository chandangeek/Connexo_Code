package com.energyict.mdc.device.data.importers.impl.devices.shipment;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImporterFactory;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.dynamic.PropertySpecService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DATE_FORMAT;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DELIMITER;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.TIME_ZONE;

@Component(name = "com.energyict.mdc.device.data.importers.DeviceShipmentImporterFactory",
        service = FileImporterFactory.class,
        immediate = true)
public class DeviceShipmentImporterFactory extends AbstractDeviceDataFileImporterFactory {

    public static final String NAME = "DeviceShipmentImporterFactory";

    // OSGI
    public DeviceShipmentImporterFactory() {
        super();
    }

    // Test purpose
    @Inject
    public DeviceShipmentImporterFactory(NlsService nlsService, PropertySpecService propertySpecService) {
        super();
        setThesaurus(nlsService);
        setPropertySpecService(propertySpecService);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        return null;
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.DEVICE_SHIPMENT_IMPORTER.getDefaultFormat();
    }

    @Override
    protected Set<DeviceDataImporterProperty> getProperties() {
        return EnumSet.of(DELIMITER, DATE_FORMAT, TIME_ZONE);
    }

    @Reference
    public final void setThesaurus(NlsService nlsService) {
        super.setThesaurus(nlsService);
    }

    @Reference
    public final void setPropertySpecService(PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }
}
