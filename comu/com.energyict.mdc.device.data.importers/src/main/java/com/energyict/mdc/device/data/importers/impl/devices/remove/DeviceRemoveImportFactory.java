package com.energyict.mdc.device.data.importers.impl.devices.remove;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImporterFactory;
import com.energyict.mdc.device.data.importers.impl.DeviceDataCsvImporter;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty;
import com.energyict.mdc.device.data.importers.impl.DevicePerLineFileImportLogger;
import com.energyict.mdc.device.data.importers.impl.FileImportDescriptionBasedParser;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.FileImportParser;
import com.energyict.mdc.device.data.importers.impl.FileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.FileImportRecord;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DELIMITER;
import static com.energyict.mdc.device.data.importers.impl.TranslationKeys.DEVICE_REMOVE_IMPORTER;

@Component(name = "com.energyict.mdc.device.data.importers." + DeviceRemoveImportFactory.NAME,
        service = FileImporterFactory.class,
        immediate = true)
public class DeviceRemoveImportFactory extends AbstractDeviceDataFileImporterFactory {
    public static final String NAME = "DeviceRemoveImportFactory";

    private volatile DeviceDataImporterContext context;

    public DeviceRemoveImportFactory() {
    }

    @Inject
    public DeviceRemoveImportFactory(DeviceDataImporterContext context) {
        super();
        setDeviceDataImporterContext(context);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return getContext().getThesaurus()
                .getString(DEVICE_REMOVE_IMPORTER.getKey(), DEVICE_REMOVE_IMPORTER.getDefaultFormat());
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        String delimiter = (String) properties.get(DELIMITER.getPropertyKey());

        FileImportParser<DeviceTransitionRecord> parser = new FileImportDescriptionBasedParser(new DeviceRemoveImportDescription());
        FileImportProcessor<DeviceTransitionRecord> processor = new DeviceRemoveImportProcessor(getContext());
        FileImportLogger<FileImportRecord> logger = new DevicePerLineFileImportLogger(getContext());
        return DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withLogger(logger).withDelimiter(delimiter.charAt(0)).build();
    }

    @Override
    protected Set<DeviceDataImporterProperty> getProperties() {
        return EnumSet.of(DELIMITER);
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
