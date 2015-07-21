package com.energyict.mdc.device.data.importers.impl.devices.shipment;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImporterFactory;
import com.energyict.mdc.device.data.importers.impl.DeviceDataCsvImporter;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty;
import com.energyict.mdc.device.data.importers.impl.FileImportDescriptionBasedParser;
import com.energyict.mdc.device.data.importers.impl.FileImportParser;
import com.energyict.mdc.device.data.importers.impl.FileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
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

    private volatile DeviceDataImporterContext context;

    public DeviceShipmentImporterFactory() {}

    @Inject
    public DeviceShipmentImporterFactory(DeviceDataImporterContext context) {
        super();
        setDeviceDataImporterContext(context);
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
    public FileImporter createImporter(Map<String, Object> properties) {
        String delimiter = (String) properties.get(DELIMITER.getPropertyKey());
        String dateFormat = (String) properties.get(DATE_FORMAT.getPropertyKey());
        String timeZone = (String) properties.get(TIME_ZONE.getPropertyKey());

        FileImportParser<DeviceShipmentImportRecord> parser = new FileImportDescriptionBasedParser(
                new DeviceShipmentImportDescription(dateFormat, timeZone));
        FileImportProcessor<DeviceShipmentImportRecord> processor = new DeviceShipmentImportProcessor();
        return DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withDelimiter(delimiter.charAt(0)).build(getContext());
    }

    @Override
    protected Set<DeviceDataImporterProperty> getProperties() {
        return EnumSet.of(DELIMITER, DATE_FORMAT, TIME_ZONE);
    }

    @Override
    protected DeviceDataImporterContext getContext() {
        return this.context;
    }

    @Override
    @Reference
    public void setDeviceDataImporterContext(DeviceDataImporterContext context) {
        this.context = context;
    }
}
