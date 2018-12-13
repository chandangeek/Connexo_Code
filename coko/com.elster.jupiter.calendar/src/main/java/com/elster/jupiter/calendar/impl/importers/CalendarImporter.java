/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.impl.MessageSeeds;
import com.elster.jupiter.calendar.impl.TranslationKeys;
import com.elster.jupiter.calendar.impl.xmlbinding.Calendars;
import com.elster.jupiter.calendar.impl.xmlbinding.XmlCalendar;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;

import org.xml.sax.SAXException;

import javax.validation.ConstraintViolationException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.time.Clock;

class CalendarImporter implements FileImporter {

    private final Thesaurus thesaurus;
    private final CalendarService calendarService;
    private final Clock clock;

    CalendarImporter(Thesaurus thesaurus, CalendarService calendarService, Clock clock) {
        this.thesaurus = thesaurus;
        this.calendarService = calendarService;
        this.clock = clock;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        try {
            Calendars xmlContents = getXmlContents(fileImportOccurrence);
            CalendarProcessor processor = new CalendarProcessor(calendarService, clock, thesaurus);
            log(fileImportOccurrence, MessageSeeds.VALIDATION_OF_FILE_SUCCEEDED);

            processor.addListener(new CalendarProcessor.ImportListener() {
                @Override
                public void created(String mrid) {
                    logCreation(fileImportOccurrence);
                }

                @Override
                public void updated(String mrid) {
                    logUpdate(fileImportOccurrence);
                }
            });

            processor.process(xmlContents);
            markSuccess(fileImportOccurrence);
        } catch (JAXBException e) {
            Throwable toLog = (e.getLinkedException() != null) ? e.getLinkedException() : e;
            String message = toLog.getLocalizedMessage();
            if ("Content is not allowed in prolog.".equals(message)) {
                throw new XmlValidationFailed(thesaurus, e);
            } else {
                throw new XmlValidationFailed(thesaurus, e, message);
            }
        } catch (ConstraintViolationException e) {
            new ExceptionLogFormatter(thesaurus, fileImportOccurrence.getLogger()).log(e);
            throw new RuntimeException(thesaurus.getFormat(TranslationKeys.CALENDAR_IMPORT_FAILED).format());
        }
    }

    Calendars getXmlContents(FileImportOccurrence fileImportOccurrence) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(XmlCalendar.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(getSchema());
        return unmarshall(unmarshaller, fileImportOccurrence.getContents());
    }

    private Calendars unmarshall(Unmarshaller unmarshaller, InputStream inputStream) throws JAXBException {
        return (Calendars) unmarshaller.unmarshal(inputStream);
    }

    private void logCreation(FileImportOccurrence fileImportOccurrence) {
        log(fileImportOccurrence, MessageSeeds.CALENDAR_CREATED);
    }

    private void logUpdate(FileImportOccurrence fileImportOccurrence) {
        log(fileImportOccurrence, MessageSeeds.CALENDAR_UPDATED);
    }

    private void log(FileImportOccurrence fileImportOccurrence, MessageSeeds messageSeeds) {
        messageSeeds.log(fileImportOccurrence.getLogger(), thesaurus);
    }

    private void markFailure(FileImportOccurrence fileImportOccurrence) {
        fileImportOccurrence.markFailure(thesaurus.getFormat(TranslationKeys.CALENDAR_IMPORT_FAILED).format());
    }

    private void markSuccess(FileImportOccurrence fileImportOccurrence) {
        fileImportOccurrence.markSuccess(
                thesaurus.getFormat(TranslationKeys.CALENDAR_IMPORTED_SUCCESSFULLY).format());
    }

    private Schema getSchema() {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return sf.newSchema(getClass().getClassLoader().getResource("calendar-import-format.xsd"));
        } catch (SAXException e) {
            throw new SchemaFailed(thesaurus, e);
        }
    }

}