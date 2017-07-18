/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.commands.FileImportCommand;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Optional;

public class CalendarBuilder implements Builder<Calendar> {
    private static final String CALENDAR_IMPORTER_FACTORY = "CalendarImporterFactory";

    private final CalendarService calendarService;
    private final FileImportCommand fileImportCommand;

    private String mrid;
    private InputStream contentStream;

    @Inject
    public CalendarBuilder(CalendarService calendarService, FileImportCommand fileImportCommand) {
        this.calendarService = calendarService;
        this.fileImportCommand = fileImportCommand;
    }

    public CalendarBuilder withMrid(String mrid) {
        this.mrid = mrid;
        return this;
    }

    public CalendarBuilder withContentStream(InputStream contentStream) {
        this.contentStream = contentStream;
        return this;
    }

    @Override
    public Optional<Calendar> find() {
        return this.calendarService.findCalendarByMRID(this.mrid);
    }

    @Override
    public Calendar create() {
        this.fileImportCommand
                .useImporter(CALENDAR_IMPORTER_FACTORY)
                .content(this.contentStream)
                .run();
        return this.calendarService.findCalendarByMRID(this.mrid).orElseThrow(() -> new UnableToCreate("Failed to create calendar " + this.mrid));
    }
}
