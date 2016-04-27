package com.elster.jupiter.calendar.importers.impl;

import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;

/**
 * Created by igh on 27/04/2016.
 */
public class TimeOfUseCalendarImporter implements FileImporter {

    private Thesaurus thesaurus;

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        try {
            JAXBContext jc = JAXBContext.newInstance("com.elster.jupiter.calendar.importers.impl");
            Unmarshaller u = jc.createUnmarshaller();
            u.setSchema(getSchema());
            Object result = u.unmarshal(fileImportOccurrence.getContents());
        } catch (JAXBException e) {
            throw new CalendarParserException(thesaurus, MessageSeeds.JAXB_FAILED, e);
        }
    }

    private Schema getSchema() {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return sf.newSchema(new File("calendar-import-format.xsd"));
        } catch (SAXException e) {
            throw new CalendarParserException(thesaurus, MessageSeeds.SCHEMA_FAILED, e);
        }
    }
}
