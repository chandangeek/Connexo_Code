package com.elster.jupiter.calendar.importers.impl;

import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.calendar.impl.xmlbinding.Calendar;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.net.URISyntaxException;

/**
 * Created by igh on 27/04/2016.
 */
public class TimeOfUseCalendarImporter implements FileImporter {

    private CalendarImporterContext context;

    public TimeOfUseCalendarImporter(CalendarImporterContext context) {
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
            com.elster.jupiter.calendar.Calendar calendar = factory.getCalendar(result);
            markSuccess(fileImportOccurrence, calendar);
        } catch (JAXBException e) {
            log(fileImportOccurrence, (e.getLinkedException() != null) ? e.getLinkedException() : e);
            log(fileImportOccurrence, MessageSeeds.VALIDATION_OF_FILE_FAILED);
            markFailure(fileImportOccurrence, false);
        } catch (CalendarParserException e) {
            log(fileImportOccurrence, e);
            markFailure(fileImportOccurrence, true);
        } catch (Exception e) {
            log(fileImportOccurrence, e);
            markFailure(fileImportOccurrence, true);
        } catch (Throwable e) {
            log (fileImportOccurrence, e);
            markFailure(fileImportOccurrence, true);
        }
    }

    private void log(FileImportOccurrence fileImportOccurrence, Throwable e) {
        fileImportOccurrence.getLogger().severe(e.getLocalizedMessage());
    }

    private void log(FileImportOccurrence fileImportOccurrence, MessageSeeds messageSeeds) {
        messageSeeds.log(fileImportOccurrence.getLogger(), context.getThesaurus());
    }

    private void markFailure(FileImportOccurrence fileImportOccurrence, boolean valiationOk) {
        if (valiationOk) {
            fileImportOccurrence.markFailure(context.getThesaurus().getFormat(TranslationKeys.TOU_CALENDAR_IMPORT_FAILED_XML_OK).format());
        } else {
            fileImportOccurrence.markFailure(context.getThesaurus().getFormat(TranslationKeys.TOU_CALENDAR_IMPORT_FAILED_XML_NOT_OK).format());
        }
    }

    private void markSuccess(FileImportOccurrence fileImportOccurrence, com.elster.jupiter.calendar.Calendar calendar) {
        fileImportOccurrence.markSuccess(
                context.getThesaurus().getFormat(TranslationKeys.TOU_CALENDAR_IMPORTED_SUCCESSFULLY).format(calendar.getName()));
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
