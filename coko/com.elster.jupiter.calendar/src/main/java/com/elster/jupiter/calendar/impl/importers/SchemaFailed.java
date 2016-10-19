package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import org.xml.sax.SAXException;

class SchemaFailed extends LocalizedException {
    SchemaFailed(Thesaurus thesaurus, SAXException e) {
        super(thesaurus, MessageSeeds.SCHEMA_FAILED, e);
    }
}
