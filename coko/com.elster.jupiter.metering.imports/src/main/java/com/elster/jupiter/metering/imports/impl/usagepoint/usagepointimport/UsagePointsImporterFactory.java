package com.elster.jupiter.metering.imports.impl.usagepoint.usagepointimport;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.metering.imports.impl.usagepoint.*;
import com.elster.jupiter.metering.imports.impl.usagepoint.properties.SupportedNumberFormat;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.elster.jupiter.metering.imports.impl.usagepoint.DataImporterProperty.DATE_FORMAT;
import static com.elster.jupiter.metering.imports.impl.usagepoint.DataImporterProperty.DELIMITER;
import static com.elster.jupiter.metering.imports.impl.usagepoint.DataImporterProperty.NUMBER_FORMAT;
import static com.elster.jupiter.metering.imports.impl.usagepoint.DataImporterProperty.TIME_ZONE;
import static com.elster.jupiter.metering.imports.impl.usagepoint.Translations.Labels.USAGEPOINT_FILE_IMPORTER;

@Component(name = "com.elster.jupiter.metering.imports.impl.usagepoint.usagepointimport.UsagePointsFileImporterFactory",
        service = FileImporterFactory.class,
        immediate = true)
public class UsagePointsImporterFactory extends AbstractFileImporterFactory {

    public static final String NAME = "UsagePointFileImporterFactory";

    private volatile MeteringDataImporterContext context;

    @Activate
    public void activate(){
        System.err.println("STARTED!!!!!!!!!!!!");
    }

    public UsagePointsImporterFactory() {
    }

    @Inject
    public UsagePointsImporterFactory(MeteringDataImporterContext context) {
        setDeviceDataImporterContext(context);
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        String delimiter = (String) properties.get(DELIMITER.getPropertyKey());
        String dateFormat = (String) properties.get(DATE_FORMAT.getPropertyKey());
        String timeZone = (String) properties.get(TIME_ZONE.getPropertyKey());
        SupportedNumberFormat numberFormat = ((SupportedNumberFormat.SupportedNumberFormatInfo) properties.get(NUMBER_FORMAT.getPropertyKey())).getFormat();

        FileImportParser<UsagePointImportRecord> parser = new FileImportDescriptionBasedParser(
                new UsagePointImportDescription(dateFormat, timeZone, numberFormat, context),context);
        UsagePointsImportProcessor processor = new UsagePointsImportProcessor(getContext());
        FileImportLogger logger = new UsagePointsImportLogger(getContext());
        return CsvImporter.withParser(parser).withProcessor(processor).withLogger(logger).withDelimiter(delimiter.charAt(0)).build();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return getContext().getThesaurus()
                .getString(USAGEPOINT_FILE_IMPORTER.getKey(), USAGEPOINT_FILE_IMPORTER.getDefaultFormat());
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
    public void setDeviceDataImporterContext(MeteringDataImporterContext context) {
        this.context = context;
    }
}
