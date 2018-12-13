/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.customattributes;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.pki.SecurityManagementService;
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
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
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
import static com.energyict.mdc.device.data.importers.impl.TranslationKeys.DEVICE_COUSTOM_ATTRIBUTES_IMPORTER;

@Component(name = "com.energyict.mdc.device.data.importers." + CustomAttributesImportFactory.NAME,
        service = FileImporterFactory.class,
        immediate = true)
public class CustomAttributesImportFactory extends AbstractDeviceDataFileImporterFactory {
    public static final String NAME = "CustomAttributesImportFactory";

    private volatile DeviceDataImporterContext context;
    private volatile SecurityManagementService securityManagementService;

    public CustomAttributesImportFactory() {
    }

    @Inject
    public CustomAttributesImportFactory(DeviceDataImporterContext context) {
        super();
        setDeviceDataImporterContext(context);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        String delimiter = (String) properties.get(DELIMITER.getPropertyKey());
        SupportedNumberFormat numberFormat = ((SupportedNumberFormat.SupportedNumberFormatInfo) properties.get(NUMBER_FORMAT.getPropertyKey())).getFormat();
        String dateFormat = (String) properties.get(DATE_FORMAT.getPropertyKey());
        String timeZone = (String) properties.get(TIME_ZONE.getPropertyKey());

        FileImportParser<CustomAttributesImportRecord> parser = new FileImportDescriptionBasedParser(new CustomAttributesImportDescription(dateFormat, timeZone, numberFormat, context));
        FileImportProcessor<CustomAttributesImportRecord> processor = new CustomAttributesImportProcessor(getContext(), delimiter);
        FileImportLogger<FileImportRecord> logger = new DevicePerLineFileImportLogger(getContext());
        return DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withLogger(logger).withDelimiter(delimiter.charAt(0)).build();
    }

    @Override
    public String getDisplayName() {
        return getContext().getThesaurus().getFormat(DEVICE_COUSTOM_ATTRIBUTES_IMPORTER).format();
    }

    @Override
    protected Set<DeviceDataImporterProperty> getProperties() {
        return EnumSet.of(DELIMITER, DATE_FORMAT, TIME_ZONE, NUMBER_FORMAT);
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

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }
}
