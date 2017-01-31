/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

@Component(name = "com.elster.jupiter.export.processor.CsvUsagePointDataFormatterFactory",
        property = {DataExportService.DATA_TYPE_PROPERTY + "=" + DataExportService.STANDARD_USAGE_POINT_DATA_TYPE},
        service = DataFormatterFactory.class,
        immediate = true)
public class CsvUsagePointDataFormatterFactory extends StandardCsvDataFormatterFactory {

    static final String NAME = "csvUsagePointDataProcessorFactory";

    //OSGI
    public CsvUsagePointDataFormatterFactory() {
    }

    // Tests
    @Inject
    public CsvUsagePointDataFormatterFactory(PropertySpecService propertySpecService, DataExportService dataExportService, NlsService nlsService) {
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
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(Translations.Labels.CSV_USAGEPOINT_DATA_FORMATTER).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                getTagProperty(),
                getSeparatorProperty()
        );
    }
}
