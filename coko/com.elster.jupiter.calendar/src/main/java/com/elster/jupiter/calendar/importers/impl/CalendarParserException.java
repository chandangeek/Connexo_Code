package com.elster.jupiter.calendar.importers.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Created by igh on 27/04/2016.
 */
class CalendarParserException extends LocalizedException {

    CalendarParserException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    CalendarParserException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    CalendarParserException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause) {
        super(thesaurus, messageSeed, cause);
    }

}