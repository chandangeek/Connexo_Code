/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataFormatter;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.PropertySpecService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Map;

@Component(name = "com.elster.jupiter.export.processor.CsvEventWithDeviceCodeDataFormatterFactory",
        property = {DataExportService.DATA_TYPE_PROPERTY + "=" + DataExportService.STANDARD_EVENT_DATA_TYPE},
        service = DataFormatterFactory.class, immediate = true)
public class CsvEventWithDeviceCodeDataFormatterFactory extends AbstractCsvEventDataFormatterFactory {

    static final String NAME = "csvEventWithDeviceCodeDataProcessorFactory";

    //OSGI
    public CsvEventWithDeviceCodeDataFormatterFactory() {
    }

    // Tests
    @Inject
    public CsvEventWithDeviceCodeDataFormatterFactory(PropertySpecService propertySpecService, DataExportService dataExportService, NlsService nlsService) {
        super(propertySpecService, dataExportService, nlsService);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }

    @Reference
    public void setThesaurus(NlsService nlsService) {
        super.setThesaurus(nlsService);
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        super.setDataExportService(dataExportService);
    }

    @Override
    public DataFormatter createDataFormatter(Map<String, Object> properties) {
        return CsvEventWithDeviceCodeDataFormatter.from(dataExportService, getSeparator(properties), getTag(properties));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(Translations.Labels.CSV_EVENTS_WITH_DEVICE_CODES_FORMATTER).format();
    }
}