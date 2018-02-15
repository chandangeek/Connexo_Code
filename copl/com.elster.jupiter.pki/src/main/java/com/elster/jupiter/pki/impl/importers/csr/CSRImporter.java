/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.TranslationKeys;

import org.xml.sax.SAXException;

import javax.validation.ConstraintViolationException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.time.Clock;
import java.util.Map;

class CSRImporter implements FileImporter {

    private final Thesaurus thesaurus;
    private final Map<String, Object> properties;
    private final Clock clock;

    CSRImporter(Thesaurus thesaurus, Map<String, Object> properties, Clock clock) {
        this.thesaurus = thesaurus;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        try {
            Calendars xmlContents = getXmlContents(fileImportOccurrence);
            CSRProcessor processor = new CSRProcessor(calendarService, clock, thesaurus);
            log(fileImportOccurrence, MessageSeeds.VALIDATION_OF_FILE_SUCCEEDED);

            processor.addListener(new CSRProcessor.ImportListener() {
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
            throw new RuntimeException(thesaurus.getFormat(TranslationKeys.CSR_IMPORT_FAILED).format());
        }
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
        fileImportOccurrence.markFailure(thesaurus.getFormat(TranslationKeys.CSR_IMPORT_FAILED).format());
    }

    private void markSuccess(FileImportOccurrence fileImportOccurrence) {
        fileImportOccurrence.markSuccess(thesaurus.getFormat(TranslationKeys.CSR_IMPORT_SUCCESS).format());
    }
}
