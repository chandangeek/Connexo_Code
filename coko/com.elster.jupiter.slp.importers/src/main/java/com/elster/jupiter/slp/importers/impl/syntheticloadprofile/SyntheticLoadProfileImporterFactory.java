/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl.syntheticloadprofile;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.csvimport.FileImportLogger;
import com.elster.jupiter.fileimport.csvimport.FileImportParser;
import com.elster.jupiter.fileimport.csvimport.FileImportProcessor;
import com.elster.jupiter.fileimport.csvimport.FileImportRecord;
import com.elster.jupiter.slp.importers.impl.AbstractFileImporterFactory;
import com.elster.jupiter.slp.importers.impl.CsvImporter;
import com.elster.jupiter.slp.importers.impl.DataImporterProperty;
import com.elster.jupiter.slp.importers.impl.SyntheticLoadProfileDataImporterContext;
import com.elster.jupiter.slp.importers.impl.parsers.BigDecimalParser;
import com.elster.jupiter.slp.importers.impl.parsers.InstantParser;
import com.elster.jupiter.slp.importers.impl.properties.SupportedNumberFormat;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.elster.jupiter.slp.importers.impl.DataImporterProperty.DATE_FORMAT;
import static com.elster.jupiter.slp.importers.impl.DataImporterProperty.DELIMITER;
import static com.elster.jupiter.slp.importers.impl.DataImporterProperty.NUMBER_FORMAT;
import static com.elster.jupiter.slp.importers.impl.DataImporterProperty.TIME_ZONE;
import static com.elster.jupiter.slp.importers.impl.TranslationKeys.Labels.CORRECTION_FACTOR_FILE_IMPORTER;

@Component(name = "com.elster.jupiter.slp.importers.impl.syntheticloadprofile.SyntheticLoadProfileImporterFactory",
        service = FileImporterFactory.class,
        immediate = true)
public class SyntheticLoadProfileImporterFactory extends AbstractFileImporterFactory {

    public static final String NAME = "SyntheticLoadProfileImporterFactory";

    private volatile SyntheticLoadProfileDataImporterContext context;

    @SuppressWarnings("unused")
    public SyntheticLoadProfileImporterFactory() {
    }

    @Inject
    public SyntheticLoadProfileImporterFactory(SyntheticLoadProfileDataImporterContext context) {
        setSyntheticLoadProfileDataImporterContext(context);
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        String delimiter = (String) properties.get(DELIMITER.getPropertyKey());
        String dateFormat = (String) properties.get(DATE_FORMAT.getPropertyKey());
        String timeZone = (String) properties.get(TIME_ZONE.getPropertyKey());
        SupportedNumberFormat numberFormat = ((SupportedNumberFormat.SupportedNumberFormatInfo) properties.get(NUMBER_FORMAT.getPropertyKey())).getFormat();

        FileImportParser<SyntheticLoadProfileImportRecord> parser = new SyntheticLoadProfileParser(context, new InstantParser(dateFormat, timeZone), new BigDecimalParser(numberFormat));

        FileImportProcessor<SyntheticLoadProfileImportRecord> processor = new SyntheticLoadProfileImportProcessor(context);

        FileImportLogger<FileImportRecord> logger = new SyntheticLoadProfileImportLogger(getContext());
        return CsvImporter.withParser(parser)
                .withProcessor(processor)
                .withLogger(logger)
                .withDelimiter(delimiter.charAt(0))
                .build();
    }

    @Override
    public String getDisplayName() {
        return getContext().getThesaurus().getFormat(CORRECTION_FACTOR_FILE_IMPORTER).format();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getApplicationName() {
        return context.insightInstalled() ? "INS" : "MDC";
    }

    @Override
    protected Set<DataImporterProperty> getProperties() {
        return EnumSet.of(DELIMITER, DATE_FORMAT, TIME_ZONE, NUMBER_FORMAT);
    }

    @Override
    protected SyntheticLoadProfileDataImporterContext getContext() {
        return this.context;
    }

    @Override
    @Reference
    public void setSyntheticLoadProfileDataImporterContext(SyntheticLoadProfileDataImporterContext context) {
        this.context = context;
    }
}
