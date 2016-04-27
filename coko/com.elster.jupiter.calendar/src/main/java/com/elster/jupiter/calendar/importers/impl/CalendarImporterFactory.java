package com.elster.jupiter.calendar.importers.impl;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.properties.PropertySpec;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by igh on 27/04/2016.
 */

@Component(name = "com.elster.jupiter.calendar.importers.impl.CalendarImporterFactory",
        service = CalendarImporterFactory.class,
        immediate = true)
public class CalendarImporterFactory implements FileImporterFactory {

    public static final String NAME = "CalendarImporterFactory";

    private volatile CalendarImporterContext context;

    @Inject
    public CalendarImporterFactory(CalendarImporterContext context) {
        setCalendarImporterContext(context);
    }


    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return context.getThesaurus()
                .getString(TranslationKeys.CALENDAR_IMPORTER.getKey(), TranslationKeys.CALENDAR_IMPORTER.getDefaultFormat());
    }

    @Override
    public String getDestinationName() {
        return null;
    }

    @Override
    public String getApplicationName() {
        return null;
    }

    @Override
    public void validateProperties(List<FileImporterProperty> properties) {

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Reference
    public void setCalendarImporterContext(CalendarImporterContext context) {
        this.context = context;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return new ArrayList<>();
    }


}
