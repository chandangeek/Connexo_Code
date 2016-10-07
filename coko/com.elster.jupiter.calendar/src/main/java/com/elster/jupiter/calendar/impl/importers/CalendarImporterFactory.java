/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.impl.TranslationKeys;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.properties.PropertySpec;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by igh on 27/04/2016.
 */
@Component(name = "com.elster.jupiter.calendar.importers.impl.CalendarImporterFactory",
        service = FileImporterFactory.class,
        immediate = true)
public class CalendarImporterFactory implements FileImporterFactory {

    public static final String NAME = "CalendarImporterFactory";

    private volatile CalendarImporterContext context;

    public CalendarImporterFactory() {
    }

    @Inject
    public CalendarImporterFactory(CalendarImporterContext context) {
        this();
        setCalendarImporterContext(context);
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        return new TimeOfUseCalendarImporter(context);
    }

    @Override
    public String getDisplayName() {
        return context.getThesaurus().getFormat(TranslationKeys.CALENDAR_IMPORTER).format();
    }

    @Override
    public String getDestinationName() {
        return CalendarImporterMessageHandler.DESTINATION_NAME;
    }

    @Override
    public String getApplicationName() {
        return "SYS";
    }

    @Override
    public void validateProperties(List<FileImporterProperty> properties) {
        // No properties to validate
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