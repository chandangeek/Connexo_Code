package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class MissingCalendarName extends LocalizedException {
    MissingCalendarName(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.MISSING_CALENDAR_NAME);
    }
}
