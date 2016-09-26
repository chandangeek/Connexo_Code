/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;

import org.xml.sax.SAXException;

import javax.validation.ConstraintViolationException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * Created by igh on 27/04/2016.
 */
class TimeOfUseCalendarImporter implements FileImporter {

    private CalendarImporterContext context;

    TimeOfUseCalendarImporter(CalendarImporterContext context) {
        this.context = context;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        try {
            JAXBContext jc = JAXBContext.newInstance(com.elster.jupiter.calendar.impl.xmlbinding.Calendar.class);
            Unmarshaller u = jc.createUnmarshaller();
            u.setSchema(getSchema());
            CalendarFactory factory = new CalendarFactory(context.getCalendarService(), context.getThesaurus());
            com.elster.jupiter.calendar.impl.xmlbinding.Calendar result =
                    (com.elster.jupiter.calendar.impl.xmlbinding.Calendar) u.unmarshal(fileImportOccurrence.getContents());
            log(fileImportOccurrence, MessageSeeds.VALIDATION_OF_FILE_SUCCEEDED);
            boolean isUpdate = context.getCalendarService().findCalendarByMRID(result.getMRID()).isPresent();
            com.elster.jupiter.calendar.Calendar calendar = factory.getCalendar(result);
            logCreationOrUpdate(fileImportOccurrence, isUpdate);
            markSuccess(fileImportOccurrence);
        } catch (JAXBException e) {
            logValidationFailed(fileImportOccurrence, e);
            markFailure(fileImportOccurrence);
        } catch (CalendarParserException e) {
            logImportFailed(fileImportOccurrence, e);
            markFailure(fileImportOccurrence);
        } catch (ConstraintViolationException e) {
            new ExceptionLogFormatter(context.getThesaurus(), fileImportOccurrence.getLogger()).log(e);
            throw new RuntimeException(context.getThesaurus().getFormat(TranslationKeys.CALENDAR_IMPORT_FAILED).format());
        }
    }

    private void logCreationOrUpdate(FileImportOccurrence fileImportOccurrence, boolean isUpdate) {
        if (isUpdate) {
            log(fileImportOccurrence, MessageSeeds.CALENDAR_UPDATED);
        } else {
            log(fileImportOccurrence, MessageSeeds.CALENDAR_CREATED);
        }
    }

    private void logValidationFailed(FileImportOccurrence fileImportOccurrence, JAXBException e) {
        Throwable toLog = (e.getLinkedException() != null) ? e.getLinkedException() : e;
        String message = toLog.getLocalizedMessage();
        if ("Content is not allowed in prolog.".equals(message)) {
            MessageSeeds.VALIDATION_OF_FILE_FAILED.log(fileImportOccurrence.getLogger(), context.getThesaurus());
        } else {
            MessageSeeds.VALIDATION_OF_FILE_FAILED_WITH_DETAIL.log(fileImportOccurrence.getLogger(), context.getThesaurus(), message);
        }
    }

    private void logImportFailed(FileImportOccurrence fileImportOccurrence, Throwable e) {
        fileImportOccurrence.getLogger().severe(e.getLocalizedMessage());
    }

    private void log(FileImportOccurrence fileImportOccurrence, MessageSeeds messageSeeds) {
        messageSeeds.log(fileImportOccurrence.getLogger(), context.getThesaurus());
    }

    private void markFailure(FileImportOccurrence fileImportOccurrence) {
        fileImportOccurrence.markFailure(context.getThesaurus().getFormat(TranslationKeys.CALENDAR_IMPORT_FAILED).format());
    }

    private void markSuccess(FileImportOccurrence fileImportOccurrence) {
        fileImportOccurrence.markSuccess(
                context.getThesaurus().getFormat(TranslationKeys.CALENDAR_IMPORTED_SUCCESSFULLY).format());
    }

    private Schema getSchema() {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return sf.newSchema(getClass().getClassLoader().getResource("calendar-import-format.xsd"));
        } catch (SAXException e) {
            throw new CalendarParserException(context.getThesaurus(), MessageSeeds.SCHEMA_FAILED, e);
        }
    }

}