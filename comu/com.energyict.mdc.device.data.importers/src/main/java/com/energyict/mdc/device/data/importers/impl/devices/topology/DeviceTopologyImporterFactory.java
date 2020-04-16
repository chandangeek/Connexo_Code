/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.topology;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImporterFactory;
import com.energyict.mdc.device.data.importers.impl.DeviceDataCsvImporter;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty;
import com.energyict.mdc.device.data.importers.impl.FileImportDescriptionBasedParser;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.FileImportParser;
import com.energyict.mdc.device.data.importers.impl.FileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.ALLOW_REASSIGNING;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DELIMITER;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DEVICE_IDENTIFIER;
import static com.energyict.mdc.device.data.importers.impl.TranslationKeys.DEVICE_TOPOLOGY_IMPORTER;

@Component(name = "com.energyict.mdc.device.data.importers." + DeviceTopologyImporterFactory.NAME,
        service = FileImporterFactory.class,
        immediate = true)
public class DeviceTopologyImporterFactory extends AbstractDeviceDataFileImporterFactory {
    public static final String NAME = "DeviceTopologyImporterFactory";
    public static final String DEVICE_IDENTIFIER_NAME = "Device name";
    public static final String DEVICE_IDENTIFIER_SERIAL = "Serial number";

    private volatile DeviceDataImporterContext context;

    public DeviceTopologyImporterFactory() {
    }

    @Inject
    public DeviceTopologyImporterFactory(DeviceDataImporterContext context) {
        this();
        setDeviceDataImporterContext(context);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return getContext().getThesaurus().getFormat(DEVICE_TOPOLOGY_IMPORTER).format();
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        String delimiter = (String) properties.get(DELIMITER.getPropertyKey());
        String deviceIdentifier = (String) properties.get(DEVICE_IDENTIFIER.getPropertyKey());
        Boolean allowReassigning = (Boolean) properties.get(ALLOW_REASSIGNING.getPropertyKey());

        FileImportParser<DeviceTopologyImportRecord> parser = new DeviceTopologyImportParser<>(
                new DeviceTopologyImportDescription());
        FileImportProcessor<DeviceTopologyImportRecord> processor = new DeviceTopologyImportProcessor(getContext(), deviceIdentifier, allowReassigning);
        FileImportLogger<FileImportRecord> logger = new DeviceTopologyImportLogger(getContext());
        return DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withLogger(logger).withDelimiter(delimiter.charAt(0)).build();
    }

    @Override
    protected Set<DeviceDataImporterProperty> getProperties() {
        return EnumSet.of(DELIMITER, DEVICE_IDENTIFIER, ALLOW_REASSIGNING);
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
