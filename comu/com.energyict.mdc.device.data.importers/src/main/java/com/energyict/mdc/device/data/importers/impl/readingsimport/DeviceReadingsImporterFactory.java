package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImporterFactory;
import com.energyict.mdc.device.data.importers.impl.DeviceDataCsvImporter;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.dynamic.PropertySpecService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DATE_FORMAT;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DELIMITER;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.NUMBER_FORMAT;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.TIME_ZONE;

@Component(name = "com.energyict.mdc.device.data.importers.impl.DeviceReadingsImporterFactory",
        service = FileImporterFactory.class,
        immediate = true)
public class DeviceReadingsImporterFactory extends AbstractDeviceDataFileImporterFactory {
    public static final String NAME = "DeviceReadingsImporterFactory";

    //OSGI
    public DeviceReadingsImporterFactory() {
        super();
    }

    // Test purpose
    @Inject
    public DeviceReadingsImporterFactory(NlsService nlsService, PropertySpecService propertySpecService) {
        super();
        setThesaurus(nlsService);
        setPropertySpecService(propertySpecService);
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        String delimiter = (String) properties.get(DELIMITER.getPropertyKey());
        String dateFormat = (String) properties.get(DATE_FORMAT.getPropertyKey());
        String timeZone = (String) properties.get(TIME_ZONE.getPropertyKey());
        SupportedNumberFormat numberFormat = ((SupportedNumberFormat.SupportedNumberFormatInfo) properties.get(NUMBER_FORMAT.getPropertyKey())).getFormat();

        DeviceReadingsImportParser parser = new DeviceReadingsImportParser(dateFormat, timeZone, numberFormat);
        DeviceReadingsImportProcessor processor = new DeviceReadingsImportProcessor();
        return new DeviceDataCsvImporter<DeviceReadingsImportRecord>(delimiter.charAt(0), parser, processor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.DEVICE_SHIPMENT_IMPORTER.getDefaultFormat();
    }

    @Override
    protected Set<DeviceDataImporterProperty> getProperties() {
        return EnumSet.of(DELIMITER, DATE_FORMAT, TIME_ZONE, NUMBER_FORMAT);
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
