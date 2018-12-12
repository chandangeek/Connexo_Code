/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.impl.TranslationKeys;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
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

    private volatile Thesaurus thesaurus;
    private volatile CalendarService calendarService;
    private volatile Clock clock;

    public CalendarImporterFactory() {
    }

    @Inject
    public CalendarImporterFactory(NlsService nlsService,
                                   UserService userService,
                                   ThreadPrincipalService threadPrincipalService,
                                   CalendarService calendarService,
                                   Clock clock) {
        setNlsService(nlsService);
        setCalendarService(calendarService);
        setClock(clock);
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        return new CalendarImporter(thesaurus, calendarService, clock);
    }

    @Override
    public String getDisplayName() {
        return thesaurus.getFormat(TranslationKeys.CALENDAR_IMPORTER).format();
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

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return new ArrayList<>();
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(CalendarService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public final void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

}