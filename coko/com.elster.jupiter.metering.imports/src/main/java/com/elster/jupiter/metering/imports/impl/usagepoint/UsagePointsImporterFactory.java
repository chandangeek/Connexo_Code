/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.metering.imports.impl.AbstractFileImporterFactory;
import com.elster.jupiter.metering.imports.impl.CsvImporter;
import com.elster.jupiter.metering.imports.impl.DataImporterProperty;
import com.elster.jupiter.metering.imports.impl.FileImportDescriptionBasedParser;
import com.elster.jupiter.metering.imports.impl.FileImportLogger;
import com.elster.jupiter.metering.imports.impl.FileImportParser;
import com.elster.jupiter.metering.imports.impl.FileImportProcessor;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;
import com.elster.jupiter.metering.imports.impl.properties.SupportedNumberFormat;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.elster.jupiter.metering.imports.impl.DataImporterProperty.DATE_FORMAT;
import static com.elster.jupiter.metering.imports.impl.DataImporterProperty.DELIMITER;
import static com.elster.jupiter.metering.imports.impl.DataImporterProperty.NUMBER_FORMAT;
import static com.elster.jupiter.metering.imports.impl.DataImporterProperty.TIME_ZONE;
import static com.elster.jupiter.metering.imports.impl.TranslationKeys.Labels.USAGEPOINT_FILE_IMPORTER;

@Component(name = "com.elster.jupiter.metering.imports.impl.usagepoint.usagepointimport.UsagePointsFileImporterFactory",
        service = FileImporterFactory.class,
        immediate = true)
public class UsagePointsImporterFactory extends AbstractFileImporterFactory {

    public static final String NAME = "UsagePointFileImporterFactory";

    private volatile MeteringDataImporterContext context;

    public UsagePointsImporterFactory() {
    }

    @Inject
    public UsagePointsImporterFactory(MeteringDataImporterContext context) {
        setMeteringDataImporterContext(context);
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        String delimiter = (String) properties.get(DELIMITER.getPropertyKey());
        String dateFormat = (String) properties.get(DATE_FORMAT.getPropertyKey());
        String timeZone = (String) properties.get(TIME_ZONE.getPropertyKey());
        SupportedNumberFormat numberFormat = ((SupportedNumberFormat.SupportedNumberFormatInfo) properties.get(NUMBER_FORMAT
                .getPropertyKey())).getFormat();

        FileImportParser<UsagePointImportRecord> parser = new FileImportDescriptionBasedParser(
                new UsagePointImportDescription(dateFormat, timeZone, numberFormat, context), context);

        FileImportProcessor<UsagePointImportRecord> processor = context.insightInstalled()
                ? new UsagePointsImportProcessor(getContext(), dateFormat, timeZone)
                : new UsagePointsImportProcessorForMultisense(getContext());

        FileImportLogger logger = new UsagePointsImportLogger(getContext());
        return CsvImporter.withParser(parser)
                .withProcessor(processor)
                .withLogger(logger)
                .withDelimiter(delimiter.charAt(0))
                .build();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return getContext().getThesaurus().getFormat(USAGEPOINT_FILE_IMPORTER).format();
    }

    @Override
    protected Set<DataImporterProperty> getProperties() {
        return EnumSet.of(DELIMITER, DATE_FORMAT, TIME_ZONE, NUMBER_FORMAT);
    }

    @Override
    protected MeteringDataImporterContext getContext() {
        return this.context;
    }

    @Override
    @Reference
    public void setMeteringDataImporterContext(MeteringDataImporterContext context) {
        this.context = context;
    }

    @Override
    public String getApplicationName() {
        return context.insightInstalled() ? "INS" : "MDC";
    }
}
