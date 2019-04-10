package com.energyict.mdc.device.data.importers.impl.deviceeventsimport;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.energyict.mdc.device.data.importers.impl.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.*;
import static com.energyict.mdc.device.data.importers.impl.TranslationKeys.DEVICE_EVENTS_IMPORTER;


@Component(name = "com.energyict.mdc.device.data.importers.impl.DeviceEventsImporterFactory",
        service = FileImporterFactory.class,
        immediate = true)

public class DeviceEventsImporterFactory  extends AbstractDeviceDataFileImporterFactory {

    private volatile DeviceDataImporterContext context;
    public static final String NAME = "DeviceEventsImporterFactory";
    public DeviceEventsImporterFactory() {
    }

    @Inject
    public DeviceEventsImporterFactory(DeviceDataImporterContext context) {
        setDeviceDataImporterContext(context);
    }

    @Override
    protected Set<DeviceDataImporterProperty> getProperties() {
        return EnumSet.of(DELIMITER, DATE_FORMAT, TIME_ZONE);
    }

    @Override
    protected DeviceDataImporterContext getContext() {
        return context;
    }

    @Override
    @Reference
    public void setDeviceDataImporterContext(DeviceDataImporterContext context) {
    this.context = context;
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        String delimiter = (String) properties.get(DELIMITER.getPropertyKey());
        String dateFormat = (String) properties.get(DATE_FORMAT.getPropertyKey());
        String timeZone = (String) properties.get(TIME_ZONE.getPropertyKey());
        FileImportParser<DeviceEventsImportRecord> parser = new FileImportReadingsDescriptionBasedParser(
                new DeviceEventsImportDescription(dateFormat, timeZone));
        DeviceEventsImportProcessor processor = new DeviceEventsImportProcessor(getContext());
        FileImportLogger logger = new DeviceEventsImportLogger(getContext());
        return DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withLogger(logger).withDelimiter(delimiter.charAt(0)).build();
    }

    @Override
    public String getDisplayName() {
        return getContext().getThesaurus().getFormat(DEVICE_EVENTS_IMPORTER).format();
    }

    @Override
    public String getName() {
        return NAME;
    }

}
