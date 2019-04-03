/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.loadprofilenextreading;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.energyict.mdc.device.data.importers.impl.*;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.*;
import static com.energyict.mdc.device.data.importers.impl.TranslationKeys.DEVICE_LOADPROFILE_NEXT_BLOCK_READINGS_IMPORTER;

@Component(name = "com.energyict.mdc.device.data.importers.impl.DeviceLoadProfileNextReadingImporterFactory",
        service = FileImporterFactory.class,
        immediate = true)
public class DeviceLoadProfileNextReadingImporterFactory extends AbstractDeviceDataFileImporterFactory {

    public static final String NAME = "DeviceLoadProfileNextReadingImporterFactory";

    private volatile DeviceDataImporterContext context;

    public DeviceLoadProfileNextReadingImporterFactory() {
    }

    @Inject
    public DeviceLoadProfileNextReadingImporterFactory(DeviceDataImporterContext context) {

        setDeviceDataImporterContext(context);
    }

    /**
     * Creates the actual importer using the actual values of importer properties.
     *
     * @param properties Represent the importer properties: Number Format (see {@link SupportedNumberFormat}),
     * Date Format, Time Zone and Delimiter, which separates the values in the file.
     */
    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        String delimiter = (String) properties.get(DELIMITER.getPropertyKey());
        String dateFormat = (String) properties.get(DATE_FORMAT.getPropertyKey());
        String timeZone = (String) properties.get(TIME_ZONE.getPropertyKey());

        FileImportParser<DeviceLoadProfileNextReadingRecord> parser = new FileImportReadingsDescriptionBasedParser(
                new DeviceLoadProfileNextReadingDescription(dateFormat, timeZone));
        DeviceLoadProfileNextReadingImportProcessor processor = new DeviceLoadProfileNextReadingImportProcessor(getContext());
        FileImportLogger logger = new DevicePerLineFileImportLogger(getContext());
        return DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withLogger(logger).withDelimiter(delimiter.charAt(0)).build();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return getContext().getThesaurus().getFormat(DEVICE_LOADPROFILE_NEXT_BLOCK_READINGS_IMPORTER).format();
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
