package com.elster.jupiter.calendar.importers.impl;

import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.calendar.impl.xmlbinding.Calendar;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileInputStream;
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
            com.elster.jupiter.calendar.Calendar calendar = factory.getCalendar(result);
            fileImportOccurrence.markSuccess(
                    context.getThesaurus().getFormat(TranslationKeys.TOU_CALENDAR_IMPORTED_SUCCESSFULLY).format(calendar.getName()));
        } catch (JAXBException e) {
            MessageSeeds.JAXB_FAILED.log(fileImportOccurrence.getLogger(), context.getThesaurus(), e);
            fileImportOccurrence.markFailure(e.getMessage());
        } catch (CalendarParserException e) {
            fileImportOccurrence.getLogger().severe(e.getLocalizedMessage());
            fileImportOccurrence.markFailure(e.getMessage());
        }
    }

    private void markFailure(FileImportOccurrence fileImportOccurrence) {
        fileImportOccurrence.markFailure(
                context.getThesaurus().getFormat(TranslationKeys.TOU_CALENDAR_IMPORT_FAILED).format());
    }

    private void markSuccess(FileImportOccurrence fileImportOccurrence, Calendar calendar) {
        fileImportOccurrence.markSuccess(
                context.getThesaurus().getFormat(TranslationKeys.TOU_CALENDAR_IMPORTED_SUCCESSFULLY).format(calendar.getName()));
    }

    private Schema getSchema() {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return sf.newSchema(new File(getClass().getClassLoader().getResource("calendar-import-format.xsd").toURI()));
        } catch (SAXException e) {
            throw new CalendarParserException(context.getThesaurus(), MessageSeeds.SCHEMA_FAILED, e);
        } catch (URISyntaxException e) {
            throw new CalendarParserException(context.getThesaurus(), MessageSeeds.SCHEMA_FAILED, e);
        }
    }
}
