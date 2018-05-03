/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.imports.impl.properties.SupportedNumberFormat;
import com.elster.jupiter.metering.imports.impl.properties.UsagePointReadingImportProperties;
import com.elster.jupiter.metering.imports.impl.usagepoint.UsagePointReadingImporter;
import com.elster.jupiter.properties.PropertySpec;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.metering.imports.impl.DataImporterProperty.DATE_FORMAT;
import static com.elster.jupiter.metering.imports.impl.DataImporterProperty.DELIMITER;
import static com.elster.jupiter.metering.imports.impl.DataImporterProperty.NUMBER_FORMAT;
import static com.elster.jupiter.metering.imports.impl.DataImporterProperty.TIME_ZONE;
import static com.elster.jupiter.metering.imports.impl.TranslationKeys.Labels.USAGEPOINT_READING_IMPORTER;


@Component(name = "com.elster.jupiter.metering.imports.impl.UsagePointReadingImporterFactory",
        service = FileImporterFactory.class,
        immediate = true)
public class UsagePointReadingImporterFactory implements FileImporterFactory {

    static final String NAME = "UsgPointReadingImportFact";
    private volatile MeteringDataImporterContext context;
    private volatile DataAggregationService dataAggregationService;

    public UsagePointReadingImporterFactory() {
    }

    @Inject
    public UsagePointReadingImporterFactory(MeteringDataImporterContext context, DataAggregationService dataAggregationService) {
        setMeteringDataImporterContext(context);
        setDataAggregationService(dataAggregationService);
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        String delimiter = (String) properties.get(DELIMITER.getPropertyKey());
        String dateFormat = (String) properties.get(DATE_FORMAT.getPropertyKey());
        String timeZone = (String) properties.get(TIME_ZONE.getPropertyKey());
        SupportedNumberFormat numberFormat = ((SupportedNumberFormat.SupportedNumberFormatInfo) properties.get(NUMBER_FORMAT
                .getPropertyKey())).getFormat();
        UsagePointReadingImportProperties usagePointReadingImportProperties = new UsagePointReadingImportProperties.UsagePointReadingImportPropertiesBuilder()
                .withDataAggregServ(dataAggregationService)
                .withMeteringDataImpContext(context)
                .withDateFormat(dateFormat)
                .withDelimiter(delimiter)
                .withNumberFormat(numberFormat)
                .withTimeZone(timeZone)
                .build();
        return new UsagePointReadingImporter(usagePointReadingImportProperties);
    }

    @Override
    public String getDisplayName() {
        return getContext().getThesaurus().getFormat(USAGEPOINT_READING_IMPORTER).format();
    }

    @Override
    public String getDestinationName() {
        return UsagePointReadingMessageHandlerFactory.DESTINATION_NAME;
    }

    @Override
    public String getApplicationName() {
        return "INS";
    }

    @Override
    public void validateProperties(List<FileImporterProperty> properties) {
        getProperties()
                .stream()
                .forEach(property -> property.validateProperties(properties, getContext()));
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return getProperties()
                .stream()
                .map(property -> property.getPropertySpec(getContext()))
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return NAME;
    }

    private Set<DataImporterProperty> getProperties() {
        return EnumSet.of(DELIMITER, DATE_FORMAT, TIME_ZONE, NUMBER_FORMAT);
    }

    @Reference
    public void setMeteringDataImporterContext(MeteringDataImporterContext context) {
        this.context = context;
    }

    @Reference
    private void setDataAggregationService(DataAggregationService dataAggregationService) {
        this.dataAggregationService = dataAggregationService;
    }

    public MeteringDataImporterContext getContext() {
        return context;
    }
}
