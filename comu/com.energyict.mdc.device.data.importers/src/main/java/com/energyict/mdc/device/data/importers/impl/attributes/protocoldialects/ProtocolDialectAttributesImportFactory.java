/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes.protocoldialects;

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
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component(name = "com.energyict.mdc.device.data.importers.impl.attributes.protocoldialects." + ProtocolDialectAttributesImportFactory.NAME,
        service = FileImporterFactory.class,
        immediate = true)
public class ProtocolDialectAttributesImportFactory extends AbstractDeviceDataFileImporterFactory {
    public static final String NAME = "ProtocolDialectAttributesImportFactory";

    private volatile DeviceDataImporterContext context;

    public ProtocolDialectAttributesImportFactory() {
    }

    @Inject
    public ProtocolDialectAttributesImportFactory(DeviceDataImporterContext context) {
        super();
        setDeviceDataImporterContext(context);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return getContext().getThesaurus().getFormat(TranslationKeys.DEVICE_PROTOCOL_DIALECT_ATTRIBUTES_IMPORTER).format();
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        String delimiter = (String) properties.get(DeviceDataImporterProperty.DELIMITER.getPropertyKey());
        SupportedNumberFormat numberFormat = ((SupportedNumberFormat.SupportedNumberFormatInfo) properties.get(DeviceDataImporterProperty.NUMBER_FORMAT.getPropertyKey())).getFormat();

        FileImportParser<ProtocolDialectAttributesImportRecord> parser = new FileImportDescriptionBasedParser(new ProtocolDialectAttributesImportDescription());
        FileImportProcessor<ProtocolDialectAttributesImportRecord> processor = new ProtocolDialectAttributesImportProcessor(getContext(), numberFormat);
        FileImportLogger<FileImportRecord> logger = new DevicePerLineFileImportLogger(getContext());
        return DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withLogger(logger).withDelimiter(delimiter.charAt(0)).build();
    }

    @Override
    protected Set<DeviceDataImporterProperty> getProperties() {
        return EnumSet.of(DeviceDataImporterProperty.DELIMITER, DeviceDataImporterProperty.NUMBER_FORMAT);
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
